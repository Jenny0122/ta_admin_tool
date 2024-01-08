var taskType = "";
var collection = "";
var hostIp = "";
var port = "";
var threshHoldFlag = "";

$(document).ready(function() {
	$('#threshold').hide();
	$('#preprocess').hide();
	$('#preprocessingGuideText').hide();
	$('#summary').hide();
})

// 시뮬레이션 목록 가져오기
function fnDoModelList() {
	var params = new Object();
		
	params.projectId = $('#project_list').val();
	
	$.ajax({
		url: `${contextPath}/simulationRest/getSimulationList`,
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
        		$("#project_desc").remove();
        		$("#model_list").remove();
        		
        		$("#project_name").after(data.dataTable);	
        		
        	}, 'Timeout...!');
		}
	});
}

// 서비스 모델 선택에 따른 시뮬레이션 모듈 로드
function fnGetTestModule(){
	
	var params = new Object();
	
	params.projectId = $('#project_list').val();
	params.taskId = $('#taskId').val();
	
	$.ajax({
		url: `${contextPath}/simulationRest/getTestModule`,
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$("#listArea").html("<div id='LoadingImage' class='loading-LoadingImage'><div style='margin-left:50%'><img th:src='@{/img/loading.gif}'></div></div>");
		},
        success: function(response) {
        	setTimeout(function() {
        		$("#LoadingImage").remove();
        		
        		$("#model_desc").html(response.taskTypeName);

				// 임계값 옵션 표시 설정
				threshHoldFlag = response.taskTypeFlag
        		if (threshHoldFlag == "C") {
					$('#threshold').show();
					$('#preprocess').hide();
					$('#preprocessingGuideText').hide();
					$('#summary').hide();
				} else if (threshHoldFlag == "P") {
					$('#threshold').hide();
					$('#preprocess').show();
					$('#preprocessingGuideText').show();
					$('#summary').hide();
				} else if (threshHoldFlag == "S") {
					$('#threshold').hide();
					$('#preprocess').hide();
					$('#preprocessingGuideText').hide();
					$('#summary').show();
				} else {
					$('#threshold').hide();
					$('#preprocess').hide();
					$('#preprocessingGuideText').hide();
					$('#summary').hide();
				}

				collection = response.collection;
        		taskType = response.taskType;
				hostIp = response.serverIp;
				port = response.serverPort;
        		
        	}, 'Timeout...!');
		}
	});
}

// 분석요청
function callSimulation(){
	
	// 프로젝트 선택 검사
	if ($('#project_list').val() == null || $('#project_list').val() == ''){
		alert("프로젝트를 선택해주세요.");
		return false;
	}
	
	// 서비스 모델 선택 검사
	if ($('#taskId').val() == null || $('#taskId').val() == ''){
		alert("서비스를 선택해주세요.");
		return false;
	}
	
	// 분석 텍스트 검사
	if ($('#simulationText').val() == null || $('#simulationText').val() == ''){
		alert("분석할 텍스트를 입력해주세요.");
		return false;
	}
	
	var params = new Object();
	
	params.collection = collection;
	params.taskType = taskType;
	params.hostIp = hostIp;
	params.port = port;
	params.simulationText = $('#simulationText').val();
	
	if (threshHoldFlag == "C" && $('#thresholdOption').val() != "") {
		params.thresholdOption = $('#thresholdOption').val();
		
		if (params.thresholdOption == "hard") {
			params.predictThreshold = $('#thresholdValue').val();
			params.thresholdMultiplier = "";
		} else {
			params.predictThreshold = "";
			params.thresholdMultiplier = $('#thresholdValue').val();
		}
		
		params.apply_black_words_removal = null;
		params.apply_pattern_removal = null;
		params.apply_sentence_separation = null;
		params.apply_spacing_correction = null;
		params.apply_speaker_combination = null;
		params.apply_split_sentence_word = null;
		
		params.preprocessOption = null
	} else if (threshHoldFlag == "P") {
		params.thresholdOption = null;
		params.predictThreshold = null;
		params.thresholdMultiplier = null;
		
		params.apply_black_words_removal = $('#apply_black_words_removal').val();
		params.apply_pattern_removal = $('#apply_pattern_removal').val();
		params.apply_sentence_separation = $('#apply_sentence_separation').val();
		params.apply_spacing_correction = $('#apply_spacing_correction').val();
		params.apply_speaker_combination = $('#apply_speaker_combination').val();
		params.apply_split_sentence_word = $('#apply_split_sentence_word').val();
		
		params.preprocessOption = null;
	} else if (threshHoldFlag == "S") {
		params.thresholdOption = null;
		params.predictThreshold = null;
		params.thresholdMultiplier = null;
		
		params.apply_black_words_removal = null;
		params.apply_pattern_removal = null;
		params.apply_sentence_separation = null;
		params.apply_spacing_correction = null;
		params.apply_speaker_combination = null;
		params.apply_split_sentence_word = null;
		
		params.preprocessOption = $('#preprocessOption').val();
	} else {
		params.thresholdOption = null;
		params.predictThreshold = null;
		params.thresholdMultiplier = null;
		
		params.apply_black_words_removal = null;
		params.apply_pattern_removal = null;
		params.apply_sentence_separation = null;
		params.apply_spacing_correction = null;
		params.apply_speaker_combination = null;
		params.apply_split_sentence_word = null;
		
		params.preprocessOption = null
	}
	
	$.ajax({
		url: `${contextPath}/simulationRest/callSimulation`,
		type:'POST',
		cache: false,
		contentType : "application/json",
		dataType: "json",
		data: JSON.stringify(params),
		async: false,
		beforeSend: function(){
			$("#resultArea").html("");
		},
        success: function(response) {
        	setTimeout(function() {
        		$("#resultArea").html(response.apiResultJson);
        	}, 'Timeout...!');
		}
	});
}

