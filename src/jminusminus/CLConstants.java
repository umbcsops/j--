// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * Constants used within CL*.java files.
 */

public class CLConstants {

    /**
     * Magic number (0xCAFEBABE) identifying the class file format.
     */
    public static final long MAGIC = 3405691582L;

    /** Major version for the class files that j-- compiles. */
    public static final int MAJOR_VERSION = 49;

    /** Minor version for the class files that j-- compiles. */
    public static final int MINOR_VERSION = 0;

    /** public access flag. */
    public static final int ACC_PUBLIC = 0x0001;

    /** private access flag. */
    public static final int ACC_PRIVATE = 0x0002;

    /** protected access flag. */
    public static final int ACC_PROTECTED = 0x0004;

    /** static access flag. */
    public static final int ACC_STATIC = 0x0008;

    /** final access flag. */
    public static final int ACC_FINAL = 0x0010;

    /** super access flag. */
    public static final int ACC_SUPER = 0x0020;

    /** synchronized access flag. */
    public static final int ACC_SYNCHRONIZED = 0x0020;

    /** volatile access flag. */
    public static final int ACC_VOLATILE = 0x0040;

    /** bridge access flag. */
    public static final int ACC_BRIDGE = 0x0040;

    /** transient access flag. */
    public static final int ACC_TRANSIENT = 0x0080;

    /** varargs access flag. */
    public static final int ACC_VARARGS = 0x0080;

    /** native access flag. */
    public static final int ACC_NATIVE = 0x0100;

    /** interface access flag. */
    public static final int ACC_INTERFACE = 0x0200;

    /** abstract access flag. */
    public static final int ACC_ABSTRACT = 0x0400;

    /** strict access flag. */
    public static final int ACC_STRICT = 0x0800;

    /** synthetic access flag. */
    public static final int ACC_SYNTHETIC = 0x1000;

    /** annotation access flag. */
    public static final int ACC_ANNOTATION = 0x2000;

    /** enum access flag. */
    public static final int ACC_ENUM = 0x4000;

    /** Identifies CONSTANT_Utf8_info constant pool structure. */
    public static final short CONSTANT_Utf8 = 1;

    /** Identifies CONSTANT_Integer_info constant pool structure. */
    public static final short CONSTANT_Integer = 3;

    /** Identifies CONSTANT_Float_info constant pool structure. */
    public static final short CONSTANT_Float = 4;

    /** Identifies CONSTANT_Long_info constant pool structure. */
    public static final short CONSTANT_Long = 5;

    /** Identifies CONSTANT_Double_info constant pool structure. */
    public static final short CONSTANT_Double = 6;

    /** Identifies CONSTANT_Class_info constant pool structure. */
    public static final short CONSTANT_Class = 7;

    /** Identifies CONSTANT_String_info constant pool structure. */
    public static final short CONSTANT_String = 8;

    /** Identifies CONSTANT_Fieldref_info constant pool structure. */
    public static final short CONSTANT_Fieldref = 9;

    /**
     * Identifies CONSTANT_Methodref_info constant pool structure.
     */
    public static final short CONSTANT_Methodref = 10;

    /**
     * Identifies CONSTANT_InterfaceMethodref_info constant pool structure.
     */
    public static final short CONSTANT_InterfaceMethodref = 11;

    /**
     * Identifies CONSTANT_NameAndType_info constant pool structure.
     */
    public static final short CONSTANT_NameAndType = 12;

    /** Identifies ConstantValue attribute. */
    public static final String ATT_CONSTANT_VALUE = "ConstantValue";

    /** Identifies Code attribute. */
    public static final String ATT_CODE = "Code";

    /** Identifies Exceptions attribute. */
    public static final String ATT_EXCEPTIONS = "Exceptions";

    /** Identifies InnerClasses attribute. */
    public static final String ATT_INNER_CLASSES = "InnerClasses";

