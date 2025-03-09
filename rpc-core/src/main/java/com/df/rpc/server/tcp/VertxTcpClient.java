package com.df.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.df.rpc.RpcApplication;
import com.df.rpc.model.RpcRequest;
import com.df.rpc.model.RpcResponse;
import com.df.rpc.model.ServiceMetaInfo;
import com.df.rpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public static Object doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
                result -> {
                    if (result.succeeded()) {
                        System.out.println("Connected to ------------------------------------------------TCP server");
                        io.vertx.core.net.NetSocket socket = result.result();
                        // 发送数据
                        // 构造消息
                        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        protocolMessage.setHeader(header);
                        protocolMessage.setBody(rpcRequest);
                        // 编码请求
                        try {
                            Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(encodeBuffer);
                        } catch (IOException e) {
                            throw new RuntimeException("协议消息编码错误");
                        }

                        // 接收响应
                        socket.handler(buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                responseFuture.complete(rpcResponseProtocolMessage.getBody());
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息解码错误");
                            }
                        });
                    } else {
                        System.err.println("Failed to connect to TCP server");
                    }
                });
        RpcResponse rpcResponse = responseFuture.get();
        // 记得关闭连接
        netClient.close();
        return rpcResponse;
    }

    public static void main(String[] args) {
        VertxTcpClient client = new VertxTcpClient();
        client.start();
    }
}
