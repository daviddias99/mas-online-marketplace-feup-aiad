package olx.models;

public class Stock {
    private Float price;
    private int quantity;

    public Stock(Float price, int quantity) {
        this.setPrice(price);
        this.setQuantity(quantity);
    }

    public void decreaseQuantity(){
        this.quantity--;
    }

    public boolean empty(){
        return this.quantity == 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price){
        this.price = price;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    @Override
    public String toString(){
        return String.format("[market=%.2f; quantity=%d]",this.getPrice(), this.getQuantity());
    }
}
