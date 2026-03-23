package com.example.bankcards.service;

import org.springframework.stereotype.Service;

public class MaskingService {
        public static String maskCardNumber(String cardNumber){
            if(cardNumber==null || cardNumber.length()<4){
                return "****";
            }

            return "**** **** **** "+cardNumber.substring(cardNumber.length()-4);
        }
}
