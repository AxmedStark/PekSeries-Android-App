# ============================================================================
# :core:network — consumer R8 rules
# ============================================================================

# --- Wire DTOs ---------------------------------------------------------------
# Same reasoning as :core:model — TMDB responses are matched by field name
# (poster_path, first_air_date, vote_average, external_ids ...). Renaming any of
# them makes Gson return an object with every field null.
-keep class az.pekstudios.pekseries.core.network.remote.** {
    <init>(...);
    <fields>;
    <methods>;
}

# --- Retrofit ----------------------------------------------------------------
# Service interfaces are implemented by a runtime proxy, so the interface, its
# annotations and its generic signatures must all survive.
-keep,allowobfuscation interface az.pekstudios.pekseries.core.network.remote.TmdbApi
-keep,allowobfuscation interface az.pekstudios.pekseries.core.network.remote.TvMazeApi

-keepattributes Signature, Exceptions, *Annotation*

# Retrofit ships its own rules, but suspend functions rely on Continuation
# surviving with its generic signature intact.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class retrofit2.adapter.rxjava2.Result

-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Gson --------------------------------------------------------------------
-dontwarn sun.misc.**
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
