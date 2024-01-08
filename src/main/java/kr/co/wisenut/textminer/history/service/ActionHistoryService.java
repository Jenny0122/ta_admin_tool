package kr.co.wisenut.textminer.history.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.history.mapper.ActionHistoryMapper;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class ActionHistoryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ActionHistoryMapper actionHistoryMapper;
	
	// 활동이력 조회
	public Map<String, Object> getActionHistoryList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 조회결과 리스트
			List<ActionHistoryVo> resultList = actionHistoryMapper.getActionHistoryList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForActionHistoryList(resultList, Integer.parseInt(paramMap.get("pageRow").toString())));
			
			// 페이징
			resultMap.put("pageNav", PageUtil.createPageNav(actionHistoryMapper.getActionHistoryCnt(paramMap), paramMap));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 활동이력 등록
	public void insertActionHistory(ActionHistoryVo actionHistoryVo) {
		
		try {
			actionHistoryMapper.insertActionHistory(actionHistoryVo);
		} catch (NullPointerException e) {
			logger.error("등록 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("등록 작업을 실패하였습니다.");
			e.printStackTrace();
		}
	}
	
	public String convertHtmlTagForActionHistoryList(List<ActionHistoryVo> resultList, int pageRow) {
		StringBuffer convertHtml = new StringBuffer();

		if (resultList.size() > 0) {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr>\n");
				convertHtml.append("\t\t<td>" + resultList.get(i).getActionDt() + "</td>\n");
				convertHtml.append("\t\t<td data-action-type=\"" + resultList.get(i).getActionType() + "\" style=\"text-align: left\">");
				convertHtml.append(resultList.get(i).getActionMsg() + "</td>\n");
				convertHtml.append("\t</tr>\n");
			}
		} else {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"2\">조회된 Batch 실행 이력이 없습니다.</td>\n");
			convertHtml.append("\t</tr>\n");
		}
		
		return convertHtml.toString();
	}
}
