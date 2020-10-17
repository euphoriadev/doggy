-dontwarn com.squareup.okhttp.**
-dontwarn org.apache.**

# keep everything in this package from being removed or renamed
-keep class ru.euphoria.doggy.api.model.** { *; }
-keep class ru.euphoria.doggy.db.** { *; }
-keep class com.android.vending.billing.**
-keepnames class ru.euphoria.doggy.api.model.** { *; }
-keepnames class ru.euphoria.doggy.db.** { *; }

-keep class org.sqlite.database.** { *; }
-keepnames class org.sqlite.database.** { *; }

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

