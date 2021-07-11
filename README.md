# Online Second-Hand Marketplace Simulation using Multi-agent Systems

**2017/2018** - 4th Year, 1st Semester

**Course:** *Agentes e Inteligência Artificial Distribuida* ([AIAD](https://sigarra.up.pt/feup/en/ucurr_geral.ficha_uc_view?pv_ocorrencia_id=272771)) | Agents and Distributed Artificial Intelligence


**Authors:** David Silva ([daviddias99](https://github.com/daviddias99)), Luís Cunha ([luispcunha](https://github.com/luispcunha)) and Manuel Coutinho ([ManelCoutinho](https://github.com/ManelCoutinho))

---

**Project 1** 

**Description:** For the first project we had the goal of proposing and implementing a multiagent system to simulate any real-life scenario using the JADE framework. We chose to create a 2nd hand marketplace where agents (sellers) would define a price for their products based on the market to try to sell them to other agents (Buyers). The sellers and buyers implemented different negotiation protocols and the sellers also implemented protocols that controlled how they would set their prices based on the market prices for their products.

Additionally, sellers had different probabillities of scamming the buyers, taking their money without giving them the product (which both gave them the chance to sell the product again and made it so that the buyers still needed to buy the product). This effected their reputation. Through this we also saught to measure how this factor would both influence the sellers (honesty vs greed) and how it affected the buyers (naiveness vs cautiousness).

More information on the project can be seen in `docs/OLX (part 1) - presentation.pdf`.


**Technologies:** Java, JADE

**Skills:** Multi-agent systems, agent-based programming, system modelling, negotation protocols, social sciences.

**Grade:** 18/20

---

**Project 2** 

**Description:** For the second project we implemented on top of the first one to use the agent simulation toolkit Repast 3 to conduct large scale experiments on the implemented marketplace scenario. Through it we were able to conduct larger experiments and to study more aspects of the different parametrizations of the agents: price elasticity, scam factor, offer and counter offer strategies, seller picking.

Repast 3 helped us to use different plots and graph based displays to visualize and understand the behaviour of our system.

More information on the project can be seen in `docs/OLX (part 2) - presentation.pdf`.


**Technologies:** Java, JADE, Repast3, Sajas

**Skills:** Multi-agent systems, agent-based programming, system modelling, negotation protocols, social sciences.

**Grade:** 19.6/20

---


## Project proposition (created before we started the projet)

The proposed multi-agent system will represent a **second-hand online marketplace** (similar to [OLX](olx.pt)  with two types of olx.agents: **buyers** and **sellers**, each trying to solve their own minimization problem. The former tries to minimize the amount of money spent, while the latter seeks to minimize the loss from the original item price.

Sellers must set an initial price at which they want to sell an item, knowing that other sellers might be selling the same item at a different price. This price is also affected by the cost of the product in a non-second-hand market, i.e. if a product is being sold at a higher price than its original counterpart, there is no incentive to buy it on the platform.

 In addition, each item has a certain number of buyers which intend to buy it. These two properties create an environment that is guided by the supply and demand simulating a simple economic model of price determination, where there is a constant fluctuation and negotiation of the price of the products.
 
Furthermore, they also have the ability to “scam” their clients by keeping the item and the money. This, although extremely profitable, can lead to losing credibility, resulting in fewer sales in the future. This credibility factor weighs in the buyer’s decision process regarding the choice of a seller, contributing to the dynamism of the environment, where the chosen seller is not exclusively determined by the lowest price.

Logically the platform wants to have as few scams as possible because its own reputation is on the line.

Regarding the communication **protocols**, olx.agents will need to advertise the products which they are selling/seeking, in order to attract potential buyers/sellers. This advertisement can also be used by other sellers in order to define the price at which they will set their products. Also, the different olx.agents will implement a negotiating protocol, which deals with the process of haggling the price of an item.

Both the sellers and buyers can implement different **strategies** for negotiating prices and choosing a retailer, respectively. A buyer can take a naïve approach, choosing the best bargain, disregarding the credibility factor. Agents may instead attribute a different weight to the credibility factor, deciding if they choose (or not) a vendor despite the offered price. A seller’s strategy is composed of two factors, one regarding the decision to scam the buyer which may alter both the advertised price and the negotiating strategy; and one regarding the choice of an advertising price in consequence of the market prices of that item.

| Independent Variables        | Dependent Variables           | 
| ------------- |:-------------:| 
| Influence of the seller’s credibility in the buyer’s decision     | Number of successful scams| 
| Product’s original market price      | Product’s avg. platform price      | 
| Number of buyers of a given product | Money saved (buyers)     |  
| Number of sellers of a given product | Money earned (sellers) |


