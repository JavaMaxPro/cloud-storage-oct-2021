package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


public class NioServer {

    private Path root;
    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buffer;


    public NioServer() throws IOException {
        root = Path.of("cloud-storage-oct-2021-server");
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
                    System.out.println("Accept");
                }
                if (key.isReadable()) {
                    handleRead(key);
                    System.out.println("Read - ");
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        StringBuilder sb = new StringBuilder();

        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }

            if (read == 0) {
                break;
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
            Path filePath;
            String result = sb.toString().trim();
//            String[] arrResult = result.split(" ");
//            arrResult[0].trim();
//            arrResult[0].toLowerCase(Locale.ROOT);
//            System.out.println(arrResult[0]);
            if (result.equals("ls")) {
                String fileList = Files.list(root)
                        .map(this::mapper)
                        .collect(Collectors.joining("\n")) + "\n";
                printCurrentDirName(channel);
                channel.write(ByteBuffer.wrap(fileList.getBytes(StandardCharsets.UTF_8)));
               System.out.println( "я в - " + result);
            } else {
                if (result.equals("cat")) {
                    filePath = root.resolve(result);
                    if (Files.exists(filePath)) {
                        List<String> fileReadLine = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                        for (String s : fileReadLine) {
                            channel.write(ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                } else {
                    channel.write(ByteBuffer.wrap("No command \n \r".getBytes(StandardCharsets.UTF_8)));
                    printCurrentDirName(channel);
                }
            }

        }

    }

    private void printCurrentDirName(SocketChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(root.toAbsolutePath().getFileName().toString().getBytes(StandardCharsets.UTF_8)));
    }

    private String mapper(Path path) {

        if (Files.isDirectory(root)) {
            return path.getFileName().toString() + " [ DIR ]";
        } else {
            try {
                long size = Files.size(root);
                return path.getFileName().toString() + "[ FILE ]" + size + " bytes";
            } catch (Exception e) {
                throw new RuntimeException("path not exist");
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        // ServerSocketChannel server = (ServerSocketChannel) key.channel(); // t.k. obsluzivaet odin server
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Hi , this is telnet \n\r".getBytes(StandardCharsets.UTF_8)));
        printCurrentDirName(channel);
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }
}
