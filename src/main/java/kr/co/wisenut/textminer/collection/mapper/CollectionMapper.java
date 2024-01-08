package kr.co.wisenut.textminer.collection.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.model.vo.ModelVo;

public interface CollectionMapper {

	public int chkDuplicatedCollectionName(CollectionVo collection);										// 컬렉션 이름 중복 체크
	public int getCollectionTotalCount(Map<String, Object> paramMap);										// 컬렉션 전체 건수 조회
	public List<CollectionVo> getCollectionNames(Map<String, Object> paramMap);								// 컬렉션 목록 조회
	public List<CollectionVo> getCollectionList(Map<String, Object> paramMap);								// 컬렉션 리스트 조회
	public CollectionVo getCollectionDetail(CollectionVo collection);										// 컬렉션 상세 조회
	public CollectionVo getCollectionDetailUseModel(ModelVo model);											// 컬렉션 상세 조회(model 사용)
	
	public int insertCollection(CollectionVo collection);													// 컬렉션 등록
	public int updateCollection(CollectionVo collection);													// 컬렉션 수정
	public int deleteCollection(CollectionVo collection);													// 컬렉션 삭제
}