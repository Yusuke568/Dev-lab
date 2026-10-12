package domain.auth;

import java.util.List;
import java.util.Optional;

/**
 * 認証・スタッフリポジトリインターフェース（Output Port）
 */
public interface AuthRepository {
    Optional<UserSession> authenticate(String username, String password);
    List<UserSession> findAllStaff();
}
