package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;


public class NioServer {

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buffer;


    public NioServer() throws IOException {
        buffer = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open(); // accept -> SocketChannel
        server.bind(new InetSocketAddress(8182));
        selector = Selector.open();
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        StringBuilder sb = new StringBuilder();

        while (true){
            int read = channel.read(buffer);
            if(read == -1){
                channel.close();
                return;
            }

            if(read == 0){
                break;
            }

            buffer.flip();
            while(buffer.hasRemaining()){
                sb.append((char)buffer.get());
            }
            buffer.clear();

            String result = "[From serever]: " + sb.toString();

            channel.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
        }

    }

    private void handleAccept(SelectionKey key) throws IOException {
        // ServerSocketChannel server = (ServerSocketChannel) key.channel(); // t.k. obsluzivaet odin server
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }
}
