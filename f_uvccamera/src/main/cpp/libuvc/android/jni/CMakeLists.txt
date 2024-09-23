# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_C_COMPILER_WORKS TRUE)
set(CMAKE_CXX_COMPILER_WORKS TRUE)
set(LOCAL_ALLOW_UNDEFINED_SYMBOLS true)

add_definitions("-DANDROID_NDK")
add_definitions("-DLOG_NDEBUG")
add_definitions("-DUVC_DEBUGGING")

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

#添加子目录，将自动找到子目录中的CMakeLists.txt
MESSAGE("MSG ff ： " ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libjpeg-turbo1500.so)

set(UVC_INCLUDE ${PROJECT_SOURCE_DIR}/libuvc/../
        ${PROJECT_SOURCE_DIR}/libusb
        ${PROJECT_SOURCE_DIR}/libjpeg-turbo-1.5.0
        ${PROJECT_SOURCE_DIR}/libjpeg-turbo-1.5.0/include
        ${PROJECT_SOURCE_DIR}/libuvc/include
        ${PROJECT_SOURCE_DIR}/libuvc/include/libuvc)
include_directories(${UVC_INCLUDE})

set(UVC_SOURCES ${PROJECT_SOURCE_DIR}/libuvc/src/ctrl.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/device.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/diag.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/frame.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/frame-mjpeg.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/init.c
        ${PROJECT_SOURCE_DIR}/libuvc/src/stream.c)

add_library( # Sets the name of the library.
        uvc

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${UVC_SOURCES})


#添加第三方动态库
add_library(jpeg-turbo
        SHARED
        IMPORTED)
#指定第三方库路径
set_target_properties(jpeg-turbo
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libjpeg-turbo1500.so)

#[[find_library(UACAudio
        ${PROJECT_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI}/libUACAudio.so)]]

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        uvc

        jpeg-turbo
        usb100
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
