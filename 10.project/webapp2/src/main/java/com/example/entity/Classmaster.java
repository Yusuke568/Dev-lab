package com.example.entity;

import java.io.Serializable;

/**
 * 役職マスタ情報を表すエンティティクラス。
 */
public class Classmaster implements Serializable {
    private static final long serialVersionUID = 1L;

    // 役職ID
    private String id;
    // 役職名
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
