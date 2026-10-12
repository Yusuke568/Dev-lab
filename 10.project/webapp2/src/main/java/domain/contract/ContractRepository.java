package domain.contract;

import java.util.Optional;

/**
 * 契約・アサインリポジトリインターフェース（Output Port）
 */
public interface ContractRepository {
    Optional<ProjectContract> findContractById(String contractId);
    Optional<Assignment> findActiveAssignment(String employeeId);
}
