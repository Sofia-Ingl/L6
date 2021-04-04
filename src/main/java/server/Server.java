package server;

import client.Client;
import server.util.CommandExecutionCode;
import server.util.Pair;
import server.util.RequestProcessor;
import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;

public class Server implements Runnable {

    private final int DEFAULT_PORT = 1376;
    private int port = -1;
    private final int timeOut;
    private ServerSocket serverSocket;
    private RequestProcessor requestProcessor;


    public static void main(String[] args) {

    }


    Server(int port, int timeOut, RequestProcessor requestProcessor) {
        this.port = port;
        this.timeOut = timeOut;
        this.requestProcessor = requestProcessor;
    }

    Server(int timeOut, RequestProcessor requestProcessor) {
        this.timeOut = timeOut;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void run() {
        createSocketFactory();
        boolean noExitCode = true;
        while (noExitCode) {
            /*
            True пока нет EXIT CODE в присланном запросе
             */
        }
    }

    private void createSocketFactory() {
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Фабрика сокетов создана");
            serverSocket.setSoTimeout(timeOut);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private Socket establishClientConnection() {
        try {
            System.out.println("Прослушивается порт " + port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Соединение установлено");
            return clientSocket;
        } catch (IOException | IllegalBlockingModeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean handleRequests(Socket socket) {
        ClientRequest clientRequest;
        Pair<CommandExecutionCode, ServerResponse> responseWithStatusCode;
        ServerResponse serverResponse;

        try (ObjectInputStream requestReader = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream responseWriter = new ObjectOutputStream(socket.getOutputStream())) {

            clientRequest = (ClientRequest) requestReader.readObject();
            responseWithStatusCode = requestProcessor.processRequest(clientRequest);
            serverResponse = responseWithStatusCode.getSecond();


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }



}

/*
Обязанности серверного приложения:

Работа с файлом, хранящим коллекцию.
Управление коллекцией объектов.
Назначение автоматически генерируемых полей объектов в коллекции.
Ожидание подключений и запросов от клиента.
Обработка полученных запросов (команд).
Сохранение коллекции в файл при завершении работы приложения.
Сохранение коллекции в файл при исполнении специальной команды, доступной только серверу (клиент такую команду отправить не может).
Серверное приложение должно состоять из следующих модулей (реализованных в виде одного или нескольких классов):
Модуль приёма подключений.
Модуль чтения запроса.
Модуль обработки полученных команд.
Модуль отправки ответов клиенту.
Сервер должен работать в однопоточном режиме.
 */