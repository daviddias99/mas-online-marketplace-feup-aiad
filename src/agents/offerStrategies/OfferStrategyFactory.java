package agents.offerStrategies;

import agents.pricePickingStrategies.MinPickingStrategy;
import agents.pricePickingStrategies.PickingStrategyFactory;
import agents.pricePickingStrategies.PricePickingStrategy;
import agents.pricePickingStrategies.TestPickingStrategy;

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
