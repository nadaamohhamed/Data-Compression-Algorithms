package Handlers;

import java.util.BitSet;

// class to handle bitset conversion and binary data
public class BinaryHandler {
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
}
