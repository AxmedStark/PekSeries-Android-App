# ============================================================================
# PekSeries — R8 configuration (release only)
#
# Module-owned rules live in each module's consumer-rules.pro and travel with
# the module. This file holds app-wide concerns only.
# ============================================================================

# --- Crash reports -----------------------------------------------------------
# Without these, every release stack trace is unreadable line noise.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Reflection metadata -----------------------------------------------------
# Signature is required for any generic type read reflectively: Retrofit's
# suspend return types and every Gson TypeToken depend on it.
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# --- Kotlin ------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Coroutines' internal service loader and debug probes.
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# --- Hilt / Dagger -----------------------------------------------------------
# The Hilt Gradle plugin contributes most of this; these cover the generated
# components and the entry points resolved by name at runtime.
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}
-dontwarn dagger.hilt.**

# --- Firebase ----------------------------------------------------------------
# Firestore deserialises POJOs reflectively, so any class handed to toObject()
# or toObjects() must keep its field names and no-arg constructor.
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-keepnames class az.pekstudios.pekseries.** extends java.lang.Enum

# PekNotification is nested in SeriesRepository and read back via toObjects().
-keep class az.pekstudios.pekseries.core.network.repository.SeriesRepository$PekNotification {
    <init>(...);
    <fields>;
}

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Timber ------------------------------------------------------------------
-dontwarn org.jetbrains.annotations.**

# --- Chucker (release uses the no-op artifact) -------------------------------
-dontwarn com.chuckerteam.chucker.**
