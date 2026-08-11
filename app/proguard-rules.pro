# ========================================
# Xixi Kitchen - ProGuard / R8 Rules
# ========================================

# ---- Basic ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses,EnclosingMethod

# ========================================
# App code — keep EVERYTHING
# ========================================
-keep class com.xixikitchen.jetpack.** { *; }
-keepclassmembers class com.xixikitchen.jetpack.** { *; }

# ========================================
# Jetpack Compose — keep metadata needed at runtime
# ========================================
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <methods>;
    @androidx.compose.runtime.Immutable <methods>;
}
-dontwarn androidx.compose.**

# ========================================
# Dagger / Hilt
# ========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.**

# ========================================
# Gson (Retrofit converter)
# ========================================
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-dontwarn com.google.gson.**

# ========================================
# Retrofit + OkHttp
# ========================================
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ========================================
# Coil (image loading)
# ========================================
-keep class coil.** { *; }
-dontwarn coil.**

# ========================================
# JPush / JCore
# ========================================
-keep class cn.jpush.** { *; }
-keep class cn.jiguang.** { *; }
-dontwarn cn.jpush.**
-dontwarn cn.jiguang.**

# ========================================
# Kotlin + Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ========================================
# DataStore / Security
# ========================================
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ========================================
# Navigation
# ========================================
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ========================================
# Lifecycle
# ========================================
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-dontwarn androidx.lifecycle.**

# ========================================
# Activity
# ========================================
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends android.app.Activity { *; }
