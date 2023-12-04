package org.example;

import org.bytedeco.javacv.Frame;
import org.jtransforms.fft.*;

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

    private static final double ratio = 0.5;
    private static final int OVERLAP = 512; // overlap size of Fast Fourier Transform(FFT)
    private ArrayList<double[][]> magnitudeSpectrumList; // list of audio signatures
    private static final int TOTAL_FILE_NUMS = 20; // num of all audio files
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
    private void applyAutomaticGainControl(short[] audioData, double targetAmplitude) {
        double max = 0.0;
        for (short sample : audioData) {
            max = Math.max(max, Math.abs(sample));
        }

        double gain = (targetAmplitude * Short.MAX_VALUE) / max;
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = (short) Math.min(Math.max(audioData[i] * gain, Short.MIN_VALUE), Short.MAX_VALUE);
        }
    }

//    private short[] getSample(String querySamplePath) {
//        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(querySamplePath))) {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = audioStream.read(buffer)) != -1) {
//                out.write(buffer, 0, read);
//            }
//            byte[] audioBytes = out.toByteArray();
//
//            ShortBuffer shortBuffer = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//            short[] samples = new short[audioBytes.length / 2];
//            shortBuffer.get(samples);
//
//            short[] samplesLeft = new short[samples.length / 2];
//            for (int i = 0; i < samples.length / 2; i++) {
//                samplesLeft[i] = samples[2 * i];
//            }
//
//            // 应用自动增益控制
//            applyAutomaticGainControl(samplesLeft, 0.8); // 假设目标振幅为0.5
//
//            return samplesLeft;
//        } catch (UnsupportedAudioFileException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Function for detecting shot boundaries
     * @param frame
     * @param audioSamplesBuffer
     * @return
     */
    public static ShortBuffer getSampleShot(Frame frame,  ShortBuffer audioSamplesBuffer) {
        if (frame.samples != null) {
            ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
            int numSamples = channelSamplesShortBuffer.remaining() / 2;

            if (audioSamplesBuffer.remaining() < numSamples) {
                ShortBuffer newBuffer = ShortBuffer.allocate(audioSamplesBuffer.capacity() + numSamples);
                audioSamplesBuffer.flip();
                newBuffer.put(audioSamplesBuffer);
                audioSamplesBuffer = newBuffer;
            }

//            audioSamplesBuffer.put(channelSamplesShortBuffer);
            for (int i = 0; channelSamplesShortBuffer.hasRemaining(); i++) {
                short sample = channelSamplesShortBuffer.get(); // 读取左声道样本
                audioSamplesBuffer.put(sample);

                if (channelSamplesShortBuffer.hasRemaining()) {
                    channelSamplesShortBuffer.get(); // 跳过右声道样本
                }
            }
        }

        return audioSamplesBuffer;
    }

    /**
     * get audio sample for shot boundary
     * @param videoIndex index of the input file
     * @param startTime start time, should be seconds
     * @param endTime end time, should be seconds
     * @return return the left sample for the required time duration
     */
    private short[] getSample(int videoIndex, double startTime, double endTime){
        String shotFilePath = filePath + "video" + videoIndex + ".wav";
        System.out.println("path: " + shotFilePath);
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(shotFilePath))) {
            AudioFormat format = audioStream.getFormat();
            int channels = format.getChannels(); // 获取音频通道数

            double durationInSeconds = audioStream.getFrameLength() / format.getFrameRate();

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

            int sampleStart = (int) (format.getFrameRate() * startTime * channels);
            int sampleEnd = (int) (format.getFrameRate() * endTime * channels);

            short[] samplesLeft = new short[sampleEnd - sampleStart];

            for (int i = sampleStart; i < sampleEnd; i++) {
                if (i < samples.length) {
                    samplesLeft[i - sampleStart] = samples[i];
                } else {
                    // 处理索引超出范围的情况
                    break;
                }
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

    public void FFTShot(short[] frame, double[] fftResult, int FRAME_SIZE){
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
     * Split the whole sample into several frames, and then perform FFT on each frame.
     * For each FFT result frame, calculate corresponding magnitude spectrum
     * @param sample the given sample
     * @param range if range is 0, only get magnitude of one frame. else get the magnitude of all frames.
     * @param magnitudeSpectrumArray store the magnitude spectrum to the given array
     */
    private void getMagnitudeShot(short[] sample, int range, double[][] magnitudeSpectrumArray, int FRAME_SIZE, int OVERLAP){
        double[] magnitudeSpectrum = new double[FRAME_SIZE / 2];
        System.out.println("magnitudeSpectrum.length: "+ magnitudeSpectrum.length);
        if (range == 0){
            for(int start = 0; start < FRAME_SIZE; start += FRAME_SIZE - OVERLAP) {
                short[] frame = Arrays.copyOfRange(sample, start, start + FRAME_SIZE);
                double[] fftResult = new double[FRAME_SIZE * 2];

                System.out.println("fftResult.length: "+ fftResult.length);
                FFTShot(frame, fftResult, FRAME_SIZE);
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
    private double[] matchAudio(short[] querySample){
        long startTime = System.currentTimeMillis();
        double[] returnArray = new double[2];

        double[][] firstMagnitudeArray = new double[1][512];
        getMagnitude(querySample, 0, firstMagnitudeArray);
        saveMagnitudeData(3, firstMagnitudeArray);
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
<<<<<<< HEAD
        System.out.println("Video " + finalVideo +  "; start time: " + minStart / 48000.0);

        playAudio((long) (minStart / 48000.0), filePath+"\\"+"Video"+finalVideo+".wav");
=======
        System.out.println("Video " + finalVideo +  "; start time: " + minStart / 44100.0);
        returnArray[0] = finalVideo;
        returnArray[1] = startTime;

        playAudio((long) (minStart / 44100.0), filePath+"\\"+"Video"+finalVideo+".wav");
        return  returnArray;
>>>>>>> 24541f64053c7ba8f9c002556c5175902ee99cac
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
            long bytesPerSecond = (long) (format.getFrameSize() * format.getSampleRate());
            long bytesToSkip = (long)(startTime * bytesPerSecond);

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
            Thread.sleep(100000);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
        e.printStackTrace();
        }
    }

    /**
     * This function is used to create audio signature while detecting shot boundaries.
     * @author pan
     */
    public void createAudioSignatureShots(int startFrame, int endFrame, short[] curSample, String path, int shots){
        int frameSize = (int)((endFrame - startFrame + 1) * ratio);
        if(frameSize == 0){
            frameSize = 1;
        }
        int overLap = frameSize / 2;
        if(overLap == 0){
            overLap = 1;
        }
        int ite_ct = curSample.length / (frameSize - overLap);
        double[][] curArray = new double[ite_ct][512];
        System.out.println("s: "+ startFrame + " e: "+ endFrame + " frame_size: "+ frameSize + " curSample.length:" + curSample.length);
        getMagnitudeShot(curSample, curSample.length, curArray, frameSize, overLap);
        magnitudeSpectrumList.add(curArray);
//        saveMagnitudeDataShot(path, shots, curArray);
        System.out.println("Shots loaded");

    }

    /**
     * get the audio signatures for shot boundary
     * @param fileIndex index of input file
     * @param startTime start time (should be seconds)
     * @param endTime end time (should be seconds)
     * @return return the generated signatures
     */
    public double[][] createAudioSignatureForShots(int fileIndex, double startTime, double endTime){
        short[] curSample = getSample(fileIndex, startTime, endTime);
        int ite_ct = curSample.length / (FRAME_SIZE - OVERLAP);
        double[][] audioSigForShot = new double[ite_ct][512];
        getMagnitude(curSample, curSample.length, audioSigForShot);

        return audioSigForShot;
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
     * save signature data(magnitude) to csv file
     * @param path
     * @param magnitudeData magnitude data
     */
    public void saveMagnitudeDataShot(String path, int index, int shots, double[][] magnitudeData){
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }

        // 构建文件完整路径
        String curFilePath = path + index +"signature" + shots + ".csv";

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
     *
     * @param queryAudioFileName is the name of query audio file, which must be at the filePath
     * @return
     */
    public double[] processAudio(String queryAudioFileName){
        double[] indexAndStartTime = new double[2];
        short[] sampleLeft = getSample(filePath + queryAudioFileName);
        magnitudeSpectrumList = new ArrayList<>();
        createAudioSignature();
        indexAndStartTime = matchAudio(sampleLeft);
        return indexAndStartTime;
    }

    private static double[][] readCsvFile(String filePath) throws IOException {
        List<double[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                data.add(row);
            }
        }
        return data.toArray(new double[0][]);
    }


}
