import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);

        // shows error if number of arguments incorrect
        if (args.length != 2) {
            System.err.println("Usage: java Main --serverPort=<port number1> --sslServerPort=<port number2>");
            System.exit(1);
        }

        // assigns parts1 and part2 to ["--serverPort", "<port number1>"] and ["--sslServerPort", "<port number2>"] accordingly
        String[] parts1 = args[0].split("=");
        String[] parts2 = args[1].split("=");

        // splits string arg
        if (parts1.length == 2 && parts1[0].equals("--serverPort") && parts2.length == 2 && parts2[0].equals("--sslServerPort")) {
            System.out.println("Server port number was " + parts1[1] + " and SSL server port number was " + parts2[1]);
        } else {
            System.out.println("flag wasn't of the form --serverPort=<port number1> --sslServerPort=<port number2>");
            System.exit(1);
        }

        HashMap<String, String> redirects = null;
        try {
            redirects = load_redirect("www/redirect.defs");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // assigns portNum to port
        int serverPortNum = Integer.parseInt(parts1[1]);
        int sslServerPortNum = Integer.parseInt(parts2[1]);

        // Instantiate and start new thread
        new Thread(new WebServer(serverPortNum, redirects)).start();
        new Thread(new SecureWebServer(sslServerPortNum, redirects)).start();
    }

    private static HashMap<String, String> load_redirect(String filename) throws IOException {
        HashMap<String, String> redirects = new HashMap<>();

        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
        	System.out.println("in load_redirects");
            String[] temp = line.split(" ");
            System.out.println(temp[0] + ": " + temp[1]);
            redirects.put(temp[0], temp[1]);
        }
        return redirects;
    }
}
