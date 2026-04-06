package com.example.application.port.out;

/**
 * 有給休暇を操作するためのポート。
 */
public interface PaidLeavePort {
    void incrementDays(int staffId);
    void decrementDays(int staffId);
}
