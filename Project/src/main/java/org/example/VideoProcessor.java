package org.example;
//package videoProcessing.src;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.FlowLayout;
import java.util.LinkedList;
import javax.imageio.ImageIO;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.ffmpeg.*;

/**
 * This class contains the basic video and audio process functions.
 * <p>
 * I suggest you to create you own class for different signal processing
 * functions that you are in charge of and call your functions within processVideo function.
 *
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
//    public static void processVideo(String path) {
//
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
//            Frame currentFrame, nextFrame, audioFrame = new Frame();
//
//            frameGrabber.start();
//            currentFrame = frameGrabber.grabImage();
//            int n = 0;
//            while ((nextFrame  = frameGrabber.grabImage()) != null ||
//                    (audioFrame = frameGrabber.grabFrame(true, false, false, false)) != null) {
//                Java2DFrameConverter converter = new Java2DFrameConverter();
//                BufferedImage currentImage = converter.convert(currentFrame);
//                BufferedImage nextImage = converter.convert(nextFrame);
//                //read the video frame and store the rgb signals for example
//                //processFrame(currentImage);
//                //process frame with shot boundary details, color and motion
//                //example:
//                //proecss the rgb signals and create a digital signature
//
//                if (currentImage != null && nextImage != null) {
//                    //processFrame(currentImage); // 处理当前帧
//                    ShotBoundaryDetails.combinedDiff(currentImage, nextImage, 0, 0);
//
//                }
//                //read the audio frame's samples
//                if(audioFrame.samples != null){
//                    //read and process audio frame
//                }
//                try {
//
//                    ImageIO.write(currentImage, "png", new File("video_tmp\\"+n+".png"));
//                    ImageIO.write(nextImage, "png", new File("video_tmp\\"+n+1+".png"));
//                    n++;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                currentFrame = nextFrame;
//            }
//             System.out.println("num: "+ n);
//            frameGrabber.stop();
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//    }
//    public static void processVideo(String path) {
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
//            frameGrabber.start();
//
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            Frame currentFrame = frameGrabber.grabImage();
//            Frame nextFrame;
//            int n = 0;
//            int skip = 0;
//            while (currentFrame != null) {
//                BufferedImage currentImage = converter.convert(currentFrame);
//                BufferedImage nextImage = null;
//                // 处理当前帧...
//                if (currentImage != null) {
//                    ImageIO.write(currentImage, "png", new File("video_tmp\\" + n + ".png"));
//                }
//
//                nextFrame = frameGrabber.grabImage(); // 获取下一帧
//                if (nextFrame != null) {
//                    nextImage = converter.convert(nextFrame);
//                    // 处理下一帧...
//                    ImageIO.write(nextImage, "png", new File("video_tmp\\" + (n + 1) + ".png"));
//                }
//                if (currentImage != null && nextImage != null) {
//                    //processFrame(currentImage); // 处理当前帧
//                   if(skip >= 4){
//                       ShotBoundaryDetails.combinedDiff(currentImage, nextImage, 0, 0);
//                       skip = 0;
//                   }
//                   else{
//                       skip++;
//                       continue;
//                   }
//                }
//                System.out.println("Processing frame: " + n);
//                System.out.println("RGB of current frame first pixel: " + currentImage.getRGB(0, 0));
//                System.out.println("RGB of next frame first pixel: " + nextImage.getRGB(0, 0));
//                // 准备下次迭代
//                currentFrame = nextFrame;
//                n++;
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

            Java2DFrameConverter converter = new Java2DFrameConverter();
            LinkedList<Frame> frameWindow = new LinkedList<>();
            final int WINDOW_SIZE = 5; // 滑动窗口大小

            Frame frame;
            while ((frame = frameGrabber.grabImage()) != null) {
                frameWindow.add(frame);
                System.out.println(frameWindow.size());
                // 当窗口满时进行处理
                if (frameWindow.size() == WINDOW_SIZE) {
                    // 分析帧窗口中的帧
                    processFrameWindow(frameWindow, converter);

                    // 移除最旧的帧，为下一帧留空间
                    frameWindow.removeFirst();
                }
            }

            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processFrameWindow(LinkedList<Frame> frameWindow, Java2DFrameConverter converter) {
        // 在这里实现你的帧窗口分析逻辑
        // 例如，比较第一帧和最后一帧的差异
        BufferedImage firstImage = converter.convert(frameWindow.getFirst());
        BufferedImage lastImage = converter.convert(frameWindow.getLast());
        System.out.println("RGB of current frame firstImage pixel: " + firstImage.getRGB(10, 10));
        System.out.println("RGB of next frame lastImage pixel: " + lastImage.getRGB(10, 10));
        System.out.println(" ");
        ShotBoundaryDetails.combinedDiff(firstImage, lastImage, 0, 0);
        // 根据差异进行进一步处理
    }

}

