package client;

import client.util.Interaction;
import client.util.UserElementGetter;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;
import shared.util.Serialization;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client implements Runnable {

    private static HashMap<String, Integer> allowedHosts;

    private final String host;
    private final int port;
    private SocketChannel socketChannel;
    private Selector selector;
    private final Interaction interaction;

    public static void main(String[] args) {

        try {
            Signal s = new Signal("TSTP");
            Signal.handle(s, SignalHandler.SIG_IGN);
        } catch (IllegalArgumentException ignored) {}

        loadConfigs();
        Pair<String, Integer> hostAndPort = getHostAndPort(args);

        Interaction interaction = new Interaction(System.in, System.out, new UserElementGetter());
        boolean reconnect;
        Client client = new Client(hostAndPort.getFirst(), hostAndPort.getSecond(), interaction);
        do {
            client.run();
            interaction.printlnMessage("Хотите переподключиться? (да|yes|y)");
            interaction.printMessage(">");
            String answer = interaction.readLine().trim().toLowerCase();
            reconnect = answer.equals("да") || answer.equals("yes") || answer.equals("y");
        } while (reconnect);

    }


    public Client(String host, int port, Interaction interaction) {
        this.host = host;
        this.port = port;
        this.interaction = interaction;
    }

    @Override
    public void run() {

        byte[] b;
        CommandExecutionCode code = CommandExecutionCode.SUCCESS;
        ClientRequest request;
        ServerResponse response;
        SelectionKey selectionKey;
        boolean clientExitCode = false;


        try {

            setConnectionWithServer();
            setSelector();

            try {

                socketChannel.register(selector, SelectionKey.OP_READ);

                selector.select(5000);
                boolean reconnect;
                int reconnects = 0;
                while (selector.selectedKeys().isEmpty()) {
                    if (reconnects == 2) {
                        throw new ConnectException("Лимит ожидания превышен => соединение будет разорвано");
                    }
                    interaction.printlnMessage("Сервер пока ничего не прислал. Хотите продолжить ожидание? (yes)");
                    interaction.printMessage(">");
                    reconnect = interaction.readLine().trim().toLowerCase().equals("yes");
                    if (!reconnect) {
                        System.exit(0);
                    }
                    reconnects++;
                    selector.select(5000);
                }

                selector.selectedKeys().clear();
                setCommandsAvailable();

                interaction.printlnMessage(interaction.showCommandsAvailable());

                socketChannel.register(selector, SelectionKey.OP_WRITE);
                while (!clientExitCode) {
                    int count = selector.select();
                    if (count == 0) {
                        break;
                    }

                    Set keys = selector.selectedKeys();
                    Iterator iterator = keys.iterator();

                    while (iterator.hasNext()) {
                        selectionKey = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (selectionKey.isReadable()) {
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                            b = getResponse();
                            response = (ServerResponse) Serialization.deserialize(b);
                            code = response.getCode();
                            interaction.printlnMessage(response.getResponseToPrint());
                        }

                        if (selectionKey.isWritable()) {
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            request = null;
                            while (request == null) {
                                request = interaction.formRequest(code);
                            }
                            if (socketChannel.isConnected()) {
                                sendClientRequest(request);
                            } else {
                                throw new IOException();
                            }

                            if (request.getCommand().equals("exit")) {
                                clientExitCode = true;
                            }

                        }
                    }
                }

                interaction.printlnMessage("Клиент завершил работу приложения.");
                if (socketChannel != null) socketChannel.close();
                System.exit(0);

            } catch (ConnectException e) {
                interaction.printlnMessage(e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                interaction.printlnMessage("Соединение разорвано");
            }
        } catch (ConnectException e) {
            interaction.printlnMessage(e.getMessage());
        } catch (Exception e) {
            interaction.printlnMessage("Возникла непредвиденная ошибка");
            e.printStackTrace();
        }
    }


    private byte[] getResponse() throws IOException {
        byte[] buffer = new byte[65555];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        socketChannel.read(byteBuffer);
        return byteBuffer.array();
    }

    private void setCommandsAvailable() throws ConnectException {
        try {
            byte[] a = getResponse();
            HashMap<String, Pair<String, Pair<Boolean, Boolean>>> map = (HashMap) Serialization.deserialize(a);

            interaction.printlnMessage("Список доступных команд инициализирован");
            interaction.setCommandsAvailable(map);

        } catch (IOException | ClassNotFoundException e) {
            throw new ConnectException("Возникла непредвиденная ошибка соединения при инициализации списка доступных команд");
        }
    }


    private void sendClientRequest(ClientRequest clientRequest) {

        try {
            socketChannel.write(ByteBuffer.wrap(Serialization.serialize(clientRequest)));
            interaction.printlnMessage("Запрос успешно отправлен");
        } catch (IOException e) {
            interaction.printlnMessage("Возникла ошибка при отправке пользовательского запроса на сервер");
        }
    }


    private void setConnectionWithServer() throws ConnectException {
        try {
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            interaction.printlnMessage("Подождите, идет подключение...");
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            interaction.printlnMessage("Соединение в неблокирующем режиме установлено");

        } catch (IOException e) {
            throw new ConnectException("Ошибка соединения с сервером");

        }
    }

    private void setSelector() {
        try {
            selector = Selector.open();
            interaction.printlnMessage("Селектор инициализирован");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Pair<String, Integer> getHostAndPort(String[] args) {
        try {
            if (args.length > 1) {
                String hostToValidate = args[0].trim();
                String host = "localhost";
                int port;
                if (allowedHosts != null && allowedHosts.containsKey(hostToValidate)) {
                    port = allowedHosts.get(hostToValidate);
                } else {
                    if (!hostToValidate.equals("localhost")) {
                        throw new IllegalArgumentException("Введенный хост не поддерживается");
                    }
                    port = Integer.parseInt(args[1]);
                }

                if (port <= 1024) {
                    throw new IllegalArgumentException("Выбранный порт должен превышать 1024");
                }
                return new Pair<>(host, port);

            } else {
                throw new IllegalArgumentException("Вы забыли указать хост и/или порт сервера, к которому собираетесь подключиться");
            }
        } catch (NumberFormatException e) {
            System.out.println("Порт должен быть целым числом");
            emergencyExit();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            emergencyExit();
        }

        return new Pair<>("localhost", 1376);
    }


    private static void loadConfigs() {
        try {
            Path path = Paths.get("config.txt");

            boolean dangerousPath = false;
            try {
                Path realPath = path.toRealPath();
                if (realPath.toString().length() > 3 && realPath.toString().trim().startsWith("/dev")) {
                    dangerousPath = true;
                }
            } catch (IOException e) {
                dangerousPath = true;
            }

            if (!path.toFile().exists() || dangerousPath) {

                System.out.println("Файл с конфигурацией не найден => localhost - единственный допустимый хост");
                allowedHosts = new HashMap<>();

            } else {

                HashMap<String, Integer> allowedHostsBuffer = new HashMap<>();
                boolean problems = false;
                try (Scanner configScanner = new Scanner(path)) {

                    String line;
                    while (configScanner.hasNextLine()) {
                        line = configScanner.nextLine();
                        String[] hostAndPort = (line + " ").split(" ", 2);
                        try {
                            int port = Integer.parseInt(hostAndPort[1].trim());
                            allowedHostsBuffer.put(hostAndPort[0].trim(), port);
                        } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
                            problems = true;
                        }
                    }
                } catch (IOException e) {
                    problems = true;
                }

                if (!problems) {
                    System.out.println("Файл конфигурации успешно прочитан");
                    allowedHosts = allowedHostsBuffer;
                } else {
                    System.out.println("Файл конфигурации некорректен => localhost - единственный допустимый хост");
                    allowedHosts = new HashMap<>();
                }

            }
        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка при поиске или чтении файла конфигурации => localhost - единственный допустимый хост");
            allowedHosts = new HashMap<>();
        }
    }


    private static void emergencyExit() {
        System.out.println("Осуществляется аварийный выход из клиента");
        System.exit(1);
    }


}