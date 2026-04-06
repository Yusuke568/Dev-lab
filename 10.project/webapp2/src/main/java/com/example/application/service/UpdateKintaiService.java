package com.example.application.service;

import com.example.adapter.in.web.dto.KintaiRecordDto;
import com.example.application.port.in.UpdateKintaiUseCase;
import com.example.application.port.out.KintaiUpdatePort;
import com.example.application.port.out.PaidLeavePort;
import com.example.application.port.out.TransactionManager;
import com.example.application.port.out.WorkTypePort;
import com.example.application.port.out.DailyWorkRecord;
import com.example.entity.WorkType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠情報を更新するユースケースの実装。
 */
public class UpdateKintaiService implements UpdateKintaiUseCase {

    private final KintaiUpdatePort kintaiUpdatePort;
    private final PaidLeavePort paidLeavePort;
    private final TransactionManager transactionManager;
    private final WorkTypePort workTypePort;

    public UpdateKintaiService(KintaiUpdatePort kintaiUpdatePort, PaidLeavePort paidLeavePort, TransactionManager transactionManager, WorkTypePort workTypePort) {
        this.kintaiUpdatePort = kintaiUpdatePort;
        this.paidLeavePort = paidLeavePort;
        this.transactionManager = transactionManager;
        this.workTypePort = workTypePort;
    }

    @Override
    public void updateKintai(List<KintaiRecordDto> records) {
        if (records == null || records.isEmpty()) return;

        transactionManager.executeInTransaction(() -> {
            List<WorkType> workTypes = workTypePort.findAll();
            Optional<WorkType> paidLeaveWorkType = workTypes.stream()
                .filter(wt -> "有給".equals(wt.getName()) || wt.isPaid())
                .findFirst();
                
            int paidLeaveId = paidLeaveWorkType.map(WorkType::getId).orElse(-1);

            for (KintaiRecordDto dto : records) {
                LocalDate date = LocalDate.parse(dto.getKintaidate(), DateTimeFormatter.ISO_LOCAL_DATE);
                DailyWorkRecord oldBean = kintaiUpdatePort.findByDate(dto.getId(), date);
                
                int oldStatusId = (oldBean != null) ? oldBean.getAbstractId() : -1;
                int newStatusId = (dto.getAbstractId() != null) ? dto.getAbstractId() : -1;

                if (paidLeaveId != -1) {
                    if (oldStatusId != paidLeaveId && newStatusId == paidLeaveId) {
                        paidLeavePort.decrementDays(dto.getId());
                    } else if (oldStatusId == paidLeaveId && newStatusId != paidLeaveId) {
                        paidLeavePort.incrementDays(dto.getId());
                    }
                }

                DailyWorkRecord newBean = new DailyWorkRecord();
                newBean.setId(dto.getId());
                newBean.setKintaidate(date);
                newBean.setWeek(dto.getWeek());
                
                if (dto.getKintaifrom() != null && !dto.getKintaifrom().isEmpty()) {
                    newBean.setKintaifrom(Timestamp.valueOf(date.toString() + " " + dto.getKintaifrom() + ":00"));
                }
                if (dto.getKintaito() != null && !dto.getKintaito().isEmpty()) {
                    newBean.setKintaito(Timestamp.valueOf(date.toString() + " " + dto.getKintaito() + ":00"));
                }
                
                newBean.setJikangai(dto.getJikangai());
                newBean.setAbstractId(newStatusId);
                newBean.setMemo(dto.getMemo());
                newBean.setCorrectionId(dto.getCorrectionId());
                newBean.setCorrectionUsTime(dto.getCorrectionUsTime());
                newBean.setCorrectionMidTime(dto.getCorrectionMidTime());
                newBean.setIndirectTime(dto.getIndirectTime());
                newBean.setTotalWorkTime(dto.getTotalWorkTime());
                newBean.setTotalDirectWorkTime(dto.getTotalDirectWorkTime());

                kintaiUpdatePort.update(newBean);
            }
        });
    }
}
