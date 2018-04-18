//COMS10009 Live Programming Example Code
//Node: implements a simple graph node
package uk.ac.bris.cs.scotlandyard.ui.ai;
public class Node<X> implements Comparable<Node<X>> {

    private X index;
    private Double weight;

    public Node(X index) {
        this.index = index;
        this.weight = 0.0;
    }
    
    public Node(X index, Double weight) {
        this.index = index;
        this.weight = weight;
    }

    public void setIndex(X index) {
        this.index = index;
    }

    public X getIndex() {
        return index;
    }

    public String toString() {
        return index.toString();
    }
    
    public Double getWeight() {
        return weight;
    }  

    public void setWeight(Double weight) {
        this.weight = weight;
    }  
    
    public int compareTo(Node<X> weightedNode) {
    	return Double.compare(this.weight, weightedNode.getWeight());  
    }
}
