package com.example.shain.application.port.in;

import com.example.shain.domain.model.ShainId;

import java.util.Optional;

/**
 * 社員更新ユースケース。
 */
public interface UpdateShainUseCase {

    /**
     * 社員情報を更新する。新しい社員IDが指定されていれば、IDも変更する。
     *
     * @return 更新後の社員ID
     * @throws com.example.shain.domain.model.InvalidShainIdException 新しいIDが不正、またはすでに使われているとき
     */
    ShainId updateShain(UpdateShainCommand command);

    /**
     * 社員 currentId のIDを newId に変えられるかを調べる（画面での入力中チェック用）。
     *
     * @return 変えられないときはその理由。変えられる（または変わらない）ときは空
     */
    Optional<String> checkNewId(String currentId, String newId);

    class UpdateShainCommand {
        private final String id;
        /** 変更後の社員ID。null か id と同じなら変更しない */
        private final String newId;
        private final String name;
        private final String namekana;
        private final int entryYear;
        private final String gender;
        private final String jobClass;

        public UpdateShainCommand(String id, String name, String namekana, int entryYear, String gender, String jobClass) {
            this(id, null, name, namekana, entryYear, gender, jobClass);
        }

        public UpdateShainCommand(String id, String newId, String name, String namekana, int entryYear, String gender,
                                  String jobClass) {
            this.id = id;
            this.newId = newId;
            this.name = name;
            this.namekana = namekana;
            this.entryYear = entryYear;
            this.gender = gender;
            this.jobClass = jobClass;
        }

        public String getId() { return id; }
        public String getNewId() { return newId; }
        public String getName() { return name; }
        public String getNamekana() { return namekana; }
        public int getEntryYear() { return entryYear; }
        public String getGender() { return gender; }
        public String getJobClass() { return jobClass; }
    }
}
