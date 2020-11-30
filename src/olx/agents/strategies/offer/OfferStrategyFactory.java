package olx.agents.strategies.offer;

public class OfferStrategyFactory {
    enum Type {
        SMART,
        RELTFT,
        ABSTFT,
    }

    public static OfferStrategy get(String typeStr) throws IllegalArgumentException {
        OfferStrategyFactory.Type type = OfferStrategyFactory.Type.valueOf(typeStr.toUpperCase());

        switch (type) {
            case SMART:
                return new SmartOfferStrategy();
            case RELTFT:
                return new RelativeTFTOfferStrategy();
            case ABSTFT:
                return new RandomAbsoluteTFTOfferStrategy();
        }

        return null;
    }
}
