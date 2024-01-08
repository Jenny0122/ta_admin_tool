package kr.co.wisenut.textminer.batch.vo.stringMatch;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnaStringMatchResultVo {
	
	private boolean ignoreOrder;
	private List<AnaStringMatchResultMatchVo> matchInfoList;
	private List<AnaStringMatchResultMatchInfoPatternVo> patterns;
    
}