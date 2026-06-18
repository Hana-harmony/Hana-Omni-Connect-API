# 아키텍처

## 목적
- 해외 협력사 거래소가 한국 주식 시장 데이터, 매매제한 판단 데이터, 뉴스·공시 인텔리전스, 세무 환급 상태를 연동하는 B2B API를 제공한다.
- 실제 주문 실행, 체결, 정산, 환전, 최종투자자 계정 관리는 이 레포 범위에서 제외한다.
- 단, 주문 화면과 모의 주문 판단에 필요한 외국인 투자 한도, VI, 상·하한가, 당일 지분율 예측 boundary 데이터는 이 레포의 API 계약에 포함한다.

## 서비스 구성
- `market`: 한국 주식 현재가, 호가, 종목 검색, 현지 통화 환산, 과거 시세 API
- `market-history`: KRX 국내 주식 과거 시세 수집, 정규화, DB 저장, 차트 조회 API
- `fx`: 실시간 또는 최신 환율 수집, 캐시, 현지통화 환산 계산
- `orderability`: 외국인 보유율, 당일 예측 지분율 boundary, VI 발동, 상·하한가 상태 API
- `alert`: 뉴스·공시 분석·번역 결과를 협력사와 종목 topic으로 송신하는 API
- `tax`: 최종투자자별 세무 서류 상태, 과세 케이스, 환급금 선지급 상태 API 계약
- `config`: API key 검증, CORS, WebSocket 설정

## API 경계
- REST: `/api/v1/market/**`, `/api/v1/alerts/**`
- Planned REST: `/api/v1/orderability/**`, `/api/v1/tax/**`
- WebSocket: `/ws/alerts`
- 협력사 topic: `/topic/partners/{partnerId}/alerts`
- 종목 topic: `/topic/stocks/{stockCode}/alerts`

## 시장 데이터 제공 방식
- KIS 실시간 체결가·호가 WebSocket은 Hana-OmniLens-API가 원천 구독하고 장중 메모리/Redis 캐시에 반영한다.
- 한국수출입은행 또는 승인된 FX provider의 최신 환율은 Hana-OmniLens-API가 수집하고 장중 캐시에 반영한다.
- 협력사 백엔드는 KIS를 직접 호출하지 않고 Hana-OmniLens-API의 REST snapshot API와 planned market WebSocket stream을 사용한다.
- 단건 상세는 `/api/v1/market/stocks/{stockCode}/quote`, 다건/전체 snapshot은 planned endpoint인 `/api/v1/market/stocks/quotes`에서 제공한다.
- 장중 가격 변동은 planned WebSocket topic인 `/topic/market/stocks/{stockCode}/quotes`, `/topic/market/indices/{market}/quotes`, `/topic/market/all/quotes`로 delta 또는 batch tick을 송신한다.
- 모든 quote snapshot과 quote stream tick은 원화 가격과 현지통화 환산 가격을 함께 포함한다.
- 현지통화 환산 필드는 `currentPriceKrw`, `executionPriceKrw`, `baseCurrency`, `localCurrency`, `fxRate`, `fxRateTime`, `fxRateSource`, `localCurrencyPrice`, `localCurrencyExecutionPrice`를 표준으로 한다.
- 환율이 지연되거나 공급자 장애가 있으면 마지막 정상 환율의 기준시각과 stale flag를 함께 내려 현지 거래소가 화면에 지연 상태를 표시할 수 있게 한다.

## 과거 시세 제공 방식
- 모든 국내 주식 과거 시세는 Hana-OmniLens-API가 KRX 데이터를 수집한다.
- Hana-OmniLens-API는 KRX 원천 데이터를 종목코드, 거래일, 시가, 고가, 저가, 종가, 거래량, 거래대금, 수정주가 기준으로 정규화해 자체 DB에 저장한다.
- 과거 시세 DB는 일봉을 기본으로 하고, KRX 또는 별도 허가 데이터로 분봉/틱 데이터가 확보되는 경우 별도 table과 endpoint로 분리한다.
- planned endpoint는 `/api/v1/market/stocks/{stockCode}/history?from=YYYY-MM-DD&to=YYYY-MM-DD&interval=1d` 형식으로 제공한다.
- Stock-exchange-BE는 KRX를 직접 호출하지 않고 Hana-OmniLens-API의 과거 시세 REST API를 호출해 FE 차트 API로 재가공한다.
- 장중 현재가/호가/VI/상·하한가 상태는 KIS 실시간 캐시 기반 snapshot/stream을 표준으로 하고, 과거 차트는 KRX 기반 DB 조회를 표준으로 한다.

## 외부 시스템
- KIS Open API: 현재가, 실시간 체결가, 실시간 호가
- KIS 종목정보 파일: 종목코드, 국문명, 영문명, 시장구분, 발행주식수, 당일 상·하한가 기준가격
- KRX: 모든 국내 주식 과거 시세, 전일 외국인 보유율과 한도소진율
- 한국수출입은행 또는 승인된 FX provider: 실시간 또는 최신 환율
- Naver News Search: 뉴스 제목, snippet, 원문 링크
- OpenDART: 공시 제목, 유형, 제출시각, 원문 링크
- Papago/DeepL: 금융 도메인 전문 용어 번역
- Hannah-Montana-AI: 뉴스·공시 종목 매핑, 이벤트, 감성, 중요도 분석
- Tax/OCR pipeline: 거주자증명서, 제한세율신청서, 거래원장, 조세조약 케이스 판정, 선지급/환수 상태

## 현재 구현 상태
- 외부 시스템은 아직 목 데이터 기반 계약 검증 상태다.
- `MarketDataService`가 표준 응답 구조와 현지 통화 환산 로직을 제공한다.
- `AlertStreamingService`가 알림 이벤트를 협력사·종목 topic으로 송신한다.

## 최신 기능정의 반영
- `quote` 응답은 현재가 KRW, 체결가 KRW, 실시간 또는 최신 환율이 적용된 현지통화 가격, 환율 기준시각/출처, 외국인 보유 수량, 보유율, 한도소진율을 포함한다.
- 현지 거래소는 모든 종목의 실시간 시세를 REST snapshot과 WebSocket stream 양쪽으로 조회할 수 있어야 하며, Hana-OmniLens-API는 KIS WebSocket 원천 데이터를 협력사용 bulk quote API와 market quote stream으로 재가공한다.
- Hana-OmniLens-API가 현지 거래소로 보내는 실시간 시세 tick에도 KRW 가격과 현지통화 환산 가격을 같이 포함한다.
- 현지 거래소의 과거 시세 차트는 KRX 과거 시세를 Hana-OmniLens-API가 수집·저장한 DB를 기준으로 제공한다.
- 다음 단계의 종목 상세/주문 상태 API는 `foreignOwnershipRate`, `foreignLimitExhaustionRate`, `predictedForeignOwnershipMin`, `predictedForeignOwnershipMax`, `viActivationStatus`, `priceLimitStatus`, `dataSource`를 단일 JSON으로 제공한다.
- 뉴스·공시 이벤트는 `alertId`, `stockCode`, `sourceType`, `originalTitle`, `translatedTitle`, `summary`, `originalUrl`, `sentiment`, `importance`, `eventTags`, `holderTarget`, `watchlistTarget`을 포함한다.
- 세무 환급 상태 API는 `investorId`, `taxCaseType`, `totalWithheldTax`, `eligibleRefundAmount`, `instantPayoutFeeRate`, `complianceSandboxFlag`를 현지 거래소 백엔드에 제공하는 계약으로 관리한다.
