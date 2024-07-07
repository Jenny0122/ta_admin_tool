// collection export and download script

var startDt = beforeDate();
var endDt = beforeDate();

$(document).ready(function() {
	fnDoList(1)
	
	$('input[name="dateRange"]').daterangepicker(
		{
			minYear: 1900,
		    maxYear: 9999,
		    startDate: startDt,
		    endDate: endDt,
		    locale: { 
				format: 'YYYY.MM.DD',
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
		}
	);
});

function fnDoList(pageRow){
	
	var params = new Object();
	
	params.pageRow = pageRow;
	params.pageSize = $('#show').val();
	
	$.ajax({
		url: contextPath + "/collectionRest/getDataList",
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
        		
        		$("#totalCount").html('전체 컬렉션 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
        		$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)
        		
        	}, 'Timeout...!');
		}
	})
}

/**
 *  비동기 요청이 폴링되고 있는지 체크.
 * 모듈 패턴으로 캠슐화 구현.
 * @type {{start(): void, stop(): void, isInProgress(): boolean}}
 * @see https://codeameba.github.io/2019/05/10/programming/js-no-more-global-variable/
 */
const PollingStatus = (function () {

    // private
    let inProgress = false;

    return {
        /**
         * @returns {boolean} return true if start in progress, else false.
         */
        isInProgress() {
            return inProgress;
        },
        /**
         * sets status as in progress.
         */
        start() {
            inProgress = true;
            document.querySelector("a.close_download").onclick = function () {
                alert("Export 가 진행중입니다.");
            };
        },
        /**
         * sets status as not in progress.
         */
        stop() {
            inProgress = false;
            document.querySelector("a.close_download").onclick = function () {
                hidePopup("download");
            };
        }
    };

}());


/**
 * collection export 요청.
 * @param {number} collectionId collection's id to export.
 * @param {string} exportFormat CSV | JSON | SCD
 */
const requestExport = function requestAsnycExportAndFileWrite(collectionId, exportFormat) {

    if (!collectionId) {
        alert("컬렉션을 먼저 선택해주세요.");
        throw `Invalid collectionId '${collectionId}'`;
    }

//    const exportFormatSet = new Set(["CSV", "SCD", "JSON"]);
    const exportFormatSet = new Set(["CSV", "JSON"]);

    if (!exportFormat || !exportFormatSet.has(exportFormat)) {
        alert("다운로드 파일 유형을 먼저 선택해주세요.");
        document.getElementById("field_type").focus();
        throw `Invalid exportFormat '${exportFormat}'`;
    }

    if (PollingStatus.isInProgress()) {
        alert("컬렉션 다운로드를 위해 파일을 생성 중입니다. 잠시만 기다려주세요.");
        // TODO(wisnt65) 진행중이던 export 상태 보여주기?
        throw `Duplicate export requests for collectionId='${collectionId}'.`;
    }

/*
    if (exportFormat === "SCD") {
        alert("SCD 파일로 다운로드할 경우, \nSCD 제약사항에 따라 \"DOCID\" 필드가 항상 최상단에 출력됩니다.");
    }
*/

    $.ajax({
        type: "POST",
        url: `${contextPath}/documentRest/${collectionId}/exports`,
        data: {format: exportFormat, dateRange: ""},
        beforeSend: function () {
            PollingStatus.start();
        },
        success: function (links, textStatus, jqXHR) {
            if (jqXHR.status == 202) {
                // if export-request accepted,
                console.info("Success to request exporting collection's documents.", links);
                setTimeout(function () {
                    refreshExportStatus(links);
                }, 500);
            }
            if (jqXHR.status == 204) { // no content
                PollingStatus.stop();
                alert("해당 컬렉션은 문서가 존재하지 않습니다.\n" + " 업로드 중일 경우 완료 후 재시도 바랍니다.");
                location.reload();
            }
            // other case ?
            console.debug(jqXHR);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            PollingStatus.stop();
            console.error(`Failed to request export collection-${collectionId}. cause=${jqXHR.responseJSON}`);
            alert("컬렉션 다운로드 요청에 실패했습니다. 로그를 확인해주세요");
        },
        complete: function (jqXHR, textStatus) {
            console.debug(jqXHR);
        }
    });
};


