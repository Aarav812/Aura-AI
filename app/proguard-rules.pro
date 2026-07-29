# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.aura.ai.**$$serializer { *; }
-keepclassmembers class com.aura.ai.** {
    *** Companion;
}
-keepclasseswithmembers class com.aura.ai.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
