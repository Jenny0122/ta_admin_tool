package kr.co.wisenut.textminer.batch.vo.preprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreprocessResultCleansingOptionVo {

	private List<String> black_word;
	private boolean apply_black_words_removal;
	private boolean apply_speaker_combination;
	private boolean apply_spacing_correction;
	private boolean apply_pattern_removal;
	private boolean apply_sentence_separation;
	private boolean apply_split_sentence_word;
	@Override
	public String toString() {
		return "PreprocessResultCleansingOptionVo [black_word=" + black_word + ", apply_black_words_removal="
				+ apply_black_words_removal + ", apply_speaker_combination=" + apply_speaker_combination
				+ ", apply_spacing_correction=" + apply_spacing_correction + ", apply_pattern_removal="
				+ apply_pattern_removal + ", apply_sentence_separation=" + apply_sentence_separation + "]";
	}
	
}
