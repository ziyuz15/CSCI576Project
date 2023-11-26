import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import java.time.LocalDateTime;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;


public class ColorProcess {
   public static void main(String[] args) {

      for(int j =1;j<=11;j++){
         String queryVideoPath = "./Queries/video"+j+"_1.mp4";
         List<ColorSignature> querySignatures = generateColorSignatures(queryVideoPath);

         int overallBestMatchIndex = -1;
         double overallBestMatchScore = Double.MAX_VALUE;
         String overallBestVideoPath = "";

         for (int i = 1; i <= 11; i++) {
            String databaseVideoPath = "./Videos/video" + i + ".mp4";
            List<ColorSignature> databaseSignatures = generateColorSignatures(databaseVideoPath);

            // Find the most matching signature
            MatchResult matchResult = findBestMatch(querySignatures, databaseSignatures);

            // Output the results
            if (matchResult.score < overallBestMatchScore) {
               overallBestMatchScore = matchResult.score;
               overallBestMatchIndex = matchResult.frameIndex;
               overallBestVideoPath = databaseVideoPath;
            }
         }
         // Output to CSV file
         try (PrintWriter writer = new PrintWriter(new FileWriter("match_results.csv", true))) {
            writer.println("queryVideo:"+j + "," +"BestmatchVideo:"+ overallBestVideoPath + ", Match FrameIndex" + overallBestMatchIndex);
         } catch (IOException e) {
            e.printStackTrace();
         }

         System.out.println("Overall best match found in video: " + overallBestVideoPath);
         System.out.println("Best match found at frame index: " + overallBestMatchIndex);
         System.out.println("Best Match Score: " + overallBestMatchScore);
      }
   }

   public static List<ColorSignature> generateColorSignatures(String videoPath) {
      List<ColorSignature> signatures = new ArrayList<>();
      Java2DFrameConverter converter = new Java2DFrameConverter();
      int frameNumber = 0;

      try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
         frameGrabber.start();
         Frame frame;
         while ((frame = frameGrabber.grabFrame()) != null) {
            if (frame.image != null && frame.keyFrame) {
               // IフレームをBufferedImageに変換
               BufferedImage image = converter.convert(frame);

               ColorSignature signature = extractColorSignature(image);
               signatures.add(signature);

               frameNumber++;
            }
         }
         frameGrabber.stop();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return signatures;
   }

   public static ColorSignature extractColorSignature(BufferedImage image) {
      int width = image.getWidth();
      int height = image.getHeight();
      int[] rHistogram = new int[256];
      int[] gHistogram = new int[256];
      int[] bHistogram = new int[256];

      // RGB data and create histogram
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int rgb = image.getRGB(x, y);
            int rValue = (rgb >> 16) & 0xFF;
            int gValue = (rgb >> 8) & 0xFF;
            int bValue = rgb & 0xFF;

            rHistogram[rValue]++;
            gHistogram[gValue]++;
            bHistogram[bValue]++;
         }
      }

      return new ColorSignature(rHistogram, gHistogram, bHistogram);
   }

   public static MatchResult findBestMatch(List<ColorSignature> querySignatures, List<ColorSignature> databaseSignatures) {
      int bestMatchIndex = -1;
      double bestMatchScore = Double.MAX_VALUE;

      // searaching bestmatch video and frame
      for (int i = 0; i < databaseSignatures.size(); i++) {

         double score = calculateMatchScore(querySignatures, databaseSignatures, i);
         System.out.println("Calculate frame no."+i+" Score"+score+" ,Current time: " + LocalDateTime.now());

         if (score < bestMatchScore) {
            bestMatchScore = score;
            bestMatchIndex = i;
         }
      }

      return new MatchResult(bestMatchIndex, bestMatchScore);
   }
   static class MatchResult {
      public final int frameIndex;
      public final double score;

      public MatchResult(int frameIndex, double score) {
         this.frameIndex = frameIndex;
         this.score = score;
      }
   }
   public static double calculateMatchScore(List<ColorSignature> querySignatures, List<ColorSignature> databaseSignatures, int startIndex) {
      double score = 0.0;

      for (int i = 0; i < 3; i++) {
         if (startIndex + i < databaseSignatures.size()) {
            ColorSignature querySignature = querySignatures.get(i);
            ColorSignature databaseSignature = databaseSignatures.get(startIndex + i);
            score += calculateSignatureDifference(querySignature, databaseSignature);
         }
      }
      return score;
   }
   private static double calculateSignatureDifference(ColorSignature signature1, ColorSignature signature2) {
      double difference = 0.0;
      for (int channel = 0; channel < 3; channel++) {
         int[] histogram1 = signature1.getHistogram(channel);
         int[] histogram2 = signature2.getHistogram(channel);
         for (int i = 0; i < histogram1.length; i++) {
            difference += Math.abs(histogram1[i] - histogram2[i]);
         }
      }
      return difference;
   }
   static class ColorSignature {

      // int[3][256]: R,G,B and color index
      private int[][] histograms;

      public ColorSignature(int[] rHistogram, int[] gHistogram, int[] bHistogram) {
         this.histograms = new int[3][];
         this.histograms[0] = rHistogram;
         this.histograms[1] = gHistogram;
         this.histograms[2] = bHistogram;
      }

      public int[] getHistogram(int channel) {
         return histograms[channel];
      }
   }
}
