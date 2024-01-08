package kr.co.wisenut.textminer.model.mapper;

import kr.co.wisenut.textminer.model.vo.ModelVo;

public interface ModelMapper {

	public int chkEnabledModel(ModelVo model);					// 현재 사용중인 모델이 있는지 체크
	
	public int insertModel(ModelVo model);						// 모델 생성
	public int updateModel(ModelVo model);						// 모델 정보변경
	public int deleteModel(ModelVo model);						// 모델 삭제
}
