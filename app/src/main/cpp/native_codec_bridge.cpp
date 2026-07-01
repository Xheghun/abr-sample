#include <jni.h>
#include <string>
#include <vector>

namespace {
constexpr const char *kCodecName = "mediaprep.rle.video.decoder";

// Demo decoder for a toy RLE grayscale frame format:
// pairs of [runLength, luma]. A production decoder would parse a real bitstream,
// output frames to a native buffer, and be driven by a custom Media3 renderer.
int DecodeRlePixelCount(const jbyte *encoded, jsize length, int width, int height) {
    if (encoded == nullptr || length <= 0 || width <= 0 || height <= 0) {
        return -1;
    }

    const int expected_pixels = width * height;
    int decoded_pixels = 0;
    for (jsize i = 0; i + 1 < length; i += 2) {
        const auto run_length = static_cast<unsigned char>(encoded[i]);
        decoded_pixels += run_length;
        if (decoded_pixels > expected_pixels) {
            return -2;
        }
    }

    return decoded_pixels == expected_pixels ? decoded_pixels : -3;
}
}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_mediaplayerprep_player_NativeCodecBridge_nativeCodecName(
    JNIEnv *env,
    jobject /* thiz */
) {
    return env->NewStringUTF(kCodecName);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_mediaplayerprep_player_NativeCodecBridge_nativeDecodeProbe(
    JNIEnv *env,
    jobject /* thiz */,
    jbyteArray encoded,
    jint width,
    jint height
) {
    if (encoded == nullptr) {
        return -1;
    }

    const jsize length = env->GetArrayLength(encoded);
    std::vector<jbyte> bytes(static_cast<size_t>(length));
    env->GetByteArrayRegion(encoded, 0, length, bytes.data());

    return DecodeRlePixelCount(bytes.data(), length, width, height);
}
