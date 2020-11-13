package utils;

import java.util.Random;

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

    public static float round(float value, int places) {
        float scale = (float) Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static int randomBetween(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return Util.getInstance().nextInt((max - min) + 1) + min;
    }

    public static float getNormalRandom(float avg, float std){

        return (float) Util.getInstance().nextGaussian() * std + avg;
    }
}
