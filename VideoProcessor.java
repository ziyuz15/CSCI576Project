//package videoProcessing.src;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    public static void processVideo(String path) {

        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path)) {
            Frame currentFrame, nextFrame, audioFrame = new Frame();

            frameGrabber.start();
            currentFrame = frameGrabber.grabImage();
            int n = 0;
            while ((nextFrame  = frameGrabber.grabImage()) != null || 
            (audioFrame = frameGrabber.grabFrame(true, false, false, false)) != null) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage currentImage = converter.convert(currentFrame);
                BufferedImage nextImage = converter.convert(nextFrame);
                //read the video frame and store the rgb signals for example
                //processFrame(currentImage);
                //process frame with shot boundary details, color and motion
                //example:
                //proecss the rgb signals and create a digital signature
                Frame videoFrame2 = frameGrabber.grabImage();

                if (currentImage != null && nextImage != null) {
                    //processFrame(currentImage); // 处理当前帧
                    ShotBoundaryDdetails.combinedDiff(currentImage, nextImage, 0, 0);
                    
                }
                //read the audio frame's samples
                if(audioFrame.samples != null){
                  //read and process audio frame  
                }
                try {
                    System.out.println("1");
                    ImageIO.write(currentImage, "png", new File("\\video_tmp\\currentImage"+n+".png"));
                    ImageIO.write(nextImage, "png", new File("\\video_tmp\\nextImage"+n+".png"));
                    n++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentFrame = nextFrame;   
            }
            // System.out.println("num: "+ num);
            frameGrabber.stop();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
