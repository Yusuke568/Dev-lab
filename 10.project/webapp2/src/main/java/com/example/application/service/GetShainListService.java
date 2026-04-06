package com.example.application.service;

import com.example.application.port.in.GetShainListUseCase;
import com.example.application.port.out.ShainPort;
import com.example.entity.Shain;

import java.util.List;

/**
 * 社員一覧取得ユースケースの実装。
 */
public class GetShainListService implements GetShainListUseCase {

    private final ShainPort shainPort;

    /**
     * コンストラクタで永続化ポートを注入（DI）します。
     * @param shainPort 社員データへのアクセスを提供するポート
     */
    public GetShainListService(ShainPort shainPort) {
        this.shainPort = shainPort;
    }

    @Override
    public List<Shain> getShainList() {
        // ポートを通じて永続化層に全件検索を依頼する
        return shainPort.findAll();
    }
}
