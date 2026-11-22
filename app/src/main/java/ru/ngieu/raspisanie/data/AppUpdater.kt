package ru.ngieu.raspisanie.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.ngieu.raspisanie.BuildConfig
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAssetDto> = emptyList(),
)

@Serializable
data class GitHubAssetDto(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = "",
    val size: Long = 0L,
)

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val tagName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val releaseNotes: String,
)

enum class InstallLaunchResult {
    LAUNCHED,
    PERMISSION_REQUIRED,
}

object AppUpdateParser {
    private val versionRegex = Regex("""v?(\d+)\.(\d+)\.(\d+)""")

    fun parseRelease(
        rawJson: String,
        currentVersionCode: Int,
        currentVersionName: String,
    ): AppUpdateInfo? {
        val dto = ScheduleParser.json.decodeFromString<GitHubReleaseDto>(rawJson)
        val apkAsset = dto.assets.firstOrNull {
            it.name.endsWith(".apk", ignoreCase = true) && it.browserDownloadUrl.isNotBlank()
        } ?: return null

        val remoteVersionName = dto.tagName.trim().removePrefix("v").ifBlank { apkAsset.name }
        val remoteBuild = extractBuildNumber(dto.tagName) ?: extractBuildNumber(apkAsset.name) ?: 0

        val isNewer = when {
            remoteBuild > 0 && currentVersionCode > 0 -> remoteBuild > currentVersionCode
            else -> compareSemver(remoteVersionName, currentVersionName) > 0
        }
        if (!isNewer) return null

        return AppUpdateInfo(
            versionCode = remoteBuild,
            versionName = remoteVersionName,
            tagName = dto.tagName.ifBlank { "v$remoteVersionName" },
            downloadUrl = apkAsset.browserDownloadUrl,
            sizeBytes = apkAsset.size,
            releaseNotes = dto.body.trim(),
        )
    }

    fun extractBuildNumber(text: String): Int? {
        val match = versionRegex.find(text) ?: return null
        return match.groupValues[3].toIntOrNull()
    }

    fun compareSemver(a: String, b: String): Int {
        val matchA = versionRegex.find(a)
        val matchB = versionRegex.find(b)
        if (matchA == null || matchB == null) return a.compareTo(b)
        for (i in 1..3) {
            val partA = matchA.groupValues[i].toIntOrNull() ?: 0
            val partB = matchB.groupValues[i].toIntOrNull() ?: 0
            if (partA != partB) return partA.compareTo(partB)
        }
        return 0
    }
}

class AppUpdater(
    private val latestReleaseUrl: String = GITHUB_LATEST_RELEASE_URL,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun checkForUpdate(): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(latestReleaseUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "NgieuRaspisanie/${BuildConfig.VERSION_NAME}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("GitHub вернул код ${response.code}")
                }
                val body = response.body?.string()?.takeIf { it.isNotBlank() }
                    ?: throw IOException("Пустой ответ от сервера обновлений")
                AppUpdateParser.parseRelease(
                    rawJson = body,
                    currentVersionCode = BuildConfig.VERSION_CODE,
                    currentVersionName = BuildConfig.VERSION_NAME,
                )
            }
        }
    }

    suspend fun downloadApk(
        context: Context,
        info: AppUpdateInfo,
        onProgress: (Float) -> Unit,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val tempFile = File(updatesDir, "ngieu-update.apk.tmp")
            val finalFile = File(updatesDir, "ngieu-update-${info.versionName}.apk")
            updatesDir.listFiles()?.forEach { existing ->
                if (existing.name != finalFile.name) existing.delete()
            }

            val request = Request.Builder()
                .url(info.downloadUrl)
                .header("User-Agent", "NgieuRaspisanie/${BuildConfig.VERSION_NAME}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Не удалось скачать APK (код ${response.code})")
                }
                val responseBody = response.body ?: throw IOException("Пустой файл обновления")
                val totalBytes = responseBody.contentLength().takeIf { it > 0 } ?: info.sizeBytes

                responseBody.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var downloaded = 0L
                        var lastReported = -1f
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (totalBytes > 0) {
                                val progress = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                if (progress - lastReported >= 0.02f || progress >= 0.999f) {
                                    lastReported = progress
                                    onProgress(progress)
                                }
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (finalFile.exists()) finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }
            onProgress(1f)
            finalFile
        }
    }

    fun launchInstaller(context: Context, apkFile: File): InstallLaunchResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
            return InstallLaunchResult.PERMISSION_REQUIRED
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
        return InstallLaunchResult.LAUNCHED
    }

    companion object {
        const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/xisscag-ops/raspisanie/releases/latest"
    }
}
