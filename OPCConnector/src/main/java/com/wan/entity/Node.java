package com.wan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WanYue
 * @date 2024-08-07
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    private String label;

    private String value;

    private Boolean disabled;

    private Boolean checkable;

    private Boolean activable;

    private Boolean leaf;

    List<Node> children = new ArrayList<>();

    public void addChild(Node child) {
        this.children.add(child);
    }
}
