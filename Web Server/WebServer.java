import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class WebServer implements Runnable{
    private int portNumber;
    private static HashMap<String, String> redirects;

    public WebServer(int portNumber, HashMap<String, String> redirects) {
        this.portNumber = portNumber;
        this.redirects = redirects;
    }

    @Override
    public void run() {

        System.out.println("HTTP Server Waiting for clients...");

        // opens sockets & writer/readers
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Connection accepted");
                    
                    boolean alive = true;
                    while (alive) {
                    	alive = HTTPResponse.http_handler(in, out, redirects);//in = the request msg that the server receives
                        clientSocket.setKeepAlive(alive);
                    }	

                    if (!alive) {
                        out.close();
                        in.close();
                        clientSocket.close();
                    }
                
                } catch (IOException e) {}
            }
        } catch (IOException e) {

            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Client disconnected.");
    }
}