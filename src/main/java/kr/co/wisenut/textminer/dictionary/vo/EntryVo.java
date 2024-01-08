package kr.co.wisenut.textminer.dictionary.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryVo {
	
	private int entryId;								// 엔트리 ID
	private int dictionaryId;							// 사전 ID
	private String entryContent;						// 엔트리
	private String synonyms;							// 동의어 (구분자 : ';')

	// 엔트리 중복검사를 위한 변수
	private String dictionaryType;						// 사전 타입
	private String dictionarySharedYn;					// 공통 사전 여부
}
