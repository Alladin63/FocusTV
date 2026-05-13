# MaX TV ProGuard Rules

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembernames class * { @javax.inject.Inject *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Retrofit + OkHttp
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson / JSON
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# DTOs réseau (garder les noms de champs pour désérialisation)
-keep class com.alladin63.maxtv.core.data.remote.dto.** { *; }

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# WorkManager
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Navigation Compose
-keep class androidx.navigation.** { *; }

# Timber
-dontwarn org.jetbrains.annotations.**

# Domain models (pas d'obfuscation pour les data classes)
-keep class com.alladin63.maxtv.core.domain.model.** { *; }

# Règles générales
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-dontwarn java.lang.invoke.**
-dontwarn **$$Lambda$*
