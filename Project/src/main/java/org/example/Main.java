package org.example;



public class Main {
    public static void main(String[] args) throws Exception {
        VideoProcessor videoProcessor = new VideoProcessor();
        long startTime = System.nanoTime();
//        VideoProcessor.processVideo("D:\\USC\\CSCI576\\Videos\\video1.mp4");
        AudioProcess audioProcess = new AudioProcess("D:\\USC\\CSCI576\\Audios_Test\\");
        audioProcess.processAudio("video3_1.wav");
        long endTime = System.nanoTime();
        long duration = endTime - startTime;


        System.out.println("Running Time: " + duration + "ns / " + duration / 1_000_000_000.0 + "s");
    }
}