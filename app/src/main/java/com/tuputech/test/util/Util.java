package com.tuputech.test.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by soap on 2017/1/20.
 */

public class Util {
    //测试地址
    public static String AUTH_URL = "http://183.60.177.232:8585/v1/app/auth";
    //请找我们申请
    public static String TP_APP_KEY  = "";

    public static String TP_APP_SECRET  ="";


    // 定义一个工具方法，将float[]数组转换为OpenGL ES所需的FloatBuffer
    public static FloatBuffer floatBufferUtil(float[] arr) {
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        FloatBuffer mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

}
