package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkManager {

    private final PrintWriter out;
    private final BufferedReader in;

    public NetworkManager(Socket socket) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        out.println(message);
        logMessage(message);
    }

    public BufferedReader getInputReader() {
        return in;
    }

    private void logMessage(String message) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println(LocalDateTime.now().format(timeFormatter) + " [Client] " + message);
    }
}
