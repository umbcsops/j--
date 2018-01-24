// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import static jminusminus.CLConstants.*;
import static jminusminus.CLConstants.Category.*;

/**
 * This class provides a high level interface for creating (in-memory and file
 * based) representation of Java classes.
 * 
 * j-- uses this interface to produce target JVM bytecode from a j-- source
 * program. During the pre-analysis and analysis phases, j-- produces partial
 * (in-memory) classes for the type declarations within the compilation unit,
 * and during the code generation phase, it produces file-based classes for the
 * declarations.
 */

public class CLEmitter {

    /** Name of the class. */
    private String name;

    /**
     * If true, the in-memory representation of the class will be written to the
     * file system. Otherwise, it won't be saved as a file.
     */
    private boolean toFile;

    /** Destination directory for the class. */
    private String destDir;

    /** In-memory representation of the class. */
    private CLFile clFile;

    /** Constant pool of the class. */
    private CLConstantPool constantPool;

    /** Direct super interfaces of the class. */
    private ArrayList<Integer> interfaces;

    /** Fields in the class. */
    private ArrayList<CLFieldInfo> fields;

    /** Attributes of the field last added. */
    private ArrayList<CLAttributeInfo> fAttributes;

    /** Methods in the class. */
    private ArrayList<CLMethodInfo> methods;

    /** Attributes of the method last added. */
    private ArrayList<CLAttributeInfo> mAttributes;

    /** Attributes of the class. */
    private ArrayList<CLAttributeInfo> attributes;

    /** Inner classes of the class. */
    private ArrayList<CLInnerClassInfo> innerClasses;

    /** Code (instruction) section of the method last added. */
    private ArrayList<CLInstruction> mCode;

    /**
     * Table containing exception handlers in the method last added.
     */
    private ArrayList<CLException> mExceptionHandlers;

    /** Access flags of the method last added. */
    private int mAccessFlags;

    /**
     * Index into the constant pool, the item at which specifies the name of the
     * method last added.
     */
    private int mNameIndex;

    /**
     * Index into the constant pool, the item at which specifies the descriptor
     * of the method last added.
     */
    private int mDescriptorIndex;

    /** Number of arguments for the method last added. */
    private int mArgumentCount;

    /** Code attributes of the method last added. */
    private ArrayList<CLAttributeInfo> mCodeAttributes;

    /** Whether the method last added needs closing. */
    private boolean isMethodOpen;

    /**
     * Stores jump labels for the method last added. When a label is created, a
     * mapping from the label to an Integer representing the pc of the next
     * instruction is created. Later on, when the label is added, its Integer
     * value is replaced by the value of pc then.
     */
    private Hashtable<String, Integer> mLabels;

    /** Counter for creating unique jump labels. */
    private int mLabelCount;

    /**
     * Whether there there was an instruction added after the last call to
     * addLabel( String label ). If not, the branch instruction that was added
     * with that label would jump beyond the code section, which is not
     * acceptable to the runtime class loader. Therefore, if this flag is false,
     * we add a NOP instruction at the end of the code section to make the jump
     * valid.
     */
    private boolean mInstructionAfterLabel = false;

    /**
     * Location counter; index of the next instruction within the code section
     * of the method last added.
     */
    private int mPC;

    /** Name of the method last added; used for error reporting. */
    private String eCurrentMethod;

    /**
     * Whether an error occurred while creating/writing the class.
     */
    private boolean errorHasOccurred;

    /**
     * Class loader to use for creating in-memory representation of classes from
     * byte streams.
     */
    private static ByteClassLoader byteClassLoader;

    /**
     * Initialize all variables used for adding a method to the ClassFile
     * structure to their appropriate values.
     */

    private void initializeMethodVariables() {
        mAccessFlags = 0;
        mNameIndex = -1;
        mDescriptorIndex = -1;
        mArgumentCount = 0;
        mPC = 0;
        mAttributes = new ArrayList<CLAttributeInfo>();
        mExceptionHandlers = new ArrayList<CLException>();
        mCode = new ArrayList<CLInstruction>();
        mCodeAttributes = new ArrayList<CLAttributeInfo>();
        mLabels = new Hashtable<String, Integer>();
        mLabelCount = 1;
        mInstructionAfterLabel = false;
    }

    /**
     * Add the method created using addMethod() to the ClassFile structure. This
     * involves adding an instance of CLMethodInfo to ClassFile.methods for the
     * method.
     */

    private void endOpenMethodIfAny() {
        if (isMethodOpen) {
            isMethodOpen = false;
            if (!mInstructionAfterLabel) {
                // Must jump to an instruction
                addNoArgInstruction(NOP);
            }

            // Resolve jump labels in exception handlers
            ArrayList<CLExceptionInfo> exceptionTable = new ArrayList<CLExceptionInfo>();
            for (int i = 0; i < mExceptionHandlers.size(); i++) {
                CLException e = mExceptionHandlers.get(i);
                if (!e.resolveLabels(mLabels)) {
                    reportEmitterError(
                            "%s: Unable to resolve exception handler "
                                    + "label(s)", eCurrentMethod);
                }

                // We allow catchType to be null (mapping to
                // index 0),
                // implying this exception handler is called for
                // all
                // exceptions. This is used to implement
                // "finally"
                int catchTypeIndex = (e.catchType == null) ? 0 : constantPool
                        .constantClassInfo(e.catchType);
                CLExceptionInfo c = new CLExceptionInfo(e.startPC, e.endPC,
                        e.handlerPC, catchTypeIndex);
                exceptionTable.add(c);
            }

            // Convert Instruction objects to bytes
            ArrayList<Integer> byteCode = new ArrayList<Integer>();
            int maxLocals = mArgumentCount;
            for (int i = 0; i < mCode.size(); i++) {
                CLInstruction instr = mCode.get(i);

                // Compute maxLocals
                int localVariableIndex = instr.localVariableIndex();
                switch (instr.opcode()) {
                case LLOAD:
                case LSTORE:
                case DSTORE:
                case DLOAD:
                case LLOAD_0:
                case LLOAD_1:
                case LLOAD_2:
                case LLOAD_3:
                case LSTORE_0:
                case LSTORE_1:
                case LSTORE_2:
                case LSTORE_3:
                case DLOAD_0:
                case DLOAD_1:
                case DLOAD_2:
                case DLOAD_3:
                case DSTORE_0:
                case DSTORE_1:
                case DSTORE_2:
                case DSTORE_3:
                    // Each long and double occupies two slots in
                    // the
                    // local variable table
                    localVariableIndex++;
                }
                maxLocals = Math.max(maxLocals, localVariableIndex + 1);

                // Resolve jump labels in flow control
                // instructions
                if (instr instanceof CLFlowControlInstruction) {
                    if (!((CLFlowControlInstruction) instr)
                            .resolveLabels(mLabels)) {
                        reportEmitterError(
                                "%s: Unable to resolve jump label(s)",
                                eCurrentMethod);
                    }
                }

                byteCode.addAll(instr.toBytes());
            }

            // Code attribute; add only if method is neither
            // native
            // nor abstract
            if (!((mAccessFlags & ACC_NATIVE) == ACC_NATIVE || (mAccessFlags & ACC_ABSTRACT) == ACC_ABSTRACT)) {
                addMethodAttribute(codeAttribute(byteCode, exceptionTable,
                        stackDepth(), maxLocals));
            }

            methods.add(new CLMethodInfo(mAccessFlags, mNameIndex,
                    mDescriptorIndex, mAttributes.size(), mAttributes));
        }

        // This method could be the last method, so we need
        // the following wrap up code

        // Add the InnerClass attribute if this class has inner
        // classes
        if (innerClasses.size() > 0) {
            addClassAttribute(innerClassesAttribute());
        }

        // Set the members of the ClassFile structure to their
        // appropriate values
        clFile.constantPoolCount = constantPool.size() + 1;
        clFile.constantPool = constantPool;
        clFile.interfacesCount = interfaces.size();
        clFile.interfaces = interfaces;
        clFile.fieldsCount = fields.size();
        clFile.fields = fields;
        clFile.methodsCount = methods.size();
        clFile.methods = methods;
        clFile.attributesCount = attributes.size();
        clFile.attributes = attributes;
    }

