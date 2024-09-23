# FateCamera
集成了android camera各种API的demo.提供 camera 1,camera 2,camera X, camera NDK, camera UVC/USB 的基础使用方法

Demo 中已经对设备是否有对应版本的相机设备做了判断。只有支持才会显示对应的入口

- 通过是否有入口可以判断设备是否有对应相机设备。
- 通过是否可以正常打开，判断对应相机设备是否正常

![multi-road camera](https://github.com/FateFeng/FateCamera/blob/main/app/release/camera_check.png?)

## City City

在这个项目中你可以看到
- [ ] 各类 Camera API 的使用
- [ ] Opengl 在 Camera 中的使用
- [ ] Jni 的在 Android 中的使用
- [ ] Cmake 在 Android 中的使用
- [ ] Kotlin 在 Android 中的使用
- [ ] 反射, C++/JAVA/KOTLIN 互相调用的使用

如果有兴趣，欢迎一起维护这个项目

如果有需要，我这边可以提供定制功能开发，不过不一定是免费的

## 主要代码模块
```
├── app                         DEMO调用入口
├── camerax                     原生cameraX的源码版本。
├── f_uvccamera                 uvc的封装版本
├── libcamera                   核心SDK部分
...
└── settings.gradle
```

## Camera 1  /  Camera 2

标准camera API

## CameraX

由于原生有很多标准流程，但是现实设备很多不符合标准，因此，直接用源码改编了一下，来确保可以正常打开相机

## CameraX

简单调用了下，很多不合理的地方，后续看情况修改

## CameraUVC

参考 https://github.com/jiangdongguo/AndroidUSBCamera/blob/master/README.md
