package com.df.rpc.server.tcp;
import com.df.rpc.model.RpcRequest;
import com.df.rpc.model.RpcResponse;
import com.df.rpc.protocol.*;
import com.df.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
//    @Override
//    public void handle(NetSocket netSocket) {
//
//        netSocket.handler(buffer -> {
//            ProtocolMessage<RpcRequest> protocolMessage;
//            try {
//                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
//            } catch (IOException e) {
//                throw new RuntimeException("协议编码错误");
//            }
//            ProtocolMessage.Header header = protocolMessage.getHeader();
//            RpcRequest rpcRequest = protocolMessage.getBody();
//            String serviceName = rpcRequest.getServiceName();
//            // 根据服务名获取服务实例
//            RpcResponse response = new RpcResponse();
//            try {
//                Class<?> aClass = LocalRegistry.get(serviceName);
//                Method method = aClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
//                Object object = method.invoke(aClass.newInstance(), rpcRequest.getArgs());
//                response.setData(object);
//                response.setDataType(method.getReturnType());
//                response.setMessage("ok");
//            }catch (Exception e){
//                response.setMessage(e.getMessage());
//                response.setException(e);
//            }
//            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
//            responseProtocolMessage.setBody(response);
//            ProtocolMessage.Header responseHeader = protocolMessage.getHeader();
//            responseHeader.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
//            responseProtocolMessage.setHeader(responseHeader);
//            // 将响应编码后发送
//            try {
//                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
//                netSocket.write(encode);
//            } catch (IOException e) {
//                throw new RuntimeException("协议消息编码失败");
//            }
//
//        });
//    }

    /**
     * 解决粘包问题后
     * @param socket
     */
    @Override
    public void handle(NetSocket socket) {
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            System.out.println("收到请求：-----------------------------------------------------------------------------" + rpcRequest);
            ProtocolMessage.Header header = protocolMessage.getHeader();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                socket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        socket.handler(bufferHandlerWrapper);
    }
}
