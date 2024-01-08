package kr.co.wisenut.textminer.batch.vo.stringMatch;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaStringMatchResultMatchInfoPatternVo {
	
    private String label;
    private List<String> pattern;
}