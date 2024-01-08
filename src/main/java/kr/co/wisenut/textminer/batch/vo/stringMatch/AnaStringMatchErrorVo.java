package kr.co.wisenut.textminer.batch.vo.stringMatch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaStringMatchErrorVo {
	
	private int code;
    private String message;
    private AnaStringMatchErrorResultVo result;
    
}