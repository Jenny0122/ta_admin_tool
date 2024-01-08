package kr.co.wisenut.textminer.batch.vo.preprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreprocessResultVo {

	private HashMap<String, String> speakers;
	private List<Map<String, String>> conversation;
	private PreprocessResultCleansingOptionVo cleansing_option;
	@Override
	public String toString() {
		return "PreprocessResultVo [speakers=" + speakers + ", conversation=" + conversation + ", cleansing_option="
				+ cleansing_option + "]";
	}

}
