import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket client;
    ObjectInputStream in;
    ObjectOutputStream out;
    Boolean running = true;



    public ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
           out = new ObjectOutputStream(client.getOutputStream());
           out.flush();
           in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String username;
        String password;
        while(running) {
            try {
                String message = (String) in.readObject();

                //LOGIN MANAGEMENT
                if(message.equals("LOGIN")){
                    username= (String) in.readObject();
                    password = (String) in.readObject();
                    if(password.equals(ShopServer.users.get(username))) {
                        out.writeObject("Login successful");
                        out.flush();
                        out.writeObject(ShopServer.productList);

                    }
                    else{
                        out.writeObject("Wrong password or username");
                    }
                }
                else if(message.equals("PURCHASE REQUEST")){
                    //reads the id of the product wanted by the client
                    String productID = (String) in.readObject();
                    String productName = (String) in.readObject();
                    String productPrice = (String) in.readObject();
                    System.out.println(productID);
                    //if
                    boolean contained = false;

                    for(Product item : ShopServer.productList){
                        if (productID.equals(item.getProductID()) && productName.equalsIgnoreCase(item.getProductName()) && productPrice.equals(item.getProductPrice())) {
                            contained = true;
                            out.writeObject("PURCHASE ACCEPTED");
                            System.out.println("PURCHASE ACCEPTED");
                        }
                    }
                    ShopServer.productList.removeIf(item -> item.getProductID().equals(productID));
                    for(Product item : ShopServer.productList){
                        System.out.println(item.getProductID());
                    }

                    if(!contained){
                        out.writeObject("PURCHASE REJECTED");
                    }
                }
                else if(message.equals("OFFER")){
                    Product offeredProduct = (Product) in.readObject();
                    String productID = offeredProduct.getProductID();
                    String productName = offeredProduct.getProductName();
                    String productPrice = offeredProduct.getProductPrice();
                    boolean invalidValue = false;

                    System.out.println("Recieved new product offer: " + offeredProduct.getProductID());
                    //checks if product already is contained
                    for(Product item : ShopServer.productList){
                        if(productID.equals(item.getProductID()) || productName.equalsIgnoreCase(item.getProductName()) || productPrice.equals(item.getProductPrice())){
                            out.writeObject("OFFER REJECTED");
                            invalidValue = true;
                        }
                    }

                    //if the shop doesn't have the offered product, accepts it and adds it to product list
                    if(!invalidValue){
                        out.writeObject("OFFER ACCEPTED");
                        ShopServer.productList.add(offeredProduct);
                    }

                }
                else if(message.equals("RETURN REQUEST")){
                    //
                    Product returnedProduct = (Product) in.readObject();
                    ShopServer.productList.add(returnedProduct);
                }
            } catch (IOException e) {
                System.out.println("Access denied");
                try {
                    client.close();
                    running = false;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
