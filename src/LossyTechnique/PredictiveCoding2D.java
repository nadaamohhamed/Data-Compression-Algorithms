package LossyTechnique;

import Handlers.CompressionTechniqueHandler;
import Handlers.ImageHandler;

import java.io.*;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Vector;

public class PredictiveCoding2D extends CompressionTechniqueHandler {
    public int height = 0;
    public int width = 0;
    public int numOfLevels = 2;

    // Class Level
    public class Level{
        public int start;
        public int end;
        public int Q;
        public int Q_1;
    }
    // Functions Predictive Coding class
    public PredictiveCoding2D(ImageHandler imageHandler) {
        super(imageHandler);
    }
    public void quantize(int[][][] differences, int[][] indices, int[][][] quantizedDiff){
        // Uniform Quantization
        Vector<Vector<Level>> table = new Vector<>();
        for(int k = 0 ; k < 3 ; k++){
            // get min and max;
            Map.Entry<Integer, Integer> maxAndMin = getMaxAndMin(differences, k);
            int maxDiff = maxAndMin.getKey();
            int minDiff = maxAndMin.getValue();
            int QStep = (int) Math.ceil((maxDiff - minDiff) / (double) numOfLevels);
            int start = minDiff, end;
            // construct the table and each row (level) data
            Vector<Level> t = new Vector<>();
            for(int i = 0;i < numOfLevels; i++){
                if(i + 1 == numOfLevels)
                    end = maxDiff;
                else
                    end = start + QStep;
                // construct a new level
                Level currLevel = new Level();
                currLevel.Q = i;
                currLevel.start = start;
                currLevel.end = end;
                currLevel.Q_1 = (int) Math.ceil((start + end) / (double) 2);
                t.add(currLevel);
                // update next start
                start = end + 1;
            }
            table.add(t);
        }
        // loop on differences and get its Q value (quantized differences)
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                for (int k = 0; k < 3; k++){
                    for(Level l : table.get(k)){
                        if(differences[i][j][k] >= l.start && differences[i][j][k] <= l.end)
                            // quantize the difference
                            quantizedDiff[i][j][k] = l.Q;
                    }
                }

            }
        }
        // set each for each Q value its Q-1 value (Quantization table)
        for(int k = 0 ; k < 3 ; k++){
            for(Level l : table.get(k)){
                indices[l.Q][k] = l.Q_1;
            }
        }
    }
    @Override
    public void compress(String file) {
        // read image
        int[][][] image = imageHandler.readImageRGB(file);
        // height, width
        height = imageHandler.height;
        width = imageHandler.width;
        int[][][] prediction = new int[height][width][3];
        // store first row and col
        setFirstRow(prediction, image);
        setFirstCol(prediction, image);
        predict(prediction);
        // get diff between original image and prediction
        int[][][] diff = calculateDifference(image, prediction);
        // quantize using uniform quantization
        int[][] indices = new int[numOfLevels][3];
        int[][][] quantizedDiff = new int[height][width][3];
        // set first row and col for quantized diff
        setFirstRow(quantizedDiff, image);
        setFirstCol(quantizedDiff, image);
        // quantize and get quantization table
        quantize(diff, indices, quantizedDiff);
        // write the image overhead (indices, quantized diff)
        writeData(indices, quantizedDiff, file);
        // decompress to output compressed img
        decompress(getCompressedPath(file));
    }
    private Map.Entry<Integer,Integer> getMaxAndMin(int[][][] image , int dimension) {
        // get the max and min of the image in the given dimension
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i = 1; i < image.length; i++) {
            for (int j = 1; j < image[0].length; j++) {
                int value = image[i][j][dimension];
                max = Math.max(max, value);
                min = Math.min(min, value);
            }
        }
        return new AbstractMap.SimpleEntry<Integer , Integer>(max, min);
    }
    @Override
    public void decompress(String fileName)  {

        try (InputStream file = new FileInputStream(fileName);
             InputStream buffer = new BufferedInputStream(file);
             ObjectInput input = new ObjectInputStream(buffer)) {

            // read from file
            int[][] indices = (int[][]) input.readObject();
            int[][][] quantizedDiff = (int[][][]) input.readObject();

            // get height and width
            int height = quantizedDiff.length;
            int width = quantizedDiff[0].length;

            // construct the decompressed image
            int[][][] decodedImg = new int[height][width][3];
            setFirstRow(decodedImg, quantizedDiff);
            setFirstCol(decodedImg, quantizedDiff);
            // predict
            int[][][] prediction = new int[height][width][3];
            setFirstRow(prediction, quantizedDiff);
            setFirstCol(prediction, quantizedDiff);
            predict(prediction);

            for(int i = 1;i < height; i++){
                for(int j = 1; j < width; j++){
                    for (int k = 0; k < 3; k++){
                        // decoded img = prediction + de-quantized diff
                        decodedImg[i][j][k] =  prediction[i][j][k] + indices[quantizedDiff[i][j][k]][k];
                        // check if out of range
                        if(decodedImg[i][j][k] < 0)
                            decodedImg[i][j][k] = 0;
                        else if(decodedImg[i][j][k] > 255)
                            decodedImg[i][j][k] = 255;
                    }
                }
            }

            // write image
            imageHandler.writeImageRGB(decodedImg, width, height, getDecompressedPath(fileName));
        } catch (IOException | ClassNotFoundException e) {
        }
    }
    void setFirstRow(int[][][] image, int[][][] originalImage){
        for(int i = 0;i < width; i++){
            image[0][i] = originalImage[0][i];
        }
    }
    void setFirstCol(int[][][] image, int[][][] originalImage){
        for(int i = 0;i < height; i++){
            image[i][0] = originalImage[i][0];
        }
    }
    public void writeData(int[][] indices, int[][][] quantizedDiff, String file){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(file));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // write to compressed File
            objectOutputStream.writeObject(indices);
            objectOutputStream.writeObject(quantizedDiff);
            objectOutputStream.close();
        }
        catch (IOException ex){
        }
    }
    int[][][] calculateDifference(int[][][] image, int[][][] prediction){
        // calc for each row and col start from index 1
        int[][][] differences = new int[height][width][3];
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                for (int k = 0; k < 3; k++) {
                    // diff = original - prediction
                    differences[i][j][k] = image[i][j][k] - prediction[i][j][k];
                }
            }
        }
        return differences;
    }
    int adaptive2DPredictor(int A, int B, int C){
        if(B <= Math.min(A, C))
            return Math.max(A, C);
        else if(B >= Math.max(A, C))
            return Math.min(A, C);
        else
            return (A + C - B);
    }
    void predict(int[][][] predict){
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                for (int k = 0; k < 3; k++) {
                    // get A, B, C and predict using adaptive 2D predictor
                    int A = predict[i][j-1][k];
                    int C = predict[i-1][j][k];
                    int B = predict[i-1][j-1][k];
                    predict[i][j][k] = adaptive2DPredictor(A, B, C);
                }
            }
        }
    }
    public String getCompressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + ".bin";
    }
    public String getDecompressedPath(String path)    {
        return path.substring(0,path.lastIndexOf('.')) + "_compressed.jpg";
    }
}
