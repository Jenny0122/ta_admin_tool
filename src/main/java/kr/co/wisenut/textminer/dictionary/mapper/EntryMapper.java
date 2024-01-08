package kr.co.wisenut.textminer.dictionary.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.dictionary.vo.EntryVo;
import kr.co.wisenut.textminer.dictionary.vo.SynonymVo;

public interface EntryMapper {

	// 1. Entry
	public int getMaxEntryId(EntryVo entry);									// 사전 내의 가장 최근에 등록된 엔트리의 ID 가져오기
	public int getEntryTotalCount(Map<String, Object> paramMap);				// 엔트리 전체건수 조회
	public List<EntryVo> getEntryList(Map<String, Object> paramMap);			// 엔트리 조회
	public List<EntryVo> getEntryForDictionary(Map<String, Object> paramMap);	// 사전적용 API에 연동할 데이터 가져오기
	
	public int checkDuplicatedEntry(EntryVo entry);								// 엔트리 중복검사(사전 내부에 해당 엔트리가 등록되어 있는지 검사)
	public String checkDuplicatedEntryOthers(EntryVo entry);					// 엔트리 중복검사(수용어, 불용어에 해당 엔트리가 등록되어 있는지 검사)
	public int checkDuplicatedEntryInOthers(EntryVo entry);						// 엔트리 중복검사(다른 사전에 해당 엔트리가 등록되어 있는지 검사) -> 사용안함
	
	public int insertEntry(EntryVo entry);										// 엔트리 등록
	public int deleteEntry(EntryVo entry);										// 엔트리 삭제
	
	// 2. Synonym
	public int checkDuplicatedSynonym(SynonymVo synonym);						// 동의어 중복검사
	
	public int insertSynonym(SynonymVo synonym);								// 동의어 등록
	public int deleteSynonym(SynonymVo synonym);								// 동의어 삭제
}