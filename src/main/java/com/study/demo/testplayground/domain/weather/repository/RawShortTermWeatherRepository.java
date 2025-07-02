package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.RawShortTermWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RawShortTermWeatherRepository extends JpaRepository<RawShortTermWeather, Long> {

    /**
     * 지역과 예보 날짜로 단기 예보 데이터 조회
     */
    List<RawShortTermWeather> findByRegionIdAndFcstDate(Long regionId, LocalDate fcstDate);

    /**
     * 지역과 날짜 범위로 단기 예보 데이터 조회
     */
    List<RawShortTermWeather> findByRegionIdAndFcstDateBetweenOrderByFcstDateAscFcstTimeAsc(
            Long regionId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 지역의 특정 날짜 대표 시간(12시) 데이터 조회
     */
    @Query("SELECT rst FROM RawShortTermWeather rst " +
            "WHERE rst.region.id = :regionId " +
            "AND rst.fcstDate = :fcstDate " +
            "AND rst.fcstTime = '1200'")
    Optional<RawShortTermWeather> findRepresentativeByRegionIdAndDate(
            @Param("regionId") Long regionId,
            @Param("fcstDate") LocalDate fcstDate);

    /**
     * 오래된 단기 예보 데이터 삭제용 조회
     */
    @Query("SELECT rst FROM RawShortTermWeather rst " +
            "WHERE rst.baseDate < :cutoffDate")
    List<RawShortTermWeather> findOldRecords(@Param("cutoffDate") LocalDate cutoffDate);
}
