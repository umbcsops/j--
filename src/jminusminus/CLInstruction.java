// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import static jminusminus.CLConstants.*;
import static jminusminus.CLConstants.Category.*;

/**
 * Representation of a JVM instruction. It stores the opcode and mnenomic of an
 * instruction, its operand count (DYNAMIC if the instruction has variable
 * number operands), pc (location counter), stack units (words produced - words
 * consumed from the operand stack), and local variable index (IRRELEVANT if the
 * instruction does not operate on local variables).
 */

abstract class CLInstruction {

    /** Opcode for this instruction. */
    protected int opcode;

    /** Mnemonic for this instruction. */
    protected String mnemonic;

    /**
     * Number of operands for this instruction; determined statically for all
     * instructions except TABLESWITCH and LOOKUPSWITCH.
     */
    protected int operandCount;

    /**
     * Location counter; index of this instruction within the code array of a
     * method.
     */
    protected int pc;

    /**
     * Stack units; words produced - words consumed from the operand stack by
     * this instruction.
     */
    protected int stackUnits;

    /**
     * Index of the local variable that this instruction refers to; applies only
     * to instructions that operate on local variables.
     */
    protected int localVariableIndex;

    /**
     * For each JVM instruction, this array stores its opcode, mnemonic, number
     * of operands (DYNAMIC for instructions with variable attribute count),
     * local variable index (IRRELEVANT where not applicable), stack units, and
     * instruction category. For example, for IMUL, these parameters are IMUL,
     * "imul", 0, IRRELEVANT, -1, ARITHMETIC1.
     */
    public static final CLInsInfo[] instructionInfo = {
            new CLInsInfo(NOP, "nop", 0, IRRELEVANT, 0, MISC),
            new CLInsInfo(ACONST_NULL, "aconst_null", 0, IRRELEVANT, 1,
                    LOAD_STORE1),
            new CLInsInfo(ICONST_M1, "iconst_m1", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_0, "iconst_0", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_1, "iconst_1", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_2, "iconst_2", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_3, "iconst_3", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_4, "iconst_4", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(ICONST_5, "iconst_5", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(LCONST_0, "lconst_0", 0, IRRELEVANT, 2, LOAD_STORE1),
            new CLInsInfo(LCONST_1, "lconst_1", 0, IRRELEVANT, 2, LOAD_STORE1),
            new CLInsInfo(FCONST_0, "fconst_0", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(FCONST_1, "fconst_1", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(FCONST_2, "fconst_2", 0, IRRELEVANT, 1, LOAD_STORE1),
            new CLInsInfo(DCONST_0, "dconst_0", 0, IRRELEVANT, 2, LOAD_STORE1),
            new CLInsInfo(DCONST_1, "dconst_1", 0, IRRELEVANT, 2, LOAD_STORE1),
            new CLInsInfo(BIPUSH, "bipush", 1, IRRELEVANT, 1, LOAD_STORE3),
            new CLInsInfo(SIPUSH, "sipush", 2, IRRELEVANT, 1, LOAD_STORE3),
            new CLInsInfo(LDC, "ldc", 1, IRRELEVANT, 1, LOAD_STORE4),
            new CLInsInfo(LDC_W, "ldc_w", 2, IRRELEVANT, 1, LOAD_STORE4),
            new CLInsInfo(LDC2_W, "ldc2_w", 2, IRRELEVANT, 2, LOAD_STORE4),
            new CLInsInfo(ILOAD, "iload", 1, DYNAMIC, 1, LOAD_STORE2),
            new CLInsInfo(LLOAD, "lload", 1, DYNAMIC, 2, LOAD_STORE2),
            new CLInsInfo(FLOAD, "fload", 1, DYNAMIC, 1, LOAD_STORE2),
            new CLInsInfo(DLOAD, "dload", 1, DYNAMIC, 2, LOAD_STORE2),
            new CLInsInfo(ALOAD, "aload", 1, DYNAMIC, 1, LOAD_STORE2),
            new CLInsInfo(ILOAD_0, "iload_0", 0, 0, 1, LOAD_STORE1),
            new CLInsInfo(ILOAD_1, "iload_1", 0, 1, 1, LOAD_STORE1),
            new CLInsInfo(ILOAD_2, "iload_2", 0, 2, 1, LOAD_STORE1),
            new CLInsInfo(ILOAD_3, "iload_3", 0, 3, 1, LOAD_STORE1),
            new CLInsInfo(LLOAD_0, "lload_0", 0, 0, 2, LOAD_STORE1),
            new CLInsInfo(LLOAD_1, "lload_1", 0, 1, 2, LOAD_STORE1),
            new CLInsInfo(LLOAD_2, "lload_2", 0, 2, 2, LOAD_STORE1),
            new CLInsInfo(LLOAD_3, "lload_3", 0, 3, 2, LOAD_STORE1),
            new CLInsInfo(FLOAD_0, "fload_0", 0, 0, 1, LOAD_STORE1),
            new CLInsInfo(FLOAD_1, "fload_1", 0, 1, 1, LOAD_STORE1),
            new CLInsInfo(FLOAD_2, "fload_2", 0, 2, 1, LOAD_STORE1),
            new CLInsInfo(FLOAD_3, "fload_3", 0, 3, 1, LOAD_STORE1),
            new CLInsInfo(DLOAD_0, "dload_0", 0, 0, 2, LOAD_STORE1),
            new CLInsInfo(DLOAD_1, "dload_1", 0, 1, 2, LOAD_STORE1),
            new CLInsInfo(DLOAD_2, "dload_2", 0, 2, 2, LOAD_STORE1),
            new CLInsInfo(DLOAD_3, "dload_3", 0, 3, 2, LOAD_STORE1),
            new CLInsInfo(ALOAD_0, "aload_0", 0, 0, 1, LOAD_STORE1),
            new CLInsInfo(ALOAD_1, "aload_1", 0, 1, 1, LOAD_STORE1),
            new CLInsInfo(ALOAD_2, "aload_2", 0, 2, 1, LOAD_STORE1),
            new CLInsInfo(ALOAD_3, "aload_3", 0, 3, 1, LOAD_STORE1),
            new CLInsInfo(IALOAD, "iaload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(LALOAD, "laload", 0, IRRELEVANT, 0, ARRAY2),
            new CLInsInfo(FALOAD, "faload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(DALOAD, "daload", 0, IRRELEVANT, 0, ARRAY2),
            new CLInsInfo(AALOAD, "aaload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(BALOAD, "baload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(CALOAD, "caload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(SALOAD, "saload", 0, IRRELEVANT, -1, ARRAY2),
            new CLInsInfo(ISTORE, "istore", 1, DYNAMIC, -1, LOAD_STORE2),
            new CLInsInfo(LSTORE, "lstore", 1, DYNAMIC, -2, LOAD_STORE2),
            new CLInsInfo(FSTORE, "fstore", 1, DYNAMIC, -1, LOAD_STORE2),
            new CLInsInfo(DSTORE, "dstore", 1, DYNAMIC, -2, LOAD_STORE2),
            new CLInsInfo(ASTORE, "astore", 1, DYNAMIC, -1, LOAD_STORE2),
            new CLInsInfo(ISTORE_0, "istore_0", 0, 0, -1, LOAD_STORE1),
            new CLInsInfo(ISTORE_1, "istore_1", 0, 1, -1, LOAD_STORE1),
            new CLInsInfo(ISTORE_2, "istore_2", 0, 2, -1, LOAD_STORE1),
            new CLInsInfo(ISTORE_3, "istore_3", 0, 3, -1, LOAD_STORE1),
            new CLInsInfo(LSTORE_0, "lstore_0", 0, 0, -2, LOAD_STORE1),
            new CLInsInfo(LSTORE_1, "lstore_1", 0, 1, -2, LOAD_STORE1),
            new CLInsInfo(LSTORE_2, "lstore_2", 0, 2, -2, LOAD_STORE1),
            new CLInsInfo(LSTORE_3, "lstore_3", 0, 3, -2, LOAD_STORE1),
            new CLInsInfo(FSTORE_0, "fstore_0", 0, 0, -1, LOAD_STORE1),
            new CLInsInfo(FSTORE_1, "fstore_1", 0, 1, -1, LOAD_STORE1),
            new CLInsInfo(FSTORE_2, "fstore_2", 0, 2, -1, LOAD_STORE1),
            new CLInsInfo(FSTORE_3, "fstore_3", 0, 3, -1, LOAD_STORE1),
            new CLInsInfo(DSTORE_0, "dstore_0", 0, 0, -2, LOAD_STORE1),
            new CLInsInfo(DSTORE_1, "dstore_1", 0, 1, -2, LOAD_STORE1),
            new CLInsInfo(DSTORE_2, "dstore_2", 0, 2, -2, LOAD_STORE1),
            new CLInsInfo(DSTORE_3, "dstore_3", 0, 3, -2, LOAD_STORE1),
            new CLInsInfo(ASTORE_0, "astore_0", 0, 0, -1, LOAD_STORE1),
            new CLInsInfo(ASTORE_1, "astore_1", 0, 1, -1, LOAD_STORE1),
            new CLInsInfo(ASTORE_2, "astore_2", 0, 2, -1, LOAD_STORE1),
            new CLInsInfo(ASTORE_3, "astore_3", 0, 3, -1, LOAD_STORE1),
            new CLInsInfo(IASTORE, "iastore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(LASTORE, "lastore", 0, IRRELEVANT, -4, ARRAY2),
            new CLInsInfo(FASTORE, "fastore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(DASTORE, "dastore", 0, IRRELEVANT, -4, ARRAY2),
            new CLInsInfo(AASTORE, "aastore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(BASTORE, "bastore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(CASTORE, "castore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(SASTORE, "sastore", 0, IRRELEVANT, -3, ARRAY2),
            new CLInsInfo(POP, "pop", 0, IRRELEVANT, -1, STACK),
            new CLInsInfo(POP2, "pop2", 0, IRRELEVANT, -2, STACK),
            new CLInsInfo(DUP, "dup", 0, IRRELEVANT, 1, STACK),
            new CLInsInfo(DUP_X1, "dup_x1", 0, IRRELEVANT, 1, STACK),
            new CLInsInfo(DUP_X2, "dup_x2", 0, IRRELEVANT, 1, STACK),
            new CLInsInfo(DUP2, "dup2", 0, IRRELEVANT, 2, STACK),
            new CLInsInfo(DUP2_X1, "dup2_x1", 0, IRRELEVANT, 2, STACK),
            new CLInsInfo(DUP2_X2, "dup2_x2", 0, IRRELEVANT, 2, STACK),
            new CLInsInfo(SWAP, "swap", 0, IRRELEVANT, 0, STACK),
            new CLInsInfo(IADD, "iadd", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(LADD, "ladd", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(FADD, "fadd", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(DADD, "dadd", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(ISUB, "isub", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(LSUB, "lsub", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(FSUB, "fsub", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(DSUB, "dsub", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(IMUL, "imul", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(LMUL, "lmul", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(FMUL, "fmul", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(DMUL, "dmul", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(IDIV, "idiv", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(LDIV, "ldiv", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(FDIV, "fdiv", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(DDIV, "ddiv", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(IREM, "irem", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(LREM, "lrem", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(FREM, "frem", 0, IRRELEVANT, -1, ARITHMETIC1),
            new CLInsInfo(DREM, "drem", 0, IRRELEVANT, -2, ARITHMETIC1),
            new CLInsInfo(INEG, "ineg", 0, IRRELEVANT, 0, ARITHMETIC1),
            new CLInsInfo(LNEG, "lneg", 0, IRRELEVANT, 0, ARITHMETIC1),
            new CLInsInfo(FNEG, "fneg", 0, IRRELEVANT, 0, ARITHMETIC1),
            new CLInsInfo(DNEG, "dneg", 0, IRRELEVANT, 0, ARITHMETIC1),
            new CLInsInfo(ISHL, "ishl", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LSHL, "lshl", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(ISHR, "ishr", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LSHR, "lshr", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(IUSHR, "iushr", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LUSHR, "lushr", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(IAND, "iand", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LAND, "land", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(IOR, "ior", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LOR, "lor", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(IXOR, "ixor", 0, IRRELEVANT, -1, BIT),
            new CLInsInfo(LXOR, "lxor", 0, IRRELEVANT, -2, BIT),
            new CLInsInfo(IINC, "iinc", 2, DYNAMIC, 0, ARITHMETIC2),
            new CLInsInfo(I2L, "i2l", 0, IRRELEVANT, 1, CONVERSION),
            new CLInsInfo(I2F, "i2f", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(I2D, "i2d", 0, IRRELEVANT, 1, CONVERSION),
            new CLInsInfo(L2I, "l2i", 0, IRRELEVANT, -1, CONVERSION),
            new CLInsInfo(L2F, "l2f", 0, IRRELEVANT, -1, CONVERSION),
            new CLInsInfo(L2D, "l2d", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(F2I, "f2i", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(F2L, "f2l", 0, IRRELEVANT, 1, CONVERSION),
            new CLInsInfo(F2D, "f2d", 0, IRRELEVANT, 1, CONVERSION),
            new CLInsInfo(D2I, "d2i", 0, IRRELEVANT, -1, CONVERSION),
            new CLInsInfo(D2L, "d2l", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(D2F, "d2f", 0, IRRELEVANT, -1, CONVERSION),
            new CLInsInfo(I2B, "i2b", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(I2C, "i2c", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(I2S, "i2s", 0, IRRELEVANT, 0, CONVERSION),
            new CLInsInfo(LCMP, "lcmp", 0, IRRELEVANT, -3, COMPARISON),
            new CLInsInfo(FCMPL, "fcmpl", 0, IRRELEVANT, -1, COMPARISON),
            new CLInsInfo(FCMPG, "fcmpg", 0, IRRELEVANT, -1, COMPARISON),
            new CLInsInfo(DCMPL, "dcmpl", 0, IRRELEVANT, -3, COMPARISON),
            new CLInsInfo(DCMPG, "dcmpg", 0, IRRELEVANT, -3, COMPARISON),
            new CLInsInfo(IFEQ, "ifeq", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFNE, "ifne", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFLT, "iflt", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFGE, "ifge", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFGT, "ifgt", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFLE, "ifle", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPEQ, "if_icmpeq", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPNE, "if_icmpne", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPLT, "if_icmplt", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPGE, "if_icmpge", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPGT, "if_icmpgt", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ICMPLE, "if_icmple", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ACMPEQ, "if_acmpeq", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(IF_ACMPNE, "if_acmpne", 2, IRRELEVANT, -2,
                    FLOW_CONTROL1),
            new CLInsInfo(GOTO, "goto", 2, IRRELEVANT, 0, FLOW_CONTROL1),
            new CLInsInfo(JSR, "jsr", 2, IRRELEVANT, 1, FLOW_CONTROL1),
            new CLInsInfo(RET, "ret", 1, IRRELEVANT, 0, FLOW_CONTROL2),
            new CLInsInfo(TABLESWITCH, "tableswitch", DYNAMIC, IRRELEVANT, -1,
                    FLOW_CONTROL3),
            new CLInsInfo(LOOKUPSWITCH, "lookupswitch", DYNAMIC, IRRELEVANT,
                    -1, FLOW_CONTROL4),
            new CLInsInfo(IRETURN, "ireturn", 0, IRRELEVANT, EMPTY_STACK,
                    METHOD2),
            new CLInsInfo(LRETURN, "lreturn", 0, IRRELEVANT, EMPTY_STACK,
                    METHOD2),
            new CLInsInfo(FRETURN, "freturn", 0, IRRELEVANT, EMPTY_STACK,
                    METHOD2),
            new CLInsInfo(DRETURN, "dreturn", 0, IRRELEVANT, EMPTY_STACK,
                    METHOD2),
            new CLInsInfo(ARETURN, "areturn", 0, IRRELEVANT, EMPTY_STACK,
                    METHOD2),
            new CLInsInfo(RETURN, "return", 0, IRRELEVANT, EMPTY_STACK, METHOD2),
            new CLInsInfo(GETSTATIC, "getstatic", 2, IRRELEVANT, DYNAMIC, FIELD),
            new CLInsInfo(PUTSTATIC, "putstatic", 2, IRRELEVANT, DYNAMIC, FIELD),
            new CLInsInfo(GETFIELD, "getfield", 2, IRRELEVANT, DYNAMIC, FIELD),
            new CLInsInfo(PUTFIELD, "putfield", 2, IRRELEVANT, DYNAMIC, FIELD),
            new CLInsInfo(INVOKEVIRTUAL, "invokevirtual", 2, IRRELEVANT,
                    DYNAMIC, METHOD1),
            new CLInsInfo(INVOKESPECIAL, "invokespecial", 2, IRRELEVANT,
                    DYNAMIC, METHOD1),
            new CLInsInfo(INVOKESTATIC, "invokestatic", 2, IRRELEVANT, DYNAMIC,
                    METHOD1),
            new CLInsInfo(INVOKEINTERFACE, "invokeinterface", 4, IRRELEVANT,
                    DYNAMIC, METHOD1),
            new CLInsInfo(INVOKEDYNAMIC, "invokedynamic", 2, IRRELEVANT,
                    DYNAMIC, METHOD1),
            new CLInsInfo(NEW, "new", 2, IRRELEVANT, 1, OBJECT),
            new CLInsInfo(NEWARRAY, "newarray", 1, IRRELEVANT, 0, ARRAY1),
            new CLInsInfo(ANEWARRAY, "anewarray", 2, IRRELEVANT, 0, ARRAY1),
            new CLInsInfo(ARRAYLENGTH, "arraylength", 0, IRRELEVANT, 0, ARRAY2),
            new CLInsInfo(ATHROW, "athrow", 0, IRRELEVANT, UNIT_SIZE_STACK,
                    MISC),
            new CLInsInfo(CHECKCAST, "checkcast", 2, IRRELEVANT, 0, OBJECT),
            new CLInsInfo(INSTANCEOF, "instanceof", 2, IRRELEVANT, 0, OBJECT),
            new CLInsInfo(MONITORENTER, "monitorenter", 0, IRRELEVANT, -1, MISC),
            new CLInsInfo(MONITOREXIT, "monitorexit", 0, IRRELEVANT, -1, MISC),
            new CLInsInfo(WIDE, "wide", 3, IRRELEVANT, 0, LOAD_STORE1),
            new CLInsInfo(MULTIANEWARRAY, "multianewarray", 3, IRRELEVANT, 0,
                    ARRAY3),
            new CLInsInfo(IFNULL, "ifnull", 2, IRRELEVANT, -1, FLOW_CONTROL1),
            new CLInsInfo(IFNONNULL, "ifnonnull", 2, IRRELEVANT, -1,
                    FLOW_CONTROL1),
            new CLInsInfo(GOTO_W, "goto_w", 4, IRRELEVANT, 0, FLOW_CONTROL1),
            new CLInsInfo(JSR_W, "jsr_w", 4, IRRELEVANT, 1, FLOW_CONTROL1) };

    /**
     * Return true if the opcode is valid; false otherwise.
     * 
     * @param opcode
     *            instruction opcode.
     * @return true or false.
     */

    public static boolean isValid(int opcode) {
        return NOP <= opcode && opcode <= JSR_W;
    }

    /**
     * Return the opcode for this instruction.
     * 
     * @return the opcode.
     */

    public int opcode() {
        return opcode;
    }

    /**
     * Return the mnemonic for this instruction.
     * 
     * @return the mnemonic.
     */

    public String mnemonic() {
        return mnemonic;
    }

    /**
     * Return the number of operands for this instruction.
     * 
     * @return number of operands.
     */

    public int operandCount() {
        return operandCount;
    }

    /**
     * Return the pc for this instruction.
     * 
     * @return the pc.
     */

    public int pc() {
        return pc;
    }

    /**
     * Return the stack units for this instruction.
     * 
     * @return the stack units.
     */

    public int stackUnits() {
        return stackUnits;
    }

    /**
     * Return the local variable index for this instruction.
     * 
     * @return the local variable index.
     */

    public int localVariableIndex() {
        return localVariableIndex;
    }

    /**
     * Return the bytecode for this instruction.
     * 
     * @return bytecode.
     */

    public abstract ArrayList<Integer> toBytes();

    /**
     * Return the byte from i at position byteNum.
     * 
     * @param i
     *            number whose individual byte is required.
     * @param byteNum
     *            the byte to return; 1 (lower) - 4 (higher) instructions.
     * @return the byte at the specified position.
     */

    protected int byteAt(int i, int byteNum) {
        int j = 0, mask = 0xFF;
        switch (byteNum) {
        case 1: // lower order
            j = i & mask;
            break;
        case 2:
            j = (i >> 8) & mask;
            break;
        case 3:
            j = (i >> 16) & mask;
            break;
        case 4: // higher order
            j = (i >> 24) & mask;
            break;
        }
        return j;
    }

}

/**
 * Representation for OBJECT instructions.
 */

class CLObjectInstruction extends CLInstruction {

    /**
     * Index into the constant pool, the item at which identifies the object
     * type.
     */
    private int index;

    /**
     * Construct a CLObjectInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param index
     *            index into the constant pool, the item at which identifies the
     *            object.
     */

    public CLObjectInstruction(int opcode, int pc, int index) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.index = index;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        bytes.add(byteAt(index, 2));
        bytes.add(byteAt(index, 1));
        return bytes;
    }

}

/**
 * Representation for FIELD instructions.
 */

class CLFieldInstruction extends CLInstruction {

    /**
     * Index into the constant pool, the item at which contains the name and
     * descriptor of the field.
     */
    private int index;

    /**
     * Construct a CLFieldInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param index
     *            index into the constant pool, the item at which contains the
     *            name and descriptor of the field.
     * @param stackUnits
     *            words produced - words consumed from the operand stack by this
     *            instruction.
     */

    public CLFieldInstruction(int opcode, int pc, int index, int stackUnits) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        super.stackUnits = stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.index = index;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        bytes.add(byteAt(index, 2));
        bytes.add(byteAt(index, 1));
        return bytes;
    }

}

/**
 * Representation for METHOD1 and METHOD2 instructions.
 */

class CLMethodInstruction extends CLInstruction {

    /**
     * Index into the constant pool, the item at which contains the name and
     * descriptor of the method.
     */
    private int index;

    /**
     * Number of arguments in case of INVOKEINTERFACE instruction.
     */
    private int nArgs;

    /**
     * Construct a CLMethodInstruction object for METHOD1 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param index
     *            index into the constant pool, the item at which contains the
     *            name and descriptor of the method.
     * @param stackUnits
     *            words produced - words consumed from the operand stack by this
     *            instruction.
     */

    public CLMethodInstruction(int opcode, int pc, int index, int stackUnits) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        super.stackUnits = stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.index = index;
    }

    /**
     * Construct a CLMethodInstruction object for METHOD2 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLMethodInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * Set the number of arguments for the method for INVOKEINTERFACE
     * instruction.
     * 
     * @param nArgs
     *            number of arguments for the method.
     */

    public void setArgumentCount(int nArgs) {
        this.nArgs = nArgs;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        if (instructionInfo[opcode].category == METHOD1) {
            bytes.add(byteAt(index, 2));
            bytes.add(byteAt(index, 1));

            // INVOKEINTERFACE expects the number of arguments of
            // the method as the third operand and a fourth
            // argument which must always be 0.
            if (opcode == INVOKEINTERFACE) {
                bytes.add(byteAt(nArgs, 1));
                bytes.add(0);
            }
        }
        return bytes;
    }

}

/**
 * Representation for ARRAY1, ARRAY2 and ARRAY3 instructions.
 */

class CLArrayInstruction extends CLInstruction {

    /**
     * A number identifying the type of primitive array, or an index into the
     * constant pool, the item at which specifies the reference type of the
     * array.
     */
    private int type;

    /** Number of dimensions in case of a multi-dimensional array. */
    private int dim;

    /**
     * Construct a CLArrayInstruction object for ARRAY1 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param type
     *            number identifying the type.
     */

    public CLArrayInstruction(int opcode, int pc, int type) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.type = type;
    }

    /**
     * Construct a CLArrayInstruction object for ARRAY2 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param type
     *            number identifying the type.
     * @param dim
     *            number of dimensions.
     */

    public CLArrayInstruction(int opcode, int pc, int type, int dim) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.type = type;
        this.dim = dim;
    }

    /**
     * Construct a CLArrayInstruction object for ARRAY3 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLArrayInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        switch (opcode) {
        case NEWARRAY:
            bytes.add(byteAt(type, 1));
            break;
        case ANEWARRAY:
            bytes.add(byteAt(type, 2));
            bytes.add(byteAt(type, 1));
            break;
        case MULTIANEWARRAY:
            bytes.add(byteAt(type, 2));
            bytes.add(byteAt(type, 1));
            bytes.add(byteAt(dim, 1));
            break;
        }
        return bytes;
    }

}

/**
 * Representation for ARITHMETIC1 and ARITHMETIC2 instructions.
 */

class CLArithmeticInstruction extends CLInstruction {

    /**
     * Whether this instruction is preceeded by a WIDE instruction; applies only
     * to IINC.
     */
    private boolean isWidened;

    /** Increment value for IINC instruction. */
    private int constVal;

    /**
     * Construct a CLArithmeticInstruction object for ARITHMETIC1 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLArithmeticInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * Construct a CLArithmeticInstruction object for IINC instruction.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param localVariableIndex
     *            index of the local variable to increment.
     * @param constVal
     *            increment value.
     * @param isWidened
     *            whether this instruction is preceeded by the WIDE (widening)
     *            instruction.
     */

    public CLArithmeticInstruction(int opcode, int pc, int localVariableIndex,
            int constVal, boolean isWidened) {
        super.opcode = opcode;
        super.pc = pc;
        super.localVariableIndex = localVariableIndex;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        this.constVal = constVal;
        this.isWidened = isWidened;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        if (opcode == IINC) {
            if (isWidened) {
                bytes.add(byteAt(localVariableIndex, 2));
                bytes.add(byteAt(localVariableIndex, 1));
                bytes.add(byteAt(constVal, 2));
                bytes.add(byteAt(constVal, 1));
            } else {
                bytes.add(byteAt(localVariableIndex, 1));
                bytes.add(byteAt(constVal, 1));
            }
        }
        return bytes;
    }

}

/**
 * Representation for BIT instructions.
 */

class CLBitInstruction extends CLInstruction {

    /**
     * Construct a CLBitInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLBitInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        return bytes;
    }

}

/**
 * Representation for COMPARISON instructions.
 */

class CLComparisonInstruction extends CLInstruction {

    /**
     * Construct a CLComparisonInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLComparisonInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        return bytes;
    }

}

/**
 * Representation for CONVERSION instructions.
 */

class CLConversionInstruction extends CLInstruction {

    /**
     * Construct a CLConversionInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLConversionInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        return bytes;
    }

}

/**
 * Representation for FLOW_CONTROL1, FLOW_CONTROL2, FLOW_CONTROL3 and
 * FLOW_CONTROL4 instructions.
 */

class CLFlowControlInstruction extends CLInstruction {

    /**
     * Jump label; this flow control instruction will jump to an instruction
     * after this label.
     */
    private String jumpToLabel;

    /** jumpLabel is resolved to this offset. */
    private int jumpToOffset;

    /**
     * Index of the local variable containing the return address; applies only
     * to RET instruction.
     */
    private int index;

    /**
     * Whether this instruction is preceeded by a WIDE instruction; applies only
     * to RET instruction.
     */
    private boolean isWidened;

    /**
     * These many (0-3) bytes are added before default offset so that the index
     * of the offset is divisible by 4.
     */
    private int pad;

    /**
     * Jump label for default value for TABLESWITCH and LOOKUPSWITCH
     * instructions.
     */
    private String defaultLabel;

    /** defaultLabel is resolved to this offset. */
    private int defaultOffset;

    /**
     * Number of pairs in the match table for LOOKUPSWITCH instruction.
     */
    private int numPairs;

    /** Key and label table for LOOKUPSWITCH instruction. */
    private TreeMap<Integer, String> matchLabelPairs;

    /**
     * Key and offset (resolved labels from matchLabelPairs) table for
     * LOOKUPSWITCH instruction.
     */
    private TreeMap<Integer, Integer> matchOffsetPairs;

    /** Smallest value of index for TABLESWITCH instruction. */
    private int low;

    /** Highest value of index for TABLESWITCH instruction. */
    private int high;

    /**
     * List of jump labels for TABLESWITCH instruction for each index value from
     * low to high, end values included.
     */
    private ArrayList<String> labels;

    /**
     * List of offsets (resolved labels from labels) for TABLESWITCH
     * instruction.
     */
    private ArrayList<Integer> offsets;

    /**
     * Construct a CLFlowControlInstruction object for FLOW_CONTROL1
     * instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param jumpToLabel
     *            the label to jump to.
     */

    public CLFlowControlInstruction(int opcode, int pc, String jumpToLabel) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.jumpToLabel = jumpToLabel;
    }

    /**
     * Construct a CLFlowControlInstruction object for RET instruction.
     * 
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param index
     *            index of the local variable containing the return address.
     * @param isWidened
     *            whether this instruction is preceeded by the WIDE (widening)
     *            instruction.
     */

    public CLFlowControlInstruction(int pc, int index, boolean isWidened) {
        super.opcode = RET;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.index = index;
        this.isWidened = isWidened;
    }

    /**
     * Construct a CLFlowControlInstruction object for TABLESWITCH instruction.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param defaultLabel
     *            jump label for default value.
     * @param low
     *            smallest value of index.
     * @param high
     *            highest value of index.
     * @param labels
     *            list of jump labels for each index value from low to high, end
     *            values included.
     */

    public CLFlowControlInstruction(int opcode, int pc, String defaultLabel,
            int low, int high, ArrayList<String> labels) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.defaultLabel = defaultLabel;
        this.low = low;
        this.high = high;
        this.labels = labels;
        pad = 4 - ((pc + 1) % 4);
        operandCount = pad + 12 + 4 * labels.size();
    }

    /**
     * Construct a CLFlowControlInstruction object for LOOKUPSWITCH instruction.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param defaultLabel
     *            jump label for default value.
     * @param numPairs
     *            number of pairs in the match table.
     * @param matchLabelPairs
     *            key match table.
     */

    public CLFlowControlInstruction(int opcode, int pc, String defaultLabel,
            int numPairs, TreeMap<Integer, String> matchLabelPairs) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.defaultLabel = defaultLabel;
        this.numPairs = numPairs;
        this.matchLabelPairs = matchLabelPairs;
        pad = 4 - ((pc + 1) % 4);
        operandCount = pad + 8 + 8 * numPairs;
    }

    /**
     * Resolve the jump labels to the corresponding offset values using the
     * given label to pc mapping. If unable to resolve a label, the offset is
     * set such that the next instruction will be executed.
     * 
     * @param labelToPC
     *            label to pc mapping.
     * @return true if all labels were resolved successfully; false otherwise.
     */

    public boolean resolveLabels(Hashtable<String, Integer> labelToPC) {
        boolean allLabelsResolved = true;
        if (instructionInfo[opcode].category == FLOW_CONTROL1) {
            if (labelToPC.containsKey(jumpToLabel)) {
                jumpToOffset = labelToPC.get(jumpToLabel) - pc;
            } else {
                jumpToOffset = operandCount;
                allLabelsResolved = false;
            }
        } else if (opcode == LOOKUPSWITCH) {
            if (labelToPC.containsKey(defaultLabel)) {
                defaultOffset = labelToPC.get(defaultLabel) - pc;
            } else {
                defaultOffset = operandCount;
                allLabelsResolved = false;
            }
            matchOffsetPairs = new TreeMap<Integer, Integer>();
            Set<Entry<Integer, String>> matches = matchLabelPairs.entrySet();
            Iterator<Entry<Integer, String>> iter = matches.iterator();
            while (iter.hasNext()) {
                Entry<Integer, String> entry = iter.next();
                int match = entry.getKey();
                String label = entry.getValue();
                if (labelToPC.containsKey(label)) {
                    matchOffsetPairs.put(match, labelToPC.get(label) - pc);
                } else {
                    matchOffsetPairs.put(match, operandCount);
                    allLabelsResolved = false;
                }
            }
        } else if (opcode == TABLESWITCH) {
            if (labelToPC.containsKey(defaultLabel)) {
                defaultOffset = labelToPC.get(defaultLabel) - pc;
            } else {
                defaultOffset = operandCount;
                allLabelsResolved = false;
            }
            offsets = new ArrayList<Integer>();
            for (int i = 0; i < labels.size(); i++) {
                if (labelToPC.containsKey(labels.get(i))) {
                    offsets.add(labelToPC.get(labels.get(i)) - pc);
                } else {
                    offsets.add(operandCount);
                    allLabelsResolved = false;
                }
            }
        }
        return allLabelsResolved;
    }

    /**
     * Return the pc of instruction to jump to.
     * 
     * @return pc to jump to.
     */

    public int jumpToOffset() {
        return jumpToOffset;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        switch (opcode) {
        case RET:
            if (isWidened) {
                bytes.add(byteAt(index, 2));
                bytes.add(byteAt(index, 1));
            } else {
                bytes.add(byteAt(index, 1));
            }
            break;
        case TABLESWITCH:
            for (int i = 0; i < pad; i++) {
                bytes.add(0);
            }
            bytes.add(byteAt(defaultOffset, 4));
            bytes.add(byteAt(defaultOffset, 3));
            bytes.add(byteAt(defaultOffset, 2));
            bytes.add(byteAt(defaultOffset, 1));
            bytes.add(byteAt(low, 4));
            bytes.add(byteAt(low, 3));
            bytes.add(byteAt(low, 2));
            bytes.add(byteAt(low, 1));
            bytes.add(byteAt(high, 4));
            bytes.add(byteAt(high, 3));
            bytes.add(byteAt(high, 2));
            bytes.add(byteAt(high, 1));
            for (int i = 0; i < offsets.size(); i++) {
                int jumpOffset = offsets.get(i);
                bytes.add(byteAt(jumpOffset, 4));
                bytes.add(byteAt(jumpOffset, 3));
                bytes.add(byteAt(jumpOffset, 2));
                bytes.add(byteAt(jumpOffset, 1));
            }
            break;
        case LOOKUPSWITCH:
            for (int i = 0; i < pad; i++) {
                bytes.add(0);
            }
            bytes.add(byteAt(defaultOffset, 4));
            bytes.add(byteAt(defaultOffset, 3));
            bytes.add(byteAt(defaultOffset, 2));
            bytes.add(byteAt(defaultOffset, 1));
            bytes.add(byteAt(numPairs, 4));
            bytes.add(byteAt(numPairs, 3));
            bytes.add(byteAt(numPairs, 2));
            bytes.add(byteAt(numPairs, 1));
            Set<Entry<Integer, Integer>> matches = matchOffsetPairs.entrySet();
            Iterator<Entry<Integer, Integer>> iter = matches.iterator();
            while (iter.hasNext()) {
                Entry<Integer, Integer> entry = iter.next();
                int match = entry.getKey();
                int offset = entry.getValue();
                bytes.add(byteAt(match, 4));
                bytes.add(byteAt(match, 3));
                bytes.add(byteAt(match, 2));
                bytes.add(byteAt(match, 1));
                bytes.add(byteAt(offset, 4));
                bytes.add(byteAt(offset, 3));
                bytes.add(byteAt(offset, 2));
                bytes.add(byteAt(offset, 1));
            }
            break;
        case GOTO_W:
        case JSR_W:
            bytes.add(byteAt(jumpToOffset, 4));
            bytes.add(byteAt(jumpToOffset, 3));
            bytes.add(byteAt(jumpToOffset, 2));
            bytes.add(byteAt(jumpToOffset, 1));
            break;
        default:
            bytes.add(byteAt(jumpToOffset, 2));
            bytes.add(byteAt(jumpToOffset, 1));
        }
        return bytes;
    }

}

/**
 * Representation for LOAD_STORE1, LOAD_STORE2, LOAD_STORE3 and LOAD_STORE4
 * instructions.
 */

class CLLoadStoreInstruction extends CLInstruction {

    /**
     * Whether this instruction is preceeded by a WIDE instruction; applies only
     * to ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE,
     * ASTORE.
     */
    private boolean isWidened;

    /**
     * A byte (for BIPUSH), a short (for SIPUSH), or a constant pool index for
     * LDC, LDC_W, LDC2_W instructions.
     */
    private int constVal;

    /**
     * Construct a CLLoadStoreInstruction object for LOAD_STORE1 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLLoadStoreInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * Construct a CLLoadStoreInstruction object for LOAD_STORE2 instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param localVariableIndex
     *            index of the local variable to increment.
     * @param isWidened
     *            whether this instruction is preceeded by the WIDE (widening)
     *            instruction.
     */

    public CLLoadStoreInstruction(int opcode, int pc, int localVariableIndex,
            boolean isWidened) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        super.localVariableIndex = localVariableIndex;
        this.isWidened = isWidened;
    }

    /**
     * Construct a CLLoadStoreInstruction object for LOAD_STORE3 and LOAD_STORE4
     * instructions.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     * @param constVal
     *            a byte (for BIPUSH), a short (for SIPUSH), or a constant pool
     *            index for LDC instructions.
     */

    public CLLoadStoreInstruction(int opcode, int pc, int constVal) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
        this.constVal = constVal;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        if (instructionInfo[opcode].operandCount > 0) {
            if (localVariableIndex != IRRELEVANT) {
                if (isWidened) {
                    bytes.add(byteAt(localVariableIndex, 2));
                }
                bytes.add(byteAt(localVariableIndex, 1));
            } else {
                switch (opcode) {
                case BIPUSH:
                case LDC:
                    bytes.add(byteAt(constVal, 1));
                    break;
                case SIPUSH:
                case LDC_W:
                case LDC2_W:
                    bytes.add(byteAt(constVal, 2));
                    bytes.add(byteAt(constVal, 1));
                }
            }
        }
        return bytes;
    }

}

/**
 * Representation for STACK instructions.
 */

class CLStackInstruction extends CLInstruction {

    /**
     * Construct a CLStackInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLStackInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        return bytes;
    }

}

/**
 * Representation for MISC instructions.
 */

class CLMiscInstruction extends CLInstruction {

    /**
     * Construct a CLMiscInstruction object.
     * 
     * @param opcode
     *            the opcode for this instruction.
     * @param pc
     *            index of this instruction within the code array of a method.
     */

    public CLMiscInstruction(int opcode, int pc) {
        super.opcode = opcode;
        super.pc = pc;
        mnemonic = instructionInfo[opcode].mnemonic;
        operandCount = instructionInfo[opcode].operandCount;
        stackUnits = instructionInfo[opcode].stackUnits;
        localVariableIndex = instructionInfo[opcode].localVariableIndex;
    }

    /**
     * @inheritDoc
     */

    public ArrayList<Integer> toBytes() {
        ArrayList<Integer> bytes = new ArrayList<Integer>();
        bytes.add(opcode);
        return bytes;
    }

}

/**
 * This class stores static information about a JVM instruction.
 */

class CLInsInfo {

    /** Opcode for this instruction. */
    public int opcode;

    /** Mnemonic for this instruction. */
    public String mnemonic;

    /** Number of operands for this instruction. */
    public int operandCount;

    /**
     * Words produced - words consumed from the operand stack by this
     * instruction.
     */
    public int stackUnits;

    /**
     * Index of the local variable that this instruction refers to; applies only
     * to instructions that operate on local variables.
     */
    public int localVariableIndex;

    /** The category under which instruction belongs. */
    public Category category;

    /**
     * Construct a CLInsInfo object.
     * 
     * @param opcode
     *            opcode for this instruction.
     * @param mnemonic
     *            name for this instruction.
     * @param operandCount
     *            number of operands for this instruction.
     * @param localVariableIndex
     *            index of the local variable that this instruction refers to.
     * @param stackUnits
     *            words produced - words consumed from the operand stack by this
     *            instruction.
     * @param category
     *            category under which this instruction belogs.
     */

    public CLInsInfo(int opcode, String mnemonic, int operandCount,
            int localVariableIndex, int stackUnits, Category category) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.operandCount = operandCount;
        this.localVariableIndex = localVariableIndex;
        this.stackUnits = stackUnits;
        this.category = category;
    }

}
