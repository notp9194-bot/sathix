-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.sathix.app.domain.model.** { *; }
-keep class com.sathix.app.data.local.entity.** { *; }
-keep class com.sathix.app.data.remote.dto.** { *; }

-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

-keep class io.socket.** { *; }
-keep class io.engineio.** { *; }
-dontwarn io.socket.**

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.database.PropertyName <fields>;
}

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
