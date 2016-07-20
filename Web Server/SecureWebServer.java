import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;

public class SecureWebServer implements Runnable {
    private int portNumber;
    private static HashMap<String, String> redirects;

    public SecureWebServer(int portNumber, HashMap<String, String> redirects) {
        this.portNumber = portNumber;
        this.redirects = redirects;
    }

    @Override
    public void run() {
        System.out.println("HTTPS Server Waiting for clients...");

        // opens sockets & writer/readers
        try {
            char[] password = "project2".toCharArray();
            // Get the JKS contents
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("server.jks"), password);

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, password);

            // Creates a socket factory using JKS contents
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket)sslServerSocketFactory.createServerSocket(portNumber);

            while (true) {
                SSLSocket clientSocket = (SSLSocket)serverSocket.accept();
                try {
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Connection accepted SSL");

                    
                    // return false if "Connection: Close"
                    boolean alive = true;
                    while (alive) {

                    	alive = HTTPResponse.http_handler(in, out, redirects);//in = the request msg that the server receives
                    	clientSocket.setKeepAlive(alive);

                    }	
                    // closes sockets & writer/readers if "Connection: Close"
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
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected.");
    }
}