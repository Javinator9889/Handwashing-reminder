# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-optimizationpasses 4

# https://github.com/mikepenz/Android-Iconics#proguard
-keep class .R
-keep class **.R$* {
    <fields>;
}

-keep class com.javinator9889.handwashingreminder.ads.AdLoaderImpl {
    com.javinator9889.handwashingreminder.ads.AdLoaderImpl$Provider Provider;
}

-keep class com.javinator9889.handwashingreminder.ads.AdLoaderImpl$Provider {
    *;
}

-keep class com.javinator9889.handwashingreminder.okhttp.OkHttpDownloader {
    com.javinator9889.handwashingreminder.okhttp.OkHttpDownloader$Provider Provider;
}

-keep class com.javinator9889.handwashingreminder.okhttp.OkHttpDownloader$Provider {
    *;
}

-keep class com.javinator9889.handwashingreminder.okhttplegacy.OkHttpDownloader {
    com.javinator9889.handwashingreminder.okhttplegacy.OkHttpDownloader$Provider Provider;
}

-keep class com.javinator9889.handwashingreminder.okhttplegacy.OkHttpDownloader$Provider {
    *;
}

-keep class com.javinator9889.handwashingreminder.bundledemoji.BundledEmojiConfig {
    *;
}

# https://bumptech.github.io/glide/doc/download-setup.html#proguard
# https://github.com/bumptech/glide/blob/master/library/proguard-rules.txt
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
# Klaxon
-keep public class kotlin.reflect.jvm.internal.impl.** { public *; }
-keep class com.beust.klaxon.** { *; }
-keep interface com.beust.klaxon.** { *; }
-keep class kotlin.Metadata { *; }

#data models
-keep class com.javinator9889.handwashingreminder.collections.** { *;}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
