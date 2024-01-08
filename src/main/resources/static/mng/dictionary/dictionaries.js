$(document).ready(function() {
	fnDoList(1)
});

function fnDoList(pageRow) {

	var params = new Object();

	params.pageRow = pageRow;
	params.pageSize = $('#show').val();

	$.ajax({
		url: contextPath + "/dictionaryRest/getDataList",
		type: 'POST',
		cache: false,
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function() {
			$("#listArea").html("<div id='LoadingImage' class='loading-LoadingImage'><div style='margin-left:50%'><img th:src='@{/img/loading.gif}'></div></div>");
		},
		success: function(data) {
			setTimeout(function() {
				$("#LoadingImage").remove();

				$("#totalCount").html('사전 리스트 (' + data.totalCount + '건)');		// 검색결과 전체 건수
				$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
				$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)

			}, 'Timeout...!');
		}
	})
}

// 사전 정보 채우기
const showUpdatePopup = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();
	const dictionarySelectedName = dictionarySelected.data("name");

	if (!dictionarySelectedId) {
		alert(`사전을 먼저 선택하세요`);
		return false;
	}

	$.ajax({
		async: false,
		type: "GET",
		url: contextPath + '/dictionaryRest/getDataDetail?dictionaryId=' + dictionarySelectedId,
		success: function(dictionary) {
			console.info(`Selected dictionary to update: ${dictionary.name}`);
			let $target = $("#update_pop");

			// [2] dictionary info 채우기
			$target.find("input[name=dictionaryId]").val(dictionary.dictionaryId);
			$target.find("input[name=dictionaryName]").val(dictionary.dictionaryName);
			$target.find("textarea[name=dictionaryDesc]").val(dictionary.dictionaryDesc);
			$target.find("input[name=dictionaryType]").val(dictionary.dictionaryType);
			$target.find("select[name=dictionaryType]").val(dictionary.dictionaryType);
			$target.find("select[name=dictionarySharedYn]").val(dictionary.dictionarySharedYn);

			// [3] 해당 팝업창 띄우기
			$target.show();
		},
		error: function(data) {
			console.error("Failed to get Dictionary");
			console.debug(data);
			alert("사전 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
		}
	});
};

// 이벤트리스너 등록 : 생성, 수정, 삭제, 업로드 버튼

// 팝업창 띄우기
const showPopup = function(target) {
	// console.debug(target);
	let $target;
	switch (target) {
		case "create":
			$target = $("#create_pop");
			$target.find("input,select").val("");
			$target.find("select[name=dictionarySharedYn]").val("N");
			$target.show();
			break;
		case "update":
			showUpdatePopup();
			$target = $("#update_pop");
			break;
		case "download":
			const $dictionary = $("input[type=radio][name=dict_choice]:checked");
			if (!$dictionary || !$dictionary.val()) {
				alert("사전을 먼저 선택하세요");
				return false;
			}
			if ($dictionary.data("count") === 0) {
				alert("해당 사전은 엔트리가 존재하지 않습니다.\n" +
					"업로드 중일 경우 완료 후 재시도 바랍니다.");
				return false;
			}

			$target = $("#download_pop");
			$target.show();
			break;
		default:
			console.error(`Undefined popup target="${target}"`);
	}
};

// 팝업창 숨기기
const hidePopup = function(target) {
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
	}
	$target.find("input,select").val("");
	$target.hide();
};

