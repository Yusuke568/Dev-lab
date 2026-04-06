package com.example.application.service;

import com.example.application.port.in.InsertShainCommand;
import com.example.application.port.in.InsertShainUseCase;
import com.example.application.port.out.ShainPort;
import com.example.entity.Shain;

/**
 * 社員登録ユースケースの実装。
 */
public class InsertShainService implements InsertShainUseCase {

    private final ShainPort shainPort;

    public InsertShainService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public void insertShain(InsertShainCommand command) {
        // 次のIDを採番するロジック
        int nextId = (int) shainPort.count() + 100;

        // コマンドオブジェクトから永続化用のエンティティを作成
        Shain shain = new Shain();
        shain.setId(nextId);
        shain.setName(command.name());
        shain.setNamekana(command.namekana());
        shain.setGender(command.gender());
        shain.setEntryyear(command.entryyear());
        shain.setJobclass(command.jobclass());

        // 永続化ポートを呼び出して保存
        shainPort.create(shain);
    }
}
