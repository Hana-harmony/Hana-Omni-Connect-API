# 테스트

## 로컬 검증
```bash
docker compose -f compose.local.yml up -d
./gradlew test --no-daemon
./gradlew bootJar --no-daemon
```

## 현재 테스트 범위
- API key 인증 성공
- API key 누락 시 `401`
- API key 해시 미설정 시 `503`
- health endpoint 공개
- 시장 데이터 응답 계약

## 추가 예정
- WebSocket subscription 계약 테스트
- 외부 API 어댑터 contract test
- 입력 validation 실패 케이스
- rate limit 정책 테스트
- 장애 상황 fallback 테스트
- KIS 실시간 체결가·호가 패킷 파싱 테스트
- KRX 외국인 보유율 캐시 갱신 테스트
- 환율 캐시 갱신과 stale flag 테스트
- KRW/현지통화 가격 환산 contract test
- VI 발동과 상·하한가 상태 계산 테스트
- 뉴스·공시 분석/번역 payload contract test
- 세무 환급 상태 API contract test
