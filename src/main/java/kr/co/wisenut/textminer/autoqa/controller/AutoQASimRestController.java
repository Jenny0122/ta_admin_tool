package kr.co.wisenut.textminer.autoqa.controller;

import kr.co.wisenut.textminer.autoqa.service.AutoQASimService;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaSimScriptVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping( "/autoqaRest" )
@Slf4j
@RequiredArgsConstructor
public class AutoQASimRestController {

    private final AutoQASimService autoQASimService;

    private final ActionHistoryService actionHistoryService;

    // 스크립트 리스트 조회
    @PostMapping("/getQAScriptListTest")
    public Map<String, Object> getQAScriptList( @RequestBody Map<String, Object> paramMap
            , HttpServletRequest request
            , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            paramMap.put("pageSize", 10);
            paramMap.put("role", user.getAuthorities().toString());
            paramMap.put("contextPath", request.getContextPath());

            resultMap = autoQASimService.getQAScriptListTest(paramMap);
        } catch (Exception e) {
            log.error("tm-user getDataList failed. {}", e.getMessage());
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 리스트 조회
     */
    @PostMapping( "/getQASimScriptList" )
    public Map<String, Object> getQASimScriptList( @RequestBody Map<String, Object> paramMap
            , HttpServletRequest request
            , @AuthenticationPrincipal( errorOnInvalidType = true ) TmUser user ) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            paramMap.put( "pageSize" , 10 );
            paramMap.put( "role" , user.getAuthorities()
                                       .toString() );
            paramMap.put( "contextPath" , request.getContextPath() );

            resultMap = autoQASimService.getQASimScript( paramMap );
        } catch( Exception e ) {
            log.error( "tm-user getDataList failed. {}" , e.getMessage() );
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 등록
     */
    @PostMapping( value = "/insertQASimScript" )
    public Map<String, Object> insertQASimScript( AutoQaSimScriptVo autoQaSimScriptVo
            , HttpServletRequest request
            , @AuthenticationPrincipal( errorOnInvalidType = true ) TmUser user ) {

        Map<String, Object> resultMap = new HashMap<>();

        try {
            autoQaSimScriptVo.setCreUser( user.getUsername() );
            autoQaSimScriptVo.setModUser( user.getUsername() );

            resultMap = autoQASimService.insertQASimScript( autoQaSimScriptVo );

            // 등록 완료 후 이력 저장
            if( resultMap.get( "result" )
                         .toString()
                         .equals( "S" ) ) {
                ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

                actionHistoryVo.setActionUser( user.getUsername() );
                actionHistoryVo.setResourceId( "0" );
                actionHistoryVo.setResourceType( TextMinerConstants.COMMON_BLANK );
                actionHistoryVo.setActionType( TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_INSERT );
                actionHistoryVo.setActionMsg( user.getUsername() + " 유사 스크립트 등록 ( 등록 유사 스크립트 : " + autoQaSimScriptVo.getSimScriptCont() + " )" );

                if( request.getRemoteAddr()
                           .equals( "0:0:0:0:0:0:0:1" ) ) {
                    actionHistoryVo.setUserIp( "127.0.0.1" );
                } else {
                    actionHistoryVo.setUserIp( request.getRemoteAddr() );
                }

                actionHistoryService.insertActionHistory( actionHistoryVo );
            }
        } catch( Exception e ) {
            log.error( "tm-user insertCollection failed. {}" , e.getMessage() );
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 수정
     */
    @DeleteMapping( "/updateQASimScript" )
    public Map<String, Object> updateQASimScript( AutoQaSimScriptVo autoQaSimScriptVo
            , HttpServletRequest request
            , @AuthenticationPrincipal( errorOnInvalidType = true ) TmUser user ) {

        Map<String, Object> resultMap = new HashMap<>();


        try {
            autoQaSimScriptVo.setRole( user.getAuthorities()
                                           .toString() );
            autoQaSimScriptVo.setModUser( user.getUserName() );
            resultMap = autoQASimService.updateQASimScript( autoQaSimScriptVo );

            // 삭제 완료 후 이력 저장
            if( resultMap.get( "result" )
                         .toString()
                         .equals( "S" ) ) {
                ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

                actionHistoryVo.setActionUser( user.getUsername() );
                actionHistoryVo.setResourceId( "0" );
                actionHistoryVo.setResourceType( TextMinerConstants.COMMON_BLANK );
                actionHistoryVo.setActionType( TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_DELETE );
                actionHistoryVo.setActionMsg( user.getUsername() + " 유사 스크립트 수정 ( 수정 대상 유사 스크립트 : " + autoQaSimScriptVo.getScriptId() + " )" );

                if( request.getRemoteAddr()
                           .equals( "0:0:0:0:0:0:0:1" ) ) {
                    actionHistoryVo.setUserIp( "127.0.0.1" );
                } else {
                    actionHistoryVo.setUserIp( request.getRemoteAddr() );
                }

                actionHistoryService.insertActionHistory( actionHistoryVo );
            }
        } catch( Exception e ) {
            log.error( "tm-user deleteSimScript failed. {}" , e.getMessage() );
        }

        return resultMap;
    }

    /**
     * 유사 스크립트 삭제
     */
    @DeleteMapping( "/deleteQASimScript" )
    public Map<String, Object> deleteQASimScript( @RequestParam int simScriptId
            , HttpServletRequest request
            , @AuthenticationPrincipal( errorOnInvalidType = true ) TmUser user ) {

        Map<String, Object> resultMap = new HashMap<>();

        try {
            AutoQaSimScriptVo autoQaSimScriptVo = new AutoQaSimScriptVo();
            autoQaSimScriptVo.setSimScriptId( simScriptId );
            autoQaSimScriptVo.setModUser( user.getUserName() );
            autoQaSimScriptVo.setRole( user.getAuthorities()
                                           .toString() );

            resultMap = autoQASimService.deleteQASimScript( autoQaSimScriptVo );

            // 삭제 완료 후 이력 저장
            if( resultMap.get( "result" )
                         .toString()
                         .equals( "S" ) ) {
                ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

                actionHistoryVo.setActionUser( user.getUsername() );
                actionHistoryVo.setResourceId( "0" );
                actionHistoryVo.setResourceType( TextMinerConstants.COMMON_BLANK );
                actionHistoryVo.setActionType( TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_DELETE );
                actionHistoryVo.setActionMsg( user.getUsername() + " 유사 스크립트 삭제 ( 삭제 대상 유사 스크립트 : " + simScriptId + " )" );

                if( request.getRemoteAddr()
                           .equals( "0:0:0:0:0:0:0:1" ) ) {
                    actionHistoryVo.setUserIp( "127.0.0.1" );
                } else {
                    actionHistoryVo.setUserIp( request.getRemoteAddr() );
                }

                actionHistoryService.insertActionHistory( actionHistoryVo );
            }
        } catch( Exception e ) {
            log.error( "tm-user deleteSimScript failed. {}" , e.getMessage() );
        }

        return resultMap;
    }

}
