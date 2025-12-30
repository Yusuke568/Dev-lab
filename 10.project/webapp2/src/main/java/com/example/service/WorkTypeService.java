package com.example.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import com.example.entity.WorkType;
import com.example.infra.ConnectionBase;
import dao.AbstractDao;

public class WorkTypeService {

    private AbstractDao workTypeDao;

    public WorkTypeService() {
        this(new AbstractDao());
    }

    public WorkTypeService(AbstractDao abstractDao) {
        this.workTypeDao = abstractDao;
    }

    /**
     * 適用マスタを全件取得します。
     * @return 適用マスタ(WorkType)のリスト
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     */
    public List<WorkType> getWorkTypeList() throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return workTypeDao.findAll(con);
        }
    }
}
