package client.util;

import java.io.*;
import java.util.Scanner;

public abstract class InteractiveConsoleUtils {
    private InputStream In = null;
    private OutputStream Out = null;
    private Scanner scanner = null;
    private boolean suppressMessages = false;

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setIn(InputStream in) {
        In = in;
    }

    public void setSuppressMessages(boolean suppressMessages) {
        this.suppressMessages = suppressMessages;
    }

    public InputStream getIn() {
        return In;
    }

    public OutputStream getOut() {
        return Out;
    }

    public void setOut(OutputStream out) {
        Out = out;
    }

    public void printlnMessage(String message) {
        printMessage(message + "\n");
    }

    public void printMessage(String message) {
        try {
            if (Out!=null && !suppressMessages) {
                Out.write(message.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода!");
        }
    }


}
