package kr.co.wisenut.textminer.dictionary.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;

public interface DictionaryMapper {

	public int getSharedDictionaryId(DictionaryVo dictionary);												// 공용사전 존재여부 확인
	public int getDictionaryTotalCount(Map<String, Object> paramMap);										// 사전 전체 건수 조회
	
	public List<DictionaryVo> getDictionaryNames(Map<String, Object> paramMap);								// 사전 목록 조회
	public List<DictionaryVo> getDictionaryList(Map<String, Object> paramMap);								// 사전 리스트 조회
	public DictionaryVo getDictionaryDetail(DictionaryVo dictionary);										// 사전 상세 조회
	
	public int insertDictionary(DictionaryVo dictionary);													// 사전 등록
	public int updateDictionary(DictionaryVo dictionary);													// 사전 수정
	public int deleteDictionary(DictionaryVo dictionary);													// 사전 삭제
}