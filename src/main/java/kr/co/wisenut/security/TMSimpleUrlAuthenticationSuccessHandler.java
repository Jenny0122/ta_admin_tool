package kr.co.wisenut.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.mapper.ActionHistoryMapper;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.mapper.UserMapper;
import kr.co.wisenut.textminer.user.vo.UserVo;

/**
 * AOP 사용을 위한 스프링 시큐리티 커스텀 핸들러 구현.
 * TODO:구현 검증 필요 {@link SimpleUrlLogoutSuccessHandler}
 */
@Component("authenticationSuccessHandler")
public class TMSimpleUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
    @Autowired
    private UserMapper mapper;
    
    @Autowired
    private ActionHistoryMapper actionHistoryMapper;
    
	@Override
	public void onAuthenticationSuccess( HttpServletRequest request
									   , HttpServletResponse response
									   , Authentication authentication) throws IOException, ServletException {
		
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
    	UserVo user = mapper.getUserInfo(userDetails.getUsername());
    	
        if (user == null) { response.sendRedirect("/login"); }			// 계정유무 체크
        if (!user.isEnabled()) { response.sendRedirect("/login"); }		// 계정 활성화 여부 체크
    	
        ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
        actionHistoryVo.setActionUser(user.getUserId());
        actionHistoryVo.setResourceId("0");
        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_USER_LOGIN);
        actionHistoryVo.setActionMsg("사용자 (" + user.getUserId() + ") 관리도구 로그인");

        // 접속자 IP 추가
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        if (details.getRemoteAddress().equals("0:0:0:0:0:0:0:1")) {
        	// localhost는 127.0.0.1로 변경
        	actionHistoryVo.setUserIp("127.0.0.1");
        } else {
        	actionHistoryVo.setUserIp(details.getRemoteAddress());
        }
        
        actionHistoryMapper.insertActionHistory(actionHistoryVo);
		
        response.sendRedirect("/dashboard");
	}
}
