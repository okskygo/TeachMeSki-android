# TeachMeSki ProGuard / R8 rules
#
# Release builds enable R8 (minify + resource shrinking). Any library that relies
# on reflection, annotations processed at runtime, or generated companion classes
# must be preserved here.

# --- Keep line numbers for Crashlytics stack traces (source file reference) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization ---
# Keep @Serializable classes and their generated $Companion / $serializer members.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class <1>$$serializer { *; }
# Keep all @Serializable classes in our own package.
-keep @kotlinx.serialization.Serializable class com.teachmeski.app.** { *; }
-keepclassmembers class com.teachmeski.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.teachmeski.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Supabase / Ktor ---
# Supabase Kotlin relies heavily on kotlinx.serialization; Ktor uses reflection
# on the HTTP engine and plugins. Keep serializer classes and all Ktor engines.
-keep class io.ktor.** { *; }
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**

# OkHttp (Ktor's engine).
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Hilt / Dagger ---
# The Hilt Gradle plugin ships its own consumer rules, but keep ViewModel and
# generated @HiltAndroidApp entry points to be safe.
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# --- Firebase ---
# Firebase SDKs ship consumer rules; keep Crashlytics mapping references.
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Google Play Billing ---
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# --- Coil ---
-dontwarn coil3.**

# --- Kotlin metadata (needed by reflection-based libs) ---
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# --- Our own data / DTO classes ---
# All classes used by Supabase Postgrest / kotlinx.serialization across the app.
-keep class com.teachmeski.app.data.** { *; }
-keep class com.teachmeski.app.iap.** { *; }

# --- Suppress warnings for optional deps ---
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
