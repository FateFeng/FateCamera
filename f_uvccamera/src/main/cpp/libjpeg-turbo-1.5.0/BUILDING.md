Building on Un*x Platforms (including Cygwin and OS X)
=======================================================


Build Requirements
------------------

- autoconf 2.56 or later
- automake 1.7 or later
- libtool 1.4 or later
  * If using Xcode 4.3 or later on OS X, autoconf and automake are no longer
    provided.  The easiest way to obtain them is from
    [MacPorts](http://www.MacPorts.org).

- NASM or YASM (if building x86 or x86-64 SIMD extensions)
  * If using NASM, 0.98, or 2.01 or later is required for an x86 build (0.99
    and 2.00 do not work properly with libjpeg-turbo's x86 SIMD code.)
  * If using NASM, 2.00 or later is required for an x86-64 build.
  * If using NASM, 2.07 or later (except 2.11.08) is required for an x86-64
    Mac build (2.11.08 does not work properly with libjpeg-turbo's x86-64 SIMD
    code when building macho64 objects.)  NASM or YASM can be obtained from
    [MacPorts](http://www.macports.org/).

  The binary RPMs released by the NASM project do not work on older Linux
  systems, such as Red Hat Enterprise Linux 4.  On such systems, you can
   easily build and install NASM from a source RPM by downloading one of the
  SRPMs from

  <http://www.nasm.us/pub/nasm/releasebuilds>

  and executing the following as root:

        ARCH=`uname -m`
        rpmbuild --rebuild nasm-{version}.src.rpm
        rpm -Uvh /usr/src/redhat/RPMS/$ARCH/nasm-{version}.$ARCH.rpm

  NOTE: the NASM build will fail if texinfo is not installed.

- GCC v4.1 (or later) or clang recommended for best performance

- If building the TurboJPEG Java wrapper, JDK or OpenJDK 1.5 or later is
  required.  Some systems, such as Solaris 10 and later and Red Hat Enterprise
  Linux 5 and later, have this pre-installed.  On OS X 10.5 and 10.6, it will
  be necessary to install the Java Developer Package, which can be downloaded
  from <http://developer.apple.com/downloads> (Apple ID required.)  For other
  systems, you can obtain the Oracle Java Development Kit from
  <http://www.java.com>.


Out-of-Tree Builds
------------------

Binary objects, libraries, and executables are generated in the same directory
from which `configure` was executed (the "binary directory"), and this
directory need not necessarily be the same as the libjpeg-turbo source
directory.  You can create multiple independent binary directories, in which
different versions of libjpeg-turbo can be built from the same source tree
using different compilers or settings.  In the sections below,
*{build_directory}* refers to the binary directory, whereas
*{source_directory}* refers to the libjpeg-turbo source directory.  For in-tree
builds, these directories are the same.


Building libjpeg-turbo
----------------------

The following procedure will build libjpeg-turbo on Linux, FreeBSD, Cygwin, and
Solaris/x86 systems (on Solaris, this generates a 32-bit library.  See below
for 64-bit build instructions.)

    cd {source_directory}
    autoreconf -fiv
    cd {build_directory}
    sh {source_directory}/configure [additional configure flags]
    make

NOTE: Running autoreconf in the source directory is not necessary if building
libjpeg-turbo from one of the official release tarballs.

This will generate the following files under .libs/:

**libjpeg.a**  
Static link library for the libjpeg API

**libjpeg.so.{version}** (Linux, Unix)  
**libjpeg.{version}.dylib** (OS X)  
**cygjpeg-{version}.dll** (Cygwin)  
Shared library for the libjpeg API

By default, *{version}* is 62.1.0, 7.1.0, or 8.0.2, depending on whether
libjpeg v6b (default), v7, or v8 emulation is enabled.  If using Cygwin,
*{version}* is 62, 7, or 8.

**libjpeg.so** (Linux, Unix)  
**libjpeg.dylib** (OS X)  
Development symlink for the libjpeg API

**libjpeg.dll.a** (Cygwin)  
Import library for the libjpeg API

**libturbojpeg.a**  
Static link library for the TurboJPEG API

**libturbojpeg.so.0.1.0** (Linux, Unix)  
**libturbojpeg.0.1.0.dylib** (OS X)  
**cygturbojpeg-0.dll** (Cygwin)  
Shared library for the TurboJPEG API

**libturbojpeg.so** (Linux, Unix)  
**libturbojpeg.dylib** (OS X)  
Development symlink for the TurboJPEG API

**libturbojpeg.dll.a** (Cygwin)  
Import library for the TurboJPEG API


### libjpeg v7 or v8 API/ABI Emulation

Add `--with-jpeg7` to the `configure` command line to build a version of
libjpeg-turbo that is API/ABI-compatible with libjpeg v7.  Add `--with-jpeg8`
to the `configure` command to build a version of libjpeg-turbo that is
API/ABI-compatible with libjpeg v8.  See [README.md](README.md) for more
information on libjpeg v7 and v8 emulation.


### In-Memory Source/Destination Managers

When using libjpeg v6b or v7 API/ABI emulation, add `--without-mem-srcdst` to
the `configure` command line to build a version of libjpeg-turbo that lacks the
`jpeg_mem_src()` and `jpeg_mem_dest()` functions.  These functions were not
part of the original libjpeg v6b and v7 APIs, so removing them ensures strict
conformance with those APIs.  See [README.md](README.md) for more information.


### Arithmetic Coding Support

Since the patent on arithmetic coding has expired, this functionality has been
included in this release of libjpeg-turbo.  libjpeg-turbo's implementation is
based on the implementation in libjpeg v8, but it works when emulating libjpeg
v7 or v6b as well.  The default is to enable both arithmetic encoding and
decoding, but those who have philosophical objections to arithmetic coding can
add `--without-arith-enc` or `--without-arith-dec` to the `configure` command
line to disable encoding or decoding (respectively.)


### TurboJPEG Java Wrapper

Add `--with-java` to the `configure` command line to incorporate an optional
Java Native Interface wrapper into the TurboJPEG shared library and build the
Java front-end classes to support it.  This allows the TurboJPEG shared library
to be used directly from Java applications.  See [java/README](java/README) for
more details.

You can set the `JAVAC`, `JAR`, and `JAVA` configure variables to specify
alternate commands for javac, jar, and java (respectively.)  You can also
set the `JAVACFLAGS` configure variable to specify arguments that should be
passed to the Java compiler when building the front-end classes, and
`JNI_CFLAGS` to specify arguments that should be passed to the C compiler when
building the JNI wrapper.  Run `configure --help` for more details.


Installing libjpeg-turbo
------------------------

If you intend to install these libraries and the associated header files, then
replace 'make' in the instructions above with

    make install prefix={base dir} libdir={library directory}

For example,

    make install prefix=/usr/local libdir=/usr/local/lib64

will install the header files in /usr/local/include and the library files in
/usr/local/lib64.  If `prefix` and `libdir` are not specified, then the default
is to install the header files in /opt/libjpeg-turbo/include and the library
files in /opt/libjpeg-turbo/lib32 (32-bit) or /opt/libjpeg-turbo/lib64
(64-bit.)

NOTE: You can specify a prefix of /usr and a libdir of, for instance,
/usr/lib64 to overwrite the system's version of libjpeg.  If you do this,
however, then be sure to BACK UP YOUR SYSTEM'S INSTALLATION OF LIBJPEG before
overwriting it.  It is recommended that you instead install libjpeg-turbo into
a non-system directory and manipulate the `LD_LIBRARY_PATH` or create symlinks
to force applications to use libjpeg-turbo instead of libjpeg.  See
[README.md](README.md) for more information.


Build Recipes
-------------


### 32-bit Build on 64-bit Linux

Add

    --host i686-pc-linux-gnu CFLAGS='-O3 -m32' LDFLAGS=-m32

to the `configure` command line.


### 64-bit Build on 64-bit OS X

Add

    --host x86_64-apple-darwin NASM=/opt/local/bin/nasm

to the `configure` command line.  NASM 2.07 or later from MacPorts must be
installed.


### 32-bit Build on 64-bit OS X

Add

    --host i686-apple-darwin CFLAGS='-O3 -m32' LDFLAGS=-m32

to the `configure` command line.


### 64-bit Backward-Compatible Build on 64-bit OS X

Add

    --host x86_64-apple-darwin NASM=/opt/local/bin/nasm \
      CFLAGS='-mmacosx-version-min=10.5 -O3' \
      LDFLAGS='-mmacosx-version-min=10.5'

to the `configure` command line.  NASM 2.07 or later from MacPorts must be
installed.


### 32-bit Backward-Compatible Build on OS X

Add

    --host i686-apple-darwin \
      CFLAGS='-mmacosx-version-min=10.5 -O3 -m32' \
      LDFLAGS='-mmacosx-version-min=10.5 -m32'

to the `configure` command line.


### 64-bit Build on 64-bit Solaris

Add

    --host x86_64-pc-solaris CFLAGS='-O3 -m64' LDFLAGS=-m64

to the `configure` command line.


### 32-bit Build on 64-bit FreeBSD

Add

    --host i386-unknown-freebsd CC='gcc -B /usr/lib32' CFLAGS='-O3 -m32' \
      LDFLAGS='-B/usr/lib32'

to the `configure` command line.  NASM 2.07 or later from FreeBSD ports must be
installed.


### Oracle Solaris Studio

Add

    CC=cc

to the `configure` command line.  libjpeg-turbo will automatically be built
with the maximum optimization level (-xO5) unless you override `CFLAGS`.

To build a 64-bit version of libjpeg-turbo using Oracle Solaris Studio, add

    --host x86_64-pc-solaris CC=cc CFLAGS='-xO5 -m64' LDFLAGS=-m64

to the `configure` command line.


### MinGW Build on Cygwin

Use CMake (see recipes below)


ARM Support
-----------

This release of libjpeg-turbo can use ARM NEON SIMD instructions to accelerate
JPEG compression/decompression by approximately 2-4x on ARMv7 and later
platforms.  If libjpeg-turbo is configured on an ARM Linux platform, then the
build system will automatically include the NEON SIMD routines, if they are
supported.  Build instructions for other ARM-based platforms follow.


### Building libjpeg-turbo for iOS

iOS platforms, such as the iPhone and iPad, use ARM processors, some of which
support NEON instructions.  Additional steps are required in order to build
libjpeg-turbo for these platforms.


#### Additional build requirements

- [gas-preprocessor.pl]
  (https://raw.githubusercontent.com/libjpeg-turbo/gas-preprocessor/master/gas-preprocessor.pl)
  should be installed in your `PATH`.


#### ARM 32-bit Build (Xcode 4.6.x and earlier, LLVM-GCC)

Set the following shell variables for simplicity:

  *Xcode 4.2 and earlier*

    IOS_PLATFORMDIR=/Developer/Platforms/iPhoneOS.platform`

  *Xcode 4.3 and later*

    IOS_PLATFORMDIR=/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform

  *All Xcode versions*

    IOS_SYSROOT=$IOS_PLATFORMDIR/Developer/SDKs/iPhoneOS*.sdk
    IOS_GCC=$IOS_PLATFORMDIR/Developer/usr/bin/arm-apple-darwin10-llvm-gcc-4.2

  *ARMv6 (code will run on all iOS devices, not SIMD-accelerated)*  
  [NOTE: Requires Xcode 4.4.x or earlier]

    IOS_CFLAGS="-march=armv6 -mcpu=arm1176jzf-s -mfpu=vfp"

  *ARMv7 (code will run on iPhone 3GS-4S/iPad 1st-3rd Generation and newer)*

    IOS_CFLAGS="-march=armv7 -mcpu=cortex-a8 -mtune=cortex-a8 -mfpu=neon"

  *ARMv7s (code will run on iPhone 5/iPad 4th Generation and newer)*  
  [NOTE: Requires Xcode 4.5 or later]

    IOS_CFLAGS="-march=armv7s -mcpu=swift -mtune=swift -mfpu=neon"

Follow the procedure under "Building libjpeg-turbo" above, adding

    --host arm-apple-darwin10 \
      CC="$IOS_GCC" LD="$IOS_GCC" \
      CFLAGS="-mfloat-abi=softfp -isysroot $IOS_SYSROOT -O3 $IOS_CFLAGS" \
      LDFLAGS="-mfloat-abi=softfp -isysroot $IOS_SYSROOT $IOS_CFLAGS"

to the `configure` command line.


#### ARM 32-bit Build (Xcode 5.0.x and later, Clang)

Set the following shell variables for simplicity:

    IOS_PLATFORMDIR=/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform
    IOS_SYSROOT=$IOS_PLATFORMDIR/Developer/SDKs/iPhoneOS*.sdk
    IOS_GCC=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang

  *ARMv7 (code will run on iPhone 3GS-4S/iPad 1st-3rd Generation and newer)*

    IOS_CFLAGS="-arch armv7"

  *ARMv7s (code will run on iPhone 5/iPad 4th Generation and newer)*

    IOS_CFLAGS="-arch armv7s"

Follow the procedure under "Building libjpeg-turbo" above, adding

    --host arm-apple-darwin10 \
      CC="$IOS_GCC" LD="$IOS_GCC" \
      CFLAGS="-mfloat-abi=softfp -isysroot $IOS_SYSROOT -O3 $IOS_CFLAGS" \
      LDFLAGS="-mfloat-abi=softfp -isysroot $IOS_SYSROOT $IOS_CFLAGS" \
      CCASFLAGS="-no-integrated-as $IOS_CFLAGS"

to the `configure` command line.


#### ARMv8 64-bit Build (Xcode 5.0.x and later, Clang)

Code will run on iPhone 5S/iPad Mini 2/iPad Air and newer.

Set the following shell variables for simplicity:

    IOS_PLATFORMDIR=/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform
    IOS_SYSROOT=$IOS_PLATFORMDIR/Developer/SDKs/iPhoneOS*.sdk
    IOS_GCC=/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang
    IOS_CFLAGS="-arch arm64"

Follow the procedure under "Building libjpeg-turbo" above, adding

    --host aarch64-apple-darwin \
      CC="$IOS_GCC" LD="$IOS_GCC" \
      CFLAGS="-isysroot $IOS_SYSROOT -O3 $IOS_CFLAGS" \
      LDFLAGS="-isysroot $IOS_SYSROOT $IOS_CFLAGS"

to the `configure` command line.


NOTE:  You can also add `-miphoneos-version-min={version}` to `$IOS_CFLAGS`
above in order to support older versions of iOS than the default version
supported by the SDK.

Once built, lipo can be used to combine the ARMv6, v7, v7s, and/or v8 variants
into a universal library.


### Building libjpeg-turbo for Android

Building libjpeg-turbo for Android platforms requires the
{Android NDK}(https://developer.android.com/tools/sdk/ndk)
and autotools.  The following is a general recipe script that can be modified for your specific needs.

    # Set these variables to suit your needs
    NDK_PATH={full path to the "ndk" directory-- for example, /opt/android/ndk}
    BUILD_PLATFORM={the platform name for the NDK package you installed--
      for example, "windows-x86" or "linux-x86_64" or "darwin-x86_64"}
    TOOLCHAIN_VERSION={"4.8", "4.9", "clang3.5", etc.  This corresponds to a
      toolchain directory under ${NDK_PATH}/toolchains/.}
    ANDROID_VERSION={The minimum version of Android to support-- for example,
      "16", "19", etc.  "21" or later is required for a 64-bit build.}

    # 32-bit ARMv7 build
    HOST=arm-linux-androideabi
    SYSROOT=${NDK_PATH}/platforms/android-${ANDROID_VERSION}/arch-arm
    ANDROID_CFLAGS="-march=armv7-a -mfloat-abi=softfp -fprefetch-loop-arrays \
      --sysroot=${SYSROOT}"

    # 64-bit ARMv8 build
    HOST=aarch64-linux-android
    SYSROOT=${NDK_PATH}/platforms/android-${ANDROID_VERSION}/arch-arm64
    ANDROID_CFLAGS="--sysroot=${SYSROOT}"

    TOOLCHAIN=${NDK_PATH}/toolchains/${HOST}-${TOOLCHAIN_VERSION}/prebuilt/${BUILD_PLATFORM}
    ANDROID_INCLUDES="-I${SYSROOT}/usr/include -I${TOOLCHAIN}/include"
    export CPP=${TOOLCHAIN}/bin/${HOST}-cpp
    export AR=${TOOLCHAIN}/bin/${HOST}-ar
    export AS=${TOOLCHAIN}/bin/${HOST}-as
    export NM=${TOOLCHAIN}/bin/${HOST}-nm
    export CC=${TOOLCHAIN}/bin/${HOST}-gcc
    export LD=${TOOLCHAIN}/bin/${HOST}-ld
    export RANLIB=${TOOLCHAIN}/bin/${HOST}-ranlib
    export OBJDUMP=${TOOLCHAIN}/bin/${HOST}-objdump
    export STRIP=${TOOLCHAIN}/bin/${HOST}-strip
    cd {build_directory}
    sh {source_directory}/configure --host=${HOST} \
      CFLAGS="${ANDROID_INCLUDES} ${ANDROID_CFLAGS} -O3 -fPIE" \
      CPPFLAGS="${ANDROID_INCLUDES} ${ANDROID_CFLAGS}" \
      LDFLAGS="${ANDROID_CFLAGS} -pie" --with-simd ${1+"$@"}
    make

If building for Android 4.0.x (API level < 16) or earlier, remove `-fPIE` from
`CFLAGS` and `-pie` from `LDFLAGS`.


Building on Windows (Visual C++ or MinGW)
=========================================


Build Requirements
------------------

- [CMake](http://www.cmake.org) v2.8.11 or later

- [NASM](http://www.nasm.us) or [YASM](http://yasm.tortall.net)
  * If using NASM, 0.98 or later is required for an x86 build.
  * If using NASM, 2.05 or later is required for an x86-64 build.
  * nasm.exe/yasm.exe should be in your `PATH`.

- Microsoft Visual C++ 2005 or later

  If you don't already have Visual C++, then the easiest way to get it is by
  installing the
  [Windows SDK](http://msdn.microsoft.com/en-us/windows/bb980924.aspx).
  The Windows SDK includes both 32-bit and 64-bit Visual C++ compilers and
  everything necessary to build libjpeg-turbo.

  * You can also use Microsoft Visual Studio Express/Community Edition, which
    is a free download.  (NOTE: versions prior to 2012 can only be used to
    build 32-bit code.)
  * If you intend to build libjpeg-turbo from the command line, then add the
    appropriate compiler and SDK directories to the `INCLUDE`, `LIB`, and
    `PATH` environment variables.  This is generally accomplished by
    executing `vcvars32.bat` or `vcvars64.bat` and `SetEnv.cmd`.
    `vcvars32.bat` and `vcvars64.bat` are part of Visual C++ and are located in
    the same directory as the compiler.  `SetEnv.cmd` is part of the Windows
    SDK.  You can pass optional arguments to `SetEnv.cmd` to specify a 32-bit
    or 64-bit build environment.

   ... OR ...

- MinGW

  [MinGW-builds](http://sourceforge.net/projects/mingwbuilds/) or
  [tdm-gcc](http://tdm-gcc.tdragon.net/) recommended if building on a Windows
  machine.  Both distributions install a Start Menu link that can be used to
  launch a command prompt with the appropriate compiler paths automatically
  set.

- If building the TurboJPEG Java wrapper, JDK 1.5 or later is required.  This
  can be downloaded from <http://www.java.com>.


Out-of-Tree Builds
------------------

Binary objects, libraries, and executables are generated in the same directory
from which `cmake` was executed (the "binary directory"), and this directory
need not necessarily be the same as the libjpeg-turbo source directory.  You
can create multiple independent binary directories, in which different versions
of libjpeg-turbo can be built from the same source tree using different
compilers or settings.  In the sections below, *{build_directory}* refers to
the binary directory, whereas *{source_directory}* refers to the libjpeg-turbo
source directory.  For in-tree builds, these directories are the same.


Building libjpeg-turbo
----------------------


### Visual C++ (Command Line)

    cd {build_directory}
    cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Release {source_directory}
    nmake

This will build either a 32-bit or a 64-bit version of libjpeg-turbo, depending
on which version of cl.exe is in the `PATH`.

The following files will be generated under *{build_directory}*:

**src.jpeg-static.lib**  
Static link library for the libjpeg API

**sharedlib/src.jpeg{version}.dll**  
DLL for the libjpeg API

**sharedlib/src.jpeg.lib**  
Import library for the libjpeg API

**turbojpeg-static.lib**  
Static link library for the TurboJPEG API

**turbojpeg.dll**  
DLL for the TurboJPEG API

**turbojpeg.lib**  
Import library for the TurboJPEG API

*{version}* is 62, 7, or 8, depending on whether libjpeg v6b (default), v7, or
v8 emulation is enabled.


### Visual C++ (IDE)

Choose the appropriate CMake generator option for your version of Visual Studio
(run `cmake` with no arguments for a list of available generators.)  For
instance:

    cd {build_directory}
    cmake -G "Visual Studio 10" {source_directory}

NOTE:  Add "Win64" to the generator name (for example, "Visual Studio 10
Win64") to build a 64-bit version of libjpeg-turbo.  Recent versions of CMake
no longer document that.  A separate build directory must be used for 32-bit
and 64-bit builds.

You can then open ALL_BUILD.vcproj in Visual Studio and build one of the
configurations in that project ("Debug", "Release", etc.) to generate a full
build of libjpeg-turbo.

This will generate the following files under *{build_directory}*:

**{configuration}/src.jpeg-static.lib**  
Static link library for the libjpeg API

**sharedlib/{configuration}/src.jpeg{version}.dll**  
DLL for the libjpeg API

**sharedlib/{configuration}/src.jpeg.lib**  
Import library for the libjpeg API

**{configuration}/turbojpeg-static.lib**  
Static link library for the TurboJPEG API

**{configuration}/turbojpeg.dll**  
DLL for the TurboJPEG API

**{configuration}/turbojpeg.lib**  
Import library for the TurboJPEG API

*{configuration}* is Debug, Release, RelWithDebInfo, or MinSizeRel, depending
on the configuration you built in the IDE, and *{version}* is 62, 7, or 8,
depending on whether libjpeg v6b (default), v7, or v8 emulation is enabled.


### MinGW

NOTE: This assumes that you are building on a Windows machine.  If you are
cross-compiling on a Linux/Unix machine, then see "Build Recipes" below.

    cd {build_directory}
    cmake -G "MinGW Makefiles" {source_directory}
    mingw32-make

This will generate the following files under *{build_directory}*:

**libjpeg.a**  
Static link library for the libjpeg API

**sharedlib/libjpeg-{version}.dll**  
DLL for the libjpeg API

**sharedlib/libjpeg.dll.a**  
Import library for the libjpeg API

**libturbojpeg.a**  
Static link library for the TurboJPEG API

**libturbojpeg.dll**  
DLL for the TurboJPEG API

**libturbojpeg.dll.a**  
Import library for the TurboJPEG API

*{version}* is 62, 7, or 8, depending on whether libjpeg v6b (default), v7, or
v8 emulation is enabled.


### Debug Build

Add `-DCMAKE_BUILD_TYPE=Debug` to the `cmake` command line.  Or, if building
with NMake, remove `-DCMAKE_BUILD_TYPE=Release` (Debug builds are the default
with NMake.)


### libjpeg v7 or v8 API/ABI Emulation

Add `-DWITH_JPEG7=1` to the `cmake` command line to build a version of
libjpeg-turbo that is API/ABI-compatible with libjpeg v7.  Add `-DWITH_JPEG8=1`
to the `cmake` command line to build a version of libjpeg-turbo that is
API/ABI-compatible with libjpeg v8.  See [README.md](README.md) for more
information on libjpeg v7 and v8 emulation.


### In-Memory Source/Destination Managers

When using libjpeg v6b or v7 API/ABI emulation, add `-DWITH_MEM_SRCDST=0` to
the `cmake` command line to build a version of libjpeg-turbo that lacks the
`jpeg_mem_src()` and `jpeg_mem_dest()` functions.  These functions were not
part of the original libjpeg v6b and v7 APIs, so removing them ensures strict
conformance with those APIs.  See [README.md](README.md) for more information.


### Arithmetic Coding Support

Since the patent on arithmetic coding has expired, this functionality has been
included in this release of libjpeg-turbo.  libjpeg-turbo's implementation is
based on the implementation in libjpeg v8, but it works when emulating libjpeg
v7 or v6b as well.  The default is to enable both arithmetic encoding and
decoding, but those who have philosophical objections to arithmetic coding can
add `-DWITH_ARITH_ENC=0` or `-DWITH_ARITH_DEC=0` to the `cmake` command line to
disable encoding or decoding (respectively.)


### TurboJPEG Java Wrapper

Add `-DWITH_JAVA=1` to the `cmake` command line to incorporate an optional Java
Native Interface wrapper into the TurboJPEG shared library and build the Java
front-end classes to support it.  This allows the TurboJPEG shared library to
be used directly from Java applications.  See [java/README](java/README) for
more details.

You can set the `Java_JAVAC_EXECUTABLE`, `Java_JAVA_EXECUTABLE`, and
`Java_JAR_EXECUTABLE` CMake variables to specify alternate commands or
locations for javac, jar, and java (respectively.)  You can also set the
`JAVACFLAGS` CMake variable to specify arguments that should be passed to the
Java compiler when building the front-end classes.


Installing libjpeg-turbo
------------------------

You can use the build system to install libjpeg-turbo into a directory of your
choosing (as opposed to creating an installer.)  To do this, add:

    -DCMAKE_INSTALL_PREFIX={install_directory}

to the cmake command line.

For example,

    cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Release \
      -DCMAKE_INSTALL_PREFIX=c:\libjpeg-turbo {source_directory}
    nmake install

will install the header files in c:\libjpeg-turbo\include, the library files
in c:\libjpeg-turbo\lib, the DLL's in c:\libjpeg-turbo\bin, and the
documentation in c:\libjpeg-turbo\doc.


Build Recipes
-------------


### 64-bit MinGW Build on Cygwin

    cd {build_directory}
    CC=/usr/bin/x86_64-w64-mingw32-gcc \
      cmake -G "Unix Makefiles" -DCMAKE_SYSTEM_NAME=Windows \
      -DCMAKE_RC_COMPILER=/usr/bin/x86_64-w64-mingw32-windres.exe \
      {source_directory}
    make

This produces a 64-bit build of libjpeg-turbo that does not depend on
cygwin1.dll or other Cygwin DLL's.  The mingw64-x86\_64-gcc-core and
mingw64-x86\_64-gcc-g++ packages (and their dependencies) must be installed.


### 32-bit MinGW Build on Cygwin

     cd {build_directory}
     CC=/usr/bin/i686-w64-mingw32-gcc \
       cmake -G "Unix Makefiles" -DCMAKE_SYSTEM_NAME=Windows \
       -DCMAKE_RC_COMPILER=/usr/bin/i686-w64-mingw32-windres.exe \
       {source_directory}
     make

This produces a 32-bit build of libjpeg-turbo that does not depend on
cygwin1.dll or other Cygwin DLL's.  The mingw64-i686-gcc-core and
mingw64-i686-gcc-g++ packages (and their dependencies) must be installed.


### MinGW Build on Linux

    cd {build_directory}
    CC={mingw_binary_path}/i686-pc-mingw32-gcc \
      cmake -G "Unix Makefiles" -DCMAKE_SYSTEM_NAME=Windows \
      -DCMAKE_RC_COMPILER={mingw_binary_path}/i686-pc-mingw32-windres \
      -DCMAKE_AR={mingw_binary_path}/i686-pc-mingw32-ar \
      -DCMAKE_RANLIB={mingw_binary_path}/i686-pc-mingw32-ranlib \
      {source_directory}
    make


Creating Release Packages
=========================

The following commands can be used to create various types of release packages:


Unix/Linux
----------

    make rpm

Create Red Hat-style binary RPM package.  Requires RPM v4 or later.

    make srpm

This runs `make dist` to create a pristine source tarball, then creates a
Red Hat-style source RPM package from the tarball.  Requires RPM v4 or later.

    make deb

Create Debian-style binary package.  Requires dpkg.

    make dmg

Create Macintosh package/disk image.  This requires pkgbuild and
productbuild, which are installed by default on OS X 10.7 and later and which
can be obtained by installing Xcode 3.2.6 (with the "Unix Development"
option) on OS X 10.6.  Packages built in this manner can be installed on OS X
10.5 and later, but they must be built on OS X 10.6 or later.

    make udmg [BUILDDIR32={32-bit build directory}]

On 64-bit OS X systems, this creates a Macintosh package and disk image that
contains universal i386/x86-64 binaries.  You should first configure a 32-bit
out-of-tree build of libjpeg-turbo, then configure a 64-bit out-of-tree
build, then run `make udmg` from the 64-bit build directory.  The build
system will look for the 32-bit build under *{source_directory}*/osxx86 by
default, but you can override this by setting the `BUILDDIR32` variable on the
make command line as shown above.

    make iosdmg [BUILDDIR32={32-bit build directory}] \
      [BUILDDIRARMV6={ARMv6 build directory}] \
      [BUILDDIRARMV7={ARMv7 build directory}] \
      [BUILDDIRARMV7S={ARMv7s build directory}] \
      [BUILDDIRARMV8={ARMv8 build directory}]

On OS X systems, this creates a Macintosh package and disk image in which the
libjpeg-turbo static libraries contain ARM architectures necessary to build
iOS applications.  If building on an x86-64 system, the binaries will also
contain the i386 architecture, as with `make udmg` above.  You should first
configure ARMv6, ARMv7, ARMv7s, and/or ARMv8 out-of-tree builds of
libjpeg-turbo (see "Building libjpeg-turbo for iOS" above.)  If you are
building an x86-64 version of libjpeg-turbo, you should configure a 32-bit
out-of-tree build as well.  Next, build libjpeg-turbo as you would normally,
using an out-of-tree build.  When it is built, run `make iosdmg` from the
build directory.  The build system will look for the ARMv6 build under
*{source_directory}*/iosarmv6 by default, the ARMv7 build under
*{source_directory}*/iosarmv7 by default, the ARMv7s build under
*{source_directory}*/iosarmv7s by default, the ARMv8 build under
*{source_directory}*/iosarmv8 by default, and (if applicable) the 32-bit build
under *{source_directory}*/osxx86 by default, but you can override this by
setting the `BUILDDIR32`, `BUILDDIRARMV6`, `BUILDDIRARMV7`, `BUILDDIRARMV7S`,
and/or `BUILDDIRARMV8` variables on the `make` command line as shown above.

NOTE: If including an ARMv8 build in the package, then you may need to use
Xcode's version of lipo instead of the operating system's.  To do this, pass
an argument of `LIPO="xcrun lipo"` on the make command line.

    make cygwinpkg

Build a Cygwin binary package.


Windows
-------

If using NMake:

    cd {build_directory}
    nmake installer

If using MinGW:

    cd {build_directory}
    make installer

If using the Visual Studio IDE, build the "installer" project.

The installer package (libjpeg-turbo[-gcc][64].exe) will be located under
*{build_directory}*.  If building using the Visual Studio IDE, then the
installer package will be located in a subdirectory with the same name as the
configuration you built (such as *{build_directory}*\Debug\ or
*{build_directory}*\Release\).

Building a Windows installer requires the Nullsoft Install System
(http://nsis.sourceforge.net/.)  makensis.exe should be in your `PATH`.


Regression testing
==================

The most common way to test libjpeg-turbo is by invoking `make test` on
Unix/Linux platforms or `ctest` on Windows platforms, once the build has
completed.  This runs a series of tests to ensure that mathematical
compatibility has been maintained between libjpeg-turbo and libjpeg v6b.  This
also invokes the TurboJPEG unit tests, which ensure that the colorspace
extensions, YUV encoding, decompression scaling, and other features of the
TurboJPEG C and Java APIs are working properly (and, by extension, that the
equivalent features of the underlying libjpeg API are also working.)

Invoking `make testclean` or `nmake testclean` (if using NMake) or building
the 'testclean' target (if using the Visual Studio IDE) will clean up the
output images generated by `make test`.

On Unix/Linux platforms, more extensive tests of the TurboJPEG C and Java
wrappers can be run by invoking `make tjtest`.  These extended TurboJPEG tests
essentially iterate through all of the available features of the TurboJPEG APIs
that are not covered by the TurboJPEG unit tests (this includes the lossless
transform options) and compare the images generated by each feature to images
generated using the equivalent feature in the libjpeg API.  The extended
TurboJPEG tests are meant to test for regressions in the TurboJPEG wrappers,
not in the underlying libjpeg API library.
