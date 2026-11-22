# Keep kotlinx.serialization serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ru.ngieu.raspisanie.data.**$$serializer { *; }
-keepclassmembers class ru.ngieu.raspisanie.data.** {
    *** Companion;
}
-keepclasseswithmembers class ru.ngieu.raspisanie.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
