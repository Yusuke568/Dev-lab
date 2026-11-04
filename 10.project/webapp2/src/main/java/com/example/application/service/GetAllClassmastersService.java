package com.example.application.service;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.application.port.out.ClassmasterPort;
import com.example.entity.Classmaster;

import java.util.List;

/**
 * 全役職情報取得ユースケースの実装。
 */
public class GetAllClassmastersService implements GetAllClassmastersUseCase {

    private final ClassmasterPort classmasterPort;

    public GetAllClassmastersService(ClassmasterPort classmasterPort) {
        this.classmasterPort = classmasterPort;
    }

    @Override
    public List<Classmaster> getAllClassmasters() {
        return classmasterPort.findAll();
    }
}
