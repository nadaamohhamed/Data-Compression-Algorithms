public class Leaf extends HuffmanNode{
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
