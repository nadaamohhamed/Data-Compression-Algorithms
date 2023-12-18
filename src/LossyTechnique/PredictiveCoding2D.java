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
        for(int k = 0 ; k < 4 ; k++){
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
                for (int k = 0; k < 4; k++){
                    for(Level l : table.get(k)){
                        if(differences[i][j][k] >= l.start && differences[i][j][k] <= l.end)
                            quantizedDiff[i][j][k] = l.Q;
                    }
                }

            }
        }
        for(int k = 0 ; k < 4 ; k++){
            for(Level l : table.get(k)){
                indices[l.Q][k] = l.Q_1;
            }
        }
    }
    public void compress(String file) {
        // read image
        int[][][] image = imageHandler.readImageRGB(file);
        // height, width
        height = imageHandler.height;
        width = imageHandler.width;
        // store first row and col in a vec
        int[][][] firstRow = getFirstRow(image);
        int[][][] firstCol = getFirstCol(image);
        // predict image
        int[][][] prediction = new int[height][width][4];
        // store first row and col
        setFirstRow(prediction, firstRow);
        setFirstCol(prediction, firstCol);
        predict(prediction);
        // get diff between original image and prediction
        int[][][] diff = calculateDifference(image, prediction);
        // quantize
        int[][] indices = new int[numOfLevels][4];
        int[][][] quantized = new int[height][width][4];
        quantize(diff, indices, quantized);
        // write image data (indices) and overhead (quantized, first col, first row)
        writeData(firstRow, firstCol, indices, quantized, file);
        // decompress to output compressed img
        decompress(getCompressedPath(file));
    }
    private Map.Entry<Integer,Integer> getMaxAndMin(int[][][] image , int dimension) {
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
    public void decompress(String fileName)  {

        try (InputStream file = new FileInputStream(fileName);
             InputStream buffer = new BufferedInputStream(file);
             ObjectInput input = new ObjectInputStream(buffer)) {

            // read from file
            int[][][] firstRow = (int[][][]) input.readObject();
            int[][][] firstCol = (int[][][])  input.readObject();
            int[][] indices = (int[][]) input.readObject();
            int[][][] quantizedDiff = (int[][][]) input.readObject();

            // get height and width
            int height = quantizedDiff.length;
            int width = quantizedDiff[0].length;

            // construct the decompressed image
            int[][][] newImg = new int[height][width][4];
            setFirstRow(newImg, firstRow);
            setFirstCol(newImg, firstCol);
            // predict
            int[][][] prediction = new int[height][width][4];
            setFirstRow(prediction, firstRow);
            setFirstCol(prediction, firstCol);
            predict(prediction);

            for(int i = 1;i < height; i++){
                for(int j = 1; j < width; j++){
                    for (int k = 0; k < 4; k++){
                        newImg[i][j][k] =  prediction[i][j][k] + indices[quantizedDiff[i][j][k]][k];
                        if(newImg[i][j][k] < 0)
                            newImg[i][j][k] = 0;
                        else if(newImg[i][j][k] > 255)
                            newImg[i][j][k] = 255;
                    }
                }
            }

            // write image
            imageHandler.writeImageRGB(newImg, width, height, getDecompressedPath(fileName));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    int[][][] getFirstRow(int[][][] image){
        int[][][] firstRow = new int[1][width][4];
        for(int i = 0;i < width; i++){
            firstRow[0][i] = image[0][i];
        }
        return firstRow;
    }
    int[][][] getFirstCol(int[][][] image){
        int[][][]  firstCol = new int[height][1][4];
        for(int i = 0;i < height; i++){
            firstCol[i][0] = image[i][0];
        }
        return firstCol;
    }
    void setFirstRow(int[][][] image, int[][][] firstRow){
        for(int i = 0;i < width; i++){
            image[0][i] = firstRow[0][i];
        }
    }
    void setFirstCol(int[][][] image, int[][][] firstCol){
        for(int i = 0;i < height; i++){
            image[i][0] = firstCol[i][0];
        }
    }
    public void writeData(int[][][] firstRow, int[][][] firstCol, int[][] indices,
                          int[][][] quantizedDiff, String file){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(file));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // write to compressed File
            objectOutputStream.writeObject(firstRow);
            objectOutputStream.writeObject(firstCol);
            objectOutputStream.writeObject(indices);
            objectOutputStream.writeObject(quantizedDiff);
            objectOutputStream.close();
        }
        catch (IOException ex){
        }
    }
    int[][][] calculateDifference(int[][][] image, int[][][] prediction){
        // calc for each row and col start from index 1
        int[][][] differences = new int[height][width][4];
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                for (int k = 0; k < 4; k++) {
                    differences[i][j][k] += image[i][j][k] - prediction[i][j][k];
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
                for (int k = 0; k < 4; k++) {
                    int A = predict[i][j-1][k];
                    int C = predict[i-1][j][k];
                    int B = predict[i-1][j-1][k];
                    int predicted = adaptive2DPredictor(A, B, C);
                    predict[i][j][k] = predicted;
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
