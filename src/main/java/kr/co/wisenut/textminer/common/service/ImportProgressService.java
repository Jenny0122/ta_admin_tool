package kr.co.wisenut.textminer.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;

@Service
public class ImportProgressService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ImportProgressMapper importProgressMapper; 
	
	// 컬렉션 목록 조회
	public List<ImportProgressVo> getFileList(ImportProgressVo importProgressVo) {
		
		List<ImportProgressVo> resultList = new ArrayList<ImportProgressVo>();
		
		try {
			resultList = importProgressMapper.getFileList(importProgressVo);
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultList;
	}
	
	// 컬렉션 목록 조회
	public ImportProgressVo getImportProgressDetail(ImportProgressVo importProgressVo) {
		
		ImportProgressVo result = new ImportProgressVo();
		
		try {
			result = importProgressMapper.getImportProgressDetail(importProgressVo);
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return result;
	}
	
}
