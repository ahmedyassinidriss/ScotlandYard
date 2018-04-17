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


@ManagedAI("AIdo")
public class AIdo implements PlayerFactory {

	// TODO create a new player here
	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	// TODO A sample player that selects a random move
	private static class MyPlayer implements Player {
	    //penalise for certain moves, include DoubleMove




        Double transportScore(ScotlandYardView view, int locationToScore) {
            // Check for how many Transport options at the location
            Double numberOfTransport = 0.0;

            boolean hasTaxi = false;
            boolean hasBus = false;
            boolean hasUnderground = false;
            boolean hasFerry = false;


            for (Edge<Integer, Transport> edge : view.getGraph().getEdgesFrom(view.getGraph().getNode(locationToScore))) {
                if (edge.data().equals(Transport.TAXI)) hasTaxi = true;
                if (edge.data().equals(Transport.BUS)) hasBus = true;
                if (edge.data().equals(Transport.UNDERGROUND)) hasUnderground = true;
                if (edge.data().equals(Transport.FERRY)) hasFerry = true;
            }

            if (hasTaxi) numberOfTransport++;
            if (hasBus) numberOfTransport++;
            if (hasUnderground) numberOfTransport++;
            if (hasFerry) numberOfTransport++;



            return numberOfTransport;
        };






        Double numberOfMovesScore (ScotlandYardView view, int locationToScore) {
    // Check for how many edges from the location
            //Double numberOfMoves = (Double) view.getGraph().getEdgesFrom(view.getGraph().getNode(locationToScore)).size(); //ask TA or Forum

            Double numberOfMoves = 0.0;
        for (Edge<Integer, Transport> edge : view.getGraph().getEdgesFrom(view.getGraph().getNode(locationToScore))) {
            numberOfMoves++;
        }
        return numberOfMoves;
    };


        @Override
		public void makeMove(ScotlandYardView view, int locationOfMrX, Set<Move> moves,
				Consumer<Move> callback) {

            Map<Move,Double> ScoresMap = new HashMap<>(); // Map of each move and its (single value) Score


            for (Move m : moves) {
              //  ScoresMap.put(m, ScoresMap.get(m).add(numberOfMovesScore(view, ((TicketMove) m).destination()))); //returns previous Map with List(using move as key) with new score
                if (m instanceof TicketMove){
                    ScoresMap.put(m, numberOfMovesScore(view, ((TicketMove) m).destination()));}
            }


            for (Move m : moves) {

                if (m instanceof TicketMove) {
                    Double newScore = (ScoresMap.get(m) + transportScore(view, ((TicketMove) m).destination()));
                    ScoresMap.put(m, newScore);
                }
            }



            //select optimalMove
            Move optimalMove = null;
            for (Move m2 : moves) {
                if (ScoresMap.get(m2) == Collections.max(ScoresMap.values())) optimalMove= m2;
            }

			callback.accept(optimalMove);

		}
	}
}
