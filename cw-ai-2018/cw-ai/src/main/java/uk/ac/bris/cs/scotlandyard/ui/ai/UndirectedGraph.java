//COMS10009 Live Programming Example Code
//UndirectedGraph: implements a simple, directed graph
package uk.ac.bris.cs.scotlandyard.ui.ai;
public class UndirectedGraph<X, Y> extends DirectedGraph<X, Y>{

    public UndirectedGraph() {
        super();
    }

    @Override
    public void add(Edge<X, Y> edge){
        super.add(edge);
        super.add(edge.swap());
    }  
}
