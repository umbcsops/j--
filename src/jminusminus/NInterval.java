// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static jminusminus.NPhysicalRegister.*;

/**
 * A lifetime interval, recording the interval of LIR code for which the
 * corresponding virtual register contains a useful value.
 */

class NInterval implements Comparable<NInterval> {

    /** Control flow graph instance. */
    private NControlFlowGraph cfg;

    /**
     * The virtual register id corresponding to the index in the array list of
     * NIntervals used by register allocation
     */
    public int vRegId;

    /** All live ranges for this virtual register */
    public ArrayList<NRange> ranges;

    /**
     * All use positions (in LIR) and their types for this virtual register
     */
    public TreeMap<Integer, InstructionType> usePositions;

    /**
     * The NPhyicalRegister assigned to this interval. If an interval ends up
     * needing more than one physical register it is split.
     */
    public NPhysicalRegister pRegister;

    /** Whether or not to spill. */
    public boolean spill;

    /** From offset. */
    public OffsetFrom offsetFrom;

    /** Offset. */
    public int offset;

    /** Parent of this interval. */
    public NInterval parent;

    /** Children of this interval. */
    public ArrayList<NInterval> children;

    /**
     * Construct a NInterval with the given virtual register ID for the given
     * control flow graph.
     * 
     * @param virtualRegID
     *            program counter.
     * @param cfg
     *            The control flow graph.
     */

    public NInterval(int virtualRegID, NControlFlowGraph cfg) {
        this.cfg = cfg;
        this.ranges = new ArrayList<NRange>();
        this.usePositions = new TreeMap<Integer, InstructionType>();
        this.vRegId = virtualRegID;
        this.children = new ArrayList<NInterval>();
        this.parent = null;
        spill = false;
        offset = -1;
    }

    /**
     * This second constructor is used in instantiating children of a split
     * interval.
     * 
     * @param virtualRegID
     *            program counter.
     * @param cfg
     *            The control flow graph.
     * @param childRanges
     *            The instruction ranges for this child.
     * @param parent
     *            The parent interval.
     */

    public NInterval(int virtualRegID, NControlFlowGraph cfg,
            ArrayList<NRange> childRanges, NInterval parent) {
        this.cfg = cfg;
        this.ranges = childRanges;
        this.usePositions = new TreeMap<Integer, InstructionType>();
        this.vRegId = virtualRegID;
        this.parent = parent;
        this.children = new ArrayList<NInterval>();
        spill = false;
        offset = -1;
    }

    /**
     * Add a new range to the existing ranges.
     * 
     * @param newNRange
     *            - the NRange to add
     */

    public void addOrExtendNRange(NRange newNRange) {
        if (!ranges.isEmpty()) {
            if (newNRange.stop + 5 == ranges.get(0).start
                    || newNRange.rangeOverlaps(ranges.get(0))) {
                ranges.get(0).start = newNRange.start;
            } else {
                ranges.add(0, newNRange);
            }
        } else {
            ranges.add(newNRange);
        }
    }

    /**
     * Looks for the very first position where an intersection with another
     * interval occurs.
     * 
     * NOTE: A.nextIntersection(B) equals B.nextIntersection(A)
     * 
     * @param otherInterval
     *            the interval to compare against for intersection.
     * @return the position where the intersection begins.
     */

    public int nextIntersection(NInterval otherInterval) {
        int a = -1, b = -2;
        for (NRange r : this.ranges) {
            if (otherInterval.isLiveAt(r.start)) {
                a = r.start;
                break;
            }
        }
        for (NRange r : otherInterval.ranges) {
            if (this.isLiveAt(r.start)) {
                b = r.start;
                break;
            }
        }
        if (a >= 0 && b >= 0) {
            return a <= b ? a : b;
        } else {
            return a > b ? a : b;
        }
    }