/**
 * 미분류 데이터 요청.
 * @param {number} collectionId collection's id to export.
 * @param {string} exportFormat CSV | JSON | SCD
 */
const downloadNotClsData = function (dateRange, exportFormat) {
	
    const exportFormatSet = new Set(["CSV", "JSON"]);
	
	if (dateRange == null || dateRange == '') {
        alert("날짜를 선택해주세요.");
        document.getElementById("dateRange").focus();
        throw `Invalid dateRange '${dateRange}'`;
	}

    if (!exportFormat || !exportFormatSet.has(exportFormat)) {
        alert("다운로드 파일 유형을 선택해주세요.");
        document.getElementById("field_type").focus();
        throw `Invalid exportFormat '${exportFormat}'`;
    }

    if (PollingStatus.isInProgress()) {
        alert("미분류 데이터 다운로드를 위해 파일을 생성 중입니다. 잠시만 기다려주세요.");
        // TODO(wisnt65) 진행중이던 export 상태 보여주기?
        throw `Duplicate export requests for collectionId='${collectionId}'.`;
    }
    
    
    $.ajax({
        type: "POST",
        url: `${contextPath}/documentRest/getNoClsDateCheck`,
        data: {dateRange: dateRange},
    	success: function (response) {
			if (response.result == "S") {
		    	$.ajax({
			        type: "POST",
			        url: `${contextPath}/documentRest/0/exports`,
			        data: {format: exportFormat, dateRange: dateRange},
			        beforeSend: function () {
			            PollingStatus.start();
			        },
			        success: function (links, textStatus, jqXHR) {
			            if (jqXHR.status == 202) {
			                // if export-request accepted,
			                console.info("Success to request exporting None Classified Data Documents.", links);
			                setTimeout(function () {
			                    refreshExportStatus(links);
			                }, 500);
			            }
			            if (jqXHR.status == 204) { // no content
			                PollingStatus.stop();
			                alert("미분류 데이터가 존재하지 않습니다.");
			                location.reload();
			            }
			            // other case ?
			            console.debug(jqXHR);
			        },
			        error: function (jqXHR, textStatus, errorThrown) {
			            PollingStatus.stop();
			            console.error(`Failed to request export None Classified Data Documents. cause=${jqXHR.responseJSON}`);
			            alert("미분류 데이터 다운로드 요청에 실패했습니다. 로그를 확인해주세요");
			        },
			        complete: function (jqXHR, textStatus) {
			            console.debug(jqXHR);
			        }
			    });
			} else {
				alert(response.resultMsg);
			}
    	},
    	error: function (jqXHR, textStatus, errorThrown) {
            PollingStatus.stop();
            console.error(`Failed to request export None Classified Data Documents. cause=${jqXHR.responseJSON}`);
            alert("미분류 데이터 다운로드 요청에 실패했습니다. 로그를 확인해주세요");
        },
        complete: function (jqXHR, textStatus) {
            console.debug(jqXHR);
        }
    });
};


/**
 * 주기적으로 export 진행 상태를 체크 및 화면를  업데이트하고
 * 완료될 경우 donwload 시작.
 * @param {Object} links
 * @param {Object} links.stop
 * @param {string} links.stop.method
 * @param {string} links.stop.href
 * @param {Object} links.status
 * @param {string} links.status.method
 * @param {string} links.status.href
 */