    /**
     * Add a field.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param type
     *            type descriptor for the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param c
     *            index into the constant pool at which there is a
     *            ConstantIntegerInfo, ConstantFloatInfo, ConstantLongInfo,
     *            ConstantDoubleInfo or ConstantStringInfo object. c is -1 if
     *            the field does not have an initialization.
     */

    private void addFieldInfo(ArrayList<String> accessFlags, String name,
            String type, boolean isSynthetic, int c) {
        if (!validTypeDescriptor(type)) {
            reportEmitterError("'%s' is not a valid type descriptor for field",
                    type);
        }
        int flags = 0;
        int nameIndex = constantPool.constantUtf8Info(name);
        int descriptorIndex = constantPool.constantUtf8Info(type);
        fAttributes = new ArrayList<CLAttributeInfo>();
        if (accessFlags != null) {
            for (int i = 0; i < accessFlags.size(); i++) {
                flags |= CLFile.accessFlagToInt(accessFlags.get(i));
            }
        }
        if (isSynthetic) {
            addFieldAttribute(syntheticAttribute());
        }
        if (c != -1) {
            addFieldAttribute(constantValueAttribute(c));
        }
        fields.add(new CLFieldInfo(flags, nameIndex, descriptorIndex,
                fAttributes.size(), fAttributes));
    }

    /**
     * Return the number of units a type with the specified descriptor produces
     * or consumes from the operand stack. 0 is returned if the specified
     * descriptor is invalid.
     * 
     * @param descriptor
     *            a type descriptor.
     * @return the number of units produced or consumed in/from the operand
     *         stack.
     */

    private int typeStackResidue(String descriptor) {
        int i = 0;
        char c = descriptor.charAt(0);
        switch (c) {
        case 'B':
        case 'C':
        case 'I':
        case 'F':
        case 'L':
        case 'S':
        case 'Z':
        case '[':
            i = 1;
            break;
        case 'J':
        case 'D':
            i = 2;
            break;
        }
        return i;
    }

    /**
     * Return the difference between the number of units consumed from the
     * operand stack by a method with the specified descriptor and the number of
     * units produced by the method in the operand stack. 0 is returned if the
     * descriptor is invalid.
     * 
     * @param descriptor
     *            a method descriptor.
     * @return number of units consumed from the operand stack - number of units
     *         produced in the stack.
     */

