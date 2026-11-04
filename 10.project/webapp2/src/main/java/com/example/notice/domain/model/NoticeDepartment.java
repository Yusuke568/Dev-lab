package com.example.notice.domain.model;

/**
 * お知らせの宛先にできる部署。
 */
public class NoticeDepartment {

    private final int id;
    private final String name;

    public NoticeDepartment(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
