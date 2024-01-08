package kr.co.wisenut.config;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import kr.co.wisenut.textminer.common.TextMinerConstants;

/**
 * Classifier 설정값
 */
@ConfigurationProperties(prefix = "classifier")
@Data
public class ClassifierProperties {
	
	// Auto classify IP & Port
	// - listener IP & Port
    private String autoListenerHost;
    private int autoListenerPort;
    
    // - trainer IP & Port
    private String autoTrainerHost;
    private int autoTrainerPort;
    
    // - analyzer IP & Port
    private String autoAnalyzerHost;
    private int autoAnalyzerPort;
    
	// Emotion classify IP & Port
	// - listener IP & Port
    private String emotionListenerHost;
    private int emotionListenerPort;
    
    // - trainer IP & Port
    private String emotionTrainerHost;
    private int emotionTrainerPort;
    
    // - analyzer IP & Port
    private String emotionAnalyzerHost;
    private int emotionAnalyzerPort;
    
    // Summarization IP & Port
    private String summaryHost;
    private int summaryPort;
    
    // Related Keyword Extraction IP & Port
    private String relatedHost;
    private int relatedPort;
    
    // Keyword Extraction IP & Port
    private String keywordHost;
    private int keywordPort;
    
    // Emotion Preprocess IP & Port
    private String emotionPreprocessHost;
    private int emotionPreprocessPort;
    
    // Summary Preprocess IP & Port
    private String summaryPreprocessHost;
    private int summaryPreprocessPort;
    
    // String Matcher IP & Port
    private String stringMatcherHost;
    private int stringMatcherPort;
}
