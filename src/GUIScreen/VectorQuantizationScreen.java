package GUIScreen;

import Handlers.ImageHandler;
import LossyTechnique.VectorQuantization;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VectorQuantizationScreen extends JFrame{
    private ImageHandler imageHandler;
    private VectorQuantization VQ;
    private JPanel VQPanel;
    private JScrollPane imageScreen;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton browseCompressButton;
    private JTextField filePathCompress;
    private JButton changeImgButton;
    private JButton returnButton;
    private JSpinner vSize;
    private JSpinner CBSize;
    private JButton browseDecompressButton;
    private JTextField filePathDecompress;
    private JLabel image = new JLabel();
    private BufferedImage Image;
    private BufferedImage compressedImage;
    private boolean flag = false;
    private File compressFile = null;
    private File decompressFile = null;

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
    VectorQuantizationScreen(){
        super("CompressifyPro");
        setContentPane(VQPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\img\\icon.png").getImage());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        imageHandler = new ImageHandler();
        VQ = new VectorQuantization(imageHandler);

        File workingDirectory = new File(System.getProperty("user.dir"));

        SpinnerModel value = new SpinnerNumberModel(2, 2, 1000000, 1);
        SpinnerModel value2 = new SpinnerNumberModel(64, 1, 1024, 1);
        vSize.setModel(value);
        CBSize.setModel(value2);
        changeImgButton.setEnabled(false);

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new HomeScreen();
            }
        });
        browseCompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter( new FileNameExtensionFilter(
                        "All Images",  ImageIO.getReaderFileSuffixes()));
                fileChooser.setCurrentDirectory(workingDirectory);
                int returnValue = fileChooser.showOpenDialog(VQPanel);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    imageHandler.imageFile = f.getAbsolutePath();
                    compressFile = fileChooser.getSelectedFile();
                    filePathCompress.setText(compressFile.getAbsolutePath());
                    filePathDecompress.setText("");
                    try {
                        Image = ImageIO.read(new File(compressFile.getAbsolutePath()));
                        image.setIcon(new ImageIcon(Image));
                        image.setHorizontalAlignment(JLabel.CENTER);
                        imageScreen.getViewport().add(image);
                        switchImage(false);
                        changeImgButton.setEnabled(false);
                    } catch (IOException e1) {
                    }
                    decompressButton.setEnabled(false);
                }

                compressButton.setEnabled(true);
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
                        VQ.vectorHeight = (int) vSize.getValue();
                        VQ.vectorWidth  = (int) vSize.getValue();
                        VQ.codeBookSize  = (int) CBSize.getValue();

                        String path = compressFile.getAbsolutePath();
                        VQ.compress(path);
                        compressedImage = ImageIO.read(new File(VQ.getDecompressedPath(path)));
                        switchImage(true);
                        JOptionPane.showMessageDialog(null, "Done Successfully!", "File Compressed",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                                JOptionPane.ERROR_MESSAGE);
                    }

                     compressButton.setEnabled(true);
                     decompressButton.setEnabled(true);
                     filePathCompress.setText("");
                     compressFile = null;
                }
            }
        });
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(decompressFile == null){
                    JOptionPane.showMessageDialog(null, "Error, please choose an image.", "Invalid Decompression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        String path = decompressFile.getAbsolutePath();
                        VQ.decompress(path);
                        compressedImage = ImageIO.read(new File(VQ.getDecompressedPath(path)));
                        switchImage(true);
                        JOptionPane.showMessageDialog(null, "Done Successfully!", "File Decompressed",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    compressButton.setEnabled(true);
                    decompressButton.setEnabled(true);
                    changeImgButton.setEnabled(false);
                    filePathDecompress.setText("");
                    decompressFile = null;
                }
            }
        });
        changeImgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchImage(!flag);
            }
        });
        browseDecompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileFilter( new FileNameExtensionFilter(
                        ".bin files",  "bin"));
                fileChooser.setCurrentDirectory(workingDirectory);
                int returnValue = fileChooser.showOpenDialog(VQPanel);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    imageHandler.imageFile = f.getAbsolutePath();
                    decompressFile = fileChooser.getSelectedFile();
                    filePathDecompress.setText(decompressFile.getAbsolutePath());
                    filePathCompress.setText("");
                    compressButton.setEnabled(false);
                    changeImgButton.setEnabled(false);
                    image.setIcon(null);
                }
                decompressButton.setEnabled(true);
            }
        });
    }


}
