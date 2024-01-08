$(document).ready(function() {
	fnDoScheduleList(1);
	showProjectList(1);
	"select[name='projectId']"
	$(function() {
	  $("input[name='startDt']").daterangepicker({
	    singleDatePicker: true,
	    showDropdowns: true,
	    minYear: 2000,
	    maxYear: 9999,
	    locale: { 
			format: 'YYYY-MM-DD',
			applyLabel: '확인', 
			cancelLabel: "취소", 
			fromLabel: "From", 
			toLabel: "To", 
			customRangeLabel: "Custom", 
			weekLabel: "주", 
			daysOfWeek: [ 
		             "일", 
		             "월", 
		             "화", 
		             "수", 
		             "목", 
		             "금", 
		             "토" 
		       ], 
		    monthNames: [ 
		             "1월", 
		             "2월", 
		             "3월", 
		             "4월", 
		             "5월", 
		             "6월", 
		             "7월", 
		             "8월", 
		             "9월", 
		             "10월", 
		             "11월", 
		             "12월" 
		        ], 
		    firstDay: 1
			}
	  });
	  $("input[name='endDt']").daterangepicker({
	    singleDatePicker: true,
	    showDropdowns: true,
	    minYear: 2000,
	    maxYear: 9999,
	    locale: { 
			format: 'YYYY-MM-DD',
			applyLabel: '확인', 
			cancelLabel: "취소", 
			fromLabel: "From", 
			toLabel: "To", 
			customRangeLabel: "Custom", 
			weekLabel: "주", 
			daysOfWeek: [ 
		             "일", 
		             "월", 
		             "화", 
		             "수", 
		             "목", 
		             "금", 
		             "토" 
		       ], 
		    monthNames: [ 
		             "1월", 
		             "2월", 
		             "3월", 
		             "4월", 
		             "5월", 
		             "6월", 
		             "7월", 
		             "8월", 
		             "9월", 
		             "10월", 
		             "11월", 
		             "12월" 
		        ], 
		    firstDay: 1
			}
	  });
	});
	
});

// 스케줄 목록 가져오기
function fnDoScheduleList() {
	var params = new Object();
		
	$.ajax({
		url: contextPath + "/scheduleRest/getScheduleList",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$("#listArea").html("<div id='LoadingImage' class='loading-LoadingImage'><div style='margin-left:50%'><img th:src='@{/img/loading.gif}'></div></div>");
		},
        success: function(data) {
        	setTimeout(function() {
        		$("#LoadingImage").remove();
        		
        		$("#totalCount").html('전체 스케줄 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#schedule_table_body").html(data.dataTable);						// 검색결과 Data Table(html)
        		
        	}, 'Timeout...!');
		}
	})
}

// 프로젝트 리스트 조회 (팝업 selectBox)
function showProjectList() {
	var params = new Object();
	
	$.ajax({
		url: contextPath + "/scheduleRest/getProjectListForSchedule",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(data) {
        	setTimeout(function() {        		
        		$("#project_id").html(data.dataSelectBox);
        		$("#project_idUP").html(data.dataSelectBox);
        	}, 'Timeout...!');
		}
	})
}

$("select[name='projectId']").change(function () {
	showTaskList($(this));
	}
)

// 서비스 리스트 조회 (팝업 selectBox)
function showTaskList(selectedProject) {
	var params = new Object();
	
	var thisId = selectedProject.attr('id');
	temp_task_id = '';
	
	params.projectId = selectedProject.val();
	
	$.ajax({
		url: contextPath + "/scheduleRest/getTaskListForSchedule",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(data) {
        	setTimeout(function() {        		
				
				if (thisId == 'project_id') {
					$("#task_id").html(data.dataSelectBox)
				} else if (thisId == 'project_idUP') {
					$("#task_idUP").html(data.dataSelectBox);
				}
				
        	}, 'Timeout...!');
		}
	})
}


