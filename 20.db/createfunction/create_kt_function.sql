DROP FUNCTION IF EXISTS ktfnc_leave_grant_days;
CREATE FUNCTION ktfnc_leave_grant_days(p_years_of_service INT)
RETURNS INT
DETERMINISTIC
RETURN CASE
    WHEN p_years_of_service = 0 THEN 10
    WHEN p_years_of_service = 1 THEN 11
    WHEN p_years_of_service = 2 THEN 12
    WHEN p_years_of_service = 3 THEN 14
    WHEN p_years_of_service = 4 THEN 16
    WHEN p_years_of_service = 5 THEN 18
    WHEN p_years_of_service >= 6 THEN 20
    ELSE 0
END;
