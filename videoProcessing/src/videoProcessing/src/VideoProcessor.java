package videoProcessing.src;

import java.awt.image.BufferedImage;
import org.bytedeco.javacv.*;

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
     * @param BufferedImage img the bufferedImage we get from each frame
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
            Frame videoFrame, audioFrame = new Frame();

            frameGrabber.start();
            while ((videoFrame = frameGrabber.grabImage()) != null || 
            (audioFrame = frameGrabber.grabFrame(true, false, false, false)) != null) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.convert(videoFrame);
                //read the video frame and store the rgb signals for example
                processFrame(bufferedImage);
                //process frame with shot boundary details, color and motion
                //example:
                //proecss the rgb signals and create a digital signature

                //read the audio frame's samples
                if(audioFrame.samples != null){
                  //read and process audio frame  
                }
                
            }

            frameGrabber.stop();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
