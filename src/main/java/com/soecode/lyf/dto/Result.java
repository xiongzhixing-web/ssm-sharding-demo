package com.soecode.lyf.dto;

import com.soecode.lyf.util.api.DocAnnotation;
import lombok.Data;
import lombok.ToString;

/**
 * 封装json对象，所有返回结果都使用它
 */

@Data
@ToString(callSuper = true)
public class Result<T> {
	public final static int ERR_SYSTEM_EXCEPTION = 301;
	public final static int ERR_PARAM_INVALID = 301;
	public final static int ERR_BIZ_DATA_EXCEPTION = 401;
	public final static int ERR_RPC_INVOKE_FAIL = 402;
	public final static int ERR_CATCH_EXCEPTION = 403;

	@DocAnnotation(comment = "错误码，200表示成功")
	private long errCode;
	@DocAnnotation(comment = "错误消息")
	private String errMessage;
    @DocAnnotation(comment = "响应数据数据")
	private T data;

	public static <T> Result<T> success(T data){
		Result<T> result = new Result<T>();
		result.setData(data);
		return result;
	}

	public static <T> Result<T> fail(long errCode,String errMessage){
		Result<T> result = new Result<>();
		result.setErrCode(errCode);
		result.setErrMessage(errMessage);
		return result;
	}
}
