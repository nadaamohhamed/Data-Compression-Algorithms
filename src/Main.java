import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Main extends JFrame {
    private JPanel Main;
    private JButton compressButton;
    private JButton decompressButton;
    private JButton chooseFileButton;
    private JComboBox comboBox;
    private JLabel fileName;
    private JLabel algoName;

    VectorQuantization v;
    String algorithm = "";
    RWFiles fileRW;

    public Main(){
        super("CompressifyPro");
        setContentPane(Main);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("D:\\Java-IntelliJ\\DataCompressionGUI\\img\\icon.png").getImage());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        comboBox.setSelectedItem(null);

        fileRW = new RWFiles();
        File workingDirectory = new File(System.getProperty("user.dir"));

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileRW.file.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose a file.", "Invalid Compression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        fileRW.compressFile(algorithm);
                    }
                   catch (IOException | ClassNotFoundException ex) {
                   }
                    comboBox.setSelectedItem(null);
                    fileName.setText("(No file chosen)");
                    algoName.setText("(No algorithm chosen)");
                    algorithm = "";
                }
            }
        });
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileRW.file.equals("")){
                    JOptionPane.showMessageDialog(null, "Error, please choose a file.", "Invalid Decompression",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        fileRW.decompressFile(algorithm);
                    }
                    catch (IOException | ClassNotFoundException ex) {
                    }
                    comboBox.setSelectedItem(null);
                    fileName.setText("(No file chosen)");
                    algoName.setText("(No algorithm chosen)");
                    algorithm = "";
                }
            }
        });
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                algorithm =  String.valueOf(comboBox.getSelectedItem());
                algoName.setText("");
                if(algorithm.equals("Vector Quantization")){
                    v = new VectorQuantization();
                    setVisible(false);
                }
                fileRW.file = "";
                fileName.setText("(No file chosen)");
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!algorithm.equals("")) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    if(algorithm.equals("Standard-Huffman"))
                        fileChooser.setFileFilter( new FileNameExtensionFilter(
                                ".txt, .bin files", "txt", "bin"));
                    else
                        fileChooser.setFileFilter( new FileNameExtensionFilter(
                                ".txt files", "txt"));
                    fileChooser.setCurrentDirectory(workingDirectory);
                    int returnValue = fileChooser.showOpenDialog(Main);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File f = fileChooser.getSelectedFile();
                        fileRW.file = f.getAbsolutePath();
                        fileName.setText(fileRW.file);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please choose an algorithm first!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }

    public static void main(String[] args) {
        new Main();
    }
}