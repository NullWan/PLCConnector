package com.wan.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WanYue
 * @date 2024-08-06
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfoVo {

    private String host;

    private String userName;

    private String password;

    private String clsid;

    private String progId;

    private String domain;
}
