package olx.utils;


import java.awt.Color;
import java.util.List;
import java.util.Random;

import static olx.agents.strategies.counter_offer.CounterOfferStrategy.*;

public abstract class Util {

    private static Random rng = null;

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    // static method to create instance of Singleton class
    private static Random getInstance() {
        if (rng == null)
            rng = new Random();

        return rng;
    }

    public static final String LIST_FORMAT = "%n - %s";

    public static float round(float value, int places) {
        float scale = (float) Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static int randomBetween(int min, int max) {

        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return Util.getInstance().nextInt(max - min) + min;
    }

    public static float getNormalRandom(float avg, float std) {

        return (float) Util.getInstance().nextGaussian() * std + avg;
    }

    public static float average(List<Float> values) {
        if (values.isEmpty())
            return -1.0f;

        float acc = 0;

        for (Float value : values) {
            acc += value;
        }

        return acc / values.size();
    }

    public static float min(List<Float> values) {
        if (values.isEmpty())
            return -1.0f;

        float min = values.get(0);

        for (Float f : values) {
            if (f < min) {
                min = f;
            }
        }

        return min;
    }

    public static boolean floatEqual(float f1, float f2) {

        return Math.abs(f1 - f2) < 0.01;
    }

    public static float randomFloatBetween(float a, float b) {

        float diff = b - a;
        float r = Util.getInstance().nextFloat() * diff;
        return a + r;
    }

    public static Color getSellerColor(int credibility) {
        float brightness = 0.1f + credibility * 0.9f / 100;
        return Color.getHSBColor(0, 1, brightness);
    }

    public static String localNameToLabel(String localName) {
        char firstChar = localName.charAt(0);

        if (firstChar != 's' && firstChar != 'b')
            return localName;

        String id = localName.substring(localName.lastIndexOf('_') + 1);

        return String.valueOf(firstChar).toUpperCase() + id;
    }

    public static String labelToLocalName(String label) {
        char firstChar = label.charAt(0);

        if (firstChar != 'S' && firstChar != 'B')
            return label;

        String id = label.substring(1);
        String type = "";
        if (firstChar == 'S')
            type = "seller";
        else if (firstChar == 'B')
            type = "buyer";

        return type + "_" + id;
    }

    public static Color getBuyerColor(Type type) {
        switch (type) {
            case SMART:
                return new Color(10,135,84);
            case RELTFT:
                return new Color(50, 97, 114);
            case ABSTFT:
                return new Color(93, 3, 127);
            default:
                return new Color(0, 0, 0);
        }
    }
}
