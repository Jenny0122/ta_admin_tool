package kr.co.wisenut.textminer.batch.vo.anaClass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaClassResultThresholdVo {
	
	private String thresholdOption;
    private double thresholdMultiplier;
    
	@Override
	public String toString() {
		return "AnaClassResultThresholdVo [thresholdOption=" + thresholdOption + ", thresholdMultiplier="
				+ thresholdMultiplier + "]";
	}
	
    
}