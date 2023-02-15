package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>(); // key - client name, value - connection

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Input server port: ");
        int port = ConsoleHelper.readInt();

        try {
            InetAddress ia;
            ia = InetAddress.getByName("localhost");
            ServerSocket serverSocket = new ServerSocket(port, 0, ia);
            ConsoleHelper.writeMessage("Server is running");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message) {

        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Can't send message");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message reply = connection.receive();
                if (reply.getType() == MessageType.USER_NAME) {
                    String name = reply.getData();
                    if (!name.isEmpty() && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return name;
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> connect : connectionMap.entrySet()) {
                String name = connect.getKey();
                if (!userName.equals(name)) {
                    Message message = new Message(MessageType.USER_ADDED, name);
                    connection.send(message);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Wrong type message!");
                }
            }
        }

        @Override
        public void run() {
            if (socket != null && socket.getRemoteSocketAddress() != null) {
                ConsoleHelper.writeMessage("Established new connection with remote address " + socket.getRemoteSocketAddress());
            }
            String clientName = null;
            try (Connection connection = new Connection(socket)) {
                clientName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));
                sendListOfUsers(connection, clientName);
                serverMainLoop(connection, clientName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Exception in exchanging data with remote address ");
            } finally {
                if (clientName != null) {
                    connectionMap.remove(clientName);
                    Message removeUserMessage = new Message(MessageType.USER_REMOVED, clientName);
                    sendBroadcastMessage(removeUserMessage);

                }
            }
            ConsoleHelper.writeMessage("Connection was closed");
        }
    }
}
