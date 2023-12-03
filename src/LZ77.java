import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LZ77 {
    RWFiles rwFiles;

    public LZ77(RWFiles rwFiles) {
        this.rwFiles = rwFiles;
    }

    public String compress(String input){
        String searchWindow = "";
        int tagLength, pos, index;
        String compressed = "";
        for (int i = 0; i < input.length(); i++)
        {
            tagLength = 0;
            pos = 0;
            index = i;
            boolean found = false;
            String matchChars = "";
            for (int j = 0; j < searchWindow.length(); j++)
            {
                if(index >= input.length()) break;
                if(input.charAt(index) == searchWindow.charAt(j))
                {
                    matchChars += input.charAt(index);
                    found = true;
                    index++;
                }
                else if(found)
                {
                    if(matchChars.length() > tagLength)
                    {
                        tagLength = matchChars.length();
                        pos = searchWindow.lastIndexOf(matchChars);
                    }
                    matchChars = "";
                    index = i;
                }
            }
            if(!matchChars.isEmpty()){
                if(matchChars.length() > tagLength)
                {
                    tagLength = matchChars.length();
                    pos = searchWindow.lastIndexOf(matchChars);
                }
            }
            String tag;
            if(!found)
            {
                tag = "<0,0," + input.charAt(i) + ">";
                searchWindow += input.charAt(i);
            }
            else
            {
                tag = "<" + (i - pos) + "," + tagLength + ',';

                if(i + tagLength >= input.length())
                {
                    tag += "null" + ">";
                }
                else
                {
                    tag += input.charAt(i + tagLength) + ">";
                }

                for (int j = i; j <= i + tagLength && j < input.length(); j++) {
                    searchWindow += input.charAt(j);
                }
                i += tagLength;
            }
            compressed += tag;
        }
        return compressed;
    }

    public String decompress(String input){
        int pos, len;
        String  next, currWindow = "";

        if(!input.equals("")) {
            String[] tags = input.split(">");

            for (int i = 0; i < tags.length; i++) {
                String currTag = tags[i];
                currTag += ">";
                String tag = currTag.substring(currTag.indexOf("<") + 1, currTag.indexOf(">"));
                String[] attributes = tag.split(",");
                pos = Integer.parseInt(attributes[0]);
                len = Integer.parseInt(attributes[1]);
                next = attributes[2];

                if (pos != 0 || len != 0) {
                    int begin = currWindow.length() - pos;
                    for (int j = 0; j < len; j++) {
                        currWindow += currWindow.charAt(begin);
                        begin++;
                    }
                }
                if(!next.equals("null"))
                    currWindow += next;
            }
        }
        return currWindow;
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
