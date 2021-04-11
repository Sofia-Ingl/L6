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

    public static void main(String[] args) {

        Client client = new Client("localhost", 666, 3, 10000,
                new Interaction(new UserElementGetter()));
        client.run();


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

//        while (!clientExitCode && reconnectionAttempts < maxReconnectionAttempts) {
//
//            try {
        setConnectionWithServer();

        setSelector();

        try {
            //
            setCommandsAvailable();
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
                        sendClientRequest(request);
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
//            } catch (ConnectException e) {
//
//                System.out.println(e.getMessage());
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
//            }
//        }


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
            interaction.setCommandsAvailable((HashMap) Serialization.deserialize(a));
        } catch (IOException | ClassNotFoundException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


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
    private void setConnectionWithServer() {
        try {
            socketAddress = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            System.out.println("Соединение с сервером в неблокирующем режиме установлено");
        } catch (IOException e) {
            e.printStackTrace();
            //
            //throw new ConnectException("Ошибка соединения с сервером");
            //
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