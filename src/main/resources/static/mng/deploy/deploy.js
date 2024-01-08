$(document).ready(function() {
	fnDoDeployList();
	
	// 30초마다 List refresh
	setInterval(fnDoDeployList, 30000);
});

// 배포 서버 정보 목록 가져오기
function fnDoDeployList() {
	var params = new Object();
		
	$.ajax({
		url: `${contextPath}/deployRest/getDeployList`,
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
        		
        		$("#deploy_table_body").html(data.dataTable);				// 검색결과 Data Table(html)
        		
        	}, 'Timeout...!');
		}
	})
}

// 배포 서버 정보 채우기 (수정)
const showUpdatePopup = function (serverId) {
    // [1] get server_id
    let server_id = serverId;
    
    $.ajax({
        async: false, //FIXME deprecated synchronous XMLHttpRequest
        type: "GET",
        url: `${contextPath}/deployRest/getDeployDetail/${server_id}`,
        success: function (data) {
            let $target = $('#update_pop');

            // [2] info 채우기
            $('#server_id').val(data.deploy.serverId);
            $('#server_nameUP').val(data.deploy.serverName);
            $('#server_ipUP').val(data.deploy.serverIp);
            $('#server_portUP').val(data.deploy.serverPort);
            $('#server_typeUP').val(data.deploy.serverType);
            $('#server_taskUP').val(data.deploy.serverTask);
            $('#server_descUP').val(data.deploy.serverDesc);
            
            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        error: function (data) {
            console.error("Failed to get Deploy Info");
            console.debug(data);
            alert("배포 서버 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
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
            
            $target.show();
            break;
        case 'update':
            showUpdatePopup();
            break;
        case 'deploy':
			showDeployPopup();
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
        case 'deploy' :
			$target = $('#deploy_pop');
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
    $target.find('input,select').val('');
    $target.hide();
};

// 선택된 배포 서버 삭제
const deleteDeploy = function (selectedId) {
    const serverId = selectedId;

    if (!confirm(`선택한 배포 서버를 삭제합니다. 삭제된 배포 서버 정보는 복구 불가능 합니다.`)) {
        return false;
    }

    $.ajax({
        type: "POST",
        url: `${contextPath}/deployRest/deleteDeploy`,
        data:{serverId : serverId},
        beforeSend: function () {
            console.log(`Deploy Info to delete: ${serverId}`);
        },
        success: function (response) {
            // Deploy Info 삭제 성공
            alert(response.resultMsg);
            
            if (response.result == 'S'){
	            fnDoDeployList();
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("배포 서버 정보 삭제 권한이 없습니다.");
                    alert("배포 서버 정보 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`배포 서버 정보 삭제 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("배포 서버 정보 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
};

// 배포 서버 정보 추가
const createDeploy = function () {
    let $form_deploy = $('#create_pop form');
    
     if (!isFormValid($form_deploy.serializeArray()))
        return false;

    $.ajax({
        type: "POST",
        url: `${contextPath}/deployRest/insertDeploy`,
        data: $form_deploy.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Deploy Info to create: ${$form_deploy.serialize()}`);
            document.querySelector("#create_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // Deploy Info 생성 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            $('#create_pop').hide();
	            fnDoDeployList();
            }
            document.querySelector("#create_pop button[type=submit]").disabled = false;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("배포 서버 정보 추가 권한이 없습니다.");
                    alert("배포 서버 정보 추가 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`배포 서버 정보 추가 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to create Deploy Info");
                    alert("배포 서버 정보 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#create_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};

// 배포 서버 정보 수정
const updateDeploy = function () {
    let $form_deploy = $('#update_pop form');
    
    if (!isFormValid($form_deploy.serializeArray()))
        return false;

    $.ajax({
        type: "POST",
        url: `${contextPath}/deployRest/updateDeploy`,
        data: $form_deploy.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Deploy Info to update: ${$form_deploy.serialize()}`);
            document.querySelector("#update_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            // Deploy Info 수정 성공
            alert(response.resultMsg);

            if (response.result == "S"){
	            location.reload();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("배포 서버 정보 수정 권한이 없습니다.");
                    alert("배포 서버 정보 수정 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`배포 서버 정보 수정 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("배포 서버 정보 수정 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#update_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};

// 연결 테스트
const healthCheck = function (serverId) {
	$.ajax({
        type: "GET",
        url: `${contextPath}/deployRest/healthChk/${serverId}`,
        success: function (response) {
			console.log(response);
			
            if (response.result == 'S'){
	            $('#statusBtn_'+serverId).html('<div></div>' + response.resultTxt);
	            $('#statusBtn_'+serverId).children('div').addClass(response.resultCss);
	            $('#statusBtn_'+serverId).children('div').addClass('mr5');
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`배포 서버 상태조회 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("배포 서버 상태조회 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
}

// 모듈 재기동
const rebootServer = function (serverId, serverName) {
	
	$.ajax({
		type: "GET",
		url: `${contextPath}/deployRest/rebootStatusCheck/${serverId}`,
		success: function(response) {
			
			console.log(response);
			
			if (response.result == "200"){
				
				if(!confirm(serverName + " 모듈을 재시작하시겠습니까?\n현재 진행중인 학습 및 분석이 중단됩니다.")) {
					return false;
				}
				
				$.ajax({
			        type: "GET",
			        url: `${contextPath}/deployRest/rebootModule/${serverId}`,
			        beforeSend: function(){
						alert(serverName + " 모듈을 재시작합니다.");
						fnDoDeployList();
					},
			        error: function (jqXHR, textStatus, errorThrown) {
			            console.debug(jqXHR);
			            console.debug(textStatus);
			            console.debug(errorThrown);
			            switch (jqXHR.status) {
			                case 409:
			                    console.error(jqXHR.responseText);
			                    alert(`모듈 재기동 실패. [${jqXHR.responseText}]`);
			                    break;
			                default:
			                    console.error("Failed to update Deploy Info");
			                    alert("모듈 재기동 실패. 서버 연결 상태 및 로그를 확인하세요.");
			                    break;
			            }
			        }
			    });
			} else {
				alert(response.message);
			}
		},
		error: function () {
			console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`모듈 재기동 진행상태조회 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("모듈 재기동 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
			}
		}
	});
}

// 모든 모듈 재기동
const rebootAllServer = function () {
	
	$.ajax({
		type: "GET",
		url: `${contextPath}/deployRest/rebootStatusCheck/0`,
		success: function(response) {
			
			console.log(response);
			
			if (response.result == "200"){
				
				if(!confirm("등록된 모든 모듈을 재시작하시겠습니까?\n현재 진행중인 학습 및 분석이 중단됩니다.")) {
					return false;
				}
				
				$.ajax({
			        type: "GET",
			        url: `${contextPath}/deployRest/rebootModule/0`,
			        beforeSend: function(){
						alert("등록된 모든 모듈을 재시작합니다.\n완료되기까지 약간의 시간이 소요됩니다.");
					},
			        error: function (jqXHR, textStatus, errorThrown) {
			            console.debug(jqXHR);
			            console.debug(textStatus);
			            console.debug(errorThrown);
			            switch (jqXHR.status) {
			                case 409:
			                    console.error(jqXHR.responseText);
			                    alert(`모듈 재기동 실패. [${jqXHR.responseText}]`);
			                    break;
			                default:
			                    console.error("Failed to update Deploy Info");
			                    alert("모듈 재기동 실패. 서버 연결 상태 및 로그를 확인하세요.");
			                    break;
			            }
						document.querySelector("#rebootAllBtn").disabled = false;
			        }
			    });
		    } else {
				alert(response.message);
			}
		},
		error: function () {
			console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`모듈 재기동 진행상태조회 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("모듈 재기동 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
			}
		}
	});
}

// 배포 
const deployModel = function(serverId) {
	
	$.ajax({
		type: "GET",
		url: `${contextPath}/deployRest/rebootStatusCheck/${serverId}`,
		success: function(response) {
			console.log(response);
			
			if (response.result == "200"){
				
				if (!confirm("배포를 진행하시겠습니까?")) {
					return false;
				}
				
				$.ajax({
			        type: "GET",
			        url: `${contextPath}/deployRest/deployModel/${serverId}`,
			        success: function (response) {
						// console.log(response.result);
						// console.log(response.resultMsg);
						alert(response.resultMsg);
						location.reload();
			        },
			        error: function (jqXHR, textStatus, errorThrown) {
			            console.debug(jqXHR);
			            console.debug(textStatus);
			            console.debug(errorThrown);
			            switch (jqXHR.status) {
			                case 409:
			                    console.error(jqXHR.responseText);
			                    alert(`배포 서버 상태조회 실패. [${jqXHR.responseText}]`);
			                    break;
			                default:
			                    console.error("Failed to update Deploy Info");
			                    alert("배포 서버 상태조회 실패. 서버 연결 상태 및 로그를 확인하세요.");
			                    break;
			            }
			        }
			    });
		    } else {
				alert(response.message);
			}
		},
		error: function () {
			console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`모듈 배포 진행상태조회 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Deploy Info");
                    alert("모듈 배포 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
			}
		}
	});
}

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
    const regexIpFormat = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
	const regexPortFormat = /^(6553[0-5]|655[0-2]\d|65[0-4]\d{2}|6[0-4]\d{3}|5\d{4}|[0-9]\d{0,4})$/;
    for (let serialize of serializeArray) {
        const fieldName = serialize.name.toString();
        const fieldValue = serialize.value.toString();

        if (fieldName == "serverIp") {
            if (!regexIpFormat.test(fieldValue)) {
                alert("잘못된 IP 형식입니다.");
                return false;
            }
        } else if (fieldName == "serverPort") {
			if (!regexPortFormat.test(fieldValue)) {
                alert("잘못된 포트번호입니다.");
                return false;
            }
		}
    }
    return true;
};