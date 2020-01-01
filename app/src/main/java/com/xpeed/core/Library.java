package com.xpeed.core;

public class Library {

     public static int generateOTP(int min, int max){
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }
}
