package com.atguigu.gmall.passport.testOauth2;

import com.atguigu.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        String access_token_url = "https://api.weibo.com/oauth2/access_token?client_id= 25920146&client_secret=dc8de1392f642a01259b136ff8e970b9&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8090/vlogin&code=e211655dd6c78a66fcfcfdff552424f6";

        Map<String,String> map = new HashMap<String,String>();
        map.put("client_id","25920146");
        map.put("client_secret","dc8de1392f642a01259b136ff8e970b9");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://passport.gmall.com:8090/vlogin");
        map.put("code","1be7d4dd82731e14574d7b1b4240baa8");
        String json = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", map);

        System.out.println(json);
    }
}
