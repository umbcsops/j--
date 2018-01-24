// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.DataOutputStream;
import java.io.IOException;
import static jminusminus.CLConstants.*;

/**
 * Representation of cp_info structure (JVM Spec Section 4.5). Classes
 * representing individual constant pool items inherit this class. Instances of
 * these classes are created and populated into the constant pool table when a
 * class is read using CLAbsorber or constructed using CLEmitter.
 */

abstract class CLCPInfo {

    // The fields below represent the members of the cp_info
    // structure and are thus inherited by the child classes of
    // CLCPInfo. These classes define their own fields (if any)
    // representing the members of the individual cp_info
    // structures they represent.

    /** Index of this object into the constant pool. */
    public int cpIndex;

    /** cp_info.tag item. */
    public short tag;

    /**
     * Write the contents of this constant pool item to the specified output
     * stream.
     * 
     * @param out
     *            output stream.
     * @throws IOException
     *             if an error occurs while writing.
     */

    public void write(CLOutputStream out) throws IOException {
        out.writeByte(tag);
    }

    /**
     * Write the content of this object to STDOUT in a format similar to that of
     * javap.
     * 
     * @param p
     *            for pretty printing with indentation.
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("%-10s", cpIndex);
    }

}

/**
 * Representation of CONSTANT_Class_info structure (JVM Spec Section 4.5.1).
 */

class CLConstantClassInfo extends CLCPInfo {

    /** CONSTANT_Class_info.name_index item. */
    public int nameIndex;

    /**
     * Construct a CLConstantClassInfo object.
     * 
     * @param nameIndex
     *            CONSTANT_Class_info.name_index item.
     */

    public CLConstantClassInfo(int nameIndex) {
        super.tag = CONSTANT_Class;
        this.nameIndex = nameIndex;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeShort(nameIndex);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantClassInfo) {
            CLConstantClassInfo c = (CLConstantClassInfo) obj;
            if (c.nameIndex == nameIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Class", nameIndex);
    }

}

/**
 * Abstract super class of CONSTANT_Fieldref_info, CONSTANT_Methodref_info,
 * CONSTANT_InterfaceMethodref_info structures (JVM Spec Section 4.5.2).
 */

abstract class CLConstantMemberRefInfo extends CLCPInfo {

    /** CONSTANT_Memberref_info.class_index item. */
    public int classIndex;

    /** CONSTANT_Memberref_info.name_and_type_index item. */
    public int nameAndTypeIndex;

    /**
     * Construct a CLConstantMemberRefInfo object.
     * 
     * @param classIndex
     *            CONSTANT_Memberref_info.class_index item.
     * @param nameAndTypeIndex
     *            CONSTANT_Memberref_info.name_and_type_index item.
     * @param tag
     *            CONSTANT_Memberref_info.tag item.
     */

    protected CLConstantMemberRefInfo(int classIndex, int nameAndTypeIndex,
            short tag) {
        super.tag = tag;
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeShort(classIndex);
        out.writeShort(nameAndTypeIndex);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantMemberRefInfo) {
            CLConstantMemberRefInfo c = (CLConstantMemberRefInfo) obj;
            if ((c.tag == tag) && (c.classIndex == classIndex)
                    && (c.nameAndTypeIndex == nameAndTypeIndex)) {
                return true;
            }
        }
        return false;
    }

}

/**
 * Representation of CONSTANT_Fieldref_info structure (JVM Spec Section 4.5.2).
 */

class CLConstantFieldRefInfo extends CLConstantMemberRefInfo {

    /**
     * Construct a CLConstantFieldRefInfo object.
     * 
     * @param classIndex
     *            CONSTANT_Fieldref_info.class_index item.
     * @param nameAndTypeIndex
     *            CONSTANT_Fieldref_info.name_and_type_index item.
     */

    public CLConstantFieldRefInfo(int classIndex, int nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex, CONSTANT_Fieldref);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%-8s%-8s\n", "FieldRef", classIndex, nameAndTypeIndex);
    }

}

/**
 * Representation of CONSTANT_Methodref_info structure (JVM Spec Section 4.5.2).
 */

class CLConstantMethodRefInfo extends CLConstantMemberRefInfo {

    /**
     * Construct a CLConstantMethodRefInfo object.
     * 
     * @param classIndex
     *            CONSTANT_Methodref_info.class_index item.
     * @param nameAndTypeIndex
     *            CONSTANT_Methodref_info.name_and_type_index item.
     */

    public CLConstantMethodRefInfo(int classIndex, int nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex, CONSTANT_Methodref);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%-8s%-8s\n", "MethodRef", classIndex, nameAndTypeIndex);
    }

}

/**
 * Representation of CONSTANT_InterfaceMethodref_info structure (JVM Spec
 * Section 4.5.2).
 */

