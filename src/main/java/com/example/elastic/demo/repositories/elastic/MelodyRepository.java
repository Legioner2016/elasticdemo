package com.example.elastic.demo.repositories.elastic;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.elastic.demo.model.elastic.Melody;

/**
 * Elastic repository
 * Есть с ними набор проблем: 
 * - count вообще сейчас не отрабатывают с custom @Query
 * - запросы работают, но они не match (или term): если искать больше, чем по одному слову - начинаются проблемы  
 * 
 * Дополнение: если репозиториев такого типа в проекте нет, index не создается автоматически - нужно создавать его вручную 
 * 
 * @author legioner
 *
 */
public interface MelodyRepository extends ElasticsearchRepository<Melody, Integer> {
	
	Optional<Melody>		findById(Integer idMelody);
	
	@Query(value="{\"bool\":{\"must\":{\"match_phrase\":{\"title\":\"?0\"}}}}")
	Page<Melody> 			findByTitleStartingWith(String title, Pageable page);
	@Query(value="{\"bool\":{\"must\":{\"match_phrase\":{\"artist\":\"?0\"}}}}")
	Page<Melody> 			findByArtistStartingWith(String artist, Pageable page);
	Page<Melody> 			findAll(Pageable page);
	
	@Query(value="{\"bool\":{\"must\":{\"match\":{\"lyric\":\"?0\"}}}}")
	Page<Melody>			findAllByLyric(String Lyric, Pageable page);
	@Query(value="{\"bool\":{\"must\":{\"match\":{\"lyric.ru\":\"?0\"}}}}")
	Page<Melody>			findAllByLyricRu(String Lyric, Pageable page);

	//Cannot use with custom query
	//https://github.com/spring-projects/spring-data-elasticsearch/issues/1156
//	long					countByTitleStartingWith(String title);
//	long					countByArtistStartingWith(String artist);
	
//	@Query(value="{\"bool\":{\"must\":{\"term\":{\"lyric\":\"?0\"}}}}")
//	long					countByLyric(String Lyric);
//	@Query(value="{\"bool\":{\"must\":{\"term\":{\"lyric.ru\":\"?0\"}}}}")
//	long					countByLyricRu(String Lyric);
}
