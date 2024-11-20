import javafx.beans.property.SimpleStringProperty;



public class ProductRow {
    private SimpleStringProperty name;
    private SimpleStringProperty ID;
    private SimpleStringProperty price;

    ProductRow(final String name, final String ID, final String price) {
        this.name = new SimpleStringProperty(name);
        this.ID = new SimpleStringProperty(ID);
        this.price = new SimpleStringProperty(price);
    }

    public String getName() {
        return name.get();
    }
    public String getID() {
        return ID.get();
    }
    public String getPrice() {
        return price.get();
    }


}