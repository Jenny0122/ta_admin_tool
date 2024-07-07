$(document).ready(function() {
	//fnDoList(1);
	
	// 트리 조회
	fnCateTree();
	
});

function fnDoList(pageRow) {

	var params = new Object();

	params.pageRow = pageRow;
	params.pageSize = $('#show').val();

	$.ajax({
		url: contextPath + "/autoqaRest/getQAScriptList",
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

				$("#totalCount").html('스크립트 리스트 (' + data.totalCount + '건)');		// 검색결과 전체 건수
				$("#dataTable").html(data.dataTable);		// 검색결과 Data Table(html)
				$("#pageNav").html(data.pageNav);			// 검색결과에 따른 Page Navigator(html)

			}, 'Timeout...!');
		}
	})
}

// 트리 조회
function fnCateTree() {
	var params = new Object();
	
	$.ajax({
		url: contextPath + "/autoqaRest/getQACategory",
		type: "POST",
		cache: false,
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		success: function(data) {
			//debugger;
			var company = new Array();
			// 데이터 받아옴
			$.each(data, function(idx, item){
				company[idx] = {id:item.id, parent:item.parentId, text:item.name};
			});
			// 트리 생성
			$('#treeViewMenu').jstree({
				core: {
						data: company    //데이터 연결
				},
				types: {
						'default': {
							'icon': 'jstree-folder'
						}
				},
				plugins: ['wholerow', 'types', 'contextmenu'],
				'contextmenu' : {
					"items": function ($node) {
						let ref = $('#treeViewMenu').jstree(true);
						return {
							"create": {
								"separator_before": false,
								"separator_after": true,
								"label": "생성",
								"action": function(obj) {}
							}
						}
					}
				}
			})
			.bind('loaded.jstree', function(event, data){
			//트리 로딩 롼료 이벤트
			})
			.bind('select_node.jstree', function(event, data){
			//노드 선택 이벤트
				var id = data.selected;
				if(data.instance.get_node(id).children.length != 0) return;

				var list = []
				while(id != '#') {
					var node = data.instance.get_node(id);
					id = node.parent;
					list.unshift(node.text)
				}
				$("#category_depth").text(list.join(' > '))
				fnDoList(1);
				// var params = new Object();
				// $.ajax({
				// 	async: false,
				// 	type: "POST",
				// 	url: contextPath + '/autoqaRest/getQAScriptListTest',
				// 	cache: false,
				// 	contentType: "application/json",
				// 	dataType: "json",
				// 	data: JSON.stringify(params),
				// 	success: function(data) {
				// 		console.log(data)
				// 		var rowData = data.dataTable
				// 		var rows = ""
				// 		for(var i in rowData){
				// 			rows = "<tr>\n"
				// 				+ "\t<td><input type=\"checkbox\" name=\"qa_choice\" value=\"qa_choice_{{0}}\"></td>\n".format(i)
				// 				+ "\t<td></td>\n"
				// 				+ "\t<td>\n"
				// 				+ "\t\t<select id=\"compliance_item_cd_{{rowNum}}\" name=\"complianceItemCd\" class=\"w180\" required readonly=\"readonly\">\n"
				// 				+ "\t\t\t<option value=\"\" selected disabled>준수항목을 선택하세요</option>\n"
				// 				+ "\t\t\t<option value=\"comItem01\" th:selected=\"${complianceItemCd=='10'}? 'selected'\">첫인사</option>\n"
				// 				+ "\t\t\t<option value=\"comItem02\" th:selected=\"${complianceItemCd=='20'}? 'selected'\">대기안내</option>\n"
				// 				+ "\t\t\t<option value=\"comItem03\" th:selected=\"${complianceItemCd=='30'}? 'selected'\">종료시점</option>\n"
				// 				+ "\t\t\t<option value=\"comItem04\" th:selected=\"${complianceItemCd=='40'}? 'selected'\">끝인사</option>\n"
				// 				+ "\t\t\t<option value=\"comItem05\" th:selected=\"${complianceItemCd=='50'}? 'selected'\">금지용어</option>\n"
				// 				+ "\t\t</select>\n"
				// 				+ "\t</td>\n"
				// 				+ "\t<td><input type=\"text\" name=\"score\" style=\"width:95%\" value=\"{{0}}\" readonly=\"readonly\"></td>\n".format(rowData[i].score)
				// 				+ "\t<td><input type=\"text\" name=\"scriptCont\" style=\"width:95%\" value=\"{{0}}\" readonly=\"readonly\"></td>\n".format(rowData[i].scriptCont)
				// 				+ "\t<td>\n"
				// 				+ "\t\t<button id=\"saveSimScriptBtn\" type=\"button\" class=\"btn btn_blue w98\" onclick=\"showPopupRowDetail('sim_script', {{0}})\"><i class=\"far fa-save\"></i>유사문장</button>\n".format(i)
				// 				+ "\t</td>\n"
				// 				+ "\t<td>\n"
				// 				+ "\t\t<button id=\"saveBtn\" type=\"button\" class=\"btn btn_green w98\" onclick=\"saveKeyword()\"><i class=\"far fa-save\"></i>키워드</button>\n"
				// 				+ "\t</td>\n"
				// 				+ "</tr>\n"
				//
				// 		}
				// 		// row 만들어서 추가
				// 		$("#dataTable > tbody").html(rows);
				// 	},
				// 	error: function(error) {}
				// });
			})
		}
	})

}

