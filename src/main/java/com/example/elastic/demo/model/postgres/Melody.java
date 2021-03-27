package com.example.elastic.demo.model.postgres;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

/**
 * Сущность мелодии (БД)
 * 
 * @author legioner
 *
 */
@Table("melodies")
@Data
public class Melody {
	@Id
	@Column("id_melody")
	Integer		idMelody;
	String		artist;
	String 		title;
	String[]	tags;
}
