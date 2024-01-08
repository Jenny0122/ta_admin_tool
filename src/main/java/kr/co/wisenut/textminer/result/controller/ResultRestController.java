package kr.co.wisenut.textminer.result.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.result.service.ResultService;

@RestController
@RequestMapping("/resultRest")
public class ResultRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ResultService resultService;
	
	// 분석결과 데이터 체크
	@PostMapping(value = "/checkAnalyzeResult")
	public Map<String, Object> checkAnalyzeResult (@RequestBody Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		JsonArray resultArray = resultService.getResultList(paramMap);
		
		if (resultArray.size() > 0) {
			resultMap.put("result", "Y");
			resultMap.put("resultMsg", "엑셀 다운로드를 시작합니다.");
		} else {
			resultMap.put("result", "N");
			resultMap.put("resultMsg", "분석된 모델이 없습니다.");
		}
		
		return resultMap;
	}
	
	// 분석결과 엑셀 다운로드
	@RequestMapping(value = "/excelDownload", method=RequestMethod.POST, produces="application/x-www-form-urlencoded;charset=UTF-8")
	public void excelDownload( @RequestParam Map<String, Object> paramMap
							 , HttpServletRequest request
							 , HttpServletResponse response) throws IOException {

		// 결과 데이터 조회하기
		JsonArray resultArray = resultService.getResultList(paramMap);
		JsonObject resultObject = null;
		
		// Excel WorkBook (.xlsx)
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("분석결과");
		Row row = null;
		Cell cell = null;
		int rowNum = 2;		// 데이터 표시 시작지점
		
		// Excel Style 만들기
		// 1) Header (테두리, 배경 회색, 데이터 가운데 정렬 및 볼드)
		CellStyle headerStyle = wb.createCellStyle();
		// 테두리
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		// 배경
		headerStyle.setFillForegroundColor(HSSFColorPredefined.GREY_40_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// 정렬
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		// 볼드
		Font bold = wb.createFont();
		bold.setBold(true);
		headerStyle.setFont(bold);
		
		// 2) Body (테두리)
		CellStyle bodyStyle = wb.createCellStyle();
		// 테두리
		bodyStyle.setBorderTop(BorderStyle.THIN);
		bodyStyle.setBorderBottom(BorderStyle.THIN);
		bodyStyle.setBorderLeft(BorderStyle.THIN);
		bodyStyle.setBorderRight(BorderStyle.THIN);
		
		// 엑셀 헤더
		List<String> excelHeader = null;
		
		// 엑셀 데이터 생성
		for (int i = 0; i < resultArray.size(); i++) {
			resultObject = resultArray.get(i).getAsJsonObject();
			
			// 엑셀 헤더 생성 & 추가
			if (i == 0) {
				row = sheet.createRow(rowNum++);
				excelHeader = resultService.createExcelHeader(resultObject, paramMap.get("taskType").toString());
				
				for (int j = 0; j < excelHeader.size(); j++) {
					cell = row.createCell(j);
					cell.setCellStyle(headerStyle);
					cell.setCellValue(excelHeader.get(j));
				}
			}

			// 엑셀 데이터 row 추가
			row = sheet.createRow(rowNum++);
			for (int j = 0; j < excelHeader.size(); j++) {
				cell = row.createCell(j);
				cell.setCellStyle(bodyStyle);
				
				if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
				 || paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
					// 자동분류 : 3 / 감성분석 : 2
					int dataLength = 0;
					if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
						dataLength = 3;
					} else {
						dataLength = 2;
					}
					
					// LABEL_, CONFIDENCE_, SCORE_ 데이터 추가
					if (excelHeader.get(j).indexOf("LABEL_") > -1 || excelHeader.get(j).indexOf("CONFIDENCE_") > -1 || excelHeader.get(j).indexOf("SCORE_") > -1) {
						for (int k = 0; k < dataLength; k++) {
							if (excelHeader.get(j).equals("LABEL_" + ( k + 1 ))) {
								String [] confidences = resultObject.get("CONFIDENCE").getAsString().split("\\^");
								cell.setCellValue(confidences.length > 1 ? confidences[k].split(":")[0] : "");
							} else if (excelHeader.get(j).equals("CONFIDENCE_" + ( k + 1 ))) {
								String [] confidences = resultObject.get("CONFIDENCE").getAsString().split("\\^");
								cell.setCellValue(confidences.length > 1 ? confidences[k].split(":")[1] : "");
							} else if (excelHeader.get(j).equals("SCORE_" + ( k + 1 ))) {
								String [] scores = resultObject.get("SCORE").getAsString().split("\\^");
								cell.setCellValue(scores.length > 1 ? scores[k].split(":")[1] : "");
							}
						}
					} else {
						cell.setCellValue(resultObject.get(excelHeader.get(j)).getAsString());
					}
				} else if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION)) {
					
					// 키워드 분석결과 출력
					if (excelHeader.get(j).indexOf("word_") > -1 
					 || excelHeader.get(j).indexOf("score_") > -1 
					 || excelHeader.get(j).indexOf("is_white_") > -1 
					 || excelHeader.get(j).indexOf("scaled_score_") > -1 
					 || excelHeader.get(j).indexOf("count_") > -1 
					 || excelHeader.get(j).indexOf("synonyms_") > -1 
					 || excelHeader.get(j).indexOf("tag") > -1) {
						JsonArray textList = resultObject.get("result").getAsJsonArray();
						JsonObject tmpObj = null;
						
						for (int k = 0; k < textList.size(); k++) {
							tmpObj = textList.get(k).getAsJsonObject();
							
							if (excelHeader.get(j).equals("word_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("word").getAsString());
							} else if (excelHeader.get(j).equals("score_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("score").getAsString());
							} else if (excelHeader.get(j).equals("is_white_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("is_white").getAsString());
							} else if (excelHeader.get(j).equals("scaled_score_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("scaled_score").getAsString());
							} else if (excelHeader.get(j).equals("count_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("count").getAsString());
							} else if (excelHeader.get(j).equals("synonyms_" + ( k + 1 ))) {
								if (tmpObj.get("synonyms").getAsJsonArray().size() > 0) {
									cell.setCellValue(tmpObj.get("synonyms").getAsJsonArray().toString());
								}
							} else if (excelHeader.get(j).equals("tag_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("tag").getAsString());
							}
						}
					} else {
						cell.setCellValue(resultObject.get(excelHeader.get(j)).getAsString());
					}
				} else if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION)) {

					// 연관어 분석결과 출력
					if (excelHeader.get(j).indexOf("keyword_") > -1 || excelHeader.get(j).indexOf("relatedKeyword_") > -1) {
						JsonArray textList = resultObject.get("result").getAsJsonArray();
						JsonObject tmpObj = null;
						
						for (int k = 0; k < textList.size(); k++) {
							tmpObj = textList.get(k).getAsJsonObject();
							
							if (excelHeader.get(j).equals("keyword_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("keyword").getAsString());
							} else if (excelHeader.get(j).equals("relatedKeyword_" + ( k + 1 ))) {
								cell.setCellValue(tmpObj.get("relatedKeyword").getAsString());
							}
						}
					} else {
						cell.setCellValue(resultObject.get(excelHeader.get(j)).getAsString());
					}
				} else if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY))  {
					
					// 문서요약은 요약 텍스트에 개행문자 추가하여 합친 후 출력
					if (excelHeader.get(j).equals("summary")) {
						JsonArray textList = resultObject.get("result").getAsJsonArray();
						StringBuffer summary = new StringBuffer();
						
						for (int k = 0; k < textList.size(); k++) {
							if (k != 0) {
								summary.append("\n\n");
							}
							summary.append(textList.get(k).getAsJsonObject().get("text").getAsString());
						}
						cell.setCellValue(summary.toString());
					} else {
						cell.setCellValue(resultObject.get(excelHeader.get(j)).getAsString());
					}
				} else {
					// 전처리 데이터 출력
					if (excelHeader.get(j).equals("json")) {
						// 업로드 작업을 위한 JSON SET 만들기
						if (paramMap.get("taskType").toString().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
							// 요약 전처리는 화자 구분만 제거
							JsonObject json = new JsonObject();
							json.addProperty("docid", resultObject.get("docid").getAsString());
							json.addProperty("label", resultObject.get("label").getAsString());
							json.addProperty("content", resultObject.get("resultPreprocess").getAsString().replace("(TX) ", "").replace("(RX) ", ""));
							cell.setCellValue(json.toString());
						} else {
							// 감성 전처리는 RX 데이터를 문장단위로 구성
							String [] talk = resultObject.get("resultPreprocess").getAsString().split("\\.");
							JsonObject json = null;
							
							int seq = 1;
							for (int k = 0; k < talk.length; k++) {
								if (talk[k].indexOf("(RX)") > -1) {
									json = new JsonObject();
									json.addProperty("docid", resultObject.get("docid").getAsString() + "_" + seq++);
									json.addProperty("label", resultObject.get("label").getAsString());
									json.addProperty("content", talk[k].replace("(RX) ", ""));

									cell.setCellValue(json.toString());
									row = sheet.createRow(rowNum++);
									cell = row.createCell(j);
									cell.setCellStyle(bodyStyle);
								}
							}
							
							// 행이 공백으로 생성되는것을 막기위하여 rowNum 1 차감
							rowNum--;
						}
					} else {
						cell.setCellValue(resultObject.get(excelHeader.get(j)).getAsString());
					}
				}
			}
		}
		
		// 파일명 지정
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		response.setContentType("ms-vnd/excel");
		response.setHeader( "Content-Disposition"
						  , "attachment;filename="
						  + new String(((String) paramMap.get("excelFileName")).getBytes("UTF-8"), "8859_1") + "_"
						  + sdf.format(new Date()) + ".xlsx");
		
		// 엑셀 출력
		wb.write(response.getOutputStream());
		wb.close();
	}
}
