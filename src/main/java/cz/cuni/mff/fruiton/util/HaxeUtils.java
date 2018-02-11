package cz.cuni.mff.fruiton.util;

import haxe.root.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class HaxeUtils {

    private HaxeUtils() {
    }

    public static <T> List<T> toList(final Array<T> array) {
        List<T> returnList = new ArrayList<>(array.length);

        for (int i = 0; i < array.length; i++) {
            returnList.add(i, array.__get(i));
        }
        return returnList;
    }

    public static <T> Stream<T> toStream(final Array<T> array) {
        Stream.Builder<T> builder = Stream.builder();

        for (int i = 0; i < array.length; i++) {
            builder.add(array.__get(i));
        }
        return builder.build();
    }

}
