import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//COMS10009 Live Programming Example Code
//Writer: writes graph to file system, simple format
public class Writer {
	private Graph<Integer,Integer> graph;
	
	public void setGraph(Graph<Integer,Integer> graph) {
		this.graph = graph;
	}
	
	public void write(String filename) throws IOException
	{
		// try and write the file
		FileWriter writer = new FileWriter(filename);
		PrintWriter printer = new PrintWriter(writer);
		
		String nodeNumber = Integer.toString(graph.getNodes().size());
		printer.println(nodeNumber);
		
		// now we write all the edges
		List<Edge<Integer,Integer>> edges = new ArrayList<Edge<Integer,Integer>>(graph.getEdges());
		for(Edge<Integer,Integer> e: edges) {		
			String line = e.getSource().toString() + " " + 
					      e.getTarget().toString() + " 1.0 LocalRoad" ;
			printer.println(line);
		}
		writer.close();
	}
}
