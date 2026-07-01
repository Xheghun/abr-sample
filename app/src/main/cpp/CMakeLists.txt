cmake_minimum_required(VERSION 3.22.1)

project(mediaprep_custom_codec)

add_library(
    mediaprep_custom_codec
    SHARED
    native_codec_bridge.cpp
)

target_link_libraries(
    mediaprep_custom_codec
    android
    log
)
