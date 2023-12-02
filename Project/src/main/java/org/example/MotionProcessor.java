package org.example;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_video.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
//import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
//import java.awt.image.DataBufferByte;

/**
 * To use this class, please ensure:
 * <br>
 * All the original video files and the query video file are at filePath;
 * <br>
 * Before running the program, adjust JVM options if necessary for memory management.
 */
public class MotionProcessor {
    private ArrayList<double[]> motionSignatureList; // List of motion signatures for each video
    private static final int TOTAL_VIDEO_NUMS = 11; // Total number of video files
    private String filePath;
    private boolean signaturesLoaded = false;

    public MotionProcessor(String filePath) {
        this.filePath = filePath;
        this.motionSignatureList = new ArrayList<>();
    }

    /**
     * Compute motion statistics for a given video path using optical flow.
     *
     * @param videoPath Path of the video file
     * @return Motion statistics array for the video
     */
//    private double[] computeMotionStatistics(String videoPath) {
//        try (FFmpegFrameGrabber frameGrabber1 = new FFmpegFrameGrabber(videoPath);
//             FFmpegFrameGrabber frameGrabber2 = new FFmpegFrameGrabber(videoPath)) {
//            frameGrabber1.start();
//            frameGrabber2.start();
//            frameGrabber2.grabImage(); // Advance the second grabber by one frame
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            List<Double> motionStats = new ArrayList<>();
//            Mat flow = new Mat();
//            Frame prevFrame, currentFrame;
//            BufferedImage prevImage, currentImage;
//
//            while ((prevFrame = frameGrabber1.grabImage()) != null &&
//                    (currentFrame = frameGrabber2.grabImage()) != null) {
//                prevImage = converter.convert(prevFrame);
//                currentImage = converter.convert(currentFrame);
//                if (prevImage == null || currentImage == null) {
//                    System.out.println("One of the frames is null, skipping...");
//                    continue;
//                }
//                Mat prevMat = bufferedImageToMat(prevImage);
//                Mat currentMat = bufferedImageToMat(currentImage);
//                cvtColor(prevMat, prevMat, COLOR_BGR2GRAY);
//                cvtColor(currentMat, currentMat, COLOR_BGR2GRAY);
//                calcOpticalFlowFarneback(prevMat, currentMat, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
//                if (!flow.empty()) {
//                    motionStats.add(calculateMotionFromFlow(flow));
//                } else {
//                    System.out.println("Flow matrix is empty.");
//                }
//            }
//            frameGrabber1.stop();
//            frameGrabber2.stop();
//            return motionStats.stream().mapToDouble(Double::doubleValue).toArray();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error processing video file: " + videoPath, e);
//        }
//    }

//    private double[] computeMotionStatistics(String videoPath) {
//        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
//            frameGrabber.start();
//            Java2DFrameConverter converter = new Java2DFrameConverter();
//            List<Double> motionStats = new ArrayList<>();
//            BufferedImage prevImage = null, currentImage;
//
//            Frame frame;
//            Mat flow = new Mat();
//
//            while ((frame = frameGrabber.grabImage()) != null) {
//                currentImage = converter.convert(frame);
//                if (currentImage == null) {
//                    System.out.println("Converted current frame is null.");
//                    continue;
//                }
//
//                Mat currentMat = new Mat(currentImage.getHeight(), currentImage.getWidth(), CV_8UC3);
//                currentMat = bufferedImageToMat(currentImage);
//
//                if (prevImage != null) {
//                    Mat prevMat = new Mat(prevImage.getHeight(), prevImage.getWidth(), CV_8UC3);
//                    prevMat = bufferedImageToMat(prevImage);
//
//                    cvtColor(prevMat, prevMat, COLOR_BGR2GRAY);
//                    cvtColor(currentMat, currentMat, COLOR_BGR2GRAY);
//
//                    calcOpticalFlowFarneback(prevMat, currentMat, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
//                    if (!flow.empty()) {
//                        motionStats.add(calculateMotionFromFlow(flow));
//                    } else {
//                        System.out.println("Flow matrix is empty.");
//                    }
//                }
//
//                prevImage = currentImage;
//            }
//
//            frameGrabber.stop();
//            return motionStats.stream().mapToDouble(Double::doubleValue).toArray();
//        } catch (Exception e) {
//            throw new RuntimeException("Error processing video file: " + videoPath, e);
//        }
//    }
//
//    private Mat bufferedImageToMat(BufferedImage bi) {
//        // Convert BufferedImage type to the appropriate OpenCV type
//        int cvType = 0;
//        switch (bi.getType()) {
//            case BufferedImage.TYPE_3BYTE_BGR:
//                cvType = org.bytedeco.opencv.global.opencv_core.CV_8UC3;
//                break;
//            case BufferedImage.TYPE_BYTE_GRAY:
//                cvType = org.bytedeco.opencv.global.opencv_core.CV_8UC1;
//                break;
//            // Add other cases for different BufferedImage types if necessary
//            default:
//                throw new IllegalArgumentException("Unsupported BufferedImage type: " + bi.getType());
//        }
//        // Create a Mat object with the appropriate size and type
//        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), cvType);
//        // Get the data buffer from the BufferedImage
//        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//        // Copy the data to the Mat object
//        mat.data().put(data);
//        return mat;
//    }

