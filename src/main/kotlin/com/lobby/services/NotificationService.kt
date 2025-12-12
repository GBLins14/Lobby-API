package com.lobby.services

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val mailSender: JavaMailSender
) {

    fun sendArrivalNotification(recipientName: String, email: String, residentName: String, trackingCode: String) {
        val message: MimeMessage = mailSender.createMimeMessage()

        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom("no-reply@lobby.com")
        helper.setTo(email)
        helper.setSubject("📦 Lobby: chegou uma encomenda para: $recipientName!")

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-top: 20px; margin-bottom: 20px; }
                    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; text-align: center; color: #333333; }
                    .image-container { margin: 20px 0; }
                    .image-container img { max-width: 100%; height: auto; border-radius: 8px; }
                    .tracking-box { background-color: #f1c40f; color: #ffffff; font-size: 24px; font-weight: bold; padding: 15px; border-radius: 8px; display: inline-block; margin: 20px 0; letter-spacing: 2px; }
                    .footer { background-color: #ecf0f1; padding: 15px; text-align: center; font-size: 12px; color: #7f8c8d; }
                    h1 { margin-top: 0; color: #2c3e50; }
                    p { font-size: 16px; line-height: 1.5; }
                    .highlight { color: #e74c3c; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🏢 LOBBY APP</h2>
                    </div>

                    <div class="content">
                        <h1>Olá, $residentName!</h1>
                        
                        <p>Temos boas notícias. Uma nova encomenda acabou de chegar à portaria para: $recipientName.</p>

                        <p>Para retirar, informe o código abaixo ao porteiro:</p>

                        <div class="tracking-box">
                            $trackingCode
                        </div>

                        <p class="highlight">A portaria está a espera!</p>
                    </div>

                    <div class="footer">
                        <p>Este é um e-mail automático do sistema Lobby.<br>
                        Por favor, não responda a este e-mail.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        helper.setText(htmlContent, true)

        mailSender.send(message)
    }
}