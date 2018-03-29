import java.util.*;

//COMS10009 Live Programming Example Code
//DirectedGraph: implements a simple, directed graph
public class DirectedGraph<X, Y> implements Graph<X, Y>{

    private Map<X, Node<X>> nodeMap;
    private Map<Node<X>, List<Edge<X, Y>>> sourceEdges;
    private Map<Node<X>, List<Edge<X, Y>>> targetEdges;
    private List<Node<X>> allNodes;
    private List<Edge<X, Y>> allEdges;

    public DirectedGraph() {
        nodeMap = new HashMap<X, Node<X>>();
        sourceEdges = new HashMap<Node<X>, List<Edge<X, Y>>>();
        targetEdges = new HashMap<Node<X>, List<Edge<X, Y>>>();
        allNodes = new ArrayList<Node<X>>();
        allEdges = new ArrayList<Edge<X, Y>>();
    }

    //@Override
    public void add(Node<X> node){
        nodeMap.put(node.getIndex(), node);
        allNodes.add(node);
        sourceEdges.put(node, new ArrayList<Edge<X, Y>>());
        targetEdges.put(node, new ArrayList<Edge<X, Y>>());
    }

    //@Override
    public void add(Edge<X, Y> edge){
        Node<X> source = getNode(edge.getSource().getIndex());
        Node<X> target = getNode(edge.getTarget().getIndex());
        sourceEdges.get(source).add(edge);
        targetEdges.get(target).add(edge);
        allEdges.add(edge);
    }

    //@Override
    public List<Edge<X, Y>> getEdges(){
        return allEdges;
    }

    //@Override
    public List<Node<X>> getNodes(){
        return allNodes;
    }

    //@Override
    public Node<X> getNode(X index){
        return nodeMap.get(index);
    }

    //@Override
    public List<Edge<X, Y>> getEdgesTo(Node<X> node){
        return targetEdges.get(node);
    }

    //@Override
    public List<Edge<X, Y>> getEdgesFrom(Node<X> node){
        return sourceEdges.get(node);
    }

    @Override
    public String toString() {
        String output = "";
        for (Node<X> node : allNodes) {
            output += node.toString() + "\n";
        }

        for (Edge<X, Y> edge : allEdges) {
            output += edge.toString() + "\n";
        }

        return output;
    }
}
