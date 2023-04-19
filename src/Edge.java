class Edge implements Comparable<Edge> {
    int u;
    int v;
    short weight;
    
    public Edge(int u, int v, short weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }
    
    @Override
    public int compareTo(Edge other) {
        return Short.compare(this.weight, other.weight);
    }
}
