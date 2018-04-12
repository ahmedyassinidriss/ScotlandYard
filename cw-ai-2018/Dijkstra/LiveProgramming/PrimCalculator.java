//COMS10009 Live Programming Example Code
//PrimCalculator: implements Prim's algorithm
public class PrimCalculator extends GraphCalculator {
	
  public PrimCalculator(Graph<Integer,Integer> graph) {
	super(graph);
  }
  
  //implements Prim's update rule
  protected Double update(Double distance, Double currentDistance, Double directDistance ) {
    return Math.min(distance, directDistance);
} }