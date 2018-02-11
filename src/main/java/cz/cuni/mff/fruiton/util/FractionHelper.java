package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.dto.GameProtos;

public final class FractionHelper {

    private FractionHelper() {
    }

    public static String getName(final GameProtos.Fraction fraction) {
        switch (fraction) {
            case NONE:
                return "None";
            case CRANBERRY_CRUSADE:
                return "Cranberry crusade";
            case GUACAMOLE_GUERILLAS:
                return "Guacamole guerillas";
            case TZATZIKI_TSARDOM:
                return "Tzatziki tsardom";
            default:
                throw new IllegalArgumentException("Unknown fraction " + fraction);
        }
    }

}
