package client;

import client.util.Interaction;
import client.util.UserElementGetter;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;
import shared.util.Serialization;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Client implements Runnable {

    private final String host;
    private final int port;
    private SocketChannel socketChannel;
    private Selector selector;
    private SocketAddress socketAddress;
    private final Interaction interaction;

    public static void main(String[] args) {

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
//                selector.select();

                selector.select(5000);
                boolean reconnect;
                while (selector.selectedKeys().isEmpty()) {
                    interaction.printlnMessage("Сервер занят. Хотите продолжить ожидание? (yes)");
                    interaction.printMessage(">");
                    reconnect = interaction.readLine().trim().toLowerCase().equals("yes");
                    if (!reconnect) {
                        System.exit(0);
                    }
                    selector.select(5000);
                }

                selector.selectedKeys().clear();
                setCommandsAvailable();

                for (String s :
                        interaction.getCommandsAvailable().keySet()) {
                    interaction.printlnMessage(s + " интерактивна? " + interaction.getCommandsAvailable().get(s).getSecond().getFirst()
                            + " принимает строчной аргумент? " + interaction.getCommandsAvailable().get(s).getSecond().getSecond());
                }

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
                            interaction.printlnMessage(response.toString());
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
                System.exit(0);

            } catch (IOException | ClassNotFoundException e) {
                interaction.printlnMessage("Соединение разорвано");
            }
        } catch (ConnectException e) {
            interaction.printlnMessage(e.getMessage());

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
            interaction.printlnMessage("Запрос успешно отправлен.");
        } catch (IOException e) {
            interaction.printlnMessage("Возникла ошибка при отправке пользовательского запроса на сервер");
        }
    }


    private void setConnectionWithServer() throws ConnectException {
        try {
            socketAddress = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            interaction.printlnMessage("Соединение с сервером в неблокирующем режиме установлено");
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
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                if (port <= 1024) {
                    throw new IllegalArgumentException("Выбранный порт должен превышать 1024");
                }
                return new Pair<>(host, port);

            } else {
                throw new IllegalArgumentException("Вы забыли указать хост и/или порт сервера, к которому собираетесь подключиться");
            }
        } catch (NumberFormatException e) {
            System.out.println("Порт должен быть целым числом");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }


}