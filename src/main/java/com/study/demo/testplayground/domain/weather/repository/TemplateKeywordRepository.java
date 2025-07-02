package com.study.demo.testplayground.domain.weather.repository;

import com.study.demo.testplayground.domain.weather.entity.TemplateKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateKeywordRepository extends JpaRepository<TemplateKeyword, Long> {
}