// 스크립트 정보 채우기
const showUpdatePopup = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();
	const dictionarySelectedName = dictionarySelected.data("name");

	if (!dictionarySelectedId) {
		alert(`스크립트를 먼저 선택하세요`);
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
			alert("스크립트 정보를 가져오는데 실패했습니다. 로그를 확인해주세요.");
		}
	});
};

// 준수항목 데이터 가져오기
function compCdDataSet() {
	var params = new Object();
	
	//카테고리 ID, 스크립트 ID 수정필요
	params.category_id = $('#choice_proj').val();
	params.script_id = $('#choice_model').val();

	if (params.category_id == null || params.script_id == null) return false;
	
	$.ajax({
		url: contextPath + "/autoqaRest/getCompCdInfo",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$('#LoadingImage').show();
			$('#drawArea').show();
		},
        success: function(data) {
        	setTimeout(function() {
				
        		$("#LoadingImage").hide();
        		
				if (data.taskForm != "") {
	        		job ='U';
	
	        		$('#drawArea').html(data.taskForm);
	        		
	        		// 옵션 비활성화 처리하기
	        		$(".optionList").find('input,select').prop('disabled', true);
	        		// predictThreshold는 조정할 수 있도록 disabled 해제
	        		$('input[name="predictThreshold"]').prop('disabled', false);
	
					$('#addBtn').hide();
					$('#saveBtn').show();
					$('#deleteBtn').show();
					$('#cancelBtn').show();
        		}
        		
        	}, 'Timeout...!');
		}
	})
}

// 이벤트리스너 등록 : 생성, 수정, 삭제, 업로드 버튼

