# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all classes in com.example to prevent R8 from stripping or obfuscating our code,
# ensuring maximum runtime stability for database entities, serialization, and ViewModels.
-keep class com.example.** { *; }

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Prevent obfuscation of iTextG and Vico charts
-keep class com.itextpdf.** { *; }
-keep class com.patrykandpatrick.vico.** { *; }

# iText uses optional signing/encryption features which reference SpongyCastle and XML signature.
# We do not use these features, so we can safely ignore those optional classes:
-dontwarn javax.xml.crypto.**
-dontwarn org.apache.jcp.xml.dsig.internal.dom.**
-dontwarn org.apache.xml.security.utils.**
-dontwarn org.spongycastle.**
-dontwarn java.awt.**


