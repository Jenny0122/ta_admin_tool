package kr.co.wisenut.textminer.common;

public class TextMinerConstants {
	
	// 공통
	public final static String COMMON_BLANK = "";
	
	// 사용자 권한
	public final static String AUTHORITY_ADMIN = "ADMIN";
	public final static String AUTHORITY_USER = "USER";
	
	// 서비스 구분
	public final static String TASK_TYPE_AUTO_CLASSIFICATION = "AUTO_CLASSIFICATION";
	public final static String TASK_TYPE_NAME_AUTO_CLASSIFICATION = "자동 분류";
	public final static String TASK_TYPE_DOCUMENT_SUMMARY = "DOCUMENT_SUMMARY";
	public final static String TASK_TYPE_NAME_DOCUMENT_SUMMARY = "문서 요약";
	public final static String TASK_TYPE_EMOTION_ANALYZE = "EMOTION_ANALYZE";
	public final static String TASK_TYPE_NAME_EMOTION_ANALYZE = "감성 분석";
	public final static String TASK_TYPE_KEYWORD_EXTRACTION = "KEYWORD_EXTRACTION";
	public final static String TASK_TYPE_NAME_KEYWORD_EXTRACTION = "키워드 추출";
	public final static String TASK_TYPE_RELATED_EXTRACTION = "RELATED_EXTRACTION";
	public final static String TASK_TYPE_NAME_RELATED_EXTRACTION = "연관어 추출";
	
	public final static String TASK_TYPE_EMOTION_PREPROECESS = "EMOTION_PREPROECESS";
	public final static String TASK_TYPE_NAME_EMOTION_PREPROECESS = "전처리(감성)";
	public final static String TASK_TYPE_SUMMARY_PREPROECESS = "SUMMARY_PREPROECESS";
	public final static String TASK_TYPE_NAME_SUMMARY_PREPROECESS = "전처리(요약)";
	public final static String TASK_TYPE_STRING_MATCHER = "STRING_MATCHER";
	public final static String TASK_TYPE_NAME_STRING_MATCHER = "문자열 매칭";
	public final static String TASK_TYPE_AUTO_QA = "AUTO_QA";
	public final static String TASK_TYPE_NAME_AUTO_QA = "AUTO QA";
	
	// 컬렉션 구분
	public final static String COLLECTION_TYPE_CLASSIFICATION = "CLASSIFICATION";
	public final static String COLLECTION_TYPE_NAME_CLASSIFICATION = "분류";
	public final static String COLLECTION_TYPE_EMOTION = "EMOTION";
	public final static String COLLECTION_TYPE_NAME_EMOTION = "감성";
	
	// 컬렉션 작업구분
	public final static String COLLECTION_JOB_ANALYSIS = "ANALYSIS";
	public final static String COLLECTION_JOB_NAME_ANALYSIS = "분석";
	public final static String COLLECTION_JOB_TRAINING = "TRAINING";
	public final static String COLLECTION_JOB_NAME_TRAINING = "학습";
	
	// 사전 구분
	public final static String DICTIONARY_TYPE_WHITE = "WHITE";
	public final static String DICTIONARY_TYPE_NAME_WHITE = "수용어 사전";
	public final static String DICTIONARY_TYPE_BLACK = "BLACK";
	public final static String DICTIONARY_TYPE_NAME_BLACK = "불용어 사전";
	
	// 전처리 패턴 구분
	public final static String DICTIONARY_TYPE_BLACK_PATTERN = "BLACK_PATTERN";
	public final static String DICTIONARY_TYPE_NAME_BLACK_PATTERN = "불용 패턴";
	public final static String DICTIONARY_TYPE_BLACK_WORD = "BLACK_WORD";
	public final static String DICTIONARY_TYPE_NAME_BLACK_WORD = "불용 어절";
	public final static String DICTIONARY_TYPE_SPLIT_SENT_WORD = "SPLIT_SENT_WORD";
	public final static String DICTIONARY_TYPE_NAME_SPLIT_SENT_WORD = "문장 분리 어절";
	
