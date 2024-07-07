// 작업구분
var job = '';

$(document).ready(function() {
	
	initForm();

	if ($('#projectId').val() != null && $('#projectId').val() != '') {
		$('#choice_proj').val($('#projectId').val()).prop('selected', true);
	} else {
		return false;
	}
	
	if ($('#taskId').val() != null && $('#taskId').val() != '') {
		$('#choice_model').val($('#taskId').val()).prop('selected', true);
	} else {
		return false;
	}

	taskDataSet();
}); 

// 양식 초기화
function initForm() {
	job = '';
	
	$('#drawArea').hide();
	$('#LoadingImage').hide();
	$('#addBtn').hide();
	$('#saveBtn').hide();
	$('#deleteBtn').hide();
	$('#cancelBtn').hide();
}

function fnSelectProject() {
	$('#proj_desc').text($('#choice_proj option:selected').attr('title'))
	
	// 입력 or 수정 중에 값 변경 시, 등록양식 초기화
	if (job != '') {
		if (!confirm("프로젝트를 변경할 경우, 현재 작업 중인 내용이 초기화됩니다.\n변경하시겠습니까?")) {
			return false;
		} else {
			initForm();
		}
	}
}

function fnGetModel() {

	if (job != '') {
		if (!confirm("서비스 구분을 변경할 경우, 현재 작업 중인 내용이 초기화됩니다.\n변경하시겠습니까?")) {
			return false;
		} else {
			initForm();
		}
	}

	if ($('#choice_proj').val() == null) {
		alert("프로젝트를 선택해주세요.");
		$('#choice_task').val(" ").prop(':selected', true);
		return false;
	}
	
	var params = new Object();
	
	params.projectId = $('#choice_proj').val();
	params.taskType = $('#choice_task').val();
	
	// 서비스모델 초기화
	$('#choice_model').empty();
	$('#choice_model').append('<option selected disabled value=" ">서비스 모델을 선택하세요</option>');
	
	$.ajax({
		url: contextPath + "/taskRest/getModelList",
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(data) {
        	setTimeout(function() {
				
				// 등록된 서비스 리스트 출력
				for (var i = 0; i < data.modelList.length; i++) {
					$('#choice_model').append('<option value="' + data.modelList[i].taskId + '">' + data.modelList[i].modelName + '</option>');
				}
				
				// 서비스는 3개까지만 추가 가능
				if (data.modelList.length < 3) $('#addBtn').show();
				
        	}, 'Timeout...!');
		}
	});
}

// 추가작업을 위한 입력양식 가져오기
function addTask() {
	
	var params = new Object();
	
	params.taskType = $('#choice_task').val();
	
	
	$.ajax({
		url: contextPath + "/taskRest/getTaskForm",
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
        		job ='I';

        		$("#LoadingImage").hide();
        		$('#drawArea').html(data.taskForm);
        		
        		// 옵션 비활성화 처리하기
        		$(".optionList").find('input,select').prop('disabled', true);

        		// predictThreshold는 조정할 수 있도록 disabled 해제
        		$('input[name="predictThreshold"').prop('disabled', false);
        		
				$('#addBtn').hide();
				$('#saveBtn').show();
				$('#cancelBtn').show();
        		
        	}, 'Timeout...!');
		}
	})
}

// 작업 취소
function cancelTask() {
	job ='';
	
	$('#drawArea').hide();
	$('#addBtn').show();
	$('#saveBtn').hide();
	$('#deleteBtn').hide();
	$('#cancelBtn').hide();
	
	$('#choice_model').val("");
}

// 서비스 데이터 가져오기
function taskDataSet() {
	var params = new Object();
	
	params.projectId = $('#choice_proj').val();
	params.taskId = $('#choice_model').val();

	if (params.projectId == null || params.taskId == null) return false;
	
	$.ajax({
		url: contextPath + "/taskRest/getTaskInfo",
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
	        		$('input[name="predictThreshold"').prop('disabled', false);
	
					$('#addBtn').hide();
					$('#saveBtn').show();
					$('#deleteBtn').show();
					$('#cancelBtn').show();
        		}
        		
        	}, 'Timeout...!');
		}
	})
}

// 서비스 추가 및 변경정보 저장
function saveTask() {
	
	// 입력값 체크
	if (!isFormValid($('#taskForm').serializeArray())) return false;
	
	var params = setParameters();
	
	$.ajax({
		url: `${contextPath}/taskRest/saveTask`,
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
        success: function(response) {
			alert(response.resultMsg);
			if (response.result == "S") {
				location.href = contextPath + "/project";
			}
		},
		error: function (jqXHR, textStatus, errorThrown) {
            console.error(`Failed to request save task. cause=${jqXHR.responseJSON}`);
            alert("서비스 저장에 실패했습니다. 로그를 확인해주세요");
        }
	})
}

// 서비스 삭제
function deleteTask() {

	if(!confirm("해당 서비스를 삭제하시겠습니까?")) {
		return false;
	}

	var params = new Object();
	
	params.projectId = $('#choice_proj').val();
	params.taskId = $('#choice_model').val();
	
	$.ajax({
		url: contextPath + "/taskRest/deleteTask",
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
        success: function(response) {
			alert(response.resultMsg);
			if (response.result == "S") {
				location.href = contextPath + "/project";
			}
		},
		error: function (jqXHR, textStatus, errorThrown) {
            console.error(`Failed to request save task. cause=${jqXHR.responseJSON}`);
            alert("서비스 삭제 실패했습니다. 로그를 확인해주세요");
        }
	})
}

