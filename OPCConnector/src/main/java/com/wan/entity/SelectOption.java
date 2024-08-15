package com.wan.entity;

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
public class SelectOption {
    private String label;

    private String value;

    private String title;

    private Boolean disabled;
}
