package com.example.elastic.demo.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response для datatable списка мелодий
 * 
 * @author legioner
 *
 * @param <T>
 */
@Getter
@Setter
@NoArgsConstructor
public class DataTablesResponseDTO<T> {

	private int draw;
	private long recordsTotal = 0L;
	private long recordsFiltered = 0L;
	private List<T> data = new ArrayList<>();

}
