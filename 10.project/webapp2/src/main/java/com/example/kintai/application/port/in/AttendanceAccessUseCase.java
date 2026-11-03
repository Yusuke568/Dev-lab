package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.Subordinate;

import java.util.List;
import java.util.Set;

/**
 * 誰の勤怠表を参照・編集できるかを決めるユースケース。
 * 本人の勤怠表に加えて、承認権限（APPROVAL）を持つ人は組織図上の配下の社員の勤怠表を扱える。
 */
public interface AttendanceAccessUseCase {

    /**
     * @param loginUser ログインしている社員ID
     * @param features  ログインしている人が使える機能（Feature の名前）
     * @param targetStaffId 参照・編集したい勤怠表の社員ID
     */
    boolean canAccess(String loginUser, Set<String> features, String targetStaffId);

    /** 勤怠表を参照・編集できる配下の社員。承認権限がなければ空。 */
    List<Subordinate> getSubordinates(String loginUser, Set<String> features);
}
