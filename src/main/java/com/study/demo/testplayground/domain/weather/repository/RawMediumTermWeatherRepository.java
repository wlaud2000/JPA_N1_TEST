package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.RawMediumTermWeather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMediumTermWeatherRepository extends JpaRepository<RawMediumTermWeather, Long> {
}
