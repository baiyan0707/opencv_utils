package xyz.byan.ffmpeg;

import xyz.byan.ffmpeg.media.domain.enums.CrfValueEnum;
import xyz.byan.ffmpeg.media.domain.enums.PresetVauleEnum;
import xyz.byan.ffmpeg.util.MediaUtils;

/**
 * @author: bai
 * @author: bai
 * @date: 2021/2/4 13:54.
 * @description: ffmpeg_test
 */
public class FfmpegTest {
    public static void main(String[] args) {
        String input = "rtmp://rtmp01open.ys7.com/openlive/c63420704f8045c094309214f9ee40cd";
        String output = "/Users/bai/test.flv";

        MediaUtils.trmp2Httpflv(input,output,Boolean.TRUE,
                CrfValueEnum.MEDIUM_QUALITY.getCode(),
                PresetVauleEnum.MEDIUM_ZIP_SPEED.getPresetValue(),
                800,400,null,null,null,null);

    }
}
