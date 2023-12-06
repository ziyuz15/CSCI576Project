package org.example;


import java.awt.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        VideoProcessor videoProcessor = new VideoProcessor();

        long startTime = System.nanoTime();
        String audioName = args[0];
        String videoName = audioName.replaceAll("\\.wav$", "");

        AudioProcess audioProcess = new AudioProcess("D:\\USC\\CSCI576\\Audios_Test\\");
        //Preload for signatures
        ArrayList<double[][]> magnitudeSpectrumList = new ArrayList<>();
        audioProcess.loadSignature(magnitudeSpectrumList);
        //Get Matching result
        double[] result = audioProcess.processAudio(magnitudeSpectrumList, audioName);
//        int frameNum = videoProcessor.processVideoRGB("D:\\USC\\CSCI576\\Videos\\video"+(int)result[0]+".rgb",result[1],"D:\\USC\\CSCI576\\Queries\\"+videoName+".rgb");
        int frameNum = videoProcessor.processVideoRGB("F:\\Videos\\video"+(int)result[0]+".rgb",result[1],"D:\\USC\\CSCI576\\Queries\\"+videoName+".rgb");

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println(" ");
        System.out.println("Total Running Time: " + duration + "ns / " + duration / 1_000_000_000.0 + "s");
        System.out.println(" ");
        System.out.println("----------------------------------");
        System.out.println("Find Frame Number: " + frameNum);

        EventQueue.invokeLater(()->{
            VideoPlayerUI playerUI = new VideoPlayerUI("D:\\USC\\CSCI576\\Videos\\video"+(int)result[0]+".mp4", frameNum, "D:\\USC\\CSCI576\\Audios_Test\\video"+(int)result[0]+".wav");
            playerUI.setVisible(true);
        });
    }
//    public static void main(String[] args) {
////        AudioProcess myAudio = new AudioProcess("/Users/ziyuzhao/Desktop/CSCI 576/project/CSCI576Project/Project/src/main/java/org/example/");
////        myAudio.processAudio("video8_1.wav");
//        String datasetPath = "/Users/ziyuzhao/Desktop/CSCI 576/project/CSCI576Project/Project/src/main/java/org/example/";
//        MotionProcessor mp = new MotionProcessor(datasetPath);
//        try{
////            mp.createMotionSignatures();
////            mp.compareVideos(datasetPath + "video5_1.mp4");
//            for (int i = 1; i <= 11; i++) {
//                mp.compareVideos(datasetPath + "video" + i + "_1.mp4");
//                if (i==6) mp.compareVideos(datasetPath + "video" + i + "_2.mp4");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}