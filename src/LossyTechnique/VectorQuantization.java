package LossyTechnique;

import Handlers.CompressionTechniqueHandler;
import Handlers.ImageHandler;

import java.io.*;
import java.util.Collections;
import java.util.Vector;

public class VectorQuantization extends CompressionTechniqueHandler {
    public int vectorHeight = 2;
    public int vectorWidth = 2;
    public int codeBookSize = 63;
    public int scaledHeight = 0;
    public int scaledWidth = 0;

    public VectorQuantization(ImageHandler imageHandler) {
        super(imageHandler);
    }

    public Vector<int[][][]> divideIntoVectors(int[][][] scaledImg, int scaledHeight, int scaledWidth){
        // divide the image into vectors of specified vector height/width
        Vector<int[][][]> blocks = new Vector<>();
        for (int i = 0; i < scaledHeight; i+= vectorHeight) {
            for (int j = 0; j < scaledWidth; j+= vectorWidth) {
                blocks.add(new int[vectorHeight][vectorWidth][3]);
                for (int x = i, a = 0; x < i + vectorHeight; x++, a++) {
                    for (int y = j, b = 0; y < j + vectorWidth; y++, b++) {
                        blocks.lastElement()[a][b] = scaledImg[x][y];
                    }
                }
            }
        }
        return blocks;
    }

