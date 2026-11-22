package ru.ngieu.raspisanie.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NgieuApiClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchStudentGroupsRaw(): String = get("$baseUrl/api/v2/Actors/Get?isStudent=true")

    suspend fun fetchScheduleRaw(actorId: String): String {
        val encoded = URLEncoder.encode(actorId, Charsets.UTF_8.name())
        return get("$baseUrl/api/v2/Schedule/Get?actorId=$encoded")
    }

    suspend fun fetchWeekTypeRaw(date: LocalDate): String {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return get("$baseUrl/api/v2/WeekType/Get?date=$dateStr")
    }

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Сервер НГИЭУ вернул код ${response.code}")
            }
            response.body?.string()?.takeIf { it.isNotBlank() }
                ?: throw IOException("Пустой ответ от сервера расписания")
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://230352-2.vm.clodo.ru"
    }
}
