package sk.upjs.kopr2014.ppatrik.client;

import sk.upjs.kopr2014.ppatrik.server.Server;

public class ClientServer {
    public static void main(String[] args) {
        new Server().start();
        new ClientGui();
    }
}