    /** Identifies EnclosingMethod attribute. */
    public static final String ATT_ENCLOSING_METHOD = "EnclosingMethod";

    /** Identifies Synthetic attribute. */
    public static final String ATT_SYNTHETIC = "Synthetic";

    /** Identifies Signature attribute. */
    public static final String ATT_SIGNATURE = "Signature";

    /** Identifies SourceFile attribute. */
    public static final String ATT_SOURCE_FILE = "SourceFile";

    /** Identifies SourceDebugExtension attribute. */
    public static final String ATT_SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";

    /** Identifies LineNumberTable attribute. */
    public static final String ATT_LINE_NUMBER_TABLE = "LineNumberTable";

    /** Identifies LocalVariableTable attribute. */
    public static final String ATT_LOCAL_VARIABLE_TABLE = "LocalVariableTable";

    /** Identifies LocalVariableTypeTable attribute. */
    public static final String ATT_LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";

    /** Identifies Deprecated attribute. */
    public static final String ATT_DEPRECATED = "Deprecated";

    /** Identifies RuntimeVisibleAnnotations attribute. */
    public static final String ATT_RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";

    /** Identifies RuntimeInvisibleAnnotations attribute. */
    public static final String ATT_RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";

    /** Identifies RuntimeVisibleParameterAnnotations attribute. */
    public static final String ATT_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";

    /** Identifies RuntimeInvisibleParameterAnnotations attribute. */
    public static final String ATT_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";

    /** Identifies AnnotationDefault attribute. */
    public static final String ATT_ANNOTATION_DEFAULT = "AnnotationDefault";

    /** Identifies boolean type of annotation element value. */
    public static final short ELT_B = 'B';

    /** Identifies char type of annotation element value. */
    public static final short ELT_C = 'C';

    /** Identifies double type of annotation element value. */
    public static final short ELT_D = 'D';

    /** Identifies float type of annotation element value. */
    public static final short ELT_F = 'F';

    /** Identifies int type of annotation element value. */
    public static final short ELT_I = 'I';

    /** Identifies long type of annotation element value. */
    public static final short ELT_J = 'J';

    /** Identifies short type of annotation element value. */
    public static final short ELT_S = 'S';

    /** Identifies boolean type of annotation element value. */
    public static final short ELT_Z = 'Z';

    /** Identifies String type of annotation element value. */
    public static final short ELT_s = 's';

    /** Identifies class type of annotation element value. */
    public static final short ELT_c = 'c';

    /** Identifies annotation type of annotation element value. */
    public static final short ELT_ANNOTATION = '@';

    /** Identifies array type of annotation element value. */
    public static final short ELT_ARRAY = '[';

    /** Identifies enum type of annotation element value. */
    public static final short ELT_e = 'e';

    // JVM instructions begin here

    /** NOP instruction. */
    public static final int NOP = 0;

    /** ACONST_NULL instruction. */
    public static final int ACONST_NULL = 1;

    /** ICONST_M1 instruction. */
    public static final int ICONST_M1 = 2;

    /** ICONST_0 instruction. */
    public static final int ICONST_0 = 3;

    /** ICONST_1 instruction. */
    public static final int ICONST_1 = 4;

    /** ICONST_2 instruction. */
    public static final int ICONST_2 = 5;

    /** ICONST_3 instruction. */
    public static final int ICONST_3 = 6;

    /** ICONST_4 instruction. */
    public static final int ICONST_4 = 7;

    /** ICONST_5 instruction. */
    public static final int ICONST_5 = 8;

    /** LCONST_0 instruction. */
    public static final int LCONST_0 = 9;

    /** LCONST_1 instruction. */
    public static final int LCONST_1 = 10;

    /** FCONST_0 instruction. */
    public static final int FCONST_0 = 11;

    /** FCONST_1 instruction. */
    public static final int FCONST_1 = 12;

    /** FCONST_2 instruction. */
    public static final int FCONST_2 = 13;

    /** DCONST_0 instruction. */
    public static final int DCONST_0 = 14;

