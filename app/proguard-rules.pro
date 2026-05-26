# Strip verbose/debug/info logs in release; keep warn/error for crash tracking
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
