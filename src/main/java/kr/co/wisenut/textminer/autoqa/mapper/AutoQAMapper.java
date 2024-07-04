package kr.co.wisenut.textminer.autoqa.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.autoqa.vo.AutoQaCateVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaScriptVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaSimScriptVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaKeywordVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaCateInfoVo;

public interface AutoQAMapper {

	public int getQACategoryTotalCount(Map<String, Object> paramMap);								// 카테고리 전체 카운트 조회	
	public List <AutoQaCateVo>getQACategoryList( Map<String, Object> paramMap);		// 카테고리 리스트조회
	public int getQACategoryID( Map<String, Object> paramMap);												// 카테고리 조회
	
	public int getQAScriptTotalCount(Map<String, Object> paramMap);									// 스크립트 전체 카운트 조회	
	public List <AutoQaScriptVo>getQAScriptList( Map<String, Object> paramMap);			// 스크립트 조회
	public int getQAScriptyID( Map<String, Object> paramMap);													// 스크립트 조회
	
	public  List <AutoQaScriptVo> getCompCdInfo(Map<String, Object> paramMap);			// 준수항목 조회
	
	public List<AutoQaCateInfoVo> getCategoryNames(Map<String, Object> paramMap);						// 카테고리 정보 조회
	public List<AutoQaKeywordVo> getkeywordDetail(Map<String, Object> paramMap);							// 키워드 조회 팝업
	public List<AutoQaSimScriptVo>getSimScriptContDetail(Map<String, Object> paramMap);				// 유사문장 조회 팝업
	
	public int insertQAScript(AutoQaScriptVo autoqacate);												// 규정 스크립트 등록
	public int updateQAScript(AutoQaScriptVo autoqacate);												// 규정 스크립트 수정
	public int deleteQAScript(AutoQaScriptVo autoqacate);												// 규정 스크립트 삭제
}