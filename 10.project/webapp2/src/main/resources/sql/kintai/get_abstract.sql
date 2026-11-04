-- 勤務区分（適用マスタ）。有給消費日数（paid_leave_days）はマイグレーション前にないこともあるので * で読む
select *
from abstract_master
order by id;