    /** DCONST_1 instruction. */
    public static final int DCONST_1 = 15;

    /** BIPUSH instruction. */
    public static final int BIPUSH = 16;

    /** SIPUSH instruction. */
    public static final int SIPUSH = 17;

    /** LDC instruction. */
    public static final int LDC = 18;

    /** LDC_W instruction. */
    public static final int LDC_W = 19;

    /** LDC2_W instruction. */
    public static final int LDC2_W = 20;

    /** ILOAD instruction. */
    public static final int ILOAD = 21;

    /** LLOAD instruction. */
    public static final int LLOAD = 22;

    /** FLOAD instruction. */
    public static final int FLOAD = 23;

    /** DLOAD instruction. */
    public static final int DLOAD = 24;

    /** ALOAD instruction. */
    public static final int ALOAD = 25;

    /** ILOAD_0 instruction. */
    public static final int ILOAD_0 = 26;

    /** ILOAD_1 instruction. */
    public static final int ILOAD_1 = 27;

    /** ILOAD_2 instruction. */
    public static final int ILOAD_2 = 28;

    /** ILOAD_3 instruction. */
    public static final int ILOAD_3 = 29;

    /** LLOAD_0 instruction. */
    public static final int LLOAD_0 = 30;

    /** LLOAD_1 instruction. */
    public static final int LLOAD_1 = 31;

    /** LLOAD_2 instruction. */
    public static final int LLOAD_2 = 32;

    /** LLOAD_3 instruction. */
    public static final int LLOAD_3 = 33;

    /** FLOAD_0 instruction. */
    public static final int FLOAD_0 = 34;

    /** FLOAD_1 instruction. */
    public static final int FLOAD_1 = 35;

    /** FLOAD_2 instruction. */
    public static final int FLOAD_2 = 36;

    /** FLOAD_3 instruction. */
    public static final int FLOAD_3 = 37;

    /** DLOAD_0 instruction. */
    public static final int DLOAD_0 = 38;

    /** DLOAD_1 instruction. */
    public static final int DLOAD_1 = 39;

    /** DLOAD_2 instruction. */
    public static final int DLOAD_2 = 40;

    /** DLOAD_3 instruction. */
    public static final int DLOAD_3 = 41;

    /** ALOAD_0 instruction. */
    public static final int ALOAD_0 = 42;

    /** ALOAD_1 instruction. */
    public static final int ALOAD_1 = 43;

    /** ALOAD_2 instruction. */
    public static final int ALOAD_2 = 44;

    /** ALOAD_3 instruction. */
    public static final int ALOAD_3 = 45;

    /** IALOAD instruction. */
    public static final int IALOAD = 46;

    /** LALOAD instruction. */
    public static final int LALOAD = 47;

    /** FALOAD instruction. */
    public static final int FALOAD = 48;

    /** DALOAD instruction. */
    public static final int DALOAD = 49;

    /** AALOAD instruction. */
    public static final int AALOAD = 50;

    /** BALOAD instruction. */
    public static final int BALOAD = 51;

    /** CALOAD instruction. */
    public static final int CALOAD = 52;

    /** SALOAD instruction. */
    public static final int SALOAD = 53;

    /** ISTORE instruction. */
    public static final int ISTORE = 54;

    /** LSTORE instruction. */
    public static final int LSTORE = 55;

    /** FSTORE instruction. */
    public static final int FSTORE = 56;

    /** DSTORE instruction. */
    public static final int DSTORE = 57;

    /** ASTORE instruction. */
    public static final int ASTORE = 58;

    /** ISTORE_0 instruction. */
    public static final int ISTORE_0 = 59;

    /** ISTORE_1 instruction. */
    public static final int ISTORE_1 = 60;

    /** ISTORE_2 instruction. */
    public static final int ISTORE_2 = 61;

    /** ISTORE_3 instruction. */
    public static final int ISTORE_3 = 62;

