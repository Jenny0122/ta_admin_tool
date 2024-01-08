package kr.co.wisenut.textminer.schedule.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;

public interface ScheduleMapper {

	public int getScheduleTotalCount(ScheduleVo schedule);				// 스케줄 전체건수 조회
	public List<ScheduleVo> getScheduleList(ScheduleVo schedule);		// 스케줄 리스트 조회
	public ScheduleVo getScheduleDetail(ScheduleVo schedule);			// 스케줄 상세 조회
	
	public int updateScheduleStatus(ScheduleVo schedule);				// 스케쥴 실행결과 변경
}
