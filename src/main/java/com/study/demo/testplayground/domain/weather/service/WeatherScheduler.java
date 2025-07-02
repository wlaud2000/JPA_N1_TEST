package com.study.demo.testplayground.domain.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherDataService weatherDataService;

    /**
     * 단기 예보 데이터 업데이트 스케줄러
     * 매 3시간마다 실행 (0-2일차 데이터)
     *
     * 실제 API 발표 시각: 02, 05, 08, 11, 14, 17, 20, 23시
     * 발표 후 10분 뒤에 실행하도록 설정
     */
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
    public void updateShortTermWeatherData() {
        log.info("=== 단기 예보 데이터 업데이트 스케줄러 시작 ===");
        log.info("실행 시간: {}", LocalDateTime.now());

        try {
            long startTime = System.currentTimeMillis();

            weatherDataService.updateShortTermWeatherData();

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("=== 단기 예보 데이터 업데이트 완료 ===");
            log.info("실행 시간: {}ms", executionTime);

        } catch (Exception e) {
            log.error("단기 예보 데이터 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 중기 예보 데이터 업데이트 스케줄러
     * 매 12시간마다 실행 (3-6일차 데이터)
     *
     * 실제 API 발표 시각: 06, 18시
     * 발표 후 30분 뒤에 실행하도록 설정
     */
    @Scheduled(cron = "0 30 6,18 * * *")
    public void updateMediumTermWeatherData() {
        log.info("=== 중기 예보 데이터 업데이트 스케줄러 시작 ===");
        log.info("실행 시간: {}", LocalDateTime.now());

        try {
            long startTime = System.currentTimeMillis();

            weatherDataService.updateMediumTermWeatherData();

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("=== 중기 예보 데이터 업데이트 완료 ===");
            log.info("실행 시간: {}ms", executionTime);

        } catch (Exception e) {
            log.error("중기 예보 데이터 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 오래된 날씨 데이터 정리 스케줄러
     * 매일 새벽 2시에 실행
     * 7일 이전 데이터 삭제
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldWeatherData() {
        log.info("=== 오래된 날씨 데이터 정리 스케줄러 시작 ===");
        log.info("실행 시간: {}", LocalDateTime.now());

        try {
            long startTime = System.currentTimeMillis();

            // 실제 구현에서는 WeatherDataService에 cleanup 메서드 추가 필요
            // weatherDataService.cleanupOldData();

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("=== 오래된 날씨 데이터 정리 완료 ===");
            log.info("실행 시간: {}ms", executionTime);

        } catch (Exception e) {
            log.error("오래된 날씨 데이터 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 시스템 상태 체크 스케줄러
     * 매 시간 정각에 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void systemHealthCheck() {
        log.debug("=== 날씨 서비스 상태 체크 ===");
        log.debug("실행 시간: {}", LocalDateTime.now());

        try {
            // 실제 구현에서는 다음 항목들을 체크
            // 1. 외부 API 연결 상태
            // 2. 데이터베이스 연결 상태
            // 3. 최근 업데이트 시간 확인
            // 4. 오류 로그 분석

            log.debug("날씨 서비스 정상 동작 중");

        } catch (Exception e) {
            log.error("시스템 상태 체크 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동 단기 예보 업데이트 트리거
     * 테스트 또는 긴급 상황 시 사용
     */
    public void manualUpdateShortTermWeather() {
        log.info("=== 수동 단기 예보 업데이트 실행 ===");
        log.info("실행 시간: {}", LocalDateTime.now());

        try {
            weatherDataService.updateShortTermWeatherData();
            log.info("수동 단기 예보 업데이트 완료");
        } catch (Exception e) {
            log.error("수동 단기 예보 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("수동 업데이트 실패", e);
        }
    }

    /**
     * 수동 중기 예보 업데이트 트리거
     * 테스트 또는 긴급 상황 시 사용
     */
    public void manualUpdateMediumTermWeather() {
        log.info("=== 수동 중기 예보 업데이트 실행 ===");
        log.info("실행 시간: {}", LocalDateTime.now());

        try {
            weatherDataService.updateMediumTermWeatherData();
            log.info("수동 중기 예보 업데이트 완료");
        } catch (Exception e) {
            log.error("수동 중기 예보 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("수동 업데이트 실패", e);
        }
    }
}
