package kr.co.wisenut.util;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PageUtil {

	public static String createPageNav(double totalCount, Map<String, Object> paramMap) {
		
		StringBuffer pageNav = new StringBuffer();
		
		double pageSize = Double.parseDouble(paramMap.get("pageSize").toString());		// 페이징 처리 건수
		double pageRow = Double.parseDouble(paramMap.get("pageRow").toString());		// 현재 페이지
		int maxPage = (int)Math.ceil(totalCount / pageSize);							// 마지막 페이지
		
		int pageGroup = (int)Math.ceil(pageRow / pageSize);								// 시작, 종료페이지 표시를 위한 변수
		
		int endPage = pageGroup * 10;													// 종료 페이지
		if (endPage > maxPage) endPage = maxPage;

		int startPage = endPage - 9;													// 시작 페이지
		if (startPage < 0) {
			startPage = 1;
		} else if (startPage > 10 && startPage < (pageGroup * 10)) {
			startPage = ((pageGroup - 1) * 10) + 1;
		}
		
		// <<, < 버튼 생성
		if (startPage > 1) { 
			pageNav.append("<a class=\"right\" href=\"#\" onclick=\"fnDoList(1)\">&lt;&lt;</a>\n");
			pageNav.append("<a class=\"right\" href=\"#\" onclick=\"fnDoList(" + ( startPage - 1 ) + ")\">&lt;</a>\n");	
		}

		// 페이징 버튼 생성
		for (int i = startPage; i <= endPage; i++) {
			if (i == pageRow) {
				pageNav.append("<a class=\"num click\" href=\"#\" onclick=\"fnDoList(" + i + ")\"> " + i + " </a>\n");
			} else {
				pageNav.append("<a class=\"num\" href=\"#\" onclick=\"fnDoList(" + i + ")\"> " + i + " </a>\n");
			}
			
			if (i == maxPage) break;
		}
		
		// >, >> 버튼 생성
		if (endPage < maxPage) { 
			pageNav.append("<a class=\"right\" href=\"#\" onclick=\"fnDoList(" + ( endPage + 1 ) + ")\">&gt</a>\n"); 
			pageNav.append("<a class=\"right\" href=\"#\" onclick=\"fnDoList(" + maxPage + ")\">&gt&gt</a>\n"); 
		}
		
		return pageNav.toString();
	}
}
