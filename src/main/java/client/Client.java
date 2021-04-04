package client;

import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {

    private String host;
    private int port;
    private int reconnectionTimeout;
    private int reconnectionAttempts = 0;
    private int maxReconnectionAttempts;
    private SocketChannel socketChannel;
    private Selector selector;
    private SocketAddress socketAddress;
    private ByteBuffer inputBuffer;
    //ObjectInputStream responseReader;


    public static void main(String[] args) {

        Client client = new Client("localhost", 666, 1, 10000);
        client.run();


        // СЕЛЕКТОРЫ !!!!!!!!!!!!!!!
        client.sendClientRequest(new ClientRequest("help", "", null));
        client.readServerResponse();



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
        //initInputBuffer();

        /*
        try {
            responseReader = new ObjectInputStream(socketChannel.socket().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

         */

        /*
        try {

            socketChannel.register(selector, SelectionKey.OP_WRITE);

            //РАБОТА С ГОТОВНОСТЬЮ СЕРВЕРА


        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

         */


    }


    private void initInputBuffer() {
        System.out.println("Буфер готов к записи");
        inputBuffer = ByteBuffer.wrap(new byte[1000000]);
    }

    /*
    private ServerResponse readServerResponse() {

        try {

            return (ServerResponse) responseReader.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

     */


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

/*
Обязанности клиентского приложения:

Чтение команд из консоли.
Валидация вводимых данных.
Сериализация введённой команды и её аргументов.
Отправка полученной команды и её аргументов на сервер.
Обработка ответа от сервера (вывод результата исполнения команды в консоль).
Команду save из клиентского приложения необходимо убрать.
Команда exit завершает работу клиентского приложения.
Важно! Команды и их аргументы должны представлять из себя объекты классов.
Недопустим обмен "простыми" строками. Так, для команды add или её аналога необходимо сформировать объект,
содержащий тип команды и объект, который должен храниться в вашей коллекции.
 */