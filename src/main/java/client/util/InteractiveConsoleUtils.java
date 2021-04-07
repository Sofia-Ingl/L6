package client.util;

import java.io.*;
import java.util.Scanner;

public abstract class InteractiveConsoleUtils {
    private InputStream In = null;
    private OutputStream Out = null;
    private Scanner scanner = null;
    private boolean isScriptReader = false;

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setIn(InputStream in) {
        In = in;
    }

    public boolean isScriptReader() {
        return isScriptReader;
    }

    public void setScriptReader(boolean scriptReader) {
        isScriptReader = scriptReader;
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

    public void writeMessage(String message) {
        try {
            if (Out!=null && !isScriptReader) {
                Out.write(message.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода!");
        }
    }

}
