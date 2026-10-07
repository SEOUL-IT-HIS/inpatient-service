-- 활력징후 체온·산소포화도 소수점 저장 (2026-10-07)
-- 체온/SpO2가 정수(NUMBER(10,0)) 컬럼이라 화면에서 36.5를 보내도 36으로 잘려 저장되던 문제
-- 코드(엔티티·DTO)를 int → double 로 바꿨고, ddl-auto=update 는 이미 있는 컬럼의 타입을 바꾸지 못해서 직접 변경해야 함
-- ※ 이미 정수로 잘려 저장된 과거 기록(예: 36.5 → 36)은 원래 값을 알 수 없어 복구되지 않음. 이 쿼리 이후 새로 저장하는 값부터 소수점이 남음

-- [실행 전 확인] 현재 컬럼 타입 — 보통 NUMBER(10,0) 으로 나옴
SELECT table_name, column_name, data_type, data_precision, data_scale
  FROM user_tab_columns
 WHERE table_name IN ('VITAL_SIGN', 'VITAL_SIGN_HISTORY')
   AND column_name IN ('TEMPERATURE', 'SPO2')
 ORDER BY table_name, column_name;

-- 1) 소수 첫째 자리까지 저장되게 변경
--    NUMBER(11,1): 정수 자리(10자리)는 그대로 두고 소수 1자리를 추가한 것
--    (정수 자리나 전체 자릿수를 줄이면 데이터가 있는 컬럼은 ORA-01440 으로 거절됨)
ALTER TABLE vital_sign         MODIFY (temperature NUMBER(11,1), spo2 NUMBER(11,1));
ALTER TABLE vital_sign_history MODIFY (temperature NUMBER(11,1), spo2 NUMBER(11,1));

-- [실행 후 확인] 위 확인 쿼리를 다시 실행해서 DATA_SCALE 이 1 이 됐는지 확인
SELECT table_name, column_name, data_type, data_precision, data_scale
  FROM user_tab_columns
 WHERE table_name IN ('VITAL_SIGN', 'VITAL_SIGN_HISTORY')
   AND column_name IN ('TEMPERATURE', 'SPO2')
 ORDER BY table_name, column_name;
