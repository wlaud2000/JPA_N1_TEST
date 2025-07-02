package com.study.demo.testplayground.domain.weather.service;

import com.study.demo.testplayground.domain.weather.converter.WeatherConverter;
import com.study.demo.testplayground.domain.weather.dto.response.WeatherResDTO;
import com.study.demo.testplayground.domain.weather.entity.*;
import com.study.demo.testplayground.domain.weather.entity.enums.PrecipCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.TempCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.WeatherType;
import com.study.demo.testplayground.domain.weather.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WeatherDataService {

    private final WeatherApiService weatherApiService;
    private final RegionRepository regionRepository;
    private final RawShortTermWeatherRepository rawShortTermWeatherRepository;
    private final RawMediumTermWeatherRepository rawMediumTermWeatherRepository;
    private final WeatherTemplateRepository weatherTemplateRepository;
    private final DailyRecommendationRepository dailyRecommendationRepository;

    /**
     * 단기 예보 데이터 업데이트 (0-2일차)
     * 3시간마다 실행
     */
    @Transactional
    public void updateShortTermWeatherData() {
        log.info("단기 예보 데이터 업데이트 시작");

        List<Region> regions = regionRepository.findAll();

        for (Region region : regions) {
            try {
                updateShortTermWeatherForRegion(region)
                        .doOnSuccess(success -> log.info("지역 {} 단기 예보 업데이트 완료", region.getName()))
                        .doOnError(error -> log.error("지역 {} 단기 예보 업데이트 실패: {}", region.getName(), error.getMessage()))
                        .subscribe();
            } catch (Exception e) {
                log.error("지역 {} 단기 예보 처리 중 오류: {}", region.getName(), e.getMessage());
            }
        }
    }

    /**
     * 중기 예보 데이터 업데이트 (3-6일차)
     * 12시간마다 실행
     */
    @Transactional
    public void updateMediumTermWeatherData() {
        log.info("중기 예보 데이터 업데이트 시작");

        List<Region> regions = regionRepository.findAll();

        for (Region region : regions) {
            try {
                updateMediumTermWeatherForRegion(region)
                        .doOnSuccess(success -> log.info("지역 {} 중기 예보 업데이트 완료", region.getName()))
                        .doOnError(error -> log.error("지역 {} 중기 예보 업데이트 실패: {}", region.getName(), error.getMessage()))
                        .subscribe();
            } catch (Exception e) {
                log.error("지역 {} 중기 예보 처리 중 오류: {}", region.getName(), e.getMessage());
            }
        }
    }

    /**
     * 특정 지역의 단기 예보 업데이트
     */
    private Mono<Void> updateShortTermWeatherForRegion(Region region) {
        String baseDate = LocalDate.now().toString().replace("-", "");
        String baseTime = getCurrentBaseTime();

        return weatherApiService.getShortTermWeather(
                        baseDate, baseTime,
                        region.getGridX().intValue(),
                        region.getGridY().intValue())
                .flatMap(response -> {
                    processShortTermWeatherResponse(response, region);
                    updateDailyRecommendationsForShortTerm(region);
                    return Mono.empty();
                });
    }

    /**
     * 특정 지역의 중기 예보 업데이트
     */
    private Mono<Void> updateMediumTermWeatherForRegion(Region region) {
        String regCode = region.getRegCode();

        return Mono.zip(
                        weatherApiService.getMediumTermTemperature(regCode),
                        weatherApiService.getMediumTermLandWeather(regCode))
                .flatMap(tuple -> {
                    WeatherResDTO.MediumTermTemperatureResponse tempResponse = tuple.getT1();
                    WeatherResDTO.MediumTermLandWeatherResponse landResponse = tuple.getT2();

                    processMediumTermWeatherResponse(tempResponse, landResponse, region);
                    updateDailyRecommendationsForMediumTerm(region);
                    return Mono.empty();
                });
    }

    /**
     * 단기 예보 응답 처리
     */
    private void processShortTermWeatherResponse(
            WeatherResDTO.ShortTermWeatherResponse response, Region region) {

        if (response.response().body().items() == null ||
                response.response().body().items().item() == null) {
            log.warn("단기 예보 응답 데이터가 비어있음 - 지역: {}", region.getName());
            return;
        }

        List<WeatherResDTO.ShortTermWeatherItem> items = response.response().body().items().item();

        // 날짜별로 그룹화하여 처리
        Map<String, List<WeatherResDTO.ShortTermWeatherItem>> itemsByDate = items.stream()
                .collect(Collectors.groupingBy(WeatherResDTO.ShortTermWeatherItem::fcstDate));

        for (Map.Entry<String, List<WeatherResDTO.ShortTermWeatherItem>> entry : itemsByDate.entrySet()) {
            String dateStr = entry.getKey();
            List<WeatherResDTO.ShortTermWeatherItem> dateItems = entry.getValue();

            // 해당 날짜의 대표 시간대 데이터만 추출 (예: 12시)
            Optional<WeatherResDTO.ShortTermWeatherItem> representativeItem =
                    findRepresentativeItem(dateItems);

            if (representativeItem.isPresent()) {
                RawShortTermWeather weather = WeatherConverter.toRawShortTermWeather(
                        representativeItem.get(), region);

                // Upsert 로직
                rawShortTermWeatherRepository.save(weather);
            }
        }
    }

    /**
     * 중기 예보 응답 처리
     */
    private void processMediumTermWeatherResponse(
            WeatherResDTO.MediumTermTemperatureResponse tempResponse,
            WeatherResDTO.MediumTermLandWeatherResponse landResponse,
            Region region) {

        if (tempResponse.response().body().items() == null ||
                landResponse.response().body().items() == null) {
            log.warn("중기 예보 응답 데이터가 비어있음 - 지역: {}", region.getName());
            return;
        }

        List<WeatherResDTO.MediumTermTemperatureItem> tempItems =
                tempResponse.response().body().items().item();
        List<WeatherResDTO.MediumTermLandWeatherItem> landItems =
                landResponse.response().body().items().item();

        // 기온과 육상 예보 데이터를 매칭하여 저장
        for (int i = 0; i < Math.min(tempItems.size(), landItems.size()); i++) {
            WeatherResDTO.MediumTermTemperatureItem tempItem = tempItems.get(i);
            WeatherResDTO.MediumTermLandWeatherItem landItem = landItems.get(i);

            RawMediumTermWeather weather = WeatherConverter.toRawMediumTermWeather(
                    tempItem, landItem, region);

            // Upsert 로직
            rawMediumTermWeatherRepository.save(weather);
        }
    }

    /**
     * 단기 예보 기반 일일 추천 업데이트
     */
    private void updateDailyRecommendationsForShortTerm(Region region) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i <= 2; i++) { // 0-2일차
            LocalDate targetDate = today.plusDays(i);
            updateDailyRecommendation(region, targetDate, true);
        }
    }

    /**
     * 중기 예보 기반 일일 추천 업데이트
     */
    private void updateDailyRecommendationsForMediumTerm(Region region) {
        LocalDate today = LocalDate.now();

        for (int i = 3; i <= 6; i++) { // 3-6일차
            LocalDate targetDate = today.plusDays(i);
            updateDailyRecommendation(region, targetDate, false);
        }
    }

    /**
     * 일일 추천 정보 업데이트
     */
    private void updateDailyRecommendation(Region region, LocalDate targetDate, boolean isShortTerm) {
        try {
            WeatherTemplate template;

            if (isShortTerm) {
                // 단기 예보 데이터에서 템플릿 생성
                template = createTemplateFromShortTermData(region, targetDate);
            } else {
                // 중기 예보 데이터에서 템플릿 생성
                template = createTemplateFromMediumTermData(region, targetDate);
            }

            if (template != null) {
                // 기존 추천 정보 조회 또는 생성
                Optional<DailyRecommendation> existingRecommendation =
                        dailyRecommendationRepository.findByRegionIdAndForecastDate(region.getId(), targetDate);

                DailyRecommendation recommendation;
                if (existingRecommendation.isPresent()) {
                    recommendation = existingRecommendation.get();
                    // 템플릿만 업데이트 (Builder 패턴이므로 새로 생성)
                    recommendation = DailyRecommendation.builder()
                            .id(recommendation.getId())
                            .region(region)
                            .weatherTemplate(template)
                            .forecastDate(targetDate)
                            .updatedAt(LocalDateTime.now())
                            .build();
                } else {
                    recommendation = DailyRecommendation.builder()
                            .region(region)
                            .weatherTemplate(template)
                            .forecastDate(targetDate)
                            .updatedAt(LocalDateTime.now())
                            .build();
                }

                dailyRecommendationRepository.save(recommendation);
                log.info("일일 추천 정보 업데이트 완료 - 지역: {}, 날짜: {}", region.getName(), targetDate);
            }
        } catch (Exception e) {
            log.error("일일 추천 정보 업데이트 실패 - 지역: {}, 날짜: {}, 오류: {}",
                    region.getName(), targetDate, e.getMessage());
        }
    }

    /**
     * 단기 예보 데이터에서 템플릿 생성
     */
    private WeatherTemplate createTemplateFromShortTermData(Region region, LocalDate targetDate) {
        // 단기 예보 데이터 조회 로직 (실제 구현에서는 Repository 메서드 추가 필요)
        // 임시로 더미 데이터 사용
        double temperature = 22.0;
        double precipitation = 0.0;
        String skyCondition = "맑음";

        return findOrCreateWeatherTemplate(temperature, precipitation, skyCondition);
    }

    /**
     * 중기 예보 데이터에서 템플릿 생성
     */
    private WeatherTemplate createTemplateFromMediumTermData(Region region, LocalDate targetDate) {
        // 중기 예보 데이터 조회 로직 (실제 구현에서는 Repository 메서드 추가 필요)
        // 임시로 더미 데이터 사용
        double temperature = 20.0;
        double precipitation = 0.0;
        String skyCondition = "구름많음";

        return findOrCreateWeatherTemplate(temperature, precipitation, skyCondition);
    }

    /**
     * 날씨 템플릿 조회 또는 생성
     */
    private WeatherTemplate findOrCreateWeatherTemplate(
            double temperature, double precipitation, String skyCondition) {

        TempCategory tempCategory = determineTempCategory(temperature);
        PrecipCategory precipCategory = determinePrecipCategory(precipitation);
        WeatherType weatherType = determineWeatherType(skyCondition);

        // 기존 템플릿 조회
        Optional<WeatherTemplate> existingTemplate = weatherTemplateRepository
                .findByWeatherAndTempCategoryAndPrecipCategory(weatherType, tempCategory, precipCategory);

        if (existingTemplate.isPresent()) {
            return existingTemplate.get();
        }

        // 새 템플릿 생성
        WeatherTemplate newTemplate = WeatherConverter.createWeatherTemplate(
                temperature, precipitation, skyCondition);

        return weatherTemplateRepository.save(newTemplate);
    }

    // === 유틸리티 메서드들 ===

    /**
     * 현재 시간 기준으로 발표 시각 결정
     */
    private String getCurrentBaseTime() {
        // 단기예보는 02, 05, 08, 11, 14, 17, 20, 23시에 발표
        int hour = LocalDateTime.now().getHour();
        int baseHour;

        if (hour < 2) baseHour = 23;
        else if (hour < 5) baseHour = 2;
        else if (hour < 8) baseHour = 5;
        else if (hour < 11) baseHour = 8;
        else if (hour < 14) baseHour = 11;
        else if (hour < 17) baseHour = 14;
        else if (hour < 20) baseHour = 17;
        else if (hour < 23) baseHour = 20;
        else baseHour = 23;

        return String.format("%02d00", baseHour);
    }

    /**
     * 대표 시간대 아이템 찾기 (12시 기준)
     */
    private Optional<WeatherResDTO.ShortTermWeatherItem> findRepresentativeItem(
            List<WeatherResDTO.ShortTermWeatherItem> items) {

        return items.stream()
                .filter(item -> "1200".equals(item.fcstTime()))
                .findFirst()
                .or(() -> items.stream().findFirst());
    }

    // 카테고리 결정 메서드들 (Converter와 동일한 로직)
    private TempCategory determineTempCategory(Double temperature) {
        if (temperature <= 10) return TempCategory.CHILLY;
        else if (temperature <= 20) return TempCategory.COOL;
        else if (temperature <= 27) return TempCategory.MILD;
        else return TempCategory.HOT;
    }

    private PrecipCategory determinePrecipCategory(Double precipitation) {
        if (precipitation >= 10) return PrecipCategory.HEAVY;
        else if (precipitation >= 1) return PrecipCategory.LIGHT;
        else return PrecipCategory.NONE;
    }

    private WeatherType determineWeatherType(String skyCondition) {
        if (skyCondition.contains("맑음")) return WeatherType.CLEAR;
        else if (skyCondition.contains("눈")) return WeatherType.SNOW;
        else return WeatherType.CLOUDY;
    }

    /**
     * 오래된 데이터 정리 (7일 이전 데이터 삭제)
     */
    @Transactional
    public void cleanupOldData() {
        log.info("오래된 날씨 데이터 정리 시작");

        LocalDate cutoffDate = LocalDate.now().minusDays(7);

        try {
            // 단기 예보 데이터 정리
            List<RawShortTermWeather> oldShortTermData =
                    rawShortTermWeatherRepository.findOldRecords(cutoffDate);

            if (!oldShortTermData.isEmpty()) {
                rawShortTermWeatherRepository.deleteAll(oldShortTermData);
                log.info("오래된 단기 예보 데이터 {}건 삭제", oldShortTermData.size());
            }

            // 중기 예보 데이터 정리
            List<RawMediumTermWeather> oldMediumTermData =
                    rawMediumTermWeatherRepository.findOldRecords(cutoffDate);

            if (!oldMediumTermData.isEmpty()) {
                rawMediumTermWeatherRepository.deleteAll(oldMediumTermData);
                log.info("오래된 중기 예보 데이터 {}건 삭제", oldMediumTermData.size());
            }

            log.info("오래된 날씨 데이터 정리 완료");

        } catch (Exception e) {
            log.error("오래된 데이터 정리 중 오류: {}", e.getMessage());
            throw e;
        }
    }
}
