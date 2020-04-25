package com.atguigu.gmall.passport.util;

import io.jsonwebtoken.*;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    public static void main(String[] args) {
//        Map<String,Object> map = new HashMap<>();
//
//        map.put("userid","1");
//        map.put("nickname","tom");
//
        String key = "atguigusso";
//
        String ip = "192.168.222.1";
//
//        String encode = encode(key, map, ip);
//
//        System.out.println(encode);


        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrbmFtZSI6InRvbSIsInVzZXJpZCI6IjEifQ" +
                ".AxTdunwdt9lCcCRRBenDITrXHvK3iEv0lC2yU_ozlLs";

        Map<String, Object> decode = decode(token, key, ip);

        System.out.println(decode);
    }

    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
        return claims;
    }
}