    /** LSTORE_0 instruction. */
    public static final int LSTORE_0 = 63;

    /** LSTORE_1 instruction. */
    public static final int LSTORE_1 = 64;

    /** LSTORE_2 instruction. */
    public static final int LSTORE_2 = 65;

    /** LSTORE_3 instruction. */
    public static final int LSTORE_3 = 66;

    /** FSTORE_0 instruction. */
    public static final int FSTORE_0 = 67;

    /** FSTORE_1 instruction. */
    public static final int FSTORE_1 = 68;

    /** FSTORE_2 instruction. */
    public static final int FSTORE_2 = 69;

    /** FSTORE_3 instruction. */
    public static final int FSTORE_3 = 70;

    /** DSTORE_0 instruction. */
    public static final int DSTORE_0 = 71;

    /** DSTORE_1 instruction. */
    public static final int DSTORE_1 = 72;

    /** DSTORE_2 instruction. */
    public static final int DSTORE_2 = 73;

    /** DSTORE_3 instruction. */
    public static final int DSTORE_3 = 74;

    /** ASTORE_0 instruction. */
    public static final int ASTORE_0 = 75;

    /** ASTORE_1 instruction. */
    public static final int ASTORE_1 = 76;

    /** ASTORE_2 instruction. */
    public static final int ASTORE_2 = 77;

    /** ASTORE_3 instruction. */
    public static final int ASTORE_3 = 78;

    /** IASTORE instruction. */
    public static final int IASTORE = 79;

    /** LASTORE instruction. */
    public static final int LASTORE = 80;

    /** FASTORE instruction. */
    public static final int FASTORE = 81;

    /** DASTORE instruction. */
    public static final int DASTORE = 82;

    /** AASTORE instruction. */
    public static final int AASTORE = 83;

    /** BASTORE instruction. */
    public static final int BASTORE = 84;

    /** CASTORE instruction. */
    public static final int CASTORE = 85;

    /** SASTORE instruction. */
    public static final int SASTORE = 86;

    /** POP instruction. */
    public static final int POP = 87;

    /** POP2 instruction. */
    public static final int POP2 = 88;

    /** DUP instruction. */
    public static final int DUP = 89;

    /** DUP_X1 instruction. */
    public static final int DUP_X1 = 90;

    /** DUP_X2 instruction. */
    public static final int DUP_X2 = 91;

    /** DUP2 instruction. */
    public static final int DUP2 = 92;

    /** DUP2_X1 instruction. */
    public static final int DUP2_X1 = 93;

    /** DUP2_X2 instruction. */
    public static final int DUP2_X2 = 94;

    /** SWAP instruction. */
    public static final int SWAP = 95;

    /** IADD instruction. */
    public static final int IADD = 96;

    /** LADD instruction. */
    public static final int LADD = 97;

    /** FADD instruction. */
    public static final int FADD = 98;

    /** DADD instruction. */
    public static final int DADD = 99;

    /** ISUB instruction. */
    public static final int ISUB = 100;

    /** LSUB instruction. */
    public static final int LSUB = 101;

    /** FSUB instruction. */
    public static final int FSUB = 102;

    /** DSUB instruction. */
    public static final int DSUB = 103;

    /** IMUL instruction. */
    public static final int IMUL = 104;

    /** LMUL instruction. */
    public static final int LMUL = 105;

    /** FMUL instruction. */
    public static final int FMUL = 106;

    /** DMUL instruction. */
    public static final int DMUL = 107;

    /** IDIV instruction. */
    public static final int IDIV = 108;

    /** LDIV instruction. */
    public static final int LDIV = 109;

    /** FDIV instruction. */
    public static final int FDIV = 110;

    /** DDIV instruction. */
    public static final int DDIV = 111;

    /** IREM instruction. */
    public static final int IREM = 112;

    /** LREM instruction. */
    public static final int LREM = 113;

    /** FREM instruction. */
    public static final int FREM = 114;

