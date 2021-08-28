package com.soecode.lyf.vo;

import com.soecode.lyf.util.api.DocAnnotation;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class BookVo extends BaseVo{
    @NotNull(message="bookId不能为空")
    @DocAnnotation(isFill = true,comment = "书本ID")
    private Integer bookId;
    @NotBlank(message="bookName不能为空")
    @DocAnnotation(isFill = false,comment = "书本名称")
    private String  bookName;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
