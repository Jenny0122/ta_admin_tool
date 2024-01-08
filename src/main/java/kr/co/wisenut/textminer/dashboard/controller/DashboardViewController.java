package kr.co.wisenut.textminer.dashboard.controller;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.textminer.user.vo.UserVo;

@Controller
public class DashboardViewController {

	private final Map<String, String> serverInfo;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    public DashboardViewController(TMProperties tmProperties) {
        String localhostAddress = "127.0.0.1";

        // get local host address
        if (isLocalHost(localhostAddress)) {
            try {
                localhostAddress = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.trace("failed to localHostAddrress. {}", e.toString());
            }
        }
        Map<String, String> serverInfoMap = new HashMap<>();
/*
        // mongoDB connection properties to show

        String mongoUri = mongoProperties.getUri();
        String mongoHost = mongoProperties.getHost();
        String mongoPort = String.valueOf(mongoProperties.getPort() == null ? 27017 : mongoProperties.getPort());
        String mongoDatabaseName = mongoProperties.getDatabase();

        // application.properties에 mongo 정보를 입력안해서 기본 설정 (127.0.0.1:27017) 사용한 경우
        if (isLocalHost(mongoHost)) {
            logger.warn("please set spring.data.mongodb host or uri explicitly in properties file.");
            mongoHost = localhostAddress;
        }
        if (mongoDatabaseName == null || mongoDatabaseName.isEmpty()) {
            logger.error("please set spring.data.mongodb database name explicitly in properties file.");
            mongoDatabaseName = "invalid database name";
        }
        if (mongoUri == null || mongoUri.isEmpty()) {
            // set default mogno connection rui
            mongoUri = String.format("mongodb://%s:%s/%s", mongoHost, mongoPort, mongoDatabaseName);
        }

        serverInfoMap.put("mongo_uri", mongoUri);
        serverInfoMap.put("mongo_host", mongoHost);
        serverInfoMap.put("mongo_port", mongoPort);
        serverInfoMap.put("mongo_database", mongoDatabaseName);
*/
        // tm-server connection properties to show
        String tmHost = tmProperties.getCoreHost();
        // if tm-server run at same server
        if ("localhost".equalsIgnoreCase(tmHost) || "127.0.0.1".equalsIgnoreCase(tmHost)) {
            try {
                tmHost = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.trace("{}", e.toString());
            }
        }
        serverInfoMap.put("tm_host", tmHost);
        serverInfoMap.put("tm_port", String.valueOf(tmProperties.getCorePort()));

        serverInfo = Collections.unmodifiableMap(serverInfoMap);
    }
    
	@GetMapping(value = {"/"})
    public String home(@RequestHeader HttpHeaders headers) {
        return "redirect:/dashboard";
    }
    
	@GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        model.addAllAttributes(serverInfo);
        return "dashboard";
    }
	
    private boolean isLocalHost(String host) {
        return host == null || host.isEmpty() || "127.0.0.1".equalsIgnoreCase(host) || "localhost".equalsIgnoreCase(host);
    }
}