    private int methodStackResidue(String descriptor) {
        int i = 0;

        // Extract types of arguments and the return type from
        // the method descriptor
        String argTypes = descriptor.substring(1, descriptor.lastIndexOf(")"));
        String returnType = descriptor
                .substring(descriptor.lastIndexOf(")") + 1);

        // Units consumed
        for (int j = 0; j < argTypes.length(); j++) {
            char c = argTypes.charAt(j);
            switch (c) {
            case 'B':
            case 'C':
            case 'I':
            case 'F':
            case 'S':
            case 'Z':
                i -= 1;
                break;
            case '[':
                break;
            case 'J':
            case 'D':
                i -= 2;
                break;
            case 'L':
                int k = argTypes.indexOf(";", j);
                j = k;
                i -= 1;
                break;
            }
        }

        // Units produced
        i += typeStackResidue(returnType);
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
     * Return true if the specified name is in the internal form of a fully
     * qualified class or interface name [JVMS 4.3], false otherwise.
     * 
     * @param name
     *            fully qualified class name in internal form.
     * @return true if the specified name is in the internal form of a fully
     *         qualified class or interface name, false otherwise.
     */

    private boolean validInternalForm(String name) {
        if ((name == null) || name.equals("") || name.startsWith("/")
                || name.endsWith("/")) {
            return false;
        }
        StringTokenizer t = new StringTokenizer(name, "/");
        while (t.hasMoreTokens()) {
            String s = t.nextToken();
            for (int i = 0; i < s.length(); i++) {
                if (i == 0) {
                    if (!Character.isJavaIdentifierStart(s.charAt(i))) {
                        return false;
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return true if the specified descriptor is a valid type descriptor [JVMS
     * 4.4.2], false otherwise.
     * 
     * @param descriptor
     *            type descriptor.
     * @return true if the specified descriptor is a valid type descriptor,
     *         false otherwise.
     */

    private boolean validTypeDescriptor(String descriptor) {
        if (descriptor != null) {
            try {
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
                    return (descriptor.length() == 1);
                case 'L':
                    if (descriptor.endsWith(";")) {
                        return validInternalForm(descriptor.substring(1,
                                descriptor.length() - 1));
                    }
                    return false;
                case '[':
                    return validTypeDescriptor(descriptor.substring(1));
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Return true if the specified descriptor is a valid method descriptor
     * [JVMS 4.4.3], false otherwise.
     * 
     * @param descriptor
     *            method descriptor.
     * @return true if the specified descriptor is a valid method descriptor,
     *         false otherwise.
     */

    private boolean validMethodDescriptor(String descriptor) {
        if ((descriptor != null) && (descriptor.length() > 0)) {
            try {
                // Extract types of arguments and the return type
                // from
                // the method descriptor
                String argTypes = descriptor.substring(1, descriptor
                        .lastIndexOf(")"));
                String returnType = descriptor.substring(descriptor
                        .lastIndexOf(")") + 1);

                // Validate argument type syntax
                if (argTypes.endsWith("[")) {
                    return false;
                }
                for (int i = 0; i < argTypes.length(); i++) {
                    char c = argTypes.charAt(i);
                    switch (c) {
                    case 'B':
                    case 'C':
                    case 'I':
                    case 'F':
                    case 'S':
                    case 'Z':
                    case 'J':
                    case 'D':
                    case '[':
                        break;
                    case 'L':
                        int j = argTypes.indexOf(";", i);
                        String s = argTypes.substring(i, j + 1);
                        i = j;
                        if (!validTypeDescriptor(s)) {
                            return false;
                        }
                        break;
                    default:
                        return false;
                    }
                }

                // Validate return type syntax
                return (returnType.equals("V") || validTypeDescriptor(returnType));
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Return the instruction with the specified pc within the code array of the
     * current method being added.
     * 
     * @param pc
     *            pc of the instruction.
     * @return the instruction with the specified pc, or null.
     */

    private CLInstruction instruction(int pc) {
        for (int j = 0; j < mCode.size(); j++) {
            CLInstruction i = mCode.get(j);
            if (i.pc() == pc) {
                return i;
            }
        }
        return null;
    }

    /**
     * Return the index of the instruction with the specified pc, within the
     * code array of the current method being added.
     * 
     * @param pc
     *            pc of the instruction.
     * @return index of the instruction with the specified pc.
     */

    private int instructionIndex(int pc) {
        int j = 0;
        for (; j < mCode.size(); j++) {
            CLInstruction i = mCode.get(j);
            if (i.pc() == pc) {
                return j;
            }
        }
        return j;
    }

    /**
     * Compute the maximum depth of the operand stack for the method last added,
     * and return the value.
     * 
     * @return maximum depth of operand stack.
     */

    private int stackDepth() {
        CLBranchStack branchTargets = new CLBranchStack();
        for (int i = 0; i < mExceptionHandlers.size(); i++) {
            CLException e = mExceptionHandlers.get(i);
            CLInstruction h = instruction(e.handlerPC);
            if (h != null) {
                // 1 because the exception that is thrown is
                // pushed
                // on top of the operand stack
                branchTargets.push(h, 1);
            }
        }
        int stackDepth = 0, maxStackDepth = 0, c = 0;
        CLInstruction instr = (mCode.size() == 0) ? null : mCode.get(c);
        while (instr != null) {
            int opcode = instr.opcode();
            int stackUnits = instr.stackUnits();
            if (stackUnits == EMPTY_STACK) {
                stackDepth = 0;
            } else if (stackUnits == UNIT_SIZE_STACK) {
                stackDepth = 1;
            } else {
                stackDepth += stackUnits;
            }
            if (stackDepth > maxStackDepth) {
                maxStackDepth = stackDepth;
            }

            // For tracing purposes
            // System.out.println( instr.mnemonic() + ", " +
            // stackUnits + ", " +
            // stackDepth + ", " + maxStackDepth );

            if (instr instanceof CLFlowControlInstruction) {
                CLFlowControlInstruction b = (CLFlowControlInstruction) instr;
                int jumpToIndex = b.pc() + b.jumpToOffset();
                CLInstruction instrAt = null;
                switch (opcode) {
                case JSR:
                case JSR_W:
                case RET:
                    instr = null;
                    break;
                case GOTO:
                case GOTO_W:
                    instr = null;
                default:
                    instrAt = instruction(jumpToIndex);
                    if (instrAt != null) {
                        branchTargets.push(instrAt, stackDepth);
                    }
                }
            } else {
                if ((opcode == ATHROW)
                        || ((opcode >= IRETURN) && (opcode <= RETURN))) {
                    instr = null;
                }
            }
            if (instr != null) {
                c++;
                instr = (c >= mCode.size()) ? null : mCode.get(c);
            }
            if (instr == null) {
                CLBranchTarget bt = branchTargets.pop();
                if (bt != null) {
                    instr = bt.target;
                    stackDepth = bt.stackDepth;
                    c = instructionIndex(instr.pc());
                }
            }
        }
        return maxStackDepth;
    }

    /**
     * Add LDC (LDC_W if index is wide) instruction.
     * 
     * @param index
     *            index into the constant pool, the item at which is a
     *            ConstantIntegerInfo, ConstantFloatInfo or ConstantStringInfo
     *            instance. If index is wide, i.e., greater than 255, LDC_W
     *            version of the instruction is added.
     */

    private void ldcInstruction(int index) {
        CLLoadStoreInstruction instr = null;
        if (index <= 255) {
            instr = new CLLoadStoreInstruction(LDC, mPC++, index);
        } else {
            instr = new CLLoadStoreInstruction(LDC_W, mPC++, index);
        }
        mPC += instr.operandCount();
        mCode.add(instr);
        mInstructionAfterLabel = true;
    }

    /**
     * Add LDC2_W instruction -- used for long and double constants. Note that
     * only a wide-index version of LDC2_W instruction exists.
     * 
     * @param index
     *            index into the constant pool, the item at which is a
     *            ConstantLongInfo or ConstantDoubleInfo item.
     */

    private void ldc2wInstruction(int index) {
        CLLoadStoreInstruction instr = new CLLoadStoreInstruction(LDC2_W,
                mPC++, index);
        mPC += instr.operandCount();
        mCode.add(instr);
        mInstructionAfterLabel = true;
    }

    /**
     * Construct and return a ConstantValue attribute given the constant value
     * index.
     * 
     * @param c
     *            index into the constant pool, the item at which is the
     *            constant value.
     * @return a ConstantValue attribute.
     */

    private CLConstantValueAttribute constantValueAttribute(int c) {
        int attributeNameIndex = constantPool
                .constantUtf8Info(ATT_CONSTANT_VALUE);
        return new CLConstantValueAttribute(attributeNameIndex, 2, c);
    }

    /**
     * Construct and return a Code attribute given the list of bytes that make
     * up the instructions and their operands, exception table, maximum depth of
     * operand stack, and maximum number of local variables.
     * 
     * @param byteCode
     *            list of bytes that make up the instructions and their
     *            operands.
     * @param exceptionTable
     *            exception table.
     * @param stackDepth
     *            maximum depth of operand stack.
     * @param maxLocals
     *            maximum number of local variables.
     * @return a Code attribute.
     */

    private CLCodeAttribute codeAttribute(ArrayList<Integer> byteCode,
            ArrayList<CLExceptionInfo> exceptionTable, int stackDepth,
            int maxLocals) {
        int codeLength = byteCode.size();
        int attributeNameIndex = constantPool.constantUtf8Info(ATT_CODE);
        int attributeLength = codeLength + 8 * exceptionTable.size() + 12;
        for (int i = 0; i < mCodeAttributes.size(); i++) {
            attributeLength += 6 + mCodeAttributes.get(i).attributeLength;
        }
        return new CLCodeAttribute(attributeNameIndex, attributeLength,
                stackDepth, maxLocals, (long) codeLength, byteCode,
                exceptionTable.size(), exceptionTable, mCodeAttributes.size(),
                mCodeAttributes);
    }

    /**
     * Construct and return an ExceptionsAttribute given the list of exceptions.
     * 
     * @param exceptions
     *            list of exceptions in internal form.
     * @return an Exceptions attribute.
     */

    private CLExceptionsAttribute exceptionsAttribute(
            ArrayList<String> exceptions) {
        int attributeNameIndex = constantPool.constantUtf8Info(ATT_EXCEPTIONS);
        ArrayList<Integer> exceptionIndexTable = new ArrayList<Integer>();
        for (int i = 0; i < exceptions.size(); i++) {
            String e = exceptions.get(i);
            exceptionIndexTable.add(new Integer(constantPool
                    .constantClassInfo(e)));
        }
        return new CLExceptionsAttribute(attributeNameIndex,
                exceptionIndexTable.size() * 2 + 2, exceptionIndexTable.size(),
                exceptionIndexTable);
    }

    /**
     * Construct and return InnerClasses attribute.
     * 
     * @return an InnerClasses attribute.
     */

    private CLInnerClassesAttribute innerClassesAttribute() {
        int attributeNameIndex = constantPool
                .constantUtf8Info(ATT_INNER_CLASSES);
        long attributeLength = innerClasses.size() * 8 + 2;
        return new CLInnerClassesAttribute(attributeNameIndex, attributeLength,
                innerClasses.size(), innerClasses);
    }

    /**
     * Construct and return a Synthetic attribute.
     * 
     * @param a
     *            Synthetic attribute.
     */

    private CLAttributeInfo syntheticAttribute() {
        int attributeNameIndex = constantPool.constantUtf8Info(ATT_SYNTHETIC);
        return new CLSyntheticAttribute(attributeNameIndex, 0);
    }

    /**
     * Used to report an error if the opcode used for adding an instruction is
     * invalid, or if an incorrect method from CLEmitter is used to add the
     * opcode.
     * 
     * @param opcode
     *            opcode of the instruction.
     */

    private void reportOpcodeError(int opcode) {
        if (!CLInstruction.isValid(opcode)) {
            reportEmitterError("%s: Invalid opcode '%d'", eCurrentMethod,
                    opcode);
        } else {
            reportEmitterError(
                    "%s: Incorrect method used to add instruction '%s'",
                    eCurrentMethod,
                    CLInstruction.instructionInfo[opcode].mnemonic);
        }
    }

    /**
     * Report any error that occurs while creating/writing the class, to STDERR.
     * 
     * @param message
     *            message identifying the error.
     * @param args
     *            related values.
     */

    private void reportEmitterError(String message, Object... args) {
        System.err.printf(message, args);
        System.err.println();
        errorHasOccurred = true;
    }

    // /////////////////////////////////////////////////////
    // CLEmitter proper begins here
    // /////////////////////////////////////////////////////

    /**
     * Construct a CLEmitter instance.
     * 
     * @param toFile
     *            if true, the in-memory representation of the class file will
     *            be written to the file system. Otherwise, it won't be saved as
     *            a file.
     */

    public CLEmitter(boolean toFile) {
        destDir = ".";
        this.toFile = toFile;
    }

    /**
     * Set the destination directory for the class file to the specified value.
     * 
     * @param destDir
     *            destination directory.
     */

    public void destinationDir(String destDir) {
        this.destDir = destDir;
    }

    /**
     * Has an emitter error occurred up to now?
     * 
     * @return true or false.
     */

    public boolean errorHasOccurred() {
        return errorHasOccurred;
    }

    /**
     * Add a class or interface. This method instantiates a class file
     * representation in memory, so must be called prior to methods that add
     * information (fields, methods, instructions, etc.) to the class.
     * 
     * @param accessFlags
     *            the access flags for the class or interface.
     * @param thisClass
     *            fully qualified name of the class or interface in internal
     *            form.
     * @param superClass
     *            fully qualified name of the parent class in internal form.
     * @param superInterfaces
     *            list of direct super interfaces of this class or interface as
     *            fully qualified names in internal form.
     * @param isSynthetic
     *            whether the class or interface is synthetic.
     */

    public void addClass(ArrayList<String> accessFlags, String thisClass,
            String superClass, ArrayList<String> superInterfaces,
            boolean isSynthetic) {
        clFile = new CLFile();
        constantPool = new CLConstantPool();
        interfaces = new ArrayList<Integer>();
        fields = new ArrayList<CLFieldInfo>();
        methods = new ArrayList<CLMethodInfo>();
        attributes = new ArrayList<CLAttributeInfo>();
        innerClasses = new ArrayList<CLInnerClassInfo>();
        errorHasOccurred = false;
        clFile.magic = MAGIC;
        clFile.majorVersion = MAJOR_VERSION;
        clFile.minorVersion = MINOR_VERSION;
        if (!validInternalForm(thisClass)) {
            reportEmitterError("'%s' is not in internal form", thisClass);
        }
        if (!validInternalForm(superClass)) {
            reportEmitterError("'%s' is not in internal form", superClass);
        }
        if (accessFlags != null) {
            for (int i = 0; i < accessFlags.size(); i++) {
                clFile.accessFlags |= CLFile
                        .accessFlagToInt(accessFlags.get(i));
            }
        }
        name = thisClass;
        clFile.thisClass = constantPool.constantClassInfo(thisClass);
        clFile.superClass = constantPool.constantClassInfo(superClass);
        for (int i = 0; superInterfaces != null && i < superInterfaces.size(); i++) {
            if (!validInternalForm(superInterfaces.get(i))) {
                reportEmitterError("'%s' is not in internal form",
                        superInterfaces.get(i));
            }
            interfaces.add(new Integer(constantPool
                    .constantClassInfo(superInterfaces.get(i))));
        }
        if (isSynthetic) {
            addClassAttribute(syntheticAttribute());
        }
    }

    /**
     * Add an inner class. Note that this only registers the inner class with
     * its parent and does not create the class.
     * 
     * @param accessFlags
     *            access flags for the inner class.
     * @param innerClass
     *            fully qualified name of the inner class in internal form.
     * @param outerClass
     *            fully qualified name of the outer class in internal form.
     * @param innerName
     *            simple name of the inner class.
     */

    public void addInnerClass(ArrayList<String> accessFlags, String innerClass,
            String outerClass, String innerName) {
        int flags = 0;
        if (accessFlags != null) {
            for (int j = 0; j < accessFlags.size(); j++) {
                flags |= CLFile.accessFlagToInt(accessFlags.get(j));
            }
        }
        CLInnerClassInfo innerClassInfo = new CLInnerClassInfo(constantPool
                .constantClassInfo(innerClass), constantPool
                .constantClassInfo(outerClass), constantPool
                .constantUtf8Info(innerName), flags);
        innerClasses.add(innerClassInfo);
    }

    /**
     * Add a field without initialization.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param type
     *            type descriptor of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     */

    public void addField(ArrayList<String> accessFlags, String name,
            String type, boolean isSynthetic) {
        addFieldInfo(accessFlags, name, type, isSynthetic, -1);
    }

    /**
     * Add an int, short, char, byte, or boolean field with initialization. If
     * the field is final, the initialization is added to the constant pool. The
     * initializations are all stored as ints, where boolean true and false are
     * 1 and 0 respectively, and short, char, and byte must be cast to int.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param type
     *            type descriptor of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param i
     *            int value.
     */

    public void addField(ArrayList<String> accessFlags, String name,
            String type, boolean isSynthetic, int i) {
        addFieldInfo(accessFlags, name, type, isSynthetic, constantPool
                .constantIntegerInfo(i));
    }

    /**
     * Add a float field with initialization. If the field is final, the
     * initialization is added to the constant pool.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param f
     *            float value.
     */

    public void addField(ArrayList<String> accessFlags, String name,
            boolean isSynthetic, float f) {
        addFieldInfo(accessFlags, name, "F", isSynthetic, constantPool
                .constantFloatInfo(f));
    }

    /**
     * Add a long field with initialization. If the field is final, the
     * initialization is added to the constant pool.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param l
     *            long value.
     */

    public void addField(ArrayList<String> accessFlags, String name,
            boolean isSynthetic, long l) {
        addFieldInfo(accessFlags, name, "J", isSynthetic, constantPool
                .constantLongInfo(l));
    }

    /**
     * Add a double field with initialization. If the field is final, the
     * initialization is added to the constant pool.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param d
     *            double value.
     */

    public void addField(ArrayList<String> accessFlags, String name,
            boolean isSynthetic, double d) {
        addFieldInfo(accessFlags, name, "D", isSynthetic, constantPool
                .constantDoubleInfo(d));
    }

    /**
     * Add a String type field with initialization. If the field is final, the
     * initialization is added to the constant pool.
     * 
     * @param accessFlags
     *            access flags for the field.
     * @param name
     *            name of the field.
     * @param isSynthetic
     *            is this a synthetic field?
     * @param s
     *            String value.
     */

    public void addField(ArrayList<String> accessFlags, String name,
            boolean isSynthetic, String s) {
        addFieldInfo(accessFlags, name, "Ljava/lang/String;", isSynthetic,
                constantPool.constantStringInfo(s));
    }

    /**
     * Add a method. Instructions can subsequently be added to this method using
     * the appropriate methods for adding instructions.
     * 
     * @param accessFlags
     *            access flags for the method.
     * @param name
     *            name of the method.
     * @param descriptor
     *            descriptor specifying the return type and the types of the
     *            formal parameters of the method.
     * @param exceptions
     *            exceptions thrown by the method, each being a name in fully
     *            qualified internal form.
     * @param isSynthetic
     *            whether this is a synthetic method?
     */

    public void addMethod(ArrayList<String> accessFlags, String name,
            String descriptor, ArrayList<String> exceptions, boolean isSynthetic) {
        if (!validMethodDescriptor(descriptor)) {
            reportEmitterError(
                    "'%s' is not a valid type descriptor for method",
                    descriptor);
        }
        endOpenMethodIfAny(); // close any previous method
        isMethodOpen = true;
        initializeMethodVariables();
        eCurrentMethod = name + descriptor;
        if (accessFlags != null) {
            for (int i = 0; i < accessFlags.size(); i++) {
                mAccessFlags |= CLFile.accessFlagToInt(accessFlags.get(i));
            }
        }
        mArgumentCount = argumentCount(descriptor)
                + (accessFlags.contains("static") ? 0 : 1);
        mNameIndex = constantPool.constantUtf8Info(name);
        mDescriptorIndex = constantPool.constantUtf8Info(descriptor);
        if (exceptions != null && exceptions.size() > 0) {
            addMethodAttribute(exceptionsAttribute(exceptions));
        }
        if (isSynthetic) {
            addMethodAttribute(syntheticAttribute());
        }
    }

    /**
     * Add an exception handler.
     * 
     * @param startLabel
     *            the exception handler is active from the instruction following
     *            this label in the code section of the current method being
     *            added ...
     * @param endLabel
     *            to the instruction following this label. Formally, the handler
     *            is active while the program counter is within the interval
     *            [startLabel, endLabel).
     * @param handlerLabel
     *            the handler begins with instruction following this label.
     * @param catchType
     *            the exception type that this exception handler is designated
     *            to catch, as a fully qualified name in internal form. If null,
     *            this exception handler is called for all exceptions; this is
     *            used to immplement "finally".
     */

    public void addExceptionHandler(String startLabel, String endLabel,
            String handlerLabel, String catchType) {
        if (catchType != null && !validInternalForm(catchType)) {
            reportEmitterError("'%s' is not in internal form", catchType);
        }
        CLException e = new CLException(startLabel, endLabel, handlerLabel,
                catchType);
        mExceptionHandlers.add(e);
    }

    /**
     * Add a no argument instruction. Following instructions can be added using
     * this method:
     * 
     * <p/>
     * Arithmetic Instructions:
     * 
     * <pre>
     *   IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, 
     *   LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, 
     *   DREM, INEG, LNEG, FNEG, DNEG
     * </pre>
     * 
     * <p/>
     * Array Instructions:
     * 
     * <pre>
     *   IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, 
     *   SALOAD, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, 
     *   BASTORE, CASTORE, SASTORE, ARRAYLENGTH
     * </pre>
     * 
     * <p/>
     * Bit Instructions:
     * 
     * <pre>
     *   ISHL, ISHR, IUSHR, LSHL, LSHR, LUSHR, IOR, LOR, IAND, LAND, 
     *   IXOR, LXOR
     * </pre>
     * 
     * <p/>
     * Comparison Instructions:
     * 
     * <pre>
     *   DCMPG, DCMPL, FCMPG, FCMPL, LCMP
     * </pre>
     * 
     * <p/>
     * Conversion Instructions:
     * 
     * <pre>
     *   I2B, I2C, I2S, I2L, I2F, I2D, L2F, L2D, L2I, F2D, F2I, 
     *   F2L, D2I, D2L, D2F
     * </pre>
     * 
     * <p/>
     * Load Store Instructions:
     * 
     * <pre>
     *   ILOAD_0, ILOAD_1, ILOAD_2, ILOAD_3, LLOAD_0, LLOAD_1, 
     *   LLOAD_2, LLOAD_3, FLOAD_0, FLOAD_1, FLOAD_2, FLOAD_3, 
     *   DLOAD_0, DLOAD_1, DLOAD_2, DLOAD_3, ALOAD_0, ALOAD_1, 
     *   ALOAD_2, ALOAD_3, ISTORE_0, ISTORE_1, ISTORE_2, ISTORE_3, 
     *   LSTORE_0, LSTORE_1, LSTORE_2, LSTORE_3, FSTORE_0, FSTORE_1, 
     *   FSTORE_2, FSTORE_3, DSTORE_0, DSTORE_1, DSTORE_2, DSTORE_3, 
     *   ASTORE_0, ASTORE_1, ASTORE_2, ASTORE_3, ICONST_0, ICONST_1, 
     *   ICONST_2, ICONST_3, ICONST_4, ICONST_5, ICONST_M1, LCONST_0, 
     *   LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, 
     *   ACONST_NULL, WIDE (added automatically where necesary)
     * </pre>
     * 
     * <p/>
     * Method Instructions:
     * 
     * <pre>
     *   IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN
     * </pre>
     * 
     * <p/>
     * Stack Instructions:
     * 
     * <pre>
     *   POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP
     * </pre>
     * 
     * <p/>
     * Miscellaneous Instructions:
     * 
     * <pre>
     *   NOP, ATHROW, MONITORENTER, MONITOREXIT
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     */

    public void addNoArgInstruction(int opcode) {
        CLInstruction instr = null;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case ARITHMETIC1:
            instr = new CLArithmeticInstruction(opcode, mPC++);
            break;
        case ARRAY2:
            instr = new CLArrayInstruction(opcode, mPC++);
            break;
        case BIT:
            instr = new CLBitInstruction(opcode, mPC++);
            break;
        case COMPARISON:
            instr = new CLComparisonInstruction(opcode, mPC++);
            break;
        case CONVERSION:
            instr = new CLConversionInstruction(opcode, mPC++);
            break;
        case LOAD_STORE1:
            instr = new CLLoadStoreInstruction(opcode, mPC++);
            break;
        case METHOD2:
            instr = new CLMethodInstruction(opcode, mPC++);
            break;
        case MISC:
            instr = new CLMiscInstruction(opcode, mPC++);
            break;
        case STACK:
            instr = new CLStackInstruction(opcode, mPC++);
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
            mInstructionAfterLabel = true;
        }
    }

    /**
     * Add a one argument instruction. Wideable instructions are widened if
     * necessary by adding a WIDE instruction before the instruction. Following
     * instructions can be added using this method:
     * 
     * <p/>
     * Load Store Instructions:
     * 
     * <pre>
     *   ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, 
     *   DSTORE, ASTORE, BIPUSH, SIPUSH
     * </pre>
     * 
     * <p/>
     * Flow Control Instructions:
     * 
     * <pre>
     * RET
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     * @param arg
     *            the argument. For the instructions that deal with local
     *            variables, the argument is the local variable index; for
     *            BIPUSH and SIPUSH instructions, the argument is the constant
     *            byte or short value.
     */

    public void addOneArgInstruction(int opcode, int arg) {
        CLInstruction instr = null;
        boolean isWidened = false;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case LOAD_STORE2:
            isWidened = arg > 255;
            if (isWidened) {
                CLLoadStoreInstruction wideInstr = new CLLoadStoreInstruction(
                        WIDE, mPC++);
                mCode.add(wideInstr);
            }
            instr = new CLLoadStoreInstruction(opcode, mPC++, arg, isWidened);
            break;
        case LOAD_STORE3:
            instr = new CLLoadStoreInstruction(opcode, mPC++, arg);
            break;
        case FLOW_CONTROL2:
            isWidened = arg > 255;
            if (isWidened) {
                CLLoadStoreInstruction wideInstr = new CLLoadStoreInstruction(
                        WIDE, mPC++);
                mCode.add(wideInstr);
            }
            instr = new CLFlowControlInstruction(mPC++, arg, isWidened);
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
            mInstructionAfterLabel = true;
        }
    }

    /**
     * Add an IINC instruction to increment a variable by a constant. The
     * instruction is widened if necessary by adding a WIDE instruction before
     * the instruction.
     * 
     * @param index
     *            local variable index.
     * @param constVal
     *            increment value.
     */

    public void addIINCInstruction(int index, int constVal) {
        boolean isWidened = index > 255 || constVal < Byte.MIN_VALUE
                || constVal > Byte.MAX_VALUE;
        if (isWidened) {
            CLLoadStoreInstruction wideInstr = new CLLoadStoreInstruction(WIDE,
                    mPC++);
            mCode.add(wideInstr);
        }
        CLArithmeticInstruction instr = new CLArithmeticInstruction(IINC,
                mPC++, index, constVal, isWidened);
        mPC += instr.operandCount();
        mCode.add(instr);
        mInstructionAfterLabel = true;
    }

    /**
     * Add a member (field & method) access instruction. Following instructions
     * can be added using this method:
     * 
     * <p/>
     * Field Instructions:
     * 
     * <pre>
     *   GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD
     * </pre>
     * 
     * <p/>
     * Method Instructions:
     * 
     * <pre>
     *   INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, 
     *   INVOKEDYNAMIC
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     * @param target
     *            fully qualified name in internal form of the class to which
     *            the member belongs.
     * @param name
     *            name of the member.
     * @param type
     *            type descriptor of the member.
     */

    public void addMemberAccessInstruction(int opcode, String target,
            String name, String type) {
        if (!validInternalForm(target)) {
            reportEmitterError("%s: '%s' is not in internal form",
                    eCurrentMethod, target);
        }
        CLInstruction instr = null;
        int index, stackUnits;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case FIELD:
            if (!validTypeDescriptor(type)) {
                reportEmitterError(
                        "%s: '%s' is not a valid type descriptor for field",
                        eCurrentMethod, type);
            }
            index = constantPool.constantFieldRefInfo(target, name, type);
            stackUnits = typeStackResidue(type);
            if ((opcode == GETFIELD) || (opcode == PUTFIELD)) {
                // This is because target of this method is also
                // consumed from the operand stack
                stackUnits--;
            }
            instr = new CLFieldInstruction(opcode, mPC++, index, stackUnits);
            break;
        case METHOD1:
            if (!validMethodDescriptor(type)) {
                reportEmitterError(
                        "%s: '%s' is not a valid type descriptor for "
                                + "method", eCurrentMethod, type);
            }
            if (opcode == INVOKEINTERFACE) {
                index = constantPool.constantInterfaceMethodRefInfo(target,
                        name, type);
            } else {
                index = constantPool.constantMethodRefInfo(target, name, type);
            }
            stackUnits = methodStackResidue(type);
            if (opcode != INVOKESTATIC) {
                // This is because target of this method is also
                // consumed from the operand stack
                stackUnits--;
            }
            instr = new CLMethodInstruction(opcode, mPC++, index, stackUnits);

            // INVOKEINTERFACE expects the number of arguments in
            // the method to be specified explicitly.
            if (opcode == INVOKEINTERFACE) {
                // We add 1 to account for "this"
                ((CLMethodInstruction) instr)
                        .setArgumentCount(argumentCount(type) + 1);
            }
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
        }
    }

    /**
     * Add a reference (object) instruction. Following instructions can be added
     * using this method:
     * 
     * <pre>
     *   NEW, CHECKCAST, INSTANCEOF
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     * @param type
     *            reference type in internal form.
     */

    public void addReferenceInstruction(int opcode, String type) {
        if (!validTypeDescriptor(type) && !validInternalForm(type)) {
            reportEmitterError("%s: '%s' is neither a type descriptor nor in "
                    + "internal form", eCurrentMethod, type);
        }
        CLInstruction instr = null;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case OBJECT:
            int index = constantPool.constantClassInfo(type);
            instr = new CLObjectInstruction(opcode, mPC++, index);
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
        }
    }

    /**
     * Add an array instruction. Following instructions can be added using this
     * method:
     * 
     * <pre>
     *   NEWARRAY, ANEWARRAY
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     * @param type
     *            array type. In case of NEWARRAY, the primitive types are
     *            specified as: "Z" for boolean, "C" for char, "F" for float,
     *            "D" for double, "B" for byte, "S" for short, "I" for int, "J"
     *            for long. In case of ANEWARRAY, reference types are specified
     *            in internal form.
     */

    public void addArrayInstruction(int opcode, String type) {
        CLInstruction instr = null;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case ARRAY1:
            int index = 0;
            if (opcode == NEWARRAY) {
                if (type.equalsIgnoreCase("Z")) {
                    index = 4;
                } else if (type.equalsIgnoreCase("C")) {
                    index = 5;
                } else if (type.equalsIgnoreCase("F")) {
                    index = 6;
                } else if (type.equalsIgnoreCase("D")) {
                    index = 7;
                } else if (type.equalsIgnoreCase("B")) {
                    index = 8;
                } else if (type.equalsIgnoreCase("S")) {
                    index = 9;
                } else if (type.equalsIgnoreCase("I")) {
                    index = 10;
                } else if (type.equalsIgnoreCase("J")) {
                    index = 11;
                } else {
                    reportEmitterError(
                            "%s: '%s' is not a valid primitive type",
                            eCurrentMethod, type);
                }
            } else {
                if (!validTypeDescriptor(type) && !validInternalForm(type)) {
                    reportEmitterError(
                            "%s: '%s' is not a valid type descriptor "
                                    + "for an array", eCurrentMethod, type);
                }
                index = constantPool.constantClassInfo(type);
            }
            instr = new CLArrayInstruction(opcode, mPC++, index);
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
        }
    }

    /**
     * Add a MULTIANEWARRAY instruction for creating multi-dimensional arrays.
     * 
     * @param type
     *            array type in internal form.
     * @param dim
     *            number of dimensions.
     */

    public void addMULTIANEWARRAYInstruction(String type, int dim) {
        CLInstruction instr = null;
        if (!validTypeDescriptor(type)) {
            reportEmitterError(
                    "%s: '%s' is not a valid type descriptor for an array",
                    eCurrentMethod, type);
        }
        int index = constantPool.constantClassInfo(type);
        instr = new CLArrayInstruction(MULTIANEWARRAY, mPC++, index, dim);
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
        }
    }

    /**
     * Add a branch instruction. Following instructions can be added using this
     * method:
     * 
     * <pre>
     *   IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, 
     *   IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, 
     *   IF_ACMPNE, GOTO, JSR, IF_NULL, IF_NONNULL, GOTO_W, JSR_W
     * </pre>
     * 
     * The opcodes for instructions are defined in CLConstants class.
     * 
     * @param opcode
     *            opcode of the instruction.
     * @param label
     *            branch label.
     */

    public void addBranchInstruction(int opcode, String label) {
        CLInstruction instr = null;
        switch (CLInstruction.instructionInfo[opcode].category) {
        case FLOW_CONTROL1:
            instr = new CLFlowControlInstruction(opcode, mPC++, label);
            break;
        default:
            reportOpcodeError(opcode);
        }
        if (instr != null) {
            mPC += instr.operandCount();
            mCode.add(instr);
            mInstructionAfterLabel = true;
        }
    }

    /**
     * Add a TABLESWITCH instruction -- used for switch statements.
     * 
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

    public void addTABLESWITCHInstruction(String defaultLabel, int low,
            int high, ArrayList<String> labels) {
        CLFlowControlInstruction instr = new CLFlowControlInstruction(
                TABLESWITCH, mPC++, defaultLabel, low, high, labels);
        mPC += instr.operandCount();
        mCode.add(instr);
        mInstructionAfterLabel = true;
    }

    /**
     * Add a LOOKUPSWITCH instruction -- used for switch statements.
     * 
     * @param defaultLabel
     *            jump label for default value.
     * @param numPairs
     *            number of pairs in the match table.
     * @param matchLabelPairs
     *            key match table.
     */

    public void addLOOKUPSWITCHInstruction(String defaultLabel, int numPairs,
            TreeMap<Integer, String> matchLabelPairs) {
        CLFlowControlInstruction instr = new CLFlowControlInstruction(
                LOOKUPSWITCH, mPC++, defaultLabel, numPairs, matchLabelPairs);
        mPC += instr.operandCount();
        mCode.add(instr);
        mInstructionAfterLabel = true;
    }

    /**
     * Add an LDC instruction to load an int constant on the operand stack.
     * 
     * @param i
     *            int constant.
     */

    public void addLDCInstruction(int i) {
        ldcInstruction(constantPool.constantIntegerInfo(i));
    }

    /**
     * Add an LDC instruction to load a float constant on the operand stack.
     * 
     * @param f
     *            float constant.
     */

    public void addLDCInstruction(float f) {
        ldcInstruction(constantPool.constantFloatInfo(f));
    }

    /**
     * Add an LDC instruction to load a long constant on the operand stack.
     * 
     * @param l
     *            long constant.
     */

    public void addLDCInstruction(long l) {
        ldc2wInstruction(constantPool.constantLongInfo(l));
    }

    /**
     * Add an LDC instruction to load a double constant on the operand stack.
     * 
     * @param d
     *            double constant.
     */

    public void addLDCInstruction(double d) {
        ldc2wInstruction(constantPool.constantDoubleInfo(d));
    }

    /**
     * Add an LDC instruction to load a String constant on the operand stack.
     * 
     * @param s
     *            String constant.
     */

    public void addLDCInstruction(String s) {
        ldcInstruction(constantPool.constantStringInfo(s));
    }

    /**
     * Add the specified class attribute to the attribyte section of the class.
     * 
     * @param attribute
     *            class attribute.
     */

    public void addClassAttribute(CLAttributeInfo attribute) {
        if (attributes != null) {
            attributes.add(attribute);
        }
    }

    /**
     * Add the specified method attribute to the attribute section of the method
     * last added.
     * 
     * @param attribute
     *            method attribute.
     */

    public void addMethodAttribute(CLAttributeInfo attribute) {
        if (mAttributes != null) {
            mAttributes.add(attribute);
        }
    }

    /**
     * Add the specified field attribute the attribute section of the field last
     * added.
     * 
     * @param attribute
     *            field attribute.
     */

    public void addFieldAttribute(CLAttributeInfo attribute) {
        if (fAttributes != null) {
            fAttributes.add(attribute);
        }
    }

    /**
     * Add the specified code attribute to the attribute section of the code for
     * the method last added.
     * 
     * @param attribute
     *            code attribute.
     */

    public void addCodeAttribute(CLAttributeInfo attribute) {
        if (mCodeAttributes != null) {
            mCodeAttributes.add(attribute);
        }
    }

    /**
     * Add a jump label to the code section of the method being added. A flow
     * control instruction that was added with this label will jump to the
     * instruction right after the label.
     * 
     * @param label
     *            jump label.
     */

    public void addLabel(String label) {
        mLabels.put(label, mPC);
        mInstructionAfterLabel = false;
    }

    /**
     * Construct and return a unique jump label.
     * 
     * @return unique jump label.
     */

    public String createLabel() {
        return "Label" + mLabelCount++;
    }

    /**
     * Return the pc (location counter). The next instruction will be added with
     * this pc.
     * 
     * @return the pc.
     */

    public int pc() {
        return mPC;
    }

    /**
     * Return the constant pool of the class being built.
     * 
     * @return constant pool.
     */

    public CLConstantPool constantPool() {
        return constantPool;
    }

    /**
     * Set a new ByteClassLoader for loading classes from byte streams.
     */

    public static void initializeByteClassLoader() {
        byteClassLoader = new ByteClassLoader();
    }

    /**
     * Return the CLFile instance corresponding to the class built by this
     * emitter.
     */

    public CLFile clFile() {
        return clFile;
    }

    /**
     * Return the class being constructed as a Java Class instance.
     * 
     * @return Java Class instance.
     */
    public Class toClass() {
        endOpenMethodIfAny();
        Class theClass = null;
        try {
            // Extract the bytes from the class representation in
            // memory into an array of bytes
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            CLOutputStream out = new CLOutputStream(new BufferedOutputStream(
                    byteStream));
            clFile.write(out);
            out.close();
            byte[] classBytes = byteStream.toByteArray();
            byteStream.close();

            // Load a Java Class instance from its byte
            // representation
            byteClassLoader.setClassBytes(classBytes);
            theClass = byteClassLoader.loadClass(name, true);
        } catch (IOException e) {
            reportEmitterError("Cannot write class to byte stream");
        } catch (ClassNotFoundException e) {
            reportEmitterError("Cannot load class from byte stream");
        }
        return theClass;
    }

    /**
     * Write out the class to the file system as a .class file if toFile is
     * true. The destination directory for the file can be set using the
     * destinationDir(String dir) method.
     */

    public void write() {
        endOpenMethodIfAny();
        if (!toFile) {
            return;
        }
        String outFile = destDir + File.separator + name + ".class";
        try {
            File file = new File(destDir + File.separator
                    + name.substring(0, name.lastIndexOf("/") + 1));
            file.mkdirs();
            CLOutputStream out = new CLOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outFile)));
            clFile.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            reportEmitterError("File %s not found", outFile);
        } catch (IOException e) {
            reportEmitterError("Cannot write to file %s", outFile);
        }
    }
}

/**
 * Representation of an exception handler.
 */

class CLException {

    /**
     * The exception handler is active from this instruction in the code section
     * of the current method being added to ...
     */
    public String startLabel;

    /**
     * this instruction. Formally, the handler is active while the program
     * counter is within the interval [startPC, endPC).
     */
    public String endLabel;

    /**
     * Instruction after this label is first instruction of the handler.
     */
    public String handlerLabel;

    /**
     * The class of exceptions that this exception handler is designated to
     * catch.
     */
    public String catchType;

    /** startLabel is resolved to this value. */
    public int startPC;

    /** endLabel is resolved to this value. */
    public int endPC;

    /** handlerLabel is resolved to this value. */
    public int handlerPC;

    /**
     * Construct a CLException object.
     * 
     * @param startLabel
     *            the exception handler is active from the instruction following
     *            this label in the code section of the current method being
     *            added ...
     * @param endLabel
     *            to the instruction following this label. Formally, the handler
     *            is active while the program counter is within the interval
     *            [startLabel, endLabel).
     * @param handlerLabel
     *            the handler begins with instruction following this label.
     * @param catchType
     *            the exception type that this exception handler is designated
     *            to catch, as a fully qualified name in internal form.
     */

    public CLException(String startLabel, String endLabel, String handlerLabel,
            String catchType) {
        this.startLabel = startLabel;
        this.endLabel = endLabel;
        this.handlerLabel = handlerLabel;
        this.catchType = catchType;
    }

    /**
     * Resolve the jump labels to the corresponding pc values using the given
     * label to pc mapping. If unable to resolve a label, the corresponding pc
     * is set to 0.
     * 
     * @param labelToPC
     *            label to pc mapping.
     * @return true if all labels were resolved successfully; false otherwise.
     */

    public boolean resolveLabels(Hashtable<String, Integer> labelToPC) {
        boolean allLabelsResolved = true;
        if (labelToPC.containsKey(startLabel)) {
            startPC = labelToPC.get(startLabel);
        } else {
            startPC = 0;
            allLabelsResolved = false;
        }
        if (labelToPC.containsKey(endLabel)) {
            endPC = labelToPC.get(endLabel);
        } else {
            endPC = 0;
            allLabelsResolved = false;
        }
        if (labelToPC.containsKey(handlerLabel)) {
            handlerPC = labelToPC.get(handlerLabel);
        } else {
            handlerPC = 0;
            allLabelsResolved = false;
        }
        return allLabelsResolved;
    }

}

/**
 * Instances of this class form the elements of the CLBranchStack which is used
 * for control flow analysis to compute maximum depth of operand stack for a
 * method.
 */

class CLBranchTarget {

    /** Target instruction. */
    public CLInstruction target;

    /** Depth of stack before the target instruction is executed. */
    public int stackDepth;

    /**
     * Construct a CLBranchTarget object.
     * 
     * @param target
     *            the target instruction.
     * @param stackDepth
     *            depth of stack before the target instruction is executed.
     */

    public CLBranchTarget(CLInstruction target, int stackDepth) {
        this.target = target;
        this.stackDepth = stackDepth;
    }

}

/**
 * This class is used for control flow analysis to compute maximum depth of
 * operand stack for a method.
 */

class CLBranchStack {

    /** Branch targets yet to visit. */
    private Stack<CLBranchTarget> branchTargets;

    /** Branch targets already visited. */
    private Hashtable<CLInstruction, CLBranchTarget> visitedTargets;

    /**
     * Return an instance of CLBranchTarget with the specified information and
     * record the target as visited.
     * 
     * @param target
     *            the target instruction.
     * @param stackDepth
     *            depth of stack before the target instruction is executed.
     * @return an instance of CLBranchTarget.
     */

    private CLBranchTarget visit(CLInstruction target, int stackDepth) {
        CLBranchTarget bt = new CLBranchTarget(target, stackDepth);
        visitedTargets.put(target, bt);
        return bt;
    }

    /**
     * Return true if the specified instruction has been visited, false
     * otherwise.
     * 
     * @param target
     *            the target instruction.
     * @return true if the specified instruction has been visited, false
     *         otherwise.
     */

    private boolean visited(CLInstruction target) {
        return (visitedTargets.get(target) != null);
    }

    /**
     * Construct a CLBranchStack object.
     */

    public CLBranchStack() {
        this.branchTargets = new Stack<CLBranchTarget>();
        this.visitedTargets = new Hashtable<CLInstruction, CLBranchTarget>();
    }

    /**
     * Push the specified information into the stack as a CLBranchTarget
     * instance if the target has not been visited yet.
     * 
     * @param target
     *            the target instruction.
     * @param stackDepth
     *            depth of stack before the target instruction is executed.
     */

    public void push(CLInstruction target, int stackDepth) {
        if (visited(target)) {
            return;
        }
        branchTargets.push(visit(target, stackDepth));
    }

    /**
     * Pop and return an element from the stack. null is returned if the stack
     * is empty.
     * 
     * @return an element from the stack.
     */

    public CLBranchTarget pop() {
        if (!branchTargets.empty()) {
            CLBranchTarget bt = (CLBranchTarget) branchTargets.pop();
            return bt;
        }
        return null;
    }

}

/**
 * A class loader to be able to load a class from a byte stream.
 */

class ByteClassLoader extends ClassLoader {

    /** Bytes representing the class. */
    private byte[] bytes;

    /** Has a package been defined for this class loader? */
    private boolean pkgDefined = false;

    /**
     * Set the bytes representing the class.
     * 
     * @param bytes
     *            bytes representing the class.
     */

    public void setClassBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @inheritDoc
     */

    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class cls = findLoadedClass(name);
        if (cls == null) {
            try {
                cls = findSystemClass(name);
            } catch (Exception e) {
                // Ignore these
            }
        }
        if (cls == null) {
            name = name.replace("/", ".");
            String pkg = name.lastIndexOf('.') == -1 ? "" : name.substring(0,
                    name.lastIndexOf('.'));
            if (!pkgDefined) {
                // Packages must be created before the class is
                // defined, and package names must be unique
                // within
                // a class loader and cannot be redefined or
                // changed once created
                definePackage(pkg, "", "", "", "", "", "", null);
                pkgDefined = true;
            }
            cls = defineClass(name, bytes, 0, bytes.length);
            if (resolve && cls != null) {
                resolveClass(cls);
            }
        }
        return cls;
    }

}

/**
 * Inherits from java.out.DataOutputStream and provides an extra function for
 * writing unsigned int to the output stream, which is required for writing Java
 * class files.
 */

class CLOutputStream extends DataOutputStream {

    /**
     * Construct a CLOutputStream from the specified output stream.
     * 
     * @param out
     *            output stream.
     */

    public CLOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Write four bytes to the output stream to represent the value of the
     * argument. The byte values to be written, in the order shown, are:
     * 
     * <pre>
     *     (byte) ( 0xFF &amp; ( v &gt;&gt; 24 ) )
     *     (byte) ( 0xFF &amp; ( v &gt;&gt; 16 ) )
     *     (byte) ( 0xFF &amp; ( v &gt;&gt; 8 ) )
     *     (byte) ( 0xFF &amp; v )
     * </pre>
     * 
     * @param v
     *            the int value to be written.
     * @throws IOException
     *             if an error occurs while writing.
     */

    public final void writeInt(long v) throws IOException {
        long mask = 0xFF;
        out.write((byte) (mask & (v >> 24)));
        out.write((byte) (mask & (v >> 16)));
        out.write((byte) (mask & (v >> 8)));
        out.write((byte) (mask & v));
    }

}
