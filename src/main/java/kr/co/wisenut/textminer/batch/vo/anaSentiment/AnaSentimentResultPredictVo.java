package kr.co.wisenut.textminer.batch.vo.anaSentiment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSentimentResultPredictVo {
	
	private String label;
    private double confidence;
    private double score;
	
}