	// 문자열 매칭 패턴
	public final static String DICTIONARY_TYPE_GROUP_PATTERN = "GROUP_PATTERN";
	public final static String DICTIONARY_TYPE_NAME_GROUP_PATTERN = "매칭 패턴";
	
	// AUTO QA 지식데이터
	public final static String DICTIONARY_TYPE_AUTO_QA_DATA = "AUTO_QA_DATA";
	public final static String DICTIONARY_TYPE_NAME_AUTO_QA_DATA = "AUTO QA 지식";
	
	// 파일 업로드 대상
	public final static String PROGRESS_TYPE_COLLECTION = "COLLECTION";
	public final static String PROGRESS_TYPE_DICTIONARY = "DICTIONARY";
	public final static String PROGRESS_TYPE_AUTOQA = "AUTOQA";
	
	// 파일 업로드 에러 제어방식
	public final static String PROGRESS_ERROR_SKIP = "SKIP";
	public final static String PROGRESS_ERROR_STOP = "STOP";
	
	// 파일 업로드 결과
	public final static String PROGRESS_STATUS_WAITING = "WAITING";
	public final static String PROGRESS_STATUS_MSG_WAITING = "대기중";
	public final static String PROGRESS_STATUS_IN_PROGRESS = "IN_PROGRESS";
	public final static String PROGRESS_STATUS_MSG_IN_PROGRESS = "진행중";
	public final static String PROGRESS_STATUS_SUCCESS = "SUCCESS";
	public final static String PROGRESS_STATUS_MSG_SUCCESS = "전체성공";
	public final static String PROGRESS_STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
	public final static String PROGRESS_STATUS_MSG_PARTIAL_SUCCESS = "부분성공";
	public final static String PROGRESS_STATUS_FAILURE = "FAILURE";
	public final static String PROGRESS_STATUS_MSG_FAILURE = "실패";
	// 상태코드
	public final static String CLASSIFIER_STATUS_READY = "READY";
	public final static String CLASSIFIER_STATUS_TRAINING = "TRAINING";
	public final static String CLASSIFIER_STATUS_MODEL_SAVING = "MODEL_SAVING";
	public final static String CLASSIFIER_STATUS_DATA_LOADING = "DATA_LOADING";
	public final static String CLASSIFIER_STATUS_MORPHOLOGICAL_ANALYSIS = "MORPHOLOGICAL_ANALYSIS";
	
	// 서버(모듈) 구분
	public final static String SERVER_TYPE_TEST = "TEST";
	public final static String SERVER_TYPE_NAME_TEST = "검증";
	public final static String SERVER_TYPE_PROD = "PROD";
	public final static String SERVER_TYPE_NAME_PROD = "서비스";
	
	// 시뮬레이션 실행구분
	public final static String EXECUTE_SIMULATION_TYPE_SIMUL = "simul";
	public final static String EXECUTE_SIMULATION_TYPE_NAME_SIMUL = "시뮬레이션";
	public final static String EXECUTE_SIMULATION_TYPE_BATCH = "batch";
	public final static String EXECUTE_SIMULATION_TYPE_NAME_BATCH = "재분석배치";
	
	// 활동 구분
	// 1) 로그인 / 로그아웃
	public final static String ACTION_HISTORY_TYPE_USER_LOGIN = "USER_LOGIN";
	public final static String ACTION_HISTORY_TYPE_USER_LOGOUT = "USER_LOGOUT";
	
	// 2) 사용자 등록/수정/삭제
	public final static String ACTION_HISTORY_TYPE_USER_INSERT = "USER_INSERT";
	public final static String ACTION_HISTORY_TYPE_USER_UPDATE = "USER_UPDATE";
	public final static String ACTION_HISTORY_TYPE_USER_DELETE = "USER_DELETE";

