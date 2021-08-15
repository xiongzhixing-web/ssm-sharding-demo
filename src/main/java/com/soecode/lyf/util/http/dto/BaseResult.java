package com.soecode.lyf.util.http.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 熊志星
 * @description
 * @date 2019/10/18
 */
@Data
@ToString(callSuper = true)
public class BaseResult<T> implements Serializable {
    private static final long serialVersionUID = 4569910467373996570L;

    private long code;
    private T data;
}