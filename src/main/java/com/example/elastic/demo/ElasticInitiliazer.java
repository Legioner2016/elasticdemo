package com.example.elastic.demo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import com.example.elastic.demo.model.postgres.Melody;
import com.example.elastic.demo.repositories.postgres.DBLyricRepository;
import com.example.elastic.demo.repositories.postgres.DBMelodyRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Инициализация индекса (индексировать что есть)
 * bulk request
 * 
 * @author legioner
 *
 */
@Profile("indexer")
@Slf4j
@SpringBootApplication
public class ElasticInitiliazer {

	public static void main(String[] args) throws IOException {
		SpringApplication application = new SpringApplication(ElasticInitiliazer.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		ApplicationContext context = application.run(args);

		DBMelodyRepository dbRepository = context.getBean(DBMelodyRepository.class);
		DBLyricRepository dbLyricRepository = context.getBean(DBLyricRepository.class);
		//MelodyRepository melodyRepository = context.getBean(MelodyRepository.class);
		RestHighLevelClient restHighLevelClient = context.getBean(RestHighLevelClient.class);
		Integer minId = dbRepository.getMinId();
		Integer maxId = dbRepository.getMaxId();



		int from = minId, step = 1000;
		while (from <= maxId) {
			List<Integer> ids = IntStream.range(from, from + step)
					.mapToObj(Integer::valueOf).collect(Collectors.toList());
			List<Melody> melodies = dbRepository.findByIdMelodyIn(ids);
			Map<Integer, String> lyrics = dbLyricRepository.findByIdMelodyIn(ids).stream()
					.collect(Collectors.toMap(l -> l.getIdMelody(), l -> l.getText()));
			List<com.example.elastic.demo.model.elastic.Melody> indexMelodies =  melodies.stream()
					.map(m -> {
						var melody = new com.example.elastic.demo.model.elastic.Melody();
						melody.setIdMelody(m.getIdMelody());
						melody.setArtist(m.getArtist());
						melody.setTitle(m.getTitle());
						melody.setLyric(lyrics.get(m.getIdMelody()));
						return melody; 
					}).collect(Collectors.toList());
			if (!indexMelodies.isEmpty()) {
				BulkRequest bulkRequest = new BulkRequest();
				indexMelodies.forEach(m -> {
					IndexRequest indexRequest = new IndexRequest("melody").id(m.getIdMelody().toString())
								.source("id_melody", m.getIdMelody(), "artist", m.getArtist(), 
										"title", m.getTitle(), "lyric", m.getLyric());
					bulkRequest.add(indexRequest);
				});
				restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
			}
			log.info("done: " + melodies.size());
			from += step;
		}

		//Теоритечески - это удалит все старые версии докентов и уменьшит размер используемого для хранения индекса места
		//На практике этого можно не делать - elastic сам выполнит удаление помеченных как удаленные документы, 
		//при приближении к границам размера сегмента. Вот только я так и не понял - где этот размер по умолчанию
		ForceMergeRequest cleanRequest = new ForceMergeRequest("melody").maxNumSegments(1).flush(true);
		//ForceMergeRequest cleanRequest = new ForceMergeRequest("melody").onlyExpungeDeletes(true).flush(true);
		restHighLevelClient.indices().forcemergeAsync(cleanRequest, RequestOptions.DEFAULT, null);
		
		((ConfigurableApplicationContext)context).close();
	}


}
