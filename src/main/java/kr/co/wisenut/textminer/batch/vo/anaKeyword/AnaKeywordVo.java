package kr.co.wisenut.textminer.batch.vo.anaKeyword;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaKeywordVo {
	
	private int code;
    private String message;
    private AnaKeywordResultVo result;
    private String description;
	
    @Override
	public String toString() {
		return "AnaKeywordVo [code=" + code + ", message=" + message + ", result=" + result + ", description="
				+ description + "]";
	}
}