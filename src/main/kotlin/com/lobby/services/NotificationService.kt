package com.lobby.services

import com.resend.Resend
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import com.resend.services.emails.model.SendEmailRequest

@Service
class NotificationService(
    private val resend: Resend
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    @Async
    fun sendArrivalNotification(recipientName: String, email: String, residentName: String, trackingCode: String) {
        val htmlContent = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Sua encomenda chegou!</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f6f9fc; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
            
            <div style="display: none; max-height: 0px; overflow: hidden;">
                Uma nova encomenda para $recipientName acabou de chegar na portaria. Veja o código de retirada.
            </div>
    
            <table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f6f9fc; padding: 40px 0;">
                <tr>
                    <td align="center">
                        <table border="0" cellpadding="0" cellspacing="0" width="600" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden;">
                            
                            <tr>
                                <td align="center" style="background-color: #ffffff; padding: 40px 0 20px 0; border-bottom: 1px solid #f0f0f0;">
                                    <div style="background-color: #4F46E5; width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                                        <span style="font-size: 28px; line-height: 50px;">📦</span>
                                    </div>
                                    <h2 style="margin: 10px 0 0 0; color: #1a1a1a; font-size: 22px; letter-spacing: -0.5px;">LOBBY APP</h2>
                                </td>
                            </tr>
    
                            <tr>
                                <td style="padding: 40px 30px; text-align: center;">
                                    <h1 style="color: #1a202c; font-size: 24px; margin: 0 0 20px 0;">Encomenda Recebida!</h1>
                                    
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                                        Olá, <strong>$residentName</strong>.<br>
                                        Registramos a chegada de um pacote na portaria destinado a:
                                    </p>
    
                                    <div style="background-color: #eff6ff; border: 1px dashed #bfdbfe; border-radius: 8px; padding: 15px; display: inline-block; margin-bottom: 25px;">
                                        <span style="color: #1e40af; font-weight: 600; font-size: 14px;">Destinatário:</span><br>
                                        <span style="color: #1e3a8a; font-size: 18px; font-weight: bold;">$recipientName</span>
                                    </div>
    
                                    <p style="color: #4a5568; font-size: 16px; margin-bottom: 15px;">
                                        Informe o código abaixo ao porteiro para retirar:
                                    </p>
    
                                    <div style="background-color: #2d3748; color: #ffffff; font-family: 'Courier New', monospace; font-size: 32px; font-weight: bold; letter-spacing: 4px; padding: 20px; border-radius: 8px; margin: 0 auto 30px auto; width: fit-content; border: 2px solid #4a5568;">
                                        $trackingCode
                                    </div>
    
                                    <p style="color: #e53e3e; font-weight: 600; font-size: 14px; background-color: #fff5f5; padding: 10px; border-radius: 6px; display: inline-block;">
                                        ⚠️ A portaria está aguardando a retirada.
                                    </p>
                                    
                                    <p style="color: #718096; font-size: 13px; margin-top: 20px;">
                                        Dica: Leve um documento com foto caso o porteiro solicite.
                                    </p>
                                </td>
                            </tr>
    
                            <tr>
                                <td style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #f0f0f0;">
                                    <p style="color: #a0aec0; font-size: 12px; margin: 0;">
                                        © 2025 Lobby App - Gestão Inteligente de Condomínios<br>
                                        Não responda a este e-mail automático.
                                    </p>
                                </td>
                            </tr>
                        </table>
                        
                        <div style="height: 40px;"></div>
                    </td>
                </tr>
            </table>
        </body>
        </html>
    """.trimIndent()

        val params = SendEmailRequest.builder()
            .from("Lobby App <onboarding@resend.dev>")
            .to(email)
            .subject("📦 Chegou uma encomenda para você!")
            .html(htmlContent)
            .build()

        try {
            resend.emails().send(params)
            logger.info("✅ Email enviado para: {}", email)
        } catch (e: Exception) {
            logger.error("❌ Erro ao enviar email: {}", e.message, e)
        }
    }
}