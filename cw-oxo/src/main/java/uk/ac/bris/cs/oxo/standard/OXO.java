package uk.ac.bris.cs.oxo.standard;

import static java.util.Objects.requireNonNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.matrix.ImmutableMatrix;
import uk.ac.bris.cs.gamekit.matrix.Matrix;
import uk.ac.bris.cs.gamekit.matrix.SquareMatrix;
import uk.ac.bris.cs.oxo.Cell;
import uk.ac.bris.cs.oxo.Outcome;
import uk.ac.bris.cs.oxo.Player;
import uk.ac.bris.cs.oxo.Side;
import uk.ac.bris.cs.oxo.Spectator;

public class OXO implements OXOGame, Consumer<Move> {

	private Player noughtSide, crossSide;
	private Side currentSide;
	private int size;
	private SquareMatrix<Cell> prematrix;
    private ImmutableMatrix<Cell> matrix;


    private Consumer<Move> callback;



    private Set<Move> validMvs;

	public OXO(int size, Side startSide, Player nought, Player cross) {

		if(size <= 0) throw new IllegalArgumentException("size invalid");
		this.size = size;

		this.currentSide = java.util.Objects.requireNonNull(startSide);
		this.noughtSide = java.util.Objects.requireNonNull(nought);
		this.crossSide = java.util.Objects.requireNonNull(cross);

		this.prematrix = new SquareMatrix<Cell>(size, new Cell());
        this.matrix = new ImmutableMatrix<Cell> (prematrix);
	}

	@Override
	public Matrix<Cell> board() {
		return matrix;
	}

	@Override
	public Side currentSide() {
		return currentSide;
	}


	@Override
	public void registerSpectators(Spectator... spectators) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectators(Spectator... spectators) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void start() {

        Player player = (currentSide == Side.CROSS) ? crossSide : noughtSide;

        this.validMvs = validMoves();

        player.makeMove(this, validMvs, callback);

    }


    private Set<Move> validMoves() {
        Set<Move> moves = new HashSet<>();
        for (int row = 0; row < matrix.rowSize(); row++) {
            for (int col = 0; col < matrix.columnSize(); col++) {
                moves.add(new Move(row, col));
            } }
        return moves;
    }


    @Override
    public void accept(Move move) {

                if (!validMvs.contains(move)) throw new IllegalArgumentException("Argument invalid");
	            	// else this.matrix[move.row][move.column] = currentSide;
								else this.matrix.put(move.row,move.column, currentSide);

    }
}
