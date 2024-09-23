#include <jni.h>
#include <string>

#include <android/surface_texture.h>
#include <android/surface_texture_jni.h>

#include "ncamera.h"
#include "camera_engine.h"
#include "native-log.h"


/**
 * 创建相机对象
 */
extern "C"
JNIEXPORT jlong JNICALL
Java_com_feng_libcamera_core_CameraNDK_createCamera(JNIEnv *env, jobject thiz, jstring cameraId) {
    auto *cam_eng = new CameraEngine(env, thiz, cameraId);
    return reinterpret_cast<jlong>(cam_eng);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_feng_libcamera_core_CameraNDK_updatePreviewSize(JNIEnv *env, jobject thiz,
                                                         jlong ndk_camera, jint width, jint height, jint rotation) {
    auto *cam_eng = reinterpret_cast<CameraEngine *> (ndk_camera);
//    cam_eng->CreateCameraSession(surface);
    cam_eng->updatePreviewSize(width, height, rotation);
    return reinterpret_cast<jlong>(cam_eng);
}

/**
 * 创建相机预览界面
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_feng_libcamera_core_CameraNDK_onPreviewSurfaceCreated(JNIEnv *env, jobject thiz,
                                                         jlong ndk_camera, jobject surface) {
    auto *cam_eng = reinterpret_cast<CameraEngine *> (ndk_camera);
//    cam_eng->CreateCameraSession(surface);
    cam_eng->setCameraImageReader(surface);
    cam_eng->Preview(true);
}

/**
 * 销毁相机预览界面
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_feng_libcamera_core_CameraNDK_onPreviewSurfaceDestroyed(JNIEnv *env, jobject thiz,
                                                           jlong ndk_camera, jobject surface) {
    auto *cam_eng = reinterpret_cast<CameraEngine *>(ndk_camera);

    jclass cls = env->FindClass("android/view/Surface");
    jmethodID toString =
            env->GetMethodID(cls, "toString", "()Ljava/lang/String;");
    auto destroyObjStr =
            reinterpret_cast<jstring>(env->CallObjectMethod(surface, toString));
    const char *destroyObjName = env->GetStringUTFChars(destroyObjStr, nullptr);
    LOG_INFO("Destroy Object Name: %s", destroyObjName);

    auto appObjStr = reinterpret_cast<jstring>(
            env->CallObjectMethod(cam_eng->GetSurfaceObject(), toString));
    const char *appObjName = env->GetStringUTFChars(appObjStr, nullptr);
    env->ReleaseStringUTFChars(destroyObjStr, destroyObjName);
    env->ReleaseStringUTFChars(appObjStr, appObjName);

    cam_eng->Preview(false);
}

/**
 * 销毁相机对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_feng_libcamera_core_CameraNDK_destroyCamera(JNIEnv *env, jobject thiz, jlong ndk_camera) {
    if (!ndk_camera) {
        return;
    }
    // 释放相机对象
    auto *cam_eng = reinterpret_cast<CameraEngine *>(ndk_camera);
    delete cam_eng;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_feng_libcamera_core_CameraNDK_getCompatiblePreviewSize(JNIEnv *env, jobject thiz,
                                                          jlong ndk_camera) {
    if (!ndk_camera) return nullptr;
    auto *cam_eng = reinterpret_cast<CameraEngine *>(ndk_camera);
    ImageFormat compat_resolution = cam_eng->GetCompatibleResolution();
    jclass cls = env->FindClass("android/util/Size");

    return env->NewObject(
            cls,
            env->GetMethodID(cls, "<init>", "(II)V"),
            compat_resolution.width,
            compat_resolution.height
    );
}
