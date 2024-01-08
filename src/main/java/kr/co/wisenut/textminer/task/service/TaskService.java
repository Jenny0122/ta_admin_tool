package kr.co.wisenut.textminer.task.service;

import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.collection.mapper.CollectionMapper;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.dictionary.mapper.DictionaryMapper;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.task.mapper.TaskMapper;
import kr.co.wisenut.textminer.task.vo.TaskVo;

@Service
public class TaskService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TMProperties tmProperties;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private CollectionMapper collectionMapper;

	@Autowired
	private DictionaryMapper dictionaryMapper;
	
	// 서비스 모델 리스트 가져오기
	public Map<String, Object> getModelList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<TaskVo> modelList = new ArrayList<TaskVo>();
		
		try {
			modelList = taskMapper.getTaskList(paramMap);
			resultMap.put("modelList", modelList);
			
			if (modelList.size() > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "조회가 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 테스크 상세조회
	public Map<String, Object> getTaskInfo(Map<String, Object> paramMap) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		

		try {
			TaskVo taskVo = new TaskVo();
			taskVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
			taskVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
			
			taskVo = taskMapper.getTaskInfo(taskVo);

			if (taskVo != null) {
				paramMap.put("collectionId", taskVo.getCollectionId());
			
				if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
				 || taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
					resultMap.put("taskForm", taskFormType1(paramMap, taskVo));
				} else if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
						|| taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
					resultMap.put("taskForm", taskFormType3(paramMap, taskVo));
				} else {
					resultMap.put("taskForm", taskFormType2(paramMap, taskVo));
				}
			} else {
				resultMap.put("taskForm", TextMinerConstants.COMMON_BLANK);
			}
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}

		return resultMap;
	}
	
	// 테스크 저장
	public Map<String, Object> saveTask(Map<String, Object> paramMap) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		TaskVo taskVo = new TaskVo();
		taskVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
		taskVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
		taskVo.setTaskType(paramMap.get("taskType").toString());
		taskVo.setTaskName(paramMap.get("taskName").toString());
		
		if ( taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
		  || taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
			taskVo.setCollectionId(Integer.parseInt(paramMap.get("collections").toString()));
			taskVo.setModelName(taskVo.getTaskName() + "_" + Integer.parseInt(paramMap.get("collections").toString()));
		} else {
			taskVo.setModelName(taskVo.getTaskName() + "_" + taskVo.getTaskType());
			taskVo.setModelFile(taskVo.getTaskName() + "_" + taskVo.getTaskType());
		}

		// TASK_CONFIG에 들어갈 json text 만들기
		Gson gson = new Gson();
		JsonObject taskConfig = new JsonObject();
		JsonParser parser = new JsonParser();
		taskConfig = (JsonObject) parser.parse(gson.toJson(paramMap));
		
		taskConfig.remove("job");
		taskConfig.remove("projectId");
		taskConfig.remove("taskId");
		taskConfig.remove("taskType");
		taskConfig.remove("taskName");
		taskConfig.remove("collections");
		taskConfig.remove("userId");

		taskVo.setTaskConfig(taskConfig.toString());
		taskVo.setCreUser(paramMap.get("userId").toString());
		taskVo.setModUser(paramMap.get("userId").toString());
		
		// 테스크 저장
		TaskVo beforeTaskVo = null;
		try {
			int result = 0;
			if (paramMap.get("job").toString().equals("I")) {
				
				// 전처리, 문자열 매칭은 무조건 사용으로 적용
				switch(paramMap.get("taskType").toString()) {
					case TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS:
					case TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS:
					case TextMinerConstants.TASK_TYPE_STRING_MATCHER:
						taskVo.setEnabled("Y");		
						break;
					default:
						taskVo.setEnabled("N");		
						break;
				}
				
				// 등록 작업 시, 기 등록된 서비스(테스크)가 3개 이상이면 등록불가 처리
				if (taskMapper.getTaskCnt(taskVo) >= 3) {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "\"" + taskVo.getLabel() + "\" 서비스는 더 이상 등록할 수 없습니다. (최대 3개까지 등록 가능)");
					return resultMap;
				}

				// 테스크 이름 중복검사 후 등록처리
				if (taskMapper.chkDuplicatedTaskName(taskVo) == 0) {
					result = taskMapper.insertTask(taskVo);
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "기존에 등록된 서비스 이름이 있어 등록 작업을 실패하였습니다.");
					return resultMap;
				}
			} else {
				beforeTaskVo = taskMapper.getTaskInfo(taskVo);
				
				// 서비스 변경 이후 해당 모델이 배포되지 않도록 사용여부를 미사용으로 초기화한다.
				// 전처리, 문자열 매칭은 무조건 사용으로 적용
				switch(paramMap.get("taskType").toString()) {
					case TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS:
					case TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS:
					case TextMinerConstants.TASK_TYPE_STRING_MATCHER:
						taskVo.setEnabled("Y");		
						break;
					default:
						taskVo.setEnabled("N");		
						break;
				}
				
				// 테스크 이름 중복검사 후 등록처리
				if (taskMapper.chkDuplicatedTaskName(taskVo) == 0) {
					result = taskMapper.updateTask(taskVo);
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "기존에 등록된 서비스 이름이 있어 등록 작업을 실패하였습니다.");
					return resultMap;
				}
			}

			// 자동분류, 감성분석 train config 파일 수정 및 Classifier 재기동
			if ( taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
			  || taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				
				// train config 파일 수정
				updateTrainConfig(paramMap.get("job").toString(), taskVo, beforeTaskVo);
			}
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "저장되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "저장 작업을 실패하였습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("저장 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "저장 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("저장 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "저장 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 사용/미사용 처리
	public Map<String, Object> updateEnabled(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		int chk = 0;
		int result = 0;
		
		try {
			// 모델 사용으로 변경처리 시, 생성된 모델 정보가 있는지 확인

			TaskVo taskVo = new TaskVo();
			taskVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
			taskVo.setTaskType(paramMap.get("taskType").toString());
			taskVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
			taskVo.setModUser(paramMap.get("user").toString());
			
			taskVo = taskMapper.getTaskInfo(taskVo);
//			if(taskVo.getModelFile() == null) {
//				resultMap.put("result", "R");
//				resultMap.put("resultMsg", "현재 생성된 모델이 없어 사용할 수 없습니다.");
//				
//				return resultMap;
//			}
			
			// 모델 설정값 변경 이후 분석이력이 없으면 해당 모델은 사용으로 변경 불가
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date modDt = new Date(sdf.parse(taskVo.getModDt()).getTime());
			Date lastAnalyzeDt = new Date(sdf.parse(taskVo.getLastAnalyzeDt()).getTime());
			
			// 수정일 > 마지막 분석일
			if (taskVo.getLastAnalyzeDt() == null) {
				resultMap.put("result", "R");
				resultMap.put("resultMsg", "분석된 이력이 없어 사용할 수 없습니다.");
				return resultMap;
			} else if (modDt.after(lastAnalyzeDt)) {
				resultMap.put("result", "R");
				resultMap.put("resultMsg", "서비스 수정 이후 분석된 이력이 없어 사용할 수 없습니다.");
				return resultMap;
			}
			
			// 모델 사용으로 변경처리 시, 현재 사용중인 모델이 있는지 체크
			if (paramMap.get("enabled").toString().equals("Y")) {
				chk = taskMapper.chkEnabledTask(taskVo);
			}
			
			// 입력값으로 설정
			taskVo.setEnabled(paramMap.get("enabled").toString());
			
			if (chk == 0) {
				taskVo.setEnableFlag("Y");
				result = taskMapper.updateTask(taskVo);
				
				if (result > 0) {
					resultMap.put("result", "S");
					resultMap.put("resultMsg", "모델 사용여부가 변경되었습니다.");
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
				}
			} else {
				resultMap.put("result", "R");
				resultMap.put("resultMsg", "현재 사용 중인 모델이 있어 사용할 수 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("수정 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("수정 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 테스크 삭제
	public Map<String, Object> deleteTask (TaskVo taskVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		int result = 0;
		
		try {
			// 설정파일에서 해당 내용 제거 (자동분류, 감성분석)
			taskVo = taskMapper.getTaskInfo(taskVo);
			
			if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
			 || taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				updateTrainConfig("D", taskVo, null);
			}
			
			// 데이터 삭제
			result = taskMapper.deleteTask(taskVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "모델이 삭제되었습니다.");
				resultMap.put("deleteTask", taskVo.getTaskName());
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "삭제 작업을 실패하였습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("삭제 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("삭제 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 입력양식 가져오기
	public Map<String, Object> getTaskForm(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
			 || paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				resultMap.put("taskForm", taskFormType1(paramMap, null));
			} else if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
					|| paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
				resultMap.put("taskForm", taskFormType3(paramMap, null));
			} else {
				resultMap.put("taskForm", taskFormType2(paramMap, null));
			}
			
		} catch (NullPointerException e) {
			logger.error("수정 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("수정 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 컬렉션 및 사전이 현재 사용중인지 체크
	public Map<String, Object> chkTaskResouce(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			int useCnt = taskMapper.chkTaskResouce(paramMap);
			
			if (useCnt == 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "삭제가능");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "현재 서비스에 사용중이기 때문에 삭제할 수 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	public String taskFormType1 (Map<String, Object> paramMap, TaskVo taskVo) {
		StringBuffer taskFormHtml = new StringBuffer();
		
		taskFormHtml.append("<form id=\"taskForm\">");
		taskFormHtml.append("	<div id=\"drawTitleArea\">");
		taskFormHtml.append("		<span style=\"position:relative; float:left;\">서비스 모델명</span>");
		taskFormHtml.append("		<input type='text' class=\"ml20\" name=\"taskModelName\" value=\"" + (taskVo!=null?taskVo.getTaskName():"") + "\">");
		taskFormHtml.append("	</div>");
		taskFormHtml.append("	<div id=\"drawLeftArea\">");
		taskFormHtml.append("		<div id=\"drawCollection\">");
		taskFormHtml.append("			<span>컬렉션 선택</span>");
		taskFormHtml.append("			<table class=\"basic_tbl_type mt10 text-ellipsis\">");
		taskFormHtml.append("				<colgroup>");
		taskFormHtml.append("					<col width=\"10%;\">");
		taskFormHtml.append("					<col width=\"*%;\">");
		taskFormHtml.append("					<col width=\"15%;\">");
		taskFormHtml.append("					<col width=\"15%;\">");
		taskFormHtml.append("				</colgroup>");
		taskFormHtml.append("				<thead>");
		taskFormHtml.append("					<th>선택</th>");
		taskFormHtml.append("					<th>컬렉션명</th>");
		taskFormHtml.append("					<th>컬렉션<br>구분</th>");
		taskFormHtml.append("					<th>작업구분</th>");
		taskFormHtml.append("				</thead>");
		taskFormHtml.append("			</table>");
		taskFormHtml.append("			<div id=\"collectionList\">");
		taskFormHtml.append("				<table class=\"basic_tbl_type text-ellipsis\">");
		taskFormHtml.append("					<colgroup>");
		taskFormHtml.append("						<col width=\"10%;\">");
		taskFormHtml.append("						<col width=\"*%;\">");
		taskFormHtml.append("						<col width=\"15%;\">");
		taskFormHtml.append("						<col width=\"15%;\">");
		taskFormHtml.append("					</colgroup>");
		taskFormHtml.append("					<tbody id=\"collections\">");
		
		// 컬렉션 리스트 추가
		paramMap.put("collectionJob", TextMinerConstants.COLLECTION_JOB_TRAINING);
		
		if (taskVo == null) {
			paramMap.put("collectionType", paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)?TextMinerConstants.COLLECTION_TYPE_CLASSIFICATION:TextMinerConstants.COLLECTION_TYPE_EMOTION);
		} else {
			paramMap.put("collectionType", taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)?TextMinerConstants.COLLECTION_TYPE_CLASSIFICATION:TextMinerConstants.COLLECTION_TYPE_EMOTION);
		}
		
		paramMap.put("pageSize", 1);
		List<CollectionVo> collections = collectionMapper.getCollectionList(paramMap);
		
		for (int i = 0; i < collections.size(); i++) {
			CollectionVo collection = collections.get(i);
			
			taskFormHtml.append("					<tr>");
			taskFormHtml.append("						<td><input type=\"radio\" name=\"collections\" value=\"" + collection.getCollectionId() + "\"");
			taskFormHtml.append((taskVo!=null?(taskVo.getCollectionId()==collection.getCollectionId()?"checked":""):i==0?"checked":"") + ">");
			taskFormHtml.append("						</td>");
			taskFormHtml.append("						<td>" + collection.getCollectionName() + "</td>");
			taskFormHtml.append("						<td>" + collection.getCollectionTypeName() + "</td>");
			taskFormHtml.append("						<td>" + collection.getCollectionJobName() + "</td>");
			taskFormHtml.append("					<tr>");
		}
		
		taskFormHtml.append("					</tbody>");
		taskFormHtml.append("				</table>");
		taskFormHtml.append("			</div>");
		taskFormHtml.append("		</div>");

		// taskVo의 TaskConfig값 parsing
		JsonObject taskConfig = null;
		if (taskVo != null) {
			JsonParser parser = new JsonParser();
			taskConfig = (JsonObject) parser.parse(taskVo.getTaskConfig());
		}
		
/*
 * Classifier는 사전 사용안함, 주석처리
 * 
		// 사전 리스트 추가
		List<DictionaryVo> dictionaries = dictionaryMapper.getDictionaryList(paramMap);
		
		// 수용어 사전
		taskFormHtml.append("		<div class=\"drawDictionaries\">");
		taskFormHtml.append("			<span>수용어 사전</span>");
		
		// 수용어 공통사전
//		taskFormHtml.append("			<p> 공통사전 사용</p><input type=\"checkbox\" name=\"whiteSharedEnabled\" value=\"Y\"");
//		taskFormHtml.append(taskConfig!=null?(taskConfig.get("whiteSharedEnabled").getAsString().equals("Y")?"checked>":">"):">");
		
		taskFormHtml.append("			<select name=\"whiteDictionary\" class=\"mt5\">");
		taskFormHtml.append("				<option value=\"\">수용어 사전 선택</option>");
		
		for (int i = 0; i < dictionaries.size(); i++) {
			DictionaryVo dictionary = dictionaries.get(i);
			
			// 공통사전 제외
			if (dictionary.getDictionarySharedYn().equals("Y")) continue;
			// 불용어사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;
			
			taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
			
			taskFormHtml.append(taskConfig!=null?(taskConfig.get("whiteDictionary").getAsString().equals(String.valueOf(dictionary.getDictionaryId()))?"selected>":">"):">");
			taskFormHtml.append(dictionary.getDictionaryName());
			taskFormHtml.append("			</option>");
		}
		
		taskFormHtml.append("			</select>");
		taskFormHtml.append("		</div>");
		
		// 불용어 사전
		taskFormHtml.append("		<div class=\"drawDictionaries\">");
		taskFormHtml.append("			<span>불용어 사전</span>");
		
		// 불용어 공통사전
//		taskFormHtml.append("			<p> 공통사전 사용</p><input type=\"checkbox\" name=\"blackSharedEnabled\" value=\"Y\"");
//		taskFormHtml.append(taskConfig!=null?(taskConfig.get("blackSharedEnabled").getAsString().equals("Y")?"checked>":">"):">");
		
		taskFormHtml.append("			<select name=\"blackDictionary\" class=\"mt5\">");
		taskFormHtml.append("				<option value=\"\">불용어 사전 선택</option>");
		
		for (int i = 0; i < dictionaries.size(); i++) {
			DictionaryVo dictionary = dictionaries.get(i);
			
			// 공통사전 제외
			if (dictionary.getDictionarySharedYn().equals("Y")) continue;
			// 수용어사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;
			
			taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
			taskFormHtml.append(taskConfig!=null?(taskConfig.get("blackDictionary").getAsString().equals(String.valueOf(dictionary.getDictionaryId()))?"selected>":">"):">");
			taskFormHtml.append(dictionary.getDictionaryName());
			taskFormHtml.append("			</option>");
		}

		taskFormHtml.append("			</select>");
		taskFormHtml.append("		</div>");
*/
		
		taskFormHtml.append("	</div>");
		
		// 2) train 설정 양식
		taskFormHtml.append("	<div id=\"drawRightArea\">");
		taskFormHtml.append("		<span>Train 설정</span>");
		taskFormHtml.append("		<div class=\"drawOptionArea\">");
		taskFormHtml.append("			<table class=\"basic_tbl_type mt10 text-ellipsis\">");
		taskFormHtml.append("				<colgroup>");
		taskFormHtml.append("					<col width=\"50%;\">");
		taskFormHtml.append("					<col width=\"50%;\">");
		taskFormHtml.append("				</colgroup>");
		taskFormHtml.append("				<thead>");
		taskFormHtml.append("					<th>옵션명</th>");
		taskFormHtml.append("					<th>옵션값</th>");
		taskFormHtml.append("				</thead>");
		taskFormHtml.append("			</table>");
		taskFormHtml.append("			<div class=\"optionList\">");
		taskFormHtml.append("				<table class=\"basic_tbl_type text-ellipsis\">");
		taskFormHtml.append("					<colgroup>");
		taskFormHtml.append("						<col width=\"25%;\">");
		taskFormHtml.append("						<col width=\"25%;\">");
		taskFormHtml.append("						<col width=\"50%;\">");
		taskFormHtml.append("					</colgroup>");
		taskFormHtml.append("					<tbody id=\"options\">");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td colspan=\"2\">algorithm</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"algorithm\">");
		taskFormHtml.append("									<option value=\"XGBoost\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("trainOptions").get("algorithm").getAsString().equals("XGBoost")?" selected>":">"):">");
		taskFormHtml.append("										XGBoost");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td rowspan=\"5\">feature</td>");
		taskFormHtml.append("							<td>maxDF</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"maxDF\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("maxDF").getAsDouble():"1.0");
		taskFormHtml.append(								"\" step=\"0.1\" min=\"0.5\" max=\"1.0\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>minDF</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"minDF\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("minDF").getAsDouble():"0.0");
		taskFormHtml.append(								"\" step=\"0.1\" min=\"0.0\" max=\"0.5\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>vocabLimit</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"vocabLimit\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("vocabLimit").getAsInt() + "\"></td>":"-1\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>printVocab</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"printVocab\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("printVocab").getAsBoolean()==true?" selected>":">"):">");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("printVocab").getAsBoolean()==false?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>printFeature</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"printFeature\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("printFeature").getAsBoolean()==true?" selected>":">"):">");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("feature").get("printFeature").getAsBoolean()==false?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td rowspan=\"3\">parameter</td>");
		taskFormHtml.append("							<td>nthread</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"nthread\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("parameter").get("nthread").getAsInt() + "\"></td>":"0\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>round</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"round\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("parameter").get("round").getAsInt() + "\"></td>":"700\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>max_depth</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"max_depth\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("trainOptions").getAsJsonObject("parameter").get("max_depth").getAsInt() + "\"></td>":"3\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("					</tbody>");
		taskFormHtml.append("				</table>");
		taskFormHtml.append("			</div>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("	</div>");
		
		// 3) analyze 설정 양식
		taskFormHtml.append("	<div id=\"drawRightArea\">");
		taskFormHtml.append("		<span>Analyze 설정</span>");
		taskFormHtml.append("		<div class=\"drawOptionArea\">");
		taskFormHtml.append("			<table class=\"basic_tbl_type mt10 text-ellipsis\">");
		taskFormHtml.append("				<colgroup>");
		taskFormHtml.append("					<col width=\"60%;\">");
		taskFormHtml.append("					<col width=\"40%;\">");
		taskFormHtml.append("				</colgroup>");
		taskFormHtml.append("				<thead>");
		taskFormHtml.append("					<th>옵션명</th>");
		taskFormHtml.append("					<th>옵션값</th>");
		taskFormHtml.append("				</thead>");
		taskFormHtml.append("			</table>");
		taskFormHtml.append("			<div class=\"optionList\">");
		taskFormHtml.append("				<table class=\"basic_tbl_type text-ellipsis\">");
		taskFormHtml.append("					<colgroup>");
		taskFormHtml.append("						<col width=\"25%;\">");
		taskFormHtml.append("						<col width=\"35%;\">");
		taskFormHtml.append("						<col width=\"40%;\">");
		taskFormHtml.append("					</colgroup>");
		taskFormHtml.append("					<tbody id=\"options\">");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td rowspan=\"5\">predict</td>");
		taskFormHtml.append("							<td>predictFieldName</td>");
		taskFormHtml.append("							<td><input type=\"text\" name=\"predictFieldName\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("predictFieldName").getAsString():"LABEL");
		taskFormHtml.append(								"\"></td>");
		taskFormHtml.append("						</tr>");
//		taskFormHtml.append("						<tr>");
//		taskFormHtml.append("							<td>predictThreshold</td>");
//		taskFormHtml.append("							<td><input type=\"number\" name=\"predictThreshold\" value=\"");
//		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("options").getAsJsonObject("feature").get("minDF").getAsDouble():"0.0");
//		taskFormHtml.append(								"\" step=\"0.01\" min=\"0.0\" max=\"0.5\"></td>");
//		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>defaultLabel</td>");
		taskFormHtml.append("							<td><input type=\"text\" name=\"defaultLabel\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("defaultLabel").getAsString() + "\"></td>":"NO_LABEL\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>thresholdOption</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"thresholdOption\">");
		taskFormHtml.append("									<option value=\"hard\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("thresholdOption").getAsString().equals("hard")?" selected>":">"):"selected>");
		taskFormHtml.append(									"hard");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"soft\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("thresholdOption").getAsString().equals("soft")?" selected>":">"):">");
		taskFormHtml.append(									">soft");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>(hard) predictThreshold</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"predictThreshold\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("predictThreshold").getAsDouble():"0.0");
		taskFormHtml.append(								"\" step=\"0.01\" min=\"0.0\" max=\"1.0\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>(soft) thresholdMultiplier</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"thresholdMultiplier\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("predict").get("thresholdMultiplier").getAsDouble():"0.0");
		taskFormHtml.append(								"\" step=\"0.01\" min=\"0.0\" max=\"1.0\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td rowspan=\"2\">confidence</td>");
		taskFormHtml.append("							<td>confidenceFieldName</td>");
		taskFormHtml.append("							<td><input type=\"text\" name=\"confidenceFieldName\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("confidence").get("confidenceFieldName").getAsString() + "\"></td>":"CONFIDENCE\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>numConfidence</td>");
		taskFormHtml.append("							<td><input type=\"number\" name=\"numConfidence\" value=\"");
		taskFormHtml.append(taskConfig!=null?taskConfig.getAsJsonObject("analyzeOptions").getAsJsonObject("confidence").get("numConfidence").getAsInt() + "\"></td>":"5\"></td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("					</tbody>");
		taskFormHtml.append("				</table>");
		taskFormHtml.append("			</div>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("	</div>");
		
		taskFormHtml.append("</from>");
		
		return taskFormHtml.toString();
	}
	
	public String taskFormType2 (Map<String, Object> paramMap, TaskVo taskVo) {

		StringBuffer taskFormHtml = new StringBuffer();
		
		taskFormHtml.append("<form id=\"taskForm\">");
		taskFormHtml.append("	<div id=\"drawTitleArea\">");
		taskFormHtml.append("		<span style=\"position:relative; float:left;\">서비스 모델명</span>");
		taskFormHtml.append("		<input type='text' class=\"ml20\" name=\"taskModelName\" value=\"" + (taskVo!=null?taskVo.getTaskName():"") + "\">");
		taskFormHtml.append("	</div>");
		taskFormHtml.append("	<div id=\"drawLeftArea\">");

		// taskVo의 TaskConfig값 parsing (키워드, 연관어 추출만 해당)
		JsonObject taskConfig = null;
		String task = null;
		
		if (taskVo != null) {
			JsonParser parser = new JsonParser();
			taskConfig = (JsonObject) parser.parse(taskVo.getTaskConfig());
			task = taskVo.getTaskType();
		} else {
			task = paramMap.get("taskType").toString();
		}
			
		if (!task.equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)){
			
			// 사전 리스트 추가
			paramMap.put("pageSize", 1);
			List<DictionaryVo> dictionaries = dictionaryMapper.getDictionaryList(paramMap);
			
			if(!task.equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
				// 수용어 사전
				taskFormHtml.append("		<div class=\"drawDictionaries\">");
				taskFormHtml.append("			<span>수용어 사전</span>");
				
				// 수용어 공통사전
				//		taskFormHtml.append("			<p> 공통사전 사용</p><input type=\"checkbox\" name=\"whiteSharedEnabled\" value=\"Y\"");
				//		taskFormHtml.append(taskConfig!=null?(taskConfig.get("whiteSharedEnabled").getAsString().equals("Y")?"checked>":">"):">");
				
				taskFormHtml.append("			<select name=\"whiteDictionary\" class=\"mt5\">");
				taskFormHtml.append("				<option value=\"\">수용어 사전 선택</option>");
				
				for (int i = 0; i < dictionaries.size(); i++) {
					DictionaryVo dictionary = dictionaries.get(i);
					
					// 공통사전 제외
//				if (dictionary.getDictionarySharedYn().equals("Y")) continue;
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;				// 불용어 사전 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN)) continue;		// 불용 패턴 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD)) continue;			// 불용 어절 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD)) continue;	// 문장 분리 어절 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) continue;		// 매칭 패턴 제외
					
					taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
					if (taskConfig != null && !taskConfig.get("whiteDictionary").getAsString().equals("")) {
						if (taskConfig.get("whiteDictionary").getAsInt() == dictionary.getDictionaryId()) {
							taskFormHtml.append("selected>");
						} else {
							taskFormHtml.append(">");
						}
					} else {
						taskFormHtml.append(">");
					}
					
					taskFormHtml.append(dictionary.getDictionaryName());
					taskFormHtml.append("			</option>");
				}
				
				taskFormHtml.append("			</select>");
				taskFormHtml.append("		</div>");
				
				// 불용어 사전
				taskFormHtml.append("		<div class=\"drawDictionaries\">");
				taskFormHtml.append("			<span>불용어 사전</span>");
				
				// 불용어 공통사전
				//		taskFormHtml.append("			<p> 공통사전 사용</p><input type=\"checkbox\" name=\"blackSharedEnabled\" value=\"Y\"");
				//		taskFormHtml.append(taskConfig!=null?(taskConfig.get("blackSharedEnabled").getAsString().equals("Y")?"checked>":">"):">");
				
				taskFormHtml.append("			<select name=\"blackDictionary\" class=\"mt5\">");
				taskFormHtml.append("				<option value=\"\">불용어 사전 선택</option>");
				
				for (int i = 0; i < dictionaries.size(); i++) {
					DictionaryVo dictionary = dictionaries.get(i);
					
					// 공통사전 제외
//				if (dictionary.getDictionarySharedYn().equals("Y")) continue;
					
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;				// 수용어사전 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN)) continue;		// 불용 패턴 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD)) continue;			// 불용 어절 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD)) continue;	// 문장 분리 어절 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) continue;		// 매칭 패턴 제외
					
					taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
					if (taskConfig != null && !taskConfig.get("blackDictionary").getAsString().equals("")) {
						if (taskConfig.get("blackDictionary").getAsInt() == dictionary.getDictionaryId()) {
							taskFormHtml.append("selected>");
						} else {
							taskFormHtml.append(">");
						}
					} else {
						taskFormHtml.append(">");
					}
					taskFormHtml.append(dictionary.getDictionaryName());
					taskFormHtml.append("			</option>");
				}
				taskFormHtml.append("		</select>");
				taskFormHtml.append("	</div>");
			} else {
				// 매칭 패턴
				taskFormHtml.append("		<div class=\"drawDictionaries\">");
				taskFormHtml.append("			<span>매칭 패턴</span>");
				
				// 수용어 공통사전
				//		taskFormHtml.append("			<p> 공통사전 사용</p><input type=\"checkbox\" name=\"whiteSharedEnabled\" value=\"Y\"");
				//		taskFormHtml.append(taskConfig!=null?(taskConfig.get("whiteSharedEnabled").getAsString().equals("Y")?"checked>":">"):">");
				
				taskFormHtml.append("			<select name=\"groupPattern\" class=\"mt5\">");
				taskFormHtml.append("				<option value=\"\">매칭 패턴 선택</option>");
				
				for (int i = 0; i < dictionaries.size(); i++) {
					DictionaryVo dictionary = dictionaries.get(i);
					
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;				// 수용어 사전 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;				// 불용어 사전 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN)) continue;		// 불용 패턴 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD)) continue;			// 불용 어절 제외
					if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD)) continue;	// 문장 분리 어절 제외
					
					taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
					if (taskConfig != null && !taskConfig.get("groupPattern").getAsString().equals("")) {
						if (taskConfig.get("groupPattern").getAsInt() == dictionary.getDictionaryId()) {
							taskFormHtml.append("selected>");
						} else {
							taskFormHtml.append(">");
						}
					} else {
						taskFormHtml.append(">");
					}
					
					taskFormHtml.append(dictionary.getDictionaryName());
					taskFormHtml.append("			</option>");
				}
				
				taskFormHtml.append("			</select>");
				taskFormHtml.append("		</div>");
				
			}
		}

		taskFormHtml.append("	</div>");
		taskFormHtml.append("</from>");
		
		return taskFormHtml.toString();
	}
	
	public String taskFormType3 (Map<String, Object> paramMap, TaskVo taskVo) {
		
		// taskVo의 TaskConfig값 parsing
		JsonObject taskConfig = null;
		if (taskVo != null) {
			JsonParser parser = new JsonParser();
			taskConfig = (JsonObject) parser.parse(taskVo.getTaskConfig());
		}
		
		StringBuffer taskFormHtml = new StringBuffer();
		
		// 1) 전처리 설정
		taskFormHtml.append("<form id=\"taskForm\">");
		taskFormHtml.append("	<div id=\"drawTitleArea\">");
		taskFormHtml.append("		<span style=\"position:relative; float:left;\">서비스 모델명</span>");
		taskFormHtml.append("		<input type='text' class=\"ml20\" name=\"taskModelName\" value=\"" + (taskVo!=null?taskVo.getTaskName():"") + "\">");
		taskFormHtml.append("	</div>");
		taskFormHtml.append("	<div id=\"drawLeftArea\" style=\"width:48%;\">");
		taskFormHtml.append("		<span>전처리 설정</span>");
		taskFormHtml.append("		<div class=\"drawOptionArea\">");
		taskFormHtml.append("			<table class=\"basic_tbl_type mt10 text-ellipsis\">");
		taskFormHtml.append("				<colgroup>");
		taskFormHtml.append("					<col width=\"50%;\">");
		taskFormHtml.append("					<col width=\"50%;\">");
		taskFormHtml.append("				</colgroup>");
		taskFormHtml.append("				<thead>");
		taskFormHtml.append("					<th>옵션명</th>");
		taskFormHtml.append("					<th>옵션값</th>");
		taskFormHtml.append("				</thead>");
		taskFormHtml.append("			</table>");
		taskFormHtml.append("			<div class=\"optionList\">");
		taskFormHtml.append("				<table class=\"basic_tbl_type text-ellipsis\">");
		taskFormHtml.append("					<colgroup>");
		taskFormHtml.append("						<col width=\"50%;\">");
		taskFormHtml.append("						<col width=\"50%;\">");
		taskFormHtml.append("					</colgroup>");
		taskFormHtml.append("					<tbody id=\"options\">");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>불용 어절제거</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_black_words_removal\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_black_words_removal").getAsBoolean()==true?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_black_words_removal").getAsBoolean()==false?" selected>":">"):">");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>불용 패턴제거</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_pattern_removal\">");
		/* 231206 - 감성분석도 true로 고정
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_pattern_removal").getAsBoolean()==true?" selected>":">"):(paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)?"selected>":">"));
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_pattern_removal").getAsBoolean()==false?" selected>":">"):(paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)?"selected>":">"));
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		*/
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_black_words_removal").getAsBoolean()==true?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_black_words_removal").getAsBoolean()==false?" selected>":">"):">");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>문장 분리</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_sentence_separation\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_sentence_separation").getAsBoolean()==true?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_sentence_separation").getAsBoolean()==false?" selected>":">"):">");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>띄어쓰기 교정</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_spacing_correction\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_spacing_correction").getAsBoolean()==true?" selected>":">"):">");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_spacing_correction").getAsBoolean()==false?" selected>":">"):"selected>");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>발화 결합</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_speaker_combination\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_speaker_combination").getAsBoolean()==true?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_speaker_combination").getAsBoolean()==false?" selected>":">"):">");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("						<tr>");
		taskFormHtml.append("							<td>문장 분리 어절 적용</td>");
		taskFormHtml.append("							<td>");
		taskFormHtml.append("								<select class=\"w180\" name=\"apply_split_sentence_word\">");
		taskFormHtml.append("									<option value=\"true\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_split_sentence_word").getAsBoolean()==true?" selected>":">"):"selected>");
		taskFormHtml.append("										true");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("									<option value=\"false\"");
		taskFormHtml.append(taskConfig!=null?(taskConfig.getAsJsonObject("preprocessOption").get("apply_split_sentence_word").getAsBoolean()==false?" selected>":">"):">");
		taskFormHtml.append("										false");
		taskFormHtml.append("									</option>");
		taskFormHtml.append("								</select>");
		taskFormHtml.append("							</td>");
		taskFormHtml.append("						</tr>");
		taskFormHtml.append("					</tbody>");
		taskFormHtml.append("				</table>");
		taskFormHtml.append("			</div>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("	</div>");
		
		// 사전 및 패턴 리스트 가져오기 
		paramMap.put("pageSize", 1);
		List<DictionaryVo> dictionaries = dictionaryMapper.getDictionaryList(paramMap);
		
		// 2) 패턴 설정 양식
		taskFormHtml.append("	<div id=\"drawRightArea\" style=\"width:48%;\">");
		taskFormHtml.append("		<span>패턴 설정</span>");
		taskFormHtml.append("		<hr style=\"border:1px solid #0054b3;margin-top:10px;\"/>");
		taskFormHtml.append("		<div class=\"drawDictionaries\">");
		taskFormHtml.append("			<span>불용 패턴</span>");		
		taskFormHtml.append("			<select name=\"blackPattern\" class=\"mt5\">");
		taskFormHtml.append("				<option value=\"\">불용 패턴 선택</option>");
		
		for (int i = 0; i < dictionaries.size(); i++) {
			DictionaryVo dictionary = dictionaries.get(i);
			
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;				// 수용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;				// 불용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD)) continue;			// 불용어절 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD)) continue;	// 문장분리어절 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) continue;		// 매칭 패턴 제외
			
			taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
			taskFormHtml.append(taskConfig!=null?(taskConfig.get("blackPattern").getAsString().equals(String.valueOf(dictionary.getDictionaryId()))?"selected>":">"):">");
			taskFormHtml.append(dictionary.getDictionaryName());
			taskFormHtml.append("			</option>");
		}
		
		taskFormHtml.append("			</select>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("		<div class=\"drawDictionaries\">");
		taskFormHtml.append("			<span>불용 어절</span>");
		taskFormHtml.append("			<select name=\"blackWord\" class=\"mt5\">");
		taskFormHtml.append("				<option value=\"\">불용 어절 선택</option>");
		
		for (int i = 0; i < dictionaries.size(); i++) {
			DictionaryVo dictionary = dictionaries.get(i);
			
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;				// 수용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;				// 불용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN)) continue;		// 불용패턴 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD)) continue;	// 문장분리어절 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) continue;		// 매칭 패턴 제외
			
			taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
			taskFormHtml.append(taskConfig!=null?(taskConfig.get("blackWord").getAsString().equals(String.valueOf(dictionary.getDictionaryId()))?"selected>":">"):">");
			taskFormHtml.append(dictionary.getDictionaryName());
			taskFormHtml.append("			</option>");
		}

		taskFormHtml.append("			</select>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("		<div class=\"drawDictionaries\">");
		taskFormHtml.append("			<span>문장 분리 어절</span>");
		taskFormHtml.append("			<select name=\"splitSentWord\" class=\"mt5\">");
		taskFormHtml.append("				<option value=\"\">문장 분리 어절 선택</option>");
		
		for (int i = 0; i < dictionaries.size(); i++) {
			DictionaryVo dictionary = dictionaries.get(i);
			
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) continue;				// 수용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) continue;				// 불용어 사전 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN)) continue;		// 불용패턴 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD)) continue;			// 불용어절 제외
			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) continue;		// 매칭 패턴 제외
			
			taskFormHtml.append("			<option value=\"" + dictionary.getDictionaryId() + "\" ");
			taskFormHtml.append(taskConfig!=null?(taskConfig.get("splitSentWord").getAsString().equals(String.valueOf(dictionary.getDictionaryId()))?"selected>":">"):">");
			taskFormHtml.append(dictionary.getDictionaryName());
			taskFormHtml.append("			</option>");
		}

		taskFormHtml.append("			</select>");
		taskFormHtml.append("		</div>");
		taskFormHtml.append("	</div>");
		taskFormHtml.append("</from>");
		
		return taskFormHtml.toString();
	}
	
	// Classifier 설정 정보 변경
	public void updateTrainConfig(String flag, TaskVo taskVo, TaskVo beforeTaskVo) {
		
		
		// 파일 변경처리를 위한 타입 변경처리
		JsonObject paramMap = new JsonObject();
		JsonObject task = new JsonObject();
		JsonObject beforeTask = null;
		
		// taskVo 변환
		try {
			Field [] fields = taskVo.getClass().getDeclaredFields();
			
			for (Field field : fields) {
				field.setAccessible(true);
				// logger.info("@@ taskVo convert : {} / {}", field.getName(), field.get(taskVo)==null?"":field.get(taskVo).toString());
				task.addProperty(field.getName(), field.get(taskVo)==null?"":field.get(taskVo).toString());
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		// beforeTaskVo 변환
		if (beforeTaskVo != null) {
			beforeTask = new JsonObject();
			Field [] fields = beforeTaskVo.getClass().getDeclaredFields();
			
			try {
				for (Field field : fields) {
					field.setAccessible(true);
					// logger.info("@@ beforeTaskVo convert : {} / {}", field.getName(), field.get(beforeTaskVo)==null?"":field.get(beforeTaskVo).toString());
					beforeTask.addProperty(field.getName(), field.get(beforeTaskVo)==null?"":field.get(beforeTaskVo).toString());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		// param 설정
		paramMap.addProperty("flag", flag);
		paramMap.add("task", task);
		paramMap.add("beforeTask", beforeTask);
		
		// api URI 설정
		URI uri = UriComponentsBuilder
			 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/updateTrainConfig")
			 .build()
			 .toUri();

		// Header 및 Body 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<>(paramMap.toString(), headers);
		
		// API Call
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(10000);
		factory.setReadTimeout(10000);			
		
		RestTemplate restTemplate = new RestTemplate(factory);
		ResponseEntity<String>responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
		
		logger.info("@@ updateTrainConfig Result : " + responseEntity.getBody().toString());
	}
}
