package olx.agents.strategies.price_picking;

public class PickingStrategyFactory {
    enum Type {
        SMART,
        NAIVE
    }

    public static PricePickingStrategy get(String typeStr) throws IllegalArgumentException {
        Type type = Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case SMART:
                return new SmartPickingStrategy();
            case NAIVE:
                return new NaivePickingStrategy();
            default:
                throw new IllegalArgumentException("Strategy " + typeStr + " does not exist");
        }

    }
}
