import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;

public class FileServer {

    private ServerSocket serverSocket = null;
    private int port;


    public FileServer(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        this.port = port;
    }
    public void handleRequests() throws IOException {
        System.out.println("Listening to port: " + port);

        // creating a thread to handle each of the clients
        while (true) {
            Socket client = serverSocket.accept();
            ConnectionHandler handler = new ConnectionHandler(client);
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
        }
    }
    public static void main(String[] args) {
        System.out.println("Test");
        int port = 1234;
        // port to listen default 8080, or the port from the argument
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        try {
            //Instantiating the HttpServer Class
            FileServer server = new FileServer(port);
            server.handleRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
