package kr.co.wisenut.security;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.mapper.ActionHistoryMapper;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.mapper.UserMapper;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.textminer.user.vo.UserVo;

@Service
public class LoginIdPwValidator implements UserDetailsService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new SHA512PasswordEncoder();
    }
    
    @Autowired
    private UserMapper mapper;
    
    @Autowired
    private ActionHistoryMapper actionHistoryMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        
    	// User 객체에서 사용할 변수(default)
    	boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
    	
    	UserVo user = mapper.getUserInfo(userId);
        
        if (user == null) { return null; }			// 계정유무 체크
        if (!user.isEnabled()) { return null; }		// 계정 활성화 여부 체크
        
        return new TmUser(userId
        					, user.getUserPw()
        					, enabled
        					, accountNonExpired
        					, credentialsNonExpired
        					, accountNonLocked
        					, authorities(user)
        					, user.getUserName()
        					, user.getUserEmail()
        					);
    }
    
    // 권한 부여하기
    private static Collection authorities(UserVo user){
    	 Collection authorities = new ArrayList<>();
         
    	 authorities.add(new SimpleGrantedAuthority(user.getUserAuth()));
         
    	 return authorities;
    }
}
