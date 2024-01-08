package kr.co.wisenut.textminer.batch.vo.stringMatch;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnaStringMatchResultMatchInfoVo {
	
    private AnaStringMatchResultMatchInfoPatternVo pattern;
    private int startPos;
    private int endPos;
    private String text;
}