package cz.cuni.mff.fruiton.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class RangedProbabilityRandom<T> {

    private final Random random = new Random();

    private final List<T> distributedValues;

    public RangedProbabilityRandom(final List<T> values, final List<Integer> probabilities) {
        if (values == null) {
            throw new IllegalArgumentException("Cannot generate random distributedValues from null distributedValues");
        }
        if (probabilities == null) {
            throw new IllegalArgumentException("Cannot generate random distributedValues from null probabilities");
        }
        if (values.size() != probabilities.size()) {
            throw new IllegalArgumentException("Values and probabilities must have the same size");
        }
        for (int probability : probabilities) {
            if (probability < 0) {
                throw new IllegalArgumentException("Probability cannot be less than 0");
            }
        }

        int sum = probabilities.stream().reduce(0, (x, y) -> x + y);
        distributedValues = new ArrayList<>(sum);

        Iterator<T> valuesIt = values.iterator();
        for (int probability : probabilities) {
            T value = valuesIt.next();
            for (int i = 0; i < probability; i++) {
                distributedValues.add(value);
            }
        }
    }

    public T next() {
        return distributedValues.get(random.nextInt(distributedValues.size()));
    }

}
