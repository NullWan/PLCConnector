package com.wan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wan.entity.DataPoint;
import com.wan.entity.Node;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jinterop.dcom.common.JIErrorCodes;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.*;
import org.openscada.opc.lib.da.browser.Branch;
import org.openscada.opc.lib.da.browser.FlatBrowser;
import org.openscada.opc.lib.da.browser.TreeBrowser;
import org.openscada.opc.lib.list.Categories;
import org.openscada.opc.lib.list.Category;
import org.openscada.opc.lib.list.ServerList;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * @author WanYue
 * @date 2024-08-05
 * @description
 */

@Log4j2
@Service
public class OpcDaService {

    private Server server;

    @Setter
    @Getter
    private String host;

    @Setter
    @Getter
    private String user;

    @Setter
    @Getter
    private String password;

    @Setter
    @Getter
    private String clsId;

    /**
     * 获取服务器列表
     *
     * @param host     服务器地址
     * @param user     用户名
     * @param password 密码
     * @return 服务器列表
     */
    public List<ClassDetails> getServerList(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        JISession session = JISession.createSession("", user, password);
        Collection<ClassDetails> servers;
        try {
            ServerList serverList = new ServerList(session, host);

            servers = serverList.listServersWithDetails(new Category[]{Categories.OPCDAServer10, Categories.OPCDAServer20, Categories.OPCDAServer30}, new Category[]{});
        } catch (UnknownHostException e) {
            log.error(e);
            throw new RuntimeException("目标主机错误：" + host + ", 请输入正确的服务器地址！");
        } catch (JIException e) {
            log.error(e);
            throw new RuntimeException("OPC DA连接失败: 请检查用户名密码是否正确！");
        }

        return servers.stream().toList();
    }


    private Boolean isConnect = false;

    /**
     * 连接OPC DA服务器
     *
     * @param host     服务器地址
     * @param user     用户名
     * @param password 密码
     * @param clsId    服务器类ID
     */
    public void connect(String host, String user, String password, String clsId) {
        this.clsId = clsId;
        if (checkInfo(host, user, password, clsId)) {
            // 创建连接信息
            ConnectionInformation information = new ConnectionInformation();
            information.setHost(host);
            information.setUser(user);
            information.setPassword(password);
            information.setClsid(clsId);
            server = new Server(information, Executors.newSingleThreadScheduledExecutor());
            try {
                server.connect();
                isConnect = true;
            } catch (UnknownHostException e) {
                isConnect = false;
                log.error(e);
                throw new RuntimeException("目标主机错误：" + host + ", 请输入正确的服务器地址！");
            } catch (JIException e) {
                isConnect = false;
                log.error(e);
                throw new RuntimeException("OPC DA连接失败: 请检查用户名密码是否正确！");
            } catch (AlreadyConnectedException e) {
                log.warn("OPC DA 服务器已经连接，请勿重复创建连接!");
            }
        }
    }

    /**
     * 断开OPC DA server连接
     */
    public void disconnect(String clientId) {
        if (!baseMap.containsKey(clientId)) {
            return;
        }
        AccessBase base = baseMap.get(clientId);
        try {
            base.clear();
            base.unbind();
        } catch (JIException e) {
            log.error("解除绑定失败：" + e.getMessage());
            throw new RuntimeException("断开服务器操作错误,请重试");
        }
    }

    private void disConnectServer() {
        if (server == null) {
            return;
        }
        server.disconnect();
        server = null;
        isConnect = false;
    }

    /**
     * 获取数据点列表
     *
     * @return 数据点列表
     */
    @Deprecated
    public List<String> getItemListAll() {
        connect(host, user, password, clsId);
        FlatBrowser browser = server.getFlatBrowser();
        Collection<String> browse;
        if (browser == null) {
            throw new RuntimeException("获取数据点列表出错：OPC DA服务器未连接！");
        }
        try {
            browse = browser.browse();
        } catch (UnknownHostException e) {
            log.error(e);
            throw new RuntimeException("获取数据点列表出错：" + e.getMessage());
        } catch (JIException e) {
            log.error(e);
            throw new RuntimeException("获取数据点列表出错：请检查用户名密码是否正确！");
        } finally {
            disConnectServer();
        }
        if (browse == null) {
            return Collections.emptyList();
        }
        return browse.stream().toList();
    }


