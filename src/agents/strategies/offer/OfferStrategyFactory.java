package agents.strategies.offer;

public class OfferStrategyFactory {
    enum Type {
        TEST
    }

    public static OfferStrategy get(String typeStr) throws IllegalArgumentException {
        OfferStrategyFactory.Type type = OfferStrategyFactory.Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case TEST:
                return new TestOfferStrategy();
        }

        return null;
    }
}
