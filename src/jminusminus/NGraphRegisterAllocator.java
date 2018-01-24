// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

/**
 * Implements register allocation using graph coloring algorithm.
 */

public class NGraphRegisterAllocator extends NRegisterAllocator {

    /**
     * Construct a NGraphRegisterAllocator.
     * 
     * @param cfg
     *            an instance of a control flow graph.
     */

    public NGraphRegisterAllocator(NControlFlowGraph cfg) {
        super(cfg);
    }

    /**
     * Build intervals with register allocation information in them.
     */

    public void allocation() {
        buildIntervals();
    }

}
