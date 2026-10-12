package usecase.shain;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import domain.shain.Classmaster;
import infrastructure.persistence.ClassmasterDao;
import infrastructure.db.ConnectionBase;

public class ClassmasterService {

    private final ClassmasterDao classmasterDao;

    public ClassmasterService() {
        this(new ClassmasterDao());
    }

    // テスト用のコンストラクタ
    public ClassmasterService(ClassmasterDao classmasterDao) {
        this.classmasterDao = classmasterDao;
    }

    /**
     * すべての役職リストを取得します。
     * @return 役職リスト
     * @throws SQLException
     * @throws NamingException
     * @throws IOException
     */
    public List<Classmaster> getAllClassmaster() throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            return classmasterDao.findAll(con);
        }
    }
}

