import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
       try {
           Socket socket = new Socket("localhost", 2201);
           System.out.println("Connected to " + socket.getRemoteSocketAddress());

           PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
           BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

           String input;

           while(true) {
               input = console.readLine();

               out.println(input);

               if(input.equalsIgnoreCase("/sair")) {
                   System.out.println("Closing connection...");
                   break;
               }

               String response = in.readLine();
               System.out.println("[Server]: " + response);
           }

           socket.close();
           System.out.println("Connection closed");
       } catch (IOException e) {
           System.out.println("Error: " + e);
       }
    }
}
