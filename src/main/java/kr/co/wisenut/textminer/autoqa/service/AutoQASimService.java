package kr.co.wisenut.textminer.autoqa.service;

import kr.co.wisenut.textminer.autoqa.mapper.AutoQAMapper;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaScriptVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaSimScriptVo;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AutoQASimService {

    @Autowired
    private AutoQAMapper autoQaMapper;

    // 상담분류 리스트 조회
    public Map<String, Object> getQAScriptListTest(Map<String, Object> paramMap) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            // 전체 건수
            double totalCount = autoQaMapper.getQAScriptTotalCount(paramMap);
            log.debug("AUTOQA totalCount = " + totalCount);

            resultMap.put("totalCount", totalCount);
            // 조회결과 리스트
            List<AutoQaScriptVo> resultList = autoQaMapper.getQAScriptList((paramMap));
            log.debug("AUTOQA resultList = " + resultList);

            resultMap.put("dataTable", resultList);
            // 페이징
//            resultMap.put("pageNav", PageUtil.createPageNav(totalCount, paramMap));

        } catch (NullPointerException e) {
            log.error("조회 시 누락된 값에 의한 오류발생!");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("조회 작업을 실패하였습니다.");
            e.printStackTrace();
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 조회
     */
    public Map<String, Object> getQASimScript( Map<String, Object> paramMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {

            // 전체 건수
            double totalCount = autoQaMapper.getQAScriptTotalCount(paramMap);
            System.out.println("AUTOQASIM totalCount = " + totalCount);

            resultMap.put("totalCount", totalCount);
            // 조회결과 리스트
            List<AutoQaSimScriptVo> resultList = autoQaMapper.getQASimScriptList(paramMap);
            System.out.println("AUTOQASIM resultList = " + resultList);

//            resultMap.put("dataTable", convertHtmlTagForQAScriptList(resultList, Integer.parseInt(paramMap.get("pageRow").toString()), paramMap.get("contextPath").toString()));
            // 페이징
//            resultMap.put("pageNav", PageUtil.createPageNav(totalCount, paramMap));

        } catch (NullPointerException e) {
            log.error("조회 시 누락된 값에 의한 오류발생!");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("조회 작업을 실패하였습니다.");
            e.printStackTrace();
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 추가
     */
    public Map<String, Object> insertQASimScript(AutoQaSimScriptVo autoQaSimScriptVo) {

        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {

            int result = autoQaMapper.insertQASimScript(autoQaSimScriptVo);

            if (result > 0) {
                resultMap.put("SimscriptId", autoQaSimScriptVo.getSimScriptId());
                resultMap.put("result", "S");
                resultMap.put("resultMsg", "유사 스크립트 등록 작업이 완료되었습니다.");
            } else {
                resultMap.put("result", "F");
                resultMap.put("resultMsg", "유사 스크립트 등록 작업을 실패하였습니다.");
            }
        } catch (NullPointerException e) {
            log.error("등록 시 누락된 값에 의한 오류발생!");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("등록 작업을 실패하였습니다.");
            e.printStackTrace();
        }

        return resultMap;
    }

    /**
     *	유사 스크립트 수정
     */
    public Map<String, Object> updateQASimScript(AutoQaSimScriptVo autoQaSimScriptVo) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            int result = autoQaMapper.updateQASimScript(autoQaSimScriptVo);

            if (result > 0) {
                resultMap.put("result", "S");
                resultMap.put("resultMsg", "유사 스크립트 수정 작업이 완료되었습니다.");
            } else {
                resultMap.put("result", "F");
                resultMap.put("resultMsg", "유사 스크립트 수정 권한이 없습니다.");
            }
        } catch (NullPointerException e) {
            log.error("수정 시 누락된 값에 의한 오류발생!");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("수정 작업을 실패하였습니다.");
            e.printStackTrace();
        }

        return resultMap;
    }
    /**
     * 유사 스크립트 삭제
     */
    public Map<String, Object> deleteQASimScript(AutoQaSimScriptVo autoQaSimScriptVo) {

        Map<String, Object> resultMap = new HashMap<>();

        try {

            int result = autoQaMapper.deleteQASimScript(autoQaSimScriptVo);

            if (result > 0) {
                // 결과 리턴
                resultMap.put("result", "S");
                resultMap.put("resultMsg", "유사 스크립트 삭제 작업이 완료되었습니다.");
            } else {
                resultMap.put("result", "F");
                resultMap.put("resultMsg", "유사 스크립트 삭제 권한이 없습니다.");
            }

        } catch (NullPointerException e) {
            log.error("삭제 시 누락된 값에 의한 오류발생!");
            e.printStackTrace();
        } catch (Exception e) {
            log.error("삭제 작업을 실패하였습니다.");
            e.printStackTrace();
        }

        return resultMap;
    }

}
