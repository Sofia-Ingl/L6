package server;

import server.commands.Add;
import server.commands.Command;
import server.commands.Help;
import server.util.CollectionStorage;
import server.util.CommandWrapper;
import shared.util.CommandExecutionCode;
import shared.serializable.Pair;
import server.util.RequestProcessor;
import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;
import shared.util.Serialization;

import java.io.IOException;
import java.net.ConnectException;
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

        String path = (args.length == 0) ? "" : args[0];
        CollectionStorage collectionStorage = new CollectionStorage();
        collectionStorage.loadCollection(path);
        Command[] commands = {new Help(), new Add()};

        Server server = new Server(666, 20000, new RequestProcessor(new CommandWrapper(collectionStorage, commands)));
        server.run();
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
        boolean noServerExitCode = true;
        /*
        True пока нет EXIT (SERVER) в присланном запросе
         */
        while (noServerExitCode) {

            try (Socket socket = establishClientConnection()) {

                //
                socket.getOutputStream().write(Serialization.serialize(requestProcessor.getCommandWrapper().mapOfCommandsToSend()));
                //

                noServerExitCode = handleRequests(socket);

            } catch (IOException e) {

                // ПРИ ОШИБКЕ ИЛИ ПРЕВЫШЕННОМ ВРЕМЕНИ ОЖИДАНИЯ СЕРВЕР ЗАВЕРШАЕТ РАБОТУ
                e.printStackTrace();
                System.out.println(e.getMessage());
                noServerExitCode = false;
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("Сервер прекращает работу");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Сервер прекращает работу с ошибкой");
            }
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

    private Socket establishClientConnection() throws ConnectException {
        try {
            System.out.println("Прослушивается порт " + port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Соединение установлено");
            return clientSocket;
        } catch (IOException | IllegalBlockingModeException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new ConnectException("Ошибка соединения.");
        }
    }

    private boolean handleRequests(Socket socket) {

        ClientRequest clientRequest;
        Pair<CommandExecutionCode, ServerResponse> responseWithStatusCode;
        ServerResponse serverResponse;

        try {

            do {

                byte[] b = new byte[66666];
                socket.getInputStream().read(b);
                clientRequest = (ClientRequest) Serialization.deserialize(b);
                //System.out.println(clientRequest);
                serverResponse = requestProcessor.processRequest(clientRequest);
                //System.out.println(serverResponse);

                socket.getOutputStream().write(Serialization.serialize(serverResponse));


            } while (serverResponse.getCode() != CommandExecutionCode.EXIT);

            return false;

        } catch (IOException | ClassNotFoundException e) {

            System.out.println("fuck");
            e.printStackTrace();
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