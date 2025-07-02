package com.study.demo.testplayground.domain.weather.dto.request;

import jakarta.validation.constraints.NotNull;

public class WeatherReqDTO {

    public record GetDailyRecommendation(
            @NotNull(message = "지역 ID는 필수 입력값입니다.")
            Long regionId,

            @NotNull(message = "예보 날짜는 필수 입력값입니다.")
            String forecastDate  // yyyy-MM-dd 형식
    ) {
    }

    public record GetWeatherByCoordinate(
            @NotNull(message = "위도는 필수 입력값입니다.")
            Double latitude,

            @NotNull(message = "경도는 필수 입력값입니다.")
            Double longitude
    ) {
    }
}
