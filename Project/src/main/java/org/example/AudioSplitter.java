package org.example;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Random;
public class AudioSplitter {
    public static void splitAudioFile(String sourceFilePath, int numberOfSplits) throws UnsupportedAudioFileException, IOException {
        File sourceFile = new File(sourceFilePath);
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(sourceFile);
        AudioFormat format = sourceStream.getFormat();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("\\") + 1).replaceAll("\\.wav$", "");

        long frames = sourceStream.getFrameLength();
        double durationInSeconds = frames / format.getFrameRate();

        if (durationInSeconds < 20 * numberOfSplits) {
            throw new IllegalArgumentException("Audio file is too short for the number of splits!");
        }

        Random random = new Random();
        long[] startPoints = new long[numberOfSplits];
        File logFile = new File(fileName + "_splits.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));

        for (int i = 0; i < numberOfSplits; i++) {
            startPoints[i] = (long) ((random.nextDouble() * (durationInSeconds - 20)) * format.getFrameRate());
            writer.write("Split " + (i + 1) + ": Start at " + (startPoints[i] / format.getFrameRate()) + " seconds\n");

            sourceStream = AudioSystem.getAudioInputStream(sourceFile);
            sourceStream.skip(startPoints[i] * format.getFrameSize());

            long framesToRead = (long) (20 * format.getFrameRate());
            AudioInputStream splitStream = new AudioInputStream(sourceStream, format, framesToRead);

            File outputFile = new File(fileName + "_" + (i + 1) + ".wav");
            AudioSystem.write(splitStream, AudioFileFormat.Type.WAVE, outputFile);

            splitStream.close();
        }

        writer.close();
        sourceStream.close();
    }

    public static void main(String[] args) {
        try {
//            for(int i = 15; i <= 20; i++){
//                splitAudioFile("D:\\USC\\CSCI576\\Audios_Test\\video"+i+".wav", new Random().nextInt(2) + 2); // 产生 2 到 3 之间的随机数
//            }
            splitAudioFile("D:\\USC\\CSCI576\\Audios_Test\\video"+7+".wav", new Random().nextInt(2) + 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
