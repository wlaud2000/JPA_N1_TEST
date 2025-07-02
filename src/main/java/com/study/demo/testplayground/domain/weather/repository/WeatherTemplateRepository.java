package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.WeatherTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherTemplateRepository extends JpaRepository<WeatherTemplate, Long> {
}
