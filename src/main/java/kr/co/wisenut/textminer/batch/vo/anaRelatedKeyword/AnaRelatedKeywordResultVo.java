package kr.co.wisenut.textminer.batch.vo.anaRelatedKeyword;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaRelatedKeywordResultVo {
	
	private LinkedHashMap<String, LinkedHashMap<String, Object>> keywords;

	
	@Override
	public String toString() {
		return "AnaRelatedKeywordResultVo [keywords=" + keywords + "]";
	}
	
}