# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_C_COMPILER_WORKS TRUE)
set(CMAKE_CXX_COMPILER_WORKS TRUE)
set(LOCAL_ALLOW_UNDEFINED_SYMBOLS true)

set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fstrict-aliasing -g -Werror")

add_definitions("-DANDROID_NDK")
add_definitions("-DLOG_NDEBUG")
add_definitions("-DACCESS_RAW_DESCRIPTORS")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#set(CMAKE_CXX_FLAGS "-Wno-error=format-security -Wno-error=pointer-sign")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../../../jnilibs/${ANDROID_ABI})

#设置头文件搜索路径（和此txt同个路径的头文件无需设置），可选
#INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/common)

#指定用到的系统库或者NDK库或者第三方库的搜索路径，可选。
#LINK_DIRECTORIES(/usr/local/lib)

set(UVCCamera_INCLUDE ${CMAKE_CURRENT_SOURCE_DIR}/
        ${CMAKE_CURRENT_SOURCE_DIR}/../
        ${PROJECT_SOURCE_DIR}/libusb/
        ${PROJECT_SOURCE_DIR}/libuvc/include
        ${PROJECT_SOURCE_DIR}/libuvc/include/libuvc
        ${CMAKE_CURRENT_SOURCE_DIR}/../rapidjson/include)
include_directories(${UVCCamera_INCLUDE})


set(UVCCamera_SOURCES _onload.cpp
        utilbase.cpp
        UVCCamera.cpp
        UVCPreview.cpp
        UVCButtonCallback.cpp
        UVCStatusCallback.cpp
        Parameters.cpp
        com_ifeng_f_uvccamera_uvc_UVCCamera.cpp)

add_library( # Sets the name of the library.
        UVCCamera

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${UVCCamera_SOURCES})

#[[find_library(UACAudio
        ${PROJECT_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libUACAudio.so)]]

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        UVCCamera

        android
        OpenSLES
        uvc
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
