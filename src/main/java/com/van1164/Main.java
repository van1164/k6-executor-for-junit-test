package com.van1164;

import com.van1164.k6executor.K6Executor;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try{
            String[] checkList = {"is status 200", "response time < 500ms"};
            K6Executor executor = new K6Executor("test.js",checkList);
            executor.runTest();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}