    /** DREM instruction. */
    public static final int DREM = 115;

    /** INEG instruction. */
    public static final int INEG = 116;

    /** LNEG instruction. */
    public static final int LNEG = 117;

    /** FNEG instruction. */
    public static final int FNEG = 118;

    /** DNEG instruction. */
    public static final int DNEG = 119;

    /** ISHL instruction. */
    public static final int ISHL = 120;

    /** LSHL instruction. */
    public static final int LSHL = 121;

    /** ISHR instruction. */
    public static final int ISHR = 122;

    /** LSHR instruction. */
    public static final int LSHR = 123;

    /** IUSHR instruction. */
    public static final int IUSHR = 124;

    /** LUSHR instruction. */
    public static final int LUSHR = 125;

    /** IAND instruction. */
    public static final int IAND = 126;

    /** LAND instruction. */
    public static final int LAND = 127;

    /** IOR instruction. */
    public static final int IOR = 128;

    /** LOR instruction. */
    public static final int LOR = 129;

    /** IXOR instruction. */
    public static final int IXOR = 130;

    /** LXOR instruction. */
    public static final int LXOR = 131;

    /** IINC instruction. */
    public static final int IINC = 132;

    /** I2L instruction. */
    public static final int I2L = 133;

    /** I2F instruction. */
    public static final int I2F = 134;

    /** I2D instruction. */
    public static final int I2D = 135;

    /** L2I instruction. */
    public static final int L2I = 136;

    /** L2F instruction. */
    public static final int L2F = 137;

    /** L2D instruction. */
    public static final int L2D = 138;

    /** F2I instruction. */
    public static final int F2I = 139;

    /** F2L instruction. */
    public static final int F2L = 140;

    /** F2D instruction. */
    public static final int F2D = 141;

    /** D2I instruction. */
    public static final int D2I = 142;

    /** D2L instruction. */
    public static final int D2L = 143;

    /** D2F instruction. */
    public static final int D2F = 144;

    /** I2B instruction. */
    public static final int I2B = 145;

    /** I2C instruction. */
    public static final int I2C = 146;

    /** I2S instruction. */
    public static final int I2S = 147;

    /** LCMP instruction. */
    public static final int LCMP = 148;

    /** FCMPL instruction. */
    public static final int FCMPL = 149;

    /** FCMPG instruction. */
    public static final int FCMPG = 150;

    /** DCMPL instruction. */
    public static final int DCMPL = 151;

    /** DCMPG instruction. */
    public static final int DCMPG = 152;

    /** IFEQ instruction. */
    public static final int IFEQ = 153;

    /** IFNE instruction. */
    public static final int IFNE = 154;

    /** IFLT instruction. */
    public static final int IFLT = 155;

    /** IFGE instruction. */
    public static final int IFGE = 156;

    /** IFGT instruction. */
    public static final int IFGT = 157;

    /** IFLE instruction. */
    public static final int IFLE = 158;

    /** IF_ICMPEQ instruction. */
    public static final int IF_ICMPEQ = 159;

    /** IF_ICMPNE instruction. */
    public static final int IF_ICMPNE = 160;

    /** IF_ICMPLT instruction. */
    public static final int IF_ICMPLT = 161;

    /** IF_ICMPGE instruction. */
    public static final int IF_ICMPGE = 162;

    /** IF_ICMPGT instruction. */
    public static final int IF_ICMPGT = 163;

    /** IF_ICMPLE instruction. */
    public static final int IF_ICMPLE = 164;

    /** IF_ACMPEQ instruction. */
    public static final int IF_ACMPEQ = 165;

    /** IF_ACMPNE instruction. */
    public static final int IF_ACMPNE = 166;

    /** GOTO instruction. */
    public static final int GOTO = 167;

    /** JSR instruction. */
    public static final int JSR = 168;

    /** RET instruction. */
    public static final int RET = 169;

