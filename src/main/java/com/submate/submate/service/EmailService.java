package com.submate.submate.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(
            String email,
            String code,
            String purpose
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("[Submate] 이메일 인증번호");

        String body;

        if (purpose.equals("SIGNUP")) {
            body = """
                    안녕하세요, Submate입니다.

                    회원가입 인증번호는 다음과 같습니다.

                    %s

                    인증번호는 5분 동안 유효합니다.

                    본인이 요청하지 않았다면 이 이메일을 무시해주세요.
                    """.formatted(code);

        } else {
            body = """
                    안녕하세요, Submate입니다.

                    비밀번호 재설정 인증번호는 다음과 같습니다.

                    %s

                    인증번호는 5분 동안 유효합니다.

                    본인이 요청하지 않았다면 이 이메일을 무시해주세요.
                    """.formatted(code);
        }

        message.setText(body);

        mailSender.send(message);
    }
}