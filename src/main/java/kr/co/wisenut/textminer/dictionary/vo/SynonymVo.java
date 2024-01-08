package kr.co.wisenut.textminer.dictionary.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SynonymVo {
	
	private int synonymId;								// 동의어 ID
	private int entryId;								// 엔트리 ID
	private int dictionaryId;							// 사전 ID
	private String synonymContent;						// 동의어
	
}
