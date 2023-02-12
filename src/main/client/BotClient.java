package main.client;

import main.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BotClient extends Client {

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
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hi chat. I'm bot. I have some commands: date, day, month, year, time, hour, minutes, seconds");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            if (message != null) {
                ConsoleHelper.writeMessage(message);
                if (message.contains(": ")) {
                    String[] nameData = message.split(": ");
                    if (nameData.length == 2) {
                        String name = nameData[0];
                        String messageData = nameData[1];
                        SimpleDateFormat simpleDateFormat = null;
                        switch (messageData) {
                            case "date":
                                simpleDateFormat = new SimpleDateFormat("d.MM.YYYY", Locale.ENGLISH);
                                break;
                            case "day":
                                simpleDateFormat = new SimpleDateFormat("d", Locale.ENGLISH);
                                break;
                            case "month":
                                simpleDateFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
                                break;
                            case "year":
                                simpleDateFormat = new SimpleDateFormat("YYYY", Locale.ENGLISH);
                                break;
                            case "time":
                                simpleDateFormat = new SimpleDateFormat("H:mm:ss", Locale.ENGLISH);
                                break;
                            case "hour":
                                simpleDateFormat = new SimpleDateFormat("H", Locale.ENGLISH);
                                break;
                            case "minutes":
                                simpleDateFormat = new SimpleDateFormat("m", Locale.ENGLISH);
                                break;
                            case "seconds":
                                simpleDateFormat = new SimpleDateFormat("s", Locale.ENGLISH);
                                break;
                        }
                        if (simpleDateFormat != null) {
                            sendTextMessage("Information for " + name + ": " + simpleDateFormat.format(Calendar.getInstance().getTime()));
                        }
                    }
                }
            }
        }
    }
}
