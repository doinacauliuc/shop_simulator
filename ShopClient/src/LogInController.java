import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class LogInController {
    private final int PORT = 8080;
    private Stage stage;
    private Scene scene;
    public static Parent root;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final List<Scene> scenes = new ArrayList<>();


    @FXML
    private PasswordField Password;
    @FXML
    private TextField Username;
    @FXML
    private Text Warning;

    public void logIn(ActionEvent event) throws IOException {
        String message;
        try{
            socket = new Socket(InetAddress.getLocalHost(), PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch(UnknownHostException e){
            System.out.println("Unknown Host: " + e.getMessage());
        }
        catch(IOException e){
            System.out.println("IO Error: " + e.getMessage());
            Warning.setText("Server is down, please try again later.");

        }
        out.writeObject("LOGIN");
        out.writeObject(Username.getText());
        out.writeObject(Password.getText());

        try{
            message = (String) in.readObject();
            if (message.equals("Wrong password or username")) {
                Warning.setText("Wrong password or username");
                try{
                    out.close();
                    in.close();
                    socket.close();
                }
                catch(IOException e){
                    System.out.println("IO Error: " + e.getMessage());
                }
            }
            else if (message.equals("Login successful")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
                loader.setController(new MainController(socket, out, in));
                Parent root = loader.load();
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                scenes.add(scene);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
            }

        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());

        }

    }
    public void exit(){
        // Close the JavaFX platform
        Platform.exit();

        // Terminate the JVM
        System.exit(0);
    }
}













