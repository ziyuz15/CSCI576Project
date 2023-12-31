package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.opencv.core.MatOfFloat;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;

public class ShotBoundaryDetails {
    static final double threshold = 20;
    public double averageH;
    public double averageS;
    public double averageV;
    public int maxH;
    public int maxS;
    public int maxV;
    static ShotBoundaryDetails ShotBoundaryDetails = new ShotBoundaryDetails();
    public ShotBoundaryDetails(){
        averageH = 0;
        averageS = 0;
        averageV = 0;
        maxH = 0;
        maxS = 0;
        maxV = 0;
    }
    public static double pixelxDiff(BufferedImage buff1, BufferedImage buff2){
        int width = buff1.getWidth();
        int height = buff1.getHeight();
        double different = 0;
        int differentPixels = 0;
        int totalPixels = width * height;

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int rgb1 = buff1.getRGB(x, y);
                int rgb2 = buff2.getRGB(x, y);
                double R1 = (rgb1 >> 16) & 0xFF;
                double G1 = (rgb1 >> 8) & 0xFF;
                double B1 = (rgb1) & 0xFF;
                double R2 = (rgb2 >> 16) & 0xFF;
                double G2 = (rgb2 >> 8) & 0xFF;
                double B2 = (rgb2) & 0xFF;

                different = Math.sqrt(Math.pow(R2 - R1, 2) + Math.pow(G2 - G1, 2) + Math.pow(B2 - B1, 2));
//                if(x == 0 && y == 0){
//                    System.out.println("rgb1: "+ rgb1 + "rgb2: " + rgb2);
//                }
                if(different >= threshold){
//                    System.out.println("different: "+ different+ " "+ (double)differentPixels / totalPixels);
                    differentPixels++;
                }
            }
        }

        return (double)differentPixels / totalPixels;
    }
    public static int convertTimeToFrameNum(int Hz, double time){
        return (int)(Hz * time);
    }
    public static Mat computeHSV(Mat image, int bins){
        Mat histogram = new Mat();
        IntPointer channels = new IntPointer(0); // 指向通道索引的指针
        int[] histSize = new int[]{bins};
        float[] range = new float[]{0, 256};
        FloatPointer ranges = new FloatPointer(range); // 指向范围的指针

        calcHist(image, 1, channels, new Mat(), histogram, 1, new IntPointer(histSize), new PointerPointer<>(ranges), true, false);

        normalize(histogram, histogram, 0, 1, NORM_MINMAX, -1, new Mat());

        return histogram;
    }
    public static Mat getHSVSignature(org.bytedeco.javacv.Frame frame){
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        Mat imageOrigin = converterToMat.convert(frame);
        Mat image = processFrameWithLowPass(imageOrigin);
        if(image.empty()){
            System.out.println("Not found");
            return null;
        }

        MatVector colorChannels = new MatVector(3);
        split(image, colorChannels);

        int bins = 256;

        Mat histR = computeHSV(colorChannels.get(0), bins);
        Mat histG = computeHSV(colorChannels.get(1), bins);
        Mat histB = computeHSV(colorChannels.get(2), bins);

        Mat allHist = new Mat();
        hconcat(new MatVector(histR, histG, histB), allHist);

        //视文件大小可能需要降维处理

        return allHist;
    }
    public static Mat processFrameWithLowPass(Mat frame) {
        Mat processedFrame = new Mat();
        GaussianBlur(frame, processedFrame, new Size(9, 9), 0);
        return processedFrame;
    }
    public static double compareHSV(Mat hist1, Mat hist2){
        return compareHist(hist1, hist2, HISTCMP_CHISQR);
    }
    public static ShotBoundaryDetails HSV(BufferedImage buff, int[] frequencyH, int[] frequencyS, int[] frequencyV){
        int width = buff.getWidth();
        int height = buff.getHeight();
        int totalPixels = width * height;
        ShotBoundaryDetails features = new ShotBoundaryDetails();

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int rgb = buff.getRGB(x, y);
                int R = (rgb >> 16) & 0xFF;
                int G = (rgb >> 8) & 0xFF;
                int B = (rgb) & 0xFF;

                float rf = R / 255f;
                float gf = G / 255f;
                float bf = B / 255f;

                float maxf = Math.max(rf, Math.max(gf, bf));
                float minf = Math.min(rf, Math.min(gf, bf));
                float delta = maxf - minf;
                float s, h = 0;
                float v = maxf;
                if (delta == 0) {
                    h = 0;
                } else if (maxf == rf) {
                    h = (60 * ((gf - bf) / delta) + 360) % 360;
                } else if (maxf == gf) {
                    h = (60 * ((bf - rf) / delta) + 120) % 360;
                } else {
                    h = (60 * ((rf - gf) / delta) + 240) % 360;
                }

                if (maxf == 0) {
                    s = 0;
                } else {
                    s = delta / maxf;
                }

                frequencyH[(int) h]++;
                frequencyS[(int) (s * 100)]++;
                frequencyV[(int) (v * 100)]++;
            }
        }

        for (int i = 0; i < 361; i++) {
            features.averageH += (double) frequencyH[i] * i;
            if (frequencyH[i] > frequencyH[features.maxH]) {
                features.maxH = i;
            }
        }
        features.averageH /= totalPixels;

        for (int i = 0; i < 101; i++) {
            features.averageS += (double) frequencyS[i] * i;
            if (frequencyS[i] > frequencyS[features.maxS]) {
                features.maxS = i;
            }

            features.averageV += (double) frequencyV[i] * i;
            if (frequencyV[i] > frequencyV[features.maxV]) {
                features.maxV = i;
            }
        }
        features.averageS /= totalPixels;
        features.averageV /= totalPixels;

        return features;
    }
    public static double hsvDiff(BufferedImage buff1, BufferedImage buff2){
        int[] frequencyH1 = new int[361], frequencyS1 = new int[101], frequencyV1 = new int[101];
        int[] frequencyH2 = new int[361], frequencyS2 = new int[101], frequencyV2 = new int[101];
        int width = buff1.getWidth();
        int height = buff1.getHeight();

        double MaxDiff = calculateMaxHsvDiff(width*height);
        double normalizedHsvDiff = 0;

        ShotBoundaryDetails = HSV(buff1, frequencyH1, frequencyS1, frequencyV1);
        HSV(buff2, frequencyH2, frequencyS2, frequencyV2);
        double distance = 0.0;
        for (int i = 0; i < 360; i++) {
            distance += Math.pow(frequencyH1[i] - frequencyH2[i], 2);
        }
        for (int i = 0; i < 100; i++) {
            distance += Math.pow(frequencyS1[i] - frequencyS2[i], 2);
            distance += Math.pow(frequencyV1[i] - frequencyV2[i], 2);
        }
        distance = Math.sqrt(distance);
        normalizedHsvDiff = distance / MaxDiff;
        if(normalizedHsvDiff>1){
            normalizedHsvDiff = 1;
        }
        // double threshold = 0.5;

        return normalizedHsvDiff;
    }

    public static double calculateMaxHsvDiff(int totalPixels) {
//        double maxDiffH = 361 * Math.pow(totalPixels, 2);
//        double maxDiffSV = 2 * 101 * Math.pow(totalPixels, 2);
//        return Math.sqrt(maxDiffH + maxDiffSV);
        int N = totalPixels;

        // 计算最大卡方距离
        // 对于H通道（360个可能值），每个值的最大差异是N
        // 对于S和V通道（各100个可能值），每个值的最大差异也是N
        double maxDiffH = 360 * N;
        double maxDiffSV = 2 * 100 * N;  // S和V通道

        // 总的最大差异是这些值的总和
        return 100000 ;
    }

    public static boolean combinedDiff(BufferedImage buff1, BufferedImage buff2, double w1, double w2, String path,
                                       int startFrame, int endFrame){
        double pixelxDiffScore = pixelxDiff(buff1, buff2);
        double hsvDiffSocre = hsvDiff(buff1, buff2);
        double weightedScore = w1 * pixelxDiffScore + w2 * hsvDiffSocre;
        String fileName = path.substring(path.lastIndexOf("\\") + 1);
        String signature;

        if(weightedScore >= 0.75){
//            System.out.println("pixel: "+ pixelxDiffScore);
//            System.out.println("hsv: "+ hsvDiffSocre);
//            System.out.println("weightedScore: "+ weightedScore);
//            System.out.println(" ");
//            signature = createSignature(ShotBoundaryDetails, pixelxDiffScore, startFrame, endFrame);
//            appendSceneSignatureToFile(fileName, signature, "signatures.csv");
            return true;
        }
        return  false;
    }
    public static double combinedDiff(BufferedImage buff1, BufferedImage buff2, double w1, double w2){
        double pixelxDiffScore = pixelxDiff(buff1, buff2);
        double hsvDiffSocre = hsvDiff(buff1, buff2);
        double weightedScore = w1 * pixelxDiffScore + w2 * hsvDiffSocre;

        return weightedScore;

    }

    public static String createSignature(ShotBoundaryDetails hsvFeatures, double pixelDiff, int startFrame, int endFrame) {
        // 将特征组合成一个字符串
        StringBuilder signatureBuilder = new StringBuilder();
        // 添加像素差异特征
        signatureBuilder.append("PixelDiff:").append(pixelDiff).append(";");
        // 添加 HSV 直方图特征
        signatureBuilder.append("H_Avg:").append(hsvFeatures.averageH).append(";");
        signatureBuilder.append("S_Avg:").append(hsvFeatures.averageS).append(";");
        signatureBuilder.append("V_Avg:").append(hsvFeatures.averageV).append(";");
        signatureBuilder.append("H_Max:").append(hsvFeatures.maxH).append(";");
        signatureBuilder.append("S_Max:").append(hsvFeatures.maxS).append(";");
        signatureBuilder.append("V_Max:").append(hsvFeatures.maxV);
        signatureBuilder.append("|").append(startFrame);
        signatureBuilder.append("|").append(endFrame);

        return signatureBuilder.toString();
    }

    public static void appendSceneSignatureToFile(String videoName, String sceneSignature, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) { // 使用追加模式
            writer.write(videoName + "," + sceneSignature + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
