package kr.co.wisenut.util.graph;

import java.util.LinkedList;
import java.util.List;

public class DirectedGraph {
    class Edge{
        public String src;
        public String dest;
        public Edge(String src, String dest){
            this.src = src;
            this.dest = dest;
        }
    }

    List<String> vertices;  // tasks
    List<Edge> edges;       // pipes

    public DirectedGraph(){
        this.vertices = new LinkedList<>();
        this.edges = new LinkedList<>();
    }

    public void addVertex(String vertex){
        if(!this.vertices.contains(vertex))
            this.vertices.add(vertex);
    }

    public void addEdge(String src, String dest){
        this.edges.add(new Edge(src, dest));
    }
}
