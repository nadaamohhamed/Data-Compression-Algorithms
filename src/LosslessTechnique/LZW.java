package LosslessTechnique;

import Handlers.CompressionTechniqueHandler;
import Handlers.FileHandler;

import java.util.HashMap;

public class LZW extends CompressionTechniqueHandler {

    private HashMap<String, Integer> dictionary1;
    private HashMap<Integer, String> dictionary2;

    public LZW(FileHandler FileHandler) {
        super(FileHandler);
    }

    public void fillDictionaryCompression(){
        dictionary1 = new HashMap<>();
        for(int i = 0;i<=127;i++){
            String c = "";
            c += (char)i;
            dictionary1.put(c, i);
        }
    }
    public void fillDictionaryDecompression(){
        dictionary2 = new HashMap<>();
        for(int i = 0;i<=127;i++){
            dictionary2.put(i, String.valueOf((char) i));
        }
    }
    @Override
    public void compress(String input){
        fillDictionaryCompression();
        int code = 128;
        String compressed = "", curr = "";
        curr += input.charAt(0);
        for(int i = 1;i<input.length();i++){
            String tag = "", next = "";
            tag += '<';
            next += input.charAt(i);
            if(!dictionary1.containsKey(curr + next)){
                tag += dictionary1.get(curr);
                tag += '>';
                compressed += tag;
                dictionary1.put(curr + next, code++);
                curr = next;
            }
            else{
                curr += next;
            }
        }
        // last tag
        compressed += '<';
        compressed += dictionary1.get(curr);
        compressed += '>';

        fileHandler.setFileContent(compressed);
    }
    @Override
    public void decompress(String input){
        fillDictionaryDecompression();

        int code = 128;
        StringBuilder decompressed = new StringBuilder();
        //String currTag = "";
        int oldCode = -1;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '<') {
                StringBuilder codeStr = new StringBuilder();
                i++;
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    codeStr.append(input.charAt(i));
                    i++;
                }

                int newCode = Integer.parseInt(codeStr.toString());

                if (oldCode == -1) {
                    // This is the first code, so append it directly
                    decompressed.append(dictionary2.get(newCode));
                } else {
                    String entry = dictionary2.get(newCode);

                    if (entry == null) {
                        entry = dictionary2.get(oldCode);
                        entry += entry.charAt(0);
                    }

                    decompressed.append(entry);
                    dictionary2.put(code, dictionary2.get(oldCode) + entry.charAt(0));
                    code++;
                }

                oldCode = newCode;
            }
            else {
                throw new RuntimeException("Invalid input format.");
            }
        }
        fileHandler.setFileContent(decompressed.toString());
    }
}
