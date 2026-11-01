package com.pf.common.service.userManagement;

import com.pf.common.dto.userManagement.CaptchaResponse;
import com.pf.common.service.redis.RedisSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String CAPTCHA_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int CAPTCHA_LENGTH = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final RedisSessionService redisSessionService;

    public CaptchaResponse generateCaptcha() throws IOException {
        String captchaId = UUID.randomUUID().toString();

        StringBuilder answer = new StringBuilder(CAPTCHA_LENGTH);

        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = secureRandom.nextInt(CAPTCHA_CHARACTERS.length());
            answer.append(CAPTCHA_CHARACTERS.charAt(index));
        }

        redisSessionService.saveCaptcha(
                captchaId,
                answer.toString()
        );

        String captchaImage = generateCaptchaImage(answer.toString());

        return new CaptchaResponse(captchaId, captchaImage);
    }

    public boolean validateCaptcha(String captchaId, String answer) {
        String storedAnswer = redisSessionService.getCaptchaAnswer(captchaId);

        if (storedAnswer == null || answer == null) {
            return false;
        }

        boolean valid = storedAnswer.equals(answer.trim());

        if (valid) {
            redisSessionService.deleteCaptcha(captchaId);
        }

        return valid;
    }

    private String generateCaptchaImage(String captchaText)
            throws IOException {

        int width = 180;
        int height = 60;

        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setFont(
                    new Font("SansSerif", Font.BOLD, 30)
            );

            graphics.setColor(Color.DARK_GRAY);
            graphics.drawString(captchaText, 25, 40);

            // Add simple interference lines.
            SecureRandom random = new SecureRandom();

            for (int i = 0; i < 5; i++) {
                graphics.setColor(new Color(
                        random.nextInt(180),
                        random.nextInt(180),
                        random.nextInt(180)
                ));

                graphics.drawLine(
                        random.nextInt(width),
                        random.nextInt(height),
                        random.nextInt(width),
                        random.nextInt(height)
                );
            }
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);

        return "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(output.toByteArray());
    }
}