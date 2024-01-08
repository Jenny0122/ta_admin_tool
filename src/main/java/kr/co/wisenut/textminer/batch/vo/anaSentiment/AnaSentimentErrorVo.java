package kr.co.wisenut.textminer.batch.vo.anaSentiment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSentimentErrorVo {
	
	private int code;
    private String message;
    private AnaSentimentResultVo result;
    private AnaSentimentThresholdVo threshold;
    private String description;
    
}