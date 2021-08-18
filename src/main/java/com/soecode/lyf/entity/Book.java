package com.soecode.lyf.entity;

import lombok.Data;
import lombok.ToString;

/**
 * 图书实体
 */
@Data
@ToString(callSuper = true)
public class Book {
	private long bookId;
	private String name;
	private int number;
	//@DocAnnotation(comment = "借书的人")
	//private People people;
	//@DocAnnotation(comment = "预约列表")
	//private List<Appointment> appointmentList
}
