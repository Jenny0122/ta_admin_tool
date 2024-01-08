package kr.co.wisenut.util.graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CycleDetector {
    public DirectedGraph graph;

    public CycleDetector(){
        this.graph = new DirectedGraph();
    }

    public CycleDetector(DirectedGraph graph){
        this.graph = graph;
    }

    public void addVertex(String vertex){
        this.graph.addVertex(vertex);
    }

    public void addEdge(String src, String dest){
        this.graph.addEdge(src, dest);
    }

    /**
     * Cycle Detection in Directed Graph (referring to Kahn's algorithm for topological sorting)
     * @return check whether if this directed graph has a cycle or not
     */
    public boolean hasCycle(){
        LinkedList<String> s = new LinkedList<>(this.graph.vertices);  // s = set of all vertices with no incoming edge
        LinkedList<DirectedGraph.Edge> edges = new LinkedList<>(this.graph.edges);

        // filter the vertices with no incoming edge
        for(DirectedGraph.Edge e: edges){
            String incoming = e.dest;
            if(s.contains(incoming)){
                s.remove(incoming);
            }
        }

        while(s.size() > 0){
            String n = s.pop();
            Iterator<DirectedGraph.Edge> iter = edges.iterator();
            while(iter.hasNext()){  // edges from n to m
                DirectedGraph.Edge e = iter.next();
                String m = e.dest;
                if(e.src.equals(n)){
                    iter.remove();
                }
                if(!hasIncomingEdge(m, edges))
                    s.add(m);
            }
        }

        if(edges.size() > 0) {
            return true;
        }else
            return false;
    }

    private boolean hasIncomingEdge(String vertex, List<DirectedGraph.Edge> edges){
        for(DirectedGraph.Edge e: edges){
            String incoming = e.dest;
            if(vertex.equals(incoming)){
                return true;
            }
        }
        return false;
    }
}
