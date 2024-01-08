package kr.co.wisenut.textminer.batch.vo.AnaSummarize;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSummarizeResultVo {
	
	private List<String> summary;

	
	@Override
	public String toString() {
		return "AnaRelatedKeywordResultVo [summary=" + summary + "]";
	}
	
}