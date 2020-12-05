package olx.utils;

import olx.agents.Buyer;
import olx.agents.Seller;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import olx.models.Product;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Config {
    private final Buyer[] buyers;
    private final Product[] products;
    private final Seller[] sellers;

    Config(@JsonProperty("products") Product[] products,
           @JsonProperty("buyers") Buyer[] buyers,
           @JsonProperty("sellers") Seller[] sellers) {
        this.products = products;
        this.buyers = buyers;
        this.sellers = sellers;
    }

    public static Config read(String path) throws IOException {
        File file = new File(path);

        ObjectMapper objectMapper;
        if (path.contains("json")) {
            objectMapper = new ObjectMapper();
        } else {
            objectMapper = new ObjectMapper(new YAMLFactory());
        }

        return objectMapper.readValue(file, Config.class);
    }

    public Product[] getProducts() {
        return products;
    }

    public Buyer[] getBuyers() {
        return buyers;
    }

    public Seller[] getSellers() {
        return sellers;
    }

    @Override
    public String toString() {
        return "Config{" +
                "\n  buyers=" + Arrays.toString(buyers) +
                ",\n  products=" + Arrays.toString(products) +
                ",\n  sellers=" + Arrays.toString(sellers) +
                "\n}";
    }
}
