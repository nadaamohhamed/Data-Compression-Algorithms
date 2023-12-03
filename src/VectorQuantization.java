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
    private BufferedImage originalImage;
    private BufferedImage compressedImage;
    private boolean compressedImgActive = false;

    private void switchImage(boolean True) {
        if(True)
        {
            if(compressedImage == null)
                return;
            image.setIcon(new ImageIcon(compressedImage));
            image.setHorizontalAlignment(JLabel.CENTER);
            imageScreen.getViewport().add(image);
            compressedImgActive = true;
        }
        else
        {
            if(originalImage == null)
                return;
            image.setIcon(new ImageIcon(originalImage));
            image.setHorizontalAlignment(JLabel.CENTER);
            imageScreen.getViewport().add(image);
            compressedImgActive = false;
        }
        if(originalImage != null && compressedImage != null)
            changeImgButton.setEnabled(true);
    }
    VectorQuantization(){
        super("CompressifyPro");
        setContentPane(VQPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\icon.png").getImage());
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
                        originalImage = ImageIO.read(new File(compressFile.getAbsolutePath()));
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
                 if(compressFile == null){
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
                switchImage(!compressedImgActive);
            }
        });
        browseButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter( new FileNameExtensionFilter(
                        ".VQ files",  "VQ"));
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
    public VectorQuantization(RWFiles rwFiles) {
        this.rwFiles = rwFiles;
    }

    public Vector<Vector<Integer>> divideIntoVectors(int[][] scaledImg, int scaledHeight, int scaledWidth){
        Vector<Vector<Integer>> blocks = new Vector<>();
        for (int i = 0; i < scaledHeight; i+= vectorHeight) {
            for (int j = 0; j < scaledWidth; j+= vectorWidth) {
                Vector<Integer> tmp = new Vector<>();
                for (int x = i; x < i + vectorHeight; x++) {
                    for (int y = j; y < j + vectorWidth; y++) {
                        tmp.add(scaledImg[x][y]);
                    }
                }
                blocks.add(tmp);
            }
        }
        return blocks;
    }

    public Vector<Integer> calculateAverage(Vector<Vector<Integer>> Vectors){
        int[] sum = new int[Vectors.get(0).size()];
        for (Vector<Integer> vector : Vectors ) {
            for (int i = 0; i < vector.size(); i++) {
                sum[i] += vector.get(i);
            }
        }
        Vector<Integer> avg = new Vector<>();
        for (int i = 0; i < sum.length; i++) {
            avg.add(sum[i] / Vectors.size());
        }
        return avg;
    }
    public Vector<Vector<Integer>> splitAverage(Vector<Integer> average){
        Vector<Vector<Integer>> returnVec = new Vector<>();

        Vector<Integer> v1 = new Vector<>();
        Vector<Integer> v2 = new Vector<>();
        for(int i = 0;i<average.size();i++){
            // split into 2 vectors
            v1.add(average.get(i) + 1);
            v2.add(average.get(i) - 1);
        }
        returnVec.add(v1);
        returnVec.add(v2);

        return returnVec;
    }
    public int calculateDistance(Vector<Integer> vec1, Vector<Integer> vec2){
        int sum = 0;
        // calculate the euclidean distance between 2 vectors
        for(int i = 0;i<vec1.size();i++) {
            sum += (int) Math.pow(vec1.get(i) - vec2.get(i), 2);
        }
        return sum;
    }
    public void quantize(int codeBookSize,  Vector<Vector<Integer>> Vectors, Vector<Vector<Integer>> quantized){
        if(codeBookSize == 1 || Vectors.size() == 0){
            if(Vectors.size() > 0)
                quantized.add(calculateAverage(Vectors));
            return;
        }

        Vector<Integer> avg = calculateAverage(Vectors);
        Vector<Vector<Integer>> splitVectors = splitAverage(avg);

        Vector<Vector<Integer>> left = new Vector<>();
        Vector<Vector<Integer>> right =  new Vector<>();

        for (Vector<Integer> vec: Vectors) {
            int dis1 = calculateDistance(vec, splitVectors.get(0));
            int dis2 = calculateDistance(vec, splitVectors.get(1));

            if(dis1 <= dis2)
                left.add(vec);
            else
                right.add(vec);
        }
        quantize(codeBookSize/2, left, quantized);
        quantize(codeBookSize/2, right, quantized);
    }
    public Vector<Integer> encodeImage(Vector<Vector<Integer>> Vectors, Vector<Vector<Integer>> quantized) {
        Vector<Integer> indices = new Vector<>();

        Vector<Integer> sums = new Vector<>();
        Vector<Integer> sortedSums = new Vector<>();

        for (Vector<Integer> vec : Vectors) {
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
    public boolean compress(String file) throws IOException, ClassNotFoundException {
        // Read image
        int[][] image = RWImage.readImage(file);

        int height = RWImage.height;
        int width  = RWImage.width;
        int scaledHeight, scaledWidth;

        // get the scaled height and scaled width
        if(height % vectorHeight == 0)
            scaledHeight = height;
        else
            scaledHeight = ((height / vectorHeight) + 1) * vectorHeight;

        if(width % vectorWidth == 0)
            scaledWidth = width;
        else
            scaledWidth = ((width / vectorWidth) + 1) * vectorWidth;


        // Scale image
        int[][] scaledImage = new int[scaledHeight][scaledWidth];
        int x, y;
        for (int i = 0; i < scaledHeight; i++) {
            if(i >= height)
                x = height - 1;
            else x = i;
            for (int j = 0; j < scaledWidth; j++) {
                if(j >= width)
                    y = width - 1;
                else y = j;
                scaledImage[i][j] = image[x][y];
            }
        }

        // Divide image into Vectors
        Vector<Vector<Integer>> Vectors = divideIntoVectors(scaledImage,scaledHeight,scaledWidth);
        // construct codebooks
        Vector<Vector<Integer>> quantized = new Vector<>();
        quantize(codeBookSize, Vectors, quantized);
        // assign every vector to its nearest codebook by its index
        Vector<Integer> output = encodeImage(Vectors, quantized);

        FileOutputStream fileOutputStream = new FileOutputStream(getCompressedPath(file));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        // Write To Compressed File
        objectOutputStream.writeObject(width);
        objectOutputStream.writeObject(height);
        objectOutputStream.writeObject(scaledWidth);
        objectOutputStream.writeObject(scaledHeight);
        objectOutputStream.writeObject(vectorWidth);
        objectOutputStream.writeObject(vectorHeight);
        objectOutputStream.writeObject(output);
        objectOutputStream.writeObject(quantized);
        objectOutputStream.close();

        decompress(getCompressedPath(file));

        return true;
    }
    public boolean decompress(String fileName) throws IOException, ClassNotFoundException {

        InputStream file = new FileInputStream(fileName);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        // read from filePathDecompress
        int width = (int) input.readObject();
        int height = (int) input.readObject();
        int scaledWidth = (int) input.readObject();
        int scaledHeight = (int) input.readObject();
        int vectorWidth = (int) input.readObject();
        int vectorHeight = (int) input.readObject();
        Vector<Integer> indices = (Vector<Integer>)input.readObject();
        Vector<Vector<Integer>> quantized = (Vector<Vector<Integer>>) input.readObject();

        int[][] newImg = new int[scaledHeight][scaledWidth];

        // ---------------------------------------------

        for (int i = 0; i < indices.size(); i++) {
            int x = i / (scaledWidth / vectorWidth);
            int y = i % (scaledWidth / vectorWidth);
            x *= vectorHeight;
            y *= vectorWidth;
            int v = 0;
            for (int j = x; j < x + vectorHeight; j++) {
                for (int k = y; k < y + vectorWidth; k++) {
                    newImg[j][k] = quantized.get(indices.get(i)).get(v++);
                }
            }
        }

        // Write image
        RWImage.writeImage(newImg, width, height, getDecompressedPath(fileName));

        return true;
    }
    public String getCompressedPath(String path) {
        return path.substring(0, path.lastIndexOf('.')) + ".VQ";
    }
    public String getDecompressedPath(String path)    {
        return path.substring(0,path.lastIndexOf('.')) + "_compressed.jpg";
    }
}
