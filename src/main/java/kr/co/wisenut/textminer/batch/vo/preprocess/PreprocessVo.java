package kr.co.wisenut.textminer.batch.vo.preprocess;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreprocessVo {

	private PreprocessResultVo result;
	private int code;
	private String description;
	private String message;
	@Override
	public String toString() {
		return "PreprocessVo [result=" + result.toString() + ", code=" + code + ", description=" + description + ", message="
				+ message + "]";
	}
}
