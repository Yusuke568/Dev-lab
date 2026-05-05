package com.example.shain.application.port.in;

import com.example.shain.domain.model.Shain;

/**
 * 社員登録ユースケース。
 */
public interface RegisterShainUseCase {
    void registerShain(RegisterShainCommand command);

    class RegisterShainCommand {
        private final String name;
        private final String namekana;
        private final int entryYear;
        private final String gender;
        private final String jobClass;

        public RegisterShainCommand(String name, String namekana, int entryYear, String gender, String jobClass) {
            this.name = name;
            this.namekana = namekana;
            this.entryYear = entryYear;
            this.gender = gender;
            this.jobClass = jobClass;
        }

        public String getName() { return name; }
        public String getNamekana() { return namekana; }
        public int getEntryYear() { return entryYear; }
        public String getGender() { return gender; }
        public String getJobClass() { return jobClass; }
    }
}
