package kr.co.wisenut.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * TODO(wisnt65) TM_Core 와 HTTP API 통신 관련 설정 (UriComponents+RestTemplate) 전부 여기서 하는게 나을듯. 상속? 구현? 빈???
 */
@Configuration
public class TMRestTemplateConfig {
//    @Autowired TextminerConfig textminerConfig;
//    UriComponentsBuilder base = UriComponentsBuilder.newInstance()
//            .scheme("http").host(textminerConfig.coreHost).port(textminerConfig.corePort);
//    public UriComponentsBuilder
//            PROJECT = base.cloneBuilder().path("/project/{project_id:[\\d]{0,5}}"),
//            TASK = base.cloneBuilder().path("/project/{project_id:[\\d]{0,5}}/task/{task_id:[\\d]{0,5}}");

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofMinutes(1)).build();
    }


    /*
     * # ParameterizedTypeReference : restTemplate 을 이용해서 responseBody(String)를 object 로 추출할때 타입지정용도로 사용
     *  - String, manager.model.Collection 같은 단순타입은 getForObject(... String.class) 로 사용하는게 편리.
     *  - List<String>, List<Map<String, String>> 같이 generic 포함하고 있을 경우 타입리퍼런스 도움 필요
     */


}
