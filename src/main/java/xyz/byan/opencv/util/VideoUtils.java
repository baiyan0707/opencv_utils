package xyz.byan.opencv.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: bai
 * @date: 2021/1/29 16:11.
 * @description: 视频处理工具类
 */
@Log4j2
@UtilityClass
public class VideoUtils {

    static CascadeClassifier faceDetector;

    static int i;

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
     * 从本地摄像头获取人脸头像并且保存
     */


    /**
     * 从外接摄像头实时读取
     */

    /**
     * 将外接摄像头数据保存到本地
     */

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
     * 截取摄像头画面并保存下来
     * @param savePath 保存路径
     * @throws InterruptedException
     * @throws FrameGrabber.Exception
     */
    public static void camera(String savePath) throws InterruptedException, FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();
        CanvasFrame canvas = new CanvasFrame("摄像头");
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        int ex = 0;

        while (true){
            if(!canvas.isDisplayable()){
                grabber.stop();
                System.exit(2);
                break;
            }

            // 获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像
            canvas.showImage(grabber.grab());
            ex++;
            // 保存图片
            doExecuteFrame(grabber.grabFrame(), ex,savePath);
            // 50毫秒刷新
            Thread.sleep(200);
        }
    }

    /**
     * 截取视频画面
     * @param filePath 文全件路径
     * @param size 需要截图的数量
     * @param savePath 保存路径
     * @throws FrameGrabber.Exception
     */
    private static void randomGrabberFFmpegImage(String filePath, int size, String savePath) throws FrameGrabber.Exception {
        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(filePath);
        ff.start();
        int ffLength = ff.getLengthInFrames();
        List<Integer> randomGrab = random(ffLength, size);
        int maxRandomGrab = randomGrab.get(randomGrab.size() - 1);
        Frame f;
        int i = 0;
        while (i < ffLength) {
            f = ff.grabImage();
            if (randomGrab.contains(i)) {
                doExecuteFrame(f, i,savePath);
            }
            if (i >= maxRandomGrab) {
                break;
            }
            i++;
        }
        ff.stop();
    }

    private static void doExecuteFrame(Frame f, int index, String savePath) {
        if (null == f || null == f.image) {
            return;
        }
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        BufferedImage bi = frameConverter.getBufferedImage(f);
        File output = new File(savePath + "/" + index + ".png");
        try {
            ImageIO.write(bi,"png",output);
        }catch (IOException e){
            log.error("截取视频画面保存异常:{}",e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Integer> random(int baseNum, int length) {
        List<Integer> list = new ArrayList<>(length);
        while (list.size() < length) {
            Integer next = (int) (Math.random() * baseNum);
            if (list.contains(next)) {
                continue;
            }
            list.add(next);
        }
        Collections.sort(list);
        return list;
    }
}
