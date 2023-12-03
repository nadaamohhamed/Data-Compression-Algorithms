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
