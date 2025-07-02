package com.study.demo.testplayground.domain.weather.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WeatherResDTO {

    // 외부 API 응답용 DTO들

    @Builder
    public record GridCoordinateResponse(
            String lat,
            String lon,
            String x,
            String y
    ) {
    }

    @Builder
    public record ShortTermWeatherItem(
            String baseDate,
            String baseTime,
            String category,
            String fcstDate,
            String fcstTime,
            String fcstValue,
            String nx,
            String ny
    ) {
    }

    @Builder
    public record ShortTermWeatherResponse(
            Response response
    ) {
        @Builder
        public record Response(
                Header header,
                Body body
        ) {
        }

        @Builder
        public record Header(
                String resultCode,
                String resultMsg
        ) {
        }

        @Builder
        public record Body(
                String dataType,
                Items items,
                String pageNo,
                String numOfRows,
                String totalCount
        ) {
        }

        @Builder
        public record Items(
                List<ShortTermWeatherItem> item
        ) {
        }
    }

    @Builder
    public record MediumTermTemperatureItem(
            String regId,
            String tmFc,
            String tmEf,
            String code,
            String stnCode,
            String ctaCode,
            String taMin3,
            String taMax3,
            String taMin3Low,
            String taMin3High,
            String taMax3Low,
            String taMax3High
    ) {
    }

    @Builder
    public record MediumTermTemperatureResponse(
            Response response
    ) {
        @Builder
        public record Response(
                Header header,
                Body body
        ) {
        }

        @Builder
        public record Header(
                String resultCode,
                String resultMsg
        ) {
        }

        @Builder
        public record Body(
                String dataType,
                Items items
        ) {
        }

        @Builder
        public record Items(
                List<MediumTermTemperatureItem> item
        ) {
        }
    }

    @Builder
    public record MediumTermLandWeatherItem(
            String regId,
            String tmFc,
            String tmEf,
            String code,
            String stnCode,
            String ctaCode,
            String wf3Am,
            String wf3Pm,
            String rnSt3Am,
            String rnSt3Pm
    ) {
    }

    @Builder
    public record MediumTermLandWeatherResponse(
            Response response
    ) {
        @Builder
        public record Response(
                Header header,
                Body body
        ) {
        }

        @Builder
        public record Header(
                String resultCode,
                String resultMsg
        ) {
        }

        @Builder
        public record Body(
                String dataType,
                Items items
        ) {
        }

        @Builder
        public record Items(
                List<MediumTermLandWeatherItem> item
        ) {
        }
    }

    // 클라이언트 응답용 DTO들

    @Builder
    public record DailyRecommendationInfo(
            Long id,
            String regionName,
            LocalDate forecastDate,
            String weatherMessage,
            String emoji,
            List<String> keywords,
            LocalDateTime updatedAt
    ) {
    }

    @Builder
    public record WeatherSummary(
            String weather,
            String temperature,
            String precipitation,
            Double actualTemp,
            Double precipitationAmount
    ) {
    }

    @Builder
    public record WeeklyRecommendations(
            String regionName,
            List<DailyRecommendationInfo> recommendations
    ) {
    }
}
