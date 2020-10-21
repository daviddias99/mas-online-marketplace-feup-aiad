# Online Second-Hand Marketplace Simulation using Multi-agent Systems

The proposed multi-agent system will represent a **second-hand online marketplace** (similar to [OLX](olx.pt)  with two types of agents: **buyers** and **sellers**, each trying to solve their own minimization problem. The former tries to minimize the amount of money spent, while the latter seeks to minimize the loss from the original item price.

Sellers must set an initial price at which they want to sell an item, knowing that other sellers might be selling the same item at a different price. This price is also affected by the cost of the product in a non-second-hand market, i.e. if a product is being sold at a higher price than its original counterpart, there is no incentive to buy it on the platform.

 In addition, each item has a certain number of buyers which intend to buy it. These two properties create an environment that is guided by the supply and demand simulating a simple economic model of price determination, where there is a constant fluctuation and negotiation of the price of the products.
 
Furthermore, they also have the ability to “scam” their clients by keeping the item and the money. This, although extremely profitable, can lead to losing credibility, resulting in fewer sales in the future. This credibility factor weighs in the buyer’s decision process regarding the choice of a seller, contributing to the dynamism of the environment, where the chosen seller is not exclusively determined by the lowest price.

Logically the platform wants to have as few scams as possible because its own reputation is on the line.

Regarding the communication **protocols**, agents will need to advertise the products which they are selling/seeking, in order to attract potential buyers/sellers. This advertisement can also be used by other sellers in order to define the price at which they will set their products. Also, the different agents will implement a negotiating protocol, which deals with the process of haggling the price of an item.

Both the sellers and buyers can implement different **strategies** for negotiating prices and choosing a retailer, respectively. A buyer can take a naïve approach, choosing the best bargain, disregarding the credibility factor. Agents may instead attribute a different weight to the credibility factor, deciding if they choose (or not) a vendor despite the offered price. A seller’s strategy is composed of two factors, one regarding the decision to scam the buyer which may alter both the advertised price and the negotiating strategy; and one regarding the choice of an advertising price in consequence of the market prices of that item.

| Independent Variables        | Dependent Variables           | 
| ------------- |:-------------:| 
| Influence of the seller’s credibility in the buyer’s decision     | Number of successful scams| 
| Product’s original market price      | Product’s avg. platform price      | 
| Number of buyers of a given product | Money saved (buyers)     |  
| Number of sellers of a given product | Money earned (sellers) |

### Notes

Money saved -> usar valor pessoal que o produto tenha para o agente
Computational Trust
Credibilidade do comprador -> não paga a totalidade do que diz que paga

