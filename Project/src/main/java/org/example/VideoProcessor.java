package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.*;



import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;

import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;


/**
 * This class contains the basic video and audio process functions.
 * <p>
 * I suggest you to create you own class for different signal processing
 * functions that you are in charge of and call your functions within processVideo function.
 * </p>
 * @author pan
 */
public class VideoProcessor {
    private static JLabel label1 = new JLabel();
    private static JLabel label2 = new JLabel();
    private static final int WIDTH = 352; // 帧宽度
    private static final int HEIGHT = 288; // 帧高度
    private static final int FRAME_SIZE = WIDTH * HEIGHT * 3; // 每帧的字节大小
    private static final int FRAME_RATE = 30;


    AudioProcess audioProcess = new AudioProcess("D:\\USC\\CSCI576\\Audios_Test\\");
    private static void createAndShowGUI() {
        // 创建 JFrame 实例
        JFrame frame = new JFrame("Video Frame Comparison");
        frame.setLayout(new FlowLayout());
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 添加标签到 JFrame
        frame.add(label1);
        frame.add(label2);

        // 显示窗口
        frame.setVisible(true);
    }

    /**
     * An example function for reading rgb signal from frames.
     * @param  img the bufferedImage we get from each frame
     * @return int[][] the array used to store each frame's rgb signal
     */
    private static int[][] processFrame(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] rgb = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgb[x][y] = img.getRGB(x, y);
            }
        }

        return rgb;
    }
    public static int processVideo(String path, double time, String queryPath){
        long startTime = System.nanoTime();
        int exactFrame = 0;
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
            frameGrabber.start();
            FFmpegFrameGrabber frameGrabber2 = new FFmpegFrameGrabber(queryPath);
            frameGrabber2.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Java2DFrameConverter converter2 = new Java2DFrameConverter();

            Frame queryFrame = frameGrabber2.grabImage();
            Frame videoFrame;
            BufferedImage queryImage = converter2.getBufferedImage(queryFrame);
            BufferedImage videoImage;

            double minDistance = 100;

            int frameNum = ShotBoundaryDetails.convertTimeToFrameNum(30,time);
            frameGrabber.setVideoFrameNumber(frameNum);
            videoFrame = frameGrabber.grabImage();
            videoImage = converter.getBufferedImage(videoFrame);
            if(queryImage == null){
                System.out.println("queryImage is null");
            }
            else if(videoImage == null){
                System.out.println("videoImage is null");
            }
            if(videoFrame == null){
                System.out.println("videoFrame is null");
            }
            System.out.println("Different Pixels Percentage: " + ShotBoundaryDetails.pixelxDiff(queryImage,videoImage) * 100 + " %");
            System.out.println(frameNum);
            System.out.println("ShotBoundaryDetails.hsvDiff(queryImage, videoImage): "+ ShotBoundaryDetails.hsvDiff(queryImage, videoImage));
            System.out.println("combinedDiff: "+ ShotBoundaryDetails.combinedDiff(queryImage,videoImage, 0.1,0.9));
            if(ShotBoundaryDetails.pixelxDiff(queryImage,videoImage) < 0.05 || ShotBoundaryDetails.hsvDiff(queryImage, videoImage) < 0.1){
                long endTime = System.nanoTime();
                long excuteTime = endTime - startTime;
                System.out.println("----------------------------------");
                System.out.println("");
                System.out.println("Pixel Difference matching running time: "+ excuteTime / 1_000_000_000.0 + "s");
                System.out.println("");
                System.out.println("----------------------------------");

                frameGrabber2.stop();
                frameGrabber2.release();
                return frameNum;
            }
            frameGrabber.setVideoFrameNumber(frameNum - 30);
            videoFrame = frameGrabber.grab();
            videoImage = converter.getBufferedImage(videoFrame);
            for(int i = frameNum - 10; i <= frameNum + 10; i++){
                if(videoImage != null && queryImage != null){
//                    double tmp = ShotBoundaryDetails.combinedDiff(queryImage, videoImage, 0.3, 0.7);
                    double tmp = ShotBoundaryDetails.pixelxDiff(queryImage,videoImage);
                    if(tmp < minDistance){
                        exactFrame = i;
                        minDistance = tmp;
                    }
                }
                videoFrame = frameGrabber.grab();
                videoImage = converter.getBufferedImage(videoFrame);
            }
            frameGrabber.stop();
            frameGrabber2.stop();
            frameGrabber2.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long excuteTime = endTime - startTime;
        System.out.println("----------------------------------");
        System.out.println("");
        System.out.println("Pixel Difference matching running time: "+ excuteTime / 1_000_000_000.0 + "s");
        System.out.println("");
        System.out.println("----------------------------------");
        return exactFrame;
    }
    /**
     * The function is used for extraing and excuting the processing the frames of Video and Audio from MP4 files.
     * @param path
     */
    public void processVideo(String path, int index) {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
            frameGrabber.start();
            FFmpegFrameGrabber frameGrabber2 = new FFmpegFrameGrabber(path);
            frameGrabber2.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Java2DFrameConverter converter2 = new Java2DFrameConverter();
            Frame previousFrame = new Frame();
            Frame currentFrame = frameGrabber.grabImage();
            BufferedImage previousImage = null;
            BufferedImage currentImage = converter.getBufferedImage(currentFrame);
            String wavPath = path.replaceAll("\\.mp4$", "");
            String fileName = path.substring(path.lastIndexOf("\\") + 1).replaceAll("\\.mp4$", "");

            ShortBuffer audioSamplesBuffer = ShortBuffer.allocate(1024 * 1024);
            double[][] signature;

            int frameCount = 0;
            int p = 0;
            int c = 1;
            int startFrame = 0;
            int endFrame = 0;
            int shots = 0;
            int lastPos = 0;
            double startTime = 0;
            double endTime = 0;

            ArrayList<Double> motionSignature = new ArrayList<>();
            while ((currentFrame = frameGrabber.grabImage()) != null){

                previousFrame = frameGrabber2.grabImage();
                previousImage = converter2.getBufferedImage(previousFrame);
                currentImage = converter.getBufferedImage(currentFrame);
                c++;
                p++;
                endFrame = p;

                if(currentImage != null){
//                    if(previousFrame.samples != null){
//                        getSampleShot(previousFrame, audioSamplesBuffer);
//                        System.out.println("flag");
//                    }
//                    if(currentFrame != null && previousFrame != null){
//                        motionSignature.add(MotionProcessor.computeMotionStatisticsShots(previousFrame, currentFrame));
//                    }
                    if(currentFrame.keyFrame && ShotBoundaryDetails.combinedDiff(previousImage, currentImage, 0.7, 0.3, path,
                            startFrame, endFrame)){
//                        saveMotionSignature("D:\\USC\\CSCI576\\CSCI576Project\\Project\\signature\\"+fileName+"_shot_" + shots + "_signature.csv", motionSignature);
//                        motionSignature = new ArrayList<Double>();
                        endTime = previousFrame.timestamp / 1_000_000.0;
                        startFrame = endFrame;
                        signature = audioProcess.createAudioSignatureForShots(index, startTime, endTime);
                        startTime = currentFrame.timestamp / 1_000_000.0;
                        shots++;

                        audioProcess.saveMagnitudeDataShot("D:\\USC\\CSCI576\\CSCI576Project\\Project\\signature", index, shots, signature);
                    }

//                    System.out.println("Processing previous frame: " + p);
//                    System.out.println("RGB of previous frame first pixel: " + previousImage.getRGB(0, 0));
//                    ImageIO.write(previousImage, "png", new File("video_tmp\\" + c + ".png"));
//                    System.out.println("Processing current frame: " + c);
//                    System.out.println("RGB of current frame first pixel: " + currentImage.getRGB(0, 0));
//                    ImageIO.write(currentImage, "png", new File("video_tmp\\" + c + ".png"));
//                    System.out.println(" ");

                }
                frameCount++;
            }

            currentFrame = frameGrabber2.grabImage();
            if (currentFrame != null) {

                currentImage = converter.getBufferedImage(currentFrame);
                if (currentImage != null && currentFrame.keyFrame && ShotBoundaryDetails.combinedDiff(previousImage, currentImage, 0.7, 0.3, path,
                        startFrame, endFrame)) {
                    System.out.println("Processing frame: " + frameCount);
                    shots++;
                }
                frameCount++;
            }
            endTime = previousFrame.timestamp / 1_000_000.0;
            signature = audioProcess.createAudioSignatureForShots(index, startTime, endTime);
            shots++;
            audioProcess.saveMagnitudeDataShot("D:\\USC\\CSCI576\\CSCI576Project\\Project\\signature", index, shots, signature);
//            saveMotionSignature("D:\\USC\\CSCI576\\CSCI576Project\\Project\\signature\\"+fileName+"_shot_" + shots + "_signature.csv", motionSignature);
//            System.out.println("Total frames processed: " + frameCount);
            System.out.println("Total shots found: " + shots);
            frameGrabber.stop();
            frameGrabber2.stop();
            frameGrabber2.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int processVideoRGB(String path, double time, String queryPath) {
        long startTime = System.nanoTime();
        int exactFrame = 0;
        double minDistance = Double.MAX_VALUE;

        try {
            ByteBuffer queryBuffer = ByteBuffer.allocate(FRAME_SIZE);

            // 读取查询帧
            try (FileInputStream fisQuery = new FileInputStream(queryPath)) {
                fisQuery.read(queryBuffer.array());
            }
            BufferedImage queryImage = convertToImage(queryBuffer);

            // 计算目标帧数
            int frameNum = (int) (time * FRAME_RATE) - 1;
            System.out.println("frameNum: "+ (frameNum + 1));
            try (FileInputStream fis = new FileInputStream(path)) {
                fis.skip((long) frameNum * FRAME_SIZE);

                byte[] frameBytes = new byte[FRAME_SIZE];
                int bytesRead = fis.read(frameBytes);
                System.out.println(bytesRead);
                if (bytesRead == FRAME_SIZE) {
                    BufferedImage videoImage = convertToImage(ByteBuffer.wrap(frameBytes));
                    System.out.println("pixelDiff : "+ ShotBoundaryDetails.pixelxDiff(queryImage, videoImage));
//                    if(ShotBoundaryDetails.pixelxDiff(queryImage, videoImage) < 0.05){
//                        long endTime = System.nanoTime();
//                        System.out.println("Video Match time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
//                        return frameNum + 1;
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int startFrame = Math.max(0, frameNum - 10);
            int endFrame = frameNum + 10;

            // 遍历帧
            try (FileInputStream fis = new FileInputStream(path)) {
                fis.skip((long) startFrame * FRAME_SIZE);

                for (int i = startFrame; i <= endFrame; i++) {
                    ByteBuffer buffer = ByteBuffer.allocate(FRAME_SIZE);
                    if (fis.read(buffer.array()) == -1) {
                        break;
                    }

                    BufferedImage videoImage = convertToImage(buffer);

                    if (videoImage != null && queryImage != null) {
                        double tmp = ShotBoundaryDetails.pixelxDiff(queryImage, videoImage);
                        if (tmp < minDistance) {
                            exactFrame = i;
                            minDistance = tmp;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        System.out.println("Video Match time: " + (endTime - startTime) / 1_000_000_000.0 + "s");
        return exactFrame;
    }

    private static BufferedImage convertToImage(ByteBuffer buffer) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int r = buffer.get() & 0xFF;
                int g = buffer.get() & 0xFF;
                int b = buffer.get() & 0xFF;
                int color = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, color);
            }
        }
        return image;
    }
    private void saveMotionSignature(String fileName, ArrayList<Double> motionSignature) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Double value : motionSignature) {
                writer.write(value.toString());
                writer.newLine(); // 每个值后面添加换行符
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

