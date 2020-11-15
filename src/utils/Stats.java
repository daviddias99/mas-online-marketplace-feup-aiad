package utils;

import models.Product;
import models.Scam;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stats {
    private static int numScams = 0;

    private static Map<Product, List<Float>> productsSold = new HashMap<>();

    public static void incScams() {
        numScams++;
    }

    public static synchronized void productSold(Product product, float price) {
        productsSold.putIfAbsent(product, new LinkedList<>());
        List<Float> prices = productsSold.get(product);
        prices.add(price);
    }

    public static void printStats() {
        String stats = "Scams: " + numScams + "\n" +
                       "Products sold:";

        for (Map.Entry<Product, List<Float>> entry : productsSold.entrySet()) {
            stats += "\n  - " + entry.getKey().getName() + " : " + entry.getValue().size() + " sold : " + Util.average(entry.getValue()) + " avg. price";
        }
    }
}