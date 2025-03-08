package com.df.rpc.server.tcp;

import com.df.rpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

public class VertxTcpServer implements HttpServer {

    private byte[] handleRequest(byte[] requestData) {
        // 在这里编写处理请求的逻辑，根据 requestData 构造响应数据并返回
        // 这里只是一个示例，实际逻辑需要根据具体的业务需求来实现
        return "Hello, client!".getBytes();
    }
    @Override
    public void doStart(int port) {
        /*
          创建vertx实例
         */
        Vertx vertx = Vertx.vertx();

        /*
          创建Tcp服务器
         */
        NetServer server = vertx.createNetServer();

        server.connectHandler(new TcpServerHandler());

        server.listen(port, "localhost", res -> {
            if (res.succeeded()) {
                System.out.println("Server started on port " + port);
            }else {
                System.out.println("Failed to start server: " + res.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) {
        VertxTcpServer server = new VertxTcpServer();
        server.doStart(8888);
    }
}