    /**
     * 获取数据点树
     *
     * @param host     主机地址
     * @param user     用户名
     * @param password 密码
     * @param clsId    OPC DA server CLSID
     * @return 数据点树
     */
    public List<Node> getItemTree(String host, String user, String password, String clsId) {
        connect(host, user, password, clsId);
        Node root = new Node();
        List<Node> nodes = new ArrayList<>();
        try {
            TreeBrowser tree = server.getTreeBrowser();
            try {
                //顶级节点
                Branch browse = tree.browse();
                root.setLabel("root");
                root.setCheckable(false);
                root.setLeaf(false);
                root.setActivable(false);
                //寻找所有子节点
                getTree(browse, root);
            } catch (UnknownHostException e) {
                log.error(e);
                throw new RuntimeException("目标主机错误：" + host + ", 请输入正确的服务器地址！");
            }
        } catch (JIException e) {
            log.error(e);
            throw new RuntimeException("获取数据点列表出错：请检查用户名密码是否正确！");
        } finally {
            disConnectServer();
        }
        nodes.add(root);
        return nodes;
    }

    /**
     * 获取数据点树
     *
     * @param branchCurrent 当前节点
     * @param parentNode    父节点
     */
    private void getTree(Branch branchCurrent, Node parentNode) {
        if (branchCurrent.getBranches().isEmpty()) {
            return;
        }
        branchCurrent.getBranches().forEach(branch -> {
            Node child = new Node();
            child.setLabel(branch.getName());
            child.setValue(String.join(".", branch.getBranchStack()));
            child.setCheckable(false);
            child.setLeaf(false);
            child.setActivable(false);
            branch.getLeaves().forEach(leaf -> {
                Node leafNode = new Node();
                leafNode.setLabel(leaf.getName());
                leafNode.setValue(String.join(".", leaf.getParent().getBranchStack()) + "." + leaf.getName());
                leafNode.setLeaf(true);
                leafNode.setCheckable(true);
                leafNode.setActivable(true);
                child.addChild(leafNode);
            });
            parentNode.addChild(child);
            getTree(branch, child);
        });
    }

    @Resource
    private ObjectMapper objectMapper;

    @Getter
    private ConcurrentHashMap<String, AccessBase> baseMap = new ConcurrentHashMap<>();

