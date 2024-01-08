package kr.co.wisenut.textminer.common.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;

public interface ImportProgressMapper {

	public List<ImportProgressVo> getFileList(ImportProgressVo importProgress);				// 업로드 파일 조회
	public ImportProgressVo getImportProgressDetail(ImportProgressVo importProgress);		// 업로드 파일 상세조회

	public int insertImportProgress(ImportProgressVo importProgress);						// 작업결과 저장 
	public int deleteImportProgress(ImportProgressVo importProgress);						// 작업결과 삭제
}
