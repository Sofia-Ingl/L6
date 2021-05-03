package server.util;

import server.Server;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ExitManager implements Runnable {

    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals("exit") || line.equals("stop")) {
                    Server.logger.info("Пришел сигнал о прекращении работы сервера");
                    System.exit(0);
                }
            }
            if (!scanner.hasNextLine()) {
                throw new IllegalStateException();
            }

        } catch (IllegalStateException | NoSuchElementException e) {

            Server.logger.warn("Поток ввода закрыт, сервер закончит работу в течение 10 секунд");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Server.logger.warn("Ошибка в приостановке потока, сервер завершает работу немедленно");
            }
        }

        System.exit(0);
    }
}
