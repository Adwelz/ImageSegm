import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PrimAlgorithm {
    
    private static class Edge {
        int src, dest;
        short weight;
        public Edge(int src, int dest, short weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }
    
    private static int[] prim(List<Edge>[] graph, int startingNode) {
        int n = graph.length;
        int[] parent = new int[n];
        short[] weight = new short[n];
        boolean[] visited = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        Arrays.fill(weight, Short.MAX_VALUE);
    
        weight[startingNode] = 0;
        parent[startingNode] = -1;
        pq.add(new Edge(-1, startingNode, (short) 0));
    
        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            if (visited[edge.dest]) continue;
            visited[edge.dest] = true;
            parent[edge.dest] = edge.src;
            for (Edge e : graph[edge.dest]) {
                if (!visited[e.dest] && e.weight < weight[e.dest]) {
                    weight[e.dest] = e.weight;
                    pq.add(e);
                }
            }
        }
        return parent;
    }
    
    public static void main(String[] args) {
        int n = 4;
        List<Edge>[] graph = new List[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        graph[0].add(new Edge(0, 1, (short) 2));
        graph[0].add(new Edge(2, 0, (short) 6));
        graph[1].add(new Edge(2, 3,(short) 2));
        graph[1].add(new Edge(1, 3, (short) 3));
        
        int[] parent = prim(graph,0);
        for (int i = 0; i < n; i++) {
            System.out.println(parent[i] + " - " + i);
        }
    }
}