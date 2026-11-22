# R8 optimization & hardening rules for Block Falls release builds

# Clean crash stack trace mapping without revealing original source paths
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Strip verbose, debug, and info logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int d(...);
}

# Preserve classes and members annotated with @Keep
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# kotlinx.serialization (required for type-safe @Serializable Navigation Compose routes)
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.ui.navigation.**$$serializer { *; }
-keepclassmembers class com.example.ui.navigation.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.ui.navigation.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Jetpack DataStore & Preferences serialization safety
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Coroutines fast path & internal exception handling
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Firebase Firestore custom class mapping & Play Integrity safety
-keepclassmembers class com.example.data.cloud.** { *; }
-keepnames class com.example.data.cloud.**
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

