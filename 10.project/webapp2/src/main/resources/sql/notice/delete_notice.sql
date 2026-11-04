-- 自分が出したお知らせだけを消せる
DELETE FROM notice WHERE id = ? AND author_staff_id = ?;
