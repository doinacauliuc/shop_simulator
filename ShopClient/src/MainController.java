import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {
    private Stage stage;
    private Scene scene;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final ObservableList<ProductRow> productRows = FXCollections.observableArrayList();
    public static List<Product> products = new ArrayList<>();
    public static ObservableList<ProductRow> OwnedRows = FXCollections.observableArrayList();
    public static List<Product> ownedProducts = new ArrayList<>();



    //Shop products table
    @FXML
    private TableView<ProductRow> productsTable;

    @FXML
    private TableColumn<Product, String> Name;

    @FXML
    private TableColumn<Product, String> ID;

    @FXML
    private TableColumn<Product, String> Price;

    //Owned products table
    @FXML
    private TableView<ProductRow> OwnedTable;

    @FXML
    private TableColumn<Product, String> OwnedName;

    @FXML
    private TableColumn<Product, String> OwnedID;

    @FXML
    private TableColumn<Product, String> OwnedPrice;

    @FXML
    private Button LogOutButton;
    @FXML
    private Button BuyButton;
    @FXML
    private Button ReturnButton;
    @FXML
    private Button AddButton;
    @FXML
    private TextField ProductName;
    @FXML
    private TextField ProductID;
    @FXML
    private TextField ProductPrice;
    @FXML
    private Text Warning;
    @FXML
    private Text Success;






    MainController(Socket socket, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    @FXML
    public void initialize() {

        // Set up the columns to use the appropriate fields from the Product class
        Name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        ID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        Price.setCellValueFactory(new PropertyValueFactory<>("Price"));

        try {
            products = (List<Product>) in.readObject();
            for (Product Product : products) {
                ProductRow productRow = new ProductRow(Product.getProductName(), Product.getProductID(), Product.getProductPrice());
                productRows.add(productRow);
            }

            productsTable.setItems(productRows);

        } catch (IOException e) {
            System.out.println("Error reading OwnedRows from socket");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }

        //set up Owned products table
        OwnedName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        OwnedID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        OwnedPrice.setCellValueFactory(new PropertyValueFactory<>("Price"));
        OwnedTable.setItems(OwnedRows);


        LogOutButton.setOnAction(event -> {
            try {
                LogOut(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        BuyButton.setOnAction(event -> {
            try {
                BuyProduct(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ReturnButton.setOnAction(event -> {
            try {
                ReturnProduct(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        AddButton.setOnAction(event -> {
            try {
                AddProduct(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void LogOut(ActionEvent event) throws IOException, ClassNotFoundException {
        try {
            // Close resources
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }

            // Load the login screen
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Login.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();

            System.out.println("Logged out and returned to login screen.");


        } catch (IOException e) {
            System.out.println("Error while logging out: " + e.getMessage());
        }

    }

    public void ReturnProduct(ActionEvent event) throws IOException {
        String productName = ProductName.getText();
        String productID = ProductID.getText();
        String productPrice = ProductPrice.getText();
        boolean returnable = false;
        Product returned = null;
        ProductRow returnedRow = null;

        for(Product product : ownedProducts) {
            if(product.getProductID().equals(productID) && product.getProductName().equalsIgnoreCase(productName) && product.getProductPrice().equals(productPrice)) {
                returnable = true;
                returned = product;
            }
        }
        if(returnable) {
            //finds which row to move
            for(ProductRow row : OwnedRows) {
                if(row.getName().equalsIgnoreCase(productName) && row.getID().equals(productID) && row.getPrice().equals(productPrice)) {
                    returnedRow = row;
                }
            }
            //sends product to server
            out.writeObject("RETURN REQUEST");
            out.writeObject(returned);
            //writes it on tableview and adds to product list
            productRows.add(returnedRow);
            products.add(returned);
            //removes from owned products table and list
            ownedProducts.remove(returned);
            OwnedRows.remove(returnedRow);

            //UI messages
            Warning.setText("");
            Success.setText("Succesfully returned: " + productName);
        }

    }

    public void AddProduct(ActionEvent event) throws IOException {
        String productName = ProductName.getText();
        String productID = ProductID.getText();
        String productPrice = ProductPrice.getText();
        String message;
        Boolean addable = true;
        Product offeredProduct = new Product(productName, productID, productPrice);

        //if any other product has the same name or ID, new product cannot be added
        for(Product product : ownedProducts) {
            if(product.getProductName().equalsIgnoreCase(productName) || product.getProductID().equals(productID)) {
                addable = false;
            }
        }

        if(addable) {
            try{
                float numericPrice = Float.parseFloat(productPrice);


                //sends offer message to server
                out.writeObject("OFFER");
                //sends new product to server
                out.writeObject(offeredProduct);

                //checks server response
                try {
                    message = (String) in.readObject();
                    if (message.equals("OFFER ACCEPTED")) {
                        //adds product to product list and to table view
                        products.add(offeredProduct);
                        ProductRow newRow = new ProductRow(productName, productID, productPrice);
                        productRows.add(newRow);
                        Warning.setText("s");
                        Success.setText("Successfully added: " + productName + " to list");

                    }
                    else if (message.equals("OFFER REJECTED")) {
                        Warning.setText("Product or product ID already exists");
                        Success.setText("");


                    }
                }
                catch (ClassNotFoundException e) {
                    System.out.println("Class not found: " + e.getMessage());
                }
            }
            catch (NumberFormatException e){
                Warning.setText("Invalid product price: must be a float (00.00)");
            }


        }
        else{
            Warning.setText("Product or product ID already exists");
            Success.setText("");
        }

    }

    public void BuyProduct(ActionEvent event) throws IOException {
        String message;
        //Requests to buy product
        out.writeObject("PURCHASE REQUEST");
        //Sends the product to server
        String productID = ProductID.getText();
        String productName = ProductName.getText();
        String productPrice = ProductPrice.getText();
        System.out.println(productName);

        out.writeObject(productID);
        out.writeObject(productName);
        out.writeObject(productPrice);

        //Reads server response
        try{
            message = (String) in.readObject();
            //Buy operations
            if(message.equals("PURCHASE ACCEPTED")){
                System.out.println(message);
                //product and row to be removed
                Product ProductRemove = null;
                ProductRow RowRemove = null;

                //finds row to remove
                for(ProductRow Row : productRows) {
                    if(Row.getID().equals(productID)) {
                       RowRemove = Row;
                    }
                }

                //finds product to remove
                for(Product product : products) {
                    if(product.getProductID().equals(productID)) {
                        ProductRemove = product;
                    }
                }

                //success message
                if(ProductRemove != null) {
                    Success.setText("You bought " + RowRemove.getName() + " for " + RowRemove.getPrice() + " successfully!");
                    Warning.setText("");

                }
                //removes row and product
                productRows.remove(RowRemove);
                products.remove(ProductRemove);
                //adds product to owned product list
                ownedProducts.add(ProductRemove);
                OwnedRows.add(RowRemove);
            }
            else if(message.equals("PURCHASE REJECTED")){
                //warning message
                Warning.setText("Purchase request rejected: non existent product");
                Success.setText("");
            }
        }
        catch (ClassNotFoundException e){
            System.out.println("Class not found: " + e.getMessage());
        }

    }
}
