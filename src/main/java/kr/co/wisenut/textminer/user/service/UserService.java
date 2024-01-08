package kr.co.wisenut.textminer.user.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.user.mapper.UserMapper;
import kr.co.wisenut.textminer.user.vo.UserVo;

@Service
public class UserService{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserMapper userMapper;
	
	public List<UserVo> getUserList() {
		return userMapper.getUserList();
	}
	
	// SSO 연동
	public Map<String, Object> checkUser (String userId) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			Map<String, Object> ssoMap = userMapper.getSsoUserInfo(userId);
			UserVo user = userMapper.getUserInfo(userId);
			
			if (ssoMap == null) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사용할 수 없는 계정입니다.");
			} else if (user != null){
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "이미 등록된 계정입니다.");
			} else {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사용 가능");
				
				resultMap.put("userName", ssoMap.get("userName"));
				resultMap.put("userEmail", ssoMap.get("userEmail"));
				resultMap.put("hlfcDscd", ssoMap.get("hlfcDscd"));
				resultMap.put("blntBrno", ssoMap.get("blntBrno"));
			}
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 사용자 정보 조회
	public UserVo getUserInfo (String userId) {
		
		UserVo user = new UserVo();
		
		try {
			user = userMapper.getUserInfo(userId);
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return user;
	}
	
	// 회원가입 및 사용자 등록
	public Map<String, Object> insertUserInfo(UserVo user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 작업구분에 따른 결과 및 에러 메시지 설정
		String resultMsg = createJobMsg(user.getJobDiv(), user.getUseYn());
		
		try {

			// SSO 연동 확인
			Map<String, Object> ssoMap = userMapper.getSsoUserInfo(user.getUserId());
			// ID 중복검사
			int check = userMapper.checkDupUserId(user);
			
			if (ssoMap != null && check == 0) {
				
				
				// 회원가입
				// - password encoding
				user.setUserPw(passwordEncoder.encode(user.getUserPw()));
				
				// - SSO Data Insert
				user.setUserName(ssoMap.get("userName").toString());
				user.setUserEmail(ssoMap.get("userEmail")==null?"-":ssoMap.get("userEmail").toString());
				user.setHlfcDscd(ssoMap.get("hlfcDscd")==null?" ":ssoMap.get("hlfcDscd").toString());
				user.setBlntBrno(ssoMap.get("blntBrno")==null?" ":ssoMap.get("blntBrno").toString());
				
				int result = userMapper.insertUserInfo(user);
				
				// 처리결과에 따른 리턴값 생성
				if (result > 0) {
					resultMap.put("result", "S");
					resultMap.put("resultMsg", resultMsg + " 작업이 완료되었습니다.");
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", resultMsg + " 작업을 실패하였습니다.");
				}
			} else if (ssoMap == null) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사용할 수 없는 계정입니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "이미 등록된 계정입니다.");
			}
		} catch (NullPointerException e) {
			logger.error(resultMsg + " 작업 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(resultMsg + " 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 사용자 정보 변경
	public Map<String, Object> updateUserInfo(UserVo user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {

			// password encoding
			if (user.getUserPw() != null) {
				if (!user.getUserPw().equals("")) {
					user.setUserPw(passwordEncoder.encode(user.getUserPw()));
				}
			} 
			
			int result = userMapper.updateUserInfo(user);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사용자 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사용자 수정 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("사용자 수정 작업 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("사용자 수정 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 사용자 활성화/비활성화 처리
	public Map<String, Object> updateUserEnabled(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {

			int result = userMapper.updateUserEnabled(paramMap);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "계정 활성화 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "계정 활성화 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("계정 활성화 작업 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("계정 활성화 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 회원삭제
	public Map<String, Object> deleteUserInfo(UserVo user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			int result = userMapper.deleteUserInfo(user);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "계정 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "계정 삭제 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("계정 삭제 작업 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("계정 삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
		
	// 작업메시지 생성
	public String createJobMsg(String jobDiv, String useYn) {
		
		StringBuffer returnMsg = new StringBuffer();
		
		switch(jobDiv) {
		case "1" :
			returnMsg.append("회원가입");
			break;
		case "2" :
			returnMsg.append("계정 등록");
			break;
		}
		
		return returnMsg.toString();
	}
}
