package com.example.auth.testsupport;

import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import com.example.shain.domain.port.out.ShainPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** staff_table の代わりのメモリ上の実装。 */
public class InMemoryShainPort implements ShainPort, FakeTransactionManager.Snapshottable {

    private Map<ShainId, Shain> shains = new HashMap<>();
    private Map<ShainId, Integer> departments = new HashMap<>();
    private Map<ShainId, Shain> shainSnapshot;
    private Map<ShainId, Integer> departmentSnapshot;

    public void add(int id, String name) {
        shains.put(ShainId.of(id), new Shain(ShainId.of(id), name, "カナ", 2020, "男", "一般", 0, 0));
    }

    public Integer departmentOf(ShainId id) {
        return departments.get(id);
    }

    @Override
    public List<Shain> findAll() {
        return new ArrayList<>(shains.values());
    }

    @Override
    public Optional<Shain> findById(ShainId id) {
        return Optional.ofNullable(shains.get(id));
    }

    @Override
    public void update(Shain shain) {
        shains.put(shain.getId(), shain);
    }

    @Override
    public void create(Shain shain) {
        shains.put(shain.getId(), shain);
    }

    @Override
    public void deleteById(ShainId id) {
        shains.remove(id);
    }

    @Override
    public ShainId nextId() {
        int max = 0;
        for (ShainId id : shains.keySet()) {
            max = Math.max(max, id.getIntValue());
        }
        return ShainId.of(max + 1);
    }

    @Override
    public void assignDepartment(ShainId id, Integer departmentId) {
        departments.put(id, departmentId);
    }

    @Override
    public void takeSnapshot() {
        shainSnapshot = new HashMap<>(shains);
        departmentSnapshot = new HashMap<>(departments);
    }

    @Override
    public void restoreSnapshot() {
        shains = shainSnapshot;
        departments = departmentSnapshot;
    }
}
