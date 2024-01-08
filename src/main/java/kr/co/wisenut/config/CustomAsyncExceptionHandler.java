package kr.co.wisenut.config;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		// TODO Auto-generated method stub
		logger.error(ex.toString());
        logger.error(method.getName());
        
        for (Object param : params) {
            System.err.println(param);
        }
	}
	
}
