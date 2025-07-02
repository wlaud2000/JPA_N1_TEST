package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.DailyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecommendationRepository extends JpaRepository<DailyRecommendation, Long> {

    /**
     * 지역 ID와 예보 날짜로 일일 추천 정보 조회
     */
    Optional<DailyRecommendation> findByRegionIdAndForecastDate(Long regionId, LocalDate forecastDate);

    /**
     * 지역 ID와 날짜 범위로 추천 정보 조회 (날짜순 정렬)
     */
    List<DailyRecommendation> findByRegionIdAndForecastDateBetweenOrderByForecastDate(
            Long regionId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 날짜의 모든 지역 추천 정보 조회 (지역명순 정렬)
     */
    @Query("SELECT dr FROM DailyRecommendation dr " +
            "JOIN FETCH dr.region r " +
            "JOIN FETCH dr.weatherTemplate wt " +
            "WHERE dr.forecastDate = :forecastDate " +
            "ORDER BY r.name")
    List<DailyRecommendation> findByForecastDateOrderByRegionName(@Param("forecastDate") LocalDate forecastDate);

    /**
     * 지역과 날짜 범위로 추천 정보 존재 여부 확인
     */
    boolean existsByRegionIdAndForecastDateBetween(Long regionId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 지역의 최신 추천 정보 조회
     */
    @Query("SELECT dr FROM DailyRecommendation dr " +
            "WHERE dr.region.id = :regionId " +
            "ORDER BY dr.forecastDate DESC, dr.updatedAt DESC")
    List<DailyRecommendation> findLatestByRegionId(@Param("regionId") Long regionId);
}
