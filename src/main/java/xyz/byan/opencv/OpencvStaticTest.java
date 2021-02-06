package xyz.byan.opencv;

import org.bytedeco.javacv.FrameGrabber;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

/**
 * @author: bai
 * @date: 2021/1/20 10:24.
 * @description: 图片人脸识别static测试
 */
public class OpencvStaticTest {

    static CascadeClassifier faceDetector;
    private static Mat img;


    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            faceDetector = new CascadeClassifier(String.valueOf(ResourceUtils
                    .getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "haarcascades/haarcascade_frontalface_alt.xml")));
            img = Imgcodecs.imread("/Users/bai/develop/opencv_data/img/wj_002.jpg");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
        showImg(img);

    }

    private static void showImg(Mat mat){
        HighGui.imshow("结果",mat);
        HighGui.waitKey();
    }
}
