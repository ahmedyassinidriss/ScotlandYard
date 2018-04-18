// COMS10009 Live Programming Example Code
// Main: runs Prim's and Dijkstra's algorithms on simple graphs
package uk.ac.bris.cs.scotlandyard.ui.ai;


public class Main {
  
  public static void main(String[] args) {

	//instances for loading and saving simple graph files

	  //Reader reader = new ScotlandYardGraphReader();
	Reader reader = new Reader();
	  System.out.println("initialised reader");
	Writer writer = new Writer();
	  System.out.println("initialised writer");
	try {
	  //load input graph
	  reader.read("graph.txt"); //TODO: change input path
	  Graph<Integer,Integer> graph = reader.graph();
	  System.out.println("Graph read.");

   	  //calculate single-source-all-shortest-paths and output one route
	  DijkstraCalculator dijk = new DijkstraCalculator(graph);
	  Graph<Integer,Integer> result = dijk.getResult(3,159); //from node 3 to node 159
				
	  //output route as graph, use 'java -jar GraphViewer.jar route.txt' to view
	  writer.setGraph(result);
	  writer.write("route.txt"); //TODO: change output path
	  System.out.println("Shortest route graph written.");
	
	  //calculate and output minimum spanning tree 
	  PrimCalculator prim = new PrimCalculator(graph);
	  result = prim.getResult(1);
				
	  //output minimum spanning tree as graph, use 'java -jar GraphViewer.jar mst.txt' to view
	  writer.setGraph(result);
	  writer.write("mst.txt"); //TODO: change output path
	  System.out.println("Minimum spanning tree written.");
	} catch (Exception e) {
		System.out.println(e);

	  //TODO: handle exceptions
} } }