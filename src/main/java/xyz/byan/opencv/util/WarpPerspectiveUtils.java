package xyz.byan.opencv.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.Arrays;
import java.util.List;

/**
 * @author: bai
 * @date: 2021/1/28 16:12.
 * @description: 透视变换工具
 */
public class WarpPerspectiveUtils {

    /**
     * 透视变换
     * @param src
     * @param points
     * @return
     */
    public static Mat warpPerspective(Mat src , Point[] points) {

        // 点的顺序[左上 ，右上 ，右下 ，左下]
        List<Point> listSrcs = Arrays.asList(points[0], points[1], points[2], points[3]);
        Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);

        List<Point> listDsts = Arrays.asList(new Point(0, 0), new Point(src.width(), 0),
                new Point(src.width(), src.height()), new Point(0, src.height()));


        Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);

        Mat perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);

        Mat dst = new Mat();

        Imgproc.warpPerspective(src, dst, perspectiveMmat, src.size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP,
                1, new Scalar(0));

        return dst;

    }

}
