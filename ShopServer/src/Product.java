import java.io.Serializable;

public class Product implements Serializable {
    String productName;
     String productID;
     String productPrice;

    public Product(String productName, String productID, String productPrice) {
        this.productName = productName;
        this.productID = productID;
        this.productPrice = productPrice;
    }

    public  String getProductName() {
        return productName;
    }
    public  String getProductPrice() {
        return productPrice;
    }
    public  String getProductID() {
        return productID;
    }
}

