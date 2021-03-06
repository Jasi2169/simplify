package org.cf.simplify;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cf.simplify.strategy.ConstantPropigationStrategy;
import org.cf.simplify.strategy.DeadRemovalStrategy;
import org.cf.simplify.strategy.OptimizationStrategy;
import org.cf.simplify.strategy.PeepholeStrategy;
import org.cf.simplify.strategy.UnreflectionStrategy;
import org.cf.smalivm.VirtualMachine;
import org.cf.smalivm.context.ExecutionGraph;
import org.jf.dexlib2.util.ReferenceUtil;
import org.jf.dexlib2.writer.builder.BuilderMethod;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Optimizer {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(Optimizer.class.getSimpleName());

    private static final Map<String, Integer> totalOptimizationCounts = new HashMap<String, Integer>();

    private final ExecutionGraphManipulator mbgraph;
    private final String methodDescriptor;
    private final List<OptimizationStrategy> reoptimizeStrategies;
    private final List<OptimizationStrategy> reexecuteStrategies;
    private final List<OptimizationStrategy> allStrategies;

    private boolean madeAnyChanges;
    private boolean shouldReexecute;
    private Map<String, Integer> optimizationCounts;

    public Optimizer(ExecutionGraph graph, BuilderMethod method, VirtualMachine vm, DexBuilder dexBuilder, Options opts) {
        methodDescriptor = ReferenceUtil.getMethodDescriptor(method);
        mbgraph = new ExecutionGraphManipulator(graph, method, vm, dexBuilder);

        reoptimizeStrategies = new LinkedList<OptimizationStrategy>();
        DeadRemovalStrategy strategy = new DeadRemovalStrategy(mbgraph);
        strategy.setRemoveWeak(opts.isRemoveWeak());
        reoptimizeStrategies.add(strategy);
        reoptimizeStrategies.add(new ConstantPropigationStrategy(mbgraph));
        reoptimizeStrategies.add(new PeepholeStrategy(mbgraph));

        // Some strategies may alter semantics. E.g. it's possible to remove method reflection without knowing the
        // result of the reflected method call. This leaves method states in a weird way, i.e. move-result has unknown
        // values. In these cases, re-execute the method to establish semantics.
        reexecuteStrategies = new LinkedList<OptimizationStrategy>();
        reexecuteStrategies.add(new UnreflectionStrategy(mbgraph));

        allStrategies = new LinkedList<OptimizationStrategy>();
        allStrategies.addAll(reoptimizeStrategies);
        allStrategies.addAll(reexecuteStrategies);

        optimizationCounts = new HashMap<String, Integer>();
    }

    public void simplify(int maxSweeps) {
        System.out.println("Simplifying: " + methodDescriptor);

        int sweep = 0;
        madeAnyChanges = false;
        boolean madeChange;
        do {
            madeChange = false;
            for (OptimizationStrategy strategy : reoptimizeStrategies) {
                madeChange |= strategy.perform();
            }
            for (OptimizationStrategy strategy : reexecuteStrategies) {
                shouldReexecute |= strategy.perform();
            }

            madeAnyChanges |= madeChange | shouldReexecute;
            sweep++;
        } while (madeChange && sweep < maxSweeps);

        updateOptimizationCounts();
    }

    public boolean madeChanges() {
        return madeAnyChanges;
    }

    public boolean shouldReexecute() {
        return shouldReexecute;
    }

    private void updateOptimizationCounts() {
        for (OptimizationStrategy strategy : allStrategies) {
            Map<String, Integer> optimizations = strategy.getOptimizationCounts();
            for (String key : optimizations.keySet()) {
                Integer currentCount = optimizationCounts.get(key);
                if (null == currentCount) {
                    currentCount = 0;
                }
                Integer totalCount = totalOptimizationCounts.get(key);
                if (null == totalCount) {
                    totalCount = 0;
                }

                int count = optimizations.get(key);
                currentCount += count;
                optimizationCounts.put(key, currentCount);
                totalCount += count;
                totalOptimizationCounts.put(key, totalCount);
            }
        }
    }

    public String getOptimizationCounts() {
        StringBuilder sb = new StringBuilder("Optimizations: ");
        sb.append(buildOptimizationCounts(optimizationCounts));

        return sb.toString();
    }

    public static String getTotalOptimizationCounts() {
        StringBuilder sb = new StringBuilder("Total optimizations: ");
        sb.append(buildOptimizationCounts(totalOptimizationCounts));

        return sb.toString();
    }

    private static StringBuilder buildOptimizationCounts(Map<String, Integer> counts) {
        List<String> keys = new LinkedList<String>(counts.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append('=').append(counts.get(key)).append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }

        return sb;
    }

}
