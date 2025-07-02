package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.RawMediumTermWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RawMediumTermWeatherRepository extends JpaRepository<RawMediumTermWeather, Long> {

    /**
     * 지역과 발효 날짜로 중기 예보 데이터 조회
     */
    Optional<RawMediumTermWeather> findByRegionIdAndTmef(Long regionId, LocalDate tmef);

    /**
     * 지역과 날짜 범위로 중기 예보 데이터 조회
     */
    List<RawMediumTermWeather> findByRegionIdAndTmefBetweenOrderByTmefAsc(
            Long regionId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 지역의 최신 중기 예보 데이터 조회
     */
    @Query("SELECT rmt FROM RawMediumTermWeather rmt " +
            "WHERE rmt.region.id = :regionId " +
            "ORDER BY rmt.tmfc DESC, rmt.tmef DESC")
    List<RawMediumTermWeather> findLatestByRegionId(@Param("regionId") Long regionId);

    /**
     * 오래된 중기 예보 데이터 삭제용 조회
     */
    @Query("SELECT rmt FROM RawMediumTermWeather rmt " +
            "WHERE rmt.tmfc < :cutoffDate")
    List<RawMediumTermWeather> findOldRecords(@Param("cutoffDate") LocalDate cutoffDate);
}
