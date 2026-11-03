package domain.contract;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 案件アサインエンティティ
 */
public class Assignment {
    private final String id;
    private final String employeeId;
    private final String projectContractId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double allocationRatio; // アサイン比率（例: 1.0 または 0.6）

    public Assignment(String id, String employeeId, String projectContractId,
                      LocalDate startDate, LocalDate endDate, double allocationRatio) {
        if (allocationRatio <= 0.0 || allocationRatio > 1.0) {
            throw new IllegalArgumentException("アサイン比率は 0.0 超 1.0 以下である必要があります。");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日は終了日以前である必要があります。");
        }
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.employeeId = Objects.requireNonNull(employeeId, "社員IDは必須です。");
        this.projectContractId = Objects.requireNonNull(projectContractId, "案件契約IDは必須です。");
        this.startDate = Objects.requireNonNull(startDate, "開始日は必須です。");
        this.endDate = endDate;
        this.allocationRatio = allocationRatio;
    }

    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getProjectContractId() { return projectContractId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getAllocationRatio() { return allocationRatio; }
}
