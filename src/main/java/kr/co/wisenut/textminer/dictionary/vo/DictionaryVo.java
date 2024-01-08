package kr.co.wisenut.textminer.dictionary.vo;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictionaryVo extends ImportProgressVo {
	
	private int dictionaryId;								// 사전ID
	private String dictionaryName;							// 사전이름
	private String dictionaryType;							// 사전구분
	private String dictionaryDesc;							// 사전설명
	private String dictionaryOwner;							// 소유자
	private String dictionarySharedYn;						// 공통사전 여부
	private int dictionaryEntryCnt;							// 사전엔트리건수
	private String creDt;									// 등록일자
	private String creUser;									// 등록자ID
	private String modDt;									// 수정날짜
	private String modUser;									// 수정자ID
	
	private String uploadStatus;							// 업로드 상테
	
	private String role;									// 로그인 아이디의 권한
	
	public String getLabel() {
		switch (this.dictionaryType) {
			case TextMinerConstants.DICTIONARY_TYPE_WHITE :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_WHITE;
			case TextMinerConstants.DICTIONARY_TYPE_BLACK :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_BLACK;
			case TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_BLACK_PATTERN;
			case TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_BLACK_WORD;
			case TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_SPLIT_SENT_WORD;
			case TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN :
				return TextMinerConstants.DICTIONARY_TYPE_NAME_GROUP_PATTERN;
			default :
				return TextMinerConstants.COMMON_BLANK;
		}
	}
}
