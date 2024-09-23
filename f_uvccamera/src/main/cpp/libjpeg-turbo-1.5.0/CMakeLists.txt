#
# Setup
#

cmake_minimum_required(VERSION 2.8.11)
#

project(libjpeg-turbo C)
set(VERSION 1.5.0)

set(JPEG_SOURCES jcapimin.c jcapistd.c jccoefct.c jccolor.c jcdctmgr.c jchuff.c
  jcinit.c jcmainct.c jcmarker.c jcmaster.c jcomapi.c jcparam.c jcphuff.c
  jcprepct.c jcsample.c jctrans.c jdapimin.c jdapistd.c jdatadst.c jdatasrc.c
  jdcoefct.c jdcolor.c jddctmgr.c jdhuff.c jdinput.c jdmainct.c jdmarker.c
  jdmaster.c jdmerge.c jdphuff.c jdpostct.c jdsample.c jdtrans.c jerror.c
  jfdctflt.c jfdctfst.c jfdctint.c jidctflt.c jidctfst.c jidctint.c jidctred.c
  jquant1.c jquant2.c jutils.c jmemmgr.c jmemnobs.c)

if(WITH_ARITH_ENC OR WITH_ARITH_DEC)
  set(JPEG_SOURCES ${JPEG_SOURCES} jaricom.c)
endif()

if(WITH_ARITH_ENC)
  set(JPEG_SOURCES ${JPEG_SOURCES} jcarith.c)
endif()

if(WITH_ARITH_DEC)
  set(JPEG_SOURCES ${JPEG_SOURCES} jdarith.c)
endif()

if(WITH_SIMD)
  add_definitions(-DWITH_SIMD)
  add_subdirectory(simd)
  if(SIMD_X86_64)
    set(JPEG_SOURCES ${JPEG_SOURCES} simd/jsimd_x86_64.c)
  else()
    set(JPEG_SOURCES ${JPEG_SOURCES} simd/jsimd_i386.c)
  endif()
  # This tells CMake that the "source" files haven't been generated yet
  set_source_files_properties(${SIMD_OBJS} PROPERTIES GENERATED 1)
else()
  set(JPEG_SOURCES ${JPEG_SOURCES} jsimd_none.c)
  message(STATUS "Not using SIMD acceleration")
endif()

if(WITH_JAVA)
  add_subdirectory(java)
  set(ENABLE_SHARED TRUE)
endif()

if(ENABLE_SHARED)
  add_subdirectory(sharedlib)
endif()

if(ENABLE_STATIC OR WITH_TURBOJPEG)
  add_library(jpeg-static STATIC ${JPEG_SOURCES} ${SIMD_OBJS})
  if(NOT MSVC)
    set_target_properties(jpeg-static PROPERTIES OUTPUT_NAME jpeg)
  endif()
  if(WITH_SIMD)
    add_dependencies(jpeg-static simd)
  endif()
endif()

if(WITH_TURBOJPEG)
  set(TURBOJPEG_SOURCES turbojpeg.c transupp.c jdatadst-tj.c jdatasrc-tj.c)
  if(WITH_JAVA)
    set(TURBOJPEG_SOURCES ${TURBOJPEG_SOURCES} turbojpeg-jni.c)
    include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2})
  endif()

  if(ENABLE_SHARED)
    add_library(turbojpeg SHARED ${TURBOJPEG_SOURCES})
    set_target_properties(turbojpeg PROPERTIES DEFINE_SYMBOL DLLDEFINE)
    if(MINGW)
      set_target_properties(turbojpeg PROPERTIES LINK_FLAGS -Wl,--kill-at)
    endif()
    target_link_libraries(turbojpeg jpeg-static)
    set_target_properties(turbojpeg PROPERTIES LINK_INTERFACE_LIBRARIES "")

    add_executable(tjunittest tjunittest.c tjutil.c)
    target_link_libraries(tjunittest turbojpeg)

    add_executable(tjbench tjbench.c bmp.c tjutil.c rdbmp.c rdppm.c wrbmp.c
      wrppm.c)
    target_link_libraries(tjbench turbojpeg jpeg-static)
    set_property(TARGET tjbench PROPERTY COMPILE_FLAGS
      "-DBMP_SUPPORTED -DPPM_SUPPORTED")
  endif()

  if(ENABLE_STATIC)
    add_library(turbojpeg-static STATIC ${JPEG_SOURCES} ${SIMD_OBJS}
      turbojpeg.c transupp.c jdatadst-tj.c jdatasrc-tj.c)
    if(NOT MSVC)
      set_target_properties(turbojpeg-static PROPERTIES OUTPUT_NAME turbojpeg)
    endif()
    if(WITH_SIMD)
      add_dependencies(turbojpeg-static simd)
    endif()

    add_executable(tjunittest-static tjunittest.c tjutil.c)
    target_link_libraries(tjunittest-static turbojpeg-static)

    add_executable(tjbench-static tjbench.c bmp.c tjutil.c rdbmp.c rdppm.c
      wrbmp.c wrppm.c)
    target_link_libraries(tjbench-static turbojpeg-static jpeg-static)
    set_property(TARGET tjbench-static PROPERTY COMPILE_FLAGS
      "-DBMP_SUPPORTED -DPPM_SUPPORTED")
  endif()
endif()

if(WITH_12BIT)
  set(COMPILE_FLAGS "-DGIF_SUPPORTED -DPPM_SUPPORTED -DUSE_SETMODE")
