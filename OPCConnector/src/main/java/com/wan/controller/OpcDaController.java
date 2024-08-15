package com.wan.controller;

import com.wan.entity.Node;
import com.wan.entity.SelectOption;
import com.wan.service.OpcDaService;
import com.wan.util.RestResponse;
import com.wan.vo.OnlineDataInfo;
import com.wan.vo.ServerInfoVo;
import org.openscada.opc.dcom.list.ClassDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WanYue
 * @date 2024-08-05
 * @description
 */

@RestController
@RequestMapping("/opc/da")
public class OpcDaController {

    @Resource
    OpcDaService daService;

    @PostMapping("/serverList")
    public RestResponse<List<SelectOption>> getServerList(
            @RequestBody ServerInfoVo serverInfoVo) {
        if (serverInfoVo.getHost() == null || serverInfoVo.getUserName() == null || serverInfoVo.getPassword() == null) {
            return RestResponse.failure("参数不能为空");
        }
        Collection<ClassDetails> list = daService.getServerList(serverInfoVo.getHost(), serverInfoVo.getUserName(), serverInfoVo.getPassword());
        List<SelectOption> selectOptions = list.stream()
                .map(classDetails -> {
                    SelectOption option = new SelectOption();
                    option.setLabel(classDetails.getDescription());
                    option.setDisabled(false);
                    option.setValue(classDetails.getClsId());
                    option.setTitle(classDetails.getProgId());
                    return option;
                })
                .collect(Collectors.toList());
        return RestResponse.success(selectOptions);
    }

    @PostMapping("/connect")
    public RestResponse<List<Node>> connectServer(@RequestBody ServerInfoVo serverInfoVo) {
        if (serverInfoVo.getHost() == null || serverInfoVo.getUserName() == null
                || serverInfoVo.getPassword() == null || serverInfoVo.getClsid() == null) {
            return RestResponse.failure("参数不能为空");
        }
        //获取所有item
        return RestResponse.success(daService.getItemTree(serverInfoVo.getHost(),
                serverInfoVo.getUserName(), serverInfoVo.getPassword(), serverInfoVo.getClsid()));
    }

    @PostMapping("/read")
    public RestResponse<String> read(@RequestBody OnlineDataInfo dataInfo) {
            daService.syncRead(
                    dataInfo.getRefreshRate(),
                    dataInfo.getItem(),
                    dataInfo.getClientId());
            daService.bind();
        return RestResponse.success("点位读取正常，请连接webSocket查看数据");
    }

    @GetMapping("/start")
    public RestResponse<String> start() {
        daService.bind();
        return RestResponse.success("点位停止读取");
    }

    @GetMapping("/stop/{clientId}")
    public RestResponse<String> stop(@PathVariable(value = "clientId") String clientId) {
        daService.unbound(clientId);
        return RestResponse.success("点位停止读取");
    }
}
