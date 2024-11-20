import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShopServer{
    private final static int default_port = 8080;
    private static boolean running;
    public static Map <String, String > users = new HashMap<>();
    public static List<Product> productList = new ArrayList<>();



    public static void fillMap(String filePath, Map <String, String > info){

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String first = parts[0].trim();
                    String second = parts[1].trim();
                    info.put(first, second);
                } else {
                    System.out.println("Wrong format: " + line);
                }
            }
        } catch (IOException e ) {
            throw new RuntimeException(e);
        }


    }

    public static void getProducts(String filePath){
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String productName = parts[0].trim();
                    String productID = parts[1].trim();
                    String productPrice = parts[2].trim();
                    Product product = new Product(productName, productID, productPrice);
                    productList.add(product);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static class Server{
        private int port;
        private static ServerSocket serverSocket;
        public Server() {
            this.port = default_port;
        }

        public void StartServer(){
            try{
                serverSocket = new ServerSocket(port);
                System.out.println("Waiting for connection...");
                running = true;

                while(running){
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static void main(String[] args){
        fillMap("users.txt", users);
        getProducts("products.txt");
        Server server = new Server();
        server.StartServer();

    }
}