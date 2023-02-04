package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {

        }
        ConsoleHelper.writeMessage("Server is running");
        Socket clientSocket;
        Handler handler;
        while (true) {
            clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (Exception e) {
                serverSocket.close();
                System.err.println(e.getMessage());
                break;
            }
            if (clientSocket != null) {
                handler = new Handler(clientSocket);
                handler.run();
            }

        }
    }

    private static class Handler extends Thread {
        Socket socket;
        public Handler(Socket socket) {
            this.socket = socket;
        }

    }
}
