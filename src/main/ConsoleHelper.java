package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage (String message) {
        System.out.println(message);
    }

    public static String readString() {
        String line = "";
        try {
             line = bufferedReader.readLine();
             bufferedReader.close();
        } catch (Exception e) {
            writeMessage("��������� ������ ��� ������� ����� ������. ���������� ��� ���.");
            readString();
        }
        return line;
    }

    public static int readInt() {
        int line = 0;
        try {
            line = Integer.parseInt(readString());
        } catch (NumberFormatException e) {
            writeMessage("��������� ������ ��� ������� ����� �����. ���������� ��� ���.");
            readInt();
        }
        return line;
    }
}
