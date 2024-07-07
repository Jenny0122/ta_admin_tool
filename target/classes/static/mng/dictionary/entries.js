// collection export and download script

let modifyFlag = 'N';
let oldSynonyms = '';

$(document).ready(function() {
	fnDoList(1);
});

function fnDoList(pageRow){
	
	var dictionaryId = $('#dictionary').val();
	
	if (dictionaryId == null){
		return false;
	}
	
	var params = new Object();
	
	params.pageRow = pageRow;
	params.pageSize = $('#show').val();
	params.dictionaryId = $('#dictionary').val();
	params.searchPattern = $('#searchPattern').val();
	
	$.ajax({
		url: contextPath + "/entryRest/getDataList",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$("#listArea").html("<div id='LoadingImage' class='loading-LoadingImage'><div class='ml19p'><img th:src='@{/img/loading.gif}'></div></div>");
		},
        success: function(data) {
        	setTimeout(function() {
        		$("#LoadingImage").remove();
        		
        		$("#entryCount").html('전체 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
        		$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)
        		
				$('#show').css('display','inline');
				$('#divideTitle').css('display','inline');
        		
        	}, 'Timeout...!');
		}
	})
}

/**
 * 사전 엔트리 검색 요청 및 결과 페이지로 이동. FIXME(wisnt65): ajax로 table.content 만 변경하는 방식으로 리팩토링
 * @param searchPattern 검색하려는 entry pattern. ex) 테스트 --> .+테스트.+ 전부 검색됨
 */
const searchEntry = function searchEntryByPatternMatching() {

    const dictionaryId = document.getElementById('dictionary').value;
    if (dictionaryId == null)
        return false;

    const searchPattern = document.getElementById('searchPattern').value;
    console.debug(searchPattern);

    let pageSize = document.querySelector('select[name=size]').value || 10;

	var params = new Object();
	
	params.pageRow = 1;
	params.pageSize = pageSize;
	params.dictionaryId = dictionaryId;
	params.searchPattern = searchPattern;
	
	$.ajax({
		url: contextPath + "/entryRest/getDataList",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$("#listArea").html("<div id='LoadingImage' class='loading-LoadingImage'><div class='ml19p'><img th:src='@{/img/loading.gif}'></div></div>");
		},
        success: function(data) {
        	setTimeout(function() {
        		$("#LoadingImage").remove();
        		
        		$("#entryCount").html('전체 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
        		$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)
        		
				$('#show').css('display','inline');
				$('#divideTitle').css('display','inline');
        		
        	}, 'Timeout...!');
		}
	})
};

/**
 * check 한 엔트리 삭제 요청 및 페이지 새로고침
 * @param {Number} dictionaryId
 */