    public int[][][] calculateAverage(Vector<int[][][]> vectors){
        int height = vectors.get(0).length;
        int width = vectors.get(0)[0].length;

        int[][][] sum = new int[height][width][3];
        // calculate average of group of vectors
        for (int[][][] vector : vectors) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    for (int k = 0; k < 3; k++) {
                        sum[i][j][k] += vector[i][j][k];
                    }
                }
            }
        }
        // divide the sum by the number of vectors
        int[][][] avg = new int[height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < 3; k++) {
                    avg[i][j][k] = sum[i][j][k] / vectors.size();
                }
            }
        }
        return avg;
    }
    public Vector<int[][][]> splitAverage(int[][][] average){
        Vector<int[][][]> returnVec = new Vector<>();
        int height = average.length;
        int width = average[0].length;
        // add 1 to the first vector and subtract 1 from the second vector
        int[][][] average1 = new int[height][width][3];
        int[][][] average2 = new int[height][width][3];
        // split the average into 2 vectors
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for(int k = 0 ; k < 3 ; k++){
                    average1[i][j][k] = average[i][j][k] + 1;
                    average2[i][j][k] = average[i][j][k] - 1;
                }
            }
        }
        // add the 2 vectors to the return vector
        returnVec.add(average1);
        returnVec.add(average2);

        return returnVec;
    }
    public int calculateDistance(int[][][] block1, int[][][] block2){
        int sum = 0;
        // calculate the euclidean distance between 2 blocks
        for (int i = 0; i < block1.length; i++) {
            for (int j = 0; j < block1[i].length; j++) {
                for (int k = 0; k < block1[i][j].length; k++) {
                    sum += (int) Math.pow(block1[i][j][k] - block2[i][j][k], 2);
                }
            }
        }
        return sum;
    }
    public void quantize(int codeBookSize,  Vector<int[][][]> vectors, Vector<int[][][]> quantized){
        if(codeBookSize == 1 || vectors.size() == 0){
            if(vectors.size() > 0)
                quantized.add(calculateAverage(vectors));
            return;
        }
        // calculate average of vectors
        int[][][] avg = calculateAverage(vectors);
        // split the average into 2 vectors
        Vector<int[][][]> splitVectors = splitAverage(avg);

        Vector<int[][][]> left = new Vector<>();
        Vector<int[][][]> right =  new Vector<>();

        for (int[][][] vec: vectors) {
            int dis1 = calculateDistance(vec, splitVectors.get(0));
            int dis2 = calculateDistance(vec, splitVectors.get(1));
            // add vector to its closest average vector group
            if(dis1 <= dis2)
                left.add(vec);
            else
                right.add(vec);
        }
        // divide variable codebook size/2 and quantize left group, quantize right group
        quantize(codeBookSize/2, left, quantized);
        quantize(codeBookSize/2, right, quantized);
    }
    public Vector<Integer> encodeImage(Vector<int[][][]> vectors, Vector<int[][][]> quantized) {
        // get for every vector the index of the closest quantized vector
        Vector<Integer> indices = new Vector<>();

        Vector<Integer> sums = new Vector<>();
        Vector<Integer> sortedSums = new Vector<>();
        for (int[][][] vec : vectors) {
            for (int i = 0; i < quantized.size(); i++) {
                // calculate the distance between it and each quantized vector
                int distance = calculateDistance(vec, quantized.get(i));
                sums.add(distance);
                sortedSums.add(distance);
            }
            // sort the distance
            Collections.sort(sortedSums);
            // pick the min distance and add its index
            int index = sums.indexOf(sortedSums.get(0));
            indices.add(index);
            // reset vectors
            sums.clear();
            sortedSums.clear();
        }
        return indices;
    }
    public int[][][] scaleImg(int[][][] image){
        // get the original height and width
        int height = ImageHandler.height, width  = ImageHandler.width, x, y;

        // get the scaled height and scaled width
        scaledHeight = height % vectorHeight == 0 ? height : ((height / vectorHeight) + 1) * vectorHeight;
        scaledWidth = width % vectorWidth == 0? width : ((width / vectorWidth) + 1) * vectorWidth;

        // copy last pixel if the scaled height or width was less than original height, width
        int[][][] scaledImage = new int[scaledHeight][scaledWidth][3];

        for (int i = 0; i < scaledHeight; i++) {
            if(i >= height)
                x = height - 1;
            else
                x = i;
            for (int j = 0; j < scaledWidth; j++) {
                if(j >= width)
                    y = width - 1;
                else
                    y = j;
                scaledImage[i][j] = image[x][y];
            }
        }

        return scaledImage;
    }
    @Override
    public void compress(String file) {
        // read image
        int[][][] image = imageHandler.readImageRGB(file);
        // Scale image
        int[][][] scaledImage = scaleImg(image);
        // Divide image into Vectors
        Vector<int[][][]> Vectors = divideIntoVectors(scaledImage, scaledHeight, scaledWidth);
        // construct codebooks
        Vector<int[][][]> quantized = new Vector<>();
        quantize(codeBookSize, Vectors, quantized);
        // assign every vector to its nearest codebook by its index
        Vector<Integer> output = encodeImage(Vectors, quantized);
        // write image data (indices) and overhead (quantized)
        writeToFile(output, quantized, file);
        // decompress to output compressed img
        decompress(getCompressedPath(file));
    }
    @Override
    public void decompress(String fileName)  {

        try (InputStream file = new FileInputStream(fileName);
             InputStream buffer = new BufferedInputStream(file);
             ObjectInput input = new ObjectInputStream(buffer)) {

            // read from file
            int width = (int) input.readObject();
            int height = (int) input.readObject();
            int scaledWidth = (int) input.readObject();
            int scaledHeight = (int) input.readObject();
            int vectorWidth = (int) input.readObject();
            int vectorHeight = (int) input.readObject();
            Vector<Integer> indices = (Vector<Integer>) input.readObject();
            Vector<int[][][]> quantized = (Vector<int[][][]>) input.readObject();

            // construct the decompressed image
            int[][][] newImg = new int[scaledHeight][scaledWidth][3];

            for (int i = 0; i < indices.size(); i++) {
                int x = i / (scaledWidth / vectorWidth) * vectorHeight;
                int y = i % (scaledWidth / vectorWidth) * vectorWidth;
                int[][][] arr = quantized.get(indices.get(i));
                for (int j = x, a = 0; j < x + vectorHeight; j++, a++) {
                    for (int k = y, b = 0; k < y + vectorWidth; k++, b++) {
                        for (int z = 0; z < 3; z++) {
                            newImg[j][k][z] = arr[a][b][z];
                        }
                    }
                }
            }

            // write image
            imageHandler.writeImageRGB(newImg, width, height, getDecompressedPath(fileName));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void writeToFile(Vector<Integer> output, Vector<int[][][]> quantized, String file){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(file));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // write to compressed File
            objectOutputStream.writeObject(imageHandler.width);
            objectOutputStream.writeObject(imageHandler.height);
            objectOutputStream.writeObject(scaledWidth);
            objectOutputStream.writeObject(scaledHeight);
            objectOutputStream.writeObject(vectorWidth);
            objectOutputStream.writeObject(vectorHeight);
            objectOutputStream.writeObject(output);
            objectOutputStream.writeObject(quantized);
            objectOutputStream.close();
        }
        catch (IOException ex){
        }
    }
    public String getCompressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + ".bin";
    }
    public String getDecompressedPath(String path)    {
        return path.substring(0,path.lastIndexOf('.')) + "_compressed.jpg";
    }

}
