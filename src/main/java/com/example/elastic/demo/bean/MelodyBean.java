package com.example.elastic.demo.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ьин, которым обмениваемся с UI
 * 
 * @author legioner
 *
 */
@Getter @Setter @NoArgsConstructor
public class MelodyBean {
	Integer	idMelody;
	String	title;
	String	artist;
	String	lyric;
	String[] tags;
}
