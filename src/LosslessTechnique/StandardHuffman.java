package LosslessTechnique;

import Handlers.CompressionTechniqueHandler;
import Handlers.FileHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import static Handlers.FileHandler.OVERHEAD_END;
import static Handlers.FileHandler.OVERHEAD_SEPARATOR;

public class StandardHuffman extends CompressionTechniqueHandler {

    private HashMap<Character, Integer> frequency = new HashMap<>();
    private HashMap<Character, String> huffmanCodes = new HashMap<>();
    private PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(); // puts the smallest on top
    private String overhead = "";

    // Class HuffmanNode
    public class HuffmanNode implements Comparable<HuffmanNode> {

        private HuffmanNode leftNode;
        private HuffmanNode rightNode;
        protected Integer frequency;

        public HuffmanNode(HuffmanNode leftNode, HuffmanNode rightNode){
            this.leftNode = leftNode;
            this.rightNode = rightNode;
            if(leftNode != null && rightNode != null) {
                // if not leaf, calculate freq
                this.frequency = leftNode.frequency + rightNode.frequency;
            }
        }

        public HuffmanNode getLeftNode() {
            return leftNode;
        }

        public HuffmanNode getRightNode() {
            return rightNode;
        }

        public Integer getFrequency() {
            return frequency;
        }

        @Override
        public int compareTo(HuffmanNode node) {
            return Integer.compare(frequency, node.getFrequency());
        }
    }
    // Class Leaf
    class Leaf extends HuffmanNode {

        private char character;
        public Leaf(char character, Integer frequency){
            super(null, null);
            this.frequency = frequency;
            this.character = character;
        }

        public char getCharacter() {
            return character;
        }
    }
    // Functions Standard Huffman class
    public StandardHuffman(FileHandler fileHandler) {
        super(fileHandler);
    }
    public void writeOverhead() {
        huffmanCodes.forEach((character, code) ->
                overhead += character + OVERHEAD_SEPARATOR + code + OVERHEAD_SEPARATOR
        );
        overhead += OVERHEAD_END;
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
    @Override
    public void compress(String input){
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
        // write overhead (char - its huffman code) when compressing
        writeOverhead();
        // pass the compressed string to the file handler
        fileHandler.setFileContent(compressed);
    }
    @Override
    public void decompress(String huffmanCode){
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
        // pass the decompressed string to the file handler
        fileHandler.setFileContent(decompressed);
    }

    public String getOverhead() {
        return overhead;
    }

    public void setOverhead(String overhead) {
        this.overhead = overhead;
    }
}