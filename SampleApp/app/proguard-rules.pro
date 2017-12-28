# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/lp-eng-ggn-097/Developer/dev-kits/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

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
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keep class com.fasterxml.jackson.annotation.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepattributes Annotation,EnclosingMethod,Signature
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep class org.codehaus.* { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
-dontwarn com.fasterxml.jackson.databind.**
