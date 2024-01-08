package kr.co.wisenut.textminer.simulation.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.project.vo.ProjectVo;

public interface SimulationMapper {

	public List<Map<String, Object>> getSimulationList(Map<String, Object> paramMap);				// 시뮬레이션 리스트 조회
}
