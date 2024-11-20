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
    private List<Scene> scenes;

    private final ObservableList<ProductRow> productRows = FXCollections.observableArrayList();
    public static List<Product> products = new ArrayList<>();




    @FXML
    private TableView<ProductRow> productsTable;

    @FXML
    private TableColumn<Product, String> Name;

    @FXML
    private TableColumn<Product, String> ID;

    @FXML
    private TableColumn<Product, String> Price;

    @FXML
    private Button LogOutButton;
    @FXML
    private Button BuyButton;
    @FXML
    private Button ReturnButton;
    @FXML
    private Button AddButton;
    @FXML
    private Button Purchases;
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






    MainController(Socket socket, ObjectOutputStream out, ObjectInputStream in, List<Scene> scenes) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.scenes = scenes;
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

        LogOutButton.setOnAction(event -> {
            try {
                LogOut(event);
            } catch (IOException e) {
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
        Purchases.setOnAction(event -> {
            try {
                OwnerPage(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }


    public void LogOut(ActionEvent event) throws IOException {
        Parent root = null;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scenes.add(scene);
        stage.setScene(scene);
        stage.show();
    }

    public void ReturnProduct(ActionEvent event) throws IOException {
        String productName = ProductName.getText();
        String productID = ProductID.getText();
        String productPrice = ProductPrice.getText();
        boolean returnable = false;

        for(Product product : ProductsController.ownedProducts) {
            if(product.getProductID().equals(productID) && product.getProductName().equalsIgnoreCase(productName) && product.getProductPrice().equals(productPrice)) {
                returnable = true;
            }
        }
        if(returnable) {
            //new product
            Product returned = new Product(productName, productID, productPrice);
            //sends product to server
            out.writeObject("RETURN REQUEST");
            out.writeObject(returned);
            //writes it on tableview
            ProductRow productRow = new ProductRow(productName, productID, productPrice);
            productRows.add(productRow);
            ProductsController.ownedProducts.remove(returned);
            Warning.setText("");
            Success.setText("Succesfully returned: " + productName);
        }

    }

    public void AddProduct(ActionEvent event) throws IOException {
        String productName = ProductName.getText();
        String productID = ProductID.getText();
        String productPrice = ProductPrice.getText();
        String message;
        Product offeredProduct = new Product(productName, productID, productPrice);

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
                }
                else if (message.equals("OFFER REJECTED")) {
                    Warning.setText("Product or product ID already exists");

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

    public void BuyProduct(ActionEvent event) throws IOException {
        String message;
        //Requests to buy product
        out.writeObject("PURCHASE REQUEST");
        //Sends the product to server
        String productID = ProductID.getText();
        String productName = ProductName.getText();
        String productPrice = ProductPrice.getText();

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
                Success.setText("You bought " + RowRemove.getName() + " for " + RowRemove.getPrice() + " successfully!");
                Warning.setText("");

                //removes row and product
                productRows.remove(RowRemove);
                products.remove(ProductRemove);
                //adds product to owned product list
                ProductsController.ownedProducts.add(ProductRemove);
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
    public void OwnerPage(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("products.fxml"));
            loader.setController(new ProductsController(socket, out, in,scenes));
            Parent root = loader.load();
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scenes.add(scene);
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }

    }


}