const deleteEntries = function deleteEntriesById(dictionaryId) {
    let ids = [];
    document.querySelectorAll('input[name=entry_choice]:checked').forEach((node, key) => {
        ids.push(node.value);
    });

    if (ids.length < 1) {
        alert('삭제할 엔트리를 먼저 선택하세요.');
        return false;
    }
    if (!confirm(`${ids.length} 개의 엔트리를 삭제하시겠습니까?`)) {
        return false;
    }

    $.ajax({
        type: "POST",
        url: `${contextPath}/entryRest/${dictionaryId}/deleteEntry`,
        data: {id: ids},
        success: function (response) {
            console.debug(`사전 엔트리 삭제 성공: `);
            console.debug(response);
            // alert(`사전 엔트리가 삭제되었습니다.`);
            location.reload(true);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("엔트리 삭제 권한이 없습니다.");
                    alert("엔트리 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`엔트리 삭제 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error(`Failed to delete Entry: ${ids}`);
                    alert("엔트리 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
    // window.location.reload(true);
};

/**
 * 사용자 입력 파싱 및 엔트리 추가 요청. +페이지 새로고침.
 * 새로 추가된 엔트리는 마지막 페이지에서 확인 가능
 * @param {Number} dictionaryId Dictionary's id
 * @param {String} entry ';' 을 구분자로 여러 엔트리 입력 가능
 */
const addEntry = function parseUserInputAsEntriesAndAddToDictionary() {
    const re = /\s*(?:;)\s*/;
    const dictionaryId = document.getElementById('dictionary').value;
    const entry = document.getElementById('entryOnly').value.trim();
    // let entries = entry.trim().split(re).filter(value => value);

	if (modifyFlag == 'Y') {
		alert("동의어 작업 중에는 엔트리를 추가할 수 없습니다.");
		return false;
	}

    if (!isEntryValid(entry))
        return;

    if (!confirm(`입력하신 엔트리(${entry})를 사전에 추가하시겠습니까?`)) {
        return;
    }

	$.ajax({
        type: "POST",
        url: `${contextPath}/entryRest/${dictionaryId}/chkDuplicateEntry`,
        data: {entry: entry},
        beforeSend: function () {
            console.debug(`엔트리 중복체크 : ${entry} `);
        },
        success: function (response) {
			
			if(response.result == 'S') {
				$.ajax({
			        type: "POST",
			        url: `${contextPath}/entryRest/${dictionaryId}/insertEntry`,
			        data: {entry: entry},
			        beforeSend: function () {
			            console.debug(`엔트리 추가 요청 : ${entry} `);
			        },
			        success: function (response) {
			            console.log(response);
			            // if (confirm(`엔트리 추가 성공. 확인하시겠습니까?`)) {
			            //     location.reload(); //FIXME: last page 로 이동
			            // }
			            location.reload(true);
			        },
			        error: function (jqXHR, textStatus, errorThrown) {
			            console.debug(jqXHR);
			            console.debug(textStatus);
			            console.debug(errorThrown);
			            switch (jqXHR.status) {
			                case 403:
			                    console.error("엔트리 추가 권한이 없습니다.");
			                    alert("엔트리 추가 권한이 없습니다.");
			                    break;
			                case 409:
			                    console.error(jqXHR.responseText);
			                    alert(`엔트리 추가 실패. [${jqXHR.responseText}]`);
			                    break;
			                default:
			                    console.error(`Failed to add Entry: ${entry}`);
			                    alert("엔트리 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			                    break;
			            }
			        }
			    });
		    } else {
				alert(response.resultMsg);
			}
		},
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            console.debug(textStatus);
            console.debug(errorThrown);
            switch (jqXHR.status) {
                case 403:
                    console.error("엔트리 추가 권한이 없습니다.");
                    alert("엔트리 추가 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`엔트리 추가 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error(`Failed to add Entry: ${entry}`);
                    alert("엔트리 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        }
    });
};

const isEntryValid = function (entry) {
    const regexForbiddenChars = /^[ㄱ-ㅎ가-힣0-9a-zA-Z]+$/; // 알파벳, 숫자, 한글만 허용
    let isValid = true;
    const entryStr = entry.toString();

    if (entryStr.length == 0) {
        alert("내용이 없습니다.");
        isValid = false;
    } 
    /*
    else if (!regexForbiddenChars.test(entryStr)) {
        alert("한글, 영문, 숫자만 사용할 수 있습니다. " + entryStr);
        isValid = false;
    }
    */
    return isValid;
};

// 동의어 inputbox 활성화
function enableTextBox(idx) {
	
	if (modifyFlag == 'Y') {
		alert("현재 작업 중인 동의어 수정 작업이 종료되어야 합니다.");
	} else {
		modifyFlag = 'Y';
		$("#synonyms_"+idx).css('width','85.5%');
		$("#synonyms_"+idx).attr('disabled', false);
		$("#modifyBtn_"+idx).css('display','none');
		$("#saveBtn_"+idx).css('display','inline-block');
		$("#cancelBtn_"+idx).css('display','inline-block');
		
		oldSynonyms = $("#synonyms_"+idx).val();
	}
}

// 동의어 inputbox 비활성화
function disableTextBox(idx) {
	
	if (modifyFlag == 'N') {
		alert("현재 작업 중인 동의어 수정 작업이 없습니다.");
	} else {
		modifyFlag = 'N';
		$("#synonyms_"+idx).val(oldSynonyms);
		
		$("#synonyms_"+idx).css('width','95%');
		$("#synonyms_"+idx).attr('disabled', true);
		$("#modifyBtn_"+idx).css('display','inline-block');
		$("#saveBtn_"+idx).css('display','none');
		$("#cancelBtn_"+idx).css('display','none');
	}
}

// 동의어 변경
function saveSynonym(idx) {
	var newSynonyms = $("#synonyms_"+idx).val();
	if (newSynonyms == oldSynonyms) {
		alert("변경된 내용이 없습니다.");
		return false;
	}
	
	if (!confirm("동의어를 저장하시겠습니까?")){
		return false;	
	}
	
	var param = new Object();
	
	param.entryId = idx;
	param.dictionaryId = document.getElementById('dictionary').value;
	param.synonyms = newSynonyms;
	
	$.ajax({
        type: "POST",
        url: `${contextPath}/entryRest/modifySynonym`,
		contentType : "application/json",
        data: JSON.stringify(param),
        success: function (response) {
			if (response.result == 'S'){
				alert(response.resultMsg);
	            location.reload(true);
			} else {
				alert(response.resultMsg);
				return false;
			}
        }
    });
}