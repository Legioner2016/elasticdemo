package com.example.elastic.demo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.elastic.demo.bean.DataTableRequestDTO;
import com.example.elastic.demo.bean.DataTablesResponseDTO;
import com.example.elastic.demo.bean.MelodyBean;
import com.example.elastic.demo.bean.OperationResult;
import com.example.elastic.demo.service.MelodyService;

/**
 * Минимальный набор операций
 * 
 * @author legioner
 *
 */
@Controller
public class MainController {

	@Autowired
	MelodyService	service;
	
	//Список мелодий (datatable, ajax)
	@PostMapping("/melodies")
	public @ResponseBody DataTablesResponseDTO<MelodyBean> filterMelodies(@RequestBody DataTableRequestDTO request) throws IOException {
		return service.getMelodyList(request);
	}
	
	//Удалить (ajax)
	@DeleteMapping("/remove/{id}")
	public @ResponseBody OperationResult removeMelody(@PathVariable Integer id) throws Exception {
		return service.removeMelody(id);
	}
	
	//Получить аднные мелодии по id (ajax)
	@GetMapping("/melody/{id}")
	public @ResponseBody MelodyBean getMelodyById(@PathVariable Integer id) throws Exception {
		return service.getMelodyById(id);
	}
	
	//Сохранить новую или отредактиорванную мелодию (http request)
	@PostMapping("/edit")
	public String saveMelody(@ModelAttribute MelodyBean bean) throws Exception {
		service.save(bean);
		Integer id = bean.getIdMelody();
		return "redirect:/edit.html?id=" + id;
	}
	
	
	
}
