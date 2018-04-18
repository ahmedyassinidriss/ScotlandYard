//COMS10009 Live Programming Example Code
//DijkstraCalculator: implements Dijkstra's algorithm
package uk.ac.bris.cs.scotlandyard.ui.ai;

public class DijkstraCalculator extends GraphCalculator {

  public DijkstraCalculator(Graph<Integer,Integer> graph) {
	super(graph);
  }
  
  // implements Dijkstra's update rule
  protected Double update(Double distance, Double currentDistance, Double directDistance ) {
	return Math.min(distance, currentDistance + directDistance);
  }
  
  // runs Dijkstra's algorithm and output particular route
  public Graph<Integer,Integer> getResult(Integer startNodeID, Integer destinationNodeID) {
    
		// calculate graph with paths from every node to start node with its distance
		Graph<Integer,Integer> anyNodeToStart = getResult(startNodeID);

	    // initialise current as end node and initialise graph that will hold the route to return
	    Node<Integer> current = anyNodeToStart.getNode(destinationNodeID);  
	    Graph<Integer,Integer> route = new DirectedGraph<Integer,Integer>();
	    route.add(current);
	    
	    // trace route from end node to start node
	    while (!anyNodeToStart.getEdgesFrom(current).isEmpty()) {
	      Edge<Integer,Integer> e = anyNodeToStart.getEdgesFrom(current).get(0);
	      route.add(e.getTarget());
	      route.add(e);
	      current = e.getTarget();
	    }
	    
	    // return path
	    return route;
} }