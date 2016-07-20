import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Chuanxi on 16/5/22.
 */
public class HTTPResponse {

    public static boolean http_handler(BufferedReader input, DataOutputStream output, HashMap<String, String> redirects) throws IOException {
    	
    	boolean persistent = true;
        //input = the request msg sent from client the server receives
        //check the request,and send a http status code back as a response

        /*** HTTP request msg look like this:
         GET /index.html HTTP/1.1         <== 1st line( method, path, httpversion)
         Host: erbosoft .com
         Connection: keep-alive
         Cache-Control: max-age=0
         Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
         ***/
        
        int method;
        String strMethod = input.readLine();

        if (strMethod == null || strMethod.length() == 0) {
            System.out.println("No input");
            return false;
        }
        if (strMethod.startsWith("User-Agent")) {
        	strMethod = input.readLine();
        }

        // Get Connection request in the message
        String connectionLine = input.readLine();
        while (connectionLine != null && !connectionLine.startsWith("Connection: ")) {
        	connectionLine = input.readLine();
        	//System.out.println("connectionLine" + connectionLine);
        }
       
        String connectionContent = connectionLine.split(" ")[1];
        //System.out.println(connectionContent);
        if (connectionContent.toLowerCase().equals("close")) {
            persistent = false;
        }

        String[] temp = strMethod.split(" "); //request line(= 1st line): GET /somedir/page.html HTTP/1.1
        String two = temp[2];//HTTP/1.1

        output.writeBytes(two);

        //check the method:
        //if the method is get or head
        //else (like, post) return
        //after
        if (strMethod.startsWith("GET ")) {
            method = 1;
        } else if (strMethod.startsWith("HEAD ")) {
            method = 2;
        } else {
            output.writeBytes(construct_http_header(403, 0, ""));
            return false;
        }
        temp = strMethod.split(" "); //request line: GET /somedir/page.html HTTP/1.1

        if (temp[1].equals("/redirect.defs")) {
            output.writeBytes(construct_http_header(404, 0, ""));
            return false;
        }

        //only Get and Head
        //System.out.println("path=" + temp[1] + ";");
        String path = temp[1];
        String instead = redirects.get(path);
        if (instead != null) {
            output.writeBytes(construct_http_header(301, 0, instead));
            return persistent;
        }
        int type_is = 0;
        FileInputStream fr;
        try {
            fr = new FileInputStream("www" + path);
        } catch (IOException e) {
            fr = null;
        }
        if (fr == null) {
            output.writeBytes(construct_http_header(404, 0, ""));
            return false;
        }
        if (path.endsWith(".txt")) {
            type_is = 1;
        }
        else if (path.endsWith(".html")) {
            type_is = 2;
        }
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            type_is = 3;
        }
        else if (path.endsWith(".png")) {
            type_is = 4;
        }
        else if (path.endsWith(".pdf")) {
            type_is = 5;
        }
        else {
            type_is = 1; //same as .txt
        }


        if (method == 1) {
        	
            output.writeBytes(construct_http_header(200, type_is, ""));
            byte[] buffer = new byte[1000];
            int len;
            while ((len = fr.read(buffer)) >= 1){
                output.write(buffer,0,len);
            }
            fr.close();
            output.flush();
        }

        if (method == 2) {
        	
            if (fr != null) fr.close();
            output.writeBytes(construct_http_header(200, type_is, ""));
        }

        return persistent;
    }

    private static String construct_http_header(int return_code, int file_type, String info) throws IOException {

        //construct the header that sends back to the browser
        /***
         HTTP Status(ex 200 OK)
         Content-Type:
         Date
         **/
        //System.out.println("called construct with" + return_code + ", " + file_type + ", " + info);
        String header = " ";
        Date date = new Date();
        switch (return_code) {
            case 200:
                header = header + "200 OK";
                break;
            case 301:
                header = header + "301 Moved Permanently\r\n";
                header +="Location: " + info;
                break;
            case 400:
                header = header + "400 Bad Request";
                break;
            case 403:
                header = header + "403 Forbidden";
                break;
            case 404:
                header = header + "404 Not Found";
                break;
        }

        header = header + "\r\n";
        //header = header + "Connection: close\r\n";
        //header = header + "Server: WebServer\r\n";

        switch (file_type) {
            case 0:
                break;
            case 1:
                header = header + "Content-Type: text/plain\r\n";
            case 2:
                header = header + "Content-Type: text/html\r\n";
                break;
            case 3:
                header = header + "Content-Type: image/jpeg\r\n";
                break;
            case 4:
                header = header + "Content-Type: image/png\r\n";
                break;
            case 5:
                header = header + "Content-Type: application/pdf\r\n";
        }

        header = header + "Date: " + date + "\r\n";
        header = header + "\r\n"; //end of header
        System.out.println("constructed header: \r\n" + header);
        return header;
    }
}
