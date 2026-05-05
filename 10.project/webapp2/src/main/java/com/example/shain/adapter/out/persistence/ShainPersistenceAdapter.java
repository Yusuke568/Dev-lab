package com.example.shain.adapter.out.persistence;

import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;
import com.example.dao.ShainDao;
import com.example.adapter.out.persistence.ConnectionBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社員データのための永続化アダプタ（完全ヘキサゴナル版）。
 */
public class ShainPersistenceAdapter implements ShainPort {

    private final ShainDao shainDao;

    public ShainPersistenceAdapter() {
        this.shainDao = new ShainDao();
    }

    @Override
    public List<Shain> findAll() {
        try (Connection con = ConnectionBase.getConnection()) {
            return shainDao.findAll(con).stream()
                    .map(this::mapToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all shain", e);
        }
    }

    @Override
    public Optional<Shain> findById(ShainId id) {
        try (Connection con = ConnectionBase.getConnection()) {
            com.example.entity.Shain entity = shainDao.findById(con, id.getIntValue());
            return Optional.ofNullable(entity).map(this::mapToDomain);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find shain by id: " + id, e);
        }
    }

    @Override
    public void update(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.update(con, mapToEntity(shain));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update shain: " + shain.getId(), e);
        }
    }

    @Override
    public void create(Shain shain) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.create(con, mapToEntity(shain));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shain", e);
        }
    }

    @Override
    public void deleteById(ShainId id) {
        try (Connection con = ConnectionBase.getConnection()) {
            shainDao.deleteById(con, id.getIntValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete shain: " + id, e);
        }
    }

    @Override
    public ShainId nextId() {
        // IDの自動採番ロジック（実際にはDBのシーケンス等を利用）
        String sql = "SELECT MAX(id) FROM staff_table";
        try (Connection con = ConnectionBase.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return ShainId.of(rs.getInt(1) + 1);
            }
            return ShainId.of(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next shain id", e);
        }
    }

    /**
     * 外部のEntity（Bean）をドメインモデルに変換します。
     */
    private Shain mapToDomain(com.example.entity.Shain entity) {
        return new Shain(
                ShainId.of(entity.getId()),
                entity.getName(),
                entity.getNamekana(),
                entity.getEntryyear(),
                entity.getGender(),
                entity.getJobclass(),
                entity.getPaidLeaveDays(),
                entity.getYearsOfService()
        );
    }

    /**
     * ドメインモデルを外部のEntity（Bean）に変換します。
     */
    private com.example.entity.Shain mapToEntity(Shain domain) {
        com.example.entity.Shain entity = new com.example.entity.Shain();
        entity.setId(domain.getId().getIntValue());
        entity.setName(domain.getName());
        entity.setNamekana(domain.getNamekana());
        entity.setEntryyear(domain.getEntryYear());
        entity.setGender(domain.getGender());
        entity.setJobclass(domain.getJobClass());
        entity.setPaidLeaveDays(domain.getPaidLeaveDays());
        entity.setYearsOfService(domain.getYearsOfService());
        return entity;
    }
}
