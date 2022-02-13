// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for a binary expression. 
 * A binary expression has an operator and two operands: a lhs and a rhs.
 */

abstract class JBinaryExpression extends JExpression {
  /** The binary operator. */
  protected String operator;

  /** The lhs operand. */
  protected JExpression lhs;

  /** The rhs operand. */
  protected JExpression rhs;

  /**
   * Constructs an AST node for a binary expression given its line number, the
   * binary operator, and lhs and rhs operands.
   * 
   * @param line
   *            line in which the binary expression occurs in the source file.
   * @param operator
   *            the binary operator.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  protected JBinaryExpression(int line, String operator, JExpression lhs,
      JExpression rhs) {
    super(line);
    this.operator = operator;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  /**
   * {@inheritDoc}
   */

  public void writeToStdOut(PrettyPrinter p) {
    p.printf("<JBinaryExpression line=\"%d\" type=\"%s\" "
        + "operator=\"%s\">\n", line(), ((type == null) ? "" : type
          .toString()), Util.escapeSpecialXMLChars(operator));
    p.indentRight();
    p.printf("<Lhs>\n");
    p.indentRight();
    lhs.writeToStdOut(p);
    p.indentLeft();
    p.printf("</Lhs>\n");
    p.printf("<Rhs>\n");
    p.indentRight();
    rhs.writeToStdOut(p);
    p.indentLeft();
    p.printf("</Rhs>\n");
    p.indentLeft();
    p.printf("</JBinaryExpression>\n");
  }

}

/**
 * The AST node for a plus (+) expression. In j--, as in Java, + is overloaded
 * to denote addition for numbers and concatenation for Strings.
 */

class JPlusOp extends JBinaryExpression {

  /**
   * Constructs an AST node for an addition expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the addition expression occurs in the source
   *            file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JPlusOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "+", lhs, rhs);
  }

  /**
   * Analysis involves first analyzing the operands. If this is a string
   * concatenation, we rewrite the subtree to make that explicit (and analyze
   * that). Otherwise we check the types of the addition operands and compute
   * the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    if (lhs.type() == Type.STRING || rhs.type() == Type.STRING) {
      return (new JStringConcatenationOp(line, lhs, rhs))
        .analyze(context);
    } else if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
      type = Type.INT;
    } else {
      type = Type.ANY;
      JAST.compilationUnit.reportSemanticError(line(),
          "Invalid operand types for +");
    }
    return this;
  }

  /**
   * Any string concatenation has been rewritten as a 
   * {@link JStringConcatenationOp} (in {@code analyze}), so code generation 
   * here involves simply generating code for loading the operands onto the 
   * stack and then generating the appropriate add instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    if (type == Type.INT) {
      lhs.codegen(output);
      rhs.codegen(output);
      output.addNoArgInstruction(IADD);
    }
  }
}

/**
 * The AST node for a subtraction (-) expression.
 */

class JSubtractOp extends JBinaryExpression {

  /**
   * Constructs an AST node for a subtraction expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the subtraction expression occurs in the source
   *            file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JSubtractOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "-", lhs, rhs);
  }

  /**
   * Analyzing the - operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the - operation involves generating code for the two
   * operands, and then the subtraction instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(ISUB);
  }

}

/**
 * The AST node for a multiplication (*) expression.
 */

class JMultiplyOp extends JBinaryExpression {
  /**
   * Constructs an AST for a multiplication expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the multiplication expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JMultiplyOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "*", lhs, rhs);
  }

  /**
   * Analyzing the * operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the * operation involves generating code for the two
   * operands, and then the multiplication instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IMUL);
  }
}

class JDivideOp extends JBinaryExpression {
  /**
   * Constructs an AST for a division expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the division expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JDivideOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "/", lhs, rhs);
  }

  /**
   * Analyzing the / operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the / operation involves generating code for the two
   * operands, and then the division instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IDIV);
  }
}

class JModulusOp extends JBinaryExpression {
  /**
   * Constructs an AST for a modulus expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the modulus expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JModulusOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "%", lhs, rhs);
  }

  /**
   * Analyzing the % operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the / operation involves generating code for the two
   * operands, and then the modulus instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IREM);
  }
}


class JShiftLeftOp extends JBinaryExpression {
  /**
   * Constructs an AST for a left shift expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the left shift expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JShiftLeftOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "<<", lhs, rhs);
  }

  /**
   * Analyzing the &lt;&lt; operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the &lt;&lt; operation involves generating code for the two
   * operands, and then the left shift instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(ISHL);
  }
}


class JShiftRightOp extends JBinaryExpression {

  /**
   * Constructs an AST for a right shift expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the right shift expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JShiftRightOp(int line, JExpression lhs, JExpression rhs) {
    super(line, ">>", lhs, rhs);
  }

  /**
   * Analyzing the &rt;&rt; operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the &rt;&rt; operation involves generating code for the two
   * operands, and then the right shift instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(ISHR);
  }
}


class JShiftRightZeroFillOp extends JBinaryExpression {
  /**
   * Constructs an AST for a right shift with zero fill expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the right shift with zero fill expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JShiftRightZeroFillOp(int line, JExpression lhs, JExpression rhs) {
    super(line, ">>>", lhs, rhs);
  }

  /**
   * Analyzing the &rt;&rt;&rt; operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the &rt;&rt;&rt; operation involves generating code for the two
   * operands, and then the right shift with zero fill instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IUSHR);
  }
}

class JBitwiseInclusiveOrOp extends JBinaryExpression {
  /**
   * Constructs an AST for a bitwise inclusive Or expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the bitwise inclusive Or expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JBitwiseInclusiveOrOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "|", lhs, rhs);
  }

  /**
   * Analyzing the | operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the | operation involves generating code for the two
   * operands, and then the bitwise inclusive Or instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IOR);
  }
}

class JBitwiseExclusiveOrOp extends JBinaryExpression {
  /**
   * Constructs an AST for a bitwise exclusive Or expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the bitwise inclusive Or expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JBitwiseExclusiveOrOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "^", lhs, rhs);
  }

  /**
   * Analyzing the ^ operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the ^ operation involves generating code for the two
   * operands, and then the bitwise exclusive Or instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IXOR);
  }
}

class JBitwiseAndOp extends JBinaryExpression {
  /**
   * Constructs an AST for a bitwise And expression given its line number,
   * and the lhs and rhs operands.
   * 
   * @param line
   *            line in which the bitwise inclusive Or expression occurs in the
   *            source file.
   * @param lhs
   *            the lhs operand.
   * @param rhs
   *            the rhs operand.
   */

  public JBitwiseAndOp(int line, JExpression lhs, JExpression rhs) {
    super(line, "&", lhs, rhs);
  }

  /**
   * Analyzing the &amp; operation involves analyzing its operands, checking
   * types, and determining the result type.
   * 
   * @param context
   *            context in which names are resolved.
   * @return the analyzed (and possibly rewritten) AST subtree.
   */

  public JExpression analyze(Context context) {
    lhs = (JExpression) lhs.analyze(context);
    rhs = (JExpression) rhs.analyze(context);
    lhs.type().mustMatchExpected(line(), Type.INT);
    rhs.type().mustMatchExpected(line(), Type.INT);
    type = Type.INT;
    return this;
  }

  /**
   * Generating code for the &amp; operation involves generating code for the two
   * operands, and then the bitwise And instruction.
   * 
   * @param output
   *            the code emitter (basically an abstraction for producing the
   *            .class file).
   */

  public void codegen(CLEmitter output) {
    lhs.codegen(output);
    rhs.codegen(output);
    output.addNoArgInstruction(IAND);
  }
}
