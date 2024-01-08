package kr.co.wisenut.textminer.user.vo;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;
import lombok.Setter;

/*
 *  해당 클래스는 Spring Security에서 사용할 User 클래스로,
 *  관리도구 데이터 CURD 로직에서 사용하는 UserVo 클래스와는 별개로 사용됨.
*/
@Getter
@Setter
public class TmUser extends User {

	// Table Column
	private String userName;		// 사용자명
	private String userEmail;		// 이메일
	private String useYn;			// 사용여부
	private String userIp;			// 접속IP
	
	// 생성자
	public TmUser( String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked
					 , Collection<? extends GrantedAuthority> authorities
					 , String userName, String userEmail ) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		
		this.userName = userName;
		this.userEmail = userEmail;
		this.useYn = (enabled == true ? "Y" : "N");
	}

}
