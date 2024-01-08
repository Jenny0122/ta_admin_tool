package kr.co.wisenut.textminer.schedule.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.schedule.mapper.ScheduleMapper;
import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class ScheduleService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ScheduleMapper scheduleMapper;
	
	// 스케줄 리스트 조회
	public Map<String, Object> getScheduleList(ScheduleVo scheduleVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 전체 건수
			resultMap.put("totalCount", scheduleMapper.getScheduleTotalCount(scheduleVo));		
			
			// 조회결과 리스트
			List<ScheduleVo> resultList = scheduleMapper.getScheduleList(scheduleVo);
			resultMap.put("dataTable", convertHtmlTagForScheduleList(resultList));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 스케줄 상세조회
	public ScheduleVo getScheduleDetail(ScheduleVo scheduleVo) {
		
		try {
			scheduleVo = scheduleMapper.getScheduleDetail(scheduleVo);
		} catch (NullPointerException e) {
			logger.error("상세조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("상세조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return scheduleVo;
	}
	
	// 마지막 스케쥴 실행상태 변경
	public void updateScheduleStatus (ScheduleVo scheduleVo) {
		try {
			scheduleMapper.updateScheduleStatus(scheduleVo);
		} catch (NullPointerException e) {
			logger.error("변경 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("변경 작업을 실패하였습니다.");
			e.printStackTrace();
		}
	}

	public String convertHtmlTagForScheduleList(List<ScheduleVo> resultList) {
		StringBuffer convertHtml = new StringBuffer();
		
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"5\">등록된 스케줄이 없습니다.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr class=\"collection\">\n");
				
				// 선택
//				convertHtml.append("\t\t<td>\n");
//				convertHtml.append("\t\t\t<input type=\"radio\" name=\"scheduleChoice\" value=\"" + resultList.get(i).getScheduleId() + "\" data-name=\"" + resultList.get(i).get("scheduleName") + "\"");
//				convertHtml.append("\t\t</td>\n");
				
				// 스케줄 명
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(resultList.get(i).getScheduleName()+"\n");
				convertHtml.append("\t\t</td>\n");
				
				// 반복 주기 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(resultList.get(i).getExecuteTimeAndInterval() + "\n");
				convertHtml.append("\t\t</td>\n");
				
				// 사용 여부 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(resultList.get(i).getUseYn()+"\n");
				convertHtml.append("\t\t</td>\n");
				
				// 마지막 실행일시 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append((resultList.get(i).getLastExecuteDt()==null?" ":resultList.get(i).getLastExecuteDt())+"\n");
				convertHtml.append("\t\t</td>\n");
				
				// 마지막 실행결과
				convertHtml.append("\t\t<td>\n");
				convertHtml.append((resultList.get(i).getLastExecuteStatus()==null?" ":resultList.get(i).getLastExecuteStatus())+"\n");
				convertHtml.append("\t\t</td>\n");
				
				
				convertHtml.append("\t</tr>\n");
			}
		}
		
		return convertHtml.toString();
	}
}