    private static double[] computeMotionStatistics(String videoPath) {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
            frameGrabber.start();
            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToMat converterToMat1 = new OpenCVFrameConverter.ToMat();
            List<Double> motionStats = new ArrayList<>();
            Mat prevImage = new Mat();
            Mat currentImage = new Mat();
            Mat flow = new Mat();

            Frame frame = frameGrabber.grabImage();
            prevImage = converterToMat.convert(frame);
            cvtColor(prevImage, prevImage, COLOR_BGR2GRAY);

            while ((frame = frameGrabber.grabImage()) != null) {
                currentImage = converterToMat1.convert(frame);
                cvtColor(currentImage, currentImage, COLOR_BGR2GRAY);
                calcOpticalFlowFarneback(prevImage, currentImage, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
                motionStats.add(calculateMotionFromFlow(flow));
                prevImage = currentImage.clone(); // This is required to keep the previous image for the next iteration
            }
            frameGrabber.stop();
            return motionStats.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing video file: " + videoPath, e);
        }
    }

    private static double[] computeMotionStatisticsCompare(String videoPath) {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
            frameGrabber.start();
            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToMat converterToMat1 = new OpenCVFrameConverter.ToMat();
            List<Double> motionStats = new ArrayList<>();
            Mat prevImage = new Mat();
            Mat currentImage = new Mat();
            Mat firstImage = new Mat();
            Mat flow = new Mat();
            int numKey = 0;
            Frame frame = frameGrabber.grabImage();
            prevImage = converterToMat.convert(frame);
            cvtColor(prevImage, prevImage, COLOR_BGR2GRAY);

            while ((frame = frameGrabber.grabImage()) != null) {
                currentImage = converterToMat1.convert(frame);
                if(frame.keyFrame){
                    cvtColor(currentImage, currentImage, COLOR_BGR2GRAY);
                    calcOpticalFlowFarneback(prevImage, currentImage, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
                    motionStats.add(calculateMotionFromFlow(flow));
                    numKey++;
                }
                prevImage = currentImage.clone(); // This is required to keep the previous image for the next iteration
            }
            frameGrabber.stop();
            if(numKey == 0){
                 return computeMotionStatistics(videoPath);
            }
            return motionStats.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing video file: " + videoPath, e);
        }
    }
    public static double computeMotionStatisticsShots(Frame prevFrame, Frame currFrame) {
        try {
            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            OpenCVFrameConverter.ToMat converterToMat2 = new OpenCVFrameConverter.ToMat();
            Mat prevMat = converterToMat.convert(prevFrame);
            Mat currentMat = converterToMat2.convert(currFrame);
            Mat flow = new Mat();
//            System.out.println("Mat is null: " + (prevMat == null));
//            System.out.println("Mat is empty: " + (prevMat != null && prevMat.empty()));

            cvtColor(prevMat, prevMat, COLOR_BGR2GRAY);
            cvtColor(currentMat, currentMat, COLOR_BGR2GRAY);

            calcOpticalFlowFarneback(prevMat, currentMat, flow, 0.5, 3, 15, 3, 5, 1.2, 0);
            converterToMat.close();
            converterToMat2.close();
            return calculateMotionFromFlow(flow);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing frames", e);
        }
    }
    /**
     * Calculate the magnitude of motion from the optical flow matrix.
     *
     * @param flow Optical flow matrix
     * @return Average magnitude of motion for the given frame
     */
    private static double calculateMotionFromFlow(Mat flow) {
        double motionSum = 0.0;
        FloatRawIndexer indexer = flow.createIndexer();
        for (int y = 0; y < flow.rows(); y++) {
            for (int x = 0; x < flow.cols(); x++) {
                float fx = indexer.get(y, x, 0); // X component of the flow vector
                float fy = indexer.get(y, x, 1); // Y component of the flow vector
                motionSum += Math.sqrt(fx * fx + fy * fy);
            }
        }
        indexer.release();
        return motionSum / (flow.rows() * flow.cols());
    }

