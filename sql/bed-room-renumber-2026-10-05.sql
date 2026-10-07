-- 병실 호수 재정리 (2026-10-05) — 병동 = 층
-- 기존: 모든 병실이 101~125호(1층)이고 같은 병동의 호수가 흩어져 있었음
-- 변경: 내과 2층 / 외과 3층 / 정형외과 4층 / 산부인과 5층 / 소아과 6층
--       층마다 x01 특실, x02·x03 1인실, x04·x05 다인실(4인실)
-- 병상 ID(BED-호수+병상)도 함께 바꾸고, 그 ID를 참조하는 배정 이력·예약도 같은 트랜잭션에서 바꿈
-- 사용중/예약 병상의 환자와 상태는 그대로 (ID와 호수만 바뀜)
-- 다시 실행해도 안전: 1xx호가 남아 있지 않으면 아무것도 바뀌지 않음
--
-- 호수 대응표
--   01 내과 → 2층: 111→201(특실), 101→202(1인실), 112→203(1인실), 102→204(다인실), 113→205(다인실)
--   02 외과 → 3층: 114→301(특실), 104→302(1인실), 115→303(1인실), 103→304(다인실), 116→305(다인실)
--   03 정형외과 → 4층: 117→401(특실), 118→402(1인실), 119→403(1인실), 105→404(다인실), 106→405(다인실)
--   04 산부인과 → 5층: 120→501(특실), 121→502(1인실), 122→503(1인실), 107→504(다인실), 108→505(다인실)
--   05 소아과 → 6층: 123→601(특실), 124→602(1인실), 125→603(1인실), 109→604(다인실), 110→605(다인실)

-- [실행 전 확인] 병상 55 / 사용중·예약 병상 목록을 기록해 두기
SELECT COUNT(*) AS beds FROM bed;
SELECT bed_id, bed_status, patient_id FROM bed WHERE bed_status <> 'EMPTY' ORDER BY bed_id;

-- 1) 병상 ID를 참조하는 데이터 먼저 변경 (BED-105A → BED-404A)
UPDATE bed_assignment SET bed_id = 'BED-' || CASE SUBSTR(bed_id, 5, 3) WHEN '111' THEN '201' WHEN '101' THEN '202' WHEN '112' THEN '203' WHEN '102' THEN '204' WHEN '113' THEN '205' WHEN '114' THEN '301' WHEN '104' THEN '302' WHEN '115' THEN '303' WHEN '103' THEN '304' WHEN '116' THEN '305' WHEN '117' THEN '401' WHEN '118' THEN '402' WHEN '119' THEN '403' WHEN '105' THEN '404' WHEN '106' THEN '405' WHEN '120' THEN '501' WHEN '121' THEN '502' WHEN '122' THEN '503' WHEN '107' THEN '504' WHEN '108' THEN '505' WHEN '123' THEN '601' WHEN '124' THEN '602' WHEN '125' THEN '603' WHEN '109' THEN '604' WHEN '110' THEN '605' END || SUBSTR(bed_id, 8) WHERE SUBSTR(bed_id, 5, 3) IN ('111', '101', '112', '102', '113', '114', '104', '115', '103', '116', '117', '118', '119', '105', '106', '120', '121', '122', '107', '108', '123', '124', '125', '109', '110');
UPDATE bed_reservation SET bed_id = 'BED-' || CASE SUBSTR(bed_id, 5, 3) WHEN '111' THEN '201' WHEN '101' THEN '202' WHEN '112' THEN '203' WHEN '102' THEN '204' WHEN '113' THEN '205' WHEN '114' THEN '301' WHEN '104' THEN '302' WHEN '115' THEN '303' WHEN '103' THEN '304' WHEN '116' THEN '305' WHEN '117' THEN '401' WHEN '118' THEN '402' WHEN '119' THEN '403' WHEN '105' THEN '404' WHEN '106' THEN '405' WHEN '120' THEN '501' WHEN '121' THEN '502' WHEN '122' THEN '503' WHEN '107' THEN '504' WHEN '108' THEN '505' WHEN '123' THEN '601' WHEN '124' THEN '602' WHEN '125' THEN '603' WHEN '109' THEN '604' WHEN '110' THEN '605' END || SUBSTR(bed_id, 8) WHERE SUBSTR(bed_id, 5, 3) IN ('111', '101', '112', '102', '113', '114', '104', '115', '103', '116', '117', '118', '119', '105', '106', '120', '121', '122', '107', '108', '123', '124', '125', '109', '110');
UPDATE isolation_management SET bed_id = 'BED-' || CASE SUBSTR(bed_id, 5, 3) WHEN '111' THEN '201' WHEN '101' THEN '202' WHEN '112' THEN '203' WHEN '102' THEN '204' WHEN '113' THEN '205' WHEN '114' THEN '301' WHEN '104' THEN '302' WHEN '115' THEN '303' WHEN '103' THEN '304' WHEN '116' THEN '305' WHEN '117' THEN '401' WHEN '118' THEN '402' WHEN '119' THEN '403' WHEN '105' THEN '404' WHEN '106' THEN '405' WHEN '120' THEN '501' WHEN '121' THEN '502' WHEN '122' THEN '503' WHEN '107' THEN '504' WHEN '108' THEN '505' WHEN '123' THEN '601' WHEN '124' THEN '602' WHEN '125' THEN '603' WHEN '109' THEN '604' WHEN '110' THEN '605' END || SUBSTR(bed_id, 8) WHERE SUBSTR(bed_id, 5, 3) IN ('111', '101', '112', '102', '113', '114', '104', '115', '103', '116', '117', '118', '119', '105', '106', '120', '121', '122', '107', '108', '123', '124', '125', '109', '110');

