package com.atguigu.gmall.interceptors;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod hm = null;
        try {
            hm = (HandlerMethod) handler;
        } catch (Exception e) {
            return true;
        }
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        if (methodAnnotation == null) {
            return true;
        } else {
            // 先获得用户cookie中关于用户的身份token
            //token有四种情况
            String token = "";

            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
            if (StringUtils.isNotBlank(oldToken)) {
                token = oldToken;
            }
            String newToken = request.getParameter("newToken");
            if (StringUtils.isNotBlank(newToken)) {
                token = newToken;
            }


            if (StringUtils.isNotBlank(token)) {
                // 验证用户的token是否正确

                // 通过远程ws请求认证中心，验证token
                String requestUrl =
                        "http://passport.gmall.com:8090/verify?token=" + token + "&currentIp=" + request.getRemoteAddr();
                String successJSON = HttpclientUtil.doGet(requestUrl);
                HashMap<String, String> hashMap = new HashMap<>();
                HashMap hashMapJSON = JSON.parseObject(successJSON, hashMap.getClass());

                if (hashMapJSON != null && hashMapJSON.get("success").equals("success")) {
                    // 重新更新cookie的过期时间
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60, true);
                    request.setAttribute("memberId", hashMapJSON.get("memberId"));
                    request.setAttribute("nickname", hashMapJSON.get("nickname"));
                    return true;
                } else {
                    if (methodAnnotation.isNeededSuccess()) {
                        String ReturnUrl = request.getRequestURL().toString();
                        response.sendRedirect("http://passport.gmall.com:8090/index?ReturnUrl=" + ReturnUrl);
                        // 拦截验证
                        return false;
                    }
                }
            } else {
                if (methodAnnotation.isNeededSuccess()) {
                    String ReturnUrl = request.getRequestURL().toString();
                    response.sendRedirect("http://passport.gmall.com:8090/index?ReturnUrl=" + ReturnUrl);
                    // 拦截验证
                    return false;
                }
            }
        }

        return true;

    }
}
