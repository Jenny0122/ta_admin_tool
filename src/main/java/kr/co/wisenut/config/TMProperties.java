package kr.co.wisenut.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TM Server manager 의 커스텀 설정 정의 밑 로더.
 * TODO(wisnt65): validation?
 */
@ConfigurationProperties(prefix = "tminterface")
@Data
public class TMProperties {

    /**
     * Text-miner v3 Server host(ip)
     */
    private String coreHost;

    /**
     * Text-miner v3 Server port (default=7777)
     */
    private int corePort;
}