else()
  set(COMPILE_FLAGS "-DBMP_SUPPORTED -DGIF_SUPPORTED -DPPM_SUPPORTED -DTARGA_SUPPORTED -DUSE_SETMODE")
  set(CJPEG_BMP_SOURCES rdbmp.c rdtarga.c)
  set(DJPEG_BMP_SOURCES wrbmp.c wrtarga.c)
endif()

if(ENABLE_STATIC)
  add_executable(cjpeg-static cjpeg.c cdjpeg.c rdgif.c rdppm.c rdswitch.c
    ${CJPEG_BMP_SOURCES})
  set_property(TARGET cjpeg-static PROPERTY COMPILE_FLAGS ${COMPILE_FLAGS})
  target_link_libraries(cjpeg-static jpeg-static)

  add_executable(djpeg-static djpeg.c cdjpeg.c rdcolmap.c rdswitch.c wrgif.c
    wrppm.c ${DJPEG_BMP_SOURCES})
  set_property(TARGET djpeg-static PROPERTY COMPILE_FLAGS ${COMPILE_FLAGS})
  target_link_libraries(djpeg-static jpeg-static)

  add_executable(jpegtran-static jpegtran.c cdjpeg.c rdswitch.c transupp.c)
  target_link_libraries(jpegtran-static jpeg-static)
  set_property(TARGET jpegtran-static PROPERTY COMPILE_FLAGS "-DUSE_SETMODE")
endif()

add_executable(rdjpgcom rdjpgcom.c)

add_executable(wrjpgcom wrjpgcom.c)

#
# Installer
#

if(MSVC)
  set(INST_PLATFORM "Visual C++")
  set(INST_NAME ${CMAKE_PROJECT_NAME}-${VERSION}-vc)
  set(INST_REG_NAME ${CMAKE_PROJECT_NAME})
elseif(MINGW)
  set(INST_PLATFORM GCC)
  set(INST_NAME ${CMAKE_PROJECT_NAME}-${VERSION}-gcc)
  set(INST_REG_NAME ${CMAKE_PROJECT_NAME}-gcc)
  set(INST_DEFS -DGCC)
endif()

if(64BIT)
  set(INST_PLATFORM "${INST_PLATFORM} 64-bit")
  set(INST_NAME ${INST_NAME}64)
  set(INST_REG_NAME ${INST_DIR}64)
  set(INST_DEFS ${INST_DEFS} -DWIN64)
endif()

if(WITH_JAVA)
  set(INST_DEFS ${INST_DEFS} -DJAVA)
endif()

if(MSVC_IDE)
  set(INST_DEFS ${INST_DEFS} "-DBUILDDIR=${CMAKE_CFG_INTDIR}\\")
else()
  set(INST_DEFS ${INST_DEFS} "-DBUILDDIR=")
endif()

STRING(REGEX REPLACE "/" "\\\\" INST_DIR ${CMAKE_INSTALL_PREFIX})

configure_file(release/libjpeg-turbo.nsi.in libjpeg-turbo.nsi @ONLY)

if(WITH_JAVA)
  set(JAVA_DEPEND java)
endif()
add_custom_target(installer
  makensis -nocd ${INST_DEFS} libjpeg-turbo.nsi
  DEPENDS jpeg jpeg-static turbojpeg turbojpeg-static rdjpgcom wrjpgcom
    cjpeg djpeg jpegtran tjbench ${JAVA_DEPEND}
  SOURCES libjpeg-turbo.nsi)

if(WITH_TURBOJPEG)
  if(ENABLE_SHARED)
    install(TARGETS turbojpeg tjbench
      ARCHIVE DESTINATION lib
      LIBRARY DESTINATION lib
      RUNTIME DESTINATION bin)
  endif()
  if(ENABLE_STATIC)
    install(TARGETS turbojpeg-static ARCHIVE DESTINATION lib)
    if(NOT ENABLE_SHARED)
      install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/tjbench-static.exe
        DESTINATION bin RENAME tjbench.exe)
    endif()
  endif()
  install(FILES ${CMAKE_SOURCE_DIR}/turbojpeg.h DESTINATION include)
endif()

if(ENABLE_STATIC)
  install(TARGETS jpeg-static ARCHIVE DESTINATION lib)
  if(NOT ENABLE_SHARED)
    install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/cjpeg-static.exe
      DESTINATION bin RENAME cjpeg.exe)
    install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/djpeg-static.exe
      DESTINATION bin RENAME djpeg.exe)
    install(PROGRAMS ${CMAKE_CURRENT_BINARY_DIR}/jpegtran-static.exe
      DESTINATION bin RENAME jpegtran.exe)
  endif()
endif()

install(TARGETS rdjpgcom wrjpgcom RUNTIME DESTINATION bin)

install(FILES ${CMAKE_SOURCE_DIR}/README.ijg ${CMAKE_SOURCE_DIR}/README.md
  ${CMAKE_SOURCE_DIR}/example.c ${CMAKE_SOURCE_DIR}/libjpeg.txt
  ${CMAKE_SOURCE_DIR}/structure.txt ${CMAKE_SOURCE_DIR}/usage.txt
  ${CMAKE_SOURCE_DIR}/wizard.txt
  DESTINATION doc)

install(FILES ${CMAKE_BINARY_DIR}/jconfig.h ${CMAKE_SOURCE_DIR}/jerror.h
  ${CMAKE_SOURCE_DIR}/jmorecfg.h ${CMAKE_SOURCE_DIR}/jpeglib.h
  DESTINATION include)
