# 구현 로드맵

## M1 API 계약 안정화
- OpenAPI 문서 추가
- 협력사 인증 정책 확정
- REST/WebSocket 계약 테스트 추가

## M2 시장 데이터 어댑터
- KIS 현재가 REST 연동
- KIS 실시간 체결가·호가 WebSocket 연동
- KIS 실시간 시세를 장중 캐시에 적재하고 REST snapshot API로 제공
- 전체 종목/다건 종목 실시간 quote bulk endpoint 추가
- 협력사용 market quote WebSocket topic 추가
- quote stream delta/batch tick 포맷과 backpressure 정책 추가
- 실시간 또는 최신 환율 수집/캐시 adapter 추가
- quote snapshot과 WebSocket tick에 KRW 가격, 현지통화 가격, 적용 환율, 환율 기준시각/출처 포함
- 환율 stale flag와 fallback 정책 추가
- KRX 모든 국내 주식 과거 시세 수집 batch 추가
- 과거 시세 정규화 DB schema 추가
- 과거 시세 chart REST API 추가
- 과거 시세 보정, 결측, 휴장일 처리 정책 추가
- 종목 마스터 DB 적재
- 외국인 보유율 전일 캐시
- 환율 캐시
- 당일 상·하한가 기준가격 적재
- VI 발동 및 단일가 매매 상태 캐시

## M3 주문 지원/매매제한 API
- 외국인 보유율과 한도소진율 응답 계약 확장
- 자체 예측 엔진의 당일 외국인 지분율 boundary 연동
- `viActivationStatus`, `priceLimitStatus`, `orderabilityWarning` 계약 추가
- 현지 MTS 종목 상세/주문 패드용 contract test 추가

## M4 뉴스·공시 알림
- Naver News Search 수집
- OpenDART 공시 수집
- 번역 공급자 어댑터
- Hannah-Montana-AI 분석 API 연동
- 중복 제거와 재전송 방지
- 협력사/종목 topic별 WebSocket 이벤트 replay와 ack 정책

## M5 세무 전산화/환급 연동
- 거주자증명서와 제한세율신청서 처리 상태 계약
- OCR/위변조 검증 결과 수신 계약
- 한국·홍콩 조세조약 케이스 판정 결과 API
- 환급금 선지급/수수료/사후 환수 상태 API
- 분기별 경정청구 배치 상태 조회

## M6 운영 하드닝
- 협력사별 rate limit
- mTLS 또는 서명 기반 인증
- 감사 로그와 장애 추적
- 배포 환경 분리
- 외부 API timeout, retry, circuit breaker
- 금융/세무 감사 로그 보존 정책
