package org.example;
//package videoProcessing.src;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.Deque;
import javax.imageio.ImageIO;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.ffmpeg.*;

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
    /**
     * The function is used for extraing and excuting the processing the frames of Video and Audio from MP4 files.
     * @param path
     */
//    public static void processVideo(String path){
//        Deque<Frame> frameQueue = new LinkedList<>();
//        try( FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
//
//            frameGrabber.start();
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            Frame frame = new Frame();
//            frame = frameGrabber.grabImage();
//            Frame previousFrame = new Frame();
//            Frame currentFrame = new Frame();
//            int n = 0;
//            while (frame != null) {
//                frameQueue.addLast(frame);
//                if(frameQueue.size() > 3){
//                    frameQueue.removeFirst();
//                }
//                Frame frame1 = new Frame();
//                frame1 = frameQueue.getFirst();
//                Frame frame2 = new Frame();
//                frame2 = frameQueue.getLast();
//
//                BufferedImage buff1 = converter.convert(frame1);
//                BufferedImage buff2 = converter.convert(frame2);
//
//                if(buff2 != null){
//                    System.out.println("Processing frame: " + n);
//                    System.out.println("RGB of previousFrame frame first pixel: " + buff1.getRGB(0, 0));
//                    ImageIO.write(buff1, "png", new File("video_tmp\\" + n + ".png"));
//                    n = n + 1;
//                    System.out.println("Processing frame: " + n);
//                    System.out.println("RGB of current frame first pixel: " + buff2.getRGB(0, 0));
//                    ImageIO.write(buff2, "png", new File("video_tmp\\" + n + ".png"));
//                    System.out.println(" ");
//                }
//
//                frame = frameGrabber.grabImage();
//
//            }
//            frameGrabber.stop();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    public static void processVideo(String path) {
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
//            frameGrabber.start();
//
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            Frame currentFrame = frameGrabber.grabFrame();
//            Frame nextFrame = null;
//            int c = 0;
//            int n = 1;
//            while ((nextFrame = frameGrabber.grabFrame()) != null) {
//                BufferedImage currentImage = converter.convert(currentFrame);
//                BufferedImage nextImage = converter.convert(nextFrame);
//                // 处理并保存当前帧
//                if (nextImage != null) {
//                    System.out.println("Processing current frame: " + c);
//                    System.out.println("RGB of current frame first pixel: " + currentImage.getRGB(0, 0));
//                    ImageIO.write(currentImage, "png", new File("video_tmp\\" + c + ".png"));
//                    System.out.println("Processing next frame: " + n);
//                    System.out.println("RGB of next frame first pixel: " + nextImage.getRGB(0, 0));
//                    ImageIO.write(nextImage, "png", new File("video_tmp\\" + n + ".png"));
//                    System.out.println(" ");
//                    c++;
//                    n++;
//                }
//
//                // 获取并准备下一帧
//                Frame tmpFrame = nextFrame.clone();
//                currentFrame = tmpFrame;
////                if(currentFrame.keyFrame){
////                    System.out.println(currentFrame.timestamp);
////                }
//            }
//
//            System.out.println("num: " + n);
//            frameGrabber.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static void processVideo(String path) {
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
            int frameCount = 0;
            int p = 0;
            int c = 1;
            while ((currentFrame = frameGrabber.grabImage()) != null){
                previousFrame = frameGrabber2.grabImage();
                previousImage = converter2.getBufferedImage(previousFrame);
                currentImage = converter.getBufferedImage(currentFrame);
                c++;
                p++;
                if(currentImage != null && currentFrame.keyFrame){
//                    System.out.println("Processing previous frame: " + p);
//                    System.out.println("RGB of previous frame first pixel: " + previousImage.getRGB(0, 0));
//                    ImageIO.write(previousImage, "png", new File("video_tmp\\" + c + ".png"));
//                    System.out.println("Processing current frame: " + c);
//                    System.out.println("RGB of current frame first pixel: " + currentImage.getRGB(0, 0));
//                    ImageIO.write(currentImage, "png", new File("video_tmp\\" + c + ".png"));
//                    System.out.println(" ");

                    if(ShotBoundaryDetails.combinedDiff(previousImage, currentImage, 0.7 ,0.3) == true){
                        System.out.println("Processing previous frame: " + p);
                        System.out.println("Processing current frame: " + c);
                    }
                }
                frameCount++;
            }
            System.out.println("Total frames processed: " + frameCount);
            frameGrabber.stop();
            frameGrabber2.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void processVideo(String path) {
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
//            frameGrabber.start();
//
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            LinkedList<Frame> frameWindow = new LinkedList<>();
//            final int WINDOW_SIZE = 5; // 滑动窗口大小
//
//            Frame frame;
//            while ((frame = frameGrabber.grabImage()) != null) {
//                frameWindow.add(frame);
//                System.out.println(frameWindow.size());
//                // 当窗口满时进行处理
//                if (frameWindow.size() == WINDOW_SIZE) {
//                    // 分析帧窗口中的帧
//                    processFrameWindow(frameWindow, converter);
//
//                    // 移除最旧的帧，为下一帧留空间
//                    frameWindow.removeFirst();
//                }
//            }
//
//            frameGrabber.stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void processFrameWindow(LinkedList<Frame> frameWindow, Java2DFrameConverter converter) {
//        // 在这里实现你的帧窗口分析逻辑
//        // 例如，比较第一帧和最后一帧的差异
//        BufferedImage firstImage = converter.convert(frameWindow.getFirst());
//        BufferedImage lastImage = converter.convert(frameWindow.getLast());
//        System.out.println("RGB of current frame firstImage pixel: " + firstImage.getRGB(10, 10));
//        System.out.println("RGB of next frame lastImage pixel: " + lastImage.getRGB(10, 10));
//        System.out.println(" ");
//        ShotBoundaryDetails.combinedDiff(firstImage, lastImage, 0, 0);
//        // 根据差异进行进一步处理
//    }

}

