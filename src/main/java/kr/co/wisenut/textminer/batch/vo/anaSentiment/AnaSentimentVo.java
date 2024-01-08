package kr.co.wisenut.textminer.batch.vo.anaSentiment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSentimentVo {
	
	private int code;
    private String message;
    private List<AnaSentimentResultVo> result;
    private AnaSentimentThresholdVo threshold;
    private String description;
    
}