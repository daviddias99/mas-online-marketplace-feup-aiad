package agents.counterOfferStrategies;

import agents.offerStrategies.OfferStrategy;
import agents.offerStrategies.OfferStrategyFactory;
import agents.offerStrategies.TestOfferStrategy;

public class CounterOfferStrategyFactory {
    enum Type {
        TEST
    }

    public static CounterOfferStrategy get(String typeStr) throws IllegalArgumentException {
        CounterOfferStrategyFactory.Type type = CounterOfferStrategyFactory.Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case TEST:
                return new TestCounterOfferStrategy();
        }

        return null;
    }
}
