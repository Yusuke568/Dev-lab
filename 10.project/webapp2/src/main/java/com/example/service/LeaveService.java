package com.example.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.example.entity.LeaveGrantRule;
import com.example.entity.Shain;
import com.example.service.transaction.TransactionManager;

import com.example.dao.LeaveDao;

public class LeaveService {

    private final LeaveDao leaveDao;
    private final ShainService shainService;
    private final TransactionManager transactionManager;

    public LeaveService() {
        this(new LeaveDao(), new ShainService(), new TransactionManager());
    }

    public LeaveService(LeaveDao leaveDao, ShainService shainService, TransactionManager transactionManager) {
        this.leaveDao = leaveDao;
        this.shainService = shainService;
        this.transactionManager = transactionManager;
    }

    /**
     * 勤続年数に応じて全社員の有給を一括付与します。
     */
    public void grantLeaveByYearsOfService() throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            List<LeaveGrantRule> rules = leaveDao.findAllRules(con);
            List<Shain> employees = shainService.getAllShainWithLeaveInfo(con);

            for (Shain employee : employees) {
                int years = employee.getYearsOfService();
                int grantDays = 0;
                for (LeaveGrantRule rule : rules) {
                    if (years >= rule.getYearsOfService()) {
                        grantDays = rule.getGrantDays();
                    } else {
                        break;
                    }
                }
                if (grantDays > 0) {
                    employee.setPaidLeaveDays(grantDays);
                }
            }
            leaveDao.batchUpdateDays(con, employees);
            return null;
        });
    }

    /**
     * 選択した複数の社員に有給を一括で加算します。
     */
    public void addDaysToSelected(String[] ids, int days) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            leaveDao.addDaysToSelected(con, ids, days);
            return null;
        });
    }

    /**
     * 指定した社員の有給日数を更新します。
     */
    public void updateDays(int id, int days) throws SQLException, NamingException, IOException {
        transactionManager.execute(con -> {
            leaveDao.updateDays(con, id, days);
            return null;
        });
    }

    /**
     * 指定した社員の有給休暇を1日加算します。
     */
    public void incrementDays(Connection con, int staffId) throws SQLException, IOException {
        leaveDao.incrementDays(con, staffId);
    }

    /**
     * 指定した社員の有給休暇を1日減算します。
     */
    public void decrementDays(Connection con, int staffId) throws SQLException, IOException {
        leaveDao.decrementDays(con, staffId);
    }
}