// 입력값 체크
function isFormValid(serializeArray) {
    const regexStartingDots = /^\./; // cannot start with dot (.)
    const regexForbiddenChars = /^[^\\/:\*\?"<>\|]+$/; // forbidden characters \ / : * ? " < > |
    const regexForbiddenFileNames = /^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i; // forbidden file names
    
	for (let serialize of serializeArray) {
		
		var fieldName = serialize.name.toString();
		var fieldValue = serialize.value.toString();

		// 서비스명 설정
	    if (fieldName == "taskModelName") {
	        if (fieldValue.startsWith(".") || fieldValue.startsWith(" ")) {
	            alert("서비스 모델명은 점('.')이나 공백(' ')으로 시작할 수 없습니다.");
	            return false;
	        }
	        // 윈도우 파일명 금지 문자 체크
	        if (!regexForbiddenChars.test(fieldValue)) {
	            alert("\\ / : * ? \" < > | 특수기호는 서비스 모델명으로 사용할 수 없습니다.");
	            return false;
	        }
	        if (regexForbiddenFileNames.test(fieldValue)) {
	            alert("사용할 수 없는 서비스 모델명입니다.");
	            return false;
	        }
	        if (fieldValue.replace(/\./g, "").trim().length === 0) {  // 점(.)과 공백 제거
	            alert("서비스 모델명은 공백과 점(.)만으로는 설정할 수 없습니다.");
	            return false;
	        }
	    }
	}
	
    return true;
}

// 파라메터 생성
function setParameters() {
	var params = new Object();
	
	params.job = job;
	params.projectId = $('#choice_proj').val();
	params.taskType = $('#choice_task').val();
	params.taskId = $('#choice_model').val()==null?0:$('#choice_model').val();
	params.taskName = $('input[name="taskModelName"]').val();
	params.collections = $('input[name="collections"]:checked').val();
	
	if (params.taskType == "AUTO_CLASSIFICATION" || params.taskType == "EMOTION_ANALYZE") {					// 자동분류, 감성분석 -> 아래 파라미터 추가
		
		// 학습 설정값
		var trainOptions = new Object();
		trainOptions.algorithm = $('select[name="algorithm"]').val();
	
		var feature = new Object();
		feature.maxDF = $('input[name="maxDF"]').val();
		feature.minDF = $('input[name="minDF"]').val();
		feature.vocabLimit = $('input[name="vocabLimit"]').val();
		feature.printVocab = $('select[name="printVocab"]').val();
		feature.printFeature = $('select[name="printFeature"]').val();
		
		var parameter = new Object();
		parameter.nthread = $('input[name="nthread"]').val();
		parameter.round = $('input[name="round"]').val();
		parameter.max_depth = $('input[name="max_depth"]').val();
		
		trainOptions.feature = feature;
		trainOptions.parameter = parameter;
		params.trainOptions = trainOptions;
		
		// 분석 설정값
		var analyzeOptions = new Object();
		
		var predict = new Object();
		predict.predictFieldName = $('input[name="predictFieldName"]').val();
		predict.defaultLabel = $('input[name="defaultLabel"]').val();
		predict.thresholdOption = $('select[name="thresholdOption"]').val();
		predict.predictThreshold = $('input[name="predictThreshold"]').val();
		predict.thresholdMultiplier = $('input[name="thresholdMultiplier"]').val();
		
		var confidence = new Object();
		confidence.confidenceFieldName = $('input[name="confidenceFieldName"]').val();
		confidence.numConfidence = $('input[name="numConfidence"]').val();

		analyzeOptions.predict = predict;
		analyzeOptions.confidence = confidence;
		params.analyzeOptions = analyzeOptions;
		
	} else if (params.taskType == "KEYWORD_EXTRACTION" || params.taskType == "RELATED_EXTRACTION") {		// 키워드, 연관어추출 -> 아래 파라미터 추가
		params.whiteSharedEnabled = $('input[name="whiteSharedEnabled"]:checked').val()==null?'N':$('input[name="whiteSharedEnabled"]:checked').val();
		params.whiteDictionary = $('select[name="whiteDictionary"]').val();
		params.blackSharedEnabled = $('input[name="blackSharedEnabled"]:checked').val()==null?'N':$('input[name="blackSharedEnabled"]:checked').val();
		params.blackDictionary = $('select[name="blackDictionary"]').val();
	} else if (params.taskType == "EMOTION_PREPROECESS" || params.taskType == "SUMMARY_PREPROECESS") {		// 전처리 -> 아래 파라미터 추가
		
		// 적용 패턴 설정
		params.blackPattern = $('select[name="blackPattern"]').val();
		params.blackWord = $('select[name="blackWord"]').val();
		params.splitSentWord = $('select[name="splitSentWord"]').val();
		
		// 전처리 설정정보
		var preprocessOption = new Object();
		preprocessOption.apply_black_words_removal = $('select[name="apply_black_words_removal"]').val();
		preprocessOption.apply_pattern_removal = $('select[name="apply_pattern_removal"]').val();
		preprocessOption.apply_sentence_separation = $('select[name="apply_sentence_separation"]').val();
		preprocessOption.apply_spacing_correction = $('select[name="apply_spacing_correction"]').val();
		preprocessOption.apply_speaker_combination = $('select[name="apply_speaker_combination"]').val();
		preprocessOption.apply_split_sentence_word = $('select[name="apply_split_sentence_word"]').val();
		
		params.preprocessOption = preprocessOption;
	} else if (params.taskType == "STRING_MATCHER") {		// 문자열 매칭 -> 아래 파라미터 추가
		// 적용 패턴 설정
		params.groupPattern = $('select[name="groupPattern"]').val();
	}
	
	return params;
}