package kr.co.wisenut.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@ComponentScan( basePackages = { "kr.co.wisenut.security"
							   , "kr.co.wisenut.textminer.batch.service"
							   , "kr.co.wisenut.textminer.collection.service"
							   , "kr.co.wisenut.textminer.common.service"
							   , "kr.co.wisenut.textminer.dashboard.service"
							   , "kr.co.wisenut.textminer.dictionary.service"
							   , "kr.co.wisenut.textminer.history.service"
							   , "kr.co.wisenut.textminer.model.service"
							   , "kr.co.wisenut.textminer.project.service"
							   , "kr.co.wisenut.textminer.resource.service"
							   , "kr.co.wisenut.textminer.result.service"
							   , "kr.co.wisenut.textminer.tesk.service"
							   , "kr.co.wisenut.textminer.user.service"
							   , "kr.co.wisenut.textminer.simulation.service"
							   , "kr.co.wisenut.textminer.schedule.service"
							   , "kr.co.wisenut.textminer.deploy.service"
							   })
@MapperScan( basePackages = { "kr.co.wisenut.textminer.batch.mapper"
	    					, "kr.co.wisenut.textminer.collection.mapper"
						    , "kr.co.wisenut.textminer.common.mapper"
						    , "kr.co.wisenut.textminer.dashboard.mapper"
						    , "kr.co.wisenut.textminer.dictionary.mapper"
						    , "kr.co.wisenut.textminer.history.mapper"
						    , "kr.co.wisenut.textminer.model.mapper"
						  	, "kr.co.wisenut.textminer.project.mapper"
						  	, "kr.co.wisenut.textminer.resource.mapper"
						  	, "kr.co.wisenut.textminer.result.mapper"
						  	, "kr.co.wisenut.textminer.task.mapper"
						  	, "kr.co.wisenut.textminer.user.mapper"
						  	, "kr.co.wisenut.textminer.simulation.mapper"
						  	, "kr.co.wisenut.textminer.schedule.mapper"
						  	, "kr.co.wisenut.textminer.deploy.mapper"
						  	, "kr.co.wisenut.textminer.autoqa.mapper"
							}
		   , sqlSessionFactoryRef = "sqlSessionFactory")
@Configuration
public class DataSourceConfig {
	
	@Value("${database.name}")
	private String databaseName;
	
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public DataSource dataSource() {
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}
	
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/" + databaseName + "/**/*.xml"));
        return sessionFactory.getObject();
	}
	
	@Bean
    public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	
	@Bean
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return transactionManager();
	}
}