package tablut;


import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Formatter;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/**
 * The state of a Tablut Game.
 *
 * @author chenyuanshan
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /**
     * Create a list the show the square adjacent to THRONE.
     */
    static final Square[] ADJACENT_THRONES = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE
    };

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        _board = model._board;
        _moveCount = model._moveCount;
        _repeated = model._repeated;
        _turn = model._turn;
        _winner = model._winner;
        _boardset = model._boardset;
        _storage = model._storage;
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {
        for (int i = 0; i < SIZE; i = i + 1) {
            for (int j = 0; j < SIZE; j = j + 1) {
                _board.put(sq(i, j), EMPTY);
            }
        }
        for (Square defenders : INITIAL_DEFENDERS) {
            _board.replace(defenders, WHITE);
        }
        for (Square attacker : INITIAL_ATTACKERS) {
            _board.replace(attacker, BLACK);
        }
        _board.replace(THRONE, KING);
        _turn = BLACK;
        _moveCount = 0;
        _winner = null;
        _repeated = false;
        clearUndo();
        _boardset.add(encodedBoard());
        _storage.push(deepcopy(_board));
    }

    /**
     * Set the move limit to N.  It is an error if 2*N <= moveCount().
     */
    void setMoveLimit(int n) {
        if (moveCount() >= 2 * n) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     */
    private void checkRepeated() {
        if (_boardset.contains(encodedBoard())) {
            _repeated = true;
        }
        if (_repeated) {
            if (_turn == BLACK) {
                this._winner = BLACK;
            } else if (_turn == WHITE) {
                this._winner = WHITE;
            }
        }
    }

    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     */
    Square kingPosition() {
        for (Square target : _board.keySet()) {
            if (_board.get(target) == KING) {
                return target;
            }
        }
        return null;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    final Piece get(int col, int row) {
        Square temp = sq(col, row);
        if (_board.containsKey(temp)) {
            return _board.get(temp);
        }
        return null;
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        if (_board.containsKey(s)) {
            _board.replace(s, p);
        }
    }

    /**
     * Set square S to P and record for undoing.
     */
    final void revPut(Piece p, Square s) {
        put(p, s);
        _moveCount = _moveCount + 1;
    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {

        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     */
    boolean isUnblockedMove(Square from, Square to) {
        if (from.isRookMove(to)) {
            int step = Math.max(Math.abs(from.row() - to.row()),
                    Math.abs(from.col() - to.col()));
            int dir = from.direction(to);
            if (step == 1) {
                return true;
            }
            int count = 0;
            for (int i = 1; i < step; i = i + 1) {
                if (_board.get(from.rookMove(dir, i)) != EMPTY) {
                    return false;
                } else {
                    count = count + 1;
                }
            }
            if (count == step - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid move.
     */
    boolean isLegal(Square from, Square to) {
        if (to == null || from == null) {
            return false;
        }
        if (!isUnblockedMove(from, to)) {
            return false;
        }
        if (_board.get(from).side() != _turn) {
            return false;
        }
        if (_board.get(to) != EMPTY || _board.get(from) == EMPTY) {
            return false;
        }
        if (_board.get(from) != KING && to == THRONE) {
            return false;
        }
        if (_board.get(from) == KING && _turn == BLACK) {
            return false;
        }
        return true;
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {

        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     */
    void makeMove(Square from, Square to) {
        Piece temp = _board.get(from); _board.replace(to, temp);
        _board.replace(from, EMPTY); _moveCount = _moveCount + 1;
        if (this.turn() == BLACK) {
            _turn = WHITE;
        } else {
            _turn = BLACK;
        }
        if (temp == KING) {
            if (to.isEdge()) {
                _winner = WHITE;
            }
        }
        docapture(to, temp);
        checkRepeated();
        _boardset.add(encodedBoard()); _storage.push(deepcopy(_board));
        if (kingPosition() == null) {
            _winner = BLACK;
        }
        if (!hasMove(_turn)) {
            if (_turn == BLACK) {
                _winner = WHITE;
            } else {
                _winner = BLACK;
            }
        }
    }
    /**
     * A new method to do capture with TO and TEMP.
     */
    void docapture(Square to, Piece temp) {
        for (int dir = 0; dir < 4; dir = dir + 1) {
            Square onestep = to.rookMove(dir, 2);
            if (onestep != null) {
                Square btw = to.between(onestep);
                if (_board.get(btw) == KING) {
                    boolean flag = false;
                    for (Square def : ADJACENT_THRONES) {
                        if (def == btw || btw == THRONE) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        int ct = 0;
                        for (int d = 0; d < 4; d = d + 1) {
                            Square srd = btw.rookMove(d, 1);
                            if (_board.get(srd).side() == BLACK
                                    || srd == THRONE) {
                                ct = ct + 1;
                            }
                        }
                        if (ct == 4) {
                            capture(to, onestep);
                        }
                    } else if ((_board.get(onestep).side() == temp.side())) {
                        if (_board.get(btw).side() != temp.side()) {
                            capture(to, onestep);
                        }
                    }
                } else if ((_board.get(onestep).side()
                        == temp.side())
                        || (onestep == THRONE
                        && _board.get(onestep) == EMPTY)) {
                    if (_board.get(btw).side() != temp.side()
                            && _board.get(btw) != EMPTY) {
                        capture(to, onestep);
                    }
                } else if (onestep == THRONE
                        && _board.get(onestep) != EMPTY) {
                    if (temp.side() == BLACK) {
                        int count = 0;
                        for (int d = 0; d < 4; d = d + 1) {
                            Square srd = onestep.rookMove(d, 1);
                            if (_board.get(srd).side() == BLACK) {
                                count = count + 1;
                            }
                        }
                        if (count == 3 && _board.get(btw).side() == WHITE) {
                            capture(to, onestep);
                        }
                    }
                }
            }
        }
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     */
    private void capture(Square sq0, Square sq2) {
        Square bt = sq0.between(sq2);
        _board.replace(bt, EMPTY);

    }

    /**
     * Undo one move.  Has no effect on the initial board.
     */
    void undo() {
        if (_moveCount > 0) {
            _boardset.remove(encodedBoard());
            undoPosition();
            _moveCount = _moveCount - 1;
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     */
    private void undoPosition() {
        if (_moveCount >= 1) {
            if (_storage.size() >= 2) {
                _storage.pop();
                _board = deepcopy(_storage.peek());
            }
            if (turn() == BLACK) {
                _turn = WHITE;
            } else {
                _turn = BLACK;
            }

        }
        _repeated = false;
        _winner = null;
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     */
    void clearUndo() {

        _boardset.clear();
        _storage.clear();
        _moveCount = 0;
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     */
    List<Move> legalMoves(Piece side) {
        ArrayList<Square> currentside = new ArrayList<Square>();
        ArrayList<Move> legalmvs = new ArrayList<Move>();
        for (Square allsquares : _board.keySet()) {
            if (_board.get(allsquares).side() == side) {
                currentside.add(allsquares);
            }
            if (_board.get(allsquares) == KING && side == WHITE) {
                currentside.add(allsquares);
            }
        }
        for (Square curposition : currentside) {
            for (int dir = 0; dir < 4; dir = dir + 1) {
                for (int step = 0; step < SIZE;
                     step = step + 1) {
                    Square destination = curposition.rookMove(dir, step);
                    if (isLegal(curposition, destination)
                            && destination != null) {
                        Move legal = mv(curposition, destination);
                        legalmvs.add(legal);
                    }
                }
            }
        }
        return legalmvs;
    }

    /**
     * Return true iff SIDE has a legal move.
     */
    boolean hasMove(Piece side) {
        return !legalMoves(side).isEmpty();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locateofside = new HashSet<>();
        for (Square s : _board.keySet()) {
            if (_board.get(s).side() == side) {
                locateofside.add(s);
            }
        }
        return locateofside;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /**
     * Return the hashmap _board of me.
     */
    public HashMap<Square, Piece> returnboard() {
        return _board;
    }


    /**
     * Return the deepcopy of hashmap B.
     */
    public HashMap<Square, Piece> deepcopy(HashMap<Square, Piece> b) {
        HashMap<Square, Piece> temp = new HashMap<Square, Piece>();
        for (Square s : b.keySet()) {
            Piece tempp = b.get(s);
            temp.put(s, tempp);
        }
        return temp;
    }

    /**
     * Return whether it's game over.
     */
    boolean gameOver() {
        if (kingPosition() == null || kingPosition().isEdge()) {
            return true;
        }
        if (_winner != null) {
            return true;
        }
        return _repeated;
    }

    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn;
    /**
     * Cached value of winner on this board, or null if it has not been
     * computed.
     */
    private Piece _winner;
    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount;
    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;

    /**
     * Create a hashmap board to map its square and piece.
     */
    private HashMap<Square, Piece> _board = new HashMap<>();


    /**
     * Create a Hashset to record the board after each movement.
     */
    private HashSet<String> _boardset = new HashSet<String>();


    /**
     * Create a stack to store the hashmap of the board.
     */
    private Stack<HashMap<Square, Piece>> _storage
            = new Stack<HashMap<Square, Piece>>();

}
