package com.pf.common.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static com.pf.common.constants.CommonConstants.CHUNK_SIZE;

public class BatchUtils {

    public static <T> void processInBatches(List<T> items, Consumer<List<T>> batchProcessor) {
        if (items == null || items.isEmpty()) return;
        for (List<T> chunk : Lists.partition(items, CHUNK_SIZE)) {
            batchProcessor.accept(chunk);
        }
    }

    public static <T, R> List<R> processInBatches(List<T> items, Function<List<T>, List<R>> batchProcessor) {
        List<R> result = Lists.newArrayList();
        if (items == null || items.isEmpty()) return result;
        for (List<T> chunk : Lists.partition(items, CHUNK_SIZE)) {
            List<R> batchResult = batchProcessor.apply(chunk);
            if (batchResult == null || batchResult.isEmpty()) continue;
            result.addAll(batchResult);
        }
        return result;
    }

    public static <T> boolean allBatchesNotPassed(List<T> items, Predicate<List<T>> validator) {
        if (items == null || items.isEmpty()) return false;
        for (List<T> chunk : Lists.partition(items, CHUNK_SIZE)) {
            if (!validator.test(chunk)) return true;
        }
        return false;
    }

    public static <T> int processBatchesAndSum(List<T> items, ToIntFunction<List<T>> batchProcessor) {
        if (items == null || items.isEmpty()) return 0;
        int total = 0;
        for (List<T> chunk : Lists.partition(items, CHUNK_SIZE)) {
            total += batchProcessor.applyAsInt(chunk);
        }
        return total;
    }

}
