package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    /**
     * 지역명으로 검색
     */
    Optional<Region> findByName(String name);

    /**
     * 지역 코드로 검색
     */
    Optional<Region> findByRegCode(String regCode);

    /**
     * 좌표 범위 내 지역 검색
     */
    @Query("SELECT r FROM Region r " +
            "WHERE r.latitude BETWEEN :minLat AND :maxLat " +
            "AND r.longitude BETWEEN :minLon AND :maxLon")
    List<Region> findByCoordinateRange(
            @Param("minLat") BigDecimal minLatitude,
            @Param("maxLat") BigDecimal maxLatitude,
            @Param("minLon") BigDecimal minLongitude,
            @Param("maxLon") BigDecimal maxLongitude
    );

    /**
     * 활성화된 모든 지역 조회 (추후 활성화 필드 추가 시 사용)
     */
    @Query("SELECT r FROM Region r ORDER BY r.name")
    List<Region> findAllOrderByName();
}
