package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>(); // key - client name, value - connection

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
                handler.start();
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {

        for (Map.Entry<String, Connection> connect : connectionMap.entrySet()) {
            try {
                connect.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Can't send message to " + connect.getKey());
            }
        }
    }

    private static class Handler extends Thread {
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while(true){
                connection.send(new Message(MessageType.NAME_REQUEST, "Please, write your name"));
                Message reply = connection.receive();
                if (reply.getType() == MessageType.USER_NAME) {
                    String name = reply.getData();
                    if (name.isEmpty() && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED, "Your name is accepted"));
                        return name;
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> connect: connectionMap.entrySet()) {
                String name = connect.getKey();
                if (!userName.equals(name)) {
                    Message message = new Message(MessageType.USER_ADDED, name);
                    connection.send(message);
                }
            }
        }
    }
}
