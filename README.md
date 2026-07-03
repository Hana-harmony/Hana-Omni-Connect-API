# Hana OmniLens API

해외 협력사 거래소·브로커에 한국 주식 데이터, 매매제한 신호, 뉴스·공시 인텔리전스, 세무 환급 상태를 제공하는 B2B API 서버다.

## 핵심 기능
- 시장 데이터: KIS/KRX 기반 종목, quote, 지수, 호가, 과거 시세, FX 메타데이터
- 실시간 quote: 인기 종목 기본 구독, 상세 진입 종목 수요 구독, `/ws/market/quotes` 송신
- 주문 참고 신호: 외국인 한도, 예측 한도소진율 boundary, VI, 단일가, 상·하한가, 거래정지
- 뉴스·공시: 시장 뉴스, 종목 뉴스, OpenDART 공시, 전문/이미지/원문 링크 저장
- AI orchestration: Hannah-Montana-AI 분석 결과와 DeepL 번역 결과를 REST/WebSocket payload로 제공
- 금융 용어: 한국 금융 용어 해설, evidence, confidence, cache 상태
- 세무: refund case 분류, 상태 sync, 분기별 경정청구 배치 상태
- 협력사 보안: partner API key 해시 저장, OpenAPI 보호, 서버 간 인증

## 실행
```bash
docker compose -f compose.local.yml up --build
curl http://localhost:8080/actuator/health
```

개발 실행:
```bash
./gradlew test
./gradlew bootRun
```

로컬 secret은 `src/main/resources/application-local.yml`에만 둔다. 운영 민감값은 GitHub Secrets가 만든 서버 env 파일로 주입한다.

## 주요 API
- Market: `/api/v1/market/stocks/**`, `/api/v1/market/quotes`, `/api/v1/market/news`
- Alerts: `/api/v1/alerts/**`, `/ws/alerts`
- Terms: `/api/v1/korean-financial-terms/**`
- Tax: `/api/v1/tax/**`
- Partner credentials: `/api/v1/partners/**`
- Spec: `/openapi.yaml`, `/v3/api-docs`, `/swagger-ui/index.html`

## 책임 경계
- 실제 주문 실행, 체결, 정산, 환전, 최종투자자 계정 관리는 협력사 또는 별도 원장 책임이다.
- 모델 학습과 추론 로직은 Hannah-Montana-AI 책임이다.
- 이 레포는 provider 수집, 데이터 정규화, 협력사 API 계약, stream 발행을 담당한다.

## 검증
```bash
./gradlew test --no-daemon
./gradlew bootJar --no-daemon
```

## 문서
- [통합 기능정의서](docs/FEATURE_DEFINITION.md)
- [아키텍처](docs/ARCHITECTURE.md)
- [API 표준](docs/API_STANDARD.md)
- [운영](docs/OPERATIONS.md)
- [배포](docs/DEPLOYMENT.md)
- [보안](docs/SECURITY.md)
- [테스트](docs/TESTING.md)
