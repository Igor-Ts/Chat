package main.client;

import main.Connection;
import main.ConsoleHelper;
import main.Message;
import main.MessageType;

import java.io.IOException;
import java.net.Socket;


public class Client extends Thread{
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                wait();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Something is wrong!!!!");
            Thread.currentThread().interrupt();
            socketThread.interrupt();
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Connection is done. If u want to quit write 'exit'");
            while (true) {
                String text = ConsoleHelper.readString();
                if (text.equalsIgnoreCase("exit")){
                    break;
                } else if(shouldSendTextFromConsole()) {
                    sendTextMessage(text);
                }
            }
        } else {
            ConsoleHelper.writeMessage("An error occurred when the client was running");
        }
    }
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Please, write server address (localhost, ip)");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Please, write server port");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Please, write username");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() { // need to override if u want to close this opportunity
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Connection lost");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has enter the chat");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has left from the chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message msg;
                while (true) {
                    try {
                        msg = connection.receive();
                    } catch (ClassNotFoundException e) {
                        throw new IOException("Unexpected MessageType");
                    }

                    if (msg.getType() == MessageType.NAME_REQUEST) {
                        String name = getUserName();
                        Message nameMessage = new Message(MessageType.USER_NAME, name);
                        connection.send(nameMessage);
                    } else if (msg.getType() == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                        break;
                    } else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message msg;
            while (true) {
                try {
                    msg = connection.receive();
                } catch (ClassNotFoundException e) {
                    throw new IOException("Unexpected MessageType");
                }
                switch (msg.getType()){
                    case TEXT: {
                        processIncomingMessage(msg.getData());
                        break;
                    }
                    case USER_ADDED: {
                        informAboutAddingNewUser(msg.getData());
                        break;
                    }
                    case USER_REMOVED: {
                        informAboutDeletingNewUser(msg.getData());
                        break;
                    }
                    default:
                        throw new IOException("Unexpected message");
                }
            }
        }

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address,port);
                 connection = new Connection(socket);
                 clientHandshake();
                 clientMainLoop();
            } catch (IOException | ClassNotFoundException e ) {
                notifyConnectionStatusChanged(false);
            }



        }
    }
}
