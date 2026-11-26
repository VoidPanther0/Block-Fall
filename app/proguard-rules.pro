# R8 optimization & hardening rules for Block Falls release builds

# Clean crash stack trace mapping without revealing original source paths
-keepattributes SourceFile,LineNumberTable,Signature,RuntimeVisibleAnnotations,AnnotationDefault,EnclosingMethod,InnerClasses
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

# Keep all project packages (game, ui, data, ads, security) intact to prevent release crashes
-keep class com.example.game.** { *; }
-keep class com.example.ui.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.ads.** { *; }
-keep class com.example.security.** { *; }
-keep class com.example.MainActivity { *; }

# kotlinx.serialization (required for type-safe @Serializable Navigation Compose routes & DataStore models)
-keepattributes *Annotation*,InnerClasses,Signature
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.**$$serializer { *; }
-keepclassmembers class com.example.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.** {
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
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# AdMob / Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep ViewModel / AndroidViewModel reflective constructors used by ViewModelProvider
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
