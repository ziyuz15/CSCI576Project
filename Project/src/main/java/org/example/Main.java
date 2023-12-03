package org.example;



public class Main {
    public static void main(String[] args) throws Exception {
        VideoProcessor videoProcessor = new VideoProcessor();

        long startTime = System.nanoTime();
//        for(int i = 1; i <= 20; i++){
//            videoProcessor.processVideo("D:\\USC\\CSCI576\\Videos\\video"+i+".mp4", i);
//        }

//        videoProcessor.processVideo("D:\\USC\\CSCI576\\Videos\\video11.mp4");
//        MotionProcessor.compareVideosShots("D:\\USC\\CSCI576\\Queries\\video2_1.mp4");
        AudioProcess audioProcess = new AudioProcess("D:\\USC\\CSCI576\\Audios_Test\\");
        audioProcess.processAudio("video13_2.wav");
        long endTime = System.nanoTime();
        long duration = endTime - startTime;


        System.out.println("Running Time: " + duration + "ns / " + duration / 1_000_000_000.0 + "s");
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