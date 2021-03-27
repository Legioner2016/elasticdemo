package com.example.elastic.demo.repositories.postgres;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.elastic.demo.model.postgres.Lyric;

/**
 * Репозиторий текстов в БД
 * 
 * @author legioner
 *
 */
public interface DBLyricRepository extends CrudRepository<Lyric, Integer> {
	
	List<Lyric> findByIdMelodyIn(List<Integer> ids);
	
	@Modifying
	@Query("INSERT INTO lyrics (id_melody, text) VALUES(:id, :text)")
	public int insert(@Param("id") Integer idMelody, @Param("text") String lyric); 

}
