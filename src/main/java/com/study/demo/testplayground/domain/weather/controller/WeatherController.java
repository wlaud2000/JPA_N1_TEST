package com.study.demo.testplayground.domain.weather.controller;

import com.study.demo.testplayground.domain.weather.dto.request.WeatherReqDTO;
import com.study.demo.testplayground.domain.weather.dto.response.WeatherResDTO;
import com.study.demo.testplayground.domain.weather.service.WeatherScheduler;
import com.study.demo.testplayground.domain.weather.service.query.WeatherQueryService;
import com.study.demo.testplayground.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "날씨 API", description = "날씨 정보 및 데이트 코스 추천 API")
public class WeatherController {

    private final WeatherQueryService weatherQueryService;
    private final WeatherScheduler weatherScheduler;

    @GetMapping("/daily/{regionId}")
    @Operation(summary = "일일 날씨 추천 조회", description = "특정 지역의 특정 날짜 날씨 추천 정보를 조회합니다.")
    public ResponseEntity<CustomResponse<WeatherResDTO.DailyRecommendationInfo>> getDailyRecommendation(
            @Parameter(description = "지역 ID", required = true, example = "1")
            @PathVariable Long regionId,

            @Parameter(description = "예보 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-03")
            @RequestParam String forecastDate) {

        log.info("일일 날씨 추천 조회 API 호출 - 지역 ID: {}, 날짜: {}", regionId, forecastDate);

        WeatherResDTO.DailyRecommendationInfo recommendation =
                weatherQueryService.getDailyRecommendation(regionId, forecastDate);

        return ResponseEntity.ok(CustomResponse.onSuccess(recommendation));
    }

    @GetMapping("/weekly/{regionId}")
    @Operation(summary = "주간 날씨 추천 조회", description = "특정 지역의 7일간 날씨 추천 정보를 조회합니다.")
    public ResponseEntity<CustomResponse<WeatherResDTO.WeeklyRecommendations>> getWeeklyRecommendations(
            @Parameter(description = "지역 ID", required = true, example = "1")
            @PathVariable Long regionId) {

        log.info("주간 날씨 추천 조회 API 호출 - 지역 ID: {}", regionId);

        WeatherResDTO.WeeklyRecommendations recommendations =
                weatherQueryService.getWeeklyRecommendations(regionId);

        return ResponseEntity.ok(CustomResponse.onSuccess(recommendations));
    }

    @PostMapping("/coordinate")
    @Operation(summary = "좌표 기반 날씨 조회", description = "위도/경도 좌표를 기반으로 해당 지역의 오늘 날씨 정보를 조회합니다.")
    public ResponseEntity<CustomResponse<WeatherResDTO.DailyRecommendationInfo>> getWeatherByCoordinate(
            @Valid @RequestBody WeatherReqDTO.GetWeatherByCoordinate request) {

        log.info("좌표 기반 날씨 조회 API 호출 - 위도: {}, 경도: {}",
                request.latitude(), request.longitude());

        WeatherResDTO.DailyRecommendationInfo recommendation =
                weatherQueryService.getWeatherByCoordinate(request.latitude(), request.longitude());

        return ResponseEntity.ok(CustomResponse.onSuccess(recommendation));
    }

    @GetMapping("/today/summary")
    @Operation(summary = "오늘 날씨 요약", description = "모든 지역의 오늘 날씨 요약 정보를 조회합니다.")
    public ResponseEntity<CustomResponse<List<WeatherResDTO.DailyRecommendationInfo>>> getTodayWeatherSummary() {

        log.info("오늘 날씨 요약 조회 API 호출");

        List<WeatherResDTO.DailyRecommendationInfo> summaries =
                weatherQueryService.getTodayWeatherSummary();

        return ResponseEntity.ok(CustomResponse.onSuccess(summaries));
    }

    @GetMapping("/statistics/{regionId}")
    @Operation(summary = "날씨 통계 조회", description = "특정 지역의 특정 날짜 날씨 통계 정보를 조회합니다.")
    public ResponseEntity<CustomResponse<WeatherResDTO.WeatherSummary>> getWeatherStatistics(
            @Parameter(description = "지역 ID", required = true, example = "1")
            @PathVariable Long regionId,

            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", required = true, example = "2025-07-03")
            @RequestParam String date) {

        log.info("날씨 통계 조회 API 호출 - 지역 ID: {}, 날짜: {}", regionId, date);

        WeatherResDTO.WeatherSummary statistics =
                weatherQueryService.getWeatherStatistics(regionId, date);

        return ResponseEntity.ok(CustomResponse.onSuccess(statistics));
    }

    // === 관리자용 API (수동 업데이트) ===

    @PostMapping("/admin/update/short-term")
    @Operation(summary = "[관리자] 단기 예보 수동 업데이트", description = "단기 예보 데이터를 수동으로 업데이트합니다.")
    public ResponseEntity<CustomResponse<String>> manualUpdateShortTermWeather() {

        log.info("[관리자] 단기 예보 수동 업데이트 API 호출");

        try {
            weatherScheduler.manualUpdateShortTermWeather();
            return ResponseEntity.ok(CustomResponse.onSuccess("단기 예보 업데이트가 완료되었습니다."));
        } catch (Exception e) {
            log.error("단기 예보 수동 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.ok(CustomResponse.onFailure("UPDATE_FAILED",
                    "단기 예보 업데이트에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/update/medium-term")
    @Operation(summary = "[관리자] 중기 예보 수동 업데이트", description = "중기 예보 데이터를 수동으로 업데이트합니다.")
    public ResponseEntity<CustomResponse<String>> manualUpdateMediumTermWeather() {

        log.info("[관리자] 중기 예보 수동 업데이트 API 호출");

        try {
            weatherScheduler.manualUpdateMediumTermWeather();
            return ResponseEntity.ok(CustomResponse.onSuccess("중기 예보 업데이트가 완료되었습니다."));
        } catch (Exception e) {
            log.error("중기 예보 수동 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.ok(CustomResponse.onFailure("UPDATE_FAILED",
                    "중기 예보 업데이트에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "날씨 서비스 상태 확인", description = "날씨 서비스의 상태를 확인합니다.")
    public ResponseEntity<CustomResponse<String>> healthCheck() {

        log.debug("날씨 서비스 상태 확인 API 호출");

        try {
            // 간단한 상태 체크 (실제로는 더 복잡한 체크 로직 필요)
            return ResponseEntity.ok(CustomResponse.onSuccess("날씨 서비스가 정상적으로 동작 중입니다."));
        } catch (Exception e) {
            log.error("날씨 서비스 상태 확인 실패: {}", e.getMessage());
            return ResponseEntity.ok(CustomResponse.onFailure("HEALTH_CHECK_FAILED",
                    "서비스 상태 확인에 실패했습니다: " + e.getMessage()));
        }
    }
}
