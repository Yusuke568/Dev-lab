package com.example.shain.application.port.in;

import com.example.auth.domain.model.InitialPassword;
import com.example.shain.domain.model.ShainId;

/**
 * 社員登録ユースケース。
 */
public interface RegisterShainUseCase {

    /**
     * 社員を登録し、同じトランザクションでログイン用のアカウント（ユーザー名は社員ID）を作ります。
     * どちらかが失敗したら両方取り消します。
     *
     * @throws com.example.auth.domain.model.PasswordValidationException 初期パスワードの入力内容に問題があるとき、
     *         または同じユーザー名のアカウントがすでにあるとき（TransactionManager の例外に包まれることがある）
     */
    RegisterShainResult registerShain(RegisterShainCommand command);

    class RegisterShainCommand {
        private final String name;
        private final String namekana;
        private final int entryYear;
        private final String gender;
        private final String jobClass;
        private final Integer departmentId;
        private final InitialPassword initialPassword;

        public RegisterShainCommand(String name, String namekana, int entryYear, String gender, String jobClass,
                                    Integer departmentId, InitialPassword initialPassword) {
            this.name = name;
            this.namekana = namekana;
            this.entryYear = entryYear;
            this.gender = gender;
            this.jobClass = jobClass;
            this.departmentId = departmentId;
            this.initialPassword = initialPassword;
        }

        public String getName() { return name; }
        public String getNamekana() { return namekana; }
        public int getEntryYear() { return entryYear; }
        public String getGender() { return gender; }
        public String getJobClass() { return jobClass; }
        public Integer getDepartmentId() { return departmentId; }
        public InitialPassword getInitialPassword() { return initialPassword; }
    }

    class RegisterShainResult {
        private final ShainId id;
        private final String temporaryPassword;

        public RegisterShainResult(ShainId id, String temporaryPassword) {
            this.id = id;
            this.temporaryPassword = temporaryPassword;
        }

        public ShainId getId() { return id; }

        /** 自動で作った仮パスワード。管理者が指定した場合は null */
        public String getTemporaryPassword() { return temporaryPassword; }
    }
}
