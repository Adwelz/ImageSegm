class Node implements Comparable<Node> {
    int id;
    float key;
        
    public Node(int id, float key) {
        this.id = id;
        this.key = key;
    }
        
    public int compareTo(Node other) {
        return Float.compare(this.key, other.key);
    }
}