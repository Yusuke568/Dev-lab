package com.example.shain.application.port.in;

import com.example.shain.domain.model.Shain;

/**
 * 社員更新ユースケース。
 */
public interface UpdateShainUseCase {
    void updateShain(UpdateShainCommand command);

    class UpdateShainCommand {
        private final String id;
        private final String name;
        private final String namekana;
        private final int entryYear;
        private final String gender;
        private final String jobClass;

        public UpdateShainCommand(String id, String name, String namekana, int entryYear, String gender, String jobClass) {
            this.id = id;
            this.name = name;
            this.namekana = namekana;
            this.entryYear = entryYear;
            this.gender = gender;
            this.jobClass = jobClass;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getNamekana() { return namekana; }
        public int getEntryYear() { return entryYear; }
        public String getGender() { return gender; }
        public String getJobClass() { return jobClass; }
    }
}
