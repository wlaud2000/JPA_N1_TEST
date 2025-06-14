package com.study.demo.testplayground.domain.member.repository;

import com.study.demo.testplayground.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
