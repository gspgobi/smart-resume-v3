# Strip verbose/debug/info logs in release; keep warn/error for crash tracking
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# iTextPDF 5.x uses reflection to instantiate image codec classes (BMP, GIF, JPEG, etc.)
# inside Image.getInstance(). R8 renaming those classes causes NoSuchMethodException at runtime.
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Gson data classes used in SplashViewModel for parsing example resumes JSON
-keep class com.nithra.nithraresume.ui.splash.Example* { *; }
-keepclassmembers class com.nithra.nithraresume.ui.splash.Example* {
    <fields>;
    <init>(...);
}