    /**
     * The next use position of this interval after the first range start of the
     * foreign interval. If there is no such use, then the first use position is
     * returned to preserve data flow (in case of loops).
     * 
     * @param currInterval
     *            the interval with starting point after which we want to find
     *            the next usage of this one.
     * 
     * @return the next use position.
     * 
     */

    public int nextUsageOverlapping(NInterval currInterval) {
        int psi = currInterval.firstRangeStart();

        if (usePositions.ceilingKey(psi) != null) {
            return usePositions.ceilingKey(psi);
        } else if (!usePositions.isEmpty()) {
            return usePositions.firstKey();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * The first use position in this interval.
     * 
     * @return the first use position.
     */

    public int firstUsage() {
        return usePositions.firstKey();
    }

    /**
     * Sets the start value of the very first range. Note: There will always be
     * at least one range before this method is used by buildIntervals.
     * 
     * @param newStart
     *            the value to which the first range's start will be set.
     */
    public void newFirstRangeStart(int newStart) {
        // Check
        if (!ranges.isEmpty()) {
            ranges.get(0).start = newStart;
        }
    }

    /**
     * Register a use (read or write)>
     * 
     * @param index
     *            the site of the use.
     * @param type
     *            the instruction type.
     */

    public void addUsePosition(Integer index, InstructionType type) {
        usePositions.put(index, type);
    }

    /**
     * Check if this vreg is alive at a given index.
     * 
     * @param atIndex
     *            the index at which to see if this register is live.
     */

    public boolean isLiveAt(int atIndex) {
        for (NRange r : ranges) {
            if (atIndex >= r.start && atIndex <= r.stop) {
                return true;
            }
        }
        return false;
    }

    /**
     * The range in this interval in which the LIR instruction with the given id
     * is live, or null. This will never return null if called for an interval
     * from the active list after it has been set up by the allocate method.
     * 
     * @param id
     *            LIR instruction id.
     * @return range in which LIR instruction with given id is live, or null.
     */

    private NRange liveRangeAt(int id) {
        for (NRange r : ranges) {
            if (id >= r.start && id <= r.stop) {
                return r;
            }
        }
        return null;
    }

    /**
     * Write the interval information to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeToStdOut(PrettyPrinter p) {
        if (cfg.registers.get(vRegId) != null) {
            String s = cfg.registers.get(vRegId).name() + ": ";
            for (NRange r : ranges) {
                s += r.toString() + " ";
            }
            if (pRegister != null) {
                s += "-> " + pRegister.name();
            } else {
                s += "-> None";
            }
            if (spill) {
                if (offsetFrom == OffsetFrom.FP) {
                    s += " [frame:" + offset + "]";
                } else {
                    s += " [stack:" + offset + "]";
                }
            }
            p.printf("%s\n", s);
            for (NInterval child : this.children) {
                child.writeToStdOut(p);
            }
        } else if (this.isChild()) {
            String s = "\tv" + this.vRegId + ": ";
            for (NRange r : ranges) {
                s += r.toString() + " ";
            }
            if (pRegister != null) {
                s += "-> " + pRegister.name();
            } else {
                s += "-> None";
            }
            if (offsetFrom == OffsetFrom.FP) {
                s += " [frame:" + offset + "]";
            } else {
                s += " [stack:" + offset + "]";
            }
            p.printf("%s\n", s);
            for (NInterval child : this.children) {
                child.writeToStdOut(p);
            }
        }
    }

    /**
     * The start position for the first range.
     * 
     * @return the start position.
     */

    public int firstRangeStart() {
        if (ranges.isEmpty())
            return -1;
        else
            return ranges.get(0).start;
    }

    /**
     * The stop position for the last range.
     * 
     * @return the stop position.
     */

    public int lastNRangeStop() {
        if (ranges.isEmpty())
            return -1;
        else
            return ranges.get(ranges.size() - 1).stop;
    }

    /**
     * Compare start positions (for ordering intervals).
     * 
     * @param other
     *            interval to compare to.
     * 
     * @return ordering value.
     */

    public int compareTo(NInterval other) {
        return this.firstRangeStart() - other.firstRangeStart();
    }

    /**
     * Two intervals are equal if they have the same virtual register ID.
     * 
     * @param other
     *            the interval we are comparing ourself with.
     * @return true if the two intervals are the same, false otherwise.
     */

    public boolean equals(NInterval other) {
        return (this.vRegId == other.vRegId);
    }

    /**
     * Split the current interval at the given index. Responsible for splitting
     * a range if the index falls on one, moving remaining ranges over to child,
     * and moving appropriate usePositions over to the child.
     * 
     * @param idx
     *            the index at which this interval is to be split
     * 
     * @return the child interval which is to be sorted onto unhandled. If there
     *         was no child created in the case of a pruning this interval is
     *         returned.
     */

    public NInterval splitAt(int idx) {

        ArrayList<NRange> childsRanges = new ArrayList<NRange>();
        if (this.isLiveAt(idx)) { // means split falls on a range
            // Assumptions: if a range is LIVE on an index, then there
            // exist usePositions at or before the index
            // within this same range.
            NRange liveRange = this.liveRangeAt(idx);
            int splitTo = idx;
            splitTo = usePositions.ceilingKey(idx);
            childsRanges.add((liveRange.splitRange(splitTo, idx - 5)));
        }

        // The following two for loops take care of any untouched ranges
        // which start after the split position and must be moved to the
        // child interval.
        for (NRange r : ranges) {
            if (r.start > idx) {
                childsRanges.add(r);
            }
        }
        for (NRange r : childsRanges) {
            ranges.remove(r);
        }

        NInterval child = new NInterval(cfg.maxIntervals++, cfg, childsRanges,
                this.getParent());
        cfg.registers.add(null); // expand size of cfg.registers to
        // avoid null pointer exception when printing.

        // transfer remaining use positions
        while (this.usePositions.ceilingKey(idx) != null)
            child.usePositions
                    .put(this.usePositions.ceilingKey(idx), this.usePositions
                            .remove(this.usePositions.ceilingKey(idx)));
        this.getParent().children.add(child);
        return child;
    }

    /**
     * The parent interval.
     * 
     * @return the parent interval.
     */

    private NInterval getParent() {
        if (parent != null)
            return parent;
        else
            return this;
    }

    /**
     * The child interval at a given instruction index.
     * 
     * @param idx
     *            The instruction index.
     * @return the child interval.
     */

    public NInterval childAt(int idx) {
        for (NInterval child : children) {
            if (child.isLiveAt(idx)) {
                return child;
            }
        }
        return this;
    }

    /**
     * A child of this interval which is live or ends before the given basic
     * block's end.
     * 
     * @param b
     *            the basic block.
     * @return the child of this interval which ends at or nearest (before) this
     *         basic block's end (last lir instruction index).
     */

    public NInterval childAtOrEndingBefore(NBasicBlock b) {
        int idx = b.getLastLIRInstId();
        for (NInterval child : children) {
            if (child.isLiveAt(idx))
                return child;
        }
        NInterval tmp = this;
        int highestEndingAllowed = b.getFirstLIRInstId();
        for (NInterval child : children) {
            // get the child which ends before idx but also ends closest to idx
            if (child.lastNRangeStop() < idx
                    && child.lastNRangeStop() > highestEndingAllowed) {
                tmp = child;
                highestEndingAllowed = tmp.lastNRangeStop();
            }
        }
        return tmp;
    }

    /**
     * The child of this interval which is live or starts after the given basic
     * block's start
     * 
     * @param b
     *            the basic block
     * @return the child of this interval which starts at or nearest (after)
     *         this basic block's start (fist lir instruction index).
     */

    public NInterval childAtOrStartingAfter(NBasicBlock b) {
        int idx = b.getFirstLIRInstId();
        for (NInterval child : children) {
            if (child.isLiveAt(idx))
                return child;
        }
        NInterval tmp = this;
        int lowestStartAllowed = b.getLastLIRInstId();// block's end
        for (NInterval child : children) {
            if (child.firstRangeStart() > idx
                    && child.firstRangeStart() < lowestStartAllowed) {
                tmp = child;
                lowestStartAllowed = tmp.firstRangeStart();
            }
        }
        return tmp;
    }

    /**
     * Returns the basic block in which this interval's start position falls.
     * 
     * @return basic block in which this interval's start position falls.
     */

    public int startsAtBlock() {
        for (NBasicBlock b : this.cfg.basicBlocks) {
            if (this.firstRangeStart() >= b.getFirstLIRInstId()
                    && this.firstRangeStart() <= b.getLastLIRInstId())
                return b.id;
        }
        return -1; // this will never happen
    }

    /**
     * The basic block in which this interval's end position falls.
     * 
     * @return the basic block number.
     */

    public int endsAtBlock() {
        for (NBasicBlock b : this.cfg.basicBlocks) {
            if (this.lastNRangeStop() >= b.getFirstLIRInstId()
                    && this.lastNRangeStop() <= b.getLastLIRInstId()) {
                return b.id;
            }
        }
        return -1; // this will never happen
    }

    /**
     * Assigns an offset to this interval (if one hasn't been already assigned).
     * Assigns that same offset to any (newly created) children.
     */

    public void spill() {
        this.spill = true;
        if (this.offset == -1) {
            this.offset = cfg.offset++;
            this.offsetFrom = OffsetFrom.SP;
        }
        for (NInterval child : children) {
            if (child.offset == -1) {
                child.offset = this.offset;
                child.offsetFrom = this.offsetFrom;
            }
        }
    }

    // The following two methods are used for insertion of move instructions
    // for spills.

    /**
     * Is this interval a child interval?
     * 
     * @return true or false.
     */

    public boolean isChild() {
        if (this.parent != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is this interval a parent interval? (Ie, does it have children?)
     * 
     * @return true or false.
     */

    public boolean isParent() {
        return !this.children.isEmpty();
    }

}

/** The types of stack pointers. **/
enum OffsetFrom {
    FP, SP
};

/** The types of possible uses. */
enum InstructionType {
    read, write
};

/**
 * A liveness range (for an interval).
 */

class NRange implements Comparable<NRange> {

    /** The range's start position. */
    public int start;

    /** The range's stop position. */
    public int stop;

    /**
     * Construct a liveness range extending from start to stop (positions in the
     * code).
     * 
     * @param start
     *            start position.
     * @param stop
     *            stop position.
     */
    public NRange(int start, int stop) {
        this.start = start;
        this.stop = stop;

    }

    /**
     * Mutates current range to be only as long as the split point and returns
     * the remainder as a new range.
     * 
     * @param newStart
     *            the split location
     * @param oldStop
     *            the split location
     * @return the new NRange which begins at the split position and runs to the
     *         end of this previously whole range.
     */

    public NRange splitRange(int newStart, int oldStop) {
        NRange newRange = new NRange(newStart, stop);
        this.stop = oldStop;

        return newRange;
    }

    /**
     * Does this liveness range overlap with another?
     * 
     * @param a
     *            The other range.
     * @return true or false.
     */

    public boolean rangeOverlaps(NRange a) {
        if (a.start < this.start) {
            if (a.stop <= this.start) {
                return false;
            } else {
                return true;
            }
        } else if (a.start < this.stop) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * One liveness range comes before another if its start position comes
     * before the other's start position.
     * 
     * @param other
     *            the other range.
     * 
     * @return comparison.
     */

    public int compareTo(NRange other) {
        return this.start - other.start;
    }

    /**
     * The string representation of the range.
     * 
     * @return "[start,stop]"
     */

    public String toString() {
        return "[" + start + ", " + stop + "]";
    }

}
