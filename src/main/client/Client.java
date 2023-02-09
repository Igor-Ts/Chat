package main.client;

import main.Connection;
import main.ConsoleHelper;
import main.Message;
import main.MessageType;

import java.io.IOException;


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
                Thread.currentThread().wait();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Something is wrong!!!!");
            Thread.currentThread().interrupt();
            socketThread.interrupt();
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Connection is done. If u want to quit write 'exit'");
        }else {
            ConsoleHelper.writeMessage("An error occurred when the client was running");
        }
        while (true){
            String text = ConsoleHelper.readString();
            if (text.equals("exit")){
                break;
            } else if(shouldSendTextFromConsole()) {
                sendTextMessage(text);
            }
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
            connection.send(new Message(MessageType.TEXT,text));
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
    }
}
