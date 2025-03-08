package com.df.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

public class VertxTcpClient {

    public void start() {

        //创建vertx实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("Connected!");
                NetSocket socket = res.result();
                //发送数据
                socket.write("hello world");
                socket.handler(buffer -> {
                    System.out.println("Received data: " + buffer.toString());
                });
            }
        });
    }

    public static void main(String[] args) {
        VertxTcpClient client = new VertxTcpClient();
        client.start();
    }
}
