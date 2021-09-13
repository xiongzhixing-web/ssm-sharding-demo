package com.soecode.lyf.validator;


import com.alibaba.fastjson.JSON;
import com.soecode.lyf.util.SignatureUtil;
import com.soecode.lyf.vo.BaseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 */
public class RegisterSignatireValidator implements Validator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Set<String> excludeProperties = new HashSet<>(Arrays.asList("sign"));
    @Override
    public boolean supports(Class<?> cls) {
        return cls == null ? false : BaseVo.class.isAssignableFrom(cls);
    }

    @Override
    public void validate(Object o, Errors errors) {
        logger.debug("RegisterSignatireValidator#validate 开始验证签名逻辑： {}", JSON.toJSONString(o));
        if(null == o){
            errors.rejectValue("o",null,null,"对象不能为空");
            return;
        }
        String invokeSign = ((BaseVo)o).getSign();
        String calSign = "";
        try {
            String str = SignatureUtil .getRequestParamStr(o,new HashSet<>(excludeProperties));
            calSign = DigestUtils.md5DigestAsHex(str.getBytes());
        } catch (Exception e) {
            logger.error("RegisterSignatireValidator#validate catch a exception",e);
            return;
        }
        if(!calSign.equals(invokeSign)){
            errors.rejectValue("sign",null,null,"签名验证失败");
            return;
        }
    }
}
