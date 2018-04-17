// COMS10009 Live Programming Example Code
// Main: runs Prim's and Dijkstra's algorithms on simple graphs
package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import com.sun.javafx.collections.MapAdapterChange;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;


//copy from ScotlandYardModel

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;


import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;



public class Main {
  
  public static void main(String[] args) {

	//instances for loading and saving simple graph files

	  Reader reader = new ScotlandYardGraphReader();
	//Reader reader = new Reader();
	  System.out.println("initialised reader");
	Writer writer = new Writer();
	  System.out.println("initialised writer");
	try {
	  //load input graph
	  reader.read("C:/Users/Davis/Desktop/Java/java/cw-ai-2018/cw-ai/resources/graph.txt"); //TODO: change input path
	  Graph<Integer,Integer> graph = reader.graph();
	  System.out.println("Graph read.");

   	  //calculate single-source-all-shortest-paths and output one route
	  DijkstraCalculator dijk = new DijkstraCalculator(graph);
	  Graph<Integer,Integer> result = dijk.getResult(3,159); //from node 3 to node 159
				
	  //output route as graph, use 'java -jar GraphViewer.jar route.txt' to view
	  writer.setGraph(result);
	  writer.write("C:/Users/Davis/Desktop/Java/java/cw-ai-2018/Dijkstra/route.txt"); //TODO: change output path
	  System.out.println("Shortest route graph written.");
	
	  //calculate and output minimum spanning tree 
	  PrimCalculator prim = new PrimCalculator(graph);
	  result = prim.getResult(1);
				
	  //output minimum spanning tree as graph, use 'java -jar GraphViewer.jar mst.txt' to view
	  writer.setGraph(result);
	  writer.write("C:/Users/Davis/Desktop/Java/java/cw-ai-2018/Dijkstra/mst.txt"); //TODO: change output path
	  System.out.println("Minimum spanning tree written.");
	} catch (Exception e) {
		System.out.println(e);

	  //TODO: handle exceptions
} } }