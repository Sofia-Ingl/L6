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

    private String host;
    private int port;
    private SocketChannel socketChannel;
    private Selector selector;
    private SocketAddress socketAddress;
    private Interaction interaction;

    public static void main(String[] args) {

        Interaction interaction = new Interaction(new UserElementGetter());
        boolean reconnect;
        Client client = new Client("localhost", 1666, interaction);
        client.run();
        System.out.println("Хотите переподключиться? (да/нет)");
        reconnect = interaction.readLine().trim().toLowerCase().equals("да");
        while (reconnect) {
            client.run();
            System.out.println("Хотите переподключиться? (да/нет)");
            reconnect = interaction.readLine().trim().toLowerCase().equals("да");
        }


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
                selector.select();
                selector.selectedKeys().clear();
                setCommandsAvailable();

                for (String s :
                        interaction.getCommandsAvailable().keySet()) {
                    System.out.println(s + " интерактивна? " + interaction.getCommandsAvailable().get(s).getSecond().getFirst()
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
                            System.out.println(response);
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

                System.out.println("Клиент завершил работу приложения.");
                System.exit(0);

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Соединение разорвано");
            }
        } catch (ConnectException e) {
            System.out.println(e.getMessage());

        }
    }


    private byte[] getResponse() throws IOException {
        byte[] buffer = new byte[65555];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        socketChannel.read(byteBuffer);
        return byteBuffer.array();
    }

    private void setCommandsAvailable() {
        try {
            byte[] a = getResponse();
            HashMap<String, Pair<String, Pair<Boolean, Boolean>>> map = (HashMap) Serialization.deserialize(a);

            System.out.println("Список доступных команд инициализирован");
            interaction.setCommandsAvailable(map);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    private void sendClientRequest(ClientRequest clientRequest) {

        try {
            socketChannel.write(ByteBuffer.wrap(Serialization.serialize(clientRequest)));
            System.out.println("Запрос успешно отправлен.");
        } catch (IOException e) {
            System.out.println("Возникла ошибка при отправке пользовательского запроса на сервер");
        }
    }


    private void setConnectionWithServer() throws ConnectException {
        try {
            socketAddress = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            System.out.println("Соединение с сервером в неблокирующем режиме установлено");
        } catch (IOException e) {
            throw new ConnectException("Ошибка соединения с сервером");

        }
    }

    private void setSelector() {
        try {
            selector = Selector.open();
            System.out.println("Селектор инициализирован");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}