package utils;

import java.util.List;
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

        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }


        return Util.getInstance().nextInt(max - min) + min;
    }

    public static float getNormalRandom(float avg, float std){

        return (float) Util.getInstance().nextGaussian() * std + avg;
    }


    public static float average(List<Float> values) {
        if (values.isEmpty())
            return  -1.0f;

        float acc = 0;

        for (Float value : values){
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

    public static boolean floatEqual(float f1, float f2){

        return Math.abs(f1-f2) < 0.01;
    }
}
