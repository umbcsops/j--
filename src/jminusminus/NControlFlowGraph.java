// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Queue;

/**
 * A tuple representation of a JVM instruction.
 */

class NTuple {

    /** Program counter of the instruction. */
    public int pc;

    /** Opcode of the instruction. */
    public int opcode;

    /** Operands of the instructions. */
    public ArrayList<Short> operands;

    /** String representation (mnemonic) of the instruction. */
    public String mnemonic;

    /** Is this tuple the leader of the block containing it. */
    public boolean isLeader;

    /**
     * Construct a tuple representing the JVM instruction with the given program
     * counter, opcode, and operand list.
     * 
     * @param pc
     *            program counter.
     * @param opcode
     *            opcode of the instruction.
     * @param operands
     *            list of operands of the instruction.
     */

    public NTuple(int pc, int opcode, ArrayList<Short> operands) {
        this.pc = pc;
        this.opcode = opcode;
        this.operands = operands;
        this.mnemonic = CLInstruction.instructionInfo[opcode].mnemonic;
        this.isLeader = false;
    }

    /**
     * Write the information pertaining to this tuple to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("%s: %s", pc, mnemonic);
        for (short s : operands) {
            p.printf(" %s", s);
        }
        p.printf("\n");
    }

}

/**
 * Representation of a block within a control flow graph.
 */

class NBasicBlock {

    /** The control flow graph (cfg) that this block belongs to. */
    public NControlFlowGraph cfg;

    /** Unique identifier of ths block. */
    public int id;

    /** List of tuples in this block. */
    public ArrayList<NTuple> tuples;

    /** List of predecessor blocks. */
    public ArrayList<NBasicBlock> predecessors;

    /** List of successor blocks. */
    public ArrayList<NBasicBlock> successors;

    /** List of high-level (HIR) instructions in this block. */
    public ArrayList<Integer> hir;

    /** List of low-level (LIR) instructions in this block. */
    public ArrayList<NLIRInstruction> lir;

    /**
     * The state array for this block that maps local variable index to the HIR
     * instruction that last affected it.
     */
    public int[] locals;

    /** Has this block been visited? */
    public boolean visited;

    /** Is this block active? */
    public boolean active;

    /** Is this block a loop head? */
    public boolean isLoopHead;

    /** Is this block a loop tail? */
    public boolean isLoopTail;

    /** Index of a loop. */
    public int loopIndex;

    /** Depth of a loop. */
    public int loopDepth;

    /** Number of forward branches to this block. */
    public int fwdBranches;

    /** Number of backward branches to this block. */
    public int bwdBranches;

    /** Ref count of this block. */
    public int ref;

    /** The dominator of this block. */
    public NBasicBlock dom;

    /** All virtual registers locally defined within this block. */
    public BitSet liveDef;

    /**
     * All virtual registers used before definition within this block.
     */
    public BitSet liveUse;

    /** All virtual registers live in the block. */
    public BitSet liveIn;

    /** All virtual registers live outside the block. */
    public BitSet liveOut;

    /**
     * Construct a block given its unique identifier.
     * 
     * @param cfg
     *            the cfg containing this block.
     * @param id
     *            id of the block.
     */

    public NBasicBlock(NControlFlowGraph cfg, int id) {
        this.cfg = cfg;
        this.id = id;
        this.tuples = new ArrayList<NTuple>();
        this.predecessors = new ArrayList<NBasicBlock>();
        this.successors = new ArrayList<NBasicBlock>();
        this.hir = new ArrayList<Integer>();
        this.lir = new ArrayList<NLIRInstruction>();
        this.isLoopHead = false;
    }

    /**
     * Return a string identifier of this block.
     * 
     * @return string identifier of this block.
     */

    public String id() {
        return "B" + id;
    }

    /**
     * Is this block the same as the other block? Two blocks are the same if
     * their ids are the same.
     * 
     * @param other
     *            the other block.
     * @return true or false.
     */

    public boolean equals(NBasicBlock other) {
        return this.id == other.id;
    }

    /**
     * Return a string representation of this block.
     * 
     * @return string representation of this block.
     */

    public String toString() {
        return "[B" + id + "]";
    }

    /**
     * Write the tuples in this block to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeTuplesToStdOut(PrettyPrinter p) {
        String s = id();
        p.printf("%s\n", s);
        for (NTuple tuple : tuples) {
            tuple.writeToStdOut(p);
        }
        p.printf("\n");
    }

    /**
     * Write the HIR instructions in this block to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeHirToStdOut(PrettyPrinter p) {
        String s = id() + (isLoopHead ? " [LH]" : "")
                + (isLoopTail ? " [LT]" : "");
        if (tuples.size() > 0) {
            s += " [" + tuples.get(0).pc + ", "
                    + tuples.get(tuples.size() - 1).pc + "]";
        }
        if (dom != null) {
            s += " dom: " + dom.id();
        }
        if (predecessors.size() > 0) {
            s += " pred: ";
            for (NBasicBlock block : predecessors) {
                s += block.id() + " ";
            }
        }
        if (successors.size() > 0) {
            s += " succ: ";
            for (NBasicBlock block : successors) {
                s += block.id() + " ";
            }
        }
        p.printf(s + "\n");
        s = "Locals: ";
        if (locals != null) {
            for (int i = 0; i < locals.length; i++) {
                if (!(cfg.hirMap.get(locals[i]) instanceof NHIRLocal)) {
                    s += cfg.hirMap.get(locals[i]).id() + " ";
                }
            }
        }
        p.printf("%s\n", s);
        for (int ins : hir) {
            if (cfg.hirMap.get(ins) instanceof NHIRPhiFunction) {
                p.printf("%s: %s\n", ((NHIRPhiFunction) cfg.hirMap.get(ins))
                        .id(), ((NHIRPhiFunction) cfg.hirMap.get(ins)));
            }
        }
        for (int ins : hir) {
            if (!(cfg.hirMap.get(ins) instanceof NHIRPhiFunction)) {
                p.printf("%s\n", cfg.hirMap.get(ins));
            }
        }
        p.printf("\n");
    }

    /**
     * Write the LIR instructions in this block to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */
    public void writeLirToStdOut(PrettyPrinter p) {
        p.printf("%s\n", id());
        for (NLIRInstruction ins : lir) {
            p.printf("%s\n", ins);
        }
        p.printf("\n");
    }

