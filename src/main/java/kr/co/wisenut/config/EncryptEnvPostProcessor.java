package kr.co.wisenut.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import kr.co.wisenut.util.AesCryptoUtil;

public class EncryptEnvPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		
		Logger logger = LoggerFactory.getLogger(this.getClass());
		
		Properties props = new Properties();
		try {
			// 인코딩된 DB 계정 패스워드를 복호화하여 접속할 수 있도록 한다.
			props.put("spring.datasource.hikari.password", AesCryptoUtil.decryptionDb(environment.getProperty("spring.datasource.hikari.password")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		environment.getPropertySources().addFirst(new PropertiesPropertySource("dbPwd", props));
	}

}
