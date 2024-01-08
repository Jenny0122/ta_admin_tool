package kr.co.wisenut.textminer.user.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.co.wisenut.textminer.user.vo.UserVo;

public interface UserMapper {
	
	public Map<String, Object> getSsoUserInfo(String userId);		// SSO ID check
	
	public UserVo getUserInfo(String userId);						// 로그인 (사용자 정보 조회)
	public int checkDupUserId(UserVo user);							// ID 중복검사
	public List<UserVo> getUserList();								// 사용자 목록 불러오기
	
	public int insertUserInfo(UserVo user);							// 회원가입 및 사용자 추가
	public int updateUserInfo(UserVo user);							// 사용자 정보변경
	public int updateUserEnabled(Map<String, Object> paramMap);		// 사용자 활성화/비활성화 처리
	public int deleteUserInfo(UserVo user);							// 사용자 삭제
	
}
