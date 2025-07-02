package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.WeatherTemplate;
import com.study.demo.testplayground.domain.weather.entity.enums.PrecipCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.TempCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.WeatherType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherTemplateRepository extends JpaRepository<WeatherTemplate, Long> {

    /**
     * 날씨, 기온, 강수량 카테고리로 템플릿 조회
     */
    Optional<WeatherTemplate> findByWeatherAndTempCategoryAndPrecipCategory(
            WeatherType weather,
            TempCategory tempCategory,
            PrecipCategory precipCategory
    );
}