class CLConstantInterfaceMethodRefInfo extends CLConstantMemberRefInfo {

    /**
     * Construct a CLConstantInterfaceMethodRefInfo object.
     * 
     * @param classIndex
     *            CONSTANT_InterfaceMethodref_info.class_index item.
     * @param nameAndTypeIndex
     *            CONSTANT_InterfaceMethodref_info.name_and_type_index item.
     */

    public CLConstantInterfaceMethodRefInfo(int classIndex, int nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex, CONSTANT_InterfaceMethodref);
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%-8s%-8s\n", "InterfaceMethodRef", classIndex,
                nameAndTypeIndex);
    }

}

/**
 * Representation of CONSTANT_String_info structure (JVM Spec Section 4.5.3).
 */

class CLConstantStringInfo extends CLCPInfo {

    /** CONSTANT_String_info.string_index item. */
    public int stringIndex;

    /**
     * Construct a CLConstantStringInfo object.
     * 
     * @param stringIndex
     *            CONSTANT_String_info.string_index item.
     */

    public CLConstantStringInfo(int stringIndex) {
        super.tag = CONSTANT_String;
        this.stringIndex = stringIndex;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeShort(stringIndex);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantStringInfo) {
            CLConstantStringInfo c = (CLConstantStringInfo) obj;
            if (c.stringIndex == stringIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "String", stringIndex);
    }

}

/**
 * Representation of CONSTANT_Integer_info structure (JVM Spec Section 4.5.4).
 */

class CLConstantIntegerInfo extends CLCPInfo {

    /** The int number. */
    public int i;

    /**
     * Construct a CLConstantIntegerInfo object.
     * 
     * @param i
     *            the int number.
     */

    public CLConstantIntegerInfo(int i) {
        super.tag = CONSTANT_Integer;
        this.i = i;
    }

    /**
     * Return CONSTANT_Integer_info.bytes item.
     * 
     * @return CONSTANT_Integer_info.bytes item.
     */

    public short[] bytes() {
        short[] s = new short[4];
        short mask = 0xFF;
        int k = i;
        for (int j = 0; j < 4; j++) {
            s[3 - j] = (short) (k & mask);
            k >>>= 8;
        }
        return s;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);

        // out is cast to DataOutputStream to resolve the
        // writeInt()
        // ambiguity
        ((DataOutputStream) out).writeInt(i);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantIntegerInfo) {
            CLConstantIntegerInfo c = (CLConstantIntegerInfo) obj;
            if (c.i == i) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Integer", i);
    }

}

/**
 * Representation of CONSTANT_Float_info structure (JVM Spec Section 4.5.4).
 */

class CLConstantFloatInfo extends CLCPInfo {

    /** The floating-point number. */
    public float f;

    /**
     * Construct a CLConstantFloatInfo object.
     * 
     * @param f
     *            the floating-point number.
     */

    public CLConstantFloatInfo(float f) {
        super.tag = CONSTANT_Float;
        this.f = f;
    }

    /**
     * Return CONSTANT_Float_info.bytes item.
     * 
     * @return CONSTANT_Float_info.bytes item.
     */

    public short[] bytes() {
        short[] s = new short[4];
        short mask = 0xFF;
        int i = Float.floatToIntBits(f);
        for (int j = 0; j < 4; j++) {
            s[3 - j] = (short) (i & mask);
            i >>>= 8;
        }
        return s;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeFloat(f);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantFloatInfo) {
            CLConstantFloatInfo c = (CLConstantFloatInfo) obj;
            if (c.f == f) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Float", f);
    }

}

/**
 * Representation of CONSTANT_Long_info structure (JVM Spec Section 4.5.5).
 */

class CLConstantLongInfo extends CLCPInfo {

    /** The long number. */
    public long l;

    /**
     * Return the 8 bytes of the long value.
     * 
     * @return the 8 bytes of the long value.
     */

    private short[] bytes() {
        short[] s = new short[8];
        short mask = 0xFF;
        long k = l;
        for (int j = 0; j < 8; j++) {
            s[7 - j] = (short) (k & mask);
            k >>>= 8;
        }
        return s;
    }

    /**
     * Construct a CLConstantLongInfo object.
     * 
     * @param l
     *            the long number.
     */

    public CLConstantLongInfo(long l) {
        super.tag = CONSTANT_Long;
        this.l = l;
    }

    /**
     * Return CONSTANT_Long_info.low_bytes item.
     * 
     * @return CONSTANT_Long_info.low_bytes item.
     */

    public short[] lowBytes() {
        short[] s = bytes();
        short[] l = new short[4];
        l[0] = s[4];
        l[1] = s[5];
        l[2] = s[6];
        l[3] = s[7];
        return l;
    }

