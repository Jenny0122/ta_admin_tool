package kr.co.wisenut;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import kr.co.wisenut.config.ClassifierProperties;
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.config.TMStorageProperties;
import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.textminer.common.service.StorageService;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({TMProperties.class, TMStorageProperties.class, ClassifierProperties.class})
public class TextMinerAdminToolsApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TextMinerAdminToolsApplication.class);
	}
	
	public static void main(String[] args) {
		// SpringApplication.run(TextMinerAdminToolsApplication.class, args);
        SpringApplication app = new SpringApplication(TextMinerAdminToolsApplication.class);
        app.addListeners(new ApplicationPidFileWriter()); // ApplicationPidFileWriter 설정
        app.run(args);
	}
	
	@Bean
    CommandLineRunner init(StorageService storageService) {
        final Logger logger = LoggerFactory.getLogger(getClass());

        return (args) -> {
            // command line arguments
            try {
                if (args != null) {
                    logger.info("commandline.args={}", Arrays.toString(args));
                }
            } catch (RuntimeException e) {
                logger.error("Failed to run application. " + e.getMessage());
                System.exit(0);
            }

            // storage(upload, download) initialize
            try {
                storageService.init();
                storageService.deleteExpired();
            } catch (StorageException e) {
                logger.error("Failed to run application. " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Cause " + e.getCause().getMessage());
                }
                System.exit(0);
            }
        };
    }
}
