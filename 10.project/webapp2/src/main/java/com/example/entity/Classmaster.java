package com.example.entity;

import java.io.Serializable;

/**
 * 蠖ｹ閨ｷ繝槭せ繧ｿ諠・ｱ繧定｡ｨ縺吶お繝ｳ繝・ぅ繝・ぅ繧ｯ繝ｩ繧ｹ縲・
 */
public class Classmaster implements Serializable {
    private static final long serialVersionUID = 1L;

    // 蠖ｹ閨ｷID
    private String id;
    // 蠖ｹ閨ｷ蜷・
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
