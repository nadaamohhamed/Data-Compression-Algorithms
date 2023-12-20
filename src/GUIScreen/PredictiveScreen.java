package GUIScreen;

import Handlers.ImageHandler;
import LossyTechnique.PredictiveCoding2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PredictiveScreen extends JFrame {
    public File compressFile = null;
    public File decompressFile = null;
    private JPanel PCPanel;
    private JScrollPane imageScreen;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton browseCompressButton;
    private JTextField filePathCompress;
    private JSpinner bits;
    private JButton changeImgButton;
    private JButton returnButton;
    private JButton browseDecompressButton;
    private JTextField filePathDecompress;
    private JTextField levels;
    private JLabel image = new JLabel();
    private BufferedImage Image;
    private BufferedImage compressedImage;
    private boolean flag = false;
    private HomeScreen home;
    private ImageHandler imageHandler;
    PredictiveCoding2D pc;
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
    PredictiveScreen(){
        super("CompressifyPro");
        setContentPane(PCPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\img\\icon.png").getImage());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        imageHandler = new ImageHandler();
        pc = new PredictiveCoding2D(imageHandler);

        File workingDirectory = new File(System.getProperty("user.dir"));


        SpinnerModel value = new SpinnerNumberModel(1, 1, 20, 1);
        bits.setModel(value);
        levels.setText(String.valueOf(pc.numOfLevels));
        changeImgButton.setEnabled(false);

        bits.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                pc.numOfLevels = (int) Math.pow(2, (Integer) bits.getValue());
                levels.setText(String.valueOf(pc.numOfLevels));
            }
        });

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                home = new HomeScreen();
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
                int returnValue = fileChooser.showOpenDialog(PCPanel);
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
                        pc.numOfLevels = (int) Math.pow(2, (Integer) bits.getValue());
                        String path = compressFile.getAbsolutePath();
                        pc.compress(path);
                        compressedImage = ImageIO.read(new File(pc.getDecompressedPath(path)));
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
                        pc.decompress(path);
                        compressedImage = ImageIO.read(new File(pc.getDecompressedPath(path)));
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
                int returnValue = fileChooser.showOpenDialog(PCPanel);
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
