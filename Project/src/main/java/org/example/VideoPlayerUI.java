package org.example;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import javax.sound.sampled.*;

public class VideoPlayerUI extends JFrame {
    private JButton playButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JLabel displayLabel;
    private Thread videoThread;
    private FFmpegFrameGrabber frameGrabber;
    // オーディオ再生用の変数
    private SourceDataLine soundLine = null;
    private static final int BUFFER_SIZE = 10000;//32kB
    private volatile boolean isPaused = false;
    private long videoStartTime = 0;

    public VideoPlayerUI(String matchedVideoPath, int matchedFrameIndex, String matchedAudioPath) {
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");
        displayLabel = new JLabel();

        this.setLayout(new FlowLayout());
        this.add(displayLabel);
        this.add(playButton);
        this.add(pauseButton);
        this.add(resetButton);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(640, 480);

        playButton.addActionListener(e -> playVideoFromFrame(matchedVideoPath, matchedFrameIndex));
        pauseButton.addActionListener(e -> pauseVideo());
        resetButton.addActionListener(e -> resetVideo(matchedVideoPath, matchedFrameIndex));
//        videoStartTime = matchedFrameIndex / 30;
        initializePlayer(matchedVideoPath);
    }
    public void initializePlayer(String videoPath) {
        if (frameGrabber != null) {
            try {
                frameGrabber.release(); // 释放之前的资源
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        frameGrabber = new FFmpegFrameGrabber(videoPath);
        try {
            frameGrabber.start();

            initAudio(frameGrabber); // 初始化音频
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing player: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void playVideoFromFrame(String videoPath, int frameIndex) {
        System.out.println(videoThread);
        if(isPaused){
            resumeVideo();
        }
        if (videoThread != null && videoThread.isAlive()) {
            return; // 如果已经在播放，则不做任何事
        }

        initializePlayer(videoPath);

//        try {
//            frameGrabber.setFrameNumber(frameIndex); // 设置起始帧
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("isPaused: "+ isPaused);

        videoThread = new Thread(() -> {
            try {
                long lastTimestamp = 0;
                frameGrabber.setFrameNumber(frameIndex);
                videoStartTime = System.currentTimeMillis();
                Frame frame;
//                if (!frameGrabber.isStarted()) {
//                    frameGrabber.start(); // 确保已经调用 start()
//                }
                while ((frame = frameGrabber.grabFrame()) != null) {
                    synchronized (this){
                        while (isPaused){
                            try {
                                this.wait();
                            }catch (InterruptedException e){
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }
                    BufferedImage image = new Java2DFrameConverter().convert(frame);
                    if(Thread.interrupted()){
                        break;
                    }
                    if (image != null) {
                        ImageIcon icon = new ImageIcon(image);
                        displayLabel.setIcon(icon);
                    }
                    // Process audio frame
                    if (frame.samples != null) {

                        ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                        short[] samples = new short[channelSamplesShortBuffer.capacity()];
                        channelSamplesShortBuffer.get(samples);
                        byte[] data = shortToByte(samples);
                        soundLine.write(data, 0, data.length);
                    }


//                    try {
//                        long sleepTime = (long) (1000 / frameGrabber.getFrameRate());
////                        System.out.println("SleepTime: "+sleepTime);
//                        Thread.sleep(sleepTime);
////                        Thread.sleep(3);
//                    } catch (InterruptedException ex) {
//                        break;
//                    }
                }
                frameGrabber.stop();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        videoThread.start();
    }

    private void pauseVideo() {
        isPaused = true;
    }
    private synchronized void resumeVideo() {
        isPaused = false;
        this.notifyAll();
    }
    private void resetVideo(String videoPath, int matchedFrameIndex) {
        pauseVideo();
        stopVideo();
//        initializePlayer(videoPath);
        resumeVideo();

        playVideoFromFrame(videoPath, matchedFrameIndex);
    }
    private void stopVideo() {
        if (videoThread != null) {
            Thread tempThread = videoThread; // 保存当前videoThread的引用
            videoThread.interrupt();

            new Thread(() -> {
                try {
                    tempThread.join(); // 使用临时变量来调用join
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            videoThread = null;
        }
        if (frameGrabber != null) {
            try {
                frameGrabber.stop();
                frameGrabber.release(); // 释放资源
            } catch (IOException e) {
                e.printStackTrace();
            }
//            frameGrabber = null;
        }
        if (soundLine != null) {
            soundLine.drain();
            soundLine.stop();
            soundLine.close();
            soundLine = null;
        }
    }
    private void initAudio(FFmpegFrameGrabber frameGrabber) {
        try {
            AudioFormat audioFormat = new AudioFormat(frameGrabber.getSampleRate(), 16, frameGrabber.getAudioChannels(), true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(audioFormat, BUFFER_SIZE);
            soundLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to open audio line: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private byte[] shortToByte(short[] samples) {
        byte[] data = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            data[i * 2] = (byte) (samples[i] & 0xff);
            data[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xff);
        }
        return data;
    }


}