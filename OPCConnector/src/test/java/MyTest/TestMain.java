package MyTest;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.wan.ConnectorMain;
import com.wan.service.OpcDaService;
import lombok.extern.log4j.Log4j2;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JISession;
import org.junit.jupiter.api.Test;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.*;
import org.openscada.opc.lib.da.browser.FlatBrowser;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;

/**
 * @author WanYue
 * @date 2024-08-02
 * @description
 */
@Log4j2

//@SpringBootTest(classes = ConnectorMain.class)
public class TestMain {

    @Test
    public void opcTest() {
        ConnectionInformation information = new ConnectionInformation();
        information.setHost("192.168.134.130");
        information.setUser("OPCUser");
        information.setPassword("8ik,9ol.");
        information.setClsid(org.openscada.opc.dcom.common.Categories.OPCDAServer10);
        //information.setClsid("7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729");
        getServerList("192.168.134.130", "OPCUser", "8ik,9ol.");
        //Server server = getServer(information);
        //server.disconnect();
    }

    private void getServerList(String host, String user, String password) {
        try {
            JISession session = JISession.createSession("", user, password);
            // 创建ServerList对象
            ServerList serverList = new ServerList(session, host);
            // 支持DA 1.0，DA 2.0规范
            Collection<ClassDetails> details = serverList.listServersWithDetails(
                    new Category[]{Categories.OPCDAServer10, Categories.OPCDAServer20},
                    new Category[]{Categories.OPCDAServer30});
            details.forEach((e) -> {

                System.out.println(e.getClsId() + "  " + e.getDescription() + "  " + e.getProgId());
            });
        } catch (UnknownHostException e) {
            throw new RuntimeException("找不到目标主机,{}", e);
        } catch (JIException e) {
            throw new RuntimeException("OPC DA连接失败，{}", e);
        }
    }

    private Server getServer(ConnectionInformation information) {
        Server server = new Server(information, null);

        try {
            server.connect();
            FlatBrowser browser = server.getFlatBrowser(); // 获取FlatBrowser对象
            browser.browse().forEach(System.out::println); // 遍历并打印所有节点

            Group group = server.addGroup("testGroup");

            Map<String, Item> itemMap = group.addItems("Test.T_EQP_1.System1:SVR1_VAR1629_D");

            itemMap.keySet().forEach(key -> {
                Item item = itemMap.get(key);
                try {
                    log.info("item的值为 = {}", item.read(true).getValue());
                } catch (JIException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return server;
    }


    @Test
    public void test3() {
        OpcDaService service = new OpcDaService();
        service.getItemListAll().forEach(System.out::println);
    }

    @Test
    public void test4() {
        OpcDaService service = new OpcDaService();
        service.getItemTree("192.168.134.130", "OPCUser", "8ik,9ol.", "7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729");
    }

    @Test
    public void test5() {
        String address = "127.0.0.1";
        int port = 3000;

        try (Socket socket = new Socket()) {
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            socket.connect(socketAddress);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("Hello, Server!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test6() {
        final int PORT = 3000; // 服务器监听的端口

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP server listening on port " + PORT);

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // 处理客户端连接
                new Thread(() -> {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                        // 读取客户端发送的消息
                        String message = in.readLine();
                        System.out.println("Received message: " + message);

                    } catch (IOException e) {
                        System.err.println("Error reading from client: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



