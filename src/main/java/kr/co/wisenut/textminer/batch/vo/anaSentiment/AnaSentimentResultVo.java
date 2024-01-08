package kr.co.wisenut.textminer.batch.vo.anaSentiment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSentimentResultVo {
	
	private String id;
	private String label;
	private List<AnaSentimentResultPredictVo> predict;
	
	private List<AnaSentimentResultFailedVo> text;
	private String description;
	
}