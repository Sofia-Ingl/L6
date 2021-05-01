package server.util;

import server.Server;
import shared.serializable.ClientRequest;
import shared.serializable.ServerResponse;
import shared.util.CommandExecutionCode;
import shared.util.Serialization;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection implements Runnable {

    private Server server;
    private Socket socket;

    public ClientConnection(Server generatingServer, Socket client) {
        this.server = generatingServer;
        this.socket = client;
    }

    @Override
    public void run() {

        ClientRequest clientRequest;
        ServerResponse serverResponse;

        try {

            socket.getOutputStream().write(Serialization.serialize(server.getRequestProcessor().getCommandWrapper().mapOfCommandsToSend()));

            do {

                byte[] b = new byte[65536];
                socket.getInputStream().read(b);
                clientRequest = (ClientRequest) Serialization.deserialize(b);
                serverResponse = server.getRequestProcessor().processRequest(clientRequest);
                if (clientRequest.getCommand().equals("exit")) {
                    Server.logger.info(serverResponse.getResponseToPrint());
                    server.getRequestProcessor().getCommandWrapper().getAllInnerCommands().get("save").execute("", null);
                } else {
                    socket.getOutputStream().write(Serialization.serialize(serverResponse));
                }

            } while (serverResponse.getCode() != CommandExecutionCode.EXIT);

        } catch (ClassNotFoundException e) {
            Server.logger.warn("Ошика при чтении данных");
        } catch (IOException e) {
            Server.logger.warn("Соединение разорвано");
        } finally {
            try {
                socket.close();
                Server.logger.info("Клиент успешно отключается");
            } catch (IOException e) {
                Server.logger.warn("Ошибка при попытке закрыть соединение с клиентом");
            }
        }

    }
}
