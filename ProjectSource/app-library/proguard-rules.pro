# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Glide

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Kayako Android SDK

-keep class com.kayako.sdk.android.k5.** { *; }
-dontwarn com.kayako.sdk.android.k5.**
-keepattributes InnerClasses

# Android

-keep class android.support.v4.** { *; }

# Phoenix client library

-keepnames class org.phoenixframework.channels.** { *; }
-keep public class org.phoenixframework.channels.** {
  public *** get*();
  public void set*(***);
}
