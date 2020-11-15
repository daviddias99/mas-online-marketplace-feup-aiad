package agents.strategies.price_picking;

public class PickingStrategyFactory {
    enum Type {
        TEST
    }

    public static PricePickingStrategy get(String typeStr) throws IllegalArgumentException {
        Type type = Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case TEST:
                return new SmartPickingStrategy();
        }

        return null;
    }
}
