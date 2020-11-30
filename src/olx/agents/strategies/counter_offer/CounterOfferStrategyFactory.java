package olx.agents.strategies.counter_offer;

public class CounterOfferStrategyFactory {
    enum Type {
        SMART,
        RELTFT,
        ABSTFT,
    }

    public static CounterOfferStrategy get(String typeStr) throws IllegalArgumentException {
        CounterOfferStrategyFactory.Type type = CounterOfferStrategyFactory.Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case SMART:
                return new SmartCounterOfferStrategy();
            case RELTFT:
                return new RelativeTFTCounterOfferStrategy();
            case ABSTFT:
                return new RandomAbsoluteTFTCounterOfferStrategy();
        }

        return null;
    }
}
