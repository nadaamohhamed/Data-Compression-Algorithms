import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class LZW {
    RWFiles rwFiles;
    private HashMap<String, Integer> dictionary1;
    private HashMap<Integer, String> dictionary2;

    public LZW(RWFiles rwFiles) {
        this.rwFiles = rwFiles;
    }

    public void fillDictionaryCompression(){
        dictionary1 = new HashMap<>();
        for(int i = 0;i<=127;i++){
            String c = "";
            c += (char)i;
            dictionary1.put(c, i);
        }
    }
    public String compress(String input){
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

        return compressed;
    }
    public void fillDictionaryDecompression(){
        dictionary2 = new HashMap<>();
        for(int i = 0;i<=127;i++){
            dictionary2.put(i, String.valueOf((char) i));
        }
    }
    public String decompress(String input){
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
        return decompressed.toString();
    }

    public boolean readFile(char option, String file){
        Path filePath = Paths.get(file);
        String output;
        try {
            String input = Files.readString(filePath);
            if(option == 'd') {
                output = decompress(input);
                writeToFile('d', output);
            }
            else{
                output = compress(input);
                writeToFile('c', output);
            }
            return true;
        }
        catch (IOException ex) {
            return false;
        }
    }

    public void writeToFile(char option, String output) throws IOException {
        String newFile;
        String file = rwFiles.file.substring(rwFiles.file.lastIndexOf('\\') + 1);
        if (option == 'd') {
            newFile = file.replace("compressed", "decompressed");
        }
        else {
            newFile = file.substring(0,file.lastIndexOf('.')) + "_compressed.txt";
        }
        Path filePath = Paths.get(newFile);
        Files.writeString(filePath, output, StandardCharsets.UTF_8);
    }
}
