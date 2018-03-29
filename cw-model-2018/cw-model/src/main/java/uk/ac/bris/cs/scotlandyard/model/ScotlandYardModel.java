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
    private List<ScotlandYardPlayer> detectives;
    private List<Spectator> spectators;


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

        spectators = new ArrayList<>(); // initiating empty list of spectators
        detectives = new ArrayList<>();//initiating empty list of detectives
        for(ScotlandYardPlayer p:players){
            if(p.colour()!=BLACK) detectives.add(p);
        }
    }


    @Override
    public void registerSpectator(Spectator spectator) {
    if(spectator==null) throw new NullPointerException("Spectator can't be null");
    if(spectators.contains(spectator)) throw new IllegalArgumentException("Spectator already registered");
    spectators.add(spectator);
    }

    @Override
    public void unregisterSpectator(Spectator spectator) {
        if(spectator==null) throw new NullPointerException("Spectator can't be null");
        if(!spectators.contains(spectator)) throw new IllegalArgumentException("Spectator not registered");
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
        Set<Colour> detectivesWin = new HashSet<>(), mrXWins = new HashSet<>();
        for(ScotlandYardPlayer d:detectives) detectivesWin.add(d.colour());
        mrXWins.add(BLACK);

        boolean mrxStuck = isStuck(players.get(0)) && currentPlayerIndex==0;
        boolean endOfGame = currentRound==rounds.size() && currentPlayerIndex==0;

        boolean allDetectivesStuck = true, mrXCaught = false;
        for(ScotlandYardPlayer d:detectives){
                if(d.location()==players.get(0).location()) mrXCaught = true;
                if(!isStuck(d)) allDetectivesStuck=false;
        }

        if(mrXCaught || mrxStuck) return Collections.unmodifiableSet(detectivesWin);
        if(noTicketsDetectives() || allDetectivesStuck || endOfGame) return Collections.unmodifiableSet(mrXWins);

        return Collections.unmodifiableSet(new HashSet<Colour>()); // no winning players
    }


    @Override
    public Optional<Integer> getPlayerLocation(Colour colour) {
        for (ScotlandYardPlayer p : players) {
            if (p.colour() == colour) {
                if (colour == BLACK) {
                    //if its reveal round show MrX location, (however if we JUST show this, we are not keeping track of the last known location.
                        return Optional.of(mrXLoc); // mrXloc updated elsewhere

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
        if(currentPlayerIndex==0) { // needs to be end of round for game to ve over
            if (isStuck(players.get(0)))return true; //checks if mrX has no moves
            if (currentRound == rounds.size()) return true; // no  more rounds
        }
        boolean noMovesDetectives = true;
        for(ScotlandYardPlayer d:detectives) {
                if(d.location()==players.get(0).location()) return true; // if a detectives is at same location as mrX then game over
                if (!noMoves(d)) noMovesDetectives = false;
        }
        if(noMovesDetectives) return true;
        return false;
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
        requestMove(); // got rid off for loop to pass test which said playersWaitForOtherPlayer if they haven't made a move yet
    }

    // request move simply get current player to make a move
    private  void requestMove(){ // basically an extension of start rotate but makes it easier to deal with needing to notify all players to make a move
        ScotlandYardPlayer p = players.get(currentPlayerIndex);
        p.player().makeMove(this, p.location(), validMove(p), requireNonNull(this)); // make current player make a move as require in startRotate

        @Override
        public void accept(Move move) {
            if (move == null) throw new NullPointerException("Move cannot be null"); // check if null to past test
            ScotlandYardPlayer current = players.get(currentPlayerIndex);
            if (!(validMove(current).contains(move)))
                throw new IllegalArgumentException("Move is not valid"); // needed to pass illegal moves test

        }
        //move.visit(this); visitor pattern (instead of instanceof)


        if (move.colour() == BLACK) {
            if (move instanceof TicketMove) {
                currentPlayerIndex++;
                players.get(0).location(((TicketMove) move).destination());
                players.get(0).removeTicket(((TicketMove) move).ticket());
                if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
                currentRound++;
                for(Spectator s:spectators) s.onRoundStarted(this, currentRound);
                for(Spectator s:spectators) s.onMoveMade(this, new TicketMove(BLACK,((TicketMove) move).ticket(),mrXLoc));
                requestMove();
            }
            if (move instanceof DoubleMove) {
                currentPlayerIndex++;
                players.get(0).removeTicket((Ticket.DOUBLE));
                int firstLocation = ((DoubleMove) move).firstMove().destination();
                int secondLocation = ((DoubleMove) move).secondMove().destination();
                int mrXlocDouble1 = mrXLoc;
                if(getRounds().get(currentRound)) mrXlocDouble1 = firstLocation;
                int mrXlocDouble2 = mrXlocDouble1;
                if(getRounds().get(currentRound + 1)) mrXlocDouble2 = secondLocation;
                TicketMove t1 = new TicketMove(BLACK, ((DoubleMove) move).firstMove().ticket(), mrXlocDouble1);
                TicketMove t2 = new TicketMove(BLACK, ((DoubleMove) move).secondMove().ticket(), mrXlocDouble2);
                for(Spectator s:spectators) s.onMoveMade(this, new DoubleMove(BLACK,t1,t2));
                players.get(0).location(firstLocation);// change location after round incrementation as said on issue tracker
                if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
                currentRound++;
                players.get(0).removeTicket(((DoubleMove) move).firstMove().ticket());
                for(Spectator s:spectators) s.onRoundStarted(this, currentRound);
                for(Spectator s:spectators) s.onMoveMade(this, new TicketMove(BLACK, ((DoubleMove) move).firstMove().ticket(), mrXLoc));
                players.get(0).location(secondLocation);
                if (getRounds().get(currentRound)) mrXLoc = players.get(0).location();
                currentRound++;
                players.get(0).removeTicket(((DoubleMove) move).secondMove().ticket());
                for(Spectator s:spectators) s.onRoundStarted(this, currentRound);
                for(Spectator s:spectators) s.onMoveMade(this, new TicketMove(BLACK, ((DoubleMove) move).secondMove().ticket(), mrXLoc));
                requestMove();

            }
        }else {  // for detectives
            if(move instanceof TicketMove) {
                players.get(currentPlayerIndex).location(((TicketMove) move).destination());
                players.get(currentPlayerIndex).removeTicket(((TicketMove) move).ticket());
                players.get(0).addTicket(((TicketMove) move).ticket());
            }
            if (currentPlayerIndex < (players.size() - 1)){ // not yet last detective
                currentPlayerIndex++;
                for(Spectator s:spectators) s.onMoveMade(this, move);
                if(isGameOver()) for(Spectator s:spectators) s.onGameOver(this,getWinningPlayers());
                else requestMove();
            }
            else { // last detective just finished moving so rotation over maybe game too
                currentPlayerIndex = 0;
                for(Spectator s:spectators) s.onMoveMade(this, move);
                if(isGameOver()) for(Spectator s:spectators) s.onGameOver(this, getWinningPlayers());
                else for(Spectator s:spectators) s.onRotationComplete(this);
            }
            }

    }

    private Set<Move> validMove(ScotlandYardPlayer p){
        if(p.colour()==BLACK) return validMoveMrx(p);
        return validMoveDetective(p);
    }

    private Set<Move> validMoveDetective(ScotlandYardPlayer detective){
        Set<Move> moves = new HashSet<>();
        for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(detective.location()))) { //loop gives all possible combinations of nodes from
            // current location with respective transport
            Integer nextLocation = edge.destination().value();
            Ticket t = fromTransport(edge.data());
            TicketMove move = new TicketMove(detective.colour(), t, nextLocation);

            if (isLocationEmpty(nextLocation)) { // check to see if location not occupied
                if (detective.hasTickets(t)) moves.add(move);
            }
        }
        boolean noNeededTicket = true;
        for(Move move:moves) if(detective.hasTickets(((TicketMove) move).ticket())) noNeededTicket = false;
            if(noMoves(detective) || noNeededTicket) moves.add(new PassMove(detective.colour()));
        return Collections.unmodifiableSet(moves);
    }

    private Set<Move> validMoveMrx(ScotlandYardPlayer mrX){
        Set<Move> moves = new HashSet<>();
        for (Edge<Integer, Transport> edge : graph.getEdgesFrom(graph.getNode(mrX.location()))) {

            Integer nextLocation = edge.destination().value();
            Ticket t = fromTransport(edge.data());
            TicketMove firstMove = new TicketMove(BLACK, t, nextLocation);
            TicketMove firstMoveSecret = new TicketMove(BLACK, SECRET,nextLocation);
            if (isLocationEmpty(nextLocation)) {
                if (mrX.hasTickets(t)) moves.add(firstMove);
                if (mrX.hasTickets(SECRET)) moves.add(firstMoveSecret); // also need a secret ticket for mrX
            }

            // Double Move
            for (Edge<Integer, Transport> secondEdge : graph.getEdgesFrom(graph.getNode(nextLocation))) { //all edges leading from all edges leading from intial location
                Integer nextLocationDouble = secondEdge.destination().value(); // double for loop for potential double moves
                Ticket t2 = fromTransport(secondEdge.data());
                TicketMove secondMove = new TicketMove(BLACK, t2, nextLocationDouble);
                TicketMove secondMoveSecret = new TicketMove(BLACK, SECRET, nextLocationDouble);
                if (isLocationEmpty(nextLocationDouble) && isLocationEmpty(nextLocation) && (mrX.hasTickets(DOUBLE)) && (getCurrentRound() < (rounds.size() - 1))) { //will need a double ticket & both locations moving to being empty
                    if (t2.equals(t)) {
                        if (mrX.tickets().get(t2) >= 2) // if using same ticket twice need at least one
                            moves.add(new DoubleMove(BLACK, firstMove, secondMove));
                    } else if (mrX.hasTickets(t)) // if not the same just need one of second type (first already checked )
                        moves.add(new DoubleMove(BLACK, firstMove, secondMove));

                    if (mrX.hasTickets(SECRET)) { // need at least one secret and either one of t or t2 for following 2 double moves
                        if (mrX.hasTickets(t2))
                            moves.add(new DoubleMove(BLACK, firstMoveSecret, secondMove));
                        if (mrX.hasTickets(t))
                            moves.add(new DoubleMove(BLACK, firstMove, secondMoveSecret));
                    }
                    if (mrX.tickets().get(SECRET) >= 2) // 2 SECRET tickets no, other tickets required
                        moves.add(new DoubleMove(BLACK, firstMoveSecret, secondMoveSecret));
                }
            }
        }
        return Collections.unmodifiableSet(moves);
    }

    // if detective is on location, returns false (if Mrx is on location, returns true, because detectives can move onto mrX
    private boolean isLocationEmpty(Integer location){ // used for valid move function to prevent players moving onto occupied places
        boolean locationEmpty = true;
        for (ScotlandYardPlayer d:detectives) {
            if (location == d.location()) locationEmpty = false;
        }
        return locationEmpty;
    }


    private boolean noMoves(ScotlandYardPlayer p){
        if (noTicketsDetectives()) return true; // will return true if no tickets left since this automatically means no moves
        boolean stuck = true;
        for (Edge<Integer, Transport> e : graph.getEdgesFrom(graph.getNode(p.location()))) {
                if (isLocationEmpty(e.destination().value())) stuck = false;// checking if player is stuck so no moves
        }
        return stuck;
    }

    private boolean isStuck(ScotlandYardPlayer player){ // function to see if mr x is stuck for the isGameOver function, loops through valid moves
        boolean isStuck = true;
        for (Move move : validMove(player)) {
            if(move instanceof TicketMove){
                for(ScotlandYardPlayer d:detectives) if(((TicketMove) move).destination()!=d.location()) isStuck=false;
            }
        }
        return isStuck;
    }

    private boolean noTicketsDetectives(){
        boolean noTickets = true;
        for(ScotlandYardPlayer d:detectives){
            for(Ticket t:Ticket.values()){
                if(d.hasTickets(t)) noTickets=false;
            }
        }
        return  noTickets;
    }


}
