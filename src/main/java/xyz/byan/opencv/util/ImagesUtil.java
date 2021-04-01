package xyz.byan.opencv.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.imgproc.Imgproc.Sobel;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * @author: bai
 * @date: 2021/1/28 11:24.
 * @description: opencv image 工具类
 */
@Log4j2
@UtilityClass
public class ImagesUtil {

    public static final int MIN_VAL = 0;

    public static final int MAX_VAL = 255;

    /** 大于中间值表示增强效果,小于中间值表示消减效果 **/
    public static final int BASE_VAL = (MIN_VAL + MAX_VAL) >>> 1;

    static CascadeClassifier faceDetector;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            faceDetector = new CascadeClassifier(String.valueOf(ResourceUtils
                    .getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "haarcascades/haarcascade_frontalface_alt.xml")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int standardizeCellSize(int cellSize) {
        return cellSize % 2 == 0 ? cellSize + 1 : cellSize;
    }


    /**
     * 预览指定的图像
     * @param imageMat
     */
    public static void preview(Mat imageMat) {
        BufferedImage image = mat2BufferedImage(imageMat);
        preview(image);
    }

    /**
     * 预览指定的图像
     * @param image
     */
    public static void preview(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.setTitle("Preview");
        frame.setBounds(20, 20, image.getWidth(), image.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        JLabel label = new JLabel();
        label.setBounds(0, 0, image.getWidth(), image.getHeight());
        frame.getContentPane().add(label);
        label.setIcon(new ImageIcon(image));
        frame.setVisible(true);
    }

    /**
     * 预览指定的图像
     * @param filename
     * @throws IOException
     */
    public static void preview(String filename) throws IOException {
        File image = new File(filename);
        preview(image);
    }

    /**
     * 预览指定的图像
     * @param image
     * @throws IOException
     */
    public static void preview(File image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image);
        preview(bufferedImage);
    }

    /**
     * 将 opencv 中 Mat 对象转为 Java 中的 BufferedImage 对象。
     * @param mat
     */
    public static BufferedImage mat2BufferedImage(Mat mat) {
        int cols = mat.cols();
        int rows = mat.rows();
        int elemSize = (int) mat.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        mat.get(0, 0, data);
        switch (mat.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }
        BufferedImage image = new BufferedImage(cols, rows, type);
        image.getRaster().setDataElements(0, 0, cols, rows, data);
        return image;
    }

    /**
     * @Description: 图像饱和度调节
     * @Param: Mat 传入的图像矩阵
     * @Param: val 调节系数
     * @Return: 调节完成的图像矩阵
     */
    public static Mat shiftSaturability(Mat mat, int val) {
        if (val == BASE_VAL) {
            return mat;
        }
        Mat hsv = new Mat();
        double f;
        boolean flag = false;
        if (val > BASE_VAL) {
            flag = true;
            f = 1.0 * (val - BASE_VAL) / BASE_VAL;
        } else {
            f = 1.0 * val / BASE_VAL;
        }

        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

        for (int i = 0; i < hsv.height(); i++) {
            for (int j = 0; j < hsv.width(); j++) {
                double[] vals = hsv.get(i, j);
                if (flag) {
                    vals[1] = vals[1] + (MAX_VAL - vals[1]) * f;
                } else {
                    vals[1] = vals[1] * f;
                }
                hsv.put(i, j, vals);
            }
        }
        Mat ret = new Mat();
        Imgproc.cvtColor(hsv, ret, Imgproc.COLOR_HSV2BGR);
        return ret;
    }

    /**
     * @Description: 图像的对比度调节
     * @Param: Mat 传入的图像矩阵
     * @Param: val 调节系数
     * @Return: 调节完成的图像矩阵
     */
    public static Mat shiftContrast(Mat img, int val) {
        if (val == BASE_VAL) {
            return img;
        }
        Mat ret = new Mat(img.height(), img.width(), img.type());
        double maxx = -1, minn = 300.0;
        for (int i = 0; i < img.height(); i++) {
            for (int j = 0; j < img.width(); j++) {
                double[] vals = img.get(i, j);
                for (int k = 0; k < vals.length; k++) {
                    maxx = maxx > vals[k] ? maxx : vals[k];
                    minn = minn < vals[k] ? minn : vals[k];
                }
            }
        }
        double midd = (maxx + minn) / 2;
        double a;
        if(val>BASE_VAL){
            if(val==MAX_VAL) {
                val--;
            }
            a = BASE_VAL/(1.0*(MAX_VAL-val));
        }else{
            a = (1.0 * val) / BASE_VAL;
        }
        double b = midd * (1 - a);
        for (int i = 0; i < img.height(); i++) {
            for (int j = 0; j < img.width(); j++) {
                double[] vals = img.get(i, j);
                for (int k = 0; k < vals.length; k++) {
                    vals[k] = a * vals[k] + b;
                }
                ret.put(i, j, vals);
            }
        }
        return ret;
    }

    /**
     * @Description: 图像亮度调节
     * @Param: Mat 传入的图像矩阵
     * @Param: val 调节系数
     * @Return: 调节完成的图像矩阵
     */
    public static Mat shiftBrightness(Mat img, int val) {
        if (val == BASE_VAL) {
            return img;
        }
        Mat ret = new Mat(img.height(), img.width(), img.type());
        int hei = img.height();
        int wid = img.width();
        double f;
        boolean flag = false;
        if (val > BASE_VAL) {
            flag = true;
            f = 1.0 * (MAX_VAL - val) / BASE_VAL;
        } else {
            f = 1.0 * val / BASE_VAL;
        }
        for (int i = 0; i < hei; i++) {
            for (int j = 0; j < wid; j++) {
                double[] vals = img.get(i, j);
                for (int k = 0; k < vals.length; k++) {
                    if (flag) {
                        vals[k] = 255 - (vals[k] * f);
                    } else {
                        vals[k] = vals[k] * f;
                    }
                }
                ret.put(i, j, vals);
            }
        }
        return ret;
    }

    /**
     * @Description: 将矩阵进行转置
     * @Param: mat 要转置的图像矩阵
     * @Return: 转置过后的图像矩阵
     */
    public static Mat transposition(Mat mat) {
        Mat ret = new Mat(mat.width(), mat.height(), mat.type());
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                double[] vals = mat.get(i, j);
                ret.put(j, i, vals);
            }
        }
        return ret;
    }

    /**
     * @Description: 高斯函数
     * @Param: x 到达中心的x方向距离
     * @Param: y 到达中心的y方向距离
     * @Param: y 到达中心的y方向距离
     * @Return: variance 方差
     */
    private static double gaussianFunction(double x, double y, double variance) {
        return 1.0 / (2 * Math.PI * variance * variance) * Math.exp((-x * x - y * y) / (2 * variance * variance));
    }

    private static boolean chackRange(int i, int j, int hei, int wid) {
        return i >= 0 && i < hei && j >= 0 && j < wid;
    }

    private static Mat filtering(Mat mat, double[][] weightMatrix) {
        Mat ret = new Mat(mat.height(), mat.width(), mat.type());
        double[][][] m = new double[mat.height()][mat.width()][mat.get(0, 0).length];
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                m[i][j] = mat.get(i, j);
            }
        }

        int halfCellSize = weightMatrix.length >>> 1;
        int hei = mat.height();
        int wid = mat.width();
        for (int i = 0; i < hei; i++) {
            for (int j = 0; j < wid; j++) {
                double[] suma = new double[3];
                double sumb = 0;
                for (int k = 0; k < weightMatrix.length; k++) {
                    for (int l = 0; l < weightMatrix.length; l++) {
                        int loci = i - (halfCellSize - k);
                        int locj = j - (halfCellSize - l);
                        if (chackRange(loci, locj, mat.height(), mat.width())) {
                            sumb += weightMatrix[k][l];
                            for (int n = 0; n < suma.length; n++) {
                                suma[n] += weightMatrix[k][l] * m[loci][locj][n];
                            }
                        }
                    }
                }
                for (int k = 0; k < suma.length; k++) {
                    suma[k] /= sumb;
                }
                ret.put(i, j, suma);
            }
        }
        return ret;
    }

    public static Mat gaussianFiltering(Mat mat, int cellSize, double variance) {
        //通过高斯函数计算每一个格子的权值
        cellSize = standardizeCellSize(cellSize);
        int hCellSize = cellSize >>> 1;
        double[][] tCell = new double[cellSize][cellSize];
        double sum = 0;
        for (int i = 0; i < tCell.length; i++) {
            for (int j = 0; j < tCell[i].length; j++) {
                tCell[i][j] = gaussianFunction(hCellSize - i, hCellSize - j, variance);
                sum += tCell[i][j];
            }
        }
        for (int i = 0; i < tCell.length; i++) {
            for (int j = 0; j < tCell[i].length; j++) {
                tCell[i][j] = tCell[i][j] * 100 / sum;
            }
        }
        return filtering(mat, tCell);
    }

    /**
     * @Description: 最普通的均值滤波
     * @Param: mat 要滤波的图像矩阵
     * @Param: cellSize 滤波方框的大小
     * @Return: 滤波完成后的矩阵
     */
    public static Mat averageFiltering(Mat mat, int cellSize) {
        //TODO 性能优化
        Mat ret = new Mat(mat.height(), mat.width(), mat.type());
        double[][][] mm = new double[mat.height()][mat.width()][mat.get(0, 0).length];
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                mm[i][j] = mat.get(i, j);
            }
        }
        cellSize = standardizeCellSize(cellSize);
        int halfCellSiz = cellSize >> 1;
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                double[] to = new double[3];
                int fi = i - halfCellSiz, ti = i + halfCellSiz, fj = j - halfCellSiz, tj = j + halfCellSiz;
                for (int k = 0; k < to.length; k++) {
                    double sum = 0;
                    int cnt = 0;
                    for (int l = fi; l <= ti; l++) {
                        for (int m = fj; m <= tj; m++) {
                            if (l >= 0 && l < mat.height() && m >= 0 && m < mat.width()) {
                                sum += mm[l][m][k];
                                cnt++;
                            }
                        }
                    }
                    to[k] = sum / cnt;
                }
                ret.put(i, j, to);
            }
        }
        return ret;
    }

    /**
     * @Description: 将图片进行锐化
     * @Param: mat 传入的图像矩阵
     * @Param: cellSize 滤波使用的方框大小
     * @Param: factor 锐化强度
     * @Return: 锐化过后的矩阵
     */
    public static Mat sharpen(Mat mat, int cellSize, int factor) {
        Mat filter = averageFiltering(mat, cellSize);
        Mat ret = new Mat(mat.height(), mat.width(), mat.type());
        for (int i = 0; i < ret.height(); i++) {
            for (int j = 0; j < ret.width(); j++) {
                double[] rgb = mat.get(i, j);
                double[] frgb = filter.get(i, j);
                for (int k = 0; k < rgb.length; k++) {
                    rgb[k] += factor * (rgb[k] - frgb[k]);
                }
                ret.put(i, j, rgb);
            }
        }
        return ret;
    }

    /**
     *
     * 求图像的暗通道
     */
    private static double[][] getDarkChannel(Mat mat, int cellSize) {
        if (cellSize % 2 == 0) {
            cellSize++;
        }
        int hcellSize = cellSize >>> 1;
        double[][] doubles = new double[mat.height()][mat.width()];
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                double[] vals = mat.get(i, j);
                doubles[i][j] = vals[0];
                for (int k = 1; k < vals.length; k++) {
                    doubles[i][j] = (doubles[i][j] < vals[k]) ? doubles[i][j] : vals[k];
                }
            }
        }

        double[][] ret = new double[mat.height()][mat.width()];
        for (int i = 0; i < mat.height(); i++) {
            for (int j = 0; j < mat.width(); j++) {
                double maxx = 300;
                for (int k = 0; k < cellSize; k++) {
                    for (int l = 0; l < cellSize; l++) {
                        int locx = i + (k - hcellSize);
                        int locy = j + (l - hcellSize);
                        if (locx >= 0 && locx < ret.length && locy >= 0 && locy < ret[locx].length) {
                            maxx = (maxx < doubles[locx][locy]) ? maxx : doubles[locx][locy];
                        }
                    }
                }
                ret[i][j] = maxx;
            }
        }
        return ret;
    }

    /**
     * @Description: 图像进行去雾处理(何凯铭去雾)
     * @Param: mat 等待去雾的图像
     * @Param: cellSize 去雾时使用的方框大小
     * @Return: 去雾完成的图像
     */
    public static Mat disFog(Mat mat, int cellSize) {
        double[][] dackCha = getDarkChannel(mat, cellSize);
        Mat ret = new Mat(mat.height(), mat.width(), mat.type());

        for (int i = 0; i < ret.height(); i++) {
            for (int j = 0; j < ret.width(); j++) {
                double[] valsTo = new double[3];
                double[] valsFrom = mat.get(i, j);
                double f = dackCha[i][j] / 255;
                for (int k = 0; k < valsFrom.length; k++) {
                    valsTo[k] = (valsFrom[k] - 127 * f) / (1 - f);
                }
                ret.put(i, j, valsTo);
            }
        }
        return ret;
    }

    /**
     * @Description: 为图像进行边缘检测
     * @Param: img 等待边缘检测的图像矩阵
     * @Return: 表示图像边缘的矩阵
     */
    public static Mat edgeDetection(Mat img) {
        Mat clone = img.clone();
        int sizex = 5, sizey = 5;
        double dx = 0, dy = 0;
        Imgproc.GaussianBlur(clone, clone, new Size(sizex, sizey), dx, dy, BORDER_DEFAULT);
        Mat grad_x = new Mat(), grad_y = new Mat(), src_gray = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();
        cvtColor(clone, src_gray, Imgproc.COLOR_BGR2GRAY);
        int scale = 1;
        int delta = 0;
        int ddepth = CV_16S;
        Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_x, abs_grad_x);

        Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_y, abs_grad_y);
        Mat ret = new Mat();
        addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, ret);
        return ret;
    }

    /**
     * @Description: 为图像添加类似油画的效果
     * @Param: img 等待处理的图像
     * @Return: 经过油画处理的图像
     */
    public static Mat oilPainting(Mat img) {

        Mat mat1 = edgeDetection(img);
        Imgproc.GaussianBlur(img, img, new Size(11, 11), 20, 20, Core.BORDER_DEFAULT);
        for (int i = 0; i < img.height(); i++) {
            for (int j = 0; j < img.width(); j++) {
                double[] doubles = img.get(i, j);
                double[] doubles1 = mat1.get(i, j);
                for (int k = 0; k < doubles.length; k++) {
                    doubles[k] -= 1 * doubles1[0];
                }
                img.put(i, j, doubles);
            }
        }
        return img;
    }

    /**
     * 旋转图片为指定角度
     * @param splitImage
     * @param angle
     * @return
     */
    public static void rotate(Mat splitImage, double angle){
        double thera = angle * Math.PI / 180;
        double a = Math.sin(thera);
        double b = Math.cos(thera);

        int wsrc = splitImage.width();
        int hsrc = splitImage.height();

        int wdst = (int) (hsrc * Math.abs(a) + wsrc * Math.abs(b));
        int hdst = (int) (wsrc * Math.abs(a) + hsrc * Math.abs(b));
        Mat imgDst = new Mat(hdst, wdst, splitImage.type());

        Point pt = new Point(splitImage.cols() / 2, splitImage.rows() / 2);
        // 获取仿射变换矩阵
        Mat affineTrans = Imgproc.getRotationMatrix2D(pt, angle, 1.0);

        System.out.println(affineTrans.dump());
        // 改变变换矩阵第三列的值
        affineTrans.put(0, 2, affineTrans.get(0, 2)[0] + (wdst - wsrc) / 2);
        affineTrans.put(1, 2, affineTrans.get(1, 2)[0] + (hdst - hsrc) / 2);

        Imgproc.warpAffine(splitImage, imgDst, affineTrans, imgDst.size(),
                Imgproc.INTER_CUBIC | Imgproc.WARP_FILL_OUTLIERS);

        HighGui.imshow("结果",imgDst);

    }

    /**
     * OpenCV-4.1.1 图片人脸识别
     * @param srcPath 图片源文件路径
     * @param savePath 图片保存路径
     * @return: void
     */
    public static void face(String srcPath,String savePath) {
        // 1 读取OpenCV自带的人脸识别特征XML文件
        //OpenCV 图像识别库一般位于 opencv\sources\data 下面
        // 2 读取测试图片
        Mat image= Imgcodecs.imread(srcPath);
        if(image.empty()){
            System.out.println("image 内容不存在！");
            return;
        }
        // 3 特征匹配
        MatOfRect face = new MatOfRect();
        faceDetector.detectMultiScale(image, face);
        // 4 匹配 Rect 矩阵 数组
        Rect[] rects=face.toArray();
        log.info("匹配到:{} 个人脸",rects.length);
        // 5 为每张识别到的人脸画一个圈
        int i =1 ;
        for (Rect rect : face.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 3);
            // 进行图片裁剪
            imageCut(srcPath, savePath + "/"+i+".jpg", rect.x, rect.y, rect.width, rect.height);
            i++;
        }
        // 6 展示图片
        HighGui.imshow("人脸识别", image);
        HighGui.waitKey(0);
    }

    /**
     * 裁剪人脸
     * @param imagePath
     * @param outFile
     * @param posX
     * @param posY
     * @param width
     * @param height
     */
    public static void imageCut(String imagePath, String outFile, int posX, int posY, int width, int height) {
        // 原始图像
        Mat image = Imgcodecs.imread(imagePath);
        // 截取的区域：参数,坐标X,坐标Y,截图宽度,截图长度
        Rect rect = new Rect(posX, posY, width, height);
        // 两句效果一样
        Mat sub = image.submat(rect);
        Mat mat = new Mat();
        Size size = new Size(width, height);
        // 将人脸进行截图并保存
        Imgproc.resize(sub, mat, size);
        Imgcodecs.imwrite(outFile, mat);
        System.out.println(String.format("图片裁切成功，裁切后图片文件为： %s", outFile));

    }

    /**
     * 人脸比对
     * @param img_1
     * @param img_2
     * @return
     */
    public static double compareImage(String img_1, String img_2) {
        Mat mat_1 = conv_Mat(img_1);
        Mat mat_2 = conv_Mat(img_2);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();

        //颜色范围
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        //直方图大小， 越大匹配越精确 (越慢)
        MatOfInt histSize = new MatOfInt(1000);

        Imgproc.calcHist(Arrays.asList(mat_1), new MatOfInt(0), new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(mat_2), new MatOfInt(0), new Mat(), hist_2, histSize, ranges);

        // CORREL 相关系数
        double res = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);
        if (res > 0.72) {
            log.warn("人脸匹配,匹配值为：{}",res);
        } else {
            log.warn("人脸不匹配,匹配值为：{}",res);
        }
        return res;
    }

    /**
     * 灰度化人脸
     * @param img
     * @return
     */
    private static Mat conv_Mat(String img) {
        Mat image0 = Imgcodecs.imread(img);

        Mat image1 = new Mat();
        // 灰度化
        Imgproc.cvtColor(image0, image1, Imgproc.COLOR_BGR2GRAY);
        // 探测人脸
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image1, faceDetections);
        // rect中人脸图片的范围
        for (Rect rect : faceDetections.toArray()) {
            Mat face = new Mat(image1, rect);
            return face;
        }
        return null;
    }
}