// 선택된 사전 삭제
const deleteDictionary = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();
	const dictionarySelectedName = dictionarySelected.data("name");
	if (!dictionarySelectedId) {
		alert(`사전을 먼저 선택하세요`);
		return false;
	}

	var params = new Object();
	params.resourceType = 'dictionary';
	params.resourceId = dictionarySelectedId;
	$.ajax({
		type: "POST",
		url: contextPath + '/taskRest/chkTaskResouce',
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		success: function(response) {
			if (response.result == "S") {
				if (confirm(`선택한 사전 '${dictionarySelectedName}' 을 삭제합니다. 삭제된 사전은 복구가 불가능 합니다.`)) {
					$.ajax({
						type: "GET",
						url: contextPath + '/dictionaryRest/deleteDictionary?dictionaryId=' + dictionarySelectedId,
						success: function(response) {
							// dictionary 삭제 성공
							alert(response.resultMsg);
							if (response.result == "S") location.reload();
						},
						error: function(jqXHR, textStatus, errorThrown) {
							switch (jqXHR.status) {
								case 403:
									console.error("해당 사전의 삭제 권한이 없습니다.");
									alert("해당 사전의 삭제 권한이 없습니다.");
									break;
								case 409:
									console.error(jqXHR.responseText);
									alert(`사전 삭제 실패. [${jqXHR.responseText}]`);
									break;
								default:
									console.error("Failed to delete Dictionary");
									alert("사전 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
							}
						}
					});
				}
			} else {
				alert(response.resultMsg);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {

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
};

// 선택된 사전 업로드 페이지로 이동
const uploadDictionary = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();
	if (!dictionarySelectedId) {
		alert(`사전을 먼저 선택하세요`);
		return false;
	}

	location.href = contextPath + '/dictionary/upload?dictionaryId=' + dictionarySelectedId;
};

// 사전 추가
const createDictionary = function() {
	let $form_dictionary = $("#create_pop form");

	if (!isFormValid($form_dictionary.serializeArray())) {
		return false;
	}

	$.ajax({
		type: "POST",
		url: `${contextPath}/dictionaryRest/insertDictionary`,
		data: $form_dictionary.serialize(), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
			console.debug(`Dictionary to create: ${$form_dictionary.serialize()}`);
			document.querySelector("#create_pop button[type=submit]").disabled = true;
		},
		success: function(response) {
			// dictionary 생성 성공
			alert(response.dictionaryName + " - " + response.resultMsg);

			if (response.result == "S") {
				location.reload();
			} else {
				document.querySelector("#create_pop button[type=submit]").disabled = false;
				return false;
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.debug(jqXHR);
			console.debug(textStatus);
			console.debug(errorThrown);
			switch (jqXHR.status) {
				case 403:
					console.error("사전 추가 권한이 없습니다.");
					alert("사전 추가 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`사전 추가 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to create Dictionary");
					alert("사전 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			document.querySelector("#create_pop button[type=submit]").disabled = false;
		}
	});

	return false;
};

// 사전 수정
const updateDictionary = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();

	if (!dictionarySelectedId) {
		alert(`사전을 먼저 선택하세요`);
		return false;
	}

	let $form_dictionary = $("#update_pop form");

	if (!isFormValid($form_dictionary.serializeArray())) {
		return false;
	}

	$.ajax({
		type: "POST",
		url: `${contextPath}/dictionaryRest/updateDictionary`,
		data: $form_dictionary.serialize(), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
			console.debug(`Dictionary to update: ${$form_dictionary.serialize()}`);
			document.querySelector("#update_pop button[type=submit]").disabled = true;
		},
		success: function(response) {
			alert(response.resultMsg);

			if (response.result == "S") location.reload();
			document.querySelector("#update_pop button[type=submit]").disabled = false;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.debug(jqXHR);
			console.debug(textStatus);
			console.debug(errorThrown);
			switch (jqXHR.status) {
				case 403:
					console.error("해당 사전의 수정 권한이 없습니다.");
					alert("해당 사전의 수정 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`사전 수정 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update Dictionary");
					alert("사전 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			document.querySelector("#update_pop button[type=submit]").disabled = false;
		}
	});

	return false;
};

/**
 * 다운로드 사전
 * @returns {boolean}
 */
const downloadDictionary = function requestExportEntriesAsAFileAndDownload() {
	const $dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = $dictionarySelected.val();
	if (!dictionarySelectedId) {
		alert(`사전을 먼저 선택하세요`);
		return false;
	}

	let export_format = $("#download_pop select[name=export_format]").val();

	if (!export_format) {
		alert(`다운로드 파일 유형을 먼저 선택하세요`);
		return false;
	}

	export_format = export_format.toUpperCase();

	$.ajax({
		type: "POST",
		url: `${contextPath}/entryRest/${dictionarySelectedId}/exports`,
		data: { format: export_format },
		xhrFields: {
			responseType: "blob"
		},
		dataType: "binary",
		beforeSend: function() {
			// todo 화면 블락
		},
		success: function(data, textStatus, jQueryXHR) {
			if (jQueryXHR.status === 201) {
				let location = jQueryXHR.getResponseHeader("location");
				console.log("Complete exporting dictionary's entries.", location);
				window.open(location, "_parent");
			} else {
				console.error(`Failed to export dictionary-${dictionarySelectedId}`);
				console.debug(jQueryXHR.responseJSON);
				alert("다운로드에 실패했습니다. 새로고침 후 다시 시도해주세요");
				location.reload();
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.debug(jqXHR);
			console.debug(textStatus);
			console.debug(errorThrown);
			switch (jqXHR.status) {
				case 403:
					console.error("해당 사전의 다운로드 권한이 없습니다.");
					alert("해당 사전의 다운로드 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);  // TODO undefined 이슈
					alert(`사전 다운로드 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update Dictionary");
					alert("사전 다운로드 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
		},
		complete: function(jqXHR, textStatus) {
			// todo 화면 언블락
			// alert("사전 다운로드 요청 완료");
			hidePopup("download");
		}
	});
};

/**
 * @param {Array} serializeArray
 * @param {Object} serializeArray[]
 * @param {String} serializeArray[].name
 * @param {String} serializeArray[].value
 *
 * @private
 */
const isFormValid = function(serializeArray) {
	const regexForbiddenChars = /^[^\\/:\*\?"<>\|]+$/; // forbidden characters \ / : * ? " < > |
	const regexStartingDots = /^\./; // cannot start with dot (.)
	const regexForbiddenFileNames = /^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i; // forbidden file names

	for (let serialize of serializeArray) {
		const fieldName = serialize.name.toString();
		const fieldValue = serialize.value.toString();

		if (fieldName == "name") {
			if (fieldValue.startsWith(".") || fieldValue.startsWith(" ")) {
				alert("점('.')이나 공백(' ')으로 사전명을 시작할 수 없습니다.");
				return false;
			}
			// 윈도우 파일명 금지 문자 체크
			if (!regexForbiddenChars.test(fieldValue)) {
				alert("\\ / : * ? \" < > | 특수기호는 사전명으로 사용할 수 없습니다.");
				return false;
			}
			if (regexForbiddenFileNames.test(fieldValue)) {
				alert("사용할 수 없는 사전명입니다.");
				return false;
			}
			if (fieldValue.replace(/\./g, "").trim().length === 0) {  // 점(.)과 공백 제거
				alert("사전명은 공백과 점(.)만으로는 설정할 수 없습니다.");
				return false;
			}
		}
	}

	return true;
};