// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.EOFException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import static jminusminus.CLConstants.*;

/**
 * CLAbsorber is for reading a Java class into an in-memory CLFile
 * representation and printing it out to STDOUT in a format similar to that of
 * javap.
 */

public class CLAbsorber {

    /** CLFile representation of the class that is read. */
    private CLFile classFile;

    /** Whether or not an error occurred in reading the class. */
    private boolean errorHasOccurred;

    /** Name of the class that is read. */
    private String className;

    /**
     * Print the specified warning to STDERR.
     * 
     * @param message
     *            warning.
     * @param args
     *            related values.
     */

    private void reportWarning(String message, Object... args) {
        System.err.printf("CLAbsorber Warning: " + message + "\n", args);
    }

    /**
     * Print the specified error message to STDERR and set the error flag to
     * true.
     * 
     * @param message
     *            error message.
     * @param args
     *            related values.
     */

    private void reportError(String message, Object... args) {
        System.err.printf("CLAbsorber Error: " + message + "\n", args);
        errorHasOccurred = true;
    }

    /**
     * Read the constant pool information from the specified stream, and return
     * the information as a CLConstantPool object.
     * 
     * @param in
     *            input stream.
     * @return the constant pool.
     */

    private CLConstantPool readConstantPool(CLInputStream in) {
        CLConstantPool cp = new CLConstantPool();
        try {
            for (int i = 1; i < classFile.constantPoolCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                case CONSTANT_Class:
                    cp
                            .addCPItem(new CLConstantClassInfo(in
                                    .readUnsignedShort()));
                    break;
                case CONSTANT_Fieldref:
                    cp.addCPItem(new CLConstantFieldRefInfo(in
                            .readUnsignedShort(), in.readUnsignedShort()));
                    break;
                case CONSTANT_Methodref:
                    cp.addCPItem(new CLConstantMethodRefInfo(in
                            .readUnsignedShort(), in.readUnsignedShort()));
                    break;
                case CONSTANT_InterfaceMethodref:
                    cp.addCPItem(new CLConstantInterfaceMethodRefInfo(in
                            .readUnsignedShort(), in.readUnsignedShort()));
                    break;
                case CONSTANT_String:
                    cp.addCPItem(new CLConstantStringInfo(in
                            .readUnsignedShort()));
                    break;
                case CONSTANT_Integer:
                    cp.addCPItem(new CLConstantIntegerInfo(in.readInt()));
                    break;
                case CONSTANT_Float:
                    cp.addCPItem(new CLConstantFloatInfo(in.readFloat()));
                    break;
                case CONSTANT_Long:
                    cp.addCPItem(new CLConstantLongInfo(in.readLong()));
                    i++;
                    break;
                case CONSTANT_Double:
                    cp.addCPItem(new CLConstantDoubleInfo(in.readDouble()));
                    i++;
                    break;
                case CONSTANT_NameAndType:
                    cp.addCPItem(new CLConstantNameAndTypeInfo(in
                            .readUnsignedShort(), in.readUnsignedShort()));
                    break;
                case CONSTANT_Utf8:
                    int length = in.readUnsignedShort();
                    byte[] b = new byte[length];
                    in.read(b);
                    cp.addCPItem(new CLConstantUtf8Info(b));
                    break;
                default:
                    reportError("Unknown cp_info tag '%d'", tag);
                    return cp;
                }
            }
        } catch (IOException e) {
            reportError("Error reading constant pool from file %s", className);
        }
        return cp;
    }

    /**
     * Read the fields from the specified stream, and return them as a list.
     * 
     * @param in
     *            input stream.
     * @param fieldsCount
     *            number of fields.
     * @return list of fields.
     */

    private ArrayList<CLFieldInfo> readFields(CLInputStream in, int fieldsCount) {
        ArrayList<CLFieldInfo> fields = new ArrayList<CLFieldInfo>();
        try {
            for (int i = 0; i < fieldsCount; i++) {
                int accessFlags = in.readUnsignedShort();
                int nameIndex = in.readUnsignedShort();
                int descriptorIndex = in.readUnsignedShort();
                int attributesCount = in.readUnsignedShort();
                fields.add(new CLFieldInfo(accessFlags, nameIndex,
                        descriptorIndex, attributesCount, readAttributes(in,
                                attributesCount)));
            }
        } catch (IOException e) {
            reportError("Error reading fields from file %s", className);
        }
        return fields;
    }

    /**
     * Read the methods from the specified stream, and return them as a list.
     * 
     * @param in
     *            input stream.
     * @param methodsCount
     *            number of methods.
     * @return the methods.
     */

    private ArrayList<CLMethodInfo> readMethods(CLInputStream in,
            int methodsCount) {
        ArrayList<CLMethodInfo> methods = new ArrayList<CLMethodInfo>();
        try {
            for (int i = 0; i < methodsCount; i++) {
                int accessFlags = in.readUnsignedShort();
                int nameIndex = in.readUnsignedShort();
                int descriptorIndex = in.readUnsignedShort();
                int attributesCount = in.readUnsignedShort();
                methods.add(new CLMethodInfo(accessFlags, nameIndex,
                        descriptorIndex, attributesCount, readAttributes(in,
                                attributesCount)));
            }
        } catch (IOException e) {
            reportError("Error reading methods from file %s", className);
        }
        return methods;
    }

    /**
     * Read the attributes from the specified stream, and return them as a list
     * 
     * @param in
     *            input stream.
     * @param attributeCount
     *            number of attributes.
     * @return list of attributes.
     */

    private ArrayList<CLAttributeInfo> readAttributes(CLInputStream in,
            int attributesCount) {
        ArrayList<CLAttributeInfo> attributes = new ArrayList<CLAttributeInfo>();
        try {
            CLConstantPool cp = classFile.constantPool;
            for (int i = 0; i < attributesCount; i++) {
                int attributeNameIndex = in.readUnsignedShort();
                long attributeLength = in.readUnsignedInt();
                CLAttributeInfo attributeInfo = null;
                String attributeName = new String(((CLConstantUtf8Info) cp
                        .cpItem(attributeNameIndex)).b);
                if (attributeName.equals(ATT_CONSTANT_VALUE)) {
                    attributeInfo = readConstantValueAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_CODE)) {
                    attributeInfo = readCodeAttribute(in, attributeNameIndex,
                            attributeLength);
                } else if (attributeName.equals(ATT_EXCEPTIONS)) {
                    attributeInfo = readExceptionsAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_INNER_CLASSES)) {
                    attributeInfo = readInnerClassesAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_ENCLOSING_METHOD)) {
                    attributeInfo = readEnclosingMethodAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_SYNTHETIC)) {
                    attributeInfo = readSyntheticAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_SIGNATURE)) {
                    attributeInfo = readSignatureAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_SOURCE_FILE)) {
                    attributeInfo = readSourceFileAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_SOURCE_DEBUG_EXTENSION)) {
                    attributeInfo = readSourceDebugExtensionAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_LINE_NUMBER_TABLE)) {
                    attributeInfo = readLineNumberTableAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_LOCAL_VARIABLE_TABLE)) {
                    attributeInfo = readLocalVariableTableAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_LOCAL_VARIABLE_TYPE_TABLE)) {
                    attributeInfo = readLocalVariableTypeTableAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_DEPRECATED)) {
                    attributeInfo = readDeprecatedAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName
                        .equals(ATT_RUNTIME_VISIBLE_ANNOTATIONS)) {
                    attributeInfo = readRuntimeVisibleAnnotationsAttribute(in,
                            attributeNameIndex, attributeLength);
                } else if (attributeName
                        .equals(ATT_RUNTIME_INVISIBLE_ANNOTATIONS)) {
                    attributeInfo = readRuntimeInvisibleAnnotationsAttribute(
                            in, attributeNameIndex, attributeLength);
                } else if (attributeName
                        .equals(ATT_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS)) {
                    attributeInfo = readRuntimeVisibleParameterAnnotationsAttribute(
                            in, attributeNameIndex, attributeLength);
                } else if (attributeName
                        .equals(ATT_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS)) {
                    attributeInfo = readRuntimeInvisibleParameterAnnotationsAttribute(
                            in, attributeNameIndex, attributeLength);
                } else if (attributeName.equals(ATT_ANNOTATION_DEFAULT)) {
                    attributeInfo = readAnnotationDefaultAttribute(in,
                            attributeNameIndex, attributeLength);
                } else {
                    reportWarning("Unknown attribute '%s'", attributeName,
                            className);
                    for (long j = 0; j < attributeLength; j++) {
                        in.readUnsignedByte();
                    }
                }
                if (attributeInfo != null) {
                    attributes.add(attributeInfo);
                }
            }
        } catch (IOException e) {
            reportError("Error reading attributes from file %s", className);
        }
        return attributes;
    }

    /**
     * Read a ConstantValue attribute from the specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a ConstantValue attribute.
     */

    private CLConstantValueAttribute readConstantValueAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLConstantValueAttribute attribute = null;
        try {
            attribute = new CLConstantValueAttribute(attributeNameIndex,
                    attributeLength, in.readUnsignedShort());
        } catch (IOException e) {
            reportError("Error reading ConstantValue_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read a Code attribute from the specified input stream, and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a Code attribute.
     */

    private CLCodeAttribute readCodeAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        CLCodeAttribute attribute = null;
        try {
            int maxStack = in.readUnsignedShort();
            int maxLocals = in.readUnsignedShort();
            ArrayList<Integer> code = new ArrayList<Integer>();
            long codeLength = in.readUnsignedInt();
            for (long l = 0; l < codeLength; l++) {
                code.add(in.readUnsignedByte());
            }
            int exceptionTableLength = in.readUnsignedShort();
            ArrayList<CLExceptionInfo> exceptionTable = new ArrayList<CLExceptionInfo>();
            for (int l = 0; l < exceptionTableLength; l++) {
                int startPC = in.readUnsignedShort();
                int endPC = in.readUnsignedShort();
                int handlerPC = in.readUnsignedShort();
                int catchType = in.readUnsignedShort();
                exceptionTable.add(new CLExceptionInfo(startPC, endPC,
                        handlerPC, catchType));
            }
            int codeAttrAttributesCount = in.readUnsignedShort();
            ArrayList<CLAttributeInfo> codeAttrAttributes = readAttributes(in,
                    codeAttrAttributesCount);
            attribute = new CLCodeAttribute(attributeNameIndex,
                    attributeLength, maxStack, maxLocals, codeLength, code,
                    exceptionTableLength, exceptionTable,
                    codeAttrAttributesCount, codeAttrAttributes);
        } catch (IOException e) {
            reportError("Error reading Code_attribute from file %s", className);
        }
        return attribute;
    }

    /**
     * Read an Exceptions attribute from the specified input stream, and return
     * it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return an Exceptions attribute.
     */

    private CLExceptionsAttribute readExceptionsAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        CLExceptionsAttribute attribute = null;
        try {
            int numberOfExceptions = in.readUnsignedShort();
            ArrayList<Integer> exceptionIndexTable = new ArrayList<Integer>();
            for (int l = 0; l < numberOfExceptions; l++) {
                exceptionIndexTable.add(in.readUnsignedShort());
            }
            attribute = new CLExceptionsAttribute(attributeNameIndex,
                    attributeLength, numberOfExceptions, exceptionIndexTable);
        } catch (IOException e) {
            reportError("Error reading Exceptions_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read an InnerClasses attribute from the specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return an InnerClasses attribute.
     */

    private CLInnerClassesAttribute readInnerClassesAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        CLInnerClassesAttribute attribute = null;
        try {
            int numberOfClasses = in.readUnsignedShort();
            ArrayList<CLInnerClassInfo> classes = new ArrayList<CLInnerClassInfo>();
            for (int m = 0; m < numberOfClasses; m++) {
                classes.add(new CLInnerClassInfo(in.readUnsignedShort(), in
                        .readUnsignedShort(), in.readUnsignedShort(), in
                        .readUnsignedShort()));
            }
            attribute = new CLInnerClassesAttribute(attributeNameIndex,
                    attributeLength, numberOfClasses, classes);

        } catch (IOException e) {
            reportError("Error reading InnerClasses_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read an EnclosingMethod attribute from the specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return an EnclosingMethod attribute.
     */

    private CLEnclosingMethodAttribute readEnclosingMethodAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLEnclosingMethodAttribute attribute = null;
        try {
            attribute = new CLEnclosingMethodAttribute(attributeNameIndex,
                    attributeLength, in.readUnsignedShort(), in
                            .readUnsignedShort());
        } catch (IOException e) {
            reportError("Error reading EnclosingMethod_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read a Synthetic attribute from the specified input stream, and return
     * it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a Synthetic attribute.
     */

    private CLSyntheticAttribute readSyntheticAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        return new CLSyntheticAttribute(attributeNameIndex, attributeLength);
    }

    /**
     * Read a Signature attribute from the specified input stream, and return
     * it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a Signature attribute.
     */

    private CLSignatureAttribute readSignatureAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        CLSignatureAttribute attribute = null;
        try {
            attribute = new CLSignatureAttribute(attributeNameIndex,
                    attributeLength, in.readUnsignedShort());
        } catch (IOException e) {
            reportError("Error reading Signature_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read a SourceFile attribute from the specified input stream, and return
     * it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a SourceFile attribute.
     */

    private CLSourceFileAttribute readSourceFileAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        CLSourceFileAttribute attribute = null;
        try {
            attribute = new CLSourceFileAttribute(attributeNameIndex,
                    attributeLength, in.readUnsignedShort());
        } catch (IOException e) {
            reportError("Error reading SourceFile_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read a SourceDebugExtension attribute from the specified input stream,
     * and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a SourceDebugExtension attribute.
     */

    private CLSourceDebugExtensionAttribute readSourceDebugExtensionAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLSourceDebugExtensionAttribute attribute = null;
        try {
            byte[] b = new byte[(int) attributeLength];

            in.read(b);
            attribute = new CLSourceDebugExtensionAttribute(attributeNameIndex,
                    attributeLength, b);
        } catch (IOException e) {
            reportError("Error reading SourceDebugExtension_attribute "
                    + "from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a LineNumberTable attribute from the specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a LineNumberTable attribute.
     */

    private CLLineNumberTableAttribute readLineNumberTableAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLLineNumberTableAttribute attribute = null;
        try {
            int lineNumberTableLength = in.readUnsignedShort();
            ArrayList<CLLineNumberInfo> lineNumberTable = new ArrayList<CLLineNumberInfo>();
            for (int m = 0; m < lineNumberTableLength; m++) {
                lineNumberTable.add(new CLLineNumberInfo(
                        in.readUnsignedShort(), in.readUnsignedShort()));
            }
            attribute = new CLLineNumberTableAttribute(attributeNameIndex,
                    attributeLength, lineNumberTableLength, lineNumberTable);
        } catch (IOException e) {
            reportError("Error reading LineNumberTable_attribute from file %s",
                    className);
        }
        return attribute;
    }

    /**
     * Read a LocalVariableTable attribute from the specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a LocalVariableTable attribute.
     */

    private CLLocalVariableTableAttribute readLocalVariableTableAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLLocalVariableTableAttribute attribute = null;
        try {
            int localVariableTableLength = in.readUnsignedShort();
            ArrayList<CLLocalVariableInfo> localVariableTable = new ArrayList<CLLocalVariableInfo>();
            for (int m = 0; m < localVariableTableLength; m++) {
                localVariableTable.add(new CLLocalVariableInfo(in
                        .readUnsignedShort(), in.readUnsignedShort(), in
                        .readUnsignedShort(), in.readUnsignedShort(), in
                        .readUnsignedShort()));
            }
            attribute = new CLLocalVariableTableAttribute(attributeNameIndex,
                    attributeLength, localVariableTableLength,
                    localVariableTable);
        } catch (IOException e) {
            reportError("Error reading LocalVariableTable_attribute "
                    + "from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a LocalVariableTypeTable attribute from the specified input stream,
     * and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a LocalVariableTypeTable attribute.
     */

    private CLLocalVariableTypeTableAttribute readLocalVariableTypeTableAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLLocalVariableTypeTableAttribute attribute = null;
        try {
            int localVariableTypeTableLength = in.readUnsignedShort();
            ArrayList<CLLocalVariableTypeInfo> localVariableTypeTable = new ArrayList<CLLocalVariableTypeInfo>();
            for (int m = 0; m < localVariableTypeTableLength; m++) {
                localVariableTypeTable.add(new CLLocalVariableTypeInfo(in
                        .readUnsignedShort(), in.readUnsignedShort(), in
                        .readUnsignedShort(), in.readUnsignedShort(), in
                        .readUnsignedShort()));
            }
            attribute = new CLLocalVariableTypeTableAttribute(
                    attributeNameIndex, attributeLength,
                    localVariableTypeTableLength, localVariableTypeTable);
        } catch (IOException e) {
            reportError("Error reading LocalVariableTypeTable_attribute"
                    + "file %s", className);
        }
        return attribute;
    }

    /**
     * Read a Deprecated attribute from the specified input stream, and return
     * it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a Deprecated attribute.
     */

    private CLDeprecatedAttribute readDeprecatedAttribute(CLInputStream in,
            int attributeNameIndex, long attributeLength) {
        return new CLDeprecatedAttribute(attributeNameIndex, attributeLength);
    }

    /**
     * Read a RuntimeVisibleAnnotations attribute from the specified input
     * stream, and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a RuntimeVisibleAnnotations attribute.
     */

    private CLRuntimeVisibleAnnotationsAttribute readRuntimeVisibleAnnotationsAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLRuntimeVisibleAnnotationsAttribute attribute = null;
        try {
            int numAnnotations = in.readUnsignedShort();
            ArrayList<CLAnnotation> annotations = new ArrayList<CLAnnotation>();
            for (int i = 0; i < numAnnotations; i++) {
                CLAnnotation annotation = readAnnotation(in);
                annotations.add(annotation);
            }
            attribute = new CLRuntimeVisibleAnnotationsAttribute(
                    attributeNameIndex, attributeLength, numAnnotations,
                    annotations);
        } catch (IOException e) {
            reportError("Error reading RuntimeVisibleAnnotations_attribute"
                    + "from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a RuntimeInvisibleAnnotations attribute from the specified input
     * stream, and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a RuntimeInvisibleAnnotations attribute.
     */

    private CLRuntimeInvisibleAnnotationsAttribute readRuntimeInvisibleAnnotationsAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLRuntimeInvisibleAnnotationsAttribute attribute = null;
        try {
            int numAnnotations = in.readUnsignedShort();
            ArrayList<CLAnnotation> annotations = new ArrayList<CLAnnotation>();
            for (int i = 0; i < numAnnotations; i++) {
                CLAnnotation annotation = readAnnotation(in);
                annotations.add(annotation);
            }
            attribute = new CLRuntimeInvisibleAnnotationsAttribute(
                    attributeNameIndex, attributeLength, numAnnotations,
                    annotations);
        } catch (IOException e) {
            reportError("Error reading RuntimeInvisibleAnnotations_attribute"
                    + "from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a RuntimeVisibleParameterAnnotations attribute from the specified
     * input stream, and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a RuntimeVisibleParameterAnnotations attribute.
     */

    private CLRuntimeVisibleParameterAnnotationsAttribute readRuntimeVisibleParameterAnnotationsAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLRuntimeVisibleParameterAnnotationsAttribute attribute = null;
        try {
            int numParameters = in.readUnsignedByte();
            ArrayList<CLParameterAnnotationInfo> parameterAnnotations = new ArrayList<CLParameterAnnotationInfo>();
            for (int i = 0; i < numParameters; i++) {
                int numAnnotations = in.readUnsignedShort();
                ArrayList<CLAnnotation> annotations = new ArrayList<CLAnnotation>();
                for (int j = 0; j < numAnnotations; j++) {
                    CLAnnotation annotation = readAnnotation(in);
                    annotations.add(annotation);
                }
                parameterAnnotations.add(new CLParameterAnnotationInfo(
                        numAnnotations, annotations));
            }
            attribute = new CLRuntimeVisibleParameterAnnotationsAttribute(
                    attributeNameIndex, attributeLength, (short) numParameters,
                    parameterAnnotations);
        } catch (IOException e) {
            reportError("Error reading "
                    + "RuntimeVisibleParameterAnnotations_attribute"
                    + " from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a RuntimeInvisibleParameterAnnotations attribute from the specified
     * input stream, and return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a RuntimeInvisibleParameterAnnotations attribute.
     */

    private CLRuntimeInvisibleParameterAnnotationsAttribute readRuntimeInvisibleParameterAnnotationsAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        CLRuntimeInvisibleParameterAnnotationsAttribute attribute = null;
        try {
            int numParameters = in.readUnsignedByte();
            ArrayList<CLParameterAnnotationInfo> parameterAnnotations = new ArrayList<CLParameterAnnotationInfo>();
            for (int i = 0; i < numParameters; i++) {
                int numAnnotations = in.readUnsignedShort();
                ArrayList<CLAnnotation> annotations = new ArrayList<CLAnnotation>();
                for (int j = 0; j < numAnnotations; j++) {
                    CLAnnotation annotation = readAnnotation(in);
                    annotations.add(annotation);
                }
                parameterAnnotations.add(new CLParameterAnnotationInfo(
                        numAnnotations, annotations));
            }
            attribute = new CLRuntimeInvisibleParameterAnnotationsAttribute(
                    attributeNameIndex, attributeLength, (short) numParameters,
                    parameterAnnotations);
        } catch (IOException e) {
            reportError("Error reading "
                    + "RuntimeInvisibleParameterAnnotations_attribute"
                    + " from file %s", className);
        }
        return attribute;
    }

    /**
     * Read a AnnotationDefault attribute from t he specified input stream, and
     * return it.
     * 
     * @param in
     *            input stream.
     * @param attributeNameIndex
     *            constant pool index of the attribute name.
     * @param attributeLength
     *            length of attribute.
     * @return a AnnotationDefault attribute.
     */

    private CLAnnotationDefaultAttribute readAnnotationDefaultAttribute(
            CLInputStream in, int attributeNameIndex, long attributeLength) {
        return new CLAnnotationDefaultAttribute(attributeNameIndex,
                attributeLength, readElementValue(in));
    }

    /**
     * Read an ElementValue from the specified input stream, and return it.
     * 
     * @param in
     *            input stream.
     * @return an ElemenvValue.
     */

    private CLElementValue readElementValue(CLInputStream in) {
        CLElementValue elementValue = null;
        try {
            int tag = in.readUnsignedByte();
            switch (tag) {
            case ELT_B:
            case ELT_C:
            case ELT_D:
            case ELT_F:
            case ELT_I:
            case ELT_J:
            case ELT_S:
            case ELT_Z:
            case ELT_s:
                elementValue = new CLElementValue((short) tag, in
                        .readUnsignedShort());
                break;
            case ELT_e:
                elementValue = new CLElementValue(in.readUnsignedShort(), in
                        .readUnsignedShort());
                break;
            case ELT_c:
                elementValue = new CLElementValue(in.readUnsignedShort());
                break;
            case ELT_ANNOTATION:
                elementValue = new CLElementValue(readAnnotation(in));
                break;
            case ELT_ARRAY:
                int numValues = in.readUnsignedShort();
                ArrayList<CLElementValue> values = new ArrayList<CLElementValue>();
                for (int i = 0; i < numValues; i++) {
                    values.add(readElementValue(in));
                }
                elementValue = new CLElementValue(numValues, values);
            }
        } catch (IOException e) {
            reportError("Error reading AnnotationDefault_attribute "
                    + "from file %s", className);
        }
        return elementValue;
    }

    /**
     * Read an Annotation from the specified input stream, and return it.
     * 
     * @param in
     *            input stream.
     * @return an Annotation.
     */

    private CLAnnotation readAnnotation(CLInputStream in) {
        CLAnnotation annotation = null;
        try {
            int typeIndex = in.readUnsignedShort();
            int numElementValuePairs = in.readUnsignedShort();
            ArrayList<CLElementValuePair> elementValuePairs = new ArrayList<CLElementValuePair>();
            for (int i = 0; i < numElementValuePairs; i++) {
                int elementNameIndex = in.readUnsignedShort();
                CLElementValue value = readElementValue(in);
                elementValuePairs.add(new CLElementValuePair(elementNameIndex,
                        value));
            }
            annotation = new CLAnnotation(typeIndex, numElementValuePairs,
                    elementValuePairs);
        } catch (IOException e) {
            reportError("Error reading Annotation from file %s", className);
        }
        return annotation;
    }

    /**
     * Construct a CLAbsorber object given the (fully-qualified) name of the
     * class file to read.
     * 
     * @param className
     *            fully qualified name of the input class file.
     */

    public CLAbsorber(String className) {
        try {
            this.className = className;
            CLPath classPath = new CLPath();
            CLInputStream in = classPath.loadClass(className);
            errorHasOccurred = false;
            if (in == null) {
                reportError("Error loading %s", className);
                return;
            }
            classFile = new CLFile();

            // Read magic number (0xCAFEBABE)
            long magic = in.readUnsignedInt();
            if (magic != MAGIC) {
                reportWarning("%s has an invalid magic number", className);
                return;
            }
            classFile.magic = magic;

            // Read minor version, major version
            classFile.minorVersion = in.readUnsignedShort();
            classFile.majorVersion = in.readUnsignedShort();

            // Read constant pool count, constant pool
            classFile.constantPoolCount = in.readUnsignedShort();
            classFile.constantPool = readConstantPool(in);
            if (errorHasOccurred()) {
                return;
            }

            // Read access flags for the class
            classFile.accessFlags = in.readUnsignedShort();

            // Read this class' constant pool index
            classFile.thisClass = in.readUnsignedShort();

            // Read super class' constant pool index
            classFile.superClass = in.readUnsignedShort();

            // Read interfaces (implemented by the class being
            // read) count and interfaces (constant pool
            // indices)
            int interfacesCount = in.readUnsignedShort();
            ArrayList<Integer> interfaces = new ArrayList<Integer>();
            classFile.interfacesCount = interfacesCount;
            for (int i = 0; i < interfacesCount; i++) {
                interfaces.add(in.readUnsignedShort());
            }
            classFile.interfaces = interfaces;

            // Read fields count and fields
            classFile.fieldsCount = in.readUnsignedShort();
            classFile.fields = readFields(in, classFile.fieldsCount);
            if (errorHasOccurred()) {
                return;
            }

            // Read methods count and methods
            classFile.methodsCount = in.readUnsignedShort();
            classFile.methods = readMethods(in, classFile.methodsCount);
            if (errorHasOccurred()) {
                return;
            }

            // Read class attributes
            classFile.attributesCount = in.readUnsignedShort();
            classFile.attributes = readAttributes(in, classFile.attributesCount);
        } catch (EOFException e) {
            reportError("Unexpected end of file %s", className);
        } catch (IOException e) {
            reportError("Error reading file %s", className);
        }
    }

    /**
     * Return the CLFile representation of the class that was read.
     * 
     * @return the CLFile representation of the class.
     */

    public CLFile classFile() {
        return classFile;
    }

    /**
     * Return true if an error had occurred while reading the class; false
     * otherwise.
     * 
     * @return true or false.
     */

    public boolean errorHasOccurred() {
        return errorHasOccurred;
    }

    /**
     * Driver for CLAbsorber. It accepts the (fully-qualified) name of a class
     * file as command-line argument and dumps its (ClassFile) structure --
     * CLFile in our representation -- to STDOUT in a format similar to that of
     * javap.
     */

    public static void main(String[] args) {
        String classFile = "";
        if (args.length == 1) {
            classFile = args[0];
        } else {
            String usage = "Usage: java jminusminus.CLAbsorber <class name>\n"
                    + "Where the class name must be fully qualified; "
                    + "eg, java/util/ArrayList";
            System.out.println(usage);
            System.exit(0);
        }
        CLAbsorber r = new CLAbsorber(classFile);
        if (!r.errorHasOccurred()) {
            CLFile c = r.classFile();
            c.writeToStdOut();
        }
    }

}

/**
 * Inherits from java.io.DataInputStream and provides an extra function for
 * reading unsigned int from the input stream, which is required for reading
 * Java class files.
 */

class CLInputStream extends DataInputStream {

    /**
     * Construct a CLInputStream object from the specified input stream.
     * 
     * @param in
     *            input stream.
     */

    public CLInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read four input bytes and return a long value in the range 0 through
     * 4294967295. Let a, b, c, d be the four bytes. The value returned is:
     * 
     * <pre>
     *   ( b[ 0 ] &amp; 0xFF ) &lt;&lt; 24 ) | 
     *   ( ( b[ 1 ] &amp; 0xFF ) &lt;&lt; 16 ) | 
     *   ( ( b[ 2 ] &amp; 0xFF ) &lt;&lt; 8 ) | 
     *   ( b[ 3 ] &amp; 0xFF )
     * </pre>
     * 
     * @return the unsigned 32-bit value.
     * @exception EOFException
     *                if this stream reaches the end before reading all the
     *                bytes.
     * @exception IOException
     *                if an I/O error occurs.
     */

    public final long readUnsignedInt() throws IOException {
        byte[] b = new byte[4];
        long mask = 0xFF, l;
        in.read(b);
        l = ((b[0] & mask) << 24) | ((b[1] & mask) << 16)
                | ((b[2] & mask) << 8) | (b[3] & mask);
        return l;
    }

}
