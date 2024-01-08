package kr.co.wisenut.textminer.batch.vo.anaKeyword;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaKeywordResultKeywordVo {
	
	private String word;
    private int score;
    private int scaled_score;
    private int count;
    private List<String> synonyms;
    private String tag;
    
}