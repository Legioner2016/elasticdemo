package com.example.elastic.demo.model.elastic;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.Data;

import org.springframework.data.elasticsearch.annotations.InnerField;

/**
 * Elastic search сущность
 * 
 * @author legioner
 *
 */
@Document(indexName = "melody")
//@Mapping(mappingPath = "melody_index_mappings.json") - не использую, работает и с декларатичным объевлением в документе
@Setting(settingPath = "melody_index_settings.json")
@Data
public class Melody {
	@Id
	@Field(type = FieldType.Keyword, store = true, name = "id_melody")
	Integer		idMelody;
	@Field(type = FieldType.Text, analyzer = "standard_edge_ngram")
	String 		title;
	@Field(type = FieldType.Text, analyzer = "standard_edge_ngram")
	String 		artist;
	@MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard_ngram"), 
			otherFields = {@InnerField(suffix = "ru", type = FieldType.Text, analyzer = "russian_analyzer") })
	String 		lyric;
	
}
