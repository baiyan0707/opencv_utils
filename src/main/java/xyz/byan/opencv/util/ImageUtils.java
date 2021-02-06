package xyz.byan.opencv.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.lang.reflect.Field;

/**
 * @author bai
 * @time
 */
@Log4j2
public class ImageUtils {

    /**
     * byte[] 转 bitmap
     * @param bytes
     * @return
     */
    public static Bitmap bytes2Bitmap(final byte[] bytes) {
        return (bytes == null || bytes.length == 0)
                ? null
                : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Drawable 转 bytes
     * @param drawable
     * @param format
     * @return
     */
    public static byte[] drawable2Bytes(final Drawable drawable, final Bitmap.CompressFormat format) {
        return drawable == null ? null : bitmap2Bytes(drawable2Bitmap(drawable), format);
    }

    /**
     * Bitmap 转 bytes
     * @param bitmap
     * @param format
     * @return
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final Bitmap.CompressFormat format) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Drawable 转 bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(final Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 帧流 转 bitmap ARGB_8888
     * @param data
     * @param width
     * @param height
     * @return
     */

    public static Bitmap rawBytes2RGBABitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }

    /**
     * 获取V7包下 ImageView Id
     * @param imageView
     * @return
     */
    public static int getV7ImageResourceId(ImageView imageView) {
        int imgid = 0;
        Field[] fields=imageView.getClass().getDeclaredFields();
        for(Field f:fields){
            if("mBackgroundTintHelper".equals(f.getName())){
                f.setAccessible(true);
                try {
                    Object obj = f.get(imageView);
                    Field[] fields2=obj.getClass().getDeclaredFields();
                    for(Field f2:fields2){
                        if("mBackgroundResId".equals(f2.getName())){
                            f2.setAccessible(true);
                            imgid = f2.getInt(obj);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return imgid;
    }

    /**
     * 创建缩略图
     * @param largeImagePath 原始大图路径
     * @param square_size 输出图片宽度
     * @return
     */
    public static Bitmap createImageThumbnail(String largeImagePath, int square_size){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        // 原始图片bitmap
        Bitmap cur_bitmap = getBitmapByPath(largeImagePath, opts);

        if (cur_bitmap == null) {
            return null;
        }
        // 原始图片的高宽
        int[] cur_img_size = new int[] { cur_bitmap.getWidth(), cur_bitmap.getHeight() };

        // 计算原始图片缩放后的宽高
        int[] new_img_size = scaleImageSize(cur_img_size, square_size);

        if (new_img_size[0] > square_size) {
            // 生成缩放后的bitmap
            return zoomBitmap(cur_bitmap, new_img_size[0], new_img_size[1]);
        } else {
            return cur_bitmap;
        }
    }

    /**
     * 计算缩放图片的宽高
     *
     * @param img_size
     * @param square_size
     * @return
     */
    public static int[] scaleImageSize(int[] img_size, int square_size) {
        if (img_size[0] <= square_size && img_size[1] <= square_size) {
            return img_size;
        }
        double ratio = square_size / (double) Math.max(img_size[0], img_size[1]);
        return new int[] { (int) (img_size[0] * ratio), (int) (img_size[1] * ratio) };
    }

    /**
     * 获取图片 通过资源文件
     * @param context
     * @param resId
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getBitmapByResource(Context context,@DrawableRes final int resId,
                                             final int maxWidth,
                                             final int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        final Resources resources = context.getResources();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }


    /**
     * 获取图片 通过路径
     *
     * @param filePath
     * @param opts
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis, null, opts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    /**
     * 获取图片 通过文件
     *
     * @param file
     * @return
     */
    public static Bitmap getBitmapByFile(final File file) {
        if (file == null) {
            return null;
        }
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * 获取bitmap
     * @param file
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap getBitmapByFile(final File file, final int maxWidth, final int maxHeight) {
        if (file == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }


    /**
     * 缩小图片 解决内存溢出
     * @param imgPath
     * @param w
     * @param h
     * @param config
     * @return
     */
    public static Bitmap zoomPic(String imgPath, int w, int h, Bitmap.Config config) {
        Bitmap bitmap = decodeSampledBitmapFromPath(imgPath, w, h, config);
        return bitmap;
    }

    /**
     * 缩小图片(精准缩放w/h为指定值)
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newbmp = null;
        try {
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width < w && height < h) {
                    return bitmap;// 不进行放大操作
                }

                Matrix matrix = new Matrix();
                float scaleWidht = ((float) w / width);/// 940/1922
                float scaleHeight = ((float) h / height);
                if (scaleWidht > scaleHeight) {
                    matrix.postScale(scaleHeight, scaleHeight);
                } else {
                    matrix.postScale(scaleWidht, scaleWidht);
                }
                newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                bitmap.recycle();
                bitmap = null;
            }
        } catch (Error e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return newbmp;
    }

    /**
     * 截取中央圆形图片 按照最小边为半径
     * @param bmp
     * @param width
     * @param radius
     * @return
     */
    public static Bitmap ClipSquareBitmap(Bitmap bmp, int width, int radius) {
        if (bmp == null || width <= 0) {
            return null;
        }
        //如果图片比较小就没必要进行缩放了
        //把图片进行缩放，以宽高最小的一边为准，缩放图片比例
        if (bmp.getWidth() > width && bmp.getHeight() > width) {
            if (bmp.getWidth() > bmp.getHeight()) {
                bmp = Bitmap.createScaledBitmap(bmp, (int) (((float) width) * bmp.getWidth() / bmp.getHeight()), width, false);
            } else {
                bmp = Bitmap.createScaledBitmap(bmp, width, (int) (((float) width) * bmp.getHeight() / bmp.getWidth()), false);
            }

        } else {
            width = bmp.getWidth() > bmp.getHeight() ? bmp.getHeight() : bmp.getWidth();
            if (radius > width) {
                radius = width;
            }
        }
        Bitmap output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        //设置画笔全透明
        canvas.drawARGB(0, 0, 0, 0);
        Paint paints = new Paint();
        paints.setColor(Color.WHITE);
        paints.setAntiAlias(true);//去锯齿
        paints.setFilterBitmap(true);
        //防抖动
        paints.setDither(true);

        //把图片圆形绘制矩形
        if (radius <= 0) {
            canvas.drawRect(new Rect(0, 0, width, width), paints);
        } else //绘制圆角
        {
            canvas.drawRoundRect(new RectF(0, 0, width, width), radius, radius, paints);
        }
        // 取两层绘制交集。显示前景色。
        paints.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect();
        if (bmp.getWidth() >= bmp.getHeight()) {
            rect.set((bmp.getWidth() - width) / 2, 0, (bmp.getWidth() + width) / 2, width);
        } else {
            rect.set(0, (bmp.getHeight() - width) / 2, width, (bmp.getHeight() + width) / 2);
        }
        Rect rect2 = new Rect(0, 0, width, width);
        //第一个rect 针对bmp的绘制区域，rect2表示绘制到上面位置
        canvas.drawBitmap(bmp, rect, rect2, paints);
        bmp.recycle();
        return output;
    }

    /**
     * 截取长方形图片
     * @param src
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static Bitmap clipRec(final Bitmap src,final int x,final int y,final int width,final int height) {
        if (isEmptyBitmap(src)) {
            return null;
        }
        Bitmap ret = Bitmap.createBitmap(src, x, y, width, height);
        return ret;
    }

    /**
     * 图片压缩解决内存溢出
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromPath(String filePath, int reqWidth, int reqHeight,
                                                     Bitmap.Config config) {
        Bitmap bitmap = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        int inSampleSize= calculateInSampleSize(options, reqWidth, reqHeight);
        options.inSampleSize = inSampleSize;
        try {
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            if (config == null) {
                config = Bitmap.Config.RGB_565;
            }
            options.inPreferredConfig = config;
            bitmap= BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e) {
            try {
                options.inSampleSize=inSampleSize+2;
                options.inJustDecodeBounds = false;
                if (config == null) {
                    config = Bitmap.Config.RGB_565;
                }
                options.inPreferredConfig = config;
                bitmap= BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int inSampleSizeH = Math.round((float) height / (float) reqHeight);
            int inSampleSizeW = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(inSampleSizeH, inSampleSizeW);
        }
        if (inSampleSize % 2 == 1) {
            inSampleSize++;
        }
        while (width / inSampleSize > reqWidth || height / inSampleSize > reqHeight) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * 保存图片到指定路径
     * @param src
     * @param filePath
     * @param format
     * @param quality
     * @return
     */
    public static boolean saveImg2File(final Bitmap src,
                                       final String filePath,
                                       final Bitmap.CompressFormat format,
                                       final int quality) {
        return saveImg2File(src, getFileByPath(filePath), format,quality, false);
    }

    /**
     * 保存图片到指定路径
     * @param src
     * @param file
     * @param format
     * @param quality
     * @return
     */
    public static boolean saveImg2File(final Bitmap src, final File file, final Bitmap.CompressFormat format,final int quality) {
        return saveImg2File(src, file, format,quality, false);
    }

    /**
     * 保存图片到指定路径
     * @param src
     * @param filePath
     * @param format
     * @param quality
     * @param recycle
     * @return
     */
    public static boolean saveImg2File(final Bitmap src,
                                       final String filePath,
                                       final Bitmap.CompressFormat format,
                                       final int quality,
                                       final boolean recycle) {
        return saveImg2File(src, getFileByPath(filePath), format, quality,recycle);
    }

    /**
     * 保存图片到指定路径
     * @param src
     * @param file
     * @param format
     * @param quality 最大值100
     * @param recycle
     * @return
     */
    public static boolean saveImg2File(final Bitmap src,
                                       final File file,
                                       final Bitmap.CompressFormat format,
                                       final int quality,
                                       final boolean recycle) {
        if (isEmptyBitmap(src) || !createFileByDeleteOldFile(file)) {
            return false;
        }
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, quality, os);
            if (recycle && !src.isRecycled()) {
                src.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 设置水印图片在左上角
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    public static Bitmap createWaterMaskLeftTop(
            Context context, Bitmap src, Bitmap watermark,
            int paddingLeft, int paddingTop) {
        return createWaterMaskBitmap(src, watermark,
                dp2px(context, paddingLeft), dp2px(context, paddingTop));
    }

    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark,
                                                int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        return newb;
    }

    /**
     * 设置水印图片在右下角
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingBottom
     * @return
     */
    public static Bitmap createWaterMaskRightBottom(
            Context context, Bitmap src, Bitmap watermark,
            int paddingRight, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到右上角
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    public static Bitmap createWaterMaskRightTop(
            Context context, Bitmap src, Bitmap watermark,
            int paddingRight, int paddingTop) {
        return createWaterMaskBitmap( src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                dp2px(context, paddingTop));
    }

    /**
     * 设置水印图片到左下角
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static Bitmap createWaterMaskLeftBottom(
            Context context, Bitmap src, Bitmap watermark,
            int paddingLeft, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark, dp2px(context, paddingLeft),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到中间
     * @param src
     * @param watermark
     * @return
     */
    public static Bitmap createWaterMaskCenter(Bitmap src, Bitmap watermark) {
        return createWaterMaskBitmap(src, watermark,
                (src.getWidth() - watermark.getWidth()) / 2,
                (src.getHeight() - watermark.getHeight()) / 2);
    }

    /**
     * 给图片添加文字到左上角
     * @param context
     * @param bitmap
     * @param text
     * @return
     */
    public static Bitmap drawTextToLeftTop(Context context, Bitmap bitmap, String text,
                                           int size, int color, int paddingLeft, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到右下角
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingRight
     * @param paddingBottom
     * @return
     */
    public static Bitmap drawTextToRightBottom(Context context, Bitmap bitmap, String text,
                                               int size, int color, int paddingRight, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制文字到右上方
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    public static Bitmap drawTextToRightTop(Context context, Bitmap bitmap, String text,
                                            int size, int color, int paddingRight, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到左下方
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text,
                                              int size, int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制文字到中间
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @return
     */
    public static Bitmap drawTextToCenter(Context context, Bitmap bitmap, String text,
                                          int size, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                (bitmap.getWidth() - bounds.width()) / 2,
                (bitmap.getHeight() + bounds.height()) / 2);
    }

    //图片上绘制文字
    private static Bitmap drawTextToBitmap(Context context, Bitmap bitmap, String text,
                                           Paint paint, Rect bounds, int paddingLeft, int paddingTop) {
        Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }

    /**
     * 缩放图片
     * @param src
     * @param w
     * @param h
     * @return
     */
    public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = (float) (w / width);
            float scaleHeight = (float) (h / height);
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }

    /**
     * dip转pix
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    private static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    private static boolean isSpace(final String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() && !file.delete()) {
            return false;
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }



}

