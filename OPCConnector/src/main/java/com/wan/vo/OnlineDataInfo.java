package com.wan.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WanYue
 * @date 2024-08-09
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineDataInfo {

    private Integer refreshRate;

    private String item;

    private String clientId;

}
