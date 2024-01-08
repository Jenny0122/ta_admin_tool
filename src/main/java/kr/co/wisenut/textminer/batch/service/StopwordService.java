package kr.co.wisenut.textminer.batch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StopwordService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private kr.co.wisenut.textminer.batch.mapper.StopwordMapper stopwordMapper;
	
	// 금칙어 분석
	public void doStopword() {
		Map<String, Object> param = new HashMap<String, Object>();
		
		try {
			// 현재 시각 기준 10분 내의 데이터를 업데이트 - 시간은 초단위 이하 절삭
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			List<String> dicType = Arrays.asList("ISSUE", "INTEREST");
			
			String currentTime = date.format(fm)+"00";
			String startTime = date.minusMinutes(10).format(fm)+"00";
			param.put("startTime", startTime);
			param.put("endTime", currentTime);
			
			for (int i=0; i<2; i++) {
				param.put("dicType", dicType.get(i));
				stopwordMapper.doStopword(param);
			}
			
		} catch (Exception e) {
	        logger.error("금칙어 집계 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
	}
}
