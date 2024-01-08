package kr.co.wisenut.textminer.batch.vo.anaClass;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaClassResultVo {
	
	private String label;
    private List<AnaClassResultPredictVo> predict;
    private String text;
    private AnaClassResultThresholdVo threshold;
    
	@Override
	public String toString() {
		return "AnaClassResultVo [label=" + label + ", predict=" + predict + ", text=" + text + ", threshold="
				+ threshold.toString() + "]";
	}
	
}