package kr.co.wisenut.textminer.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.service.UserService;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.textminer.user.vo.UserVo;

@RestController
public class UserRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TMProperties tmProperties;

	@Autowired
	private UserService userService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;

	// SSO 연동 및 계정 사용가능여부 체크
	@GetMapping("/user/checkUser/{userId}")
	public Map<String, Object> checkUser(@PathVariable String userId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = userService.checkUser(userId);
		} catch (Exception e) {
			logger.error("tm-user checkUser failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
	@PostMapping("/user/register")
	public Map<String, Object> register(UserVo newUser
									  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
									  , HttpServletRequest request) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			newUser.setCreUser(user.getUsername());
			newUser.setModUser(user.getUsername());
			
			resultMap = userService.insertUserInfo(newUser);
			
			// 등록 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_USER_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사용자 등록 ( 등록계정 : " + newUser.getUserId() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user register failed. {}", e.getMessage());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "서버에서 오류가 발생하였습니다.\n관리자에게 문의바랍니다.");
		}

		logger.info("tm-user registered. {}", newUser.getUserId());

		return resultMap;
	}

	@GetMapping("/user/getUserInfo")
	public UserVo getUserInfo(String userId, HttpServletRequest request) {

		UserVo user = new UserVo();

		try {
			user = userService.getUserInfo(userId);
		} catch (Exception e) {
			logger.error("tm-user getUserInfo failed. {}", e.getMessage());
		}

		return user;
	}
	
	@PostMapping("/user/updateUser")
	public Map<String, Object> updateUser(UserVo modifyUser
										, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
										, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			modifyUser.setModUser(user.getUsername());
			
			resultMap = userService.updateUserInfo(modifyUser);
			
			// 변경 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_USER_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사용자 정보변경 ( 변경계정 : " + modifyUser.getUserId() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user register failed. {}", e.getMessage());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "서버에서 오류가 발생하였습니다.\n관리자에게 문의바랍니다.");
		}

		logger.info("tm-user updated. {}", modifyUser.getUserId());
		
		return resultMap;
	}
	
	@PostMapping("/user/updateUserEnabled")
	public Map<String, Object> updateUserEnabled(@RequestBody Map<String, Object> paramMap
			, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
			, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("modUser", user.getUsername());
			
			resultMap = userService.updateUserEnabled(paramMap);

			// 활성화/비활성화 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_USER_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사용자 사용여부 '" + paramMap.get("useYn").toString() + "' 변경 ( 변경계정 : " + paramMap.get("userId").toString() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user register failed. {}", e.getMessage());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "서버에서 오류가 발생하였습니다.\n관리자에게 문의바랍니다.");
		}
		
		logger.info("tm-user updated. {}", paramMap.get("userId"));
		
		return resultMap;
	}
	
	@PostMapping("/user/deleteUser")
	public Map<String, Object> deleteUser(UserVo deleteUser
			, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
			, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = userService.deleteUserInfo(deleteUser);
			
			// 삭제 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_USER_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사용자 삭제 ( 삭제계정 : " + deleteUser.getUserId() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user register failed. {}", e.getMessage());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "서버에서 오류가 발생하였습니다.\n관리자에게 문의바랍니다.");
		}
		
		logger.info("tm-user updated. {}", deleteUser.getUserId());
		
		return resultMap;
	}
}
