package com.example.application.service;

import com.example.application.port.in.GetWorkTypesUseCase;
import com.example.application.port.out.WorkTypePort;
import com.example.entity.WorkType;

import java.util.List;

/**
 * 勤務区分リストを取得するユースケースの実装。
 */
public class GetWorkTypesService implements GetWorkTypesUseCase {

    private final WorkTypePort workTypePort;

    public GetWorkTypesService(WorkTypePort workTypePort) {
        this.workTypePort = workTypePort;
    }

    @Override
    public List<WorkType> getWorkTypes() {
        return workTypePort.findAll();
    }
}
