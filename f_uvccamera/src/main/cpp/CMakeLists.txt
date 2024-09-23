# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_C_COMPILER_WORKS TRUE)
set(CMAKE_CXX_COMPILER_WORKS TRUE)

# Declares and names the project.

project("f_uvccamera")

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

ADD_SUBDIRECTORY(${PROJECT_SOURCE_DIR}/libusb/android/jni)
ADD_SUBDIRECTORY(${PROJECT_SOURCE_DIR}/libuvc/android/jni)
ADD_SUBDIRECTORY(${PROJECT_SOURCE_DIR}/UVCCamera)
ADD_SUBDIRECTORY(${PROJECT_SOURCE_DIR}/libyuv)

#[[
add_library( # Sets the name of the library.
        f_uvccamera

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        f_uvccamera.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        f_uvccamera

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})]]
