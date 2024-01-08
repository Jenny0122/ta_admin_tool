package kr.co.wisenut.textminer.batch.vo.anaClass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaClassResultPredictVo {
	
	private String label;
    private double confidence;
    private double score;
    
	@Override
	public String toString() {
		return "AnaClassResultPredictVo [label=" + label + ", confidence=" + confidence + ", score=" + score + "]";
	}
	
    
}