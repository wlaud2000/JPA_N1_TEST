package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.RawShortTermWeather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawShortTermWeatherRepository extends JpaRepository<RawShortTermWeather, Long> {
}
