package kr.co.wisenut.textminer.deploy.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.deploy.vo.DeployVo;
import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;

public interface DeployMapper {

	public List<DeployVo> getDeployList(DeployVo deployVo);									// 배포 서버 정보 리스트 조회
	public DeployVo getDeployDetail(DeployVo deployVo);										// 배포 서버 정보 상세 조회 

	public int insertDeploy(DeployVo deployVo);												// 배포 서버 정보 등록
	public int updateDeploy(DeployVo deployVo);												// 배포 서버 정보 수정
	public int deleteDeploy(DeployVo deployVo);												// 배포 서버 정보 삭제
	
}
