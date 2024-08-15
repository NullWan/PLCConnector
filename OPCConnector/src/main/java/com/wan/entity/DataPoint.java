package com.wan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author WanYue
 * @date 2024-08-09
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPoint {

    private String id;

    private String name;

    private String label;

    private Object value;

    private Long timestamp;

    private Short quality;

    private String unit;

    private String description;

    @Override
    public String toString() {
        return "DataPoint{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", quality=" + quality +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
