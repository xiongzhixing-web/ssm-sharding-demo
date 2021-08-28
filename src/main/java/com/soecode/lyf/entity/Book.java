package com.soecode.lyf.entity;

import com.soecode.lyf.util.api.DocAnnotation;
import lombok.Data;
import lombok.ToString;

/**
 * 图书实体
 */
@Data
@ToString(callSuper = true)
public class Book {
	@DocAnnotation(comment = "书本ID")
	private long bookId;
	@DocAnnotation(comment = "书本名称")
	private String name;
	private int number;
	//@DocAnnotation(comment = "借书的人")
	//private People people;
	//@DocAnnotation(comment = "预约列表")
	//private List<Appointment> appointmentList
}
