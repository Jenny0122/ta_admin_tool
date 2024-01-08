package kr.co.wisenut.textminer.project.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.project.vo.ProjectVo;

public interface ProjectMapper {

	public int getProjectTotalCount(Map<String, Object> paramMap);								// 프로젝트 전체건수 조회
	public List<Map<String, Object>> getProjectListForSelectBox(Map<String, Object> paramMap);	// 프로젝트 리스트 조회(selectBox)
	public List<Map<String, Object>> getProjectList(Map<String, Object> paramMap);				// 프로젝트 리스트 조회
	public ProjectVo getProjectDetail(ProjectVo project);										// 프로젝트 상세조회
	
	public int insertProject(ProjectVo project);												// 프로젝트 등록
	public int updateProject(ProjectVo project);												// 프로젝트 수정
	public int deleteProject(ProjectVo project);												// 프로젝트 삭제
	
}
