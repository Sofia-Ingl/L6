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
    private final int reconnectionTimeout;
    private int reconnectionAttempts = 0;
    private final int maxReconnectionAttempts;
    private SocketChannel socketChannel;
    private Selector selector;
    private SocketAddress socketAddress;
    private Interaction interaction;
    private boolean needCommands = true;

    public static void main(String[] args) {

        Interaction interaction = new Interaction(new UserElementGetter());
        boolean reconnect = false;
        Client client = new Client("localhost", 666, 3, 1000,
                interaction);
        client.run();
        System.out.println("Хотите переподключиться? (да/нет)");
        reconnect = interaction.readLine().trim().toLowerCase().equals("да");
        while (reconnect) {
            //client = new Client("localhost", 666, 3, 1000,
            //       interaction);
            client.run();
            System.out.println("Хотите переподключиться? (да/нет)");
            reconnect = interaction.readLine().trim().toLowerCase().equals("да");
        }


    }


    public Client(String host, int port, int maxReconnectionAttempts, int reconnectionTimeout, Interaction interaction) {
        this.host = host;
        this.port = port;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.reconnectionTimeout = reconnectionTimeout;
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

        //while (!clientExitCode && reconnectionAttempts < maxReconnectionAttempts) {
        //  while (!clientExitCode) {

        try {

            setConnectionWithServer();

//                try {
//                    if (needCommands) {
//                        System.out.println("NEED");
//                        sendClientRequest(new ClientRequest("need_commands", "", null));
//                        needCommands = false;
//                        byte[] commands = getResponse();
//                        interaction.setCommandsAvailable((HashMap) Serialization.deserialize(commands));
//                    } else {
//                        sendClientRequest(new ClientRequest("dont_need_commands", "", null));
//                    }
//
//                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }

            setSelector();

            setCommandsAvailable();

            try {
                //
                for (String s :
                        interaction.getCommandsAvailable().keySet()) {
                    System.out.println(s + " интерактивна? " + interaction.getCommandsAvailable().get(s).getSecond().getFirst()
                            + " принимает строчной аргумент? " + interaction.getCommandsAvailable().get(s).getSecond().getSecond());
                }
                //

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

                            //
                            //
                            if (request.getCommand().equals("exit")) {
                                clientExitCode = true;
                            }
                            //
                            //
                            //System.out.println("Sent");
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                //e.printStackTrace();
                System.out.println("Соединение разорвано");
            }
        } catch (ConnectException e) {
            System.out.println("Ошибка соединения");
//
//                System.out.println(e.getMessage());
//                try {
//                    //socketChannel.close();
//                    socketChannel.finishConnect();
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//                reconnectionAttempts++;
//
//                if (reconnectionAttempts == maxReconnectionAttempts) {
//                    System.out.println("Число попыток переподключения исчерпано, клиент завершает работу.");
//                } else {
//                    System.out.println("До следующей поытки подключение осталось " + reconnectionTimeout / 1000 + " сек");
//                    try {
//                        Thread.sleep(reconnectionTimeout);
//                    } catch (Exception exception) {
//                        exception.printStackTrace();
//                    }
//                }
//
//
//            }
        }
    }


    private byte[] getResponse() throws IOException {
        byte[] buffer = new byte[65555];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        socketChannel.read(byteBuffer);
        return byteBuffer.array();
    }

    private void setCommandsAvailable() {
        System.out.println("AAAAAAAAA");
        try {
            byte[] a = getResponse();
            HashMap<String, Pair<String, Pair<Boolean, Boolean>>> map = (HashMap) Serialization.deserialize(a);
            interaction.setCommandsAvailable(map);
        } catch (StreamCorruptedException e) {
            //e.printStackTrace();
            setCommandsAvailable();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    // ПЕРЕПИСАТЬ С ПОМОЩЬЮ SERIALIZE
    private void sendClientRequest(ClientRequest clientRequest) {

        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream)) {

            objectStream.writeObject(clientRequest);
            socketChannel.write(ByteBuffer.wrap(byteArrayStream.toByteArray()));
            System.out.println("Запрос успешно отправлен.");

        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Ошибка при отправке пользовательского запроса на сервер");
        }
    }


    //private void setConnectionWithServer() throws ConnectException {
    private void setConnectionWithServer() throws ConnectException {
        try {
            socketAddress = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(socketAddress);
            //if (socketChannel == null) throw new ConnectException("Сервер недоступен.");

            //socketChannel.socket().getInputStream().readAllBytes();

            socketChannel.configureBlocking(false);
            System.out.println("Соединение с сервером в неблокирующем режиме установлено");
        } catch (IOException e) {
            e.printStackTrace();
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

    public Selector getSelector() {
        return selector;
    }


}