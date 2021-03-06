package org.cf.simplify.strategy;

import static org.junit.Assert.assertEquals;
import gnu.trove.map.TIntObjectMap;

import org.cf.simplify.ExecutionGraphManipulator;
import org.cf.simplify.OptimizerTester;
import org.cf.smalivm.VMTester;
import org.cf.smalivm.context.HeapItem;
import org.cf.smalivm.type.UninitializedInstance;
import org.cf.smalivm.type.UnknownValue;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.util.ReferenceUtil;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Enclosed.class)
public class TestPeepholeStrategy {

    private static final String CLASS_NAME = "Lpeephole_strategy_test;";

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TestPeepholeStrategy.class.getSimpleName());

    private static ExecutionGraphManipulator getOptimizedGraph(String methodName, Object... args) {
        TIntObjectMap<HeapItem> initial = VMTester.buildRegisterState(args);
        ExecutionGraphManipulator mbgraph = OptimizerTester.getGraphManipulator(CLASS_NAME, methodName, initial);
        PeepholeStrategy strategy = new PeepholeStrategy(mbgraph);
        strategy.perform();

        return mbgraph;
    }

    public static class TestConstantPredicate {

        private static final String METHOD_NAME = "constantPredicate()I";

        @Test
        public void testConstantPredicateReplacedWithUnconditionalBranch() {
            // I say phrases like "unconditional branch" instead of "goto".
            // I'm also a riot at dinner parties.
            ExecutionGraphManipulator mbgraph = getOptimizedGraph(METHOD_NAME);

            BuilderInstruction instruction = mbgraph.getInstruction(1);

            assertEquals(Opcode.GOTO_32, instruction.getOpcode());
            assertEquals(4, ((OffsetInstruction) instruction).getCodeOffset());
        }

    }

    public static class TestPeepClassForName {

        private static final int ADDRESS = 0;
        private static final String METHOD_NAME = "classForName()V";

        private void testForExpectedInstruction(String register0, String expectedClassName) {
            ExecutionGraphManipulator mbgraph = getOptimizedGraph(METHOD_NAME, 0, register0, "Ljava/lang/String;");

            BuilderInstruction21c instruction = (BuilderInstruction21c) mbgraph.getInstruction(ADDRESS);
            assertEquals(Opcode.CONST_CLASS, instruction.getOpcode());
            assertEquals(0, instruction.getRegisterA());

            String actualClassName = ReferenceUtil.getReferenceString(instruction.getReference());
            assertEquals(expectedClassName, actualClassName);
        }

        @Test
        public void testInvokeClassForNameForImaginaryClassIsReplaced() {
            testForExpectedInstruction("com.funky.imaginary.class", "Lcom/funky/imaginary/class;");
        }

        @Test
        public void testInvokeClassForNameForKnownClassIsReplaced() {
            testForExpectedInstruction("java.lang.String", "Ljava/lang/String;");
        }

        @Test
        public void testInvokeClassForNameForLocalClassIsReplaced() {
            testForExpectedInstruction("peephole_strategy_test", "Lpeephole_strategy_test;");
        }

        @Test
        public void testInvokeClassForNameForUnknownValueIsNotReplaced() {
            ExecutionGraphManipulator mbgraph = getOptimizedGraph(METHOD_NAME, 0, new UnknownValue(), "Ljava/lang/String;");
            Instruction35c instruction = (Instruction35c) mbgraph.getInstruction(ADDRESS);
            String methodDescriptor = ReferenceUtil.getMethodDescriptor((MethodReference) instruction.getReference());

            assertEquals("Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;", methodDescriptor);
        }
    }

    public static class TestStringInit {

        private static final int ADDRESS = 0;
        private static final String METHOD_NAME = "stringInit()V";
        private static final String ZENSUNNI_POEM = "Sand keeps the skin clean, and the mind.";

        private void testForExpectedInstruction(Object register1, String expectedConstant) {
            ExecutionGraphManipulator mbgraph = getOptimizedGraph(METHOD_NAME, 0, new UninitializedInstance(
                            "Ljava/lang/String;"), "Ljava/lang/String;", 1, register1, "[B");

            BuilderInstruction21c instruction = (BuilderInstruction21c) mbgraph.getInstruction(ADDRESS);
            assertEquals(Opcode.CONST_STRING, instruction.getOpcode());
            assertEquals(0, instruction.getRegisterA());

            String actualConstant = ((StringReference) instruction.getReference()).getString();
            assertEquals(expectedConstant, actualConstant);
        }

        @Test
        public void testStringInitWithKnownStringIsReplaced() {
            testForExpectedInstruction(ZENSUNNI_POEM.getBytes(), ZENSUNNI_POEM);
        }

        @Test
        public void testStringInitWithUnknownValueIsNotReplaced() {
            ExecutionGraphManipulator mbgraph = getOptimizedGraph(METHOD_NAME, 0, new UninitializedInstance(
                            "Ljava/lang/String;"), "Ljava/lang/String;", 1, new UnknownValue(), "[B");
            Instruction35c instruction = (Instruction35c) mbgraph.getInstruction(ADDRESS);
            String methodDescriptor = ReferenceUtil.getMethodDescriptor((MethodReference) instruction.getReference());

            assertEquals("Ljava/lang/String;-><init>([B)V", methodDescriptor);
        }
    }

}
