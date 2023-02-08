package main;

import javax.net.SocketFactory;
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

        try {InetAddress ia;
            ia = InetAddress.getByName("localhost");
            ServerSocket serverSocket= new ServerSocket(port,0,ia);
            ConsoleHelper.writeMessage("Server is running");
             while (true) {
                 System.out.println("ddd");
                 new Handler(serverSocket.accept()).start();
             }
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while(true){
                connection.send(new Message(MessageType.NAME_REQUEST,"Type your name"));
                Message reply = connection.receive();
                if (reply.getType().equals(MessageType.USER_NAME)) {
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

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message newMessage = new Message(MessageType.TEXT,userName + ": " + message.getData());
                    sendBroadcastMessage(newMessage);
                } else {
                    ConsoleHelper.writeMessage("Wrong type message!");
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Established new connection with remote address " + socket.getRemoteSocketAddress());
            String clientName = null;
            try {Connection connection = new Connection(socket);
                System.out.println("im here");
                clientName = serverHandshake(connection);
                sendListOfUsers(connection, clientName);
                for (String names: connectionMap.keySet()) {
                    if (!names.equals(clientName)){
                        Message msg = new Message(MessageType.TEXT, names);
                        connection.send(msg);
                    }
                }
                serverMainLoop(connection, clientName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Exception in exchanging data with remote address ");
/*                try {
                    if (connection != null){
                        connection.close();
                    }
                } catch (IOException ex) {}*/
                if (clientName != null && connectionMap.containsKey(clientName)) {
                    Message removeUserMessage = new Message(MessageType.USER_REMOVED, clientName);
                    sendBroadcastMessage(removeUserMessage);
                    connectionMap.remove(clientName);
                }
            }
            ConsoleHelper.writeMessage("Connection was closed");
        }
    }
}