    /**
     * The instruction identifier for the first LIR instruction.
     * 
     * @return the instruction identifier.
     */
    public int getFirstLIRInstId() {
        if (lir.isEmpty()) {
            return -1;
        }
        return lir.get(0).id;
    }

    /**
     * The instruction identifier for the last LIR instruction.
     * 
     * @return the instruction identifier.
     */
    public int getLastLIRInstId() {
        if (lir.isEmpty()) {
            return -1;
        }
        return lir.get(lir.size() - 1).id;
    }

    /**
     * Iterates through the lir array of this block, returning an
     * NLIRInstruction with the specified id.
     * 
     * @param id
     *            the id to look for.
     * 
     * @return NLIRInstruction with the specified id, null if none matched.
     */
    public NLIRInstruction getInstruction(int id) {
        for (NLIRInstruction i : this.lir) {
            if (i.id == id) {
                return i;
            }
        }
        return null;
    }

    /**
     * Checks to see if there is an LIRInstruction with this id in the block's
     * lir.
     * 
     * @return true or false.
     */
    public boolean idIsFree(int id) {
        if (this.getInstruction(id) != null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Inserts an NLIRInstruction to the appropriate place in this block's lir
     * array based on its id -- preserving order by id.
     * 
     * @param inst
     *            the NLIRInstruction to be inserted.
     */
    public void insertLIRInst(NLIRInstruction inst) {
        int idx = -1;
        for (int i = 0; i < this.lir.size(); i++) {
            if (this.lir.get(i).id < inst.id) {
                idx = i;
            }
        }
        if (++idx == this.lir.size()) {
            this.lir.add(inst);
        } else {
            this.lir.add(idx, inst);
        }
    }

}

/**
 * Representation of a control flow graph (cfg) for a method.
 */

class NControlFlowGraph {

    /** Constant pool for the class containing the method. */
    private CLConstantPool cp;

    /** Contains information about the method. */
    private CLMethodInfo m;

    /** Maps the pc of a JVM instruction to the block it's in. */
    private HashMap<Integer, NBasicBlock> pcToBasicBlock;

    /** block identifier. */
    public static int blockId;

    /** HIR instruction identifier. */
    public static int hirId;

    /** HIR instruction identifier. */
    public static int lirId;

    /** Virtual register identifier. */
    public static int regId;

    /** Stack offset counter.. */
    public int offset;

    /** Loop identifier. */
    public static int loopIndex;

    /** Name of the method this cfg corresponds to. */
    public String name;

    /** Descriptor of the method this cfg corresponds to. */
    public String desc;

    /**
     * List of blocks forming the cfg for the method.
     */
    public ArrayList<NBasicBlock> basicBlocks;

    /** Maps HIR instruction ids in this cfg to HIR instructions. */
    public TreeMap<Integer, NHIRInstruction> hirMap;

    /**
     * Registers allocated for this cfg by the HIR to LIR conversion algorithm.
     */
    public ArrayList<NRegister> registers;

    /**
     * The total number of intervals. This is used to name split children and
     * grows as more intervals are created by spills.
     */
    public int maxIntervals;

    /**
     * Physical registers allocated for this cfg by the HIR to LIR conversion
     * algorithm.
     */
    public ArrayList<NPhysicalRegister> pRegisters;

    /**
     * Intervals allocated by the register allocation algorithm.
     */
    public ArrayList<NInterval> intervals;

    /** Used to construct jump labels in spim output. */
    public String labelPrefix;

    /**
     * SPIM code for string literals added to the data segment.
     */
    public ArrayList<String> data;

    /**
     * Construct an NControlFlowGraph object for a method given the constant
     * pool for the class containing the method and the object containing
     * information about the method.
     * 
     * @param cp
     *            constant pool for the class containing the method.
     * @param m
     *            contains information about the method.
     */

    public NControlFlowGraph(CLConstantPool cp, CLMethodInfo m) {
        this.cp = cp;
        this.m = m;
        name = new String(((CLConstantUtf8Info) cp.cpItem(m.nameIndex)).b);
        desc = new String(((CLConstantUtf8Info) cp.cpItem(m.descriptorIndex)).b);
        basicBlocks = new ArrayList<NBasicBlock>();
        pcToBasicBlock = new HashMap<Integer, NBasicBlock>();
        ArrayList<Integer> code = getByteCode();
        ArrayList<NTuple> tuples = bytecodeToTuples(code);
        if (tuples.size() == 0) {
            return;
        }
        NTuple[] tupleAt = new NTuple[code.size()];
        for (NTuple tuple : tuples) {
            tupleAt[tuple.pc] = tuple;
        }

        // Identify the leaders.
        tuples.get(0).isLeader = true;
        for (int j = 1; j < tuples.size(); j++) {
            NTuple tuple = tuples.get(j);
            boolean jumpInstruction = true;
            short operandByte1, operandByte2, operandByte3, operandByte4;
            int offset;
            switch (tuple.opcode) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case GOTO:
            case JSR:
            case IFNULL:
            case IFNONNULL:
                operandByte1 = tuple.operands.get(0);
                operandByte2 = tuple.operands.get(1);
                offset = shortValue(operandByte1, operandByte2);
                tupleAt[tuple.pc + offset].isLeader = true;
                break;
            case GOTO_W:
            case JSR_W:
                operandByte1 = tuple.operands.get(0);
                operandByte2 = tuple.operands.get(1);
                operandByte3 = tuple.operands.get(2);
                operandByte4 = tuple.operands.get(3);
                offset = intValue(operandByte1, operandByte2, operandByte3,
                        operandByte4);
                tupleAt[tuple.pc + offset].isLeader = true;
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case RET:
            case ATHROW:
                break;
            case TABLESWITCH: // TBD
                break;
            case LOOKUPSWITCH: // TBD
                break;
            default:
                jumpInstruction = false;
            }
            if (jumpInstruction) {
                if (j < tuples.size() - 1) {
                    tuples.get(j + 1).isLeader = true;
                }
            }
        }

        // Form blocks.
        {
            blockId = 0;
            NBasicBlock block = new NBasicBlock(this, blockId++);
            for (NTuple tuple : tuples) {
                if (tuple.isLeader) {
                    basicBlocks.add(block);
                    block = new NBasicBlock(this, blockId++);
                    if (!pcToBasicBlock.containsKey(tuple.pc)) {
                        pcToBasicBlock.put(tuple.pc, block);
                    }
                }
                block.tuples.add(tuple);
            }
            basicBlocks.add(block);
        }

        // Connect up the blocks for this method, that is, build
        // its control flow graph.
        basicBlocks.get(0).successors.add(basicBlocks.get(1));
        basicBlocks.get(1).predecessors.add(basicBlocks.get(0));
        NBasicBlock[] blockAt = new NBasicBlock[code.size()];
        for (NBasicBlock block : basicBlocks) {
            if (block.tuples.size() == 0) {
                continue;
            }
            blockAt[block.tuples.get(0).pc] = block;
        }
        for (int j = 0; j < basicBlocks.size(); j++) {
            NBasicBlock block = basicBlocks.get(j);
            if (block.tuples.size() == 0) {
                continue;
            }
            NTuple tuple = block.tuples.get(block.tuples.size() - 1);
            short operandByte1, operandByte2, operandByte3, operandByte4;
            int offset;
            NBasicBlock target;
            switch (tuple.opcode) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case IFNULL:
            case IFNONNULL:
                operandByte1 = tuple.operands.get(0);
                operandByte2 = tuple.operands.get(1);
                offset = shortValue(operandByte1, operandByte2);
                target = blockAt[tuple.pc + offset];
                if (j < basicBlocks.size() - 1) {
                    block.successors.add(basicBlocks.get(j + 1));
                    basicBlocks.get(j + 1).predecessors.add(block);
                }
                block.successors.add(target);
                target.predecessors.add(block);
                break;
            case GOTO:
            case JSR:
                operandByte1 = tuple.operands.get(0);
                operandByte2 = tuple.operands.get(1);
                offset = shortValue(operandByte1, operandByte2);
                target = blockAt[tuple.pc + offset];
                block.successors.add(target);
                target.predecessors.add(block);
                break;
            case GOTO_W:
            case JSR_W:
                operandByte1 = tuple.operands.get(0);
                operandByte2 = tuple.operands.get(1);
                operandByte3 = tuple.operands.get(2);
                operandByte4 = tuple.operands.get(3);
                offset = intValue(operandByte1, operandByte2, operandByte3,
                        operandByte4);
                target = blockAt[tuple.pc + offset];
                block.successors.add(target);
                target.predecessors.add(block);
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case RET:
            case ATHROW:
                break;
            case TABLESWITCH: // TBD
                break;
            case LOOKUPSWITCH: // TBD
                break;
            default:
                if (j < basicBlocks.size() - 1) {
                    block.successors.add(basicBlocks.get(j + 1));
                    basicBlocks.get(j + 1).predecessors.add(block);
                }
            }
        }

        // Calculate the ref count and number of forward branches
        // to each block in the this cfg.
        for (NBasicBlock block : basicBlocks) {
            block.ref = block.predecessors.size();
            block.fwdBranches = block.predecessors.size() - block.bwdBranches;
        }
    }

