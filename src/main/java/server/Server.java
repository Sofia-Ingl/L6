package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.commands.abstracts.InnerServerCommand;
import server.commands.abstracts.UserCommand;
import server.commands.inner.Save;
import server.commands.user.*;
import server.util.CollectionStorage;
import server.util.CommandWrapper;
import shared.serializable.Pair;
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
import java.util.concurrent.TimeoutException;

public class Server implements Runnable {

    public final static Logger logger = LoggerFactory.getLogger(Server.class);

    private int port = -1;
    private final int timeOut;
    private ServerSocket serverSocket;
    private final RequestProcessor requestProcessor;


    public static void main(String[] args) {

        Pair<String, Integer> pathAndPort = getPathAndPort(args);
        CollectionStorage collectionStorage = new CollectionStorage();
        collectionStorage.loadCollection(pathAndPort.getFirst());
        InnerServerCommand[] innerServerCommands = {new Save()};
        UserCommand[] userCommands = {new Help(), new History(), new Clear(), new Add(), new Show(), new ExecuteScript(),
                new GoldenPalmsFilter(), new Info(), new AddIfMax(), new PrintAscending(), new RemoveAllByScreenwriter(),
                new RemoveById(), new RemoveGreater(), new Update(), new Exit()};

        Server server = new Server(pathAndPort.getSecond(), 10000, new RequestProcessor(new CommandWrapper(collectionStorage, userCommands, innerServerCommands)));
        server.run();
    }


    Server(int port, int timeOut, RequestProcessor requestProcessor) {
        if (port > 1024) {
            this.port = port;
        }
        this.timeOut = timeOut;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void run() {

        logger.info("Сервер запускается");
        createSocketFactory();
        boolean noServerExitCode = true;

        while (noServerExitCode) {

            try (Socket socket = establishClientConnection()) {

                socket.getOutputStream().write(Serialization.serialize(requestProcessor.getCommandWrapper().mapOfCommandsToSend()));

                noServerExitCode = handleRequests(socket);

            }
            catch (ConnectException e) {
                logger.info(e.getMessage());
                requestProcessor.getCommandWrapper().getAllInnerCommands().get("save").execute("", null);
                noServerExitCode = false;
            } catch (IOException e) {
                logger.info("Непредвиденная ошибка соединения (клиент, бывший в очереди, отключился)");
            }
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
                logger.info("Сервер прекращает работу");
            } catch (IOException e) {
                logger.warn("Сервер прекращает работу с ошибкой");
            }
        }


    }

    private void createSocketFactory() {
        if (port == -1) {
            port = 1376;
        }
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Фабрика сокетов создана");
            serverSocket.setSoTimeout(timeOut);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private Socket establishClientConnection() throws ConnectException {
        try {
            logger.info("Прослушивается порт {}", port);
            Socket clientSocket = serverSocket.accept();
            logger.info("Соединение с клиентом, находящимся по адресу {}, установлено", clientSocket.getRemoteSocketAddress().toString());
            return clientSocket;
        } catch (SocketTimeoutException e) {
            throw new ConnectException("Превышено время ожидания клиентского запроса.");
        } catch (IOException | IllegalBlockingModeException | IllegalArgumentException e) {
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
                    logger.info(serverResponse.getResponseToPrint());
                    // А надо ли сохранять, когда клиент отключается?
                    requestProcessor.getCommandWrapper().getAllInnerCommands().get("save").execute("", null);
                } else {
                    socket.getOutputStream().write(Serialization.serialize(serverResponse));
                }


            } while (serverResponse.getCode() != CommandExecutionCode.EXIT);

            return false;

        } catch (IOException | ClassNotFoundException e) {

            logger.warn("Соединение разорвано");
        }

        return true;
    }


    private static Pair<String, Integer> getPathAndPort(String[] args) {
        try {
            if (args.length > 1) {
                String path = args[0];
                int port = Integer.parseInt(args[1]);
                return new Pair<>(path, port);

            } else {
                throw new IllegalArgumentException("Не указан путь к коллекции и/или порт, который будет прослушиваться сервером");
            }
        } catch (NumberFormatException e) {
            logger.error("Порт должен быть целым числом");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }


}