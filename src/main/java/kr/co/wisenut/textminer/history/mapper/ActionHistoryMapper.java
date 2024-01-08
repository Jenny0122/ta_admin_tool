package kr.co.wisenut.textminer.history.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;

public interface ActionHistoryMapper {

	public int getActionHistoryCnt(Map<String, Object> paramMap);							// 활동이력 전체건수 조회
	public List<ActionHistoryVo> getActionHistoryList(Map<String, Object> paramMap);		// 활동이력 조회
	
	public int insertActionHistory(ActionHistoryVo actionHistory);							// 활동이력 등록
}