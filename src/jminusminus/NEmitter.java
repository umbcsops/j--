// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A class for generating native SPIM code.
 */

public class NEmitter {

    /** Source program file name. */
    private String sourceFile;

    /**
     * Map of maps, one per class in the compilation unit. Each one of them maps
     * methods in a class to their control flow graph.
     */
    private HashMap<CLFile, HashMap<CLMethodInfo, NControlFlowGraph>> classes;

    /** Destination directory for the native SPIM code. */
    private String destDir;

    /**
     * Whether an error occurred while creating/writing SPIM code.
     */
    private boolean errorHasOccurred;

    /**
     * Report any error that occurs while creating/writing the spim file, to
     * STDERR.
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

    /**
     * Emits SPIM code to setup a stack frame for the procedure denoted by cfg.
     * This involves saving the return address (ra), saving the frame pointer
     * (fp), saving any physical registers (t0, ..., t9, s0, ..., s7) used by
     * the procedure, and setting up the new value for fp (i.e. pushing a stack
     * frame).
     * 
     * @param cfg
     *            the control flow graph instance.
     * @param out
     *            output stream for SPIM code.
     */

    private void pushStackFrame(NControlFlowGraph cfg, PrintWriter out) {
        int frameSize = cfg.pRegisters.size() * 4 + cfg.offset * 4 + 8;
        out.printf(
                "    subu    $sp,$sp,%d \t # Stack frame is %d bytes long\n",
                frameSize, frameSize);
        out.printf("    sw      $ra,%d($sp) \t # Save return address\n",
                frameSize - 4);
        out.printf("    sw      $fp,%d($sp) \t # Save frame pointer\n",
                frameSize - 8);
        int i = 12;
        for (NPhysicalRegister pRegister : cfg.pRegisters) {
            out.printf("    sw      %s,%d($sp) \t # Save register %s\n",
                    pRegister, frameSize - i, pRegister);
            i += 4;
        }
        out.printf("    addiu   $fp,$sp,%d \t # Save frame pointer\n",
                frameSize - 4);
        out.println();
    }

    /**
     * Emits SPIM code to pop the stack frame that was setup for the procedure
     * denoted by cfg. This involves restoring the return address (ra), the
     * frame pointer (fp), any physical registers (t0, ..., t9, s0, ..., s7)
     * used by the procedure, setting fp to the restored value (i.e. popping the
     * stack frame), and finally jumping to ra (the caller).
     * 
     * @param cfg
     *            the control flow graph instance.
     * @param out
     *            output stream for SPIM code.
     */

    private void popStackFrame(NControlFlowGraph cfg, PrintWriter out) {
        int frameSize = cfg.pRegisters.size() * 4 + cfg.offset * 4 + 8;
        out.printf("%s.restore:\n", cfg.labelPrefix);
        out.printf("    lw      $ra,%d($sp) \t # Restore return address\n",
                frameSize - 4);
        out.printf("    lw      $fp,%d($sp) \t # Restore frame pointer\n",
                frameSize - 8);
        int i = 12;
        for (NPhysicalRegister pRegister : cfg.pRegisters) {
            out.printf("    lw      %s,%d($sp) \t # Restore register %s\n",
                    pRegister, frameSize - i, pRegister);
            i += 4;
        }
        out.printf("    addiu   $sp,$sp,%d \t # Pop stack\n", frameSize);
        out.printf("    jr      $ra \t # Return to caller\n", frameSize);
        out.println();
    }

    /**
     * Construct an NEmitter instance.
     * 
     * @param sourceFile
     *            the source j-- program file name.
     * @param clFiles
     *            list of CLFile objects.
     * @param ra
     *            register allocation scheme (naive, linear, or graph).
     */

