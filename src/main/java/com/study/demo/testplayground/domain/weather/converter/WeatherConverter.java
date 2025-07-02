package com.study.demo.testplayground.domain.weather.converter;

import com.study.demo.testplayground.domain.weather.dto.response.WeatherResDTO;
import com.study.demo.testplayground.domain.weather.entity.*;
import com.study.demo.testplayground.domain.weather.entity.enums.PrecipCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.TempCategory;
import com.study.demo.testplayground.domain.weather.entity.enums.WeatherType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WeatherConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    /**
     * 단기 예보 API 응답을 RawShortTermWeather 엔티티로 변환
     */
    public static RawShortTermWeather toRawShortTermWeather(
            WeatherResDTO.ShortTermWeatherItem item, Region region) {

        LocalDate baseDate = LocalDate.parse(item.baseDate(), DATE_FORMATTER);
        LocalDate fcstDate = LocalDate.parse(item.fcstDate(), DATE_FORMATTER);

        return RawShortTermWeather.builder()
                .region(region)
                .baseDate(baseDate)
                .baseTime(item.baseTime())
                .fcstDate(fcstDate)
                .fcstTime(item.fcstTime())
                .tmp(Double.parseDouble(item.fcstValue()))
                .sky(mapSkyCode(item.fcstValue()))
                .pop(0.0) // 초기값, 다른 item에서 업데이트됨
                .pty(mapPtyCode(item.fcstValue()))
                .pcp(0.0) // 초기값, 다른 item에서 업데이트됨
                .build();
    }

    /**
     * 중기 기온 예보와 육상 예보를 결합하여 RawMediumTermWeather 엔티티로 변환
     */
    public static RawMediumTermWeather toRawMediumTermWeather(
            WeatherResDTO.MediumTermTemperatureItem tempItem,
            WeatherResDTO.MediumTermLandWeatherItem landItem,
            Region region) {

        LocalDate tmfc = LocalDate.parse(tempItem.tmFc(), DATE_FORMATTER);
        LocalDate tmef = LocalDate.parse(tempItem.tmEf(), DATE_FORMATTER);

        return RawMediumTermWeather.builder()
                .region(region)
                .tmfc(tmfc)
                .tmef(tmef)
                .sky(landItem.wf3Am())
                .pop(0.0) // 중기예보에서는 강수확률 정보 없음
                .minTmp(Double.parseDouble(tempItem.taMin3()))
                .maxTmp(Double.parseDouble(tempItem.taMax3()))
                .build();
    }

    /**
     * 날씨 데이터를 기반으로 WeatherTemplate 생성
     */
    public static WeatherTemplate createWeatherTemplate(
            Double temperature, Double precipitation, String skyCondition) {

        TempCategory tempCategory = determineTempCategory(temperature);
        PrecipCategory precipCategory = determinePrecipCategory(precipitation);
        WeatherType weatherType = determineWeatherType(skyCondition);

        return WeatherTemplate.builder()
                .weather(weatherType)
                .tempCategory(tempCategory)
                .precipCategory(precipCategory)
                .message(generateMessage(weatherType, tempCategory, precipCategory))
                .emoji(generateEmoji(weatherType, tempCategory))
                .build();
    }

    /**
     * DailyRecommendation을 응답 DTO로 변환
     */
    public static WeatherResDTO.DailyRecommendationInfo toDailyRecommendationInfo(
            DailyRecommendation dailyRecommendation) {

        WeatherTemplate template = dailyRecommendation.getWeatherTemplate();

        List<String> keywords = template.getTemplateKeywords().stream()
                .map(tk -> tk.getKeyword().getName())
                .collect(Collectors.toList());

        return WeatherResDTO.DailyRecommendationInfo.builder()
                .id(dailyRecommendation.getId())
                .regionName(dailyRecommendation.getRegion().getName())
                .forecastDate(dailyRecommendation.getForecastDate())
                .weatherMessage(template.getMessage())
                .emoji(template.getEmoji())
                .keywords(keywords)
                .updatedAt(dailyRecommendation.getUpdatedAt())
                .build();
    }

    /**
     * 주간 추천 정보로 변환
     */
    public static WeatherResDTO.WeeklyRecommendations toWeeklyRecommendations(
            String regionName, List<DailyRecommendation> recommendations) {

        List<WeatherResDTO.DailyRecommendationInfo> dailyInfos = recommendations.stream()
                .map(WeatherConverter::toDailyRecommendationInfo)
                .collect(Collectors.toList());

        return WeatherResDTO.WeeklyRecommendations.builder()
                .regionName(regionName)
                .recommendations(dailyInfos)
                .build();
    }

    /**
     * 날씨 요약 정보 생성
     */
    public static WeatherResDTO.WeatherSummary toWeatherSummary(
            Double temperature, Double precipitation, String skyCondition) {

        TempCategory tempCategory = determineTempCategory(temperature);
        PrecipCategory precipCategory = determinePrecipCategory(precipitation);
        WeatherType weatherType = determineWeatherType(skyCondition);

        return WeatherResDTO.WeatherSummary.builder()
                .weather(getWeatherDescription(weatherType))
                .temperature(getTempDescription(tempCategory))
                .precipitation(getPrecipDescription(precipCategory))
                .actualTemp(temperature)
                .precipitationAmount(precipitation)
                .build();
    }

    // === 분류 메서드들 ===

    /**
     * 기온 카테고리 결정
     */
    private static TempCategory determineTempCategory(Double temperature) {
        if (temperature <= 10) {
            return TempCategory.CHILLY;  // 쌀쌀함
        } else if (temperature <= 20) {
            return TempCategory.COOL;    // 선선함
        } else if (temperature <= 27) {
            return TempCategory.MILD;    // 적당함
        } else {
            return TempCategory.HOT;     // 무더움
        }
    }

    /**
     * 강수량 카테고리 결정
     */
    private static PrecipCategory determinePrecipCategory(Double precipitation) {
        if (precipitation >= 10) {
            return PrecipCategory.HEAVY; // 강우 많음
        } else if (precipitation >= 1) {
            return PrecipCategory.LIGHT; // 약간 비옴
        } else {
            return PrecipCategory.NONE;  // 없음
        }
    }

    /**
     * 날씨 타입 결정
     */
    private static WeatherType determineWeatherType(String skyCondition) {
        if (skyCondition.contains("맑음")) {
            return WeatherType.CLEAR;
        } else if (skyCondition.contains("눈")) {
            return WeatherType.SNOW;
        } else {
            return WeatherType.CLOUDY;
        }
    }

    // === 매핑 메서드들 ===

    /**
     * 하늘상태 코드를 텍스트로 변환
     */
    private static String mapSkyCode(String skyCode) {
        return switch (skyCode) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "흐림";
        };
    }

    /**
     * 강수형태 코드를 텍스트로 변환
     */
    private static String mapPtyCode(String ptyCode) {
        return switch (ptyCode) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "빗방울";
            case "6" -> "빗방울눈날림";
            case "7" -> "눈날림";
            default -> "없음";
        };
    }

    // === 메시지 생성 메서드들 ===

    /**
     * 날씨 메시지 생성
     */
    private static String generateMessage(WeatherType weatherType, TempCategory tempCategory, PrecipCategory precipCategory) {
        String weather = getWeatherDescription(weatherType);
        String temp = getTempDescription(tempCategory);
        String precip = getPrecipDescription(precipCategory);

        if (precipCategory == PrecipCategory.NONE) {
            return String.format("%s 하늘과 %s 날씨예요. %s", weather, temp, getTempAdvice(tempCategory));
        } else {
            return String.format("%s 하늘과 %s 날씨예요. %s %s", weather, temp, precip, getPrecipAdvice(precipCategory));
        }
    }

    /**
     * 이모지 생성
     */
    private static String generateEmoji(WeatherType weatherType, TempCategory tempCategory) {
        if (weatherType == WeatherType.SNOW) {
            return "❄️";
        } else if (weatherType == WeatherType.CLEAR) {
            return tempCategory == TempCategory.HOT ? "☀️" : "☀️";
        } else {
            return "☁️";
        }
    }

    // === 설명 메서드들 ===

    private static String getWeatherDescription(WeatherType weatherType) {
        return switch (weatherType) {
            case CLEAR -> "맑은";
            case CLOUDY -> "흐린";
            case SNOW -> "눈이 내리는";
        };
    }

    private static String getTempDescription(TempCategory tempCategory) {
        return switch (tempCategory) {
            case CHILLY -> "쌀쌀한";
            case COOL -> "선선한";
            case MILD -> "적당한";
            case HOT -> "더운";
        };
    }

    private static String getPrecipDescription(PrecipCategory precipCategory) {
        return switch (precipCategory) {
            case NONE -> "비가 오지 않아요";
            case LIGHT -> "가벼운 비가 와요";
            case HEAVY -> "비가 많이 와요";
        };
    }

    private static String getTempAdvice(TempCategory tempCategory) {
        return switch (tempCategory) {
            case CHILLY -> "따뜻한 옷을 준비하세요.";
            case COOL -> "가벼운 겉옷을 챙기세요.";
            case MILD -> "야외 활동하기 좋은 날씨예요.";
            case HOT -> "수분 섭취를 충분히 하세요.";
        };
    }

    private static String getPrecipAdvice(PrecipCategory precipCategory) {
        return switch (precipCategory) {
            case LIGHT -> "우산을 챙기는 것이 좋겠어요.";
            case HEAVY -> "실내 데이트를 추천해요.";
            default -> "";
        };
    }
}