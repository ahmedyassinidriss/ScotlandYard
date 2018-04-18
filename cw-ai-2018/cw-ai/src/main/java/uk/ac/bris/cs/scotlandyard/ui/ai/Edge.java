//COMS10009 Live Programming Example Code
////Edge: implements a simple graph edge
package uk.ac.bris.cs.scotlandyard.ui.ai;
public class Edge<X, Y> {

    private Node<X> source;
    private Node<X> target;
    private Y data;

    public Edge(Node<X> source, Node<X> target, Y data) {
        this.source = source;
        this.target = target;
        this.data = data;
    }

    public void setData(Y data) {
        this.data = data;
    }

    public Node<X> getSource() {
        return source;
    }

    public Node<X> getTarget() {
        return target;
    }

    public Y getData() {
        return data;
    }

    public Node<X> other(Node<X> n) {
        if (source.equals(n)) {
            return target;
        } else if (target.equals(n)) {
            return source;
        }
        return null;
    }

    public Edge<X, Y> swap() {
        return new Edge<X, Y>(target, source, data);
    }

    public String toString() {
        return source.toString() + " " + target.toString() + " "
                + data.toString();
    }
}
