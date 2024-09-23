
cmake_minimum_required(VERSION 3.10.2)

# lame
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/module/mp3/lame)
aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR}/module/mp3/lame SRC_LAME)

#[[set(SRC_LAME ${CMAKE_SOURCE_DIR}/module/mp3/lame/bitstream.c
        encoder.c
        fft.c
        gain_analysis.c
        id3tag.c
        lame.c
        lame.rc
        mpglib_interface.c
        newmdct.c
        presets.c
        psymodel.c
        quantize.c
        quantize_pvt.c
        reservoir.c
        set_get.c
        tables.c
        takehiro.c
        util.c
        vbrquantize.c
        VbrTag.c
        version.c)]]

add_library(
        nativelib
        SHARED

        ${SRC_LAME}
        utils/logger.cpp
        module/yuv/yuv.cpp
        module/mp3/mp3.cpp
        proxy/proxy_yuv.cpp
        proxy/proxy_mp3.cpp
        nativelib.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        nativelib
        ${log-lib})