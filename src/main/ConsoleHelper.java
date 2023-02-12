package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage (String message) {
        System.out.println(message);
    }

    public static String readString() {
        while (true) {
            try {
                return bufferedReader.readLine();
            } catch (Exception e) {
                writeMessage("Some error occurred when trying to enter a text. Please try again.");
            }
        }
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                writeMessage("Some error occurred when trying to enter a number. Please try again.");
            }
        }
    }

}
