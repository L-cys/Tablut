package tablut;

import static java.lang.Math.*;

import static tablut.Piece.*;

/**
 * A Player that automatically generates moves.
 *
 * @author chenyuanshan
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A number that used to countdown the piece.
     */
    private static final int TWENTY = 20;

    /**
     * A number that used to calculate the king.
     */
    private static final int FORTY = 40;

    /**
     * The number of nine.
     */
    private static final int NINE = 9;


    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        if (board().turn() != myPiece() || board().winner() != null) {
            return "No legal move";
        } else {
            Move rst = findMove();
            _controller.reportMove(rst);
            return rst.toString();
        }
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (b.turn() == BLACK) {
            findMove(b, maxDepth(b), true, -1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            findMove(b, maxDepth(b), true, 1,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (sense == 1) {
            if (depth == 0 || board.gameOver()) {
                return staticScore(board);
            }
            int bestSoFar = Integer.MIN_VALUE;
            for (Move m : board.legalMoves(WHITE)) {
                board.makeMove(m);
                if (board.repeatedPosition()) {
                    board.undo(); continue;
                }
                if (board.winner() == WHITE) {
                    _lastFoundMove = m;
                    bestSoFar = WINNING_VALUE; break;
                }
                int t = findMove(board, depth - 1, false,
                        -1 * sense, alpha, beta);
                board.undo();
                if (t >= bestSoFar) {
                    bestSoFar = t; alpha = max(alpha, bestSoFar);
                    if (saveMove) {
                        _lastFoundMove = m;
                    }
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            return bestSoFar;
        } else {
            if (depth == 0 || board.gameOver()) {
                return staticScore(board);
            }
            int bestSoFara = Integer.MAX_VALUE;
            for (Move mv : board.legalMoves(BLACK)) {
                board.makeMove(mv);
                if (board.repeatedPosition()) {
                    board.undo(); continue;
                }
                if (board.winner() == BLACK) {
                    _lastFoundMove = mv;
                    bestSoFara = Integer.MIN_VALUE; break;
                }
                int ta = findMove(board, depth - 1, false,
                        -1 * sense, alpha, beta);
                board.undo();
                if (ta <= bestSoFara) {
                    bestSoFara = ta; beta = min(beta, bestSoFara);
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return bestSoFara;
        }
    }

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        return 1;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        int result = 0;
        if (board.kingPosition() == null) {
            return Integer.MIN_VALUE;
        }
        if (board.kingPosition().isEdge()) {
            return WINNING_VALUE;
        }


        Square king = board.kingPosition();

        int count = 0;
        for (int dir = 0; dir < 4; dir = dir + 1) {
            Square adjacent = board.kingPosition().rookMove(dir, 1);
            if (board.returnboard().get(adjacent).side() == BLACK) {
                count = count + 1;
            }
        }
        result = result - count * FORTY;
        int coldis = max(abs(NINE - king.col()), king.col());
        int rowdis = max(abs(NINE - king.col()), king.row());
        int dis = max(coldis, rowdis);
        result = result + dis * FORTY;

        int blacknumber = 0;
        int whitenumber = 0;
        for (Square sq : board.returnboard().keySet()) {
            if (board.returnboard().get(sq).side() == BLACK) {
                blacknumber = blacknumber + 1;
            } else if (board.returnboard().get(sq).side() == WHITE) {
                whitenumber = whitenumber + 1;
            }
        }
        int whitegone = NINE - whitenumber;
        result = result - whitegone * TWENTY;
        result = result - blacknumber * TWENTY;
        return result;
    }


}
