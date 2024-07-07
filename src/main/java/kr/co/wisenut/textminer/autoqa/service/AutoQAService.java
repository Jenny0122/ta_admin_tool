package kr.co.wisenut.textminer.autoqa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.autoqa.vo.AutoQaSimScriptVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.autoqa.mapper.AutoQAMapper;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaCateInfoVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaScriptVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
//import kr.co.wisenut.textminer.autoqa.vo.AutoQaCateInfoVo;
//import kr.co.wisenut.textminer.autoqa.vo.AutoQaSimScriptVo;
//import kr.co.wisenut.textminer.autoqa.vo.AutoQaKeywordVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class AutoQAService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AutoQAMapper autoQaMapper;

	@Autowired
	private ImportProgressMapper importProgressMapper;

	// 상담분류 리스트 조회
	public Map<String, Object> getQAScriptList(Map<String, Object> paramMap) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			// 전체 건수
			double totalCount = autoQaMapper.getQAScriptTotalCount(paramMap);
			System.out.println("AUTOQA totalCount = " + totalCount);

			resultMap.put("totalCount", totalCount);
			// 조회결과 리스트
			List<AutoQaScriptVo> resultList = autoQaMapper.getQAScriptList((paramMap));
			System.out.println("AUTOQA resultList = " + resultList);

			resultMap.put("dataTable", convertHtmlTagForQAScriptList(resultList, Integer.parseInt(paramMap.get("pageRow").toString()), paramMap.get("contextPath").toString()));
			// 페이징
			resultMap.put("pageNav", PageUtil.createPageNav(totalCount, paramMap));

		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}

		return resultMap;
	}

	// 스크립트 등록
	public Map<String, Object> insertQAScript(AutoQaScriptVo autoqascriptVo) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			int result = autoQaMapper.insertQAScript(autoqascriptVo);

			if (result > 0) {
				resultMap.put("scriptId", autoqascriptVo.getScriptId());
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사전 등록 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사전 등록 작업을 실패하였습니다.");
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

	// 스크립트 수정
	public Map<String, Object> updateQAScript(AutoQaScriptVo autoqascriptVo) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			int result = autoQaMapper.updateQAScript(autoqascriptVo);

			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "스크립트 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "스크립트 수정 권한이 없습니다.");
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

	// 스크립트 삭제
	public Map<String, Object> deleteQAScript(AutoQaScriptVo autoqascriptVo) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			int result = autoQaMapper.deleteQAScript(autoqascriptVo);

			if (result > 0) {
				// 사전 업로드 이력 삭제
				ImportProgressVo importProgressVo = new ImportProgressVo();
				importProgressVo.setResourceId(autoqascriptVo.getScriptId());
				importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_AUTOQA);
				importProgressMapper.deleteImportProgress(importProgressVo);

				// 결과 리턴
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "스크립트 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "스크립트 삭제 권한이 없습니다.");
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

	// 준수항목 조회
		public Map<String, Object> getCompCdInfo(Map<String, Object> paramMap) {

			Map<String, Object> resultMap = new HashMap<String, Object>();
			// 조회결과 리스트
			List<AutoQaScriptVo> resultList = autoQaMapper.getCompCdInfo((paramMap));

			try {
				//이부분에 스크립트 아이디에 등록된 준수항목 set하는 부분 추가필요
					paramMap.put( null, resultList);

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

	public String convertHtmlTagForQAScriptList(List<AutoQaScriptVo> resultList, int pageRow, String contextPath) {
		StringBuffer convertHtml = new StringBuffer();

		// colgroup 설정시작
		convertHtml.append("<colgroup>\n");
		convertHtml.append("\t<col width=\"6%;\">\n");
		convertHtml.append("\t<col width=\"6%;\">\n");
		convertHtml.append("\t<col width=\"9%;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
		convertHtml.append("\t<col width=\"*;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
		convertHtml.append("</colgroup>\n\n");
		// colgroup 설정종료

		// thead 설정시작
		convertHtml.append("<thead>\n");
		convertHtml.append("\t<th>선택</th>\n");
		convertHtml.append("\t<th>NO.</th>\n");
		convertHtml.append("\t<th>준수항목</th>\n");
		convertHtml.append("\t<th>배점</th>\n");
		convertHtml.append("\t<th>스크립트</th>\n");
		convertHtml.append("\t<th>유사문장</th>\n");
		convertHtml.append("\t<th>키워드</th>\n");
		convertHtml.append("</thead>\n\n");
		// thead 설정종료

		// tbody 설정시작
		convertHtml.append("<tbody>\n");

		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"8\">생성된 스크립트가 없습니다. 스크립트를 생성해주세요.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr>\n");
				// 선택
 				convertHtml.append("\t\t<td><input type=\"checkbox\" name=\"entry_choice\" value=\"" + resultList.get(i).getScriptId() + "\"</td>\n");
 				// No.
 				convertHtml.append("\t\t<td class=\"no\">" + ((i + 1) + (pageRow * 10) - 10) + "</td>\n");
 				// 준수항목
 				convertHtml.append("\t\t<td class=\"compItemcd\">" + resultList.get(i).getComplianceItemCd() +"</td>\n");

				// 배점
				convertHtml.append("\t\t<td class=\"score\">");
				convertHtml.append(resultList.get(i).getScore());
				convertHtml.append("</td>\n");

				// 스크립트
				convertHtml.append("\t\t<td>");
				convertHtml.append(resultList.get(i).getScriptCont());
				convertHtml.append("\t\t\t<a href=\"#\" title=\"클릭 시 스크립트 수정\" onclick=\"showPopup('update', '" + resultList.get(i).getScriptCont() + "')\">");
				convertHtml.append("</td>\n");

				// 유사문장
				convertHtml.append("\t\t<td>");
				convertHtml.append("<button type=\"button\" class=\"btn test_btn btn_gray w98\" onclick=\"showPopupRowDetail('sim_script', '"+ resultList.get(i).getScriptId() + "')\"><i class=\"fas fa-link mr5\"></i>유사문장</button>\n");
				convertHtml.append("</td>\n");

				// 키워드
				convertHtml.append("\t\t<td>");
				convertHtml.append("\t\t\t<button type=\"button\" id=\"keyBtn_" + resultList.get(i).getScriptId() + "\" class=\"btn_red w84\" style=\"display:none;\" onclick=\"keySave(" + resultList.get(i).getScriptId() + ")\">키워드</button>\n");
				convertHtml.append("</td>\n");
				convertHtml.append("\t</tr>");
			}
		}

		convertHtml.append("<tbody>\n");
		// tbody 설정종료

		return convertHtml.toString();
	}

	// 상담카테고리 Depth 구조 조회
	public List<AutoQaCateInfoVo> getQACategory(Map<String, Object> paramMap) {
		return autoQaMapper.getQACategory(paramMap);
	}
}
