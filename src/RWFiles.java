import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;

public class RWFiles {

    String file = "";
    StandardHuffman huffman = new StandardHuffman(this);
    LZ77 lz77 = new LZ77(this);
    LZW lzw = new LZW(this);
    VectorQuantization v = new VectorQuantization(this);

    public BitSet stringToBitset(String code) {
        // used in compression while writing to file to convert the data into bits
        BitSet bits = new BitSet(code.length());
        int len = code.length();
        for (int i = len - 1;i >= 0; i--) {
            if (code.charAt(i) == '1') {
                // set from right to left
                bits.set((len - 1) - i);
            }
        }
        return bits;
    }
    public String bitsetToString(BitSet input, int size) {
        // used in decompression while reading file to convert the bits into data string
        String output = "";
        // read from right to left
        for (int i = size - 1; i >= 0; i--) {
            if (input.get(i))
                output += "1";
            else
                output += "0";
        }
        return output;
    }
    public void writeBits(Path filePath, String overhead, String binary, boolean writeSize) {
        if (writeSize) {
            overhead = binary.length() + "|" + overhead;
        }
        // overhead data will be written as it is
        byte[] overheadBytes = overhead.getBytes();
        // the data that will be converted to bits - every 8 bits
        byte[] dataBytes = stringToBitset(binary).toByteArray();
        try {
            Files.write(filePath, overheadBytes);
            Files.write(filePath, dataBytes, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
        }
    }

    public void compressFile(String algo) throws IOException, ClassNotFoundException {
        boolean read = switch (algo) {
            case "LZ77" -> lz77.readFile('c', file);
            case "LZW" -> lzw.readFile('c', file);
            case "Standard-Huffman" -> huffman.readFile('c', file);
            case "Vector Quantization" -> v.compress(file);
            default -> false;
        };

        if(read){
            JOptionPane.showMessageDialog(null, "Done Successfully!", "File Compressed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            // show message box error
            JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                    JOptionPane.ERROR_MESSAGE);

        }
        file = "";
    }
    public void decompressFile(String algo) throws IOException, ClassNotFoundException {
        boolean read = switch (algo) {
            case "LZ77" -> lz77.readFile('d', file);
            case "LZW" -> lzw.readFile('d', file);
            case "Standard-Huffman" -> huffman.readFile('d', file);
            case "Vector Quantization" -> v.decompress(file);
            default -> false;
        };

        if(read){
            JOptionPane.showMessageDialog(null, "Done Successfully!", "File Decompressed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            // show message box error
            JOptionPane.showMessageDialog(null, "Error, file not valid or found.", "Invalid File",
                    JOptionPane.ERROR_MESSAGE);

        }
        file = "";
    }
}