    /**
     * Create motion signatures for all videos in the dataset.
     */
    public void createMotionSignatures() {
        long startTime = System.currentTimeMillis();
        String combinedFilePath = filePath + "CombinedMotionSignatures.csv";
        for (int i = 1; i <= TOTAL_VIDEO_NUMS; i++) {
            String videoPath = filePath + "video" + i + ".mp4";
            double[] motionStats = computeMotionStatistics(videoPath);
            motionSignatureList.add(motionStats);
            saveMotionData(combinedFilePath, i, motionStats); // Optional: Save motion data to a file
            System.out.println("Motion signature for video " + i + " created.");
        }
        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
        System.out.println("Database Execution Duration: " + executionDuration + " ms");
    }

    /**
     * Compare the query video with the motion signatures of all videos in the dataset.
     *
     * @param queryVideoPath Path of the query video
     */
    public void compareVideos(String queryVideoPath) throws Exception {
        long startTime = System.currentTimeMillis();
        double[] queryMotionStats = computeMotionStatistics(queryVideoPath);
        Map<Integer, double[]> allMotionSignatures;
        allMotionSignatures = loadMotionData(filePath + "CombinedMotionSignatures.csv");

        double minDistance = Double.MAX_VALUE;
        int bestMatchStartFrame = -1;
        int bestMatchVideoIndex = -1;

        // Iterate through each video's motion signature in the loaded map
        for (Map.Entry<Integer, double[]> entry : allMotionSignatures.entrySet()) {
            int videoIndex = entry.getKey();
            double[] currentVideoSignature = entry.getValue();

            // Sliding window approach over the motion statistics of the database video
            for (int frameIndex = 0; frameIndex <= currentVideoSignature.length - queryMotionStats.length; frameIndex++) {
                double distance = calculateEuclideanDistance(queryMotionStats, currentVideoSignature, frameIndex);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatchStartFrame = frameIndex;
                    bestMatchVideoIndex = videoIndex; // Use the video index from the map
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
        // Convert frame number to time (assuming frame rate is known, e.g., 30 fps)
        double startTimeInSeconds = bestMatchStartFrame / 30.0;
        if (bestMatchVideoIndex != -1) {
            System.out.println("The query segment matches with Video " + bestMatchVideoIndex
                    + ", starting at frame " + bestMatchStartFrame
                    + " (approx. " + startTimeInSeconds + " seconds)"
                    + " with a distance of " + minDistance);
        } else {
            System.out.println("No similar segment found in the database.");
        }
        System.out.println("Execution Duration: " + executionDuration + " ms");
    }

    private static double[] loadMotionSignature(String filePath) {
        long startTime = System.currentTimeMillis();
        List<Double> motionStats = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                motionStats.add(Double.parseDouble(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
//        System.out.println("read file time: " + executionDuration);
        return motionStats.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static void compareVideosShots(String queryVideoPath) throws Exception {
        long startTime = System.currentTimeMillis();
        // 假设 queryVideoPath 是查询镜头的路径
        double[] queryMotionStats = computeMotionStatistics(queryVideoPath); // 计算查询视频的运动特征
        File signaturesDirectory = new File("D:\\USC\\CSCI576\\CSCI576Project\\Project\\signature\\"); // 存储签名文件的目录
        System.out.println("queryMotionStats.lenght: "+ queryMotionStats.length);
        double minDistance = Double.MAX_VALUE;
        String bestMatchShotFile = "";

        for (File signatureFile : signaturesDirectory.listFiles()) {
            double[] shotSignature = loadMotionSignature(signatureFile.getPath());
            if(shotSignature.length >= queryMotionStats.length){
                for (int frameIndex = 0; frameIndex <= shotSignature.length - queryMotionStats.length; frameIndex++) {
                    double distance = calculateEuclideanDistance(queryMotionStats, shotSignature, frameIndex);
                    if (distance < minDistance) {
                        minDistance = distance;
//                    bestMatchStartFrame = frameIndex;
                        //bestMatchVideoIndex = videoIndex; // Use the video index from the map
                        bestMatchShotFile = signatureFile.getName();
                    }
                }
            }
            else {
                double distance = calculateEuclideanDistance(queryMotionStats, shotSignature, 0);
                if (distance < minDistance) {
                    minDistance = distance;
//                    bestMatchStartFrame = frameIndex;
                    //bestMatchVideoIndex = videoIndex; // Use the video index from the map
                    bestMatchShotFile = signatureFile.getName();
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
        // 输出最佳匹配结果
        if (!bestMatchShotFile.isEmpty()) {
            System.out.println("Best match is " + bestMatchShotFile + " with a distance of " + minDistance+ " waste time: "+ executionDuration / 1000);

        } else {
            System.out.println("No match found.");
        }
    }
    private static double calculateEuclideanDistance(double[] queryStats, double[] videoStats, int startFrame) {
        double sum = 0.0;
        if(startFrame > videoStats.length || startFrame < 0){
            startFrame = 0;
        }
        for (int i = 0; i < queryStats.length; i++) {
            int videoFrameIndex = startFrame + i;
            if (videoFrameIndex < videoStats.length) {
                sum += Math.pow(queryStats[i] - videoStats[videoFrameIndex], 2);
            } else {
                break; // Break if we reach the end of the video stats array
            }
        }
        return Math.sqrt(sum);
    }

//    public void compareVideos(String queryVideoPath) throws Exception {
//        double[] queryMotionStats = computeMotionStatistics(queryVideoPath);
//
//        double minDistance = Double.MAX_VALUE;
//        int mostSimilarVideoIndex = -1;
//
//        // Iterate through each video's motion signature in the list
//        for (int i = 0; i < motionSignatureList.size(); i++) {
//            double[] currentSignature = motionSignatureList.get(i);
//            double distance = calculateEuclideanDistance(queryMotionStats, currentSignature);
//
//            if (distance < minDistance) {
//                minDistance = distance;
//                mostSimilarVideoIndex = i + 1; // Adding 1 because index starts from 0
//            }
//        }
//
//        if (mostSimilarVideoIndex != -1) {
//            System.out.println("The most similar video to the query is Video " + mostSimilarVideoIndex + " with a distance of " + minDistance);
//        } else {
//            System.out.println("No similar videos found.");
//        }
//    }
//
//    private double calculateEuclideanDistance(double[] arr1, double[] arr2) {
//        double sum = 0.0;
//        int length = Math.min(arr1.length, arr2.length);
//
//        for (int i = 0; i < length; i++) {
//            sum += Math.pow(arr1[i] - arr2[i], 2);
//        }
//
//        return Math.sqrt(sum);
//    }

    private void saveMotionData(String filePath, int fileIndex, double[] motionData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write("Video " + fileIndex + "\n"); // Unique identifier for each video
            for (double value : motionData) {
                writer.write(String.format("%.6f", value));
                writer.newLine();
            }
            writer.newLine(); // Separate different videos' data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, double[]> loadMotionData(String combinedFilePath) {
        Map<Integer, double[]> motionDataMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(combinedFilePath))) {
            String line;
            int currentVideoIndex = -1;
            List<Double> currentMotionDataList = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Trim to remove any leading or trailing whitespace

                if (line.startsWith("Video")) {
                    if (currentVideoIndex != -1) {
                        // Save the previously read data
                        motionDataMap.put(currentVideoIndex, currentMotionDataList.stream().mapToDouble(Double::doubleValue).toArray());
                        currentMotionDataList.clear();
                    }
                    // Extract video index from the line
                    currentVideoIndex = Integer.parseInt(line.split(" ")[1]);
                } else if (!line.isEmpty()) {
                    // Add motion data to the list
                    currentMotionDataList.add(Double.parseDouble(line));
                }
            }

            // Add the last video's data
            if (currentVideoIndex != -1 && !currentMotionDataList.isEmpty()) {
                motionDataMap.put(currentVideoIndex, currentMotionDataList.stream().mapToDouble(Double::doubleValue).toArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return motionDataMap;
    }

    public List<String> getSignatureFilePaths(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        List<String> filePaths = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }

        return filePaths;
    }
}


