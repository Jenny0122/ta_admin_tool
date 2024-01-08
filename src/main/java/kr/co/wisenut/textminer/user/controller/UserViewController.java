package kr.co.wisenut.textminer.user.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.user.service.UserService;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.textminer.user.vo.UserVo;

@Controller
public class UserViewController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

    protected UserDetailsManager userDetailsManager;
	
    @Autowired
    private UserService userService;
    
    @Autowired
    TMProperties tmProperties;
    
    @Autowired
    SessionRegistry sessionRegistry;
    
    @GetMapping("/login")
    public String login(Model model, @RequestParam @ModelAttribute Map<String, Object> requestParams, @AuthenticationPrincipal UserVo user) {
        if (user != null) {
            return "redirect:/dashboard";
        }

        return "user/login";//"spring_security_example_login";
    }
    
    /**
     * 로그아웃 or 세션 타임아웃(application.properties#server.servlet.session.timeout 설정값) or
     * 서버 재기동 등으로 사용자 세션이 invalidate 된 경우 진입
     *
     * @param model
     * @return
     */
    @GetMapping("/logout/invalid")
    public String sessionInvalid(Model model, HttpServletRequest request, @AuthenticationPrincipal TmUser user) {
        String referer = request.getHeader("Referer");
        logger.debug("'/login/invalid' Referer={}, {}", referer, model);

        // 인증된 사용자가 접근했을 경우 --> /dashboard 로 이동
        if (user != null) {
//            logger.trace("tm-user authentication not invalidate yet. redirect to dashboard. user(username={}, remoteHost={})", user.toStringForLogging());
            return "redirect:/dashboard";
        }

        // '/logout' 이 아닌 경우 : 서버 재구동 등 --> /login 로 이동
        if (referer == null || referer.contains("/login")) {
            logger.debug("session not invalidated by '/logout'. Referer={}", referer);
            return "redirect:/login";
        }

        // '/logout' 시
        logger.debug("session invalidated by '/logout'. request(remoteHost={}, remotePort={}, referer={})",
                request.getRemoteHost(), request.getRemotePort(), request.getHeader("referer"));
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            ServletContext c = session.getServletContext();
            int timeout = c.getSessionTimeout(); // min
            logger.debug("session(id={}, timeout={}min)", session.getId(), timeout);
        }

        logger.info("tm-user session invalidated. request({} {}, remoteHost={}, sessionId={})",
                request.getMethod(), request.getRequestURI(), request.getRemoteHost(), request.getRequestedSessionId());

        return "user/sessionInvalid";
    }

    /**
     * 동일한 계정으로 다른 브라우저 or 호스트에서 접속한 경우, 기존 세션은 expire 된다.
     *
     * @param model
     * @return
     * @see kr.wisenut.manager.security.WebSecurityConfig#configure(HttpSecurity)
     * htttp.sessionManagement()..maxSessionsPreventsLogin(false); 동일 아이디로 다른곳에서 로그인 할 경우 세션 만료
     * - true 일 경우 로그인 불가 (기존 세션 우선)
     */
    @GetMapping("/logout/expired")
    public String sessionExpired(Model model, HttpServletRequest request, @AuthenticationPrincipal UserVo user) {
        if (user != null) {
            return "redirect:/dashboard";
        }
        logger.info("tm-user session expired. request({} {}, remoteHost={}, sessionId={})",
                request.getMethod(), request.getRequestURI(), request.getRemoteHost(), request.getRequestedSessionId());

        return "user/sessionExpired";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model, @AuthenticationPrincipal UserVo user) {
        if (user != null) {
            return "redirect:/dashboard";
        }

        return "user/register";
    }
    
    /**
     * 사용자 관리 페이지 : 관리자 계정만 접근
     * - spring security 설정으로 가능 : .authorizeRequests().antMatchers().hasAuthority("ADMIN")
     *
     * @param model
     * @return
     */
    @GetMapping("/users")
    public String users(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
        List<UserVo> users = userService.getUserList();
        model.addAttribute("users", users);
        
        List<TmUser> onlineUsers = sessionRegistry.getAllPrincipals().stream()
        		.filter(o -> !sessionRegistry.getAllSessions(o, false).isEmpty())
                .map(principal -> (TmUser) principal)
                .collect(Collectors.toList());

        List<String> onlineUserNames =  onlineUsers.stream()
                .map(u -> u.getUsername())
                .collect(Collectors.toList());
        
        model.addAttribute("onlineUsers", onlineUserNames);

//        String onlineUserWithHost = onlineUsers.stream()
//                .map(u -> u.getUserId() + " at " + u.getRemoteHost())
//                .collect(Collectors.joining(",", "[", "]"));
//
//        logger.info("tm-users currently logged in={}", onlineUserWithHost);

        return "user/users";
    }
    
    @RequestMapping("/user/mypage")
    public String myPage(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        model.addAttribute("userId", user.getUsername());
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("userEmail", user.getUserEmail());

        // 서버 연결설정 관련하여 로직 변경 필요
        // 수협은 단일서버라 문제가 없지만,
        // 신한의 경우 서버가 여러개라 각 서버에 대한 설정정보를 불러오는 방법에 대한 정의가 필요한 상황
        model.addAttribute("tmProperties", tmProperties);

        if (model.containsAttribute("msg")) {
            String msg = (String) model.getAttribute("msg");
            logger.debug("redirect.msg={}", msg);
            model.addAttribute("msg", msg);
        }

        return "user/mypage";
    }
}
