package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.SECRET;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;

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



// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private List<ScotlandYardPlayer> players;
	private int currentRound = NOT_STARTED;
	private int mrXLoc; //Location of mrX that's displayed
	private int currentPlayerIndex = 0;


	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
							 PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {
		this.rounds = requireNonNull(rounds);
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		this.graph = requireNonNull(graph);
		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty graph");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX has colour Black");
		}

		List<PlayerConfiguration> configurations = new ArrayList<>();
		for (PlayerConfiguration configuration : restOfTheDetectives) {
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, requireNonNull(firstDetective));
		configurations.add(0, requireNonNull(mrX));


		Set<Integer> locationSet = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (locationSet.contains(configuration.location))
				throw new IllegalArgumentException("Duplicate location");
			locationSet.add(configuration.location);
		}


		Set<Colour> colourSet = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {

			if (colourSet.contains(configuration.colour))
				throw new IllegalArgumentException("Duplicate colour");
			colourSet.add(configuration.colour);


			for (PlayerConfiguration c : configurations) {
				for (Ticket t : Ticket.values()) {
					if (!c.tickets.containsKey(t)) {
						throw new IllegalArgumentException("Missing ticket");
					}

				}
				if (c.colour.isDetective()) {
					if (c.tickets.get(Ticket.DOUBLE) != 0 || c.tickets.get(Ticket.SECRET) != 0) {
						throw new IllegalArgumentException("Detective has illegal ticket");
					}
				}
			}
		}
		players = new ArrayList<>();
		for (PlayerConfiguration c : configurations)
			players.add(new ScotlandYardPlayer(c.player, c.colour, c.location, c.tickets));
	}


	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> listOfPlayerColours = new ArrayList<>();
		for (ScotlandYardPlayer p : players) listOfPlayerColours.add(p.colour());
		return Collections.unmodifiableList(listOfPlayerColours);
	}


	@Override
	public Set<Colour> getWinningPlayers() {
		return Collections.unmodifiableSet(new HashSet<>());
	}


	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {    //the function returns the location of the player specified in the argument, not a list of locations of all players. also can't for loop a return statement anyways.
		for (ScotlandYardPlayer p : players) { //better way to specify specific player when given color
			if (p.colour() == colour) {
				if (colour == BLACK) {
					if (getRounds().get(currentRound)) {    //if its reveal round show MrX actual location
						return Optional.of(p.location());
					} else return Optional.of(mrXLoc);
				}
				return Optional.of(p.location());
			}
		}
		return Optional.empty();    //if the player specified in the argument is not in the list of players
	}

		    /*  TA said getPlayerLocation should just return the player Location, not modify anything, which makes sense, get playerLocation is a getter, so have moved the changing the location of MrX to accept method
			if (p.colour() == colour) {
				if (colour == BLACK) {
				    System.out.println(currentRound);
                    System.out.println("size is" + rounds.size());  //on the test, rounds of length 1 is supplied, and we are trying to access the second item [1] in that list, so is Index Error
					if (getRounds().get(currentRound)) {    //LENGTH of rounds is number of moves mrX can make. i.e. has 22 items
						mrXLoc = p.location();
						return Optional.of(p.location());
					} else {
						return Optional.of(mrXLoc);
					}
				} else {
					return Optional.of(p.location());
				}
			}
		}
		return Optional.empty();
		*/


	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer p : players) {
			if (p.colour() == colour) return Optional.ofNullable(p.tickets().get(ticket));
		}
		return Optional.empty();
	}

	@Override
	public boolean isGameOver() {
		return noMoves(players.get(0)); //checks if mrX has no moves so far need to add more conditions such as no rounds left etc
	}

	@Override
	public Colour getCurrentPlayer() {
		return players.get(currentPlayerIndex).colour();
	}

	@Override
	public int getCurrentRound() {
		return currentRound;
	}

	@Override
	public List<Boolean> getRounds() {
		return Collections.unmodifiableList(rounds);
	}


	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<>(graph);
	}

	@Override
	public void startRotate() {
		if (isGameOver()) throw new IllegalStateException();
		requestMove(); // got rid off for loop to pass test which said playersWaitForOtherPlayer if they havent made a move yet
	}


	/* Can do visitor pattern or instanceof
	visit(DoubleMove d )

	*/

	@Override
	public void accept(Move move) {
		if (move == null) throw new NullPointerException("Move cannot be null"); // check if null to past test
		ScotlandYardPlayer current = players.get(currentPlayerIndex);
		if (!(validMove(current).contains(move))) throw new IllegalArgumentException("Move is not valid"); // needed to pass illegal moves test

		//have moved the updating location into accept method, instead of in a getter.
        if (move instanceof DoubleMove){
			current.location(((DoubleMove) move).secondMove().destination()); //moving to final location after the DoubleMove, no need to go to intermediate position, as it is already checked whether the location is empty or not.
        }
        if (move instanceof TicketMove){
			current.location(((TicketMove) move).destination());
        }


		//move.visit(this); visitor pattern (instead of instanceof)
		//mvnw -Dtest=ModelValidMoveTest#testMrXOnlySecretMovesIfOnlySecretMoveTicketsLeft* test

		System.out.println("[currentRound is: " + currentRound + "]");
		System.out.println("[size of rounds is " + rounds.size() + "]");

		if (move.colour() == BLACK) {
            currentRound++;
            if (getRounds().get(currentRound)){
                mrXLoc = current.location();
            }
			if (move instanceof  DoubleMove) {
                currentRound++;     // might be incorrect way of checking for double move (currentRound stuff needs work)
                if (getRounds().get(currentRound)){
                    mrXLoc = current.location();
                }
            }
		}

        //cannot be done with iterator?
		if (currentPlayerIndex < (players.size() - 1)) { // if not yet at the end of the list of players "this is where it loops through all players till the last detective" How??
			currentPlayerIndex++;  // if not yet at the end of the list of players will go to next player
			requestMove();
		}
		else currentPlayerIndex = 0; // if at the end we go back to mrX and startRotate needs to be called again cuz end of round
	}

	// request move simply get current player to make a move
	private  void requestMove() { // basically an extension of start rotate but makes it easier to deal with needing to notify all players to make a move
		ScotlandYardPlayer p = players.get(currentPlayerIndex);
		p.player().makeMove(this, p.location(), validMove(p), requireNonNull(this)); // make current player make a move as require in startRotate
	}

	private Set<Move> validMove(ScotlandYardPlayer p){
		if(p.colour()==BLACK) return validMoveMrx(p);
		return validMoveDetective(p);
	}

		private Set<Move> validMoveDetective(ScotlandYardPlayer current){
			Set<Move> moves = new HashSet<>();
			if(noMoves(current)) moves.add(new PassMove(current.colour())); // if stuck allow passmove
			for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(current.location()))) { //loop gives all possible combinations of nodes from
																										// current location with respective transport
				Integer nextLocation = edge.destination().value();
				Ticket t = fromTransport(edge.data());
				TicketMove firstMove = new TicketMove(current.colour(), t, nextLocation);

					if (isLocationEmpty(nextLocation)) { // check to see if location not occupied
						if (current.tickets().get(t) > 0) moves.add(firstMove);
					}
				}
			return Collections.unmodifiableSet(moves);
		}

	private Set<Move> validMoveMrx(ScotlandYardPlayer current){
		Set<Move> moves = new HashSet<>();
		for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(current.location()))) {

			Integer nextLocation = edge.destination().value();
			Ticket t = fromTransport(edge.data());
			TicketMove firstMove = new TicketMove(current.colour(), t, nextLocation);
			TicketMove firstMoveSecret = new TicketMove(BLACK, SECRET,nextLocation);
				if (isLocationEmpty(nextLocation)) {

					if (current.tickets().get(t) > 0) moves.add(firstMove);
					if (current.tickets().get(SECRET) > 0) moves.add(firstMoveSecret); // also need a secret ticket for mrX
				}

				// Double Move
				for (Edge<Integer, Transport> secondEdge : graph.getEdgesFrom(graph.getNode(nextLocation))) { //all edges leading from all edges leading from initial location
					Integer nextLocationDouble = secondEdge.destination().value(); // double for loop for potential double moves
					Ticket t2 = fromTransport(secondEdge.data());
					TicketMove secondMove = new TicketMove(current.colour(), t2, nextLocationDouble);
					TicketMove secondMoveSecret = new TicketMove(current.colour(), SECRET, nextLocationDouble);
					if (isLocationEmpty(nextLocationDouble) && isLocationEmpty(nextLocation) && (current.tickets().get(DOUBLE) > 0)) { //will need a double ticket & both locations moving to being empty
							if (t2.equals(t)) {
								if (current.tickets().get(t2) >= 2) // making double move using same ticket twice
									moves.add(new DoubleMove(current.colour(), firstMove, secondMove));
							} else if (current.tickets().get(t2) >= 1) // making double move using different (non-SECRET) ticket
								moves.add(new DoubleMove(current.colour(), firstMove, secondMove));

							if (current.tickets().get(SECRET) > 0) { // need at least one secret and either one of t or t2 for following 2 double moves
								if (current.tickets().get(t2) > 0)
									moves.add(new DoubleMove(current.colour(), firstMoveSecret, secondMove));
								if (current.tickets().get(t) > 0)
									moves.add(new DoubleMove(current.colour(), firstMove, secondMoveSecret));
							}
							if (current.tickets().get(SECRET) >= 2) // 2 SECRET tickets, no other tickets required
								moves.add(new DoubleMove(current.colour(), firstMoveSecret, secondMoveSecret));
						}
					}
				}
		return Collections.unmodifiableSet(moves);
	}

	// if detective is on location, returns false (if Mrx is on location, returns true, because detectives can move onto)
		private boolean isLocationEmpty(Integer location) { // used for valid move function mainly to prevent players moving onto occupied places
            List<ScotlandYardPlayer> tempPlayers = new ArrayList<>();
            for (ScotlandYardPlayer p : players) {
                if (p.colour() != BLACK) tempPlayers.add(p); // list of detectives
            } // only need to take into account detectives since "mrX cant move onto mrX" anyway and detectives can do so as to win the game

			for (ScotlandYardPlayer t : tempPlayers) {
				if (location == t.location()) return false; // simply return false/true much better than i++
			}
			return true;
        }


		private boolean noMoves(ScotlandYardPlayer p){
			int i = 0;
			for (Ticket t : Ticket.values()) {
				if (p.tickets().get(t) == 0) i++;
			}
			if (i == 5) return true; // will return true if no tickets left since this automatically means no moves
			int j = 0;
			for (Edge<Integer, Transport> e : graph.getEdgesFrom(graph.getNode(p.location()))) {
				if (p.colour() == BLACK) {
					if (isLocationEmpty(e.destination().value())) j++;// checking if mrX is stuck so no moves
				} else if (isLocationEmpty(e.destination().value())) j++; // check if detective is stuck so no moves
			}
			return (j == 0);
		}
}


