##TUPU-Android-SDK
TUPU-Android-SDK 是集成了面部关键点追踪和美颜等功能的开发套件,通过获取 Camera实例,将美颜后的图像输出到 GLSurfaceView上,并追踪人脸关键点位置

##TUPUSDK 的特性

* 人脸关键点追踪
* 速度: 12-20 ms
* 数量：83 点
* 美颜

##依赖
* libLandMark_Api.so

##运行环境
* Android 4.0 以上
* OpenGLES2.0

##集成
``` java

//初始化关键点追踪模型
FaceLandMark.initModel(getAssets(), MODEL_FILE, DETECT_FILE)
//设置模型参数
FaceLandMark.setSmoothRatio(smoothRatioValue);
FaceLandMark.setFacenessThreshold(faceness);

//实例化 GPUImage
GPUImage gpuImage = new GPUImage(Activity.this);

//实例化美颜滤镜
MagicBeautyFilter beautifyFilter = new MagicBeautyFilter();
//设置美颜程度(0-100),数值越大美颜越明显
beautyFilter.setBeautyLevel(75)
//设置美颜滤镜到 GPUImage
gpuImage.setFilter(beautyFilter)

//直接将美颜后的结果渲染到mGlSurfaceView上
gpuImage.setGLSurfaceView(mGlSurfaceView);

//设置关键点追踪回调
gpuImage.setPreviewResultCallback(Activity.this);
//设置 ICamera 用来获取相机实例
ICamera mICamera = new ICamera();
//设置相机属性,旋转角度
gpuImage.setUpCamera(mCamera, degree);
//关键点回调,返回关键点数组和预览图像宽高
@Override
public void getLandMarkResult(float[] points, int width, int height) {

}


```

##关键点
coming soon...

##美颜
coming soon...

##贴纸
coming soon...

##自定义贴纸通道
coming soon...

##鉴权
coming soon...