const refreshExportStatus = function refreshCollectionExportStatus(links) {
    console.debug(links);
    const statusAPI = links.status;

    const getExportStatus = function getCollectionExportProgressStatus() {
        const progressElement = document.getElementById("progress-span");

        $.ajax({
            // type: statusAPI.method,
            // url: statusAPI.href,
            type: statusAPI.method,
            url: statusAPI.href,
            timeout: 1000,
            statusCode: {
                202: function (exportStatus, textStatus, jqXHR) {
                    progressElement.innerText = `파일 생성 중... ${exportStatus.writeCount}/${exportStatus.resourceInfo.countDocuments}`;
                    console.debug("Keep checking");
                },
                201: function (filename, textStatus, jqXHR) {
                    let location = jqXHR.getResponseHeader("location");
                    console.log("Complete exporting collection's documents.", location);

                    clearInterval(statusCheckInterval);
                    PollingStatus.stop();

                    progressElement.title = decodeURI(filename);
                    // progressElement.innerHTML = `<a href="${location}" title="마우스 우측 클릭 > 다른 이름으로 링크 저장">파일 다운로드 링크</a>`;
                    progressElement.innerHTML = "잠시 후 다운로드가 시작됩니다.";

                    // download exported-file
                    setTimeout(function () {
                        // GET Resource (exorted file) from URI
                        window.open(location, "_parent");
                        alert("파일 생성이 완료되었습니다. 잠시 다운로드가 시작됩니다. ");
                        document.getElementById("progress-span").innerHTML = "";
                        hidePopup("download");
                    }, 500);
                },
                204: function (data, textStatus, jqXHR) {
                    document.getElementById("progress").innerText = data;
                    console.error("Failed to proceed exporting collection's documents. Check log message.");

                    clearInterval(statusCheckInterval);
                    PollingStatus.stop();

                    // TODO(wisnt65) : 관리자에게만 공개 for debugging, logfile 확인.
                    if (confirm("파일 생성에 실패했습니다. 로그를 확인해주세요")) {
                        window.open("/actuator/logfile", "_blank");
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error("Failed to check state");
                console.error(jqXHR.responseJSON);
                clearInterval(statusCheckInterval);
                PollingStatus.stop();
            },
        });
    };

    getExportStatus();

    const statusCheckInterval = setInterval(getExportStatus, 500);

    console.debug(`statusCheckInterval : ${statusCheckInterval}`);

    // 일단은 3분 제한
    setTimeout(function () {
        console.debug(statusCheckInterval);
        if (statusCheckInterval) {
            clearInterval(statusCheckInterval);
        }
    }, 1000 * 60 * 3);
};


/** 각 컬렉션의 문서 업로드 상태를 조회하여 업데이트
 * <h3>업로드 상태 </h3>
 * <table>
 *     <th>UPLOAD STATUS</th>
 *     <th>in progress</th>
 *     <th>staged file</th>
 *     <th>upload history</th>
 *     <tr><td>진행 중</td><td>O</td><td>O</td><td>O</td></tr>
 *     <tr><td>-</td><td rowspan="3">X</td><td>X</td><td>X</td></tr>
 *     <tr><td>대기</td><td>O</td><td>X</td></tr>
 *     <tr><td>완료</td><td>X</td><td>O</td></tr>
 * </table>
 */
 
 /*
  - 파일 업로드 상태 확인을 위한 function,
  - 현재 테이블 설계 안되어있는 상태로 추후 작업예정 
 
const refreshUploadStatus = function refreshCollectionUploadStatus() {
    const elementsUploadStatus = {};
    [...document.querySelectorAll("tbody > tr")].forEach(value => {
        elementsUploadStatus[value.dataset.collectionId] = value.querySelector("td.uploadState");
    });

    if (!Object.keys(elementsUploadStatus).length) {
        throw `Collections table is empty!`;
    }

    const getUploadStatus = function requestGetUploadStatus() {
        $.ajax({
            type: "GET",
            url: "/api/collection/all/status",
            timeout: 500,
            success: function (collectionsStatus, textStatus, request) {
                for (let status of collectionsStatus) {
                    if (elementsUploadStatus[status.id]) {
                        // elementsUploadStatus[status.id].innerHTML = `<a href="/collection/${status.id}/upload">${status.label}</a>`;
                        elementsUploadStatus[status.id].innerHTML = `<span title="${status.detail}" ` +
                            `onclick="location.href='/collection/${status.id}/upload';" ` +
                            `style="cursor:pointer">${status.label}</span>`;
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.debug("Failed to get upload status of collections.");
            }
        });
    };

    // init request
    getUploadStatus();

    const checkStatusInterval = setInterval(getUploadStatus, 2500);

    // 5분 후 종료
    setTimeout(function () {
        clearInterval(checkStatusInterval)
    }, 5 * 60 * 1000)

};
*/

/**
 * 컬렉션 정보 채우기
 * @returns {boolean}
 */
const showUpdatePopup = function () {
    // [1] get collectionId
    let collectionId = $("input[type=radio][name=coll_choice]:checked").val();
    if (!collectionId) {
        alert(`컬렉션을 먼저 선택하세요`);
        return false;
    }

    $.ajax({
        async: false,
        type: "GET",
        url: contextPath + '/collectionRest/getDataDetail?collectionId=' + collectionId,
        /**
         *
         * @param {Object} collection
         * @param {String} collection.name
         * @param {String} collection.desc
         */
        success: function (collection) {
            let $target = $("#update_pop");

            // [2] collection info 채우기
            $target.find("input[name=collectionId]").val(collection.collectionId).focus();
            $target.find("input[name=collectionName]").val(collection.collectionName).focus();
            $target.find("select[name=collectionType]").val(collection.collectionType);
            $target.find("select[name=collectionJob]").val(collection.collectionJob);
            $target.find("textarea[name=collectionDesc]").val(collection.collectionDesc);

            // [3] 해당 팝업창 띄우기
            $target.show();
        },
        /**
         *
         * @param {jqXHR} jqXHR
         * @param {String} textStatus
         * @param {String} errorThrown
         */
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Failed to get Collection");
            console.error(jqXHR.status);
            console.error(jqXHR.responseJSON.message);
            console.debug(jqXHR.responseJSON);
            alert("컬렉션 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

/**
 * 팝업창 띄우기
 * @param target
 * @returns {boolean}
 */
const showPopup = function showPopupByTarget(target, nsFlag) {
    let $target;
    switch (target) {
        case "create":
            $target = $("#create_pop");
            $target.find("input,select").val("");
            $target.find("select[name=collectionType]").val("CLASSIFICATION");
            $target.find("select[name=collectionJob]").val("ANALYSIS");
            $target.show();
            break;
        case "update":
            showUpdatePopup();
            break;
        case "download":
        	if (nsFlag != 'N') {
				$('#downloadPopTitle').text("컬렉션 다운로드");
				
		        const $collection = $("input[type=radio][name=coll_choice]:checked");
		        if (!$collection || !$collection.val()) {
		            alert(`컬렉션을 먼저 선택하세요`);
		            return false;
		        }
		        if ($collection.data("count") === 0) {
		            alert("해당 컬렉션은 문서가 존재하지 않습니다.\n" +
		                "업로드 중일 경우 완료 후 재시도 바랍니다.");
		            return false;
		        }
		        
				$('#selectDateRange').hide();				
		        $('#exportCollBtn').show();
		        $('#exportNClsBtn').hide();
			} else {
				$('#downloadPopTitle').text("미분류 데이터 다운로드");
				
				$('#selectDateRange').show();	
		        $('#exportCollBtn').hide();
		        $('#exportNClsBtn').show();

				// 오늘날짜로 설정
				$('input[name="dateRange"]').data('daterangepicker').setStartDate(startDt);			
				$('input[name="dateRange"]').data('daterangepicker').setEndDate(endDt);			
				// $('input[name="dateRange"]').val(startDt + ' - ' + endDt);			
			}
			
            $target = $("#download_pop");
            $target.show();
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
};

/**
 * 팝업창 숨기기
 * @param target
 */
const hidePopup = function (target) {
    let $target;
    switch (target) {
        case "create":
            $target = $("#create_pop");
            break;
        case "update":
            $target = $("#update_pop");
            break;
        case "download":
            $target = $("#download_pop");
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
    $target.find("input,select,textarea").val("");
    $target.hide();
};

/**
 * 선택된 컬렉션 삭제
 * @returns {boolean}
 */
const deleteCollection = function () {
    const checkedCollection = document.querySelector("input[type=radio][name=coll_choice]:checked");
    let collectionId = checkedCollection.value;
    if (!collectionId) {
        alert(`컬렉션을 먼저 선택하세요`);
        return false;
    }

    let btnDelete = document.querySelector("button[name=delete]");
    btnDelete.disabled = true;

	var params = new Object();
	params.resourceType = 'collection';
	params.resourceId = collectionId;
	
	$.ajax({
		type: "POST",
		url: contextPath + '/taskRest/chkTaskResouce',
		contentType : "application/json",
		dataType: "json",
		data : JSON.stringify(params),
		success: function (response) {
			if (response.result == "S") {
				if (confirm(`선택한 컬렉션 '${checkedCollection.dataset.collectionName}' 을 삭제합니다. 삭제된 컬렉션은 복구 불가능 합니다.`)) {
			        $.ajax({
			            type: "GET",
			            url: contextPath + '/collectionRest/deleteCollection?collectionId=' + collectionId,
			            success: function (response) {
			                // collection 삭제 성공
			                console.log(`컬렉션 삭제 성공`);
							
			                if (confirm(response.resultMsg)) {
			                    location.reload(true);
			                }
			            },
			            error: function (jqXHR, textStatus, errorThrown) {
			
			                switch (jqXHR.status) {
			                    case 403:
			                        console.error("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
			                        alert("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
			                        break;
			                    case 409:
			                        console.error(jqXHR.responseText);
			                        alert(`컬렉션 삭제 실패. [${jqXHR.responseText}]`);
			                        break;
			                    default:
			                        console.debug(jqXHR.status);
			                        console.debug(jqXHR.responseJSON.message);
			                        console.debug(jqXHR.responseJSON);
			                        console.error("Failed to create Collection");
			                        alert("컬렉션 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
			                        break;
			                }
			            }
			        });
			    }
			} else {
				alert(response.resultMsg);
			}
		},
        error: function (jqXHR, textStatus, errorThrown) {

            switch (jqXHR.status) {
                case 403:
                    console.error("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
                    alert("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`컬렉션 삭제 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.debug(jqXHR.status);
                    console.debug(jqXHR.responseJSON.message);
                    console.debug(jqXHR.responseJSON);
                    console.error("Failed to create Collection");
                    alert("컬렉션 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
     });
    btnDelete.disabled = false;
};

/**
 * 선택된 컬렉션 업로드 페이지로 이동
 * @returns {boolean}
 */
const uploadCollection = function () {
    let collectionId = $("input[type=radio][name=coll_choice]:checked").val();
    if (!collectionId) {
        alert(`컬렉션을 먼저 선택하세요`);
        return false;
    }

    location.href = contextPath + '/collection/upload?collectionId=' + collectionId;
};

/**
 * 컬렉션 추가
 * @returns {boolean}
 */
const createCollection = function () {
    let $form_collection = $("#create_pop form");

    if (!isFormValid($form_collection.serializeArray()))
        return false;
     
    $.ajax({
        type: "POST",
        url: contextPath + '/collectionRest/insertCollection',
        data: $form_collection.serialize(),
        beforeSend: function () {
            console.log(`Collection to create: ${$form_collection.serialize()}`);
            document.querySelector("#create_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            
            if (response.result == "S") {
	            // collection 생성 성공
	            console.info("created collection : ", response.collectionName);
	            if (confirm("컬렉션이 추가되었습니다.\n" + `컬렉션 '${response.collectionName}'에 바로 문서 업로드를 진행하시겠습니까?`)) {
	                location.href = `${contextPath}/collection/upload?collectionId=${response.collectionId}`;
	            } else {
	                location.reload();
	            }
			} else {
				alert(response.resultMsg);
				$('#collectionName').focus();
            	document.querySelector("#create_pop button[type=submit]").disabled = false;
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Failed to create Collection");
            document.querySelector("#create_pop button[type=submit]").disabled = false;
            alert(jqXHR.responseText);
        }
    });
    
    return false;
};

/**
 * 컬렉션 수정
 * @returns {boolean}
 */
const updateCollection = function () {
    let collectionId = $("input[type=radio][name=coll_choice]:checked").val();
    if (!collectionId) {
        alert(`컬렉션을 먼저 선택하세요`);
        return false;
    }

    let $form_collection = $("#update_pop form");

    if (!isFormValid($form_collection.serializeArray()))
        return false;

    $.ajax({
        type: "POST",
        url: contextPath + '/collectionRest/updateCollection',
        data: $form_collection.serialize(), //default contentType: 'application/x-www-form-urlencoded'
        beforeSend: function () {
            console.log(`Collection to update: ${$form_collection.serialize()}`);
            document.querySelector("#update_pop button[type=submit]").disabled = true;
        },
        success: function (response) {
            alert(response.resultMsg);
            
            if (response.result == "S"){
	            location.reload();
			} else {
				document.querySelector("#update_pop button[type=submit]").disabled = false;
				return false
			}
        },
        error: function (jqXHR, textStatus, errorThrown) {
            switch (jqXHR.status) {
                case 403:
                    console.error("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
                    alert("해당 컬렉션 수정 및 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`컬렉션 수정 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.debug(jqXHR.status);
                    console.debug(jqXHR.responseJSON);
                    console.error("Failed to create Collection");
                    alert("컬렉션 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#update_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};

/**
 * @param {Array} serializeArray
 * @param {Object} serializeArray[]
 * @param {String} serializeArray[].name
 * @param {String} serializeArray[].value
 *
 * @returns {boolean} return true if form is valid, else return false.
 */
const isFormValid = function (serializeArray) {
    const regexStartingDots = /^\./; // cannot start with dot (.)
    const regexForbiddenChars = /^[^\\/:\*\?"<>\|]+$/; // forbidden characters \ / : * ? " < > |
    const regexForbiddenFileNames = /^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i; // forbidden file names

    for (let serialize of serializeArray) {
        const fieldName = serialize.name.toString();
        const fieldValue = serialize.value.toString();

        if (fieldName == "name") {
            if (fieldValue.startsWith(".") || fieldValue.startsWith(" ")) {
                alert("점('.')이나 공백(' ')으로 컬렉션명을 시작할 수 없습니다.");
                return false;
            }
            // 윈도우 파일명 금지 문자 체크
            if (!regexForbiddenChars.test(fieldValue)) {
                alert("\\ / : * ? \" < > | 특수기호는 컬렉션명으로 사용할 수 없습니다.");
                return false;
            }
            if (regexForbiddenFileNames.test(fieldValue)) {
                alert("사용할 수 없는 컬렉션명입니다.");
                return false;
            }
            if (fieldValue.replace(/\./g, "").trim().length === 0) {  // 점(.)과 공백 제거
                alert("컬렉션명은 공백과 점(.)만으로는 설정할 수 없습니다.");
                return false;
            }
        }
    }
    return true;
};

function beforeDate() {
	
	var date = new Date();
	date.setDate(date.getDate() - 1);

	return date.getFullYear() + '.' + (date.getMonth() + 1) + '.' + (date.getDate() > 10 ? date.getDate() : '0' + date.getDate());
}