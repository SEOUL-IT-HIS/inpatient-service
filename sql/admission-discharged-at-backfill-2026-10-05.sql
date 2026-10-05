-- 퇴원일(discharged_at) 채우기 (2026-10-05)
-- 입원 건에 퇴원 확정 시각 컬럼이 새로 생겨서, 이미 퇴원한 건은 값이 비어 있음
-- 퇴원 확정 = 병상 퇴상이 같은 시점(퇴원 확정 트랜잭션에서 함께 처리)이므로, 병상배정의 퇴상 시각으로 채움
-- 다시 실행해도 안전: 이미 값이 있는 건은 건드리지 않음

-- 1) 컬럼이 없으면 추가 (inpatient-service를 한 번 재시작하면 ddl-auto=update로 자동 추가되므로 보통은 이미 있음)
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_tab_columns WHERE table_name = 'ADMISSION' AND column_name = 'DISCHARGED_AT';
    IF n = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE admission ADD (discharged_at TIMESTAMP(6))';
    END IF;
END;
/

-- [실행 전 확인] 퇴원했는데 퇴원일이 비어 있는 건
SELECT admission_id, status, discharged_at FROM admission WHERE status = 'DISCHARGED' AND discharged_at IS NULL;

-- 2) 퇴원한 건의 퇴원일 = 그 입원 건 병상배정의 마지막 퇴상 시각
UPDATE admission a
   SET discharged_at = (SELECT MAX(b.released_at) FROM bed_assignment b WHERE b.admission_id = a.admission_id)
 WHERE a.status = 'DISCHARGED'
   AND a.discharged_at IS NULL
   AND EXISTS (SELECT 1 FROM bed_assignment b WHERE b.admission_id = a.admission_id AND b.released_at IS NOT NULL);

-- [실행 후 확인] 퇴원 건의 퇴원일 (병상배정 없이 퇴원한 건은 비어 있을 수 있음 → 화면에서는 "전체 이력"에만 표시)
SELECT admission_id, admission_date, discharged_at FROM admission WHERE status = 'DISCHARGED' ORDER BY discharged_at;

COMMIT;
