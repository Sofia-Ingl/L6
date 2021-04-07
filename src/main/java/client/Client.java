package client;

import client.util.Interaction;
import client.util.UserElementGetter;
import shared.serializable.ClientRequest;
import shared.serializable.Pair;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;
import shared.util.Serialization;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client implements Runnable {

    private String host;
    private int port;
    private int reconnectionTimeout;
    private int reconnectionAttempts = 0;
    private int maxReconnectionAttempts;
    private SocketChannel socketChannel;
    private Selector selector;
    private SocketAddress socketAddress;
    private Interaction interaction;

    public static void main(String[] args) {

        Client client = new Client("localhost", 666, 1, 10000,
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

        setConnectionWithServer();
        setSelector();

        byte[] b;
        CommandExecutionCode code;
        ServerResponse response;
        SelectionKey selectionKey;

        try {
            //
            setCommandsAvailable();
            for (String s:
                 interaction.getCommandsAvailable().keySet()) {
                System.out.println(s + " " + interaction.getCommandsAvailable().get(s).getSecond());
            }
            //

            socketChannel.register(selector, SelectionKey.OP_WRITE);
            while (true) {
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
                        sendClientRequest(interaction.formRequest());
                        //sendClientRequest(new ClientRequest("help", "", null));
                        System.out.println("Sent");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
            interaction.setCommandsAvailable((HashMap) Serialization.deserialize(a));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*

    private void fillTheBuffer() {
        try {
            socketChannel.register(selector, SelectionKey.OP_READ);

            while (true) {
                int count = selector.select();
                // нечего обрабатывать
                if (count == 0) {
                    continue;
                }
                inputBuffer.clear();
                socketChannel.read(inputBuffer);
                inputBuffer.flip();
                selector.selectedKeys().clear();
                break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ServerResponse readServerResponse() {

        fillTheBuffer();

        try {
//            inputBuffer.clear();
//            socketChannel.read(inputBuffer);
//            inputBuffer.flip();

            fillTheBuffer();

            try (ByteArrayInputStream buf = new ByteArrayInputStream(inputBuffer.array());
                 ObjectInputStream objectReader = new ObjectInputStream(buf)) {

                return (ServerResponse) objectReader.readObject();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

     */


    private void sendClientRequest(ClientRequest clientRequest) {

        try (ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream)) {

            objectStream.writeObject(clientRequest);
            socketChannel.write(ByteBuffer.wrap(byteArrayStream.toByteArray()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setConnectionWithServer() {
        try {
            socketAddress = new InetSocketAddress(host, port);
            socketChannel = SocketChannel.open(socketAddress);
            socketChannel.configureBlocking(false);
            System.out.println("Соединение с сервером в неблокирующем режиме установлено");
        } catch (IOException e) {
            e.printStackTrace();
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