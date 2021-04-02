package com.example.elastic.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.elastic.demo.bean.DataTableRequestDTO;
import com.example.elastic.demo.bean.DataTablesResponseDTO;
import com.example.elastic.demo.bean.MelodyBean;
import com.example.elastic.demo.bean.OperationResult;
import com.example.elastic.demo.model.elastic.Melody;
import com.example.elastic.demo.model.postgres.Lyric;
import com.example.elastic.demo.repositories.elastic.MelodyRepository;
import com.example.elastic.demo.repositories.postgres.DBLyricRepository;
import com.example.elastic.demo.repositories.postgres.DBMelodyRepository;

/**
 * Операции с мелодиями
 * 
 * @author legioner
 *
 */
@Service
public class MelodyService {
	
	@Autowired
	MelodyRepository	elasticRepository;

	@Autowired
	DBMelodyRepository	melodyRepository;

	@Autowired
	DBLyricRepository	lyricRepository;
	
	@Autowired
	RestHighLevelClient restHighLevelClient;
	
	private static final String INDEX_NAME = "melody";

	//Список мелодий
	//Сначала идем в elastic - потом для найденной страницы - идем в БД репозитории за данными 
	public DataTablesResponseDTO<MelodyBean> getMelodyList(DataTableRequestDTO request) throws IOException {
		var result = new DataTablesResponseDTO<MelodyBean>();
		List<Integer> temp = null;
		result.setDraw(request.getDraw());
		result.setRecordsTotal(elasticRepository.count());
		//Тут ветвления - в зависимости от типа фильтра.
		//Можно было бы сочетать фильтры - но тут загадочно для меня - как считать релевантность
		if (request.getId() != null && request.getId() > 0) {
			if (melodyExists(request.getId())) {
				temp = new ArrayList<>();
				temp.add(request.getId());
				result.setRecordsFiltered(1);
			}
			else {
				result.setRecordsFiltered(0);
			}
		}
		else if (request.getTitle() != null && !request.getTitle().isEmpty()) {
			result.setRecordsFiltered(countBy("title",  request.getTitle()));
			Page<Melody> page = elasticRepository.findByTitleStartingWith(request.getTitle(),
					PageRequest.of(request.getStart() / request.getLength(), request.getLength()));
			temp = page.getContent().stream().map(Melody::getIdMelody).collect(Collectors.toList());
		}
		else if (request.getArtist() != null && !request.getArtist().isEmpty()) {
			result.setRecordsFiltered(countBy("artist", request.getArtist()));
			Page<Melody> page = elasticRepository.findByArtistStartingWith(request.getArtist(),
					PageRequest.of(request.getStart() / request.getLength(), request.getLength()));
			temp = page.getContent().stream().map(Melody::getIdMelody).collect(Collectors.toList());
		}
		else if (request.getLyric() != null && !request.getLyric().isEmpty()) {
			if (Boolean.TRUE.equals(request.getLyricRu())) {
				result.setRecordsFiltered(countByLyric("lyric.ru", request.getLyric()));
				Page<Melody> page = elasticRepository.findAllByLyricRu(request.getLyric(),
						PageRequest.of(request.getStart() / request.getLength(), request.getLength()));
				temp = page.getContent().stream().map(Melody::getIdMelody).collect(Collectors.toList());
			}
			else {
				result.setRecordsFiltered(countByLyric("lyric", request.getLyric()));
				Page<Melody> page = elasticRepository.findAllByLyric(request.getLyric(),
						PageRequest.of(request.getStart() / request.getLength(), request.getLength()));
				temp = page.getContent().stream().map(Melody::getIdMelody).collect(Collectors.toList());
			}
		}
		else {
			result.setRecordsFiltered(result.getRecordsTotal());
			Page<Melody> page = elasticRepository.findAll(PageRequest.of(request.getStart() / request.getLength(),
																	request.getLength()));
			temp = page.getContent().stream().map(Melody::getIdMelody).collect(Collectors.toList());
		}
		//Если нашлись какие-то мелодии в elastic - заполнить данные страницы из БД
		result.setData(toMelodyList(temp));
		return result;
	}
	
	//Получить одну мелодию (из БД. два вызова: сама мелодия, затем текст)
	//У меня текст и мелодия - разные таблицы в одной БД, так что можно было бы одним запросом;
	//но в таком варианте несложно развернуть на схему - разные БД мелодий и текстов 
	public MelodyBean getMelodyById(Integer id) throws Exception {
		return melodyRepository.findById(id)
				.map(m -> {
					MelodyBean b = new MelodyBean();
					b.setArtist(m.getArtist());
					b.setTitle(m.getTitle());
					b.setTags(m.getTags());
					b.setIdMelody(m.getIdMelody());
					b.setLyric(lyricRepository.findById(b.getIdMelody()).map(Lyric::getText).orElse(null));
					return b;
				})
				.orElse(new MelodyBean());
	}
	
