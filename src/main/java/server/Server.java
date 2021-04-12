package server;

import server.commands.abstracts.InnerServerCommand;
import server.commands.abstracts.UserCommand;
import server.commands.inner.Save;
import server.commands.user.*;
import server.util.CollectionStorage;
import server.util.CommandWrapper;
import shared.util.CommandExecutionCode;
import server.util.RequestProcessor;
import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;
import shared.util.Serialization;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

public class Server implements Runnable {

    private int port = -1;
    private final int timeOut;
    private ServerSocket serverSocket;
    private final RequestProcessor requestProcessor;


    public static void main(String[] args) {

        String path = (args.length == 0) ? "" : args[0];
        CollectionStorage collectionStorage = new CollectionStorage();
        collectionStorage.loadCollection(path);
        InnerServerCommand[] innerServerCommands = {new Save()};
        UserCommand[] userCommands = {new Help(), new History(), new Clear(), new Add(), new Show(), new ExecuteScript(),
                new GoldenPalmsFilter(), new Info(), new AddIfMax(), new PrintAscending(), new RemoveAllByScreenwriter(),
                new RemoveById(), new RemoveGreater(), new Update(), new Exit()};

        Server server = new Server(1666, 10000, new RequestProcessor(new CommandWrapper(collectionStorage, userCommands, innerServerCommands)));
        server.run();
    }


    Server(int port, int timeOut, RequestProcessor requestProcessor) {
        if (port > 1024) {
            this.port = port;
        }
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
        boolean noServerExitCode = true;

        while (noServerExitCode) {

            try (Socket socket = establishClientConnection()) {

                socket.getOutputStream().write(Serialization.serialize(requestProcessor.getCommandWrapper().mapOfCommandsToSend()));

                noServerExitCode = handleRequests(socket);

            } catch (IOException e) {

                // ПРИ ОШИБКЕ ИЛИ ПРЕВЫШЕННОМ ВРЕМЕНИ ОЖИДАНИЯ СЕРВЕР ЗАВЕРШАЕТ РАБОТУ
                System.out.println(e.getMessage());
                requestProcessor.getCommandWrapper().getAllInnerCommands().get("save").execute("", null);
                noServerExitCode = false;
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("Сервер прекращает работу");
            } catch (IOException e) {
                System.out.println("Сервер прекращает работу с ошибкой");
            }
        }


    }

    private void createSocketFactory() {
        if (port == -1) {
            port = 1376;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Фабрика сокетов создана");
            serverSocket.setSoTimeout(timeOut);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private Socket establishClientConnection() throws ConnectException {
        try {
            System.out.println("Прослушивается порт " + port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Соединение установлено");
            return clientSocket;
        } catch (SocketTimeoutException e) {
            throw new ConnectException("Превышено время ожидания клиентского запроса.");
        } catch (IOException | IllegalBlockingModeException | IllegalArgumentException e) {
            //e.printStackTrace();
            throw new ConnectException("Ошибка соединения.");
        }
    }

    private boolean handleRequests(Socket socket) {

        ClientRequest clientRequest;
        ServerResponse serverResponse;

        try {

            do {

                byte[] b = new byte[66666];
                socket.getInputStream().read(b);
                clientRequest = (ClientRequest) Serialization.deserialize(b);
                serverResponse = requestProcessor.processRequest(clientRequest);
                if (clientRequest.getCommand().equals("exit")) {
                    System.out.println(serverResponse.getResponseToPrint());
                    requestProcessor.getCommandWrapper().getAllInnerCommands().get("save").execute("", null);
                } else {
                    socket.getOutputStream().write(Serialization.serialize(serverResponse));
                }


            } while (serverResponse.getCode() != CommandExecutionCode.EXIT);

            return false;

        } catch (IOException | ClassNotFoundException e) {

            System.out.println("Соединение разорвано");
        }

        return true;
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