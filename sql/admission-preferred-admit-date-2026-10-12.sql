-- 외래 입원요청 연동: 입원 희망일(preferred_admit_date) 컬럼 추가 (2026-10-12)
-- 외래가 입원요청에 담아 보내는 선택 값 (입원일 admission_date와는 별개). 외래 건이 아니면 계속 NULL
-- 다시 실행해도 안전: 이미 있으면 건너뜀

-- 컬럼이 없으면 추가 (inpatient-service를 한 번 재시작하면 ddl-auto=update로 자동 추가되므로 보통은 이미 있음)
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_tab_columns WHERE table_name = 'ADMISSION' AND column_name = 'PREFERRED_ADMIT_DATE';
    IF n = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE admission ADD (preferred_admit_date DATE)';
    END IF;
END;
/

-- [확인] 외래 요청으로 들어온 입원 건 (경로 Outpatient, 외래 requestId는 admission_request_id에 저장)
SELECT admission_id, patient_id, status, admission_request_id, preferred_admit_date
FROM admission
WHERE admission_route = 'Outpatient';