    public NEmitter(String sourceFile, ArrayList<CLFile> clFiles, String ra) {
        this.sourceFile = sourceFile.substring(sourceFile
                .lastIndexOf(File.separator) + 1);
        classes = new HashMap<CLFile, HashMap<CLMethodInfo, NControlFlowGraph>>();
        for (CLFile clFile : clFiles) {
            CLConstantPool cp = clFile.constantPool;
            HashMap<CLMethodInfo, NControlFlowGraph> methods = new HashMap<CLMethodInfo, NControlFlowGraph>();
            for (int i = 0; i < clFile.methodsCount; i++) {
                CLMethodInfo m = clFile.methods.get(i);

                // Build a control flow graph (cfg) for this method.
                // Each block in the cfg, at the end of this step,
                // has the JVM bytecode translated into tuple
                // representation.
                NControlFlowGraph cfg = new NControlFlowGraph(cp, m);

                // Write the tuples in cfg to STDOUT.
                PrettyPrinter p = new PrettyPrinter();
                p.printf("%s %s\n", cfg.name, cfg.desc);
                cfg.writeTuplesToStdOut(p);

                // Identify blocks in cfg that are loop heads and
                // loop tails. Also, compute number of backward
                // branches to blocks.
                cfg.detectLoops(cfg.basicBlocks.get(0), null);

                // Remove unreachable blocks from cfg.
                cfg.removeUnreachableBlocks();

                // Compute the dominator of each block in the cfg.
                cfg.computeDominators(cfg.basicBlocks.get(0), null);

                // Convert the tuples in each block in the cfg to
                // high-level (HIR) instructions.
                cfg.tuplesToHir();

                // Eliminate redundant phi functions, i.e., replace
                // phi functions of the form x = (y, x, x, ..., x)
                // with y.
                cfg.eliminateRedundantPhiFunctions();

                // Perform optimizations on the high-level
                // instructions.
                cfg.optimize();

                // Write the HIR instructions in cfg to STDOUT.
                cfg.writeHirToStdOut(p);

                // Convert the HIR instructions in each block in the
                // cfg to low-level (LIR) instructions.
                cfg.hirToLir();

                // Resolve phi functions;
                cfg.resolvePhiFunctions();

                // Compute block order.
                cfg.orderBlocks();

                // Assign new ids to LIR instructions.
                cfg.renumberLirInstructions();

                // Write the LIR instructions in cfg to STDOUT.
                cfg.writeLirToStdOut(p);

                // Save the cfg for the method in a map keyed in by
                // the CLMethodInfo object for the method.
                methods.put(m, cfg);

                // Perform register allocation.
                NRegisterAllocator regAllocator;
                if (ra.equals("naive")) {
                    regAllocator = new NNaiveRegisterAllocator(cfg);
                } else if (ra.equals("linear")) {
                    regAllocator = new NLinearRegisterAllocator(cfg);
                } else {
                    regAllocator = new NGraphRegisterAllocator(cfg);
                }
                regAllocator.allocation();

                // Write the intervals in cfg to STDOUT.
                cfg.writeIntervalsToStdOut(p);

                // Replace references to virtual registers in LIR
                // instructions with references to physical registers.
                cfg.allocatePhysicalRegisters();

                // Write the LIR instructions in cfg to STDOUT.
                cfg.writeLirToStdOut(p);
            }

            // Store the cfgs for the methods in this class in a map.
            classes.put(clFile, methods);
        }
    }

    /**
     * Set the destination directory for the SPIM files to the specified value.
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
     * Write out SPIM file(s) to the file system. The destination directory for
     * the files can be set using the destinationDir(String dir) method.
     */

    public void write() {
        String file = "";
        try {
            file = destDir + File.separator + sourceFile.replace(".java", ".s");
            PrintWriter out = new PrintWriter(file);

            // Header.
            out.printf("# %s\n", file);
            out.printf("# Source file: %s\n", sourceFile);
            out.printf("# Compiled: %s\n\n", Calendar.getInstance().getTime()
                    .toString());

            // Translate classes and their methods to SPIM.
            for (CLFile clFile : classes.keySet()) {
                HashMap<CLMethodInfo, NControlFlowGraph> aClass = classes
                        .get(clFile);
                CLConstantPool cp = clFile.constantPool;
                int nameIndex = ((CLConstantClassInfo) cp
                        .cpItem(clFile.thisClass)).nameIndex;
                String className = new String(((CLConstantUtf8Info) cp
                        .cpItem(nameIndex)).b);
                for (CLMethodInfo m : aClass.keySet()) {
                    NControlFlowGraph cfg = aClass.get(m);
                    String methodName = cfg.name;
                    String methodDesc = cfg.desc;
                    if (methodName.equals("<init>")) {
                        continue;
                    }
                    out.printf(".text\n\n");
                    if (methodName.equals("main")
                            && methodDesc.equals("([Ljava/lang/String;)V")) {
                        out.printf("%s:\n", methodName);
                        cfg.labelPrefix = methodName;
                    } else {
                        out.printf("%s.%s:\n", className, methodName);
                        cfg.labelPrefix = className + "." + methodName;
                    }

                    // Setup stack frame for this method
                    pushStackFrame(cfg, out);

                    for (NBasicBlock block : cfg.basicBlocks) {
                        out.printf("%s.%d:\n", cfg.labelPrefix, block.id);
                        for (NLIRInstruction lir : block.lir) {
                            lir.toSpim(out);
                        }
                        out.printf("\n");
                    }

                    // Pop the stack frame for this method.
                    popStackFrame(cfg, out);

                    // Data segment for this cfg storing string
                    // literals.
                    if (cfg.data.size() > 0) {
                        out.printf(".data\n\n");
                        for (String line : cfg.data) {
                            out.printf(line);
                        }
                    }

                    out.printf("\n\n");
                }
            }

            // Emit SPIM runtime code; just SPIM.s for now.
            String[] libs = { "SPIM.s" };
            out.printf("# SPIM Runtime\n\n");
            for (String lib : libs) {
                file = System.getenv("j") + File.separator + "src"
                        + File.separator + "spim" + File.separator + lib;
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line;
                while ((line = in.readLine()) != null) {
                    out.printf("%s\n", line);
                }
                in.close();
            }

            out.close();
        } catch (FileNotFoundException e) {
            reportEmitterError("File %s not found", file);
        } catch (IOException e) {
            reportEmitterError("Cannot write to file %s", file);
        }
    }

}
