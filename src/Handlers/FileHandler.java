package Handlers;

import LosslessTechnique.LZ77;
import LosslessTechnique.LZW;
import LosslessTechnique.StandardHuffman;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;

public class FileHandler {
    public String filePath = "";
    public String fileExtension = "";
    public String fileName = "";
    public CompressionTechniqueHandler currTechnique = null;
    public String fileContent = "";
    public BinaryHandler binaryHandler = new BinaryHandler();
    public static final String OVERHEAD_SEPARATOR = "|";
    public static final String OVERHEAD_END = "$$";

    public FileHandler(String filePath, CompressionTechniqueHandler currTechnique) {
        this.filePath = filePath;
        setTechnique(currTechnique);
        setFile(filePath);
    }

    public FileHandler() {
    }

    public void setTechnique(CompressionTechniqueHandler currTechnique) {
        // set the technique (strategy) used in compression/decompression text files
        if(currTechnique instanceof LZ77)
            this.currTechnique = new LZ77(this);
        else if(currTechnique instanceof LZW)
            this.currTechnique = new LZW(this);
        else if(currTechnique instanceof StandardHuffman)
            this.currTechnique = new StandardHuffman(this);
    }
    public void setFile(String filePath) {
        this.filePath = filePath;
        this.fileName = parseFileName(filePath);
        this.fileExtension = parseFileExtension(fileName);
    }
    public void resetFile() {
        this.filePath = "";
        this.fileName = "";
        this.fileExtension = "";
        this.currTechnique = null;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String parseFileExtension(String fileName) {
        // get extension of file: ".bin", ".txt" ...etc.
        return fileName.substring(fileName.lastIndexOf('.'));
    }
    public String parseFileName(String filePath) {
        // get name of file: "file.bin", "file.txt" ...etc.
        return filePath.substring(filePath.lastIndexOf('\\') + 1);
    }
    public boolean readFileBinary(char option, String file) {
        Path filePath = Paths.get(file);
        try {
            if (option == 'd') {
                // read the binary file and converting into bytes array
                byte[] allBytes = Files.readAllBytes(filePath);
                // convert the bytes array to string to get the overhead
                String allBytesString = new String(allBytes);
                // get the index of the size written in the file
                int endSizeIndex = allBytesString.indexOf(OVERHEAD_SEPARATOR);
                // convert the size form string to integer
                int size = Integer.parseInt(allBytesString.substring(0, endSizeIndex));
                // get the overhead
                String overhead = allBytesString.substring(endSizeIndex + 1, allBytesString.indexOf(OVERHEAD_END));
                // get the end header index from the file
                int endHeaderIndex = allBytesString.indexOf(OVERHEAD_END) + 2;
                // add the binary data to a separate bytes array for converting it to string
                byte[] dataBytes = new byte[allBytes.length - endHeaderIndex];
                // copy from allBytes array starting from endHeaderIndex to dataBytes array (remaining bytes for huffmanCode)
                System.arraycopy(allBytes, endHeaderIndex, dataBytes, 0, dataBytes.length);
                // converting the binary data to string and pass size of huffman code to it
                String code = binaryHandler.bitsetToString(BitSet.valueOf(dataBytes), size);
                // cast the technique to StandardHuffman to set the overhead
                ((StandardHuffman) currTechnique).setOverhead(overhead);
                currTechnique.decompress(code);
                writeToFile('d');
            } else {
                String input = Files.readString(filePath);
                currTechnique.compress(input);
                writeToFileBinary('c');
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    public void writeBits(Path filePath, String overhead, String binary, boolean writeSize) {
        if (writeSize) {
            overhead = binary.length() + OVERHEAD_SEPARATOR + overhead;
        }
        // overhead data will be written as it is
        byte[] overheadBytes = overhead.getBytes();
        // the data that will be converted to bits - every 8 bits
        byte[] dataBytes = binaryHandler.stringToBitset(binary).toByteArray();
        try {
            Files.write(filePath, overheadBytes);
            Files.write(filePath, dataBytes, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
        }
    }
    public void writeToFileBinary(char option) {
        String newFile;
        if (option == 'd') {
            newFile = fileName.replace("compressed", "decompressed");
            newFile = fileName.replace(".bin", ".txt");
            Path filePath = Paths.get(newFile);
            writeBits(filePath, fileContent, "", false);
        }
        else {
            fileExtension = ".bin";
            newFile = fileName.substring(0,fileName.lastIndexOf('.')) + "_compressed" + fileExtension;
            Path filePath = Paths.get(newFile);
            writeBits(filePath, ((StandardHuffman) currTechnique).getOverhead(), fileContent, true);
        }
    }

    public boolean readFile(char option){
        // binary files only handled in huffman
        if(currTechnique instanceof StandardHuffman)
            readFileBinary(option, filePath);
        else {
            Path file = Paths.get(filePath);
            try {
                String input = Files.readString(file);
                if (option == 'd') {
                    currTechnique.decompress(input);
                    writeToFile(option);
                } else {
                    currTechnique.compress(input);
                    writeToFile(option);
                }
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
        return true;
    }

    public void writeToFile(char option) throws IOException {
        // binary files only handled in huffman
        if(currTechnique instanceof StandardHuffman)
            writeToFileBinary(option);
        else {
            String newFile;
            if (option == 'd') {
                newFile = fileName.replace("compressed", "decompressed");
            } else {
                newFile = fileName.substring(0, fileName.lastIndexOf('.')) + "_compressed" + fileExtension;
            }
            Path filePath = Paths.get(newFile);
            Files.writeString(filePath, fileContent, StandardCharsets.UTF_8);
        }
    }

    public void compressFile() throws IOException, ClassNotFoundException {
        if(readFile('c')){
            JOptionPane.showMessageDialog(null, "Done Successfully!", "File Compressed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            // show message box error
            JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                    JOptionPane.ERROR_MESSAGE);

        }
        resetFile();
    }
    public void decompressFile() throws IOException, ClassNotFoundException {
        if(readFile('d')){
            JOptionPane.showMessageDialog(null, "Done Successfully!", "File Decompressed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            // show message box error
            JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                    JOptionPane.ERROR_MESSAGE);

        }
        resetFile();
    }
}
