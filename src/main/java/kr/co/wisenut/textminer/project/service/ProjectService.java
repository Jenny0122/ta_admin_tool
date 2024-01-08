package kr.co.wisenut.textminer.project.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.deploy.mapper.DeployMapper;
import kr.co.wisenut.textminer.deploy.service.DeployService;
import kr.co.wisenut.textminer.deploy.vo.DeployVo;
import kr.co.wisenut.textminer.project.mapper.ProjectMapper;
import kr.co.wisenut.textminer.project.vo.ProjectVo;
import kr.co.wisenut.textminer.task.mapper.TaskMapper;
import kr.co.wisenut.textminer.task.vo.TaskVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class ProjectService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProjectMapper projectMapper;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private DeployService deployService;
	
	@Autowired
	private DeployMapper deployMapper;
	
	// 프로젝트 리스트 조회(SelectBox)
	public List<Map<String, Object>> getProjectListForSelectBox(Map<String, Object> paramMap) {
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		try {
			// 조회결과 리스트
			resultList = projectMapper.getProjectListForSelectBox(paramMap);
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultList;
	}
	
	// 프로젝트 리스트 조회
	public Map<String, Object> getProjectList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 전체 건수
			resultMap.put("totalCount", projectMapper.getProjectTotalCount(paramMap));		
			
			// 조회결과 리스트
			List<Map<String, Object>> resultList = projectMapper.getProjectList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForProjectList(resultList, paramMap.get("contextPath").toString()));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 프로젝트 상세 조회
	public Map<String, Object> getProjectDetail(ProjectVo projectVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 상세조회
			resultMap.put("project", projectMapper.getProjectDetail(projectVo));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 프로젝트 등록
	public Map<String, Object> insertProject(ProjectVo projectVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = projectMapper.insertProject(projectVo);
			
			if (result > 0) {
				resultMap.put("projectId", projectVo.getProjectId());
				resultMap.put("projectName", projectVo.getProjectName());
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "프로젝트 등록 작업이 완료되었습니다.");
			} else {
				resultMap.put("projectName", projectVo.getProjectName());
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "프로젝트 등록 작업을 실패하였습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("등록 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("등록 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 프로젝트 수정
	public Map<String, Object> updateProject(ProjectVo projectVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = projectMapper.updateProject(projectVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "프로젝트 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "프로젝트 수정 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("수정 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("수정 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 프로젝트 삭제
	public Map<String, Object> deleteProject(ProjectVo projectVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = projectMapper.deleteProject(projectVo);
			
			if (result > 0) {
				TaskVo taskVo = new TaskVo();
				taskVo.setProjectId(projectVo.getProjectId());
				taskMapper.deleteTask(taskVo);
				
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "프로젝트 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "프로젝트 삭제 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("삭제 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	public String convertHtmlTagForProjectList(List<Map<String, Object>> resultList, String contextPath) {
		
		// 테스크 타입
		String [] taskType = { "AUTO_CLASSIFICATION"			// 자동분류
							 , "EMOTION_ANALYZE"				// 감성분석
							 , "DOCUMENT_SUMMARY"				// 문서요약
							 , "KEYWORD_EXTRACTION"				// 키워드추출
							 , "RELATED_EXTRACTION"				// 연관어추출
							 , "EMOTION_PREPROECESS"			// 전처리(감성)
							 , "SUMMARY_PREPROECESS"			// 전처리(요약)
							 , "STRING_MATCHER"					// 문자열 매칭
							 };
		
		// 리턴될 HTML
		StringBuffer convertHtml = new StringBuffer();
		
		// 테스크 조회를 위한 map
		Map<String, Object> paramMap = null;
		
		int seq = 0;
		int beforeProjectId = 0;
		
		// 조회결과 없을경우
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"9\">등록된 프로젝트가 없습니다.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				seq++;
				
				convertHtml.append("\t<tr class=\"collection\">\n");
					
				// NO.
				convertHtml.append("\t\t<td rowspan=\"" + (resultList.get(i).get("taskCnt").toString().equals("0")?1:resultList.get(i).get("taskCnt")) + "\">" + seq + "</td>\n");
				
				// 프로젝트 명
				convertHtml.append("\t\t<td rowspan=\"" + (resultList.get(i).get("taskCnt").toString().equals("0")?1:resultList.get(i).get("taskCnt")) + "\">\n");
				convertHtml.append("\t\t\t<a href=\"" + contextPath + "/task/" + resultList.get(i).get("projectId") + "\" title=\"클릭 시 [서비스 관리] 이동\" >");
				convertHtml.append(resultList.get(i).get("projectName") + "</a>");
				convertHtml.append("\t\t</td>\n");
				
				// 프로젝트 설명
				convertHtml.append("\t\t<td rowspan=\"" + (resultList.get(i).get("taskCnt").toString().equals("0")?1:resultList.get(i).get("taskCnt")) + "\" class=\"field-desc\" title=\"" + resultList.get(i).get("projectDesc") + "\">\n");
				convertHtml.append("\t\t\t<a href=\"#\" title=\"클릭 시 프로젝트 수정\" onclick=\"showPopup('update', '" + resultList.get(i).get("projectId") + "')\">");
				convertHtml.append(resultList.get(i).get("projectDesc"));
				convertHtml.append("\t\t</a></td>\n");

				if (resultList.get(i).get("taskCnt").toString().equals("0")) {
					convertHtml.append("\t\t<td class=\"task_type\"></td>\n");			// 서비스명
					convertHtml.append("\t\t<td class=\"task_name\"></td>\n");			// 서비스명
					convertHtml.append("\t\t<td class=\"task_model\"></td>\n");			// 모델명
					convertHtml.append("\t\t<td class=\"task_last_analyze_dt\"></td>");	// 최근분석일
					convertHtml.append("\t\t<td class=\"task_enabled\"></td>\n");		// 모델상태
					convertHtml.append("\t\t<td class=\"task_job\"></td>\n");			// 생성/분석/결과
					convertHtml.append("\t\t<td><button type=\"button\" class=\"btn btn_red w70 ml5\" onclick=\"deleteProject(" + resultList.get(i).get("projectId") + ");\"><i class=\"fas fa-trash-alt  mr5\"></i> 삭제</button></td>\n");			// 프로젝트 삭제버튼
					convertHtml.append("\t</tr>\n");
				} else {
					for (int j = 0; j < taskType.length; j++) {
						
						// 테스크 조회
						paramMap = new HashMap<String, Object>();
						paramMap.put("projectId", resultList.get(i).get("projectId"));
						paramMap.put("taskType", taskType[j]);
						List<TaskVo> taskList = taskMapper.getTaskList(paramMap);
						
						if (taskList != null) {

							// 테스크별 출력건수 제한을 위한 변수
							int taskCnt = 0;
							
							if (taskList.size() > 3) {
								taskCnt = 3;
							} else {
								taskCnt = taskList.size();
							}
							
							for (int k = 0; k < taskCnt; k++) {
								
								if (k == 0) {
									convertHtml.append("\t\t<td class=\"task_type\" rowspan=\"" + taskCnt + "\">" + taskList.get(k).getLabel() + "</td>");
								}
								
								// 서비스명
								convertHtml.append("\t\t<td class=\"task_name\"><a href=\"" + contextPath + "/task/" + taskList.get(k).getProjectId() + "/" + taskList.get(k).getTaskType() + "/" + taskList.get(k).getTaskId() + "\" title=\"클릭 시 [서비스 관리] 이동\">" + taskList.get(k).getTaskName() + "</a></td>");
								
								// 모델명
								convertHtml.append("\t\t<td class=\"task_model\" style=\"text-align:left;\">" + taskList.get(k).getModelFile() + "</td>");
								
								// 최근분석
								convertHtml.append("\t\t<td class=\"task_last_analyze_dt\">" + taskList.get(k).getLastAnalyzeDt() + "</td>");

								// 모델상태
								if (!taskType[j].equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
								 && !taskType[j].equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)
								 && !taskType[j].equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
									convertHtml.append("\t\t<td class=\"task_enabled\">");
									convertHtml.append("\t\t\t<input type=\"checkbox\" id=\"enabled_" + taskList.get(k).getProjectId() + taskList.get(k).getTaskId() + "\"");
									convertHtml.append(" style=\"display:none;\" onclick=\"chgEnabled(" + taskList.get(k).getProjectId() + ", " + taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "')\" ");
									convertHtml.append((taskList.get(k).getEnabled().equals("Y")?"checked":"") + ">");
									convertHtml.append("<label for=\"enabled_" + taskList.get(k).getProjectId() + taskList.get(k).getTaskId() + "\"></label>");
									convertHtml.append("\t\t</td>");
								} else {
									convertHtml.append("\t\t<td class=\"task_enabled\"></td>");
								}
								
								// 생성/분석/결과
								convertHtml.append("\t\t<td class=\"task_job\" style=\"text-align:right;\">");
								
								// 모델 생성은 자동분류, 감성분석에서만 사용 / 사전/패턴 설정은 키워드 추출, 연관어 추출, 전처리에서만 사용
								if (taskType[j].equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
								 || taskType[j].equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn train_btn btn_gray w108\" onclick=\"createModel(" + taskList.get(k).getProjectId() + ", " + taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-folder-plus mr5\"></i>모델 생성</button>");
								} else if (taskType[j].equals(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION) 
										|| taskType[j].equals(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION)) {
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn train_btn btn_gray w108\" onclick=\"replaceDictionary(" + taskList.get(k).getProjectId() + ", " + taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-book mr5\"></i>사전 설정</button>");
								} else if (taskType[j].equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
										|| taskType[j].equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn train_btn btn_gray w108\" onclick=\"replaceDictionary(" + taskList.get(k).getProjectId() + ", " + taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-book mr5\"></i>패턴 설정</button>");
								} else if (taskType[j].equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn train_btn btn_gray w205\" onclick=\"replaceDictionary(" + taskList.get(k).getProjectId() + ", " + taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-book mr5\"></i>패턴 설정</button>");
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn delete_btn btn_gray w205\" onclick=\"deleteModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ")\"><i class=\"fas fa-trash-alt mr5\"></i>모델 삭제</button>");
								}
								
								// 문서요약의 경우, 버튼이 3개만 활성화 됨으로 class 변경필요
								if (taskType[j].equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn predict_btn btn_gray w137\" onclick=\"showAnalyzePopup(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ", '" +taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-file-medical-alt mr5\"></i>분석</button>");
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn result_btn btn_gray w137\" onclick=\"resultModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "', '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-file-excel mr5\"></i>결과보기</button>");
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn delete_btn btn_gray w137\" onclick=\"deleteModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ")\"><i class=\"fas fa-trash-alt mr5\"></i>모델 삭제</button>");
								} 
								// 문자열 매칭의 버튼은 여기서 처리하지 않는다.
								else if (!taskType[j].equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {	
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn predict_btn btn_gray w88\" onclick=\"showAnalyzePopup(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ", '" +taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-file-medical-alt mr5\"></i>분석</button>");
									if (taskType[j].equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) || taskType[j].equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
										convertHtml.append("\t\t\t<button type=\"button\" class=\"btn result_btn btn_gray w108\" onclick=\"resultModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ", '" + taskList.get(k).getModelFile() + "', '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-file-excel mr5\"></i>결과보기</button>");
									} else {
										convertHtml.append("\t\t\t<button type=\"button\" class=\"btn result_btn btn_gray w108\" onclick=\"resultModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ", '" + taskList.get(k).getTaskType() + "', '" + taskList.get(k).getTaskType() + "')\"><i class=\"fas fa-file-excel mr5\"></i>결과보기</button>");
									}
									convertHtml.append("\t\t\t<button type=\"button\" class=\"btn delete_btn btn_gray w108\" onclick=\"deleteModel(" + taskList.get(k).getProjectId() + ", " +taskList.get(k).getTaskId() + ")\"><i class=\"fas fa-trash-alt mr5\"></i>모델 삭제</button>");
								}
								
								convertHtml.append("\t\t</td>");
								
								// 프로젝트 삭제버튼
								if (i == 0) {
									if (beforeProjectId != Integer.parseInt(resultList.get(i).get("projectId").toString())) {
										beforeProjectId = Integer.parseInt(resultList.get(i).get("projectId").toString());
										convertHtml.append("\t\t<td rowspan=\"" + (resultList.get(i).get("taskCnt").toString().equals("0")?1:resultList.get(i).get("taskCnt")) + "\">");
										convertHtml.append("\t\t<button type=\"button\" class=\"btn btn_red w70 ml5\" onclick=\"deleteProject(" + resultList.get(i).get("projectId") + ");\"><i class=\"fas fa-trash-alt  mr5\"></i> 삭제</button></td>\n");			// 프로젝트 삭제버튼
										convertHtml.append("\t\t</td>\n");
									}
								} else if (beforeProjectId != Integer.parseInt(resultList.get(i).get("projectId").toString())) {
									beforeProjectId = Integer.parseInt(resultList.get(i).get("projectId").toString());
									convertHtml.append("\t\t<td rowspan=\"" + (resultList.get(i).get("taskCnt").toString().equals("0")?1:resultList.get(i).get("taskCnt")) + "\">");
									convertHtml.append("\t\t<button type=\"button\" class=\"btn btn_red w70 ml5\" onclick=\"deleteProject(" + resultList.get(i).get("projectId") + ");\"><i class=\"fas fa-trash-alt  mr5\"></i> 삭제</button></td>\n");			// 프로젝트 삭제버튼
									convertHtml.append("\t\t</td>\n");
								}
								
								convertHtml.append("\t</tr>\n");
							}
						}
					}
				}
			}
		}
		
		return convertHtml.toString();
	}
	
	// 테스트 모듈 상태확인
	public Map<String, Object> getTestModuleStatus(){

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			DeployVo deployVo = new DeployVo();
			deployVo.setServerType(TextMinerConstants.SERVER_TYPE_TEST);
			
			List<DeployVo> testModuleList = deployMapper.getDeployList(deployVo);
			
			resultMap.put("trafficContent", requestTestModuleStatus(testModuleList));

		} catch (NullPointerException e) {
			logger.error("요청 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
			resultMap.put("trafficContent", requestTestModuleStatus(null));
		} catch (Exception e) {
			logger.error("요청 작업을 실패하였습니다.");
			e.printStackTrace();
			resultMap.put("trafficContent", requestTestModuleStatus(null));
		}

		return resultMap;
	}
	
	public String requestTestModuleStatus(List<DeployVo> testModuleList) {
		StringBuffer trafficContent = new StringBuffer();
		
		Map<String, Object> statusMap = new HashMap<String, Object>();
		
		if (testModuleList == null) {
			trafficContent.append("<td align=\"center\" colspan=\"5\">등록된 모듈이 없습니다.</td>");
		} else {
			DeployVo testModule = null;
			
			for(int i = 0; i < testModuleList.size(); i++) {
				testModule = testModuleList.get(i);
				
				statusMap = deployService.requestHealthCheckModule(testModule.getServerIp(), testModule.getServerPort());
				
				// 자동분류, 감성분석은 학습상태를 확인하여 리턴할 수 있도록 한다.
				if (testModule.getServerTask().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
				 || testModule.getServerTask().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
					statusMap = deployService.requestStatusCheckModule(testModule.getServerIp(), testModule.getServerPort());
				}
				
				trafficContent.append("<td>\n");
				trafficContent.append("<button type=\"button\" id=\"statusBtn_" + testModule.getServerId() + "\" class=\"radiusBtn w120\" style=\"cursor:default;\" disabled>\n");
				
				trafficContent.append("<div class=\"" + statusMap.get("resultCss") + " mr5\"></div>");
				trafficContent.append(statusMap.get("resultTxt") + "\n");
				trafficContent.append("</button>\n");
				trafficContent.append("</td>\n");
			}
		}
		
		return trafficContent.toString();
	}
}
