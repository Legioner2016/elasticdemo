package com.example.elastic.demo.bean;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DataTable request списка мелодий
 * 
 * @author legioner
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class DataTableRequestDTO {

    private int draw;
    private List<HashMap<String, String>> columns;
    private List<HashMap<String, String>> order;
    private int start;
    private int length;
    private Integer id;
    private String title;
    private String artist;
    private String lyric;
    private Boolean lyricRu;
	
}
