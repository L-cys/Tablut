package tablut;

import org.junit.Test;

import static org.junit.Assert.*;

import ucb.junit.textui;

/**
 * The suite of all JUnit tests for the enigma package.
 *
 * @author chenyuanshan
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * Test simple movement.
     */
    @Test
    public void simplemovetest() {
        Board simplemove = new Board();
        Square fa = Square.sq(7, 4);
        Square ta = Square.sq(7, 5);
        simplemove.makeMove(fa, ta);
    }

    /**
     * Test King movement.
     */
    @Test
    public void kingtest() {
        Board kingmove = new Board();
        Square f = Square.sq(7, 4);
        Square t = Square.sq(7, 5);
        kingmove.makeMove(f, t);
        Square fa = Square.sq(4, 3);
        Square ta = Square.sq(1, 3);
        kingmove.makeMove(fa, ta);
        Square fc = Square.sq(7, 5);
        Square tc = Square.sq(7, 6);
        kingmove.makeMove(fc, tc);
        Square fb = Square.sq(4, 4);
        Square tb = Square.sq(4, 3);
        kingmove.makeMove(fb, tb);
    }

    /**
     * Test whether AI will run.
     */
    @Test
    public void repeattest() {
        Board cptest = new Board();
        Square f = Square.sq(8, 5);
        Square t = Square.sq(8, 8);
        cptest.makeMove(f, t);
        Square fa = Square.sq(4, 3);
        Square ta = Square.sq(1, 3);
        cptest.makeMove(fa, ta);
        Square fc = Square.sq(8, 8);
        Square tc = Square.sq(8, 5);
        cptest.makeMove(fc, tc);
        Square fd = Square.sq(1, 3);
        Square td = Square.sq(4, 3);
        cptest.makeMove(fd, td);


    }

    @Test
    public void capturetest() {
        Board cp = new Board();
        cp.makeMove(Square.sq(8, 5), Square.sq(5, 5));
        cp.makeMove(Square.sq(4, 5), Square.sq(1, 5));
        cp.makeMove(Square.sq(8, 3), Square.sq(5, 3));
        cp.makeMove(Square.sq(4, 3), Square.sq(1, 3));
        cp.makeMove(Square.sq(5, 5), Square.sq(4, 5));
        cp.makeMove(Square.sq(1, 3), Square.sq(1, 0));
        cp.makeMove(Square.sq(5, 3), Square.sq(4, 3));
        cp.makeMove(Square.sq(3, 4), Square.sq(3, 7));
        cp.makeMove(Square.sq(3, 0), Square.sq(3, 4));
        cp.makeMove(Square.sq(6, 4), Square.sq(5, 4));
        cp.makeMove(Square.sq(7, 4), Square.sq(6, 4));
    }


}


