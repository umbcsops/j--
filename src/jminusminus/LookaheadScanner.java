// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.FileNotFoundException;
import java.util.Stack;
import java.util.Vector;

/**
 * A lexical analyzer for j-- that interfaces with the hand-written parser
 * (Parser.java). It provides a backtracking mechanism, and makes use of the
 * underlying hand-written Scanner.
 */

class LookaheadScanner {

    /** The underlying hand-written scanner. */
    private Scanner scanner;

    /** Backtracking queue. */
    private Vector<TokenInfo> backtrackingQueue;

    /** Token queue. */
    private Vector<TokenInfo> nextQueue;

    /** Stack of token queues for nested lookahead. */
    private Stack<Vector<TokenInfo>> queueStack;

    /** Whether we are looking ahead. */
    public boolean isLookingAhead;

    /** Previous token. */
    private TokenInfo previousToken;

    /** Current token. */
    private TokenInfo token;

    /**
     * Construct a LookaheadScanner from a file name.
     * 
     * @param fileName
     *            the name of the file containing the source.
     * @exception FileNotFoundException
     *                when the named file cannot be found.
     */

    public LookaheadScanner(String fileName) throws FileNotFoundException {
        scanner = new Scanner(fileName);
        backtrackingQueue = new Vector<TokenInfo>();
        nextQueue = new Vector<TokenInfo>();
        queueStack = new Stack<Vector<TokenInfo>>();
        isLookingAhead = false;
    }

    /**
     * Scan to the next token in the input.
     */

    public void next() {
        previousToken = token;
        if (backtrackingQueue.size() == 0) {
            token = scanner.getNextToken();
        } else {
            token = backtrackingQueue.remove(0);
        }
        if (isLookingAhead) {
            nextQueue.add(token);
        }
    }

    /**
     * Record the current position in the input, so that we can start looking
     * ahead in the input (and later return to this position). We'll queue up
     * the current and subsequent tokens until returnToPosition() is invoked.
     * These recordPosition's can be nested.
     */

    public void recordPosition() {
        isLookingAhead = true;
        queueStack.push(nextQueue);
        nextQueue = new Vector<TokenInfo>();
        nextQueue.add(previousToken);
        nextQueue.add(token);
    }

    /**
     * Return to the previously recorded position in the input stream of tokens.
     * If this is a nested lookahead, then return to the previous token queue.
     */

    public void returnToPosition() {
        while (backtrackingQueue.size() > 0) {
            nextQueue.add(backtrackingQueue.remove(0));
        }
        backtrackingQueue = nextQueue;
        nextQueue = queueStack.pop();
        isLookingAhead = !(queueStack.empty());

        // Restore previous and current tokens
        previousToken = backtrackingQueue.remove(0);
        token = backtrackingQueue.remove(0);
    }

    /**
     * The currently scanned token.
     * 
     * @return the current token.
     */

    public TokenInfo token() {
        return token;
    }

    /**
     * The previously scanned token. We use this in the parser to get at a
     * token's semantic info (for example an identifier's name), after we've
     * scanned it.
     * 
     * @return the previous token.
     */

    public TokenInfo previousToken() {
        return previousToken;
    }

    /**
     * Has an error occurred up to now in lexical analysis?
     * 
     * @return true or false.
     */

    public boolean errorHasOccured() {
        return scanner.errorHasOccurred();
    }

    /**
     * Return the name of the source file.
     * 
     * @return name of the source file.
     */

    public String fileName() {
        return scanner.fileName();
    }

}
