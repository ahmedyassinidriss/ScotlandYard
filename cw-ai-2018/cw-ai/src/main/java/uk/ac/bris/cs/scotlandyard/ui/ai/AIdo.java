package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;
import java.util.*;


@ManagedAI("AIdo")
public class AIdo implements PlayerFactory {

	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	private static class MyPlayer implements Player {

        // gives score based on the type of move to make
        Double ticketTypeScore(ScotlandYardView view, TicketMove move){
            Double ticketType = 0.0;
            if (move.ticket()== Ticket.TAXI) ticketType = 2.5;
            if (move.ticket()== Ticket.BUS) ticketType = 2.3;
            if (move.ticket()== Ticket.UNDERGROUND) ticketType = 2.0;
            if (move.ticket()== Ticket.SECRET) ticketType = 0.4;
            if (move.ticket()== Ticket.DOUBLE) ticketType = 0.2;
            return ticketType;
        }

        // gives score based on the number and types of transport options from the location to score
        Double transportScore(ScotlandYardView view, int locationToScore) {
            Double numberOfTransport = 0.0;
            for (Edge<Integer, Transport> edge : view.getGraph().getEdgesFrom(view.getGraph().getNode(locationToScore))) {
                if (edge.data().equals(Transport.TAXI)) numberOfTransport = numberOfTransport + .5;
                if (edge.data().equals(Transport.BUS)) numberOfTransport = numberOfTransport + 1.5;
                if (edge.data().equals(Transport.UNDERGROUND)) numberOfTransport = numberOfTransport + 3;
                if (edge.data().equals(Transport.FERRY)) numberOfTransport = numberOfTransport + 4;
            }
            return numberOfTransport;
        }

        // gives score based on how many moves are possible from the location to score
        Double numberOfMovesScore (ScotlandYardView view, int locationToScore) {
            Double numberOfMoves = (double) view.getGraph().getEdgesFrom(view.getGraph().getNode(locationToScore)).size();
        return numberOfMoves;
    }

        @Override
		public void makeMove(ScotlandYardView view, int locationOfMrX, Set<Move> moves,
				Consumer<Move> callback) {

            Map<Move,Double> ScoresMap = new HashMap<>(); // Map of each move and its (single value) Score

            //give scores
            for (Move m : moves) {
                if (m instanceof TicketMove){
                    ScoresMap.put(m, numberOfMovesScore(view, ((TicketMove) m).destination()) +
                            transportScore(view, ((TicketMove) m).destination()) + ticketTypeScore(view, ((TicketMove) m)));
                }
            }

            //select optimalMove
            Move optimalMove = null;
            for (Move move : moves) {
                if (ScoresMap.get(move) == Collections.max(ScoresMap.values())) optimalMove= move;
            }

			callback.accept(optimalMove);
		}
	}
}