// 팝업창 띄우기
const showPopup = function(target) {
	// console.debug(target);
	let $target;
	switch (target) {
		case "create":
			$target = $("#create_pop");
			$target.find("input,select").val("");
			$target.show();
			break;
		case "update":
			showUpdatePopup();
			$target = $("#update_pop");
			break;
		case "download":
			const $categoryId = $("input[type=check][name=script_choice]:checked");
			if (!$categoryId || !$categoryId.val()) {
				alert("사전을 먼저 선택하세요");
				return false;
			}
			if ($categoryId.data("count") === 0) {
				alert("해당 분류의  스크립트가 존재하지 않습니다.\n" +
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
		case "sim_script":
			$target = $("#sim_script_pop");
			break;
		default:
			console.error(`Undefined popup target="${target}"`);
	}
	$target.find("input,select").val("");
	$target.hide();
};

const showPopupRowDetail = function(target, scriptId) {
	let $target;

	var params = new Object();
	params.scriptId = scriptId;
	switch (target) {
		case "sim_script":
			$target = $("#sim_script_pop");
			$.ajax({
				url: contextPath + '/autoqaRest/getQASimScriptList',
				type: "POST",
				cache: false,
				contentType: "application/json",
				dataType: "json",
				data: JSON.stringify(params),
				async: false,
				success: function(data) {
					var rowData = data.resultList
					var rows = ""
					for(var i in rowData) {
						rows += "<tr>\n"
						+ "\t<td>\n"
						+ "\t\t<input type=\"text\" class=\"ml0 w75\" id=\"input_simscript_" + rowData[i].simScriptId + "\" name=\"sim_script\" value=\"" + rowData[i].simScriptCont + "\">\n"
						+ "\t\t<input type=\"button\" class=\"btn btn_sky w65 ml10\" value=\"수정\" onclick=\"updateSimScript(" + rowData[i].simScriptId + ")\" style=\"font-size:inherit;\">\n"
						+ "\t\t<input type=\"button\" class=\"btn btn_sky w65 ml10\" value=\"삭제\" onclick=\"deleteSimScript(" + rowData[i].simScriptId + ")\" style=\"font-size:inherit;\">\n"
						+ "\t</td>\n"
						+ "</tr>\n"
					}

					$("#sim_script_table > tbody").html(rows)
				},
				error: function(error) {
					console.log(error)
				}
			});


			$target.show();
			break;
		default:
			console.error(`Undefined popup target="${target}"`);
	}
};


// 선택된 스크립트 삭제
const deleteScript = function() {
	const scriptSelected = $("input[type=check][name=dict_choice]:checked");
	const scriptSelectedId = scriptSelected.val();
	const scriptSelectedName = scriptSelected.data("name");
	if (!scriptSelectedId) {
		alert(`스크립트을 먼저 선택하세요`);
		return false;
	}

	var params = new Object();
	params.resourceType = 'script';
	params.resourceId = scriptSelectedId;
	$.ajax({
		type: "POST",
		url: contextPath + '/taskRest/chkTaskResouce',
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		success: function(response) {
			if (response.result == "S") {
				if (confirm(`선택한 스크립트 '${scriptSelectedName}' 를 삭제합니다. 삭제된 스크립트는 복구가 불가능 합니다.`)) {
					$.ajax({
						type: "GET",
						url: contextPath + '/dictionaryRest/deleteDictionary?scriptId=' + scriptSelectedId,
						success: function(response) {
							// script 삭제 성공
							alert(response.resultMsg);
							if (response.result == "S") location.reload();
						},
						error: function(jqXHR, textStatus, errorThrown) {
							switch (jqXHR.status) {
								case 403:
									console.error("해당 스크립트의 삭제 권한이 없습니다.");
									alert("해당 스크립트의 삭제 권한이 없습니다.");
									break;
								case 409:
									console.error(jqXHR.responseText);
									alert(`스크립트 삭제 실패. [${jqXHR.responseText}]`);
									break;
								default:
									console.error("Failed to delete Dictionary");
									alert("스크립트 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
							}
						}
					});
				}
			} else {
				alert(response.resultMsg);
			}
		}
	});
};

// 선택된 스크립트 업로드 페이지로 이동
const uploadQAScript = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();
	if (!dictionarySelectedId) {
		alert(`스크립트를 먼저 선택하세요`);
		return false;
	}

	location.href = contextPath + '/dictionary/upload?dictionaryId=' + dictionarySelectedId;
};

// 스크립트 추가
const createDictionary = function() {
	let $form_script = $("#create_pop form");

	if (!isFormValid($form_script.serializeArray())) {
		return false;
	}

	$.ajax({
		type: "POST",
		url: `${contextPath}/autoqaRest/insertQAScript`,
		data: $form_script.serialize(), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
			console.debug(`AutoQAScript to create: ${$form_script.serialize()}`);
			document.querySelector("#create_pop button[type=submit]").disabled = true;
		},
		success: function(response) {
			// 스크립트 생성 성공
			alert(response.autoQAScript + " - " + response.resultMsg);

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
					console.error("스크립트 추가 권한이 없습니다.");
					alert("스크립트 추가 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`스크립트 추가 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to create AutoQAScript");
					alert("스크립트 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			document.querySelector("#create_pop button[type=submit]").disabled = false;
		}
	});

	return false;
};

// 스크립트 수정
const updateDictionary = function() {
	const dictionarySelected = $("input[type=radio][name=dict_choice]:checked");
	const dictionarySelectedId = dictionarySelected.val();

	if (!dictionarySelectedId) {
		alert(`스크립트를 먼저 선택하세요`);
		return false;
	}

	let $form_script = $("#update_pop form");

	if (!isFormValid($form_script.serializeArray())) {
		return false;
	}

	$.ajax({
		type: "POST",
		url: `${contextPath}/autoqaRest/updateQAScript`,
		data: $form_script.serialize(), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
			console.debug(`AutoQAScript to update: ${$form_script.serialize()}`);
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
					console.error("해당 스크립트의 수정 권한이 없습니다.");
					alert("해당 스크립트의 수정 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`스크립트 수정 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update AutoQAScript");
					alert("스크립트 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			document.querySelector("#update_pop button[type=submit]").disabled = false;
		}
	});

	return false;
};

/**
 * 다운로드 스크립트
 * @returns {boolean}
 */
const downloadQAScript = function requestExportEntriesAsAFileAndDownload() {
	const $scriptSelected = $("input[type=check][name=script_choice]:checked");
	const scriptSelectedId = $scriptSelected.val();
	if (!scriptSelectedId) {
		alert(`스크립트를 먼저 선택하세요`);
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
					console.error("해당 스크립트의 다운로드 권한이 없습니다.");
					alert("해당 사전의 다운로드 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);  // TODO undefined 이슈
					alert(`스크립트 다운로드 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update Dictionary");
					alert("스크립트 다운로드 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
		},
		complete: function(jqXHR, textStatus) {
			// todo 화면 언블락
			// alert("스크립트 다운로드 요청 완료");
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
				alert("점('.')이나 공백(' ')으로 스크립트 파일명을 시작할 수 없습니다.");
				return false;
			}
			// 윈도우 파일명 금지 문자 체크
			if (!regexForbiddenChars.test(fieldValue)) {
				alert("\\ / : * ? \" < > | 특수기호는 스크립트 파일명을 사용할 수 없습니다.");
				return false;
			}
			if (regexForbiddenFileNames.test(fieldValue)) {
				alert("사용할 수 없는 스크립트 파일명 입니다.");
				return false;
			}
			if (fieldValue.replace(/\./g, "").trim().length === 0) {  // 점(.)과 공백 제거
				alert("스크립트 파일명은 공백과 점(.)만으로는 설정할 수 없습니다.");
				return false;
			}
		}
	}

	return true;
};

const updateSimScript = function(simScriptId) {
	var params = new Object()
	params.simScriptId = simScriptId;
	params.simScriptCont = $("#input_simscript_" + simScriptId).val();

	$.ajax({
		type: "POST",
		url: `${contextPath}/autoqaRest/updateQASimScript`,
		data: JSON.stringify(params), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
		},
		success: function(data) {
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
					console.error("해당 유사 스크립트의 수정 권한이 없습니다.");
					alert("해당 유사 스크립트의 수정 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`유사 스크립트 수정 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update AutoQAScript");
					alert("유사 스크립트 추가 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			// document.querySelector("#update_pop button[type=submit]").disabled = false;
		}
	});

}

const deleteSimScript = function(simScriptId) {
	var params = new Object()
	params.simScriptId = simScriptId;

	$.ajax({
		type: "DELETE",
		url: `${contextPath}/autoqaRest/deleteQASimScript`,
		data: JSON.stringify(params), //default contentType: 'application/x-www-form-urlencoded'
		beforeSend: function() {
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
					console.error("해당 유사 스크립트의 삭제 권한이 없습니다.");
					alert("해당 유사 스크립트의 삭제 권한이 없습니다.");
					break;
				case 409:
					console.error(jqXHR.responseText);
					alert(`유사 스크립트 삭제 실패. [${jqXHR.responseText}]`);
					break;
				default:
					console.error("Failed to update AutoQAScript");
					alert("유사 스크립트 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
			}
			// document.querySelector("#update_pop button[type=submit]").disabled = false;
		}
	});

}