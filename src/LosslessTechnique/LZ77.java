package LosslessTechnique;

import Handlers.CompressionTechniqueHandler;
import Handlers.FileHandler;

public class LZ77 extends CompressionTechniqueHandler {

    public LZ77(FileHandler fileHandler) {
        super(fileHandler);
    }
    @Override
    public void compress(String input){
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
        fileHandler.setFileContent(compressed);
    }
    @Override
    public void decompress(String input){
        int pos, len;
        String  next, decompressed = "";

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
                    int begin = decompressed.length() - pos;
                    for (int j = 0; j < len; j++) {
                        decompressed += decompressed.charAt(begin);
                        begin++;
                    }
                }
                if(!next.equals("null"))
                    decompressed += next;
            }
        }
        fileHandler.setFileContent(decompressed);
    }
}
