package olx.agents.strategies.counter_offer;

public class CounterOfferStrategyFactory {
    public static CounterOfferStrategy get(String typeStr) throws IllegalArgumentException {
        CounterOfferStrategy.Type type = CounterOfferStrategy.Type.valueOf(typeStr.toUpperCase());

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
