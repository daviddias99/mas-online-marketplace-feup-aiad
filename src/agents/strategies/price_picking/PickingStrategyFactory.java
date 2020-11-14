package agents.strategies.price_picking;

public class PickingStrategyFactory {
    enum Type {
        MIN,
        TEST
    }

    public static PricePickingStrategy get(String typeStr) throws IllegalArgumentException {
        Type type = Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case MIN:
                return new MinPickingStrategy();
            case TEST:
                return new TestPickingStrategy();
        }

        return null;
    }
}
