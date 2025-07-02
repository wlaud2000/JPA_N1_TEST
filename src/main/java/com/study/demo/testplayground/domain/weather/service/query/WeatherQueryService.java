package com.study.demo.testplayground.domain.weather.service.query;

import com.study.demo.testplayground.domain.weather.converter.WeatherConverter;
import com.study.demo.testplayground.domain.weather.dto.response.WeatherResDTO;
import com.study.demo.testplayground.domain.weather.entity.DailyRecommendation;
import com.study.demo.testplayground.domain.weather.entity.Region;
import com.study.demo.testplayground.domain.weather.repository.DailyRecommendationRepository;
import com.study.demo.testplayground.domain.weather.repository.RegionRepository;
import com.study.demo.testplayground.domain.weather.service.WeatherApiService;
import com.study.demo.testplayground.global.apiPayload.code.GeneralErrorCode;
import com.study.demo.testplayground.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherQueryService {

    private final DailyRecommendationRepository dailyRecommendationRepository;
    private final RegionRepository regionRepository;

    /**
     * 특정 지역의 일일 추천 정보 조회
     */
    public WeatherResDTO.DailyRecommendationInfo getDailyRecommendation(Long regionId, String forecastDateStr) {
        log.info("일일 추천 정보 조회 - 지역 ID: {}, 예보 날짜: {}", regionId, forecastDateStr);

        // 날짜 형식 검증 및 변환
        LocalDate forecastDate;
        try {
            forecastDate = LocalDate.parse(forecastDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            log.error("잘못된 날짜 형식: {}", forecastDateStr);
            throw new CustomException(GeneralErrorCode.VALIDATION_FAILED);
        }

        // 지역 존재 여부 확인
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 지역 ID: {}", regionId);
                    return new CustomException(GeneralErrorCode.NOT_FOUND_404);
                });

        // 일일 추천 정보 조회
        Optional<DailyRecommendation> recommendation =
                dailyRecommendationRepository.findByRegionIdAndForecastDate(regionId, forecastDate);

        if (recommendation.isEmpty()) {
            log.warn("해당 날짜의 추천 정보가 없음 - 지역: {}, 날짜: {}", region.getName(), forecastDate);
            throw new CustomException(GeneralErrorCode.NOT_FOUND_404);
        }

        return WeatherConverter.toDailyRecommendationInfo(recommendation.get());
    }

    /**
     * 특정 지역의 주간 추천 정보 조회 (7일간)
     */
    public WeatherResDTO.WeeklyRecommendations getWeeklyRecommendations(Long regionId) {
        log.info("주간 추천 정보 조회 - 지역 ID: {}", regionId);

        // 지역 존재 여부 확인
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 지역 ID: {}", regionId);
                    return new CustomException(GeneralErrorCode.NOT_FOUND_404);
                });

        // 오늘부터 7일간의 추천 정보 조회
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(6);

        List<DailyRecommendation> recommendations =
                dailyRecommendationRepository.findByRegionIdAndForecastDateBetweenOrderByForecastDate(
                        regionId, startDate, endDate);

        if (recommendations.isEmpty()) {
            log.warn("주간 추천 정보가 없음 - 지역: {}", region.getName());
            throw new CustomException(GeneralErrorCode.NOT_FOUND_404);
        }

        return WeatherConverter.toWeeklyRecommendations(region.getName(), recommendations);
    }

    /**
     * 좌표 기반 날씨 정보 조회
     */
    public WeatherResDTO.DailyRecommendationInfo getWeatherByCoordinate(Double latitude, Double longitude) {
        log.info("좌표 기반 날씨 정보 조회 - 위도: {}, 경도: {}", latitude, longitude);

        try {
            // 좌표 유효성 검증
            validateCoordinates(latitude, longitude);

            // 가장 가까운 지역 찾기
            Region nearestRegion = findNearestRegion(latitude, longitude);

            if (nearestRegion == null) {
                log.warn("좌표에 해당하는 지역을 찾을 수 없음 - 위도: {}, 경도: {}", latitude, longitude);
                throw new CustomException(GeneralErrorCode.NOT_FOUND_404);
            }

            // 오늘 날짜의 추천 정보 조회
            LocalDate today = LocalDate.now();
            Optional<DailyRecommendation> recommendation =
                    dailyRecommendationRepository.findByRegionIdAndForecastDate(nearestRegion.getId(), today);

            if (recommendation.isEmpty()) {
                log.warn("오늘 날짜의 추천 정보가 없음 - 지역: {}", nearestRegion.getName());
                throw new CustomException(GeneralErrorCode.NOT_FOUND_404);
            }

            return WeatherConverter.toDailyRecommendationInfo(recommendation.get());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("좌표 기반 날씨 정보 조회 중 오류: {}", e.getMessage());
            throw new CustomException(GeneralErrorCode.INTERNAL_SERVER_ERROR_500);
        }
    }

    /**
     * 모든 지역의 오늘 날씨 요약 정보 조회
     */
    public List<WeatherResDTO.DailyRecommendationInfo> getTodayWeatherSummary() {
        log.info("오늘 날씨 요약 정보 조회");

        LocalDate today = LocalDate.now();

        List<DailyRecommendation> todayRecommendations =
                dailyRecommendationRepository.findByForecastDateOrderByRegionName(today);

        if (todayRecommendations.isEmpty()) {
            log.warn("오늘 날짜의 추천 정보가 없음");
            throw new CustomException(GeneralErrorCode.NOT_FOUND_404);
        }

        return todayRecommendations.stream()
                .map(WeatherConverter::toDailyRecommendationInfo)
                .toList();
    }

    /**
     * 지역별 날씨 상태 통계 조회
     */
    public WeatherResDTO.WeatherSummary getWeatherStatistics(Long regionId, String dateStr) {
        log.info("날씨 상태 통계 조회 - 지역 ID: {}, 날짜: {}", regionId, dateStr);

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            log.error("잘못된 날짜 형식: {}", dateStr);
            throw new CustomException(GeneralErrorCode.VALIDATION_FAILED);
        }

        // 지역 존재 여부 확인
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 지역 ID: {}", regionId);
                    return new CustomException(GeneralErrorCode.NOT_FOUND_404);
                });

        // 실제 구현에서는 Raw 데이터에서 통계 계산
        // 현재는 임시 데이터 사용
        double avgTemperature = 22.0;
        double totalPrecipitation = 0.0;
        String dominantWeather = "맑음";

        return WeatherConverter.toWeatherSummary(avgTemperature, totalPrecipitation, dominantWeather);
    }

    // === 유틸리티 메서드들 ===

    /**
     * 좌표 유효성 검증
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new CustomException(GeneralErrorCode.VALIDATION_FAILED);
        }

        // 대한민국 영역 대략 체크
        if (latitude < 33.0 || latitude > 43.0 || longitude < 124.0 || longitude > 132.0) {
            log.warn("유효하지 않은 좌표 범위 - 위도: {}, 경도: {}", latitude, longitude);
            throw new CustomException(GeneralErrorCode.VALIDATION_FAILED);
        }
    }

    /**
     * 가장 가까운 지역 찾기 (간단한 거리 계산)
     */
    private Region findNearestRegion(Double latitude, Double longitude) {
        List<Region> allRegions = regionRepository.findAll();

        Region nearestRegion = null;
        double minDistance = Double.MAX_VALUE;

        for (Region region : allRegions) {
            double distance = calculateDistance(
                    latitude, longitude,
                    region.getLatitude().doubleValue(),
                    region.getLongitude().doubleValue()
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestRegion = region;
            }
        }

        return nearestRegion;
    }

    /**
     * 두 좌표 간의 거리 계산 (단순 유클리드 거리)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = lat1 - lat2;
        double deltaLon = lon1 - lon2;
        return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
    }
}
