package kr.co.wisenut.textminer.batch.vo.anaClass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaClassVo {
	
	private int code;
    private String message;
    private AnaClassResultVo result;
    private String description;
	
    @Override
	public String toString() {
		return "AnaClassVo [code=" + code + ", message=" + message + ", result=" + result.toString() + ", description="
				+ description + "]";
	}
}