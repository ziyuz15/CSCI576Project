package org.example;

import java.io.IOException;
import java.util.Random;
import org.bytedeco.ffmpeg.*;
public class VideoSplitter {
    public static void main(String[] args) {
        String videoFilePath = "D:\\USC\\CSCI576\\Videos\\video13.mp4"; // 替换为你的视频文件路径
        int totalClips = 2 + new Random().nextInt(2); // 生成2或3

        for (int i = 0; i < totalClips; i++) {
            int startTime = new Random().nextInt(240); // 假设视频长度大于260秒，以确保20秒的剪辑
            String outputFilePath = "video13_" + i + ".mp4"; // 输出文件的命名

            String command = "ffmpeg -ss " + startTime + " -i " + videoFilePath +
                    " -t 20 -c copy " + outputFilePath;

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
