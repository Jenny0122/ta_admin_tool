package kr.co.wisenut.textminer.batch.vo;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResultErrorVo {
	
	
	private int code;
	private String message;
	Map<String, Object> result;
    
	@Override
	public String toString() {
		return "ApiResultErrorVo [code=" + code + ", message=" + message + ", result=" + result + "]";
	}
}