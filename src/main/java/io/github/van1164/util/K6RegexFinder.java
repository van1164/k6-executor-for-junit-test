package io.github.van1164.util;

import io.github.van1164.result.HttpReq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K6RegexFinder {

    public static HttpReq countHttpReq(String result){
        try {
            String regex = "http_req_failed.+:\\s+[0-9]\\.[0-9][0-9]%\\s+.\\s+(\\d+)\\s+.\\s(\\d+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            if(matcher.find() && matcher.groupCount() ==2){
                Integer failed = Integer.valueOf(matcher.group(1));
                Integer success = Integer.valueOf(matcher.group(2));
                return new HttpReq(success,failed,success+failed);
            }
            else{
                return new HttpReq();
            }
        }
        catch (Exception e){
            return new HttpReq();
        }
    }
}
