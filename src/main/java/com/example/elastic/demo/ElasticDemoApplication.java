package com.example.elastic.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Главный класс приложения (если мы не выполняем начальное индексирование)
 * 
 * @author legioner
 *
 */
@Profile("!indexer")
@EnableElasticsearchRepositories
@SpringBootApplication
public class ElasticDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticDemoApplication.class, args);
	}
	
}
