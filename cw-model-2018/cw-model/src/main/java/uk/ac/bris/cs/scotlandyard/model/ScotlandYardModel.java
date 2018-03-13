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
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;



// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {

	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private List<ScotlandYardPlayer> players;
	private int currentRound = NOT_STARTED;
	private int mrXloc;
	private int currrentPlayerIndex = 0;



	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {
		this.rounds = requireNonNull(rounds);
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		this.graph = requireNonNull(graph);
		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		if (mrX.colour != BLACK) {
			throw new IllegalArgumentException("MrX should be Black");
		}

		List<PlayerConfiguration> configurations = new ArrayList<>();
		for (PlayerConfiguration configuration : restOfTheDetectives) {
			configurations.add(requireNonNull(configuration));
		}
		configurations.add(0, requireNonNull(firstDetective));
		configurations.add(0, requireNonNull(mrX));
		mrXloc = mrX.location;

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
						throw new IllegalArgumentException("Missing ticket");
					}
				}
			}
		}
		players = new ArrayList<>();
		for (PlayerConfiguration c:configurations)
			players.add(new ScotlandYardPlayer(c.player,c.colour,c.location,c.tickets));
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
	public void startRotate() {
		if(isGameOver()) throw new IllegalStateException();
		ScotlandYardPlayer current = players.get(currrentPlayerIndex);
		//if( for(ScotlandYardPlayer p:players) p
		Set<Move> moves = new HashSet<>();
		moves.add(new PassMove(current.colour()));
		current.player().makeMove(this, current.location(), moves, this);
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> l = new ArrayList<>();
		for (ScotlandYardPlayer p : players) l.add(p.colour());
		return Collections.unmodifiableList(l);
	}


	@Override
	public Set<Colour> getWinningPlayers() {
		return Collections.unmodifiableSet(new HashSet<>());
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
			for (ScotlandYardPlayer p : players) {
				if (p.colour() == colour && p.colour() != BLACK) return Optional.of(p.location());
				if (p.colour() == BLACK && p.colour() == colour) {
					if (getCurrentRound() < 3) return Optional.of(0);
					if (getRounds().get(getCurrentRound() - 1)) {
						mrXloc = p.location();
						return Optional.of(mrXloc);
					} else return Optional.of(mrXloc);
				}
			}
			return Optional.empty();
	}


	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for(ScotlandYardPlayer p:players){
			if(p.colour()==colour) return Optional.ofNullable(p.tickets().get(ticket));
		}
		return Optional.empty();
	}

	@Override
	public boolean isGameOver() {
	return false;
	}

	@Override
	public Colour getCurrentPlayer() {
	return players.get(currrentPlayerIndex).colour();
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

}