	// 3) 프로젝트 등록/수정/삭제
	public final static String ACTION_HISTORY_TYPE_PROJECT_INSERT = "PROJECT_INSERT";
	public final static String ACTION_HISTORY_TYPE_PROJECT_UPDATE = "PROJECT_UPDATE";
	public final static String ACTION_HISTORY_TYPE_PROJECT_DELETE = "PROJECT_DELETE";

	// 4) 서비스(테스크) 등록/수정/삭제
	public final static String ACTION_HISTORY_TYPE_SERVICE_INSERT = "SERVICE_INSERT";
	public final static String ACTION_HISTORY_TYPE_SERVICE_UPDATE = "SERVICE_UPDATE";
	public final static String ACTION_HISTORY_TYPE_SERVICE_DELETE = "SERVICE_DELETE";

	// 5) 컬렉션 등록/수정/삭제
	public final static String ACTION_HISTORY_TYPE_COLLECTION_INSERT = "COLLECTION_INSERT";
	public final static String ACTION_HISTORY_TYPE_COLLECTION_UPDATE = "COLLECTION_UPDATE";
	public final static String ACTION_HISTORY_TYPE_COLLECTION_DELETE = "COLLECTION_DELETE";

	// 6) 사전 등록/수정/삭제
	public final static String ACTION_HISTORY_TYPE_DICTIONARY_INSERT = "DICTIONARY_INSERT";
	public final static String ACTION_HISTORY_TYPE_DICTIONARY_UPDATE = "DICTIONARY_UPDATE";
	public final static String ACTION_HISTORY_TYPE_DICTIONARY_DELETE = "DICTIONARY_DELETE";

	// 7) 컬렉션 문서 / 사전 단어 업로드	
	public final static String ACTION_HISTORY_TYPE_DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
	public final static String ACTION_HISTORY_TYPE_ENTRY_UPLOAD = "ENTRY_UPLOAD";
	
	// 8) 엔트리 등록 / 수정 / 삭제
	public final static String ACTION_HISTORY_TYPE_ENTRY_INSERT = "ENTRY_INSERT";
	public final static String ACTION_HISTORY_TYPE_ENTRY_UPDATE = "ENTRY_UPDATE";
	public final static String ACTION_HISTORY_TYPE_ENTRY_DELETE = "ENTRY_DELETE";
	
	// 9) 문서 수정
	public final static String ACTION_HISTORY_TYPE_DOCUMENT_UPDATE = "DOCUMENT_UPDATE";
	
	// 10) 모델 학습 / 분석요청
	public final static String ACTION_HISTORY_TYPE_TRAIN_MODEL = "TRAIN_MODEL";
	public final static String ACTION_HISTORY_TYPE_ANALYZE_MODEL = "ANALYZE_MODEL";
	
	// 11) 모듈 등록 / 수정 / 삭제 / 재시작 / 배포
	public final static String ACTION_HISTORY_TYPE_MODULE_INSERT = "MODULE_INSERT";
	public final static String ACTION_HISTORY_TYPE_MODULE_UPDATE = "MODULE_UPDATE";
	public final static String ACTION_HISTORY_TYPE_MODULE_DELETE = "MODULE_DELETE";
	public final static String ACTION_HISTORY_TYPE_MODULE_REBOOT = "MODULE_REBOOT";
	public final static String ACTION_HISTORY_TYPE_MODULE_DEPLOY = "MODULE_DEPLOY";
	
	// 12) Batch 실행
	public final static String ACTION_HISTORY_TYPE_SYSTEM_BATCH = "SYSTEM_BATCH";
	
	// 12-1) Batch 실행결과
	public final static String SYSTEM_BATCH_STATUS_SUCCESS = "SUCCESS";
	public final static String SYSTEM_BATCH_STATUS_FAIL = "FAIL";
	
}
