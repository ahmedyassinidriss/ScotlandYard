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
    private List<Spectator> spectators;
    private List<ScotlandYardPlayer> detectives;


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

        spectators = new ArrayList<>(); // intiating empty list of spectators

        detectives = new ArrayList<>();
        for (ScotlandYardPlayer p : players) {
            if (p.isDetective()) detectives.add(p);
        }
    }


    @Override
    public void registerSpectator(Spectator spectator) {
        if (spectator == null) throw new NullPointerException("Spectator can't be null");
        if (spectators.contains(spectator)) throw new IllegalArgumentException("Spectator already registered");
        spectators.add(spectator);

    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        if (spectator == null) throw new NullPointerException("Spectator can't be null");
        if (!spectators.contains(spectator)) throw new IllegalArgumentException("Spectator not registered");
        spectators.remove(spectator);
    }

    @Override
    public Collection<Spectator> getSpectators() {
        return Collections.unmodifiableList(spectators);
    }

    @Override
    public List<Colour> getPlayers() {
        List<Colour> listOfPlayerColours = new ArrayList<>();
        for (ScotlandYardPlayer p : players) listOfPlayerColours.add(p.colour());
        return Collections.unmodifiableList(listOfPlayerColours);
    }


    @Override
    public Set<Colour> getWinningPlayers() {
        Set<Colour> mrXWins = new HashSet<Colour>();
        mrXWins.add(BLACK);
        Set<Colour> detectivesWin = new HashSet<>();
        for(ScotlandYardPlayer d:detectives) detectivesWin.add(d.colour());

        //if no Detective has a ticket, MrX wins
        boolean noDetectiveHasTickets = true;
        for (ScotlandYardPlayer d : detectives) {
            for (Ticket t : Ticket.values()) {
                if (d.hasTickets(t)) noDetectiveHasTickets = false;
            }
        }
        if (noDetectiveHasTickets) return detectivesWin;

        // if MrX is stuck, all Detectives win
        if (stuck(players.get(0))) return Collections.unmodifiableSet(detectivesWin);
        //if any detective at same location detectives win
        boolean AllDetectivesStuck = true;
        for (ScotlandYardPlayer d : detectives) {
            if (d.location() == players.get(0).location()) return Collections.unmodifiableSet(detectivesWin);
            if (!stuck(d)) AllDetectivesStuck = false;
        }

        if (AllDetectivesStuck) return Collections.unmodifiableSet(mrXWins);
        if (currentRound == rounds.size() && currentPlayerIndex == 0) return Collections.unmodifiableSet(mrXWins);
        return Collections.unmodifiableSet(emptySet());
    }


    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        for (ScotlandYardPlayer p : players) {
            if (p.colour() == colour) {
                if (colour == BLACK) {
                    //if its reveal round show MrX location, (however if we JUST show this, we are not keeping track of the last known location.
                    return Optional.of(mrXLoc);

                }
                return Optional.of(p.location());
            }
        }
        return Optional.empty();

    }



    @Override
    public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
        for (ScotlandYardPlayer p : players) {
            if (p.colour() == colour) return Optional.ofNullable(p.tickets().get(ticket));
        }
        return Optional.empty();
    }

    @Override
    public boolean isGameOver() {
        if (currentPlayerIndex == 0) { // needs to be end of round for game to be over
            if (stuck(players.get(0)))
                return true; //checks if mrX is stuck so far need to add more conditions such as no rounds left etc
            if (currentRound == rounds.size()) return true;
        }
        boolean allDetectivesStuck = true;
        for (ScotlandYardPlayer d:detectives) {
            if (d.location() == players.get(0).location())
                return true; // if a detectives is at same location as mrX then game over
            if (!stuck(d)) allDetectivesStuck = false;
        }
        return (allDetectivesStuck);// if i is same as the number of detectives they must all be stuck
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
        if (isGameOver()) throw new IllegalStateException("Game is over");
        requestMove();
    }


	/* Can do visitor pattern or instanceof
	visit(DoybleMovr d )
	*/

	//changes state of game and informs spectators of change
    @Override
    public void accept(Move move) {
        if (move == null) throw new NullPointerException("Move cannot be null"); // check if null to past test
        ScotlandYardPlayer current = players.get(currentPlayerIndex);
        if (!(validMove(current).contains(move)))
            throw new IllegalArgumentException("Move is not valid"); // needed to pass illegal moves test

        //move.visit(this); visitor pattern (instead of instanceof)
        if (move.colour() == BLACK) acceptMrX(move);
        else acceptDetective(move);
    }


    void acceptMrX(Move move) {

        if (move instanceof TicketMove) {
            currentPlayerIndex++;
            players.get(0).location(((TicketMove) move).destination());
            players.get(0).removeTicket(((TicketMove) move).ticket());
            if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
            currentRound++;
            for (Spectator s : spectators) {
                s.onRoundStarted(this, currentRound);
                s.onMoveMade(this, new TicketMove(BLACK, ((TicketMove) move).ticket(), mrXLoc));
            }
            requestMove();

        }
        if (move instanceof DoubleMove) {
            currentPlayerIndex++;
            players.get(0).removeTicket((Ticket.DOUBLE));
            int firstLocation = ((DoubleMove) move).firstMove().destination();
            int secondLocation = ((DoubleMove) move).secondMove().destination();
            int mrXlocDouble1 = mrXLoc;
            if (getRounds().get(currentRound)) mrXlocDouble1 = firstLocation;
            int mrXlocDouble2 = mrXlocDouble1;
            if (getRounds().get(currentRound + 1)) mrXlocDouble2 = secondLocation;
            TicketMove t1 = new TicketMove(BLACK, ((DoubleMove) move).firstMove().ticket(), mrXlocDouble1);
            TicketMove t2 = new TicketMove(BLACK, ((DoubleMove) move).secondMove().ticket(), mrXlocDouble2);
            for (Spectator s : spectators) s.onMoveMade(this, new DoubleMove(BLACK, t1, t2));

            players.get(0).location(firstLocation);// change location after round incrementation as said on issue tracker
            if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
            currentRound++;
            players.get(0).removeTicket(((DoubleMove) move).firstMove().ticket());
            for (Spectator s : spectators) {
                s.onRoundStarted(this, currentRound);
                s.onMoveMade(this, new TicketMove(BLACK, ((DoubleMove) move).firstMove().ticket(), mrXLoc));
            }

            players.get(0).location(secondLocation);
            if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
            currentRound++;
            players.get(0).removeTicket(((DoubleMove) move).secondMove().ticket());
            for (Spectator s : spectators) {
                s.onRoundStarted(this, currentRound);
                s.onMoveMade(this, new TicketMove(BLACK, ((DoubleMove) move).secondMove().ticket(), mrXLoc));
            }
            requestMove();
        }
    }


    void acceptDetective(Move move){
        {
            if (move instanceof TicketMove) {
                players.get(currentPlayerIndex).location(((TicketMove) move).destination());
                players.get(currentPlayerIndex).removeTicket(((TicketMove) move).ticket());
                players.get(0).addTicket(((TicketMove) move).ticket());
            }
            if (currentPlayerIndex < (players.size() - 1)) {
                currentPlayerIndex++;
                for (Spectator s : spectators) s.onMoveMade(this, move);
                if (isGameOver()) for (Spectator s : spectators) s.onGameOver(this, getWinningPlayers());
                else requestMove();
            } else {
                currentPlayerIndex = 0;
                for (Spectator s : spectators) s.onMoveMade(this, move);
                if (isGameOver()) for (Spectator s : spectators) s.onGameOver(this, getWinningPlayers());
                else for (Spectator s : spectators) s.onRotationComplete(this);
            }
        }
    }

    // request current player to make a move
    private void requestMove() {
        ScotlandYardPlayer p = players.get(currentPlayerIndex);
        p.player().makeMove(this, p.location(), validMove(p), requireNonNull(this));
    }

    //return valid Moves Player can make
    private Set<Move> validMove(ScotlandYardPlayer p) {
        if (p.colour() == BLACK) return validMoveMrx(p);
        else return validMoveDetective(p);
    }

    // returns all valid Moves Detectives can make
    private Set<Move> validMoveDetective(ScotlandYardPlayer current) {
        Set<Move> moves = new HashSet<>();
        for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(current.location()))) {
            Integer nextLocation = edge.destination().value();
            Ticket t = fromTransport(edge.data());
            TicketMove firstMove = new TicketMove(current.colour(), t, nextLocation);

            if (isLocationEmpty(nextLocation)) {
                if (current.hasTickets(t)) moves.add(firstMove);
            }
        }
        if(moves.isEmpty()) moves.add(new PassMove(current.colour()));
        return Collections.unmodifiableSet(moves);
    }


    // returns all valid Moves MrX can make
    private Set<Move> validMoveMrx(ScotlandYardPlayer current) {
        Set<Move> moves = new HashSet<>();
        for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(current.location()))) {

            Integer nextLocation = edge.destination().value();
            Ticket t = fromTransport(edge.data());
            TicketMove firstMove = new TicketMove(current.colour(), t, nextLocation);
            TicketMove firstMoveSecret = new TicketMove(BLACK, SECRET, nextLocation);
            if (isLocationEmpty(nextLocation)) {
                if (current.hasTickets(t)) moves.add(firstMove);
                if (current.hasTickets(SECRET)) moves.add(firstMoveSecret);
            }

            // Double Move
            for (Edge<Integer, Transport> secondEdge : graph.getEdgesFrom(graph.getNode(nextLocation))) { //all edges leading from all edges leading from intial location
                Integer nextLocationDouble = secondEdge.destination().value();
                Ticket t2 = fromTransport(secondEdge.data());
                TicketMove secondMove = new TicketMove(current.colour(), t2, nextLocationDouble);
                TicketMove secondMoveSecret = new TicketMove(current.colour(), SECRET, nextLocationDouble);
                if (isLocationEmpty(nextLocationDouble) && isLocationEmpty(nextLocation) && (current.hasTickets(DOUBLE)) && (getCurrentRound() < (rounds.size() - 1))) { //will need a double ticket & both locations moving to being empty
                    if (t2.equals(t)) {
                        if (current.tickets().get(t2) >= 2)
                            moves.add(new DoubleMove(current.colour(), firstMove, secondMove));
                    } else if (current.hasTickets(t2) && current.hasTickets(t))
                        moves.add(new DoubleMove(current.colour(), firstMove, secondMove));

                    if (current.hasTickets(SECRET)) {
                        if (current.hasTickets(t2))
                            moves.add(new DoubleMove(current.colour(), firstMoveSecret, secondMove));
                        if (current.hasTickets(t))
                            moves.add(new DoubleMove(current.colour(), firstMove, secondMoveSecret));
                    }
                    if (current.tickets().get(SECRET) >= 2)
                        moves.add(new DoubleMove(current.colour(), firstMoveSecret, secondMoveSecret));
                }
            }
        }
        return Collections.unmodifiableSet(moves);
    }

    // if detective is on location, returns false (if MrX is on location, returns true, because detectives can move onto MrX)
    private boolean isLocationEmpty(Integer location) {
        for (ScotlandYardPlayer d:detectives) {
            if (location == d.location()) return false;
        }
        return true;
    }

// checks if player can make any more moves
    private boolean stuck(ScotlandYardPlayer player){
        boolean isStuck = true;
        for (Move move : validMove(player)) {
            if (move instanceof TicketMove) {
                for (ScotlandYardPlayer d : detectives) {
                    if (((TicketMove) move).destination() != d.location()) {
                        isStuck = false;
                        break;
                    }
                }
            }
        }
        return (isStuck);
    }
}