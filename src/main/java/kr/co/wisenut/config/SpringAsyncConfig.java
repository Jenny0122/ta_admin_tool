package kr.co.wisenut.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

public class SpringAsyncConfig implements AsyncConfigurer {
	
    @Bean(name="threadPoolTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 미리 스레드 생성, 대기
        executor.setMaxPoolSize(5); // corePoolSize 꽉찰 경우 스레드 생성 단위
        executor.setQueueCapacity(10); // maxPoolSize 꽉찰 경우, 큐에 등록하여 순차 수행, queueCapacity 꽉차면 예외 발생
        executor.setThreadNamePrefix("async-job-max5-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}
