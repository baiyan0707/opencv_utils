package xyz.byan.opencv.util;

import lombok.experimental.UtilityClass;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author: bai
 * @date: 2021/1/28 16:10.
 * @description: 归一化工具类
 */
@UtilityClass
public class ResizeUtils {

    /**
     * 把图片归一化到相同的大小
     *
     * @param src
     *            Mat矩阵对象
     * @return
     */
    public static Mat resize(Mat src) {
        return resize(src , new Size(28 , 28));
    }


    public static Mat resize(Mat src , Size dsize) {
        Mat dst = new Mat();
        // 区域插值(INTER_AREA):图像放大时类似于线性插值，图像缩小时可以避免波纹出现。
        Imgproc.resize(src, dst, dsize, 0, 0, Imgproc.INTER_AREA);
        return dst;
    }
}
