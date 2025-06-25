package com.eduhub.auth_service.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtil {

    private static final int OTP_LENGTH = 6;
    private static final Random random = new Random();

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}