package org.example;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Random;
public class AudioSplitter {
    public static void splitAudioFile(String sourceFilePath, int numberOfSplits) throws UnsupportedAudioFileException, IOException {
        // 加载原始音频文件
        File sourceFile = new File(sourceFilePath);
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(sourceFile);
        AudioFormat format = sourceStream.getFormat();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("\\") + 1).replaceAll("\\.mp4$", "");
        // 获取音频长度（单位：秒）
        long frames = sourceStream.getFrameLength();
        double durationInSeconds = frames / format.getFrameRate();

        // 检查文件长度是否足够
        if (durationInSeconds < 20 * numberOfSplits) {
            throw new IllegalArgumentException("Audio file is too short for the number of splits!");
        }

        // 生成随机开始点
        Random random = new Random();
        long[] startPoints = new long[numberOfSplits];
        for (int i = 0; i < numberOfSplits; i++) {
            // 确保每段音频长度为 20 秒
            startPoints[i] = (long) ((random.nextDouble() * (durationInSeconds - 20)) * format.getFrameRate());
        }

        // 创建拆分的音频文件
        for (int i = 0; i < numberOfSplits; i++) {
            sourceStream = AudioSystem.getAudioInputStream(sourceFile); // 重新打开流
            sourceStream.skip(startPoints[i] * format.getFrameSize()); // 跳到开始点

            long framesToRead = (long) (20 * format.getFrameRate()); // 20 秒的帧数
            AudioInputStream splitStream = new AudioInputStream(sourceStream, format, framesToRead);

            File outputFile = new File(fileName + (i + 1) + ".wav");
            AudioSystem.write(splitStream, AudioFileFormat.Type.WAVE, outputFile);

            splitStream.close();
        }

        sourceStream.close();
    }

    public static void main(String[] args) {
        try {
            for(int i = 15; i <= 20; i++){
                splitAudioFile("D:\\USC\\CSCI576\\Audios_Test\\video"+i+".wav", new Random().nextInt(2) + 2); // 产生 2 到 3 之间的随机数
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