    /**
     * Implements loop detection algorithm to figure out if the specified block
     * is a loop head or a loop tail. Also calculates the number of backward
     * branches to the block.
     * 
     * @param block
     *            a block.
     * @param pred
     *            block's predecessor or null.
     */

    public void detectLoops(NBasicBlock block, NBasicBlock pred) {
        if (!block.visited) {
            block.visited = true;
            block.active = true;
            for (NBasicBlock succ : block.successors) {
                detectLoops(succ, block);
            }
            block.active = false;
        } else if (block.active) {
            block.isLoopHead = true;
            pred.isLoopTail = true;
            block.bwdBranches++;
            block.loopIndex = NControlFlowGraph.loopIndex++;
        }
    }

    /**
     * Remove blocks that cannot be reached from the begin block (B0). Also
     * removes these blocks from the predecessor lists.
     */

    public void removeUnreachableBlocks() {
        // Create a list of blocks that cannot be reached.
        ArrayList<NBasicBlock> toRemove = new ArrayList<NBasicBlock>();
        for (NBasicBlock block : basicBlocks) {
            if (!block.visited) {
                toRemove.add(block);
            }
        }

        // From the predecessor list for each blocks, remove
        // the ones that are in toRemove list.
        for (NBasicBlock block : basicBlocks) {
            for (NBasicBlock pred : toRemove) {
                block.predecessors.remove(pred);
            }
        }

        // From the list of all blocks, remove the ones that
        // are in toRemove list.
        for (NBasicBlock block : toRemove) {
            basicBlocks.remove(block);
        }
    }

    /**
     * Compute the dominator of each block in this cfg recursively given the
     * starting block and its predecessor.
     * 
     * @param block
     *            starting block.
     * @param pred
     *            block's predecessor.
     */

