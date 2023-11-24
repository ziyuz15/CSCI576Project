package org.example;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_video.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.awt.image.BufferedImage;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;

public class Motion {
    public static double[] computeMotionStatistics(String videoPath) throws Exception {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
            frameGrabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Mat prevImage = new Mat(), currentImage = new Mat();
            Mat flow = new Mat();

            double[] motionStats = new double[frameGrabber.getLengthInFrames()];
            int frameIndex = 0;

            Frame frame = frameGrabber.grabImage();
            if (frame != null) {
                prevImage = converterToMat(converter.convert(frame));
                cvtColor(prevImage, prevImage, COLOR_BGR2GRAY);
            }

            while ((frame = frameGrabber.grabImage()) != null && frameIndex < motionStats.length) {
                currentImage = converterToMat(converter.convert(frame));
                cvtColor(currentImage, currentImage, COLOR_BGR2GRAY);

                calcOpticalFlowFarneback(prevImage, currentImage, flow, 0.5, 3, 15, 3, 5, 1.2, 0);

                // Calculate and store the motion statistic for the current frame
                motionStats[frameIndex++] = calculateMotionFromFlow(flow);

                prevImage = currentImage.clone();
            }

            frameGrabber.stop();
            return motionStats;
        }
    }

    private static double calculateMotionFromFlow(Mat flow) {
        double motionSum = 0.0;
        FloatRawIndexer indexer = flow.createIndexer();
        float[] vector = new float[2]; // To store the X and Y components of the flow vector

        for (int y = 0; y < flow.rows(); y++) {
            for (int x = 0; x < flow.cols(); x++) {
                indexer.get(y, x, vector); // Get both components of the flow vector
                double fx = vector[0]; // X component
                double fy = vector[1]; // Y component
                motionSum += Math.sqrt(fx * fx + fy * fy); // Magnitude of the flow vector
            }
        }
        indexer.release();
        return motionSum / (flow.rows() * flow.cols()); // Average motion
    }


    private static Mat converterToMat(BufferedImage image) {
        return new OpenCVFrameConverter.ToMat().convert(new Java2DFrameConverter().convert(image));
    }
}

//
//public class Motion {
//    private static boolean debugCall = true;
//    public static int[] computeMotionStatistics(String videoPath) throws Exception {
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
//            frameGrabber.start();
//
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            int frameCount = frameGrabber.getLengthInFrames();
//            int[] motionStats = new int[frameCount];
//            BufferedImage prevImage = null, currentImage;
//
//            for (int i = 0; i < frameCount; i++) {
//                Frame frame = frameGrabber.grabImage();
//                if (frame == null) break;
//                currentImage = converter.convert(frame);
//
//                if (prevImage != null && currentImage != null) {
//                    motionStats[i] = calculateFrameDifference(prevImage, currentImage);
//                }
//                prevImage = currentImage;
//            }
//
//            frameGrabber.stop();
//            return motionStats;
//        }
//    }
//
//    private static int calculateFrameDifference(BufferedImage img1, BufferedImage img2) {
//        int width = img1.getWidth();
//        int height = img1.getHeight();
//        int motion = 0;
//
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int rgb1 = img1.getRGB(x, y);
//                int rgb2 = img2.getRGB(x, y);
//                int diff = Math.abs(rgb1 - rgb2);
//                motion += diff;
//            }
//        }
//        if (debugCall) { // Assume debugCall is a static boolean initialized to true
//            for (int y = 0; y < Math.min(height, 10); y++) {
//                for (int x = 0; x < Math.min(width, 10); x++) {
//                    int rgb1 = img1.getRGB(x, y);
//                    int rgb2 = img2.getRGB(x, y);
//                    System.out.println("Pixel [" + x + ", " + y + "]: " + rgb1 + " vs " + rgb2);
//                }
//            }
//            debugCall = false; // Set to false so this only prints once
//        }
//
//        return motion;
//    }
//}






