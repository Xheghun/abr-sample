package com.example.mediaplayerprep.player

data class CustomCodecInfo(
    val mimeType: String,
    val codecName: String,
    val isNativeLibraryLoaded: Boolean,
    val probeResult: String
) {
    val summary: String
        get() = if (isNativeLibraryLoaded) "$codecName handles $mimeType ($probeResult)" else "Native codec unavailable: $probeResult"
}

object CustomCodecRegistry {
    const val DemoMimeType = "video/x-mediaprep-rle"

    fun probe(): CustomCodecInfo {
        if (!NativeCodecBridge.isLoaded) {
            return CustomCodecInfo(
                mimeType = DemoMimeType,
                codecName = "unavailable",
                isNativeLibraryLoaded = false,
                probeResult = NativeCodecBridge.loadError ?: "library did not load"
            )
        }

        val decodedPixels = NativeCodecBridge.decodeProbe(
            encoded = byteArrayOf(16, 127),
            width = 4,
            height = 4
        )
        val result = when (decodedPixels) {
            16 -> "probe decoded 16 pixels"
            -1 -> "invalid input"
            -2 -> "decoded more pixels than expected"
            -3 -> "incomplete frame"
            else -> "unexpected result $decodedPixels"
        }

        return CustomCodecInfo(
            mimeType = DemoMimeType,
            codecName = NativeCodecBridge.codecName(),
            isNativeLibraryLoaded = true,
            probeResult = result
        )
    }
}

object NativeCodecBridge {
    val isLoaded: Boolean
    val loadError: String?

    init {
        var loaded = false
        var error: String? = null
        try {
            System.loadLibrary("mediaprep_custom_codec")
            loaded = true
        } catch (throwable: UnsatisfiedLinkError) {
            error = throwable.message
        }
        isLoaded = loaded
        loadError = error
    }

    fun codecName(): String = if (isLoaded) nativeCodecName() else "unavailable"

    fun decodeProbe(encoded: ByteArray, width: Int, height: Int): Int =
        if (isLoaded) nativeDecodeProbe(encoded, width, height) else -1

    private external fun nativeCodecName(): String
    private external fun nativeDecodeProbe(encoded: ByteArray, width: Int, height: Int): Int
}
