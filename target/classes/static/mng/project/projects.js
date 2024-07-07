$(document).ready(function() {
	statusTestModule();
	fnDoList(1)
	
	// 15초 마다 상태 조회
	setInterval(statusTestModule, 15000);
});

// 프로젝트 목록 가져오기
function fnDoList() {
	var params = new Object();
	
	params.projectId = $('#divide').val();
	
	$.ajax({
		url: contextPath + "/projectRest/getProjectList",
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
        		
        		$("#totalCount").html('전체 프로젝트 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#project_table_body").html(data.dataTable);						// 검색결과 Data Table(html)
        		
        	}, 'Timeout...!');
		}
	})
}

// 모델 사용/미사용 처리
function chgEnabled(projectId, taskId, taskType){
	
	var params = new Object();
	
	params.projectId = projectId;
	params.taskId = taskId;
	params.taskType = taskType;
	params.enabled = $("input:checkbox[id='enabled_" + projectId + taskId + "']").is(":checked")?'Y':'N';
	
	$.ajax({
		url: contextPath + "/taskRest/updateEnabled",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(data) {
			if (data.result != "S") {
				
				if (data.result == "R" && params.enabled == "Y") {
					alert(data.resultMsg);
					
					if($("input:checkbox[id='enabled_" + projectId + taskId + "']").is(":checked")) {
						$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", false);
					} else {
						$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", true);
					}
				} else {
					// 이전 상태로 복구
					setTimeout(function() {
						if($("input:checkbox[id='enabled_" + projectId + taskId + "']").is(":checked")) {
							$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", false);
						} else {
							$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", true);
						}
					}, 500);
				}
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
			
			// 이전 상태로 복구
			setTimeout(function() {
				if($("input:checkbox[id='enabled_" + projectId + taskId + "']").is(":checked")) {
					$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", false);
				} else {
					$("input:checkbox[id='enabled_" + projectId + taskId + "']").prop("checked", true);
				}
			}, 500);
		}
	})
}

// 프로젝트 정보 채우기
const showUpdatePopup = function (id) {
    // [1] get Project_id
    let project_id = id;
//    let project_id = $('input[type=radio][name=project_choice]:checked').val();
//    if (!project_id) {
//        alert(`프로젝트를 먼저 선택하세요`);
//        return false;
//    }

    $.ajax({
        async: false, //FIXME deprecated synchronous XMLHttpRequest
        type: "GET",
        url: `${contextPath}/projectRest/getProjectDetail/${project_id}`,
        success: function (data) {
            let $target = $('#update_pop');

            // [2] project info 채우기
            $target.find('input[name=projectId]').val(data.project.projectId);
            $target.find('input[name=projectName]').val(data.project.projectName);
            $target.find('textarea[name=projectDesc]').val(data.project.projectDesc);

            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        error: function (data) {
            console.error("Failed to get Project");
            console.debug(data);
            alert("프로젝트 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

// 이벤트리스너 등록 : 생성, 수정, 삭제, 업로드 버튼

// 팝업창 띄우기
const showPopup = function (target, id) {
    let $target;
    switch (target) {
        case 'create':
            $target = $('#create_pop');
            $target.find('input,select,textarea').val('');
            $target.show();
            $target.find('input[type=text]:eq(0)').focus();
            break;
        case 'update':
            showUpdatePopup(id);
            break;
        // case 'log':
        //     let project_id = $('input[type=radio][name=project_choice]:checked').val();
        //     if (!project_id) {
        //         alert(`프로젝트를 먼저 선택하세요`);
        //         return false;
        //     }
        //     //TODO(wisnt65) get_project_log
        //     $target = $('#log_pop');
        //     $target.show();
        //     $target.find('input[type=text]:eq(0)').focus();
        //     break;
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
        case 'analyze':
            $target = $('#analyze_pop');
            break;
        case 'log':
            $target = $('#log_pop');
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
    $target.find('input,select').val('');
    $target.hide();
};

// 선택된 프로젝트 삭제
const deleteProject = function (id) {
    const projectSelected = $("input[type=radio][name=project_choice]:checked");
    const projectId = id;
    if (!projectId) {
        alert(`프로젝트를 먼저 선택하세요`);
        return false;
    }

    if (!confirm(`해당 프로젝트를 삭제합니다. 삭제된 프로젝트는 복구 불가능 합니다.`)) {
        return false;
    }

    $.ajax({
        type: "POST",
        url: `${contextPath}/projectRest/deleteProject`,
        data:{projectId : projectId},
        beforeSend: function () {
            console.log(`Project to delete: ${projectId}`);
        },
        success: function (response) {
            // project 삭제 성공
            alert(response.resultMsg);
            
            if (response.result == 'S'){
	            fnDoList();
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("프로젝트 삭제 권한이 없습니다.");
                    alert("프로젝트 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`프로젝트 삭제 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Project");
                    alert("프로젝트 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
};

// 프로젝트 추가
const createProject = function () {
    let $form_project = $('#create_pop form');

    if (!isFormValid($form_project.serializeArray()))
        return false;

    $.ajax({
        type: "POST",
        url: `${contextPath}/projectRest/insertProject`,
        data: $form_project.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Project to create: ${$form_project.serialize()}`);
            document.querySelector("#create_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // project 생성 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            $('#create_pop').hide();
	            fnDoList();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("프로젝트 추가 권한이 없습니다.");
                    alert("프로젝트 추가 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`프로젝트 추가 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to create Project");
                    alert("프로젝트 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });

    document.querySelector("#create_pop button[type=submit]").disabled = false;

    return false;
};

// 프로젝트 수정
const updateProject = function () {
	let project_id = $('input[name="projectId"]').val()

    let $form_project = $('#update_pop form');

    if (!isFormValid($form_project.serializeArray()))
        return false;

    $.ajax({
        type: "POST",
        url: `${contextPath}/projectRest/updateProject`,
        data: $form_project.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Project to update: ${$form_project.serialize()}`);
            document.querySelector("#update_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // project 수정 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            $('#update_pop').hide();
	            fnDoList();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("프로젝트 수정 권한이 없습니다.");
                    alert("프로젝트 수정 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`프로젝트 수정 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Project");
                    alert("프로젝트 수정 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });

    document.querySelector("#update_pop button[type=submit]").disabled = false;

    return false;
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

// 모델 생성
function createModel(projectId, taskId, taskType) {
	
	var params = new Object();

	params.projectId = projectId;
	params.taskId = taskId;
	params.taskType = taskType;
	params.jobDiv = 'TRAINING';
	
	// 학습 대기중인지 확인
	$.ajax({
		url: contextPath + "/modelRest/chkStatus",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		success: function(data) {
			if (data.result == 'Y') {
				
				if (!confirm("모델을 생성하시겠습니까?")) {
					return false;
				}
				
				// 모델 생성요청
				$.ajax({
					url: contextPath + "/modelRest/createModel",
					type:'POST',
					cache: false,
					contentType : "application/json",
					dataType: "json",
					data: JSON.stringify(params),
					beforeSend: function () {
						alert("모델 생성을 요청합니다.");
					},
			        success: function(data) {
						console.log(data.resultMsg);
					}, 
					error: function(request,status,error) {
						console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
					}
				});
			} else {
				alert(data.resultMsg);
				return false;
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}

// 사전 설정
function replaceDictionary(projectId, taskId, taskType) {
	
	var params = new Object();

	params.projectId = projectId;
	params.taskId = taskId;
	params.taskType = taskType;
	
	// 상태 확인
	$.ajax({
		url: contextPath + "/modelRest/chkStatus",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		success: function(data) {
			if (data.result == 'Y') {
				
				if (!confirm("사전(패턴)을 설정 하시겠습니까?")) {
					return false;
				}
				
				// 사전 설정 요청
				$.ajax({
					url: contextPath + "/modelRest/replaceDictionary",
					type:'POST',
					cache: false,
					contentType : "application/json",
					dataType: "json",
					data: JSON.stringify(params),
			        success: function(response) {
						alert(response.resultMsg);
					}, 
					error: function(request,status,error) {
						alert.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
					}
				});
			} else {
				alert(data.resultMsg);
				return false;
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}

// 분석 Modal 띄우기
const showAnalyzePopup = function (projectId, taskId, taskType) {

	var params = new Object();
	
	params.projectId = projectId;
	params.taskId = taskId;

    $.ajax({
        async: false, //FIXME deprecated synchronous XMLHttpRequest
        type: "POST",
        url: `${contextPath}/collectionRest/getAnalyzeCollections`,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
        success: function (data) {
            let $target = $('#analyze_pop');

            // [2] project info 채우기
            $target.find('input[name=projectId]').val(projectId);
            $target.find('input[name=taskId]').val(taskId);
            $target.find('input[name=taskType]').val(taskType);
            
			$target.find('select[name=analyzeCollection]').empty();
			$target.find('select[name=analyzeCollection]').append('<option selected disabled value=" ">분석할 컬렉션을 선택하세요</option>');
            
			for (var i = 0; i < data.analyzeCollection.length; i++){
				$target.find('select[name=analyzeCollection]').append('<option value="' + data.analyzeCollection[i].collectionId + '">' + data.analyzeCollection[i].collectionName +  '</option>');
			}

            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        error: function (data) {
            console.error("Failed to get Project");
            console.debug(data);
            alert("프로젝트 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

// 분석요청
function analyzeModel() {

	var params = new Object();

	params.projectId = $('#analyze_pop').find('input[name=projectId]').val();
	params.taskId = $('#analyze_pop').find('input[name=taskId]').val();
	params.taskType = $('#analyze_pop').find('input[name=taskType]').val();
	params.collectionId = $('#analyze_pop').find('select[name=analyzeCollection]').val();
	params.jobDiv = 'ANALYSIS';
	
	// 학습 대기중인지 확인
	$.ajax({
		url: contextPath + "/modelRest/chkStatus",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		success: function(data) {
			if (data.result == 'Y') {
				
				if (!confirm("모델을 분석하시겠습니까?")) {
					return false;
				}
				
				// 분석요청
				$.ajax({
					url: contextPath + "/modelRest/analyzeModel",
					type:'POST',
					cache: false,
					contentType : "application/json",
					dataType: "json",
					data: JSON.stringify(params),
					beforeSend: function () {
						alert("모델 분석을 요청합니다.");
						$('#analyze_pop').hide();
					},
			        success: function(data) {
						console.log(data.resultMsg);
					}, 
					error: function(request,status,error) {
						console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
					}
				});
			} else {
				alert(data.resultMsg);
				return false;
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}

// 결과보기
function resultModel(projectId, taskId, excelFileName, taskType) {

var params = new Object();

	params.projectId = projectId;
	params.taskId = taskId;
	params.taskType = taskType;
	
	// 생성된 결과가 있는지 확인
	$.ajax({
		url: contextPath + "/resultRest/checkAnalyzeResult",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		success: function(response) {
			alert(response.resultMsg);
			
			if (response.result == 'Y') {
				var form = document.createElement("form");
				form.setAttribute("charset", "UTF-8");
				form.setAttribute("method", "POST");
				form.setAttribute("action", contextPath + "/resultRest/excelDownload");
				
				var hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", "projectId");
				hiddenField.setAttribute("value", projectId);
				form.appendChild(hiddenField);
				
				hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", "taskId");
				hiddenField.setAttribute("value", taskId);
				form.appendChild(hiddenField);
				
				hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", "excelFileName");
				hiddenField.setAttribute("value", excelFileName);
				form.appendChild(hiddenField);
				
				hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", "taskType");
				hiddenField.setAttribute("value", taskType);
				form.appendChild(hiddenField);
			
				document.body.appendChild(form);
				form.submit();
			} else {
				return false;
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}

// 모델삭제
function deleteModel(projectId, taskId){
	
	var enabled = $("input:checkbox[id='enabled_" + projectId + taskId + "']").is(":checked")?'Y':'N';

	if (enabled == 'Y'){
		alert('현재 사용 중인 모델은 삭제할 수 없습니다.');
		return false;
	}
	
    if (!confirm(`해당 모델을 삭제합니다. 삭제된 모델은 복구 불가능 합니다.`)) {
        return false;
    }
    
	var params = new Object();

	params.projectId = projectId;
	params.taskId = taskId;
	
	$.ajax({
		url: contextPath + "/taskRest/deleteTask",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(data) {
			alert(data.resultMsg);
			if (data.result == "S") {
				fnDoList();
			}
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}

function statusTestModule(){
	console.log("statusTestModule call...");
	
	$.ajax({
		url: contextPath + "/projectRest/statusTestModule",
		type:'GET',
		cache: false,
		contentType : "application/json",
		async: false,
        success: function(response) {
			$('#trafficContent').html(response.trafficContent);
		}, 
		error: function(request,status,error) {
			console.log("code:" + request.status + "\n" + "message:"+request.responseText + "\n" + "error:" + error);
		}
	});
}
	
//	/**
//	 * 프로젝트, 태스크 상태 업데이트
//	 */
//	const refreshTaskStatus = function getProjectTaskStatusFromTMServerAndRefreshTable() {
//	
//	    $.ajax({
//	        type: "GET",
//	        url: `${contextPath}/api/project/status`,
//	        /**
//	         *
//	         * @param {Object} allStatus
//	         * @param {String} allStatus.code
//	         * @param {String} allStatus.message
//	         * @param {String} allStatus.status
//	         * @param {Object} allStatus.data
//	         * @param {Object} allStatus.data[projectId]
//	         * @param {Object} allStatus.data[projectId][taskId]
//	         * @param {Number} allStatus.data[projectId][taskId].project_id
//	         * @param {Number} allStatus.data[projectId][taskId].task_id
//	         * @param {String} allStatus.data[projectId][taskId].status
//	         * @param {String} allStatus.data[projectId][taskId].owner
//	         * @param {Number} allStatus.data[projectId][taskId].pid
//	         * @param {Number} allStatus.data[projectId][taskId].memory
//	         * @param {String} allStatus.data[projectId][taskId].start_time
//	         * @param {String} allStatus.data[projectId][taskId].end_time
//	         * @param {Number} allStatus.data[projectId][taskId].progress
//	         * @param {Number} allStatus.data[projectId][taskId].chunk_index
//	         */
//	        success: function (allStatus, textStatus, request) {
//	            for (let projectId in allStatus) {
//	                //console.table(allStatus[projectId]);
//	
//	                const projectRow = document.querySelector(`tr[class^=project-${projectId}]`);
//	                if (!projectRow || !allStatus[projectId] || Object.keys(allStatus[projectId]).length === 0) {
//	                    continue;
//	                }
//	
//	                // tasks
//	                for (let taskId in allStatus[projectId]) {
//	                    const taskStatus = allStatus[projectId][taskId];
//	
//	                    const taskRow = document.querySelector(`.project-${projectId}-${taskId}`);
//	
//	                    if (!taskRow) {
//	                        console.error(`Failed to find project-${projectId}-${taskId}`);
//	                        continue;
//	                    }
//	
//	                    taskRow.querySelector("td.task_progress").innerText = `${localizeTaskStatus(taskStatus.status)}(${(taskStatus.progress * 100).toFixed(2)}%)`;
//	                    taskRow.querySelector("td.task_progress").title = `${taskStatus.status}`;
//	                    taskRow.querySelector("td.date").innerText = `${simpleTime(taskStatus.running_time)}\n${simpleTime(taskStatus.start_time)} ~ ${simpleTime(taskStatus.end_time)}`;
//	                    taskRow.querySelector("td.memory").innerText = get_file_size(taskStatus.memory);
//	                }
//	            }
//	            console.debug("api 결과 확인: ", allStatus);
//	        },
//	        error: function (data) {
//	            console.error("Failed to get Project");
//	            console.debug(data);
//	        }
//	    });
//	};
//	
//	const localizeTaskStatus = function localizeMessageOfTaskStatus(status) {
//	    let statusStr;
//	    if (status === "finished")
//	        statusStr = "완료";
//	    else if (status === "initial")
//	        statusStr = "준비";
//	    else if (status === "paused")
//	        statusStr = "일시정지";
//	    else if (status === "stopped")
//	        statusStr = "중지";
//	    else if (status === "training" || status === "analyzing")
//	        statusStr = "실행중";
//	    else if (status === "failed")
//	        statusStr = "오류";
//	    else if (status === "syserror")
//	        statusStr = "시스템 오류";
//	    else
//	        statusStr = status;
//	    return statusStr;
//	};
//	
//	const simpleTime = function simplifyTimeStampHourMinSec(time) {
//	    let convert_time;
//	    try {
//	        if (time && time.length > 7)
//	            convert_time = time.substring(0, time.length - 7);  // remove microseconds
//	        else
//	            convert_time = time;
//	    } catch (e) {
//	        convert_time = "N/A";
//	        console.error("convert time error. {}", time, e);
//	    }
//	    return convert_time;
//	};