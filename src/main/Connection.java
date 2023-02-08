package main;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection {
    private Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream outputStream = socket.getOutputStream();
        this.out = new ObjectOutputStream(outputStream);
        InputStream inputStream = socket.getInputStream();
        this.in = new ObjectInputStream(inputStream);
    }

    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message message;
        synchronized (in) {
            message = (Message) in.readObject();
        }
        return message;
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
