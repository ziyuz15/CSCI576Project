package org.example;

import org.bytedeco.javacv.FFmpegFrameGrabber;

public class Main {
    public static void main(String[] args) {
        VideoProcessor videoProcessor = new VideoProcessor();
        VideoProcessor.processVideo("D:\\USC\\CSCI576\\Videos\\video3.mp4");
    }
}