// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import static jminusminus.NPhysicalRegister.*;

/**
 * Implemets a naive register allocation method. Each interval is considered
 * live for the entire cfg. Intervals are assigned physical registers on a first
 * come basis. When we run out of registers, we reuse the ones already assigned
 * and spill.
 */

public class NNaiveRegisterAllocator extends NRegisterAllocator {

    /**
     * Construct a NNaiveRegisterAllocator.
     * 
     * @param cfg
     *            an instance of a control flow graph.
     */

    public NNaiveRegisterAllocator(NControlFlowGraph cfg) {
        super(cfg);
    }

    /**
     * Build intervals with (naive) register allocation information in them.
     */

    public void allocation() {
        // In this allocation scheme, each interval just has a single
        // range spanning the entire cfg.
        for (NInterval interval : cfg.intervals) {
            NBasicBlock lastBlock = cfg.basicBlocks
                    .get(cfg.basicBlocks.size() - 1);
            NLIRInstruction lastLir = lastBlock.lir
                    .get(lastBlock.lir.size() - 1);
            interval.ranges.add(new NRange(0, lastLir.id));
        }

        // Allocate any fixed registers (a0, ..., a3 and v0) that were
        // assigned during generation phase to the appropriate
        // interval.
        for (int i = 0; i < 32; i++) {
            if (cfg.registers.get(i) != null) {
                cfg.intervals.get(i).pRegister = (NPhysicalRegister) cfg.registers
                        .get(i);
            }
        }

        // Assign stack offset (relative to fp) for formal parameters
        // fourth and above, and stack offset (relative to sp) for
        // arguments fourth or above.
        for (NBasicBlock block : cfg.basicBlocks) {
            for (NLIRInstruction lir : block.lir) {
                if (lir instanceof NLIRLoadLocal) {
                    NLIRLoadLocal loadLocal = (NLIRLoadLocal) lir;
                    if (loadLocal.local >= 4) {
                        NInterval interval = cfg.intervals
                                .get(((NVirtualRegister) loadLocal.write)
                                        .number());
                        interval.spill = true;
                        interval.offset = loadLocal.local - 3;
                        interval.offsetFrom = OffsetFrom.FP;
                    }
                }
            }
        }

        // Allocate registers.
        Queue<NInterval> assigned = new LinkedList<NInterval>();
        for (int i = 32, j = 0; i < cfg.intervals.size(); i++) {
            NInterval interval = cfg.intervals.get(i);
            if (interval.pRegister == null) {
                if (j >= NPhysicalRegister.MAX_COUNT) {
                    // Pull out (from a queue) a register that's
                    // already assigned to another interval and
                    // re-assign it to this interval. But then
                    // we have a spill situation, so
                    // create an offset for the spill.
                    NInterval spilled = assigned.remove();
                    spilled.spill = true;
                    if (spilled.offset == -1) {
                        spilled.offset = cfg.offset++;
                        spilled.offsetFrom = OffsetFrom.SP;
                    }
                    interval.pRegister = spilled.pRegister;
                    interval.spill = true;
                    if (interval.offset == -1) {
                        interval.offset = cfg.offset++;
                        interval.offsetFrom = OffsetFrom.SP;
                    }
                } else {
                    // Allocate free register to interval.
                    NPhysicalRegister pRegister = NPhysicalRegister.regInfo[T0
                            + j++];
                    interval.pRegister = pRegister;
                    cfg.pRegisters.add(pRegister);
                }
                assigned.add(interval);
            }
        }

        // Make sure that inputs of LIR instructions are not all
        // assigned the
        // same register. Also, Handle spills, i.e., generate loads
        // and
        // stores where needed.
        for (int i = 1; i < cfg.basicBlocks.size(); i++) { // We
            // ignore
            // block B0
            NBasicBlock block = cfg.basicBlocks.get(i);
            ArrayList<NLIRInstruction> newLir = new ArrayList<NLIRInstruction>();
            for (NLIRInstruction lir : block.lir) {
                newLir.add(lir);
            }
            for (NLIRInstruction lir : block.lir) {
                int id = lir.id;

                if (lir.reads.size() == 2) {
                    NInterval input1 = cfg.intervals.get(
                            lir.reads.get(0).number()).childAt(id);
                    NInterval input2 = cfg.intervals.get(
                            lir.reads.get(1).number()).childAt(id);
                    if (input1.pRegister == input2.pRegister) {
                        input2.pRegister = NPhysicalRegister.regInfo[T0
                                + (input2.pRegister.number() + 1)
                                % NPhysicalRegister.MAX_COUNT];
                    }
                }

                // Loads.
                for (int j = 0; j < lir.reads.size(); j++) {
                    NInterval input = cfg.intervals.get(
                            lir.reads.get(j).number()).childAt(id);
                    if (input.spill) {
                        NLIRLoad load = new NLIRLoad(block, id
                                - lir.reads.size() + j, input.offset,
                                input.offsetFrom, input.pRegister);
                        newLir.add(newLir.indexOf(lir), load);
                    }
                }

                // Stores.
                if (lir.write != null) {
                    NInterval output = cfg.intervals.get(lir.write.number());
                    if (output.spill) {
                        NLIRStore store = new NLIRStore(block, id + 1,
                                output.offset, output.offsetFrom, lir.write);
                        newLir.add(newLir.indexOf(lir) + 1, store);
                    }
                }
            }
            block.lir = newLir;
        }
    }

}