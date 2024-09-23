# FateCamera
集成了android camera各种API的demo.提供 camera 1,camera 2,camera X, camera NDK, camera UVC/USB 的基础使用方法

Demo 中已经对设备是否有对应版本的相机设备做了判断。只有支持才会显示对应的入口

- [ ] 通过是否有入口可以判断设备是否有对应相机设备。
- [ ] 通过是否可以正常打开，判断对应相机设备是否正常

![multi-road camera](https://github.com/FateFeng/FateCamera/blob/main/app/release/camera_check.png?)


## 主要代码模块
```
├── app                         DEMO调用入口
├── camerax                     原生cameraX的源码版本。
├── f_uvccamera                 uvc的封装版本
├── libcamera                   核心SDK部分
...
└── settings.gradle
```
