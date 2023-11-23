package org.example;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * To use class, please make sure that:
 * <br>
 * All the original audio files and the query sample file are at filePath;
 * <br>
 * Before running the program, change JVM options: -Xmx4096m, to increase the JVM heap size to 4G
 * @author PennyZ
 */
public class AudioProcess {
    private static final int FRAME_SIZE = 1024; // frame size of Fast Fourier Transform(FFT)
    private static final int OVERLAP = 512; // overlap size of Fast Fourier Transform(FFT)
    private ArrayList<double[][]> magnitudeSpectrumList; // list of audio signatures
    private static final int TOTAL_FILE_NUMS = 11; // num of all audio files
    private String filePath;

    public AudioProcess(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Read audio file, convert audio stream to a short array.
     * @param querySamplePath Path of the audio file
     * @return all the provided audio files are stereo, and only the left channel is returned for analysis
     */

    private short[] getSample(String querySamplePath) {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(querySamplePath))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = audioStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            byte[] audioBytes = out.toByteArray();

            ShortBuffer shortBuffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            short[] samples = new short[audioBytes.length / 2];
            shortBuffer.get(samples);

            short[] samplesLeft = new short[samples.length / 2];

            for (int i = 0; i < samples.length / 2; i++) {
                samplesLeft[i] = samples[2 * i];
            }

            return samplesLeft;
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * calculate the Euclidean distance between two frames.
     * @param magnitudeData1 the first magnitude data array
     * @param magnitudeData2 the second magnitude data array
     * @return the Euclidean distance
     * */
    private double calculateEuclideanDistance(double[] magnitudeData1, double[] magnitudeData2) {
        double sum = 0.0;
        for (int i = 0; i < magnitudeData1.length; i++) {
            sum += Math.pow(magnitudeData1[i] - magnitudeData2[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * get the sum of an array
     * @param sampleArray the given array
     * @return sum of the array
     */
    private long getSum(short[] sampleArray){
        long sum = 0;
        for (int i = 0; i < sampleArray.length; i++) {
            sum += (long) sampleArray[i];
        }
        return sum;
    }

    /**
     * perform FFT on the given frame
     * @param frame an FFT frame
     * @param fftResult FFT result with the real and imaginary parts
     */
    public void FFT(short[] frame, double[] fftResult){
        // Hann window：
        for(int i = 0; i < FRAME_SIZE; i++) {
            frame[i] *= 0.5 * (1 - Math.cos(2 * Math.PI * i / (FRAME_SIZE - 1)));
        }

        // prepare real & imaginary parts
        for (int i = 0; i < FRAME_SIZE; i++) {
            fftResult[2 * i] = frame[i]; // real part
            fftResult[2 * i + 1] = 0; // imaginary part
        }

        // perform FFT
        new DoubleFFT_1D(FRAME_SIZE).complexForward(fftResult);
    }

    /**
     * Split the whole sample into several frames, and then perform FFT on each frame.
     * For each FFT result frame, calculate corresponding magnitude spectrum
     * @param sample the given sample
     * @param range if range is 0, only get magnitude of one frame. else get the magnitude of all frames.
     * @param magnitudeSpectrumArray store the magnitude spectrum to the given array
     */
    private void getMagnitude(short[] sample, int range, double[][] magnitudeSpectrumArray){
        double[] magnitudeSpectrum = new double[FRAME_SIZE / 2];
        if (range == 0){
            for(int start = 0; start < FRAME_SIZE; start += FRAME_SIZE - OVERLAP) {
                short[] frame = Arrays.copyOfRange(sample, start, start + FRAME_SIZE);
                double[] fftResult = new double[FRAME_SIZE * 2];
                FFT(frame, fftResult);
                for(int i = 0; i < magnitudeSpectrum.length; i++) {
                    magnitudeSpectrumArray[0][i] = Math.sqrt(fftResult[2 * i] * fftResult[2 * i] + fftResult[2 * i + 1] * fftResult[2 * i + 1]);
                }
            }
        }else {
            int j = 0;
            for(int start = 0; start + FRAME_SIZE <= range; start += FRAME_SIZE - OVERLAP) {
                short[] frame = Arrays.copyOfRange(sample, start, start + FRAME_SIZE);
                double[] fftResult = new double[FRAME_SIZE * 2];
                FFT(frame, fftResult);
                for(int i = 0; i < magnitudeSpectrum.length; i++) {
                    magnitudeSpectrumArray[j][i] = Math.sqrt(fftResult[2 * i] * fftResult[2 * i] + fftResult[2 * i + 1] * fftResult[2 * i + 1]);
                }
                j += 1;
            }
        }
    }

    /**
     * iterate through ALL audio signatures, to match the query sample with all files in dataset
     * @param querySample the given query sample
     */
    private void matchAudio(short[] querySample){
        long startTime = System.currentTimeMillis();

        double[][] firstMagnitudeArray = new double[1][512];
        getMagnitude(querySample, 0, firstMagnitudeArray);
        double[] firstMagnitude = firstMagnitudeArray[0];

        double minDistance = Double.POSITIVE_INFINITY;
        int minStart = 0;
        int finalVideo = 0;

        for (int i = 0; i < TOTAL_FILE_NUMS; i++){
            double[][] curMagi = magnitudeSpectrumList.get(i);

            // if you want to load the saved signatures(e.g. csv files), please call loadSignature():
            // int fileIndex = i + 1;
            // double[][] curMagi = loadSignature(fileIndex);

            for (int j = 0; j < curMagi.length; j++){
                double[] curFrame = curMagi[j];
                double curDistance = calculateEuclideanDistance(firstMagnitude, curFrame);
                if (curDistance < minDistance){
                    minStart = j * (FRAME_SIZE - OVERLAP);
                    minDistance = curDistance;
                    finalVideo = i + 1;
                }
            }

        }
        long endTime = System.currentTimeMillis();
        long executionDuration = endTime - startTime;
        System.out.println("Execution Duration: " + executionDuration);
        System.out.println("Video " + finalVideo +  "; start time: " + minStart / 44100.0);
    }


    /**
     * play audio file starting from the startTime
     * @param startTime the starting time of the audio file, should be second(s)
     * @param filePath path of the audio file
     */
    private void playAudio(long startTime, String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            AudioFormat format = audioStream.getFormat();
            int frameSize = format.getFrameSize();
            long bytesToSkip = (long)(startTime * frameSize);

            audioStream.skip(bytesToSkip);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            //clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    System.exit(0);
                }
            });

            clip.start();
            Thread.sleep(1000000);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
        e.printStackTrace();
    }
    }

    private void createAudioSignature(){
        for (int i = 0; i < TOTAL_FILE_NUMS; i++){
            int fileIndex = i + 1;
            short[] curSample = getSample(filePath + "video" + fileIndex + ".wav");
            int ite_ct = curSample.length / (FRAME_SIZE - OVERLAP);
            double[][] curArray = new double[ite_ct][512];
            getMagnitude(curSample, curSample.length, curArray);
            magnitudeSpectrumList.add(curArray);

            // if you want to save the magnitude data to local file, call saveMagnitudeData()
            // saveMagnitudeData(fileIndex, curArray);
            System.out.println("Video " + (i+1) + " loaded");
        }
    }

    /**
     * save signature data(magnitude) to csv file
     * @param fileIndex index of the saved file
     * @param magnitudeData magnitude data
     */
    private void saveMagnitudeData(int fileIndex, double[][] magnitudeData){
        String curFilePath = filePath + "AudioSignature" + fileIndex +".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(curFilePath))) {
            for (double[] row : magnitudeData) {
                String[] stringRow = new String[row.length];
                for (int j = 0; j < row.length; j++) {
                    stringRow[j] = String.format("%.6f", row[j]);
                }
                String line = String.join(",", stringRow);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load signatures from local file
     * @param fileIndex the index of the file to be loaded
     * @return loaded signature data
     */
    private double[][] loadSignature(int fileIndex){
        String curFilePath = filePath + "AudioSignature" + fileIndex +".csv";
        List<double[]> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(curFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] stringValues = line.split(",");
                double[] doubleValues = new double[stringValues.length];
                for (int i = 0; i < stringValues.length; i++) {
                    doubleValues[i] = Double.parseDouble(stringValues[i]);
                }
                dataList.add(doubleValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[][] signatureData = new double[dataList.size()][512];

        signatureData = dataList.toArray(signatureData);
        return signatureData;
    }

    /**
     * process query audio
     * @param queryAudioFileName is the name of query audio file, which must be at the filePath
     */
    public void processAudio(String queryAudioFileName){
        short[] sampleLeft = getSample(filePath + queryAudioFileName);
        magnitudeSpectrumList = new ArrayList<>();
        createAudioSignature();
        matchAudio(sampleLeft);
    }
}
