package com.danlls.daniel.pastelink.util;

import java.net.Socket;

/**
 * Created by danieL on 12/20/2017.
 */

public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }
}