	//Проверить, что мелодия в БД есть
	public boolean melodyExists(Integer id) {
		return melodyRepository.existsById(id);
	}
	
	//Сохранить новую (или отредактированную мелодию)
	//Сначала в БД, потом в текстах; а затем - в elastic
	public void save(MelodyBean bean) throws Exception {
		var melodyDB = new com.example.elastic.demo.model.postgres.Melody();
		melodyDB.setArtist(bean.getArtist());
		melodyDB.setTitle(bean.getTitle());
		if (bean.getIdMelody() != null && bean.getIdMelody() > 0) {
			melodyDB.setIdMelody(bean.getIdMelody());	
		}
		melodyRepository.save(melodyDB);
		bean.setIdMelody(melodyDB.getIdMelody());		
		Optional<Lyric> lyric = lyricRepository.findById(melodyDB.getIdMelody());
		if (lyric.isPresent()) {
			if (bean.getLyric() != null && !bean.getLyric().isEmpty()) {
				lyric.get().setText(bean.getLyric());
				lyricRepository.save(lyric.get());
			}
			else {
				lyricRepository.deleteById(melodyDB.getIdMelody());
			}
		}
		else if (bean.getLyric() != null && !bean.getLyric().isEmpty()) {
			//Я не могу добавить новую запись с id - repository пойдет в update
			//Кроме - как запросом
			lyricRepository.insert(melodyDB.getIdMelody(), bean.getLyric());
		}
		Melody melodyElastic = new Melody();
		melodyElastic.setIdMelody(melodyDB.getIdMelody());
		melodyElastic.setArtist(bean.getArtist());
		melodyElastic.setTitle(bean.getTitle());
		melodyElastic.setLyric(bean.getLyric());
		elasticRepository.save(melodyElastic);
	}
	
	//По списку id построить список мелодий для отображения
	//порядок сортировки сохранить
	public List<MelodyBean> toMelodyList(List<Integer> ids) {
		List<MelodyBean> result = null;
		if (ids != null && !ids.isEmpty()) {
			Map<Integer, MelodyBean> temp = StreamSupport.stream(melodyRepository.findAllById(ids).spliterator(), false)
				.map(m -> {
					MelodyBean b = new MelodyBean();
					b.setArtist(m.getArtist());
					b.setTitle(m.getTitle());
					b.setTags(m.getTags());
					b.setIdMelody(m.getIdMelody());
					return b;
				})	
				.peek(b -> 
					b.setLyric(lyricRepository.findById(b.getIdMelody()).map(Lyric::getText).orElse(null)))
				.collect(Collectors.toMap(b -> b.getIdMelody(), b -> b));
			result = ids.stream().map(id -> temp.get(id)).collect(Collectors.toList());
		}
		else {
			result = new ArrayList<>();
		}
		return result;
	}
	
	//Удалить мелодию
	//3 вызова - в каждом репозитории по id
	public OperationResult removeMelody(Integer id) throws Exception {
		elasticRepository.deleteById(id);
		lyricRepository.deleteById(id);
		melodyRepository.deleteById(id);
		return new OperationResult();		
	}
	
	//Пока проблемы с custom @Query у count в elastic репозиториях
	//Поэтому подсчет общего числа отфильтрованных - запросом
	long countBy(String field, String title) throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(0);

		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		qb.should(QueryBuilders.matchPhraseQuery(field, title).boost(1f));
		qb.should(QueryBuilders.matchPhraseQuery(field + ".pre", title).boost(3f));

		searchRequest.indices(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);
		searchSourceBuilder.query(qb);
		
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		return searchResponse.getHits().getTotalHits().value;
	}

	//Пока проблемы с custom @Query у count в elastic репозиториях
	//Поэтому подсчет общего числа отфильтрованных - запросом
	//(Отличие от предыдущего метода - только в типе запроса)
	long countByLyric(String field, String title) throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(0);

		BoolQueryBuilder qb = QueryBuilders.boolQuery();
		qb.must(QueryBuilders.matchQuery(field, title));
		searchSourceBuilder.query(qb);

		searchRequest.indices(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);
		
		
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		return searchResponse.getHits().getTotalHits().value;
	}
	
	
}
