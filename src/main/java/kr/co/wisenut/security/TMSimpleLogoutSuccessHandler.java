package kr.co.wisenut.security;

import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;


/**
 * AOP 사용을 위한 스프링 시큐리티 커스텀 핸들러 구현.
 * TODO:구현 검증 필요 {@link SimpleUrlLogoutSuccessHandler}
 */
@Component("logoutSuccessHandler")
public class TMSimpleLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
}