-- 2) 병상의 호수와 ID 변경
UPDATE bed SET room_no = CASE room_no WHEN '111' THEN '201' WHEN '101' THEN '202' WHEN '112' THEN '203' WHEN '102' THEN '204' WHEN '113' THEN '205' WHEN '114' THEN '301' WHEN '104' THEN '302' WHEN '115' THEN '303' WHEN '103' THEN '304' WHEN '116' THEN '305' WHEN '117' THEN '401' WHEN '118' THEN '402' WHEN '119' THEN '403' WHEN '105' THEN '404' WHEN '106' THEN '405' WHEN '120' THEN '501' WHEN '121' THEN '502' WHEN '122' THEN '503' WHEN '107' THEN '504' WHEN '108' THEN '505' WHEN '123' THEN '601' WHEN '124' THEN '602' WHEN '125' THEN '603' WHEN '109' THEN '604' WHEN '110' THEN '605' END, bed_id = 'BED-' || CASE room_no WHEN '111' THEN '201' WHEN '101' THEN '202' WHEN '112' THEN '203' WHEN '102' THEN '204' WHEN '113' THEN '205' WHEN '114' THEN '301' WHEN '104' THEN '302' WHEN '115' THEN '303' WHEN '103' THEN '304' WHEN '116' THEN '305' WHEN '117' THEN '401' WHEN '118' THEN '402' WHEN '119' THEN '403' WHEN '105' THEN '404' WHEN '106' THEN '405' WHEN '120' THEN '501' WHEN '121' THEN '502' WHEN '122' THEN '503' WHEN '107' THEN '504' WHEN '108' THEN '505' WHEN '123' THEN '601' WHEN '124' THEN '602' WHEN '125' THEN '603' WHEN '109' THEN '604' WHEN '110' THEN '605' END || bed_no, updated_at = SYSTIMESTAMP WHERE room_no IN ('111', '101', '112', '102', '113', '114', '104', '115', '103', '116', '117', '118', '119', '105', '106', '120', '121', '122', '107', '108', '123', '124', '125', '109', '110');

-- [실행 후 확인] 아래가 모두 기대값이면 COMMIT, 하나라도 다르면 ROLLBACK
--   병상 수 55
SELECT COUNT(*) AS beds FROM bed;
--   1xx호가 남아 있지 않음 → 0
SELECT COUNT(*) AS old_rooms FROM bed WHERE room_no LIKE '1__';
--   ID와 호수가 맞지 않는 병상 → 0
SELECT COUNT(*) AS mismatched FROM bed WHERE bed_id <> 'BED-' || room_no || bed_no;
--   배정·예약이 없는 병상을 가리키는 경우 → 0
SELECT COUNT(*) AS orphan_assignments FROM bed_assignment a WHERE NOT EXISTS (SELECT 1 FROM bed b WHERE b.bed_id = a.bed_id);
SELECT COUNT(*) AS orphan_reservations FROM bed_reservation r WHERE NOT EXISTS (SELECT 1 FROM bed b WHERE b.bed_id = r.bed_id);
--   사용중·예약 병상: 실행 전 목록과 환자·상태가 같은지 (ID만 바뀜)
SELECT bed_id, room_no, bed_status, patient_id FROM bed WHERE bed_status <> 'EMPTY' ORDER BY bed_id;

COMMIT;
