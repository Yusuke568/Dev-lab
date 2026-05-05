package com.example.application.service;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.application.port.out.ClassmasterPort;
import com.example.entity.Classmaster;

import java.util.List;

/**
 * 全役職惁E��取得ユースケースの実裁E��E
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