    /** TABLESWITCH instruction. */
    public static final int TABLESWITCH = 170;

    /** LOOKUPSWITCH instruction. */
    public static final int LOOKUPSWITCH = 171;

    /** IRETURN instruction. */
    public static final int IRETURN = 172;

    /** LRETURN instruction. */
    public static final int LRETURN = 173;

    /** FRETURN instruction. */
    public static final int FRETURN = 174;

    /** DRETURN instruction. */
    public static final int DRETURN = 175;

    /** ARETURN instruction. */
    public static final int ARETURN = 176;

    /** RETURN instruction. */
    public static final int RETURN = 177;

    /** GETSTATIC instruction. */
    public static final int GETSTATIC = 178;

    /** PUTSTATIC instruction. */
    public static final int PUTSTATIC = 179;

    /** GETFIELD instruction. */
    public static final int GETFIELD = 180;

    /** PUTFIELD instruction. */
    public static final int PUTFIELD = 181;

    /** INVOKEVIRTUAL instruction. */
    public static final int INVOKEVIRTUAL = 182;

    /** INVOKESPECIAL instruction. */
    public static final int INVOKESPECIAL = 183;

    /** INVOKESTATIC instruction. */
    public static final int INVOKESTATIC = 184;

    /** INVOKEINTERFACE instruction. */
    public static final int INVOKEINTERFACE = 185;

    /** INVOKEDYNAMIC instruction. */
    public static final int INVOKEDYNAMIC = 186;

    /** NEW instruction. */
    public static final int NEW = 187;

    /** NEWARRAY instruction. */
    public static final int NEWARRAY = 188;

    /** ANEWARRAY instruction. */
    public static final int ANEWARRAY = 189;

    /** ARRAYLENGTH instruction. */
    public static final int ARRAYLENGTH = 190;

    /** ATHROW instruction. */
    public static final int ATHROW = 191;

    /** CHECKCAST instruction. */
    public static final int CHECKCAST = 192;

    /** INSTANCEOF instruction. */
    public static final int INSTANCEOF = 193;

    /** MONITORENTER instruction. */
    public static final int MONITORENTER = 194;

    /** MONITOREXIT instruction. */
    public static final int MONITOREXIT = 195;

    /** WIDE instruction. */
    public static final int WIDE = 196;

    /** MULTIANEWARRAY instruction. */
    public static final int MULTIANEWARRAY = 197;

    /** IFNULL instruction. */
    public static final int IFNULL = 198;

    /** IFNONNULL instruction. */
    public static final int IFNONNULL = 199;

    /** GOTO_W instruction. */
    public static final int GOTO_W = 200;

    /** JSR_W instruction. */
    public static final int JSR_W = 201;

    // JVM instructions end here

    /**
     * We classify the JVM instructions into the following categories.
     */
    enum Category {
        OBJECT, FIELD, METHOD1, METHOD2, ARRAY1, ARRAY2, ARRAY3, ARITHMETIC1, ARITHMETIC2, BIT, COMPARISON, CONVERSION, FLOW_CONTROL1, FLOW_CONTROL2, FLOW_CONTROL3, FLOW_CONTROL4, LOAD_STORE1, LOAD_STORE2, LOAD_STORE3, LOAD_STORE4, STACK, MISC;
    }

    // The constants below simply serve as markers. We are not
    // interested in their values, which however have been picked
    // so
    // as not to conflict with others.

    /**
     * Denotes values that are irrelevant to certain instructions. For example,
     * local variable index for arithmetic instructions.
     */
    public static final int IRRELEVANT = -1;

    /**
     * Denotes values that are not statically known. For example, stack units
     * for field instructions.
     */
    public static final int DYNAMIC = 300;

    /**
     * Stack units for the instructions that empty the operand stack.
     */
    public static final int EMPTY_STACK = 301;

    /**
     * Stack units for the instructions that set the operand stack to unit size.
     */
    public static final int UNIT_SIZE_STACK = 302;

}
