package olx.utils;

import olx.agents.Buyer;
import olx.agents.Seller;
import olx.models.Product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Stats {
    private static int totalScams = 0;

    private static Map<Seller, Integer> scams = new HashMap<>();
    private static Map<Seller, Float> moneyGainedSellers = new HashMap<>();
    private static Map<Product, List<Float>> productsSold = new HashMap<>();
    private static Map<Buyer, Float> moneySavedBuyers = new HashMap<>();

    private Stats() {
        throw new IllegalStateException("Stats class");
    }

    public static synchronized void scam(Seller seller, float price) {
        totalScams++;
        int nScams = scams.getOrDefault(seller, 0);
        scams.put(seller, nScams + 1);

        float totalEarnings = moneyGainedSellers.getOrDefault(seller, 0.0f);
        moneyGainedSellers.put(seller, totalEarnings + price);
    }

    public static synchronized void productSold(Seller seller, Product product, float price) {
        productsSold.putIfAbsent(product, new LinkedList<>());
        List<Float> prices = productsSold.get(product);
        prices.add(price);

        float totalEarnings = moneyGainedSellers.getOrDefault(seller, 0.0f);
        moneyGainedSellers.put(seller, totalEarnings + price);
    }

    public static synchronized void updateMoneySaved(Buyer buyer) {
        Map<Product, Integer> products = buyer.getProductsBought();
        float savedMoney = - buyer.getMoneySpent();

        for (Map.Entry<Product, Integer> p : products.entrySet()) {
            savedMoney += (p.getKey().getOriginalPrice() * p.getValue());
        }

        moneySavedBuyers.put(buyer, savedMoney);
    }


    public static void printStats() {
        String stats = "Products sold:";
        String format = "\n  - ";

        for (Map.Entry<Product, List<Float>> entry : productsSold.entrySet()) {
            stats += format + entry.getKey().getName() + " : " + entry.getValue().size() + " sold : " + String.format("%.2f", Util.average(entry.getValue())) + " avg. price";
        }

        stats += "\nBuyers:";

        for (Map.Entry<Buyer, Float> entry : moneySavedBuyers.entrySet()) {
            stats += format + entry.getKey().getLocalName() + " : " + String.format("%.2f", entry.getValue()) + "$ saved";
        }

        stats += "\nSellers:";

        for (Map.Entry<Seller, Float> entry : moneyGainedSellers.entrySet()) {
            stats += format + entry.getKey().getLocalName() + " : " + String.format("%.2f", entry.getValue()) + "$ earned";
        }

        stats += "\nScams (total=" + totalScams + "):";

        for (Map.Entry<Seller, Integer> entry : scams.entrySet()) {
            stats += format + entry.getKey().getLocalName() + " : " + entry.getValue();
        }

        System.out.println(stats);
    }
}