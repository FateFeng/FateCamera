# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_C_COMPILER_WORKS TRUE)
set(CMAKE_CXX_COMPILER_WORKS TRUE)

set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fstrict-aliasing -fprefetch-loop-arrays  -g  -wall  ")

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

set(USB_INCLUDE ${CMAKE_CURRENT_SOURCE_DIR}/../../
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/os
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../
        ${PROJECT_SOURCE_DIR}/libuvc/include
        ${PROJECT_SOURCE_DIR}/libuvc/include/libuvc
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../include
        ${CMAKE_CURRENT_SOURCE_DIR}/../../android)
include_directories(${USB_INCLUDE})

set(USB_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/core.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/descriptor.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/hotplug.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/io.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/sync.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/strerror.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/os/android_usbfs.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/os/poll_posix.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/os/threads_posix.c
        ${CMAKE_CURRENT_SOURCE_DIR}/../../libusb/os/android_netlink.c)

add_library( # Sets the name of the library.
        usb100

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${USB_SOURCES})

#指定第三方库路径
#[[set_target_properties(jpeg
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/libjpeg-turbo1500.so )]]



find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        usb100

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

