//package videoProcessing.src;
import java.awt.image.BufferedImage;

public class ShotBoundaryDdetails {

    static final double threshold = 0.1;

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
                
                if(different >= threshold){
                   System.out.println(different);
                   differentPixels++;
                }
            }
        }
       
        return (double)differentPixels / totalPixels * 100;
    }
    public static void HSV(BufferedImage buff, int[] frequencyH, int[] frequencyS, int[] frequencyV){
        int width = buff.getWidth();
        int height = buff.getHeight();
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
    }
    public static double hsvDiff(BufferedImage buff1, BufferedImage buff2){
        int[] frequencyH1 = new int[361], frequencyS1 = new int[101], frequencyV1 = new int[101];
        int[] frequencyH2 = new int[361], frequencyS2 = new int[101], frequencyV2 = new int[101];

        HSV(buff1, frequencyH1, frequencyS1, frequencyV1);
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
        

        // double threshold = 0.5;

        return distance;
    }
    public static void combinedDiff(BufferedImage buff1, BufferedImage buff2, double w1, double w2){
        double pixelxDiffScore = pixelxDiff(buff1, buff2);
        double hsvDiffSocre = hsvDiff(buff1, buff2);

        System.out.println("pixel: "+ pixelxDiffScore);
        System.out.println("hsv: "+ hsvDiffSocre);
    }
}