// 스케줄 정보 채우기
const showUpdatePopup = function () {
    // [1] get schedule_id
    let schedule_id = $('input[type=radio][name=scheduleChoice]:checked').val();
    if (!schedule_id) {
        alert(`스케줄을 먼저 선택하세요`);
        return false;
    }

    $.ajax({
        async: false, //FIXME deprecated synchronous XMLHttpRequest
        type: "GET",
        url: `${contextPath}/scheduleRest/getScheduleDetail/${schedule_id}`,
        success: function (data) {
            let $target = $('#update_pop');

            // [2] info 채우기
            $('#schedule_idUP').val(data.schedule.scheduleId);
            $('#schedule_nameUP').val(data.schedule.scheduleName);
            $('#start_dtUP').val(data.schedule.startDt);
            $('#end_dtUP').val(data.schedule.endDt);
            $target.find('input:radio[name=scheduleTypeUPR][value="'+data.schedule.scheduleType+'"]').prop('checked', true);
            $('#schedule_typeUP').val(data.schedule.scheduleType);
            $('#start_timeUP').val(data.schedule.startTime);
            $('#end_timeUp').val(data.schedule.endTime);
            $('#execute_intervalUp').val(data.schedule.executeInterval);
            $target.find('input:radio[name=useYnUPR][value="'+data.schedule.useYn+'"]').prop('checked', true);
            $('#use_ynUP').val(data.schedule.useYn);
            
            var typeValue = data.schedule.scheduleType;
	        if (typeValue == 'deploy') {
				$('#deploy_onlyUP').css('display', 'block');
				$('#batch_onlyUP').css('display', 'none');
			} else if (typeValue == 'batch') {
				$('#batch_onlyUP').css('display', 'block');
				$('#deploy_onlyUP').css('display', 'none');
			}
			
            //projectId
            //programId
            //taskId

            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        error: function (data) {
            console.error("Failed to get Schedule");
            console.debug(data);
            alert("스케줄 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

// 이벤트리스너 등록 : 생성, 수정, 삭제, 업로드 버튼

// 팝업창 띄우기
const showPopup = function (target) {
    let $target;
    switch (target) {
        case 'create':
            $target = $('#create_pop');
            $target.find('input').val('');
            
            $target.find('input[type=text]:eq(0)').focus();
            $('#schedule_deploy').attr('value','deploy');
            $('#schedule_batch').attr('value','batch');
            $('#use_y').attr('value','Y');
            $('#use_n').attr('value','N');
            $('#start_time').val('0');
            $('#end_time').val('1');
            $('#execute_interval').val('5');
            
            $target.show();
            break;
        case 'update':
			$('#schedule_deployUP').attr('value','deploy');
            $('#schedule_batchUP').attr('value','batch');
            $('#use_yUP').attr('value','Y');
            $('#use_nUP').attr('value','N');
            showUpdatePopup();
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
};

// 팝업창 숨기기
const hidePopup = function (target) {
    let $target;
    switch (target) {
        case 'create':
            $target = $('#create_pop');
            break;
        case 'update':
            $target = $('#update_pop');
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
    $target.find('input,select').val('');
    $target.hide();
};

// 선택된 스케줄 삭제
const deleteSchedule = function () {
    const scheduleSelected = $("input[type=radio][name=scheduleChoice]:checked");
    const scheduleId = scheduleSelected.val();
    const scheduleName = scheduleSelected.data("name");
    if (!scheduleId) {
        alert(`스케줄을 먼저 선택하세요`);
        return false;
    }

    if (!confirm(`선택한 스케줄 '${scheduleName}' 을 삭제합니다. 삭제된 스케줄은 복구 불가능 합니다.`)) {
        return false;
    }

    $.ajax({
        type: "POST",
        url: `${contextPath}/scheduleRest/deleteSchedule`,
        data:{scheduleId : scheduleId},
        beforeSend: function () {
            console.log(`Schedule to delete: ${scheduleId}`);
        },
        success: function (response) {
            // schedule 삭제 성공
            alert(response.resultMsg);
            
            if (response.result == 'S'){
	            fnDoScheduleList();
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("스케줄 삭제 권한이 없습니다.");
                    alert("스케줄 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`스케줄 삭제 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Schedule");
                    alert("스케줄 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
};

// 스케줄 유형 변경시 양식 변경, 값 초기화 (등록)
$("input[name='scheduleTypeR']:radio").change(function () {
        var typeValue = this.value;
        if (typeValue == 'deploy') {
			$('#deploy_only').css('display', 'block');
			$('#project_id').attr('required', 'required');
			$('#task_id').attr('required', 'required');
			
			$('#batch_only').css('display', 'none');
			$('#program_id').val('');
			$('#program_id').removeAttr('required');
			
		} else if (typeValue == 'batch') {
			$('#batch_only').css('display', 'block');
			$('#program_id').attr('required', 'required');
			
			$('#deploy_only').css('display', 'none');
			$('#project_id').val('');
			$('#project_id').removeAttr('required');
			$('#task_id').val('');
			$('#task_id').removeAttr('required');
		}
});

// 스케줄 유형 변경시 양식 변경, 값 초기화 (수정)
let temp_project_id;
let temp_task_id;
let temp_program_id;
$("input[name='scheduleTypeUPR']:radio").change(function () {
        var typeValue = this.value;
        if (typeValue == 'deploy') {
			$('#deploy_onlyUP').css('display', 'block');
			$('#project_idUP').attr('required', 'required');
			$('#task_idUP').attr('required', 'required');
			$('#project_idUP').val(temp_project_id);
			$('#task_idUP').val(temp_task_id);
			
			
			$('#batch_onlyUP').css('display', 'none');
			temp_program_id = $('#program_idUP').val();
			$('#program_idUP').val('');
			$('#program_idUP').removeAttr('required');
		} else if (typeValue == 'batch') {
			$('#batch_onlyUP').css('display', 'block');
			$('#program_idUP').attr('required', 'required');
			$('#program_idUP').val(temp_program_id);
			
			$('#deploy_onlyUP').css('display', 'none');
			temp_project_id = $('#project_idUP').val();
			temp_task_id = $('#task_idUP').val();
			$('#project_idUP').val('');
			$('#task_idUP').val('');
			$('#project_idUP').removeAttr('required');
			$('#task_idUP').removeAttr('required');
		}
});

// 종료 일자 미정 체크박스
let temp_endDt;
let temp_endDtUP;
const changeEndDt = function() {
	if ($(no_end_dt).is(":checked")) {
		temp_endDt = $('#end_dt').val();
		$('#end_dt').val('9999-12-31');
	} else if (!$(no_end_dt).is(":checked")){
		$('#end_dt').val(temp_endDt);
		temp_endDt = '';
	}
	if ($(no_end_dtUP).is(":checked")) {
		temp_endDtUP = $('#end_dtUP').val();
		$('#end_dtUP').val('9999-12-31');
	} else if (!$(no_end_dtUP).is(":checked")){
		$('#end_dtUP').val(temp_endDtUP);
		temp_endDtUP = '';
	}
}

// 스케줄 추가
const createSchedule = function () {
    let $form_schedule = $('#create_pop form');
    
  	if (!checkTimeValue($('#start_time').val(),$('#end_time').val()))
    	return false;
    if (!checkDateValue($('#start_dt').val(),$('#end_dt').val()))
    	return false;

	$('#schedule_type').val($('input[name="scheduleTypeR"]:checked').val());
	$('#use_yn').val($('input[name="useYnR"]:checked').val());

    $.ajax({
        type: "POST",
        url: `${contextPath}/scheduleRest/insertSchedule`,
        data: $form_schedule.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Schedule to create: ${$form_schedule.serialize()}`);
            document.querySelector("#create_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // schedule 생성 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            $('#create_pop').hide();
	            fnDoScheduleList();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("스케줄 추가 권한이 없습니다.");
                    alert("스케줄 추가 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`스케줄 추가 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to create Schedule");
                    alert("스케줄 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#create_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};

// 스케줄 수정
const updateSchedule = function () {
    let schedule_id = $('input[type=radio][name=scheduleChoice]:checked').val();
    if (!schedule_id) {
        alert(`스케줄을 먼저 선택하세요`);
        return false;
    }
    
    let $form_schedule = $('#update_pop form');

  	if (!checkTimeValue($('#start_timeUP').val(),$('#end_timeUP').val()))
    	return false;
    if (!checkDateValue($('#start_dtUP').val(),$('#end_dtUP').val()))
    	return false;

    $.ajax({
        type: "POST",
        url: `${contextPath}/scheduleRest/updateSchedule`,
        data: $form_schedule.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Schedule to update: ${$form_schedule.serialize()}`);
            document.querySelector("#update_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // schedule 수정 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            $('#update_pop').hide();
	            fnDoScheduleList();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("스케줄 수정 권한이 없습니다.");
                    alert("스케줄 수정 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`스케줄 수정 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Schedule");
                    alert("스케줄 수정 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#update_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};

/**
 * 프로젝트, 태스크 상태 업데이트
 */
const refreshTaskStatus = function getProjectTaskStatusFromTMServerAndRefreshTable() {

    $.ajax({
        type: "GET",
        url: `${contextPath}/api/project/status`,
        /**
         *
         * @param {Object} allStatus
         * @param {String} allStatus.code
         * @param {String} allStatus.message
         * @param {String} allStatus.status
         * @param {Object} allStatus.data
         * @param {Object} allStatus.data[projectId]
         * @param {Object} allStatus.data[projectId][taskId]
         * @param {Number} allStatus.data[projectId][taskId].project_id
         * @param {Number} allStatus.data[projectId][taskId].task_id
         * @param {String} allStatus.data[projectId][taskId].status
         * @param {String} allStatus.data[projectId][taskId].owner
         * @param {Number} allStatus.data[projectId][taskId].pid
         * @param {Number} allStatus.data[projectId][taskId].memory
         * @param {String} allStatus.data[projectId][taskId].start_time
         * @param {String} allStatus.data[projectId][taskId].end_time
         * @param {Number} allStatus.data[projectId][taskId].progress
         * @param {Number} allStatus.data[projectId][taskId].chunk_index
         */
        success: function (allStatus, textStatus, request) {
            for (let projectId in allStatus) {
                //console.table(allStatus[projectId]);

                const projectRow = document.querySelector(`tr[class^=project-${projectId}]`);
                if (!projectRow || !allStatus[projectId] || Object.keys(allStatus[projectId]).length === 0) {
                    continue;
                }

                // tasks
                for (let taskId in allStatus[projectId]) {
                    const taskStatus = allStatus[projectId][taskId];

                    const taskRow = document.querySelector(`.project-${projectId}-${taskId}`);

                    if (!taskRow) {
                        console.error(`Failed to find project-${projectId}-${taskId}`);
                        continue;
                    }

                    taskRow.querySelector("td.task_progress").innerText = `${localizeTaskStatus(taskStatus.status)}(${(taskStatus.progress * 100).toFixed(2)}%)`;
                    taskRow.querySelector("td.task_progress").title = `${taskStatus.status}`;
                    taskRow.querySelector("td.date").innerText = `${simpleTime(taskStatus.running_time)}\n${simpleTime(taskStatus.start_time)} ~ ${simpleTime(taskStatus.end_time)}`;
                    taskRow.querySelector("td.memory").innerText = get_file_size(taskStatus.memory);
                }
            }
            console.debug("api 결과 확인: ", allStatus);
        },
        error: function (data) {
            console.error("Failed to get Project");
            console.debug(data);
        }
    });
};

const localizeTaskStatus = function localizeMessageOfTaskStatus(status) {
    let statusStr;
    if (status === "finished")
        statusStr = "완료";
    else if (status === "initial")
        statusStr = "준비";
    else if (status === "paused")
        statusStr = "일시정지";
    else if (status === "stopped")
        statusStr = "중지";
    else if (status === "training" || status === "analyzing")
        statusStr = "실행중";
    else if (status === "failed")
        statusStr = "오류";
    else if (status === "syserror")
        statusStr = "시스템 오류";
    else
        statusStr = status;
    return statusStr;
};

const simpleTime = function simplifyTimeStampHourMinSec(time) {
    let convert_time;
    try {
        if (time && time.length > 7)
            convert_time = time.substring(0, time.length - 7);  // remove microseconds
        else
            convert_time = time;
    } catch (e) {
        convert_time = "N/A";
        console.error("convert time error. {}", time, e);
    }
    return convert_time;
};


/**
 * @param {Array} serializeArray
 * @param {Object} serializeArray[]
 * @param {String} serializeArray[].name
 * @param {String} serializeArray[].value
 *
 * @private
 */
const isFormValid = function (serializeArray) {
    const regexForbiddenChars = /^[^\\/:\*\?"<>\|]+$/; // forbidden characters \ / : * ? " < > |
    const regexStartingDots = /^\./; // cannot start with dot (.)
    const regexForbiddenFileNames = /^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i; // forbidden file names

    for (let serialize of serializeArray) {
        const fieldName = serialize.name.toString();
        const fieldValue = serialize.value.toString();

        if (fieldName == "projectName") {
            if (fieldValue.startsWith(".") || fieldValue.startsWith(" ")) {
                alert("점('.')이나 공백(' ')으로 프로젝트명을 시작할 수 없습니다.");
                return false;
            }
            // 윈도우 파일명 금지 문자 체크
            if (!regexForbiddenChars.test(fieldValue)) {
                alert("\\ / : * ? \" < > | 특수기호는 프로젝트명으로 사용할 수 없습니다.");
                return false;
            }
            if (regexForbiddenFileNames.test(fieldValue)) {
                alert("사용할 수 없는 프로젝트명입니다.");
                return false;
            }
            if (fieldValue.replace(/\./g, "").trim().length === 0) {  // 점(.)과 공백 제거
                alert("프로젝트명은 공백과 점(.)만으로는 설정할 수 없습니다.");
                return false;
            }
        }
    }
    return true;
};


const checkTimeValue = function (start_time, end_time) {
	const startTime = parseInt(start_time, 10);
	const endTime = parseInt(end_time, 10);
	
	if (startTime == endTime) {
		alert("시작 시간과 종료 시간은 동일할 수 없습니다.");
		return false;
	} else if (startTime > endTime) {
		alert("종료 시간이 시작 시간보다 작을 수 없습니다.");
		return false;
	}
	return true;
}

const checkDateValue = function (start_dt, end_dt) {
	const startDt = parseInt(start_dt.replaceAll('-',''), 10);
	const endDt = parseInt(end_dt.replaceAll('-',''), 10);
	
	if (startDt > endDt) {
		console.log(startDt+' :: '+endDt);
		alert("종료 날짜가 시작 날짜보다 작을 수 없습니다.");
		return false;
	}
	return true;
}

