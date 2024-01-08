package kr.co.wisenut.textminer.batch.vo.AnaSummarize;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaSummarizeVo {
	
	private int code;
    private String message;
    private AnaSummarizeResultVo result;
    private String description;
    
    
	@Override
	public String toString() {
		return "AnaRelatedKeywordVo [code=" + code + ", message=" + message + ", result=" + result + ", description="
				+ description + "]";
	}
}