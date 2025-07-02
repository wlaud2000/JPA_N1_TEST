package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
