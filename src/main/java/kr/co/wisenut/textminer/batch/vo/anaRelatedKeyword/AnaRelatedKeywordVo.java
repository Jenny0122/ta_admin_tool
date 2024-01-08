package kr.co.wisenut.textminer.batch.vo.anaRelatedKeyword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaRelatedKeywordVo {
	
	private int code;
    private String message;
    private AnaRelatedKeywordResultVo result;
    private String description;
    
    
	@Override
	public String toString() {
		return "AnaRelatedKeywordVo [code=" + code + ", message=" + message + ", result=" + result + ", description="
				+ description + "]";
	}
}