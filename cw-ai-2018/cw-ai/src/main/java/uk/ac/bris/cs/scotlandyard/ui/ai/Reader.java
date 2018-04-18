package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.io.*;

//COMS10009 Live Programming Example Code
//Reader: reads undirected graph from file system, simple format
public class Reader
{
    private Graph<Integer,Integer> graph;

    public Graph<Integer,Integer> graph() {
      return graph;
    }
    
    void read(String filename) throws IOException
    {
 	
    	// initialise the graph
    	graph = new UndirectedGraph<Integer,Integer>();
    	
    	// load the file
        File file = new File(filename);
        Scanner in = new Scanner(file);
        
        // get the top line
        String topLine = in.nextLine();      
        int numberOfNodes = Integer.parseInt(topLine);
        
        // create the number of nodes
        for(int i = 0; i < numberOfNodes; i++) {
        	Node<Integer> n = new Node<Integer>(i+1);
        	graph.add(n);
        }      
        
        // read in the graph
        while (in.hasNextLine())
        {
            String line = in.nextLine();
            String[] names = line.split(" ");
            String id1 = names[0];
            String id2 = names[1];
            int mtype;
            if(names[3].equals("LocalRoad")) {
            	mtype = 0;
            } else if(names[3].equals("Underground")) {
            	mtype = 1;
            } else 
            {
            	mtype = 2;
            };
            
            Edge<Integer,Integer> edge = new Edge<Integer,Integer>(graph.getNode(Integer.parseInt(id1)), 
            		                                               graph.getNode(Integer.parseInt(id2)),new Integer(mtype));
            if (mtype==0) {
            	graph.add(edge);
            }
        }
        in.close();
    }    
}