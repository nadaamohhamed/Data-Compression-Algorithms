import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StandardHuffman {
    RWFiles rwFiles;
    HashMap<Character, Integer> frequency = new HashMap<>();
    HashMap<Character, String> huffmanCodes = new HashMap<>();
    PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(); // puts the smallest on top
    String overhead = "";

    public StandardHuffman(RWFiles rwFiles) {
        this.rwFiles = rwFiles;
    }

    public void setOverhead() {
        huffmanCodes.forEach((character, code) ->
                overhead += character + "|" + code + "|"
        );
        overhead += "$$";
    }

    public void setFrequencyTable(String str){
        // get the frequency for each character
        for (Character c : str.toCharArray()) {
            if (frequency.containsKey(c))
                frequency.replace(c, frequency.get(c) + 1);
            else
                frequency.put(c, 1);
        }
        // add each character as a huffman leaf in priority queue
        frequency.forEach((character, freq) ->
                pq.add(new Leaf(character, freq))
        );
    }
    public void generateHuffmanTree(HuffmanNode node, String huffmanCode){
        if(node instanceof Leaf){
            huffmanCodes.put(((Leaf) node).getCharacter(), huffmanCode);
            return;
        }
        generateHuffmanTree(node.getLeftNode(), huffmanCode.concat("0"));
        generateHuffmanTree(node.getRightNode(), huffmanCode.concat("1"));
    }
    public String compress(String input){
        setFrequencyTable(input);
        // generate new nodes by taking the sum of smallest 2 frequencies
        while (pq.size() > 1){
            pq.add(new HuffmanNode(pq.poll(), pq.poll()));
        }
        HuffmanNode root = pq.poll();
        // start generating the tree and assign left 0, right 1 to each node & its children
        generateHuffmanTree(root, "");
        // get huffman code of each char in the input string
        String compressed = "";
        for (Character c : input.toCharArray()){
            compressed += huffmanCodes.get(c);
        }
        // set overhead (char - its huffman code) when compressing
        setOverhead();

        return compressed;
    }

    public String decompress(String huffmanCode, String overhead){
        String decompressed = "", currCode = "";
        // fill in the huffman codes map
        String[] splitValues = overhead.split("\\|");
        for (int i = 0; i < splitValues.length; i += 2) {
            // put character, huffman code
            huffmanCodes.put(splitValues[i].charAt(0), splitValues[i + 1]);
        }
        // loop on the huffman code string
        for (Character c : huffmanCode.toCharArray()){
            currCode += c;
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                // if the curr code is present in the map, get its key
                if (Objects.equals(currCode, entry.getValue())) {
                    decompressed += entry.getKey();
                    currCode = "";
                }
            }
        }
        return decompressed;
    }

    public boolean readFile(char option, String file){
        Path filePath = Paths.get(file);
        String output;
        try {
            if(option == 'd') {
                // read the binary file and converting into bytes array
                byte[] allBytes = Files.readAllBytes(filePath);
                // convert the bytes array to string to get the overhead
                String allBytesString = new String(allBytes);
                // get the index of the size written in the file
                int endSizeIndex = allBytesString.indexOf('|');
                // convert the size form string to integer
                int size = Integer.parseInt(allBytesString.substring(0, endSizeIndex));
                // get the overhead
                String overhead = allBytesString.substring(endSizeIndex + 1, allBytesString.indexOf("$$"));
                // get the end header index from the file
                int endHeaderIndex = allBytesString.indexOf("$$") + 2;
                // add the binary data to a separate bytes array for converting it to string
                byte[] dataBytes = new byte[allBytes.length - endHeaderIndex];
                // copy from allBytes array starting from endHeaderIndex to dataBytes array (remaining bytes for huffmanCode)
                System.arraycopy(allBytes, endHeaderIndex, dataBytes, 0, dataBytes.length);
                // converting the binary data to string and pass size of huffman code to it
                String code = rwFiles.bitsetToString(BitSet.valueOf(dataBytes), size);
                output = decompress(code, overhead);
                writeToFile('d', output);
            }
            else{
                String input = Files.readString(filePath);
                output = compress(input);
                writeToFile('c', output);
            }
            return true;
        }
        catch (IOException ex) {
            return false;
        }
    }

    public void writeToFile(char option, String output) {
        String file = rwFiles.file.substring(rwFiles.file.lastIndexOf('\\') + 1);
        String newFile;
        if (option == 'd') {
            newFile = file.replace("compressed", "decompressed");
            newFile = newFile.replace(".bin", ".txt");
            Path filePath = Paths.get(newFile);
            rwFiles.writeBits(filePath, output, "", false);
        }
        else {
            newFile = file.substring(0,file.lastIndexOf('.')) + "_compressed.bin";
            Path filePath = Paths.get(newFile);
            rwFiles.writeBits(filePath, overhead, output, true);
        }
    }

}
