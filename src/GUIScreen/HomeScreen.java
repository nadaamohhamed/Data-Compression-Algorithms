package GUIScreen;

import Handlers.CompressionTechniqueHandler;
import Handlers.FileHandler;
import Handlers.ImageHandler;
import LosslessTechnique.LZ77;
import LosslessTechnique.LZW;
import LosslessTechnique.StandardHuffman;
import LossyTechnique.VectorQuantization;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class HomeScreen extends JFrame {
    private JPanel Main;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton chooseFileButton;
    private JComboBox comboBox;
    private JLabel fileName;
    private JLabel algoName;

    private FileHandler fileHandler;
    private CompressionTechniqueHandler currTechnique = null;

    public HomeScreen(){
        super("CompressifyPro");
        setContentPane(Main);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\img\\icon.png").getImage());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        comboBox.setSelectedItem(null);

        File workingDirectory = new File(System.getProperty("user.dir"));
        fileHandler = new FileHandler();
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileHandler.filePath.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose a file.", "Invalid Compression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        fileHandler.compressFile();
                        comboBox.setSelectedItem(null);
                        fileName.setText("(No file chosen)");
                        algoName.setText("(No algorithm chosen)");
                        currTechnique = null;
                    }
                   catch (IOException | ClassNotFoundException ex) {
                   }
                }
            }
        });
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileHandler.filePath.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose a file.", "Invalid Decompression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        fileHandler.decompressFile();
                        comboBox.setSelectedItem(null);
                        fileName.setText("(No file chosen)");
                        algoName.setText("(No algorithm chosen)");
                        currTechnique = null;
                    }
                    catch (IOException | ClassNotFoundException ex) {
                    }
                }
            }
        });
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentTechnique =  String.valueOf(comboBox.getSelectedItem());
                switch (currentTechnique) {
                    case "Vector Quantization" -> {
                        currTechnique = new VectorQuantization(new ImageHandler());
                        new VectorQuantizationScreen();
                        setVisible(false);
                    }
                    case "2D Predictive Coding" -> {
                        currTechnique = new VectorQuantization(new ImageHandler());
                        new PredictiveScreen();
                        setVisible(false);
                    }
                    case "LZ77" -> {
                        currTechnique = new LZ77(fileHandler);
                        fileHandler.setTechnique(currTechnique);
                    }
                    case "LZW" -> {
                        currTechnique = new LZW(fileHandler);
                        fileHandler.setTechnique(currTechnique);
                    }
                    case "Standard-Huffman" -> {
                        currTechnique = new StandardHuffman(fileHandler);
                        fileHandler.setTechnique(currTechnique);
                    }
                }
                algoName.setText("");
                fileHandler.resetFile();
                fileName.setText("(No file chosen)");
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(currTechnique == null)) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    if(currTechnique instanceof StandardHuffman)
                        fileChooser.setFileFilter( new FileNameExtensionFilter(
                                ".txt, .bin files", "txt", "bin"));
                    else
                        fileChooser.setFileFilter( new FileNameExtensionFilter(
                                ".txt files", "txt"));
                    fileChooser.setCurrentDirectory(workingDirectory);
                    int returnValue = fileChooser.showOpenDialog(Main);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File f = fileChooser.getSelectedFile();
                        String file = f.getAbsolutePath();
                        fileHandler = new FileHandler(file, currTechnique);
                        fileName.setText(fileHandler.filePath);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please choose an algorithm first!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }
}