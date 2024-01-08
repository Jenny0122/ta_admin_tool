package kr.co.wisenut.textminer.batch.controller;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.co.wisenut.textminer.batch.service.BatchService;
import kr.co.wisenut.textminer.batch.service.SttContentsService;
import kr.co.wisenut.textminer.batch.service.TaModuleService;
import kr.co.wisenut.textminer.batch.vo.SttContentsVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.schedule.service.ScheduleService;
import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;

@Component
public class BatchController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	@Autowired
	private BatchService batchService;
	
	@Autowired
	private SttContentsService sttContentsService;
	
	@Autowired
	private TaModuleService taModuleService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	@Autowired
	private ScheduleService scheduleService;
	
	/**
	 * 
	 *	Schedule Name : dataTotal
	 *	Schedule Desc : 분석결과 집계
	 *	Execute Time  : 10분 간격으로 실행
	 *
	 */
	@Scheduled(cron = "0 0/10 * * * *")
	public void dataTotal() {
		
		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
			
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(1);				// 분석결과 집계 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
	        actionHistoryVo.setActionUser("SYSTEM");
	        actionHistoryVo.setResourceId("0");
	        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			
			try {
				// 배치가 실행되는 일자 및 요일 구하기
		    	LocalDateTime date = LocalDateTime.now();
				DayOfWeek dayOfWeek = date.getDayOfWeek();
		    	int dayOfWeekNum = dayOfWeek.getValue();	// 월요일 = 1
				
		    	logger.info("@@Batch Start Time 1 : " + date);
		    	
				// 고객정보 Update(merge)
				batchService.updateCustInfo();
		    	
				// 실행시간에 따른 type 설정
		    	List<String> types = new ArrayList<String>();
	
		    	if (date.getMonthValue() == 1 && date.getDayOfMonth() == 1 && date.getHour() == 0 && date.getMinute() == 0) {
		    		// 1월 1일 00시 00분 -> 년,월,일,시,분 데이터 집계
		    		// 월요일이면 주 데이터도 집계되도록 한다.
		    		if (dayOfWeekNum == 1) {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    			types.add("WS");
		    			types.add("MS");
		    			types.add("YS");
		    		} else {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    			types.add("MS");
		    			types.add("YS");
		    		}
		    	} else if (date.getDayOfMonth() == 1 && date.getHour() == 0 && date.getMinute() == 0) {	
		    		// 1일 00시 00분 -> 월,일,시,분 데이터 집계
		    		// 월요일이면 주 데이터도 집계되도록 한다.
		    		if (dayOfWeekNum == 1) {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    			types.add("WS");
		    			types.add("MS");
		    		} else {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    			types.add("MS");
		    		}
		    	} else if (date.getHour() == 0 && date.getMinute() == 0) {	
		    		// 00시 00분 -> 일,시,분 데이터 집계
		    		// 월요일이면 주 데이터도 집계되도록 한다.
		    		if (dayOfWeekNum == 1) {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    			types.add("WS");
		    		} else {
		    			types.add("TS");
		    			types.add("HS");
		    			types.add("DS");
		    		}
		    	} else if (date.getMinute() == 0) {	
	    			// 00분 -> 시,분 데이터 집계
		    		types.add("TS");
	    			types.add("HS");
		    	} else {							
		    		// 그 외 -> 분 데이터 집계
	    			types.add("TS");
		    	}
				
		    	// 집계 수행
		    	logger.info("@@ dataTotal Start -> type {} ({}) ", types, types.size());
		    	
				for (int i = 0; i < types.size(); i++) {
					batchService.dataTotal(types.get(i), date);
				}
				
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
				
				// Batch 수행이력
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
		        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행완료");
			} catch (Exception e) {
				logger.error("@@ data-total Batch failed. -> {}", e.getMessage());
				e.printStackTrace();
	
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
				
				// Batch 수행이력
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
		        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( " + e.getMessage() + " )");
			}
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
	
	/**
	 * 
	 *	Schedule Name : failedReAnalyzer
	 *	Schedule Desc : 모듈분석 실패 콜 대상 재분석
	 *	Execute Time  : 새벽 2시 15분
	 *
	 */
	@Scheduled(cron = "0 15 2 * * *")
	public void failedReAnalyzer() {
		
		boolean isAutoCompleted = false;		// 상담유형 재분석 처리결과
		boolean isSentCompleted = false;		// 감성분석 재분석 처리결과
		boolean isKeywordCompleted = false;		// 키워드추출 재분석 처리결과
		boolean isRelateCompleted = false;		// 연관어추출 재분석 처리결과
		boolean isSummCompleted = false;		// 문서요약 재분석 처리결과
		
		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
			
			logger.info("failedReAnalyzer ==> Start...!");
			
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(3);				// 모듈분석 실패 콜 대상 재분석 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
	        actionHistoryVo.setActionUser("SYSTEM");
	        actionHistoryVo.setResourceId("0");
	        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			
			try {
				// 재분석 대상 CallId 조회
				List<Map<String, Object>> callInfoList = batchService.getModuleFailedCallInfo();
				
				logger.info("failedReAnalyzer ==> callInfoList {} 건", callInfoList.size());
				
				// 재분석 요청
				for (int i=0; i < callInfoList.size(); i++) {
					SttContentsVo sttContentsVo = sttContentsService.getSttContents(String.valueOf(callInfoList.get(i).get("callId")));
					sttContentsVo.setTbDiv(callInfoList.get(i).get("tbDiv").toString());
					sttContentsVo.setResultCd(callInfoList.get(i).get("resultCd").toString());
					
					// 기존데이터 삭제 후 재분석할 수 있도록한다.
					logger.info("failedReAnalyzer ==> delete Start...!");
					batchService.deleteAnalyzeFailData(sttContentsVo);
					logger.info("failedReAnalyzer ==> delete End...!\n");
					
					// 키워드 추출 모듈 호출
					logger.info("failedReAnalyzer ==> isKeywordCompleted Start...!");
					isKeywordCompleted = taModuleService.callModule(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION, sttContentsVo);
					logger.info("failedReAnalyzer ==> isKeywordCompleted end...! {}", isKeywordCompleted);
					
					// 상담유형 분류 모듈
					logger.info("failedReAnalyzer ==> isAutoCompleted Start...!"); 
					isAutoCompleted = taModuleService.callModule(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION, sttContentsVo);
					logger.info("failedReAnalyzer ==> isAutoCompleted end...! {}", isAutoCompleted);
					
					// 감성분석 모듈 
					logger.info("failedReAnalyzer ==> isSentCompleted Start...!"); 
					isSentCompleted = taModuleService.callModule(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE, sttContentsVo);
					logger.info("failedReAnalyzer ==> isSentCompleted end...! {}", isSentCompleted);
					
					// 요약 모듈 
					logger.info("failedReAnalyzer ==> isSummCompleted Start...!"); 
					isSummCompleted = taModuleService.callModule(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY, sttContentsVo);
					logger.info("failedReAnalyzer ==> isSummCompleted end...! {}", isSummCompleted);
					
					// 연관어 추출 모듈 
					logger.info("failedReAnalyzer ==> isRelateCompleted Start...!"); 
					isRelateCompleted = taModuleService.callModule(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION, sttContentsVo);
					logger.info("failedReAnalyzer ==> isRelateCompleted end...! {}", isRelateCompleted);

					// 전체 모듈이 성공으로 완료되었을 경우 분석 상태 성공으로 업데이트 (아웃바운드 콜인 경우 유형분류 결과는 고려하지 않음)
					if (isKeywordCompleted && isAutoCompleted && isSentCompleted && isSummCompleted && isRelateCompleted) {
						logger.info("failedReAnalyzer ==> updateAnaStatus Start...!"); 

						Map<String, Object> paramMap = new HashMap<String, Object>();
						paramMap.put("applicationId", sttContentsVo.getApplicationId());
						paramMap.put("anaStatus", "400300");
						taModuleService.updateAnaStatus(paramMap);

						logger.info("failedReAnalyzer ==> updateAnaStatus end...! {}", isRelateCompleted);
					}
				}
				
				// 재분석 완료 후 집계 업데이트 처리
				batchService.updateStopword();
				batchService.updateTotal();
				
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
				
				// Batch 수행이력
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
		        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행완료");
			} catch (Exception e) {
				logger.error("@@ failedReAnalyzer Batch failed. -> {}", e.getMessage());
	
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
				
				// Batch 수행이력
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
	        	if (e.getMessage().length() >= 400) {
	        		actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( " + e.getMessage().substring(0, 400) + " )");
	        	} else {
	        		actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( " + e.getMessage() + " )");
	        	}
			}
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
	
	/**
	 * 
	 *	Schedule Name : sendMsg
	 *	Schedule Desc : 반복민원 고객ID 메시지 발송
	 *	Execute Time  : 오전 9시
	 *
	 */
	@Scheduled(cron = "0 0 9 * * *")
	public void sendMsg() {

		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
			
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(4);				// 반복민원 고객ID 메시지 발송 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
	        actionHistoryVo.setActionUser("SYSTEM");
	        actionHistoryVo.setResourceId("0");
	        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        
	        try {
	        	// 메시지 발송처리
	        	int result = batchService.sendMsg();
	        	
	        	if (result == 0) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
					
					// Batch 수행이력
		        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
			        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행완료");
	        	} else if (result == -2) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
					
					// Batch 수행이력
		        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
			        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( API 호출 에러 )");
	        	} else if (result == -3) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
					
					// Batch 수행이력
		        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
			        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행완료 ( 반복민원 없음 )");
	        	}
			} catch (Exception e) {
				logger.error("@@ sendMsg Batch failed. -> {}", e.getMessage());
	
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
				
				// Batch 수행이력
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
		        actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( " + e.getMessage() + " )");
			}
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
	
	/**
	 * 
	 *	Schedule Name : sendEdw
	 *	Schedule Desc : EDW 분석결과 전송
	 *	Execute Time  : 10분 간격으로 실행
	 *
	 */
	@Scheduled(cron = "30 0/10 * * * *")
	public void sendEdw() {

		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
	
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(5);				// 상담유형 코드 최신화 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
			actionHistoryVo.setActionUser("SYSTEM");
			actionHistoryVo.setResourceId("0");
			actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			
			try {
				// EDW 발송처리
				int result = batchService.sendEdw();
				
				if (result == 0) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
					
					// Batch 수행이력
					actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
					actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행완료");
				} else if (result == -1) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
					
					// Batch 수행이력
					actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
					actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( EDW Data Error )");
				} else if (result == -2) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
					
					// Batch 수행이력
					actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
					actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( EDW Error )");
				} else if (result == -3) {
					// 스케쥴 마지막 실행상태 변경
					scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
					
					// Batch 수행이력
					actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
					actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행성공 ( 전송데이터 없음 )");
				}
			} catch (Exception e) {
				logger.error("@@ sendEdw Batch failed. -> {}", e.getMessage());
				
				// 스케쥴 마지막 실행상태 변경
				scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_FAIL);
				
				// Batch 수행이력
				actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
				actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행실패 ( " + e.getMessage() + " )");
			}
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
	

	/**
	 * 
	 *	Schedule Name : deleteLog
	 *	Schedule Desc : 2년 지난 로그삭제
	 *	Execute Time  : 00시 05분
	 *
	 */
	@Scheduled(cron = "0 5 0 * * *")
	public void deleteLog() {
		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
			
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(6);				// 2년 지난 로그삭제 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
			actionHistoryVo.setActionUser("SYSTEM");
			actionHistoryVo.setResourceId("0");
			actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			
			try {
				batchService.deleteLog();
				logger.info("@@ deleteLog Batch Completed");
			} catch (Exception e) {
				logger.error("@@ deleteLog Batch failed. -> {}", e.getMessage());
			}
			
			// 스케쥴 마지막 실행상태 변경
			scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
			
			// Batch 수행이력
			actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
			actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행성공");
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
	
	
	/**
	 * 
	 *	Schedule Name : deleteAnalyzeData
	 *	Schedule Desc : 5년 지난 분석결과 및 통계 데이터 삭제
	 *	Execute Time  : 00시 05분
	 *
	 */
	@Scheduled(cron = "0 5 0 * * *")
	public void deleteAnalyzeData() {
		// 로컬에서는 해당 배치가 실행되지 않도록 한다.
		if (!profile.equals("local")) {
			
			// 스케쥴 정보조회
			ScheduleVo scheduleVo = new ScheduleVo();
			scheduleVo.setScheduleId(7);				// 5년 지난 분석결과 데이터 삭제 Schedule ID
			scheduleVo = scheduleService.getScheduleDetail(scheduleVo);
			
			// Batch 실행이력 등록 Vo
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();	
			actionHistoryVo.setActionUser("SYSTEM");
			actionHistoryVo.setResourceId("0");
			actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			
			try {
				batchService.deleteAnalyzeAndStatisticsData();
				logger.info("@@ deleteAnalyzeData Batch Completed");
			} catch (Exception e) {
				logger.error("@@ deleteAnalyzeData Batch failed. -> {}", e.getMessage());
			}

			// 스케쥴 마지막 실행상태 변경
			scheduleVo.setLastExecuteStatus(TextMinerConstants.SYSTEM_BATCH_STATUS_SUCCESS);
			
			// Batch 수행이력
			actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SYSTEM_BATCH);
			actionHistoryVo.setActionMsg("\"" + scheduleVo.getScheduleName() + "\" 스케쥴 실행성공");
			
			// Batch 수행 완료 후 실행결과 Update
			scheduleService.updateScheduleStatus(scheduleVo);
			
			// Batch 수행 완료 후 실행이력 저장
			actionHistoryService.insertActionHistory(actionHistoryVo);
		}
	}
}