    @Resource
    SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 同步读取实时值(单条数据)
     *
     * @param refreshRate 刷新频率
     * @param item        数据点
     * @param clientId    客户端ID
     */
    public AccessBase syncRead(Integer refreshRate, @NotNull String item, @NotNull String clientId) {
        if (!isConnect) {
            connect(host, user, password, clsId);
        }
        AccessBase base;
        if (baseMap.containsKey(clientId)) {
            base = baseMap.get(clientId);
        } else {
            try {
                base = new SyncAccess(server, refreshRate);
            } catch (UnknownHostException e) {
                log.error(e);
                throw new RuntimeException("读取实时值出错，未知主机：" + e.getMessage());
            } catch (NotConnectedException e) {
                connect(host, user, password, clsId);
                log.error("OPC服务器未连接，开始重连...");
                throw new RuntimeException("读取实时值出错，OPC 服务未连接：" + e.getMessage());
            } catch (JIException e) {
                log.error(e);
                throw new RuntimeException("获取数据点列表出错：请检查用户名密码是否正确！");
            } catch (DuplicateGroupException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            base.addItem(item, (item1, itemState) -> {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setName(item1.getId());
                dataPoint.setId(item1.getId());
                String json;
                try {
                    dataPoint.setValue(getValueByType(item1.read(true).getValue()));
                } catch (JIException e) {
                    throw new RuntimeException(e);
                }
                dataPoint.setTimestamp(itemState.getTimestamp().getTimeInMillis());
                dataPoint.setQuality(itemState.getQuality());
                try {
                    json = objectMapper.writeValueAsString(dataPoint);
                } catch (JsonProcessingException e) {
                    log.error("转换数据点为json失败：" + dataPoint);
                    throw new RuntimeException(e);
                }
                simpMessagingTemplate.convertAndSend("/topic/opc/realtime", json);
            });
            baseMap.put(clientId, base);
        } catch (JIException e) {
            throw new RuntimeException(e);
        } catch (AddFailedException e) {
            log.error("添加数据点失败：" + item);
            throw new RuntimeException(e);
        }
        return base;
    }

    /**
     * 移除数据点
     *
     * @param clientId 客户端Id
     * @param item     数据点
     */
    public void removeItem(String clientId, String item) {
        if (!baseMap.containsKey(clientId)) {
            return;
        }
        AccessBase base = baseMap.get(clientId);
        base.removeItem(item);
    }

    /**
     * 同步读取实时值绑定
     */
    public void bind(String clientId) {
        if (!baseMap.containsKey(clientId)) {
            return;
        }
        AccessBase base = baseMap.get(clientId);
        if (base == null || base.isBound()) {
            // 如果base为空或已激活，则直接返回
            return;
        }
        base.bind();
    }

    /**
     * 解除绑定
     */
    public void unbound(String clientId) {
        if (!baseMap.containsKey(clientId)) {
            return;
        }
        AccessBase base = baseMap.get(clientId);
        if (base == null || !base.isBound()) {
            // 如果base为空或未激活，则直接返回
            return;
        }
        try {
            base.unbind();
        } catch (JIException e) {
            log.error("解除绑定失败：" + e.getMessage());
            throw new RuntimeException("服务器出现错误，请稍后重试，{}", e);
        }
    }

    /**
     * 检查连接信息
     *
     * @return Boolean
     */
    private Boolean checkInfo(String host, String user, String password, String clsId) {
        if (host == null || host.isEmpty()) {
            throw new RuntimeException("请输入OPC服务器地址");
        }
        if (user == null || user.isEmpty()) {
            throw new RuntimeException("请输入OPC服务器用户名");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("请输入OPC服务器密码");
        }
        if (clsId == null || clsId.isEmpty()) {
            throw new RuntimeException("请选择OPC服务器");
        }
        return true;
    }


    /**
     * 数据类型转换
     *
     * @return Object
     * @throws JIException 异常
     */
    private Object getValueByType(JIVariant var) throws JIException {
        int variantType = var.getType();
        return switch (variantType) {
            case JIVariant.VT_I2 -> var.getObjectAsShort();
            case JIVariant.VT_I4 -> var.getObjectAsInt();
            case JIVariant.VT_I8 -> var.getObjectAsLong();
            case JIVariant.VT_R4 -> var.getObjectAsFloat();
            case JIVariant.VT_R8 -> var.getObjectAsDouble();
            case JIVariant.VT_BSTR -> var.getObjectAsString2();
            case JIVariant.VT_BOOL -> var.getObjectAsBoolean();
            case JIVariant.VT_UI2, JIVariant.VT_UI4 -> var.getObjectAsUnsigned().getValue();
            case JIVariant.VT_EMPTY -> throw new JIException(JIErrorCodes.JI_VARIANT_IS_NULL, "Variant is Empty.");
            case JIVariant.VT_NULL -> throw new JIException(JIErrorCodes.JI_VARIANT_IS_NULL, "Variant is null.");
            default -> throw new JIException(JIErrorCodes.JI_VARIANT_IS_NULL, "Unknown Type.");
        };
    }
}
