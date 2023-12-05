import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.Vector;

public class VectorQuantization extends JFrame{

    public RWFiles rwFiles;
    public int vectorHeight = 2;
    public int vectorWidth = 2;
    public int codeBookSize = 64;
    public File compressFile = null;
    public File decompressFile = null;
    public int scaledHeight = 0;
    public int scaledWidth = 0;
    JPanel VQPanel;
    JScrollPane imageScreen;
    JButton compressButton;
    JButton decompressButton;
    JButton browseButton;
    JTextField filePathCompress;
    private JButton changeImgButton;
    private JButton returnButton;
    private JSpinner vSize;
    private JSpinner CBSize;
    private JButton browseButton2;
    private JTextField filePathDecompress;
    private Main home;
    private JLabel image = new JLabel();
    private BufferedImage Image;
    private BufferedImage compressedImage;
    private boolean flag = false;

    private void switchImage(boolean change) {
        if(compressedImage == null || Image == null)
            return;

        if(change) {
            image.setIcon(new ImageIcon(compressedImage));
            image.setHorizontalAlignment(JLabel.CENTER);
            imageScreen.getViewport().add(image);
            flag = true;
        }
        else {
            image.setIcon(new ImageIcon(Image));
            image.setHorizontalAlignment(JLabel.CENTER);
            imageScreen.getViewport().add(image);
            flag = false;
        }

        if(Image != null && compressedImage != null) {
            changeImgButton.setEnabled(true);
        }
    }
    VectorQuantization(){
        super("CompressifyPro");
        setContentPane(VQPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\img\\icon.png").getImage());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        rwFiles = new RWFiles();

        SpinnerModel value = new SpinnerNumberModel(2, 2, 1000000, 1);
        SpinnerModel value2 = new SpinnerNumberModel(64, 1, 1024, 1);
        vSize.setModel(value);
        CBSize.setModel(value2);
        changeImgButton.setEnabled(false);
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                home = new Main();
            }
        });
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter( new FileNameExtensionFilter(
                        "All Images",  ImageIO.getReaderFileSuffixes()));
                int returnValue = fileChooser.showOpenDialog(VQPanel);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    rwFiles.file = f.getAbsolutePath();
                    compressFile = fileChooser.getSelectedFile();
                    filePathCompress.setText(compressFile.getAbsolutePath());
                    filePathDecompress.setText("");
                    try {
                        Image = ImageIO.read(new File(compressFile.getAbsolutePath()));
                        switchImage(false);
                        changeImgButton.setEnabled(false);
                    } catch (IOException e1) {
                    }
                }

                compressButton.setEnabled(true);
                decompressButton.setEnabled(false);
            }
        });
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if(rwFiles.file.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose an image.", "Invalid Compression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        vectorHeight = (int) vSize.getValue();
                        vectorWidth = (int) vSize.getValue();
                        codeBookSize = (int) CBSize.getValue();

                        String path = compressFile.getAbsolutePath();
                        rwFiles.compressFile("Vector Quantization");
                        compressedImage = ImageIO.read(new File(getDecompressedPath(path)));
                        switchImage(true);
                    }
                    catch (IOException | ClassNotFoundException ex) {
                    }
                     compressButton.setEnabled(true);
                     decompressButton.setEnabled(true);
                     filePathCompress.setText("");
                }
            }
        });
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(rwFiles.file.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose an image.", "Invalid Decompression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        String path = decompressFile.getAbsolutePath();
                        rwFiles.decompressFile("Vector Quantization");
                        compressedImage = ImageIO.read(new File(getDecompressedPath(path)));
                        switchImage(true);
                    }
                    catch (IOException | ClassNotFoundException ex) {
                    }
                    compressButton.setEnabled(true);
                    decompressButton.setEnabled(true);
                    changeImgButton.setEnabled(false);
                    filePathDecompress.setText("");
                }
            }
        });
        changeImgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchImage(!flag);
            }
        });
        browseButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter( new FileNameExtensionFilter(
                        ".bin files",  "bin"));
                int returnValue = fileChooser.showOpenDialog(VQPanel);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    rwFiles.file = f.getAbsolutePath();
                    decompressFile = fileChooser.getSelectedFile();
                    filePathDecompress.setText(rwFiles.file);
                    filePathCompress.setText("");
                }
                decompressButton.setEnabled(true);
                compressButton.setEnabled(false);
            }
        });
    }

    // ------------------------------------------------------------------------------

    public VectorQuantization(RWFiles rwFiles) {
        this.rwFiles = rwFiles;
    }

    public Vector<int[][][]> divideIntoVectors(int[][][] scaledImg, int scaledHeight, int scaledWidth){
        // divide the image into vectors of specified vector height/width
        Vector<int[][][]> blocks = new Vector<>();
        for (int i = 0; i < scaledHeight; i+= vectorHeight) {
            for (int j = 0; j < scaledWidth; j+= vectorWidth) {
                blocks.add(new int[vectorHeight][vectorWidth][4]);
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

        int[][][] sum = new int[height][width][4];
        // calculate average of group of vectors
        for (int[][][] vector : vectors) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    for (int k = 0; k < 4; k++) {
                        sum[i][j][k] += vector[i][j][k];
                    }
                }
            }
        }
        int[][][] avg = new int[height][width][4];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < 4; k++) {
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

        int[][][] average1 = new int[height][width][4];
        int[][][] average2 = new int[height][width][4];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for(int k = 0 ; k < 4 ; k++){
                    average1[i][j][k] = average[i][j][k] + 1;
                    average2[i][j][k] = average[i][j][k] - 1;
                }
            }
        }

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
        int height = RWImage.height, width  = RWImage.width, x, y;

        // get the scaled height and scaled width
        scaledHeight = height % vectorHeight == 0 ? height : ((height / vectorHeight) + 1) * vectorHeight;
        scaledWidth = width % vectorWidth == 0? width : ((width / vectorWidth) + 1) * vectorWidth;

        // copy last pixel if the scaled height or width was less than original height, width
        int[][][] scaledImage = new int[scaledHeight][scaledWidth][4];

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
    public boolean compress(String file) {
        // read image
        int[][][] image = RWImage.readImage(file);
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
        writeVectorData(output, quantized, file);
        // decompress to output compressed img
        decompress(getCompressedPath(file));
        return true;
    }
    public boolean decompress(String fileName)  {

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
            int[][][] newImg = new int[scaledHeight][scaledWidth][4];

            for (int i = 0; i < indices.size(); i++) {
                int x = i / (scaledWidth / vectorWidth) * vectorHeight;
                int y = i % (scaledWidth / vectorWidth) * vectorWidth;
                int[][][] arr = quantized.get(indices.get(i));
                for (int j = x, a = 0; j < x + vectorHeight; j++, a++) {
                    for (int k = y, b = 0; k < y + vectorWidth; k++, b++) {
                        for (int z = 0; z < 4; z++) {
                            newImg[j][k][z] = arr[a][b][z];
                        }
                    }
                }
            }

            // write image
            RWImage.writeImage(newImg, width, height, getDecompressedPath(fileName));

            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void writeVectorData(Vector<Integer> output, Vector<int[][][]> quantized, String file){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(file));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // write to compressed File
            objectOutputStream.writeObject(RWImage.width);
            objectOutputStream.writeObject(RWImage.height);
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
