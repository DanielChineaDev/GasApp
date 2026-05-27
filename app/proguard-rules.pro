# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.**

-keepclassmembers class com.bpo.gasapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.bpo.gasapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bpo.gasapp.**$$serializer { *; }

# --- Models (defensive; usados con serialización / Firestore) ---
-keep class com.bpo.gasapp.domain.model.** { *; }
-keep class com.bpo.gasapp.data.remote.dto.** { *; }

# --- Retrofit / OkHttp / Coroutines ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn kotlinx.coroutines.**

# El resto de librerías (Firebase, Glance, ML Kit, Maps, Room, Hilt) incluyen
# sus propias reglas de consumidor, por lo que no hacen falta keeps extra.
