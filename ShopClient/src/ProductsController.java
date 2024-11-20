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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ProductsController {
    private Stage stage;
    private Scene scene;
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private List<Scene> scenes;
    public static List<Product> ownedProducts = new ArrayList<>();
    public static ObservableList<ProductRow> OwnedRows = FXCollections.observableArrayList();
    @FXML
    private TableView<ProductRow> productsTable;

    @FXML
    private TableColumn<Product, String> Name;

    @FXML
    private TableColumn<Product, String> ID;

    @FXML
    private TableColumn<Product, String> Price;
    @FXML
    private Button Main;

    ProductsController(Socket socket, ObjectOutputStream out, ObjectInputStream in, List<Scene> scenes) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.scenes = scenes;
    }


    @FXML
    public void initialize() {
        Name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        ID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        Price.setCellValueFactory(new PropertyValueFactory<>("Price"));


        for (Product Product : ownedProducts) {
            ProductRow productRow = new ProductRow(Product.getProductName(), Product.getProductID(), Product.getProductPrice());
            OwnedRows.add(productRow);
        }

        productsTable.setItems(OwnedRows);

        Main.setOnAction(event -> {
            try {
                switchToMain(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void switchToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        scenes.add(scene);
        stage.show();
    }
}
