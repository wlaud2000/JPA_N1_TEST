package com.study.demo.testplayground.domain.member.converter;

import com.study.demo.testplayground.domain.member.dto.response.MemberResDTO;
import com.study.demo.testplayground.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberConverter {

    // Member 엔티티 -> ResponseDTO
    public static MemberResDTO.MemberResponseDTO toMemberResponseDTO(Member member) {
        return MemberResDTO.MemberResponseDTO.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .postCount(member.getPosts().size())
                .commentCount(member.getComments().size())
                .build();
    }

    // Member 리스트 -> ListResponseDTO
    public static MemberResDTO.MemberListResponseDTO toMemberListResponseDTO(List<Member> members) {
        List<MemberResDTO.MemberResponseDTO> memberList = members.stream()
                .map(MemberConverter::toMemberResponseDTO)
                .collect(Collectors.toList());

        return MemberResDTO.MemberListResponseDTO.builder()
                .members(memberList)
                .totalCount(members.size())
                .message("회원 목록 조회 완료! 콘솔에서 SQL 쿼리 개수를 확인하세요 🔥")
                .build();
    }
}
