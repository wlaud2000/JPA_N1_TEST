package com.study.demo.testplayground.domain.weather.service;

import com.study.demo.testplayground.domain.weather.dto.response.WeatherResDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherApiService {

    private final WebClient kmaWebClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 격자 데이터 위경도 조회 API 호출
     * URL: https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat?authKey=XXX&lat=37.571711&lon=126.986070
     */
    public Mono<WeatherResDTO.GridCoordinateResponse> getGridCoordinate(Double lat, Double lon) {
        log.info("격자 좌표 조회 API 호출 - 위도: {}, 경도: {}", lat, lon);

        return kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat")
                        .queryParam("authKey", "{authKey}")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseGridCoordinateResponse)
                .doOnSuccess(response -> log.info("격자 좌표 조회 성공: {}", response))
                .doOnError(error -> log.error("격자 좌표 조회 실패: {}", error.getMessage()));
    }

    /**
     * 단기 예보 조회 API 호출
     * URL: https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst?pageNo=1&numOfRows=152&dataType=JSON&base_date=20250702&base_time=1400&nx=55&ny=127&authKey=XXX
     */
    public Mono<WeatherResDTO.ShortTermWeatherResponse> getShortTermWeather(
            String baseDate, String baseTime, Integer nx, Integer ny) {

        log.info("단기 예보 조회 API 호출 - 기준일시: {} {}, 격자: ({}, {})", baseDate, baseTime, nx, ny);

        return kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst")
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "1000")
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .queryParam("authKey", "{authKey}")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResDTO.ShortTermWeatherResponse.class)
                .doOnSuccess(response -> log.info("단기 예보 조회 성공"))
                .doOnError(error -> log.error("단기 예보 조회 실패: {}", error.getMessage()));
    }

    /**
     * 중기 기온 예보 조회 API 호출
     * URL: https://apihub.kma.go.kr/api/typ01/url/fct_afs_wc.php?reg=11B10101&authKey=XXX
     */
    public Mono<WeatherResDTO.MediumTermTemperatureResponse> getMediumTermTemperature(String regCode) {
        log.info("중기 기온 예보 조회 API 호출 - 지역코드: {}", regCode);

        return kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/typ01/url/fct_afs_wc.php")
                        .queryParam("reg", regCode)
                        .queryParam("authKey", "{authKey}")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseMediumTermTemperatureResponse)
                .doOnSuccess(response -> log.info("중기 기온 예보 조회 성공"))
                .doOnError(error -> log.error("중기 기온 예보 조회 실패: {}", error.getMessage()));
    }

    /**
     * 중기 육상 예보 조회 API 호출
     * URL: https://apihub.kma.go.kr/api/typ01/url/fct_afs_wl.php?reg=11B00000&authKey=XXX
     */
    public Mono<WeatherResDTO.MediumTermLandWeatherResponse> getMediumTermLandWeather(String regCode) {
        log.info("중기 육상 예보 조회 API 호출 - 지역코드: {}", regCode);

        return kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/typ01/url/fct_afs_wl.php")
                        .queryParam("reg", regCode)
                        .queryParam("authKey", "{authKey}")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseMediumTermLandWeatherResponse)
                .doOnSuccess(response -> log.info("중기 육상 예보 조회 성공"))
                .doOnError(error -> log.error("중기 육상 예보 조회 실패: {}", error.getMessage()));
    }

    /**
     * 격자 좌표 응답 파싱
     * 응답 형태:
     * #START7777
     * #       LON,         LAT,   X,   Y
     *  126.986069,   37.571712,  60, 127
     */
    private WeatherResDTO.GridCoordinateResponse parseGridCoordinateResponse(String response) {
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // 데이터 라인을 찾았을 때
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    return WeatherResDTO.GridCoordinateResponse.builder()
                            .lon(parts[0].trim())
                            .lat(parts[1].trim())
                            .x(parts[2].trim())
                            .y(parts[3].trim())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("격자 좌표 응답 파싱 실패: {}", e.getMessage());
        }

        // 파싱 실패 시 기본값 반환
        log.warn("격자 좌표 파싱 실패, 기본값 반환");
        return WeatherResDTO.GridCoordinateResponse.builder()
                .lat("37.5714")
                .lon("126.9906")
                .x("60")
                .y("127")
                .build();
    }

    /**
     * 중기 기온 예보 응답 파싱
     * 응답 형태:
     * # REG_ID TM_FC        TM_EF        MOD STN C MIN MAX MIN_L MIN_H MAX_L MAX_H
     * 11B10101 202507020600 202507060000 A01 109 2  25  31    1    1    1    1
     *
     * 필드 매핑:
     * parts[0] = REG_ID, parts[1] = TM_FC, parts[2] = TM_EF
     * parts[6] = MIN (최저기온), parts[7] = MAX (최고기온)
     */
    private WeatherResDTO.MediumTermTemperatureResponse parseMediumTermTemperatureResponse(String response) {
        List<WeatherResDTO.MediumTermTemperatureItem> items = new ArrayList<>();

        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // 공백으로 분할 (여러 공백도 처리)
                String[] parts = line.split("\\s+");
                if (parts.length >= 8) {  // 최소 8개 필드 (REG_ID~MAX)까지 있어야 함
                    WeatherResDTO.MediumTermTemperatureItem item =
                            WeatherResDTO.MediumTermTemperatureItem.builder()
                                    .regId(parts[0])      // REG_ID
                                    .tmFc(parts[1])       // TM_FC
                                    .tmEf(parts[2])       // TM_EF
                                    .code(parts[3])       // MOD
                                    .stnCode(parts[4])    // STN
                                    .ctaCode(parts[5])    // C
                                    .taMin3(parts[6])     // MIN (최저기온)
                                    .taMax3(parts[7])     // MAX (최고기온)
                                    // MIN_L, MIN_H, MAX_L, MAX_H는 Entity에서 사용하지 않으므로 생략
                                    .taMin3Low(parts.length > 8 ? parts[8] : "1")
                                    .taMin3High(parts.length > 9 ? parts[9] : "1")
                                    .taMax3Low(parts.length > 10 ? parts[10] : "1")
                                    .taMax3High(parts.length > 11 ? parts[11] : "1")
                                    .build();
                    items.add(item);
                }
            }

            log.info("중기 기온 예보 파싱 완료 - {}건", items.size());

        } catch (Exception e) {
            log.error("중기 기온 예보 응답 파싱 실패: {}", e.getMessage());
        }

        return WeatherResDTO.MediumTermTemperatureResponse.builder()
                .response(WeatherResDTO.MediumTermTemperatureResponse.Response.builder()
                        .header(WeatherResDTO.MediumTermTemperatureResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(WeatherResDTO.MediumTermTemperatureResponse.Body.builder()
                                .dataType("TEXT")
                                .items(WeatherResDTO.MediumTermTemperatureResponse.Items.builder()
                                        .item(items)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * 중기 육상 예보 응답 파싱
     * 응답 형태:
     * # REG_ID TM_FC        TM_EF        MOD STN C SKY  PRE  CONF WF    RN_ST
     * 11B00000 202507020600 202507060000 A02 109 2 WB04 WB00 없음 "흐림" 40
     *
     * 필드 매핑:
     * parts[0] = REG_ID, parts[1] = TM_FC, parts[2] = TM_EF
     * parts[9] = WF (날씨 상태: "흐림", "구름많음" 등)
     */
    private WeatherResDTO.MediumTermLandWeatherResponse parseMediumTermLandWeatherResponse(String response) {
        List<WeatherResDTO.MediumTermLandWeatherItem> items = new ArrayList<>();

        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // 특수 처리: "흐림", "구름많음" 등이 따옴표로 감싸져 있을 수 있음
                String processedLine = line;
                if (line.contains("\"")) {
                    processedLine = line.replaceAll("\"([^\"]+)\"", "$1");
                }

                String[] parts = processedLine.split("\\s+");
                if (parts.length >= 10) {  // 최소 10개 필드 (REG_ID~WF)까지 있어야 함
                    // parts[9] = WF (날씨 상태)
                    String wf = parts[9];

                    WeatherResDTO.MediumTermLandWeatherItem item =
                            WeatherResDTO.MediumTermLandWeatherItem.builder()
                                    .regId(parts[0])      // REG_ID
                                    .tmFc(parts[1])       // TM_FC
                                    .tmEf(parts[2])       // TM_EF
                                    .code(parts[3])       // MOD
                                    .stnCode(parts[4])    // STN
                                    .ctaCode(parts[5])    // C
                                    .wf3Am(wf)           // WF (날씨 상태)
                                    .wf3Pm(wf)           // 오전/오후 동일하게 처리
                                    .rnSt3Am(parts.length > 10 ? parts[10] : "0")  // RN_ST
                                    .rnSt3Pm(parts.length > 10 ? parts[10] : "0")  // 오전/오후 동일하게 처리
                                    .build();
                    items.add(item);
                }
            }

            log.info("중기 육상 예보 파싱 완료 - {}건", items.size());

        } catch (Exception e) {
            log.error("중기 육상 예보 응답 파싱 실패: {}", e.getMessage());
        }

        return WeatherResDTO.MediumTermLandWeatherResponse.builder()
                .response(WeatherResDTO.MediumTermLandWeatherResponse.Response.builder()
                        .header(WeatherResDTO.MediumTermLandWeatherResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(WeatherResDTO.MediumTermLandWeatherResponse.Body.builder()
                                .dataType("TEXT")
                                .items(WeatherResDTO.MediumTermLandWeatherResponse.Items.builder()
                                        .item(items)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
