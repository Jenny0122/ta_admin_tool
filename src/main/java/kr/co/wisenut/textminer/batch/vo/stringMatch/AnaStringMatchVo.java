package kr.co.wisenut.textminer.batch.vo.stringMatch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaStringMatchVo {
	
	private int code;
    private String message;
    private AnaStringMatchResultVo result;
    private String description;
    
	@Override
	public String toString() {
		return "AnaStringMatchVo [code=" + code + ", message=" + message + ", result=" + result + ", description="
				+ description + "]";
	}
}