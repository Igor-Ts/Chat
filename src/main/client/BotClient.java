package main.client;

public class BotClient  extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.start();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + ((int) (Math.random() * 99));
    }

    public class BotSocketThread extends SocketThread {

    }
}
