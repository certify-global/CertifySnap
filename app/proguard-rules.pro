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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
    #
    #############################################
    -optimizationpasses 5
        -dontskipnonpubliclibraryclasses


        -verbose

        -dontskipnonpubliclibraryclassmembers

        -dontpreverify

        -keepattributes *Annotation*,InnerClasses

        -keepattributes Signature

        -keepattributes SourceFile,LineNumberTable

        -renamesourcefileattribute SourceFile

        -optimizations !code/simplification/cast,!field/*,!class/merging/*

         #############################################
            #
            #
            #############################################

            -keep public class * extends android.app.Activity
            -keep public class * extends android.app.Appliction
            -keep public class * extends android.app.Service
            -keep public class * extends android.content.BroadcastReceiver
            -keep public class * extends android.content.ContentProvider
            -keep public class * extends android.app.backup.BackupAgentHelper
            -keep public class * extends android.preference.Preference
            -keep public class * extends android.view.View
            -keep public class com.android.vending.licensing.ILicensingService

                -keep class android.support.** {*;}

                -keep public class * extends android.support.v4.**
                -keep public class * extends android.support.v7.**
                -keep public class * extends android.support.annotation.**

                -keep class **.R$* {*;}

                -keepclasseswithmembernames class * {
                    native <methods>;
                }

                -keepclassmembers class * extends android.app.Activity{
                    public void *(android.view.View);
                }

                -keepclassmembers enum * {
                    public static **[] values();
                    public static ** valueOf(java.lang.String);
                }

                -keep public class * extends android.view.View{
                    *** get*();
                    void set*(***);
                    public <init>(android.content.Context);
                    public <init>(android.content.Context, android.util.AttributeSet);
                    public <init>(android.content.Context, android.util.AttributeSet, int);
                }

                -keep class * implements android.os.Parcelable {
                    public static final android.os.Parcelable$Creator *;
                }

                -keepclassmembers class * implements java.io.Serializable {
                    static final long serialVersionUID;
                    private static final java.io.ObjectStreamField[] serialPersistentFields;
                    !static !transient <fields>;
                    !private <fields>;
                    !private <methods>;
                    private void writeObject(java.io.ObjectOutputStream);
                    private void readObject(java.io.ObjectInputStream);
                    java.lang.Object writeReplace();
                    java.lang.Object readResolve();
                }

                -keepclassmembers class * {
                    void *(**On*Event);
                    void *(**On*Listener);
                }

                -keepclassmembers class fqcn.of.javascript.interface.for.webview {
                    public *;
                }
                -keepclassmembers class * extends android.webkit.webViewClient {
                    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
                    public boolean *(android.webkit.WebView, java.lang.String);
                }
                -keepclassmembers class * extends android.webkit.webViewClient {
                    public void *(android.webkit.webView, jav.lang.String);
                }

                    -keep public class com.certify.snap.bean.**{*;}

                     -keep public class com.certify.snap.model.**{*;}


                                            # AndroidEventBus
                                            -keep class org.simple.** { *; }
                                            -keep interface org.simple.** { *; }
                                            -keepclassmembers class * {
                                                @org.simple.eventbus.Subscriber <methods>;
                                            }

    # Bugly
    -dontwarn com.tencent.bugly.**
    -keep class com.tencent.bugly.** { *; }

      -keep class com.arcsoft.face.** { *; }

      -keep class com.arcsoft.imageutil.** { *; }

      -keep class com.google.zxing.** { *; }

      -keep class com.telpo.tps550.api.** { *; }

       -keep class com.example.a950jnisdk.** { *; }

       -keep class com.common.thermalimage.** {*;}

       -libraryjars libs/pushservice-6.5.0.75.jar
            -dontwarn com.baidu.**
            -keep class com.baidu.**{*; }

       -libraryjars libs/poi-3.12-android-a.jar
            -dontwarn org.apache.**
            -keep class org.apache.**{*; }
            -keep class aavax.xml.**{*; }
            -keep class com.bea.**{*; }
            -keep class repackage.**{*; }
            -keep class schemaorg_apache_xmlbeans.**{*; }

       -libraryjars libs/poi-ooxml-schemas-3.12-20150511-a.jar
            -dontwarn org.openxmlformats.schemas.**
            -keep class org.openxmlformats.schemas.**{*; }

       -keepattributes Signature

       # For using GSON @Expose annotation
       -keepattributes *Annotation*

       # Gson specific classes
       -keep class sun.misc.Unsafe { *; }
       -keep class com.google.gson.stream.** { *; }

             # Fresco
             -keep class com.facebook.fresco.** {*;}
             -keep interface com.facebook.fresco.** {*;}
             -keep enum com.facebook.fresco.** {*;}

              # Glide
                 -keep public class * implements com.bumptech.glide.module.GlideModule
                 -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
                   **[] $VALUES;
                   public *;
                 }


                 # OkHttp3
                     -dontwarn com.squareup.okhttp3.**
                     -keep class com.squareup.okhttp3.** { *;}
                     -dontwarn okio.**

                       # Retrofit
                         -dontwarn retrofit2.**
                         -keep class retrofit2.** { *; }
                         -keepattributes Signature
                         -keepattributes Exceptions

                             # RxJava RxAndroid
                             -dontwarn sun.misc.**
                             -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
                                 long producerIndex;
                                 long consumerIndex;
                             }
                             -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
                                 rx.internal.util.atomic.LinkedQueueNode producerNode;
                             }
                             -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
                                 rx.internal.util.atomic.LinkedQueueNode consumerNode;
                             }

                             -keep class com.tamic.novate.** {*;}

                             -keep class org.litepal.** {
                                 *;
                             }

                             -keep class * extends org.litepal.crud.DataSupport {
                                 *;
                             }

                             -keep class * extends org.litepal.crud.LitePalSupport {
                                 *;
                             }