    /**
     * Return CONSTANT_Long_info.high_bytes item.
     * 
     * @return CONSTANT_Long_info.high_bytes item.
     */

    public short[] highBytes() {
        short[] s = bytes();
        short[] h = new short[4];
        h[0] = s[0];
        h[1] = s[1];
        h[2] = s[2];
        h[3] = s[3];
        return h;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeLong(l);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantLongInfo) {
            CLConstantLongInfo c = (CLConstantLongInfo) obj;
            if (c.l == l) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Long", l);
    }

}

/**
 * Representation of CONSTANT_Double_info structure (JVM Spec Section 4.5.5).
 */

class CLConstantDoubleInfo extends CLCPInfo {

    /** The double precision floating-point number. */
    public double d;

    /**
     * Return the 8 bytes of the double precision floating-point value.
     * 
     * @return the 8 bytes of the double precision floating-point value.
     */

    private short[] bytes() {
        short[] s = new short[8];
        short mask = 0xFF;
        long l = Double.doubleToLongBits(d);
        for (int j = 0; j < 8; j++) {
            s[7 - j] = (short) (l & mask);
            l >>>= 8;
        }
        return s;
    }

    /**
     * Construct a CLConstantDoubleInfo object.
     * 
     * @param d
     *            the double precision floating-point number.
     */

    public CLConstantDoubleInfo(double d) {
        super.tag = CONSTANT_Double;
        this.d = d;
    }

    /**
     * Return CONSTANT_Double_info.low_bytes item.
     * 
     * @return CONSTANT_Double_info.low_bytes item.
     */

    public short[] lowBytes() {
        short[] s = bytes();
        short[] l = new short[4];
        l[0] = s[4];
        l[1] = s[5];
        l[2] = s[6];
        l[3] = s[7];
        return l;
    }

    /**
     * Return CONSTANT_Double_info.high_bytes item.
     * 
     * @return CONSTANT_Double_info.high_bytes item.
     */

    public short[] highBytes() {
        short[] s = bytes();
        short[] h = new short[4];
        h[0] = s[0];
        h[1] = s[1];
        h[2] = s[2];
        h[3] = s[3];
        return h;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeDouble(d);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantDoubleInfo) {
            CLConstantDoubleInfo c = (CLConstantDoubleInfo) obj;
            if (c.d == d) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Double", d);
    }

}

/**
 * Representation of CONSTANT_NameAndType_info structure (JVM Spec Section
 * 4.5.6).
 */

class CLConstantNameAndTypeInfo extends CLCPInfo {

    /** CONSTANT_NameAndType_info.name_index item. */
    public int nameIndex;

    /** CONSTANT_NameAndType_info.descriptor_index item. */
    public int descriptorIndex;

    /**
     * Construct a CLConstantNameAndTypeInfo object.
     * 
     * @param nameIndex
     *            CONSTANT_NameAndType_info.name_index item.
     * @param descriptorIndex
     *            CONSTANT_NameAndType_info.descriptor_index item.
     */

    public CLConstantNameAndTypeInfo(int nameIndex, int descriptorIndex) {
        super.tag = CONSTANT_NameAndType;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeShort(nameIndex);
        out.writeShort(descriptorIndex);
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantNameAndTypeInfo) {
            CLConstantNameAndTypeInfo c = (CLConstantNameAndTypeInfo) obj;
            if ((c.nameIndex == nameIndex)
                    && (c.descriptorIndex == descriptorIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%-8s%-8s\n", "NameAndType", nameIndex, descriptorIndex);
    }

}

/**
 * Representation of CONSTANT_Utf8_info structure (JVM Spec Section 4.5.7).
 */

class CLConstantUtf8Info extends CLCPInfo {

    /** CONSTANT_Utf8_info.bytes item. */
    public byte[] b;

    /**
     * Construct a CLConstantUtf8Info object.
     * 
     * @param b
     *            a constant string value.
     */

    public CLConstantUtf8Info(byte[] b) {
        super.tag = CONSTANT_Utf8;
        this.b = b;
    }

    /**
     * Return CONSTANT_Utf8_info.length item.
     * 
     * @return CONSTANT_Utf8_info.length item.
     */

    public int length() {
        return b.length;
    }

    /**
     * @inheritDoc
     */

    public void write(CLOutputStream out) throws IOException {
        super.write(out);
        out.writeUTF(new String(b));
    }

    /**
     * @inheritDoc
     */

    public boolean equals(Object obj) {
        if (obj instanceof CLConstantUtf8Info) {
            CLConstantUtf8Info c = (CLConstantUtf8Info) obj;
            if ((new String(b)).equals(new String(c.b))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        super.writeToStdOut(p);
        p.printf("%-20s%s\n", "Utf8", new String(b));
    }

}
