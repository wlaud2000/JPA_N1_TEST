package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.DailyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRecommendationRepository extends JpaRepository<DailyRecommendation, Long> {
}
