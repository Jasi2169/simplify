package org.cf.smalivm.opcode;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.cf.smalivm.VMTester;
import org.cf.smalivm.VirtualMachine;
import org.cf.smalivm.context.ExecutionGraph;
import org.cf.smalivm.context.ExecutionNode;
import org.cf.smalivm.context.HeapItem;
import org.cf.smalivm.context.MethodState;
import org.cf.smalivm.type.UnknownValue;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class TestIfOp {

    public static class UnitTest {

        private static final int ADDRESS = 0;
        private static final int ARG1_REGISTER = 2;
        private static final int ARG2_REGISTER = 4;

        private TIntObjectMap<MethodLocation> addressToLocation;

        private BuilderInstruction instruction;

        private MethodLocation location;

        private MethodState mState;
        private ExecutionNode node;
        private IfOp op;
        private IfOpFactory opFactory;
        private VirtualMachine vm;

        @Before
        public void setUp() {
            vm = mock(VirtualMachine.class);
            mState = mock(MethodState.class);
            node = mock(ExecutionNode.class);
            location = mock(MethodLocation.class);
            when(location.getCodeAddress()).thenReturn(ADDRESS);

            addressToLocation = new TIntObjectHashMap<MethodLocation>();
            addressToLocation.put(ADDRESS, location);

            opFactory = new IfOpFactory();
        }

        @Test
        public void hasExpectedToStringValue() {
            int value = 0;
            VMTester.addHeapItem(mState, ARG1_REGISTER, value, "D");
            VMTester.addHeapItem(mState, ARG2_REGISTER, value, "D");

            instruction = buildInstruction22t(Opcode.IF_GE, 0);
            op = (IfOp) opFactory.create(location, addressToLocation, vm);
            op.execute(node, mState);

            assertEquals("if-ge r2, r4, #0", op.toString());
        }

        private BuilderInstruction buildInstruction22t(Opcode opcode, int offset) {
            BuilderInstruction instruction = mock(BuilderInstruction.class,
                            withSettings().extraInterfaces(Instruction22t.class));
            when(location.getInstruction()).thenReturn(instruction);
            when(instruction.getLocation()).thenReturn(location);
            when(instruction.getCodeUnits()).thenReturn(0);
            when(instruction.getOpcode()).thenReturn(opcode);
            when(((Instruction22t) instruction).getRegisterA()).thenReturn(ARG1_REGISTER);
            when(((Instruction22t) instruction).getRegisterB()).thenReturn(ARG2_REGISTER);
            when(((Instruction22t) instruction).getCodeOffset()).thenReturn(offset);

            return instruction;
        }
    }

    public static class TestCompareObjectReferences {

        @Test
        public void testIdenticalObjectReferencesAreEqual() {
            String methodSignature = "IfEqual()V";
            String obj = "object string";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, obj, "Ljava/lang/String;", 1, obj,
                            "Ljava/lang/String;");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfFalseBooleanEqualZero() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, Boolean.FALSE, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualWithStringAndArrayReferenceIsTrue() {
            String methodSignature = "IfNotEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, "object string", "Ljava/lang/String;", 1,
                            new int[0], "[I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualWithTwoDifferentStringReferencesIsTrue() {
            String methodSignature = "IfNotEqual()V";
            String obj1 = "object string";
            // Need to get crafty or javac will be smart enough to use same literal for both objects
            String obj2 = new StringBuilder(obj1).toString();
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, obj1, "Ljava/lang/String;", 1, obj2,
                            "Ljava/lang/String;");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNullEqualZero() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, null, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfObjectNotEqualZero() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, "object string", "Ljava/lang/String;");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfPrimitiveArrayNotEqualZero() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, new byte[] { 0x1 }, "[B");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfTrueBooleanNotEqualZero() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, Boolean.TRUE, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfZeroIntegerEqualZero() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, Integer.valueOf(0), "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfUnknownIntegerTakesBothPaths() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, new UnknownValue(), "I");
            ExecutionGraph graph = VMTester.execute(CLASS_NAME, methodSignature, initial);
            int[] addresses = graph.getAddresses();
            TIntList expectedVisits = new TIntArrayList(IF_ALL_VISITATIONS);
            TIntList actualVisits = new TIntArrayList();
            for (int address : addresses) {
                if (!graph.wasAddressReached(address)) {
                    continue;
                }
                actualVisits.add(address);
            }
            actualVisits.reverse();

            assertEquals(expectedVisits, actualVisits);
            assertEquals(1, graph.getNodePile(ADDRESS_NOP).size());
            // Two execution paths hit return
            assertEquals(2, graph.getNodePile(ADDRESS_RETURN).size());
        }
    }

    public static class TestIdenticalPrimitiveValueTypes {

        @Test
        public void testIfEqualWithOneAndZeroIsFalse() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithTwoEqualIntegersIsTrue() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithTwoUnequalIntegersIsFalse() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithZeroAndZeroIsTrue() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualZeroWith0ByteIsTrue() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 0x0, "B");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualWith0And0IsTrue() {
            String methodSignature = "IfGreaterOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualWith0And1IsFalse() {
            String methodSignature = "IfGreaterOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualWith1And0IsTrue() {
            String methodSignature = "IfGreaterOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualWithOneAndZeroIsTrue() {
            String methodSignature = "IfGreaterOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualZeroWithNegativeOneIsFalse() {
            String methodSignature = "IfGreaterOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, -1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualZeroWithOneIsTrue() {
            String methodSignature = "IfGreaterOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterOrEqualZeroWithZeroIsTrue() {
            String methodSignature = "IfGreaterOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterThanWith0And1IsFalse() {
            String methodSignature = "IfGreaterThan()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfGreaterThanWith1And0IsTrue() {
            String methodSignature = "IfGreaterThan()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfGreaterThanZeroWith0IsFalse() {
            String methodSignature = "IfGreaterThanZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfGreaterThanZeroWithOneIsTrue() {
            String methodSignature = "IfGreaterThanZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualWith0And0IsTrue() {
            String methodSignature = "IfLessOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualWith0And1IsTrue() {
            String methodSignature = "IfLessOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualWith1And0IsFalse() {
            String methodSignature = "IfLessOrEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualZeroWith0IsTrue() {
            String methodSignature = "IfLessOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualZeroWithNegative1IsTrue() {
            String methodSignature = "IfLessOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, -1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessOrEqualZeroWithOneIsFalse() {
            String methodSignature = "IfLessOrEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfLessThanWith0And1IsFalse() {
            String methodSignature = "IfLessThan()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfLessThanWith0And1IsTrue() {
            String methodSignature = "IfLessThan()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfLessThanZeroWith0IsFalse() {
            String methodSignature = "IfLessThanZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfLessThanZeroWithNegativeOneIsTrue() {
            String methodSignature = "IfLessThanZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, -1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualWith0And0IsFalse() {
            String methodSignature = "IfNotEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualWith0And1IsTrue() {
            String methodSignature = "IfNotEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I", 1, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualZeroWith0IsFalse() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 0, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_FALSE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualZeroWith7ByteIsTrue() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 0x7, "B");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualZeroWithOneIsTrue() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }
    }

    public static class TestValueTypeCombinations {

        @Test
        public void testIfEqualWithBooleanAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, true, "Z", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndDouble() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, 1D, "D");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndFloat() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, 1F, "F");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndLong() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, 1L, "J");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithByteAndShort() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (byte) 1, "B", 1, (short) 1, "S");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithDoubleAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1D, "D", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithDoubleAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1D, "D", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithFloatAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1F, "F", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithFloatAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1F, "F", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithFloatAndDouble() {
            // 3.2 is tricky, 3.2F != 3.2D && Double.compareTo(3.2F, 3.2D)
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 3.2F, "F", 1, 3.2D, "D");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndByte() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, (byte) 1, "B");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndDouble() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 1D, "D");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndFloat() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 1F, "F");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndLong() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, 1L, "J");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithIntAndShort() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1, "I", 1, (short) 1, "S");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithLongAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1L, "J", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithLongAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1L, "J", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithLongAndDouble() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1L, "J", 1, 1D, "D");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithLongAndFloat() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, 1L, "J", 1, 1F, "F");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithShortAndBoolean() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (short) 1, "S", 1, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithShortAndChar() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (short) 1, "S", 1, (char) 1, "C");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithShortAndDouble() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (short) 1, "S", 1, 1D, "D");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithShortAndFloat() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (short) 1, "S", 1, 1F, "F");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualWithShortAndLong() {
            String methodSignature = "IfEqual()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, (short) 1, "S", 1, 1L, "J");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfEqualZeroWithFalseIsTrue() {
            String methodSignature = "IfEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, false, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }

        @Test
        public void testIfNotEqualZeroWithTrueIsTrue() {
            String methodSignature = "IfNotEqualZero()V";
            TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(0, true, "Z");
            VMTester.testVisitation(CLASS_NAME, methodSignature, initial, IF_TRUE_VISITATIONS);
        }
    }

    private static final int ADDRESS_IF = 0;
    private static final int ADDRESS_NOP = 2;
    private static final int ADDRESS_RETURN = 3;

    private static final String CLASS_NAME = "Lif_test;";

    private static final int[] IF_FALSE_VISITATIONS = new int[] { ADDRESS_IF, ADDRESS_NOP, ADDRESS_RETURN };

    private static final int[] IF_TRUE_VISITATIONS = new int[] { ADDRESS_IF, ADDRESS_RETURN };

    private static final int[] IF_ALL_VISITATIONS = new int[] { ADDRESS_IF, ADDRESS_NOP, ADDRESS_RETURN };

}
