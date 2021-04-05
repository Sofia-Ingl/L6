package client;

import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;
import shared.util.Serialization;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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


    public static void main(String[] args) {

        Client client = new Client("localhost", 666, 1, 10000);
        client.run();


    }


    public Client(String host, int port, int maxReconnectionAttempts, int reconnectionTimeout) {
        this.host = host;
        this.port = port;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.reconnectionTimeout = reconnectionTimeout;
    }

    @Override
    public void run() {

        setConnectionWithServer();
        setSelector();

        try {
            socketChannel.register(selector, SelectionKey.OP_WRITE);
            while (true) {
                int count = selector.select();
                if (count == 0) {
                    break;
                }

                Set keys = selector.selectedKeys();
                Iterator iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        byte[] b = getResponse();
                        ServerResponse response = (ServerResponse) Serialization.deserialize(b);
                        System.out.println(response);
                    }
                    if (selectionKey.isWritable()) {
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        sendClientRequest(new ClientRequest("help", "", null));
                        System.out.println("Sent");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    private byte[] getResponse() throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[65555];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        socketChannel.read(byteBuffer);
        return byteBuffer.array();
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