package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
