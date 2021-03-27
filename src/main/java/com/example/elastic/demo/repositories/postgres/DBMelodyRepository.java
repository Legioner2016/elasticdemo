package com.example.elastic.demo.repositories.postgres;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.elastic.demo.model.postgres.Melody;

/**
 * Репозиторий мелодий
 * 
 * @author legioner
 *
 */
public interface DBMelodyRepository extends CrudRepository<Melody, Integer> {
	
	//min - max - чтобы определить гшраницы индексирования и индексировать группами (bulk)
	@Query("SELECT min(id_melody) FROM melodies")
	Integer	getMinId();

	@Query("SELECT max(id_melody) FROM melodies")
	Integer	getMaxId();

	List<Melody>  findByIdMelodyIn(List<Integer> ids);
	
}
