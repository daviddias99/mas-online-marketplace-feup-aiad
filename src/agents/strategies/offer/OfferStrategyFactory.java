package agents.strategies.offer;

public class OfferStrategyFactory {
    enum Type {
        TEST,
        TURTLE
    }

    public static OfferStrategy get(String typeStr) throws IllegalArgumentException {
        OfferStrategyFactory.Type type = OfferStrategyFactory.Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case TEST:
                return new TestOfferStrategy();
            case TURTLE:
                return new TurtleOfferStrategy();
        }

        return null;
    }
}