    public void computeDominators(NBasicBlock block, NBasicBlock pred) {
        if (block.ref > 0) {
            block.ref--;
        }
        if (block.dom == null) {
            block.dom = pred;
        } else {
            block.dom = commonDom(block.dom, pred);
        }
        if (block.ref == block.bwdBranches) {
            for (NBasicBlock s : block.successors) {
                computeDominators(s, block);
            }
        }
    }

    /**
     * Convert tuples in each block to their high-level (HIR) representations.
     */

    public void tuplesToHir() {
        clearBlockVisitations();
        hirId = 0;
        loopIndex = 0;
        hirMap = new TreeMap<Integer, NHIRInstruction>();
        int numLocals = numLocals();
        int[] locals = new int[numLocals];
        ArrayList<String> argTypes = argumentTypes(desc);
        NBasicBlock beginBlock = basicBlocks.get(0);
        for (int i = 0; i < locals.length; i++) {
            NHIRInstruction ins = null;
            if (i < argTypes.size()) {
                String lType = argTypes.get(i);
                ins = new NHIRLoadLocal(beginBlock, hirId++, i,
                        shortType(lType), lType);
                beginBlock.hir.add(ins.id);
            } else {
                ins = new NHIRLocal(beginBlock, hirId++, i, "", "");
            }
            beginBlock.cfg.hirMap.put(ins.id, ins);
            locals[i] = ins.id;
        }
        beginBlock.locals = locals;
        Stack<Integer> operandStack = new Stack<Integer>();
        Queue<NBasicBlock> q = new LinkedList<NBasicBlock>();
        beginBlock.visited = true;
        q.add(beginBlock);
        while (!q.isEmpty()) {
            NBasicBlock block = q.remove();
            for (NBasicBlock succ : block.successors) {
                if (!succ.visited) {
                    succ.visited = true;
                    q.add(succ);
                }
            }

            // Convert tuples in block to HIR instructions.
            if (block.predecessors.size() == 1) {
                block.locals = block.predecessors.get(0).locals.clone();
            } else if (block.predecessors.size() > 1) {
                if (block.isLoopHead) {
                    for (NBasicBlock pred : block.predecessors) {
                        if (pred.locals != null) {
                            block.locals = pred.locals.clone();
                            break;
                        }
                    }
                    for (int i = 0; i < block.locals.length; i++) {
                        ArrayList<Integer> args = new ArrayList<Integer>();
                        NHIRPhiFunction phi = new NHIRPhiFunction(block,
                                hirId++, args, i);
                        block.hir.add(phi.id);
                        block.cfg.hirMap.put(phi.id, phi);
                        phi.arguments.add(block.locals[i]);
                        for (int j = 1; j < block.predecessors.size(); j++) {
                            phi.arguments.add(phi.id);
                        }
                        block.locals[i] = phi.id;
                        phi.inferType();
                    }
                } else {
                    block.locals = block.predecessors.get(0).locals.clone();
                    for (int i = 1; i < block.predecessors.size(); i++) {
                        NBasicBlock pred = block.predecessors.get(i);
                        mergeLocals(block, pred);
                    }
                }
            }
            for (NTuple tuple : block.tuples) {
                CLInsInfo insInfo = CLInstruction.instructionInfo[tuple.opcode];
                int localVariableIndex = insInfo.localVariableIndex;
                NHIRInstruction ins = null;
                short operandByte1 = 0, operandByte2 = 0, operandByte3 = 0, offset = 0;
                int operand1 = 0, operand2 = 0, operand3 = 0;
                switch (insInfo.opcode) {
                case MULTIANEWARRAY: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    operandByte3 = tuple.operands.get(2);
                    int index = shortValue(operandByte1, operandByte2);
                    int classIndex = ((CLConstantClassInfo) cp.cpItem(index)).nameIndex;
                    String type = new String(((CLConstantUtf8Info) cp
                            .cpItem(classIndex)).b);
                    ins = new NHIRNewArray(block, hirId++, insInfo.opcode,
                            (int) operandByte3, shortType(type), type);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case AALOAD: {
                    operand2 = operandStack.pop();
                    operand1 = operandStack.pop();

                    // Compute base address.
                    NHIRInstruction ins1 = new NHIRIntConstant(block, hirId++,
                            12);
                    NHIRInstruction ins2 = new NHIRArithmetic(block, hirId++,
                            IADD, operand1, ins1.id);
                    block.cfg.hirMap.put(ins1.id, ins1);
                    block.hir.add(ins1.id);
                    block.cfg.hirMap.put(ins2.id, ins2);
                    block.hir.add(ins2.id);

                    // Compute index.
                    NHIRInstruction ins3 = new NHIRIntConstant(block, hirId++,
                            4);
                    NHIRInstruction ins4 = new NHIRArithmetic(block, hirId++,
                            IMUL, operand2, ins3.id);
                    block.cfg.hirMap.put(ins3.id, ins3);
                    block.hir.add(ins3.id);
                    block.cfg.hirMap.put(ins4.id, ins4);
                    block.hir.add(ins4.id);

                    ins = new NHIRALoad(block, hirId++, insInfo.opcode,
                            ins2.id, ins4.id, "L", "L");
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case IALOAD: {
                    operand2 = operandStack.pop();
                    operand1 = operandStack.pop();

                    // Compute base address.
                    NHIRInstruction ins1 = new NHIRIntConstant(block, hirId++,
                            12);
                    NHIRInstruction ins2 = new NHIRArithmetic(block, hirId++,
                            IADD, operand1, ins1.id);
                    block.cfg.hirMap.put(ins1.id, ins1);
                    block.hir.add(ins1.id);
                    block.cfg.hirMap.put(ins2.id, ins2);
                    block.hir.add(ins2.id);

                    // Compute index.
                    NHIRInstruction ins3 = new NHIRIntConstant(block, hirId++,
                            4);
                    NHIRInstruction ins4 = new NHIRArithmetic(block, hirId++,
                            IMUL, operand2, ins3.id);
                    block.cfg.hirMap.put(ins3.id, ins3);
                    block.hir.add(ins3.id);
                    block.cfg.hirMap.put(ins4.id, ins4);
                    block.hir.add(ins4.id);

                    ins = new NHIRALoad(block, hirId++, insInfo.opcode,
                            ins2.id, ins4.id, "I", "I");
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case IASTORE: {
                    operand3 = operandStack.pop();
                    operand2 = operandStack.pop();
                    operand1 = operandStack.pop();

                    // Compute base address.
                    NHIRInstruction ins1 = new NHIRIntConstant(block, hirId++,
                            12);
                    NHIRInstruction ins2 = new NHIRArithmetic(block, hirId++,
                            IADD, operand1, ins1.id);
                    block.cfg.hirMap.put(ins1.id, ins1);
                    block.hir.add(ins1.id);
                    block.cfg.hirMap.put(ins2.id, ins2);
                    block.hir.add(ins2.id);

                    // Compute index.
                    NHIRInstruction ins3 = new NHIRIntConstant(block, hirId++,
                            4);
                    NHIRInstruction ins4 = new NHIRArithmetic(block, hirId++,
                            IMUL, operand2, ins3.id);
                    block.cfg.hirMap.put(ins3.id, ins3);
                    block.hir.add(ins3.id);
                    block.cfg.hirMap.put(ins4.id, ins4);
                    block.hir.add(ins4.id);

                    ins = new NHIRAStore(block, hirId++, insInfo.opcode,
                            ins2.id, ins4.id, operand3, "I", "I");
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5: {
                    ins = new NHIRIntConstant(block, hirId++, tuple.opcode - 3);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case ILOAD: {
                    operandByte1 = tuple.operands.get(0);
                    localVariableIndex = operandByte1;
                    operandStack.push(block.locals[localVariableIndex]);
                    break;
                }
                case ILOAD_0:
                case ILOAD_1:
                case ILOAD_2:
                case ILOAD_3:
                case ALOAD_0:
                case ALOAD_1:
                case ALOAD_2:
                case ALOAD_3: {
                    operandStack.push(block.locals[localVariableIndex]);
                    break;
                }
                case ISTORE: {
                    operandByte1 = tuple.operands.get(0);
                    localVariableIndex = operandByte1;
                    block.locals[localVariableIndex] = operandStack.pop();
                    break;
                }
                case ISTORE_0:
                case ISTORE_1:
                case ISTORE_2:
                case ISTORE_3:
                case ASTORE_0:
                case ASTORE_1:
                case ASTORE_2:
                case ASTORE_3: {
                    block.locals[localVariableIndex] = operandStack.pop();
                    break;
                }
                case BIPUSH: {
                    operandByte1 = tuple.operands.get(0);
                    ins = new NHIRIntConstant(block, hirId++, operandByte1);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case SIPUSH: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    ins = new NHIRIntConstant(block, hirId++, shortValue(
                            operandByte1, operandByte2));
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case LDC: {
                    operandByte1 = tuple.operands.get(0);

                    // Only allowing ldc of string constants for
                    // now.
                    int stringIndex = ((CLConstantStringInfo) cp
                            .cpItem(operandByte1)).stringIndex;
                    String s = new String(((CLConstantUtf8Info) cp
                            .cpItem(stringIndex)).b);
                    ins = new NHIRStringConstant(block, hirId++, s);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case IADD:
                case ISUB:
                case IMUL: {
                    operand2 = operandStack.pop();
                    operand1 = operandStack.pop();
                    ins = new NHIRArithmetic(block, hirId++, insInfo.opcode,
                            operand1, operand2);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    operandStack.push(ins.id);
                    break;
                }
                case IINC: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    operand1 = block.locals[operandByte1];
                    NHIRInstruction ins1 = new NHIRIntConstant(block, hirId++,
                            (byte) operandByte2);
                    ins = new NHIRArithmetic(block, hirId++, IADD, operand1,
                            ins1.id);
                    block.locals[operandByte1] = ins.id;
                    block.hir.add(ins1.id);
                    block.cfg.hirMap.put(ins1.id, ins1);
                    block.hir.add(ins.id);
                    block.cfg.hirMap.put(ins.id, ins);
                    break;
                }
                case IF_ICMPNE:
                case IF_ICMPGT:
                case IF_ICMPLE: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    offset = shortValue(operandByte1, operandByte2);
                    int rhs = operandStack.pop();
                    int lhs = operandStack.pop();
                    NBasicBlock trueDestination = pcToBasicBlock.get(tuple.pc
                            + offset);
                    NBasicBlock falseDestination = pcToBasicBlock
                            .get(tuple.pc + 3);
                    ins = new NHIRConditionalJump(block, hirId++, lhs, rhs,
                            insInfo.opcode, trueDestination, falseDestination);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case GOTO: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    offset = shortValue(operandByte1, operandByte2);
                    NBasicBlock destination = pcToBasicBlock.get(tuple.pc
                            + offset);
                    ins = new NHIRGoto(block, hirId++, destination);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case GETSTATIC:
                case PUTSTATIC: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    int index = shortValue(operandByte1, operandByte2);
                    int classIndex = ((CLConstantFieldRefInfo) cp.cpItem(index)).classIndex;
                    int nameAndTypeIndex = ((CLConstantFieldRefInfo) cp
                            .cpItem(index)).nameAndTypeIndex;
                    int nameIndex = ((CLConstantClassInfo) cp
                            .cpItem(classIndex)).nameIndex;
                    String target = new String(((CLConstantUtf8Info) cp
                            .cpItem(nameIndex)).b);
                    int fieldNameIndex = ((CLConstantNameAndTypeInfo) cp
                            .cpItem(nameAndTypeIndex)).nameIndex;
                    int fieldDescIndex = ((CLConstantNameAndTypeInfo) cp
                            .cpItem(nameAndTypeIndex)).descriptorIndex;
                    String name = new String(((CLConstantUtf8Info) cp
                            .cpItem(fieldNameIndex)).b);
                    String desc = new String(((CLConstantUtf8Info) cp
                            .cpItem(fieldDescIndex)).b);
                    if (insInfo.opcode == PUTSTATIC) {
                        ins = new NHIRPutField(block, hirId++, insInfo.opcode,
                                target, name, shortType(desc), desc,
                                operandStack.pop());
                    } else {
                        ins = new NHIRGetField(block, hirId++, insInfo.opcode,
                                target, name, shortType(desc), desc);
                        operandStack.push(ins.id);
                    }
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case INVOKESPECIAL:
                case INVOKESTATIC: {
                    operandByte1 = tuple.operands.get(0);
                    operandByte2 = tuple.operands.get(1);
                    int index = shortValue(operandByte1, operandByte2);
                    int classIndex = ((CLConstantMethodRefInfo) cp
                            .cpItem(index)).classIndex;
                    int nameAndTypeIndex = ((CLConstantMethodRefInfo) cp
                            .cpItem(index)).nameAndTypeIndex;
                    int nameIndex = ((CLConstantClassInfo) cp
                            .cpItem(classIndex)).nameIndex;
                    String target = new String(((CLConstantUtf8Info) cp
                            .cpItem(nameIndex)).b);
                    int methodNameIndex = ((CLConstantNameAndTypeInfo) cp
                            .cpItem(nameAndTypeIndex)).nameIndex;
                    int methodDescIndex = ((CLConstantNameAndTypeInfo) cp
                            .cpItem(nameAndTypeIndex)).descriptorIndex;
                    String name = new String(((CLConstantUtf8Info) cp
                            .cpItem(methodNameIndex)).b);
                    String desc = new String(((CLConstantUtf8Info) cp
                            .cpItem(methodDescIndex)).b);
                    ArrayList<Integer> args = new ArrayList<Integer>();
                    int numArgs = argumentCount(desc);
                    for (int i = 0; i < numArgs; i++) {
                        int arg = operandStack.pop();
                        args.add(0, arg);
                    }
                    String returnType = returnType(desc);
                    ins = new NHIRInvoke(block, hirId++, insInfo.opcode,
                            target, name, args, shortType(returnType),
                            returnType);
                    if (!returnType.equals("V")) {
                        operandStack.push(ins.id);
                    }
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case IRETURN:
                case ARETURN: {
                    ins = new NHIRReturn(block, hirId++, insInfo.opcode,
                            operandStack.pop());
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                case RETURN: {
                    ins = new NHIRReturn(block, hirId++, insInfo.opcode, -1);
                    block.cfg.hirMap.put(ins.id, ins);
                    block.hir.add(ins.id);
                    break;
                }
                }
            }
        }
    }

    /**
     * Carry out optimizations on the high-level instructions.
     */

    public void optimize() {
        // TBD
    }

    /**
     * Eliminate redundant phi functions of the form x = (y, x, x, ..., x) with
     * y.
     */

    public void eliminateRedundantPhiFunctions() {
        for (int ins : hirMap.keySet()) {
            NHIRInstruction hir = hirMap.get(ins);
            if (hir instanceof NHIRPhiFunction) {
                NHIRPhiFunction phi = (NHIRPhiFunction) hir;
                int firstArg = phi.arguments.get(0);
                boolean match = true;
                NBasicBlock block = phi.block;
                if (!block.isLoopHead) {
                    continue;
                }
                for (int i = 1; i < phi.arguments.size(); i++) {
                    if (phi.arguments.get(i) != block.predecessors.get(i).locals[phi.local]) {
                        match = false;
                        phi.arguments.set(i,
                                block.predecessors.get(i).locals[phi.local]);
                    }
                }
                if (match && firstArg != phi.id) {
                    hirMap.put(phi.id, hirMap.get(firstArg));
                    phi.block.hir.remove((Integer) phi.id);
                }
            }
        }
    }

    /**
     * Convert the hir instructions in this cfg to lir instructions.
     */

    public void hirToLir() {
        lirId = 0;
        regId = 32;
        offset = 0;
        registers = new ArrayList<NRegister>();
        data = new ArrayList<String>();
        for (int i = 0; i < 32; i++) {
            registers.add(null);
        }
        pRegisters = new ArrayList<NPhysicalRegister>();
        for (int ins : hirMap.keySet()) {
            hirMap.get(ins).toLir();
        }

        // We now know how many virtual registers are needed, so
        // we can initialize bitset fields in each block that are
        // needed for interval calculation.
        int size = registers.size();
        for (NBasicBlock block : basicBlocks) {
            block.liveDef = new BitSet(size);
            block.liveUse = new BitSet(size);
            block.liveIn = new BitSet(size);
            block.liveOut = new BitSet(size);
        }
    }

    /**
     * Resolve the phi functions in this cfg, i.e., for each x = phi(x1, x2,
     * ..., xn) generate an (LIR) move xi, x instruction at the end of the
     * predecessor i of thte block defining the phi function; if the instruction
     * there is a branch, add the instruction prior to the branch.
     */

    public void resolvePhiFunctions() {
        for (int ins1 : hirMap.keySet()) {
            NHIRInstruction hir = hirMap.get(ins1);
            if (hir instanceof NHIRPhiFunction) {
                NHIRPhiFunction phi = (NHIRPhiFunction) hir;
                NBasicBlock block = phi.block;
                for (int i = 0; i < phi.arguments.size(); i++) {
                    NHIRInstruction arg = hirMap.get(phi.arguments.get(i));
                    if (arg.sType.equals("")) {
                        continue;
                    }
                    NBasicBlock targetBlock = block.predecessors.get(i);
                    NLIRMove move = new NLIRMove(arg.block, lirId++, arg.lir,
                            phi.lir);
                    int len = targetBlock.hir.size();
                    if (hirMap.get(targetBlock.hir.get(len - 1)) instanceof NHIRGoto
                            || hirMap.get(targetBlock.hir.get(len - 1)) instanceof NHIRConditionalJump) {
                        targetBlock.lir.add(len - 1, move);
                    } else {
                        targetBlock.lir.add(move);
                    }
                }
            }
        }
    }

    /**
     * Compute optimal ordering of the basic blocks in this cfg.
     */

    public void orderBlocks() {
        // TBD
    }

    /**
     * The basic block at a particular instruction id.
     * 
     * @param id
     *            the (LIR) instruction id.
     * @return the basic block.
     */

    public NBasicBlock blockAt(int id) {
        for (NBasicBlock b : this.basicBlocks) {
            if (b.getFirstLIRInstId() <= id && b.getLastLIRInstId() >= id)
                return b;
        }
        return null;
    }

    /**
     * Assign new ids to the LIR instructions in this cfg.
     */

    public void renumberLirInstructions() {
        int nextId = 0;
        for (NBasicBlock block : basicBlocks) {
            ArrayList<NLIRInstruction> newLir = new ArrayList<NLIRInstruction>();
            for (NLIRInstruction lir : block.lir) {
                if (lir instanceof NLIRLoadLocal
                        && ((NLIRLoadLocal) lir).local < 4) {
                    // Ignore first four formals.
                    continue;
                }
                lir.id = nextId;
                nextId += 5; // an extra slot for spills though we
                // don't use it
                newLir.add(lir);
            }
            block.lir = newLir;
        }
    }

    /**
     * Replace references to virtual registers in LIR instructions with
     * references to physical registers.
     */

    public void allocatePhysicalRegisters() {
        for (NBasicBlock block : basicBlocks) {
            for (NLIRInstruction lir : block.lir) {
                lir.allocatePhysicalRegisters();
            }
        }
    }

    /**
     * Write the tuples in this cfg to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeTuplesToStdOut(PrettyPrinter p) {
        p.indentRight();
        p.printf("========== TUPLES ==========\n\n");
        for (NBasicBlock block : basicBlocks) {
            block.writeTuplesToStdOut(p);
        }
        p.indentLeft();
    }

    /**
     * Write the hir instructions in this cfg to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeHirToStdOut(PrettyPrinter p) {
        p.indentRight();
        p.printf("========== HIR ==========\n\n");
        for (NBasicBlock block : basicBlocks) {
            block.writeHirToStdOut(p);
        }
        p.indentLeft();
    }

    /**
     * Write the lir instructions in this cfg to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeLirToStdOut(PrettyPrinter p) {
        p.indentRight();
        p.printf("========== LIR ==========\n\n");
        for (NBasicBlock block : basicBlocks) {
            block.writeLirToStdOut(p);
        }
        p.indentLeft();
    }

    /**
     * Write the intervals in this cfg to STDOUT.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeIntervalsToStdOut(PrettyPrinter p) {
        p.indentRight();
        p.printf("========== INTERVALS ==========\n\n");
        for (NInterval interval : intervals) {
            interval.writeToStdOut(p);
        }
        p.indentLeft();
        p.printf("\n");
    }

    /**
     * Clear the visitation information in each block in this cfg.
     */

    private void clearBlockVisitations() {
        for (NBasicBlock block : basicBlocks) {
            block.visited = false;
        }
    }

    /**
     * Given a basic block and its predecessor, return their common dominator.
     * 
     * @param b
     *            a basic block.
     * @param pred
     *            predecessor of b.
     * 
     * @return common dominator of the given block and its predecessor.
     */

    private NBasicBlock commonDom(NBasicBlock b, NBasicBlock pred) {
        NBasicBlock dom = b;
        clearBlockVisitations();
        while (dom != null) {
            dom.visited = true;
            dom = dom.dom;
        }
        dom = pred;
        while (!dom.visited) {
            dom = dom.dom;
        }
        return dom;
    }

    /**
     * Merge the locals from each of the predecessors of the specified block
     * with the locals in the block.
     * 
     * @param block
     *            block to merge into.
     */

    private void mergeLocals(NBasicBlock block) {
        for (NBasicBlock other : block.predecessors) {
            mergeLocals(block, other);
        }
    }

    /**
     * Merge the locals in block b with the locals in block a.
     * 
     * @param a
     *            block to merge into.
     * @param b
     *            block to merge from.
     */

    private void mergeLocals(NBasicBlock a, NBasicBlock b) {
        for (int i = 0; i < a.locals.length; i++) {
            if (a.cfg.hirMap.get(a.locals[i]).equals(
                    b.cfg.hirMap.get(b.locals[i]))) {
                continue;
            } else {
                ArrayList<Integer> args = new ArrayList<Integer>();
                args.add(a.locals[i]);
                args.add(b.locals[i]);
                NHIRInstruction ins = new NHIRPhiFunction(a,
                        NControlFlowGraph.hirId++, args, i);
                a.locals[i] = ins.id;
                a.hir.add(ins.id);
                a.cfg.hirMap.put(ins.id, ins);
                ((NHIRPhiFunction) ins).inferType();
            }
        }
    }

    /**
     * Convert the bytecode in the specified list to their tuple
     * representations.
     * 
     * @param code
     *            bytecode to convert.
     * 
     * @return list of tuples.
     */

    private ArrayList<NTuple> bytecodeToTuples(ArrayList<Integer> code) {
        ArrayList<NTuple> tuples = new ArrayList<NTuple>();
        for (int i = 0; i < code.size(); i++) {
            int pc = i;
            int opcode = code.get(i);
            int operandBytes = CLInstruction.instructionInfo[opcode].operandCount;
            short operandByte1, operandByte2, operandByte3, operandByte4;
            int pad, deflt;
            ArrayList<Short> operands = new ArrayList<Short>();
            switch (operandBytes) {
            case 0:
                break;
            case 1:
                operandByte1 = code.get(++i).shortValue();
                operands.add(operandByte1);
                break;
            case 2:
                operandByte1 = code.get(++i).shortValue();
                operandByte2 = code.get(++i).shortValue();
                operands.add(operandByte1);
                operands.add(operandByte2);
                break;
            case 3:
                operandByte1 = code.get(++i).shortValue();
                operandByte2 = code.get(++i).shortValue();
                operandByte3 = code.get(++i).shortValue();
                operands.add(operandByte1);
                operands.add(operandByte2);
                operands.add(operandByte3);
                break;
            case 4:
                operandByte1 = code.get(++i).shortValue();
                operandByte2 = code.get(++i).shortValue();
                operandByte3 = code.get(++i).shortValue();
                operandByte4 = code.get(++i).shortValue();
                operands.add(operandByte1);
                operands.add(operandByte2);
                operands.add(operandByte3);
                operands.add(operandByte4);
                break;
            case DYNAMIC: // TBD
                break;
            }
            tuples.add(new NTuple(pc, opcode, operands));
        }
        return tuples;
    }

    /**
     * Construct and return a short integer from two unsigned bytes specified.
     * 
     * @param a
     *            unsigned byte.
     * @param b
     *            unsigned byte.
     * 
     * @return a short integer constructed from the two unsigned bytes
     *         specified.
     */

    private short shortValue(short a, short b) {
        return (short) ((a << 8) | b);
    }

    /**
     * Construct and return an integer from the four unsigned bytes specified.
     * 
     * @param a
     *            unsigned byte.
     * @param b
     *            unsigned byte.
     * @param c
     *            unsigned byte.
     * @param d
     *            unsigned byte.
     * @return an integer constructed from the four unsigned bytes specified.
     */

    private int intValue(short a, short b, short c, short d) {
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    /**
     * Extract and return the JVM bytecode for the method denoted by this cfg.
     * 
     * @return JVM bytecode for the method denoted by this cfg.
     */

    private ArrayList<Integer> getByteCode() {
        ArrayList<Integer> code = null;
        for (CLAttributeInfo info : m.attributes) {
            if (info instanceof CLCodeAttribute) {
                code = ((CLCodeAttribute) info).code;
                break;
            }
        }
        return code;
    }

    /**
     * Return short form of the specified type descriptor.
     * 
     * @param descriptor
     *            type descriptor.
     * 
     * @return short form of type descriptor.
     */

    private String shortType(String descriptor) {
        String sType = "V";
        char c = descriptor.charAt(0);
        switch (c) {
        case 'B':
        case 'C':
        case 'I':
        case 'F':
        case 'S':
        case 'Z':
        case 'J':
        case 'D':
            sType = c + "";
            break;
        case '[':
        case 'L':
            sType = "L";
            break;
        }
        return sType;
    }

    /**
     * Return the number of local variables in the method denoted by this cfg.
     * 
     * @return number of local variables.
     */

    private int numLocals() {
        ArrayList<Integer> code = null;
        int numLocals = 0;
        for (CLAttributeInfo info : m.attributes) {
            if (info instanceof CLCodeAttribute) {
                code = ((CLCodeAttribute) info).code;
                numLocals = ((CLCodeAttribute) info).maxLocals;
                break;
            }
        }
        return numLocals;
    }

    /**
     * Return the argument count (number of formal parameters) for the specified
     * method. 0 is returned if the descriptor is invalid.
     * 
     * @param descriptor
     *            method descriptor.
     * @return argument count for the specified method.
     */

    private int argumentCount(String descriptor) {
        int i = 0;

        // Extract types of arguments and the return type from
        // the method descriptor
        String argTypes = descriptor.substring(1, descriptor.lastIndexOf(")"));

        // Find number of arguments
        for (int j = 0; j < argTypes.length(); j++) {
            char c = argTypes.charAt(j);
            switch (c) {
            case 'B':
            case 'C':
            case 'I':
            case 'F':
            case 'S':
            case 'Z':
                i += 1;
                break;
            case '[':
                break;
            case 'J':
            case 'D':
                i += 2;
                break;
            case 'L':
                int k = argTypes.indexOf(";", j);
                j = k;
                i += 1;
                break;
            }
        }
        return i;
    }

    /**
     * Return the argument count (number of formal parameters) for the specified
     * method. 0 is returned if the descriptor is invalid.
     * 
     * @param descriptor
     *            method descriptor.
     * @return argument count for the specified method.
     */

    private ArrayList<String> argumentTypes(String descriptor) {
        ArrayList<String> args = new ArrayList<String>();
        int i = 0;

        // Extract types of arguments and the return type from
        // the method descriptor
        String argTypes = descriptor.substring(1, descriptor.lastIndexOf(")"));

        String type = "";

        // Find number of arguments
        for (int j = 0; j < argTypes.length(); j++) {
            char c = argTypes.charAt(j);
            switch (c) {
            case 'B':
            case 'C':
            case 'I':
            case 'F':
            case 'S':
            case 'Z':
            case 'J':
            case 'D':
                args.add(type + String.valueOf(c));
                type = "";
                break;
            case '[':
                type += c;
                break;
            case 'L':
                int k = argTypes.indexOf(";", j);
                args.add(type + argTypes.substring(j, k));
                type = "";
                j = k;
                break;
            }
        }
        return args;
    }

    /**
     * Return the return type of a method given its descriptor.
     * 
     * @param descriptor
     *            descriptor of the method.
     * 
     * @return return type, "V" if void.
     */

    private String returnType(String descriptor) {
        String returnType = descriptor
                .substring(descriptor.lastIndexOf(")") + 1);
        return returnType;
    }

}
