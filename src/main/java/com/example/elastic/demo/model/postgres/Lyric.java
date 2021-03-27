package com.example.elastic.demo.model.postgres;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

/**
 * Сущность для текста мелодии (БД)
 * 
 * @author legioner
 *
 */
@Table("lyrics")
@Data
public class Lyric {
	@Id
	@Column("id_melody")
	Integer		idMelody;
	String		text;
}
