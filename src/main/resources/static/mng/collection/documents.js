// collection export and download script

$(document).ready(function() {
	
	if (collectionId > 0) {
		$('#show').css('display','inline');
		$('#divideTitle').css('display','inline');
	} else {
		$('#show').css('display','none');
		$('#divideTitle').css('display','none');
	}
	
	fnDoList(1);
});

function fnDoList(pageRow){
	
	var collectionId = $('#divide').val();
	
	if (collectionId == null){
		return false;
	}
	
	var params = new Object();
	
	params.pageRow = pageRow;
	params.collectionId = $('#divide').val();
	params.searchPattern = $('#searchPattern').val();
	
	$.ajax({
		url: contextPath + "/documentRest/getDataList",
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
        		
        		$("#documentCount").html('전체 (' + data.totalCount + '건)');		// 검색결과 전체 건수
        		$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
        		$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)
        		
				$('#show').css('display','inline');
				$('#divideTitle').css('display','inline');
				
        	}, 'Timeout...!');
		}
	})
}

/**
 * 팝업창 띄우기
 * @param target
 * @returns {boolean}
 */
const showPopup = function showPopupByTarget(target, collectionId, docId, pageRow) {
    let $target;
    switch (target) {
        case "document":
            showDocumentPopup(collectionId, docId, pageRow);
            break;
        default:
            console.error(`Undefined popup target="${target}"`);
            break;
    }
};

const showDocumentPopup = function (collectionId, docId, pageRow) {

	var param = new Object();
	
	param.collectionId = collectionId;
	param.docId = docId;
	param.pageRow = pageRow;

    $.ajax({
        async: false,
        type: "POST",
        url: contextPath + '/documentRest/getDataDetail',
        data: JSON.stringify(param),
        contentType: 'application/json;charset=UTF-8',
        success: function (document) {
            let $target = $("#document_pop");
			
			// 양식 추가
			$('#documentForm').html(document.documentForm);

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
            alert("문서 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
        }
    });
};

/**
 * 팝업창 숨기기
 * @param target
 */
const hidePopup = function (target) {
    let $target;
    switch (target) {
        case "document":
            $target = $("#document_pop");
            break;
    }
    
    $target.find("input,select,textarea").val("");
    $target.hide();
};

/**
 * 컬렉션 수정
 * @returns {boolean}
 */
const updateDocument = function () {

    let $form_document = $("#document_pop form");

    $.ajax({
        type: "POST",
        url: contextPath + '/documentRest/updateDocument',
        data: $form_document.serialize(),
        beforeSend: function () {
            console.log(`Collection to update: ${$form_document.serialize()}`);
            document.querySelector("#document_pop button[type=submit]").disabled = true;
        },
        success: function (result) {
            alert(result.resultMsg);
            
            if (result.result == "S"){
	            location.href = "/collection/" + result.collectionId + "/documents";
			} 
            document.querySelector("#document_pop button[type=submit]").disabled = false;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            switch (jqXHR.status) {
                case 403:
                    console.error("해당 문서 수정 및 삭제 권한이 없습니다.");
                    alert("해당 문서 수정 및 삭제 권한이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`문서 수정 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.debug(jqXHR.status);
                    console.debug(jqXHR.responseJSON);
                    console.error("Failed to create Collection");
                    alert("문서 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
            document.querySelector("#document_pop button[type=submit]").disabled = false;
        }
    });

    return false;
};
