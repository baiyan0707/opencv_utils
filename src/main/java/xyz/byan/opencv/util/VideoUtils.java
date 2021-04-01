package xyz.byan.opencv.util;

import lombok.experimental.UtilityClass;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

/**
 * @author: bai
 * @date: 2021/1/29 16:11.
 * @description: 视频处理工具类 - 基于 opencv
 */
@UtilityClass
public class VideoUtils {

    private static final Logger log = LoggerFactory.getLogger(VideoUtils.class);

    static CascadeClassifier faceDetector;

    static int i;

    private static final int NUM = 1;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            faceDetector = new CascadeClassifier(String.valueOf(ResourceUtils
                    .getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "haarcascades/haarcascade_frontalface_alt.xml")));
        } catch (FileNotFoundException e) {
            log.error("opencv初始化失败:{}",e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从文件中将视频转换为图片
     * @param videoPath 视频路径
     * @param imgPath 图片保存路径，保存图片为人脸图片
     * @param num 取帧间隔
     */
    public static void video2Img(String videoPath, String imgPath, int num){
        // get video
        VideoCapture capture = new VideoCapture(videoPath);
        // 获取帧数
        double totalFrameNumber = capture.get(7);
        log.info("总帧数：{}",totalFrameNumber);
        // 定义一个Mat变量，用来存放存储每一帧图像
        Mat frame = new Mat();
        // 循环标志位
        boolean flags = true;
        // 定义当前帧
        long currentFrame = 0;
        // 定义保存图片数
        long sum = 0;

        while (flags) {
            // 读取视频每一帧
            capture.read(frame);
            if (currentFrame % num == 0) {
                String fname = sum + ".jpg";
                // 将帧转成图片输出
                Imgcodecs.imwrite(imgPath + "/" + fname, frame);
                sum++;
            }
            if (currentFrame >= totalFrameNumber) {
                flags = false;
            }
            currentFrame++;
        }
        capture.release();
        log.info("视频解析结束！");
    }

    /**
     * OpenCV-4.1.1 将摄像头拍摄的视频写入本地
     * @param imgPath 保存路径
     * @return: void
     */
    public static void writeVideo(String imgPath) {
        //1 如果要从摄像头获取视频 则要在 VideoCapture 的构造方法写 0
        VideoCapture capture=new VideoCapture(0);
        Mat video=new Mat();
        int index;
        Size size=new Size(capture.get(Videoio.CAP_PROP_FRAME_WIDTH),capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        VideoWriter writer=new VideoWriter(imgPath,VideoWriter.fourcc('D', 'I', 'V', 'X'), 15.0,size, true);
        while(capture.isOpened()) {
            //2 将摄像头的视频写入 Mat video 中
            capture.read(video);
            writer.write(video);
            //3 显示图像
            HighGui.imshow("摄像头获取视频", video);
            //4 获取键盘输入
            index=HighGui.waitKey(100);
            //5 如果是 Esc 则退出
            if(index==27) {
                capture.release();
                writer.release();
                return;
            }
        }
    }

    /**
     * OpenCV-4.1.1 从本地摄像头实时读取
     * @param savePath 保存路径
     * @return: void
     */
    public static void getVideoFromCamera(String savePath) {
        //1 如果要从摄像头获取视频 则要在 VideoCapture 的构造方法写 0
        VideoCapture capture=new VideoCapture(0);
        Mat video=new Mat();
        int index;
        int i = 0;
        if (capture.isOpened()) {
            // 匹配成功3次退出
            while(i<3) {
                capture.read(video);
                HighGui.imshow("实时人脸识别", getFace(video,savePath));
                index=HighGui.waitKey(100);
                if (index==27) {
                    capture.release();
                    break;
                }
            }
        }else{
            log.error("摄像头未开启");
        }
        try {
            capture.release();
            Thread.sleep(1000);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * OpenCV-4.1.1 从视频文件中读取
     * @param src 视频全路径
     * @param imgPath 图片保存路径,保存图片为某一帧人脸图片
     * @return: void
     */
    public static void getVideoFromFile(String src,String imgPath) {
        VideoCapture capture=new VideoCapture();
        //1 读取视频文件的路径
        capture.open(src);

        if(!capture.isOpened()){
            System.out.println("读取视频文件失败！");
            return;
        }
        Mat video=new Mat();
        int index;
        while(capture.isOpened()) {
            //2 视频文件的视频写入 Mat video 中
            capture.read(video);
            //3 显示图像
            HighGui.imshow("本地视频识别人脸", getFace(video,imgPath));
            //4 获取键盘输入
            index=HighGui.waitKey(100);
            //5 如果是 Esc 则退出
            if(index==27) {
                capture.release();
                return;
            }
        }
    }

    /**
     * OpenCV-4.1.1 人脸识别
     * @param image 待处理Mat图片(视频中的某一帧)
     * @return 处理后的图片
     */
    public static Mat getFace(Mat image,String savePath) {
        // 1 读取OpenCV自带的人脸识别特征XML文件(faceDetector)
        // 2  特征匹配类
        MatOfRect face = new MatOfRect();
        // 3 特征匹配
        faceDetector.detectMultiScale(image, face);
        Rect[] rects=face.toArray();
        System.out.println("匹配到 "+rects.length+" 个人脸");
        if(rects != null && rects.length >= 1) {

            // 4 为每张识别到的人脸画一个圈
            for (int i = 0; i < rects.length; i++) {
                Imgproc.rectangle(image, new Point(rects[i].x, rects[i].y), new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height), new Scalar(0, 255, 0));
                Imgproc.putText(image, "Human", new Point(rects[i].x, rects[i].y), Imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX, 1.0, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA, false);
            }
            i++;
            // 获取匹配成功第10次的照片
            if(i==3) {
                Imgcodecs.imwrite(savePath + "/face.png", image);
            }
        }
        return image;
    }

    /**
     * 本地视频抽帧
     * @param time 抽帧时长
     * @param videoPath 视频路径
     * @param savePath 保存路径
     */
    public void localFrame(int time,String videoPath,String savePath) {
        try {
            long start = System.currentTimeMillis();
            long end = start + time * 1000;

            log.info("开始抽取本地视频，抽取时长 {} 秒", time);
            log.info("获取本地视频，视频路径为:{}", videoPath );
            // get video
            VideoCapture capture = new VideoCapture(videoPath);
            if (!capture.isOpened()) {
                log.error("打开视频失败.");
                System.exit(0);
            }
            // 获取帧数
            double totalFrameNumber = capture.get(7);
            log.info("总帧数：{},保存路径为:{}", totalFrameNumber, savePath);

            // 定义当前帧
            long currentFrame = 0;
            // 定义保存图片数
            long sum = 0;
            // 定义一个Mat变量，用来存放存储每一帧图像
            Mat frame = new Mat();

            while (System.currentTimeMillis() < end) {

                // 读取视频每一帧
                capture.read(frame);
                if (currentFrame % NUM == 0) {
                    String fname = sum + ".jpg";
                    // 将帧转成图片输出
                    Imgcodecs.imwrite(savePath + "/" + fname, frame);
                    frame.release();
                    sum++;
                }
                currentFrame++;
            }

            capture.release();
            log.info("视频解析结束,抽取图片为:{} 张。", sum);
        } catch (Exception e) {
            log.error("抽帧异常:{}", e.getMessage());
        }
    }

    /**
     * 直播流
     * @param time 抽帧时长
     * @param savePath 保存路径
     * @param url 视频地址
     */
    public void rtmpFrame(int time,String url,String savePath) {
        try {
            long start = System.currentTimeMillis();
            long end = start + time * 1000;

            log.info("开始抽取直播流视频，抽取时长 {} 秒",time);
            log.info("直播流地址为:{}",url);
            // get video
            VideoCapture capture = new VideoCapture(url);
            if (!capture.isOpened()) {
                log.error("打开直播流失败");
                System.exit(0);
            }

            log.info("保存路径为:{}", savePath);
            // 定义一个Mat变量，用来存放存储每一帧图像
            Mat frame = new Mat();
            // 定义当前帧
            long currentFrame = 0;
            // 定义保存图片数
            long sum = 0;

            while (System.currentTimeMillis() < end) {
                if ((System.currentTimeMillis() - start) % (60 * 1000) == 0) {
                    log.info("当前以执行 {} 秒,抽取图片为: {} 张", (System.currentTimeMillis() - start) / 1000, sum);
                }
                // 读取视频每一帧
                capture.read(frame);
                if (currentFrame % NUM == 0) {
                    String fname = sum + ".jpg";
                    // 将帧转成图片输出
                    Imgcodecs.imwrite(savePath + "/" + fname, frame);
                    frame.release();
                    sum++;
                }
                currentFrame++;
            }
            capture.release();
            log.info("视频解析结束,抽取图片为:{} 张。", sum);
        } catch (Exception e) {
            log.error("抽帧异常:{}", e.getMessage());
        }
    }
}
