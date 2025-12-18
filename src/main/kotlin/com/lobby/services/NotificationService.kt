package com.lobby.services

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Service
class NotificationService(
    @Value("\${app.resend-api-key}") private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(ForgotPasswordService::class.java)

    private val restClient = RestClient.builder()
        .baseUrl("https://api.resend.com")
        .defaultHeader("Authorization", "Bearer $apiKey")
        .build()

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
        <body style="margin: 0; padding: 0; background-color: #f6f9fc; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased;">
            
            <div style="display: none; max-height: 0px; overflow: hidden;">
                Uma nova encomenda para $recipientName acabou de chegar na portaria.
            </div>
    
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f6f9fc;">
                <tr>
                    <td align="center" style="padding: 40px 10px;">
                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; margin: 0 auto;">
                            
                            <tr>
                                <td align="center" style="background-color: #ffffff; padding: 40px 0 20px 0; border-bottom: 1px solid #f0f0f0;">
                                    <div style="background-color: #4F46E5; width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                                        <span style="font-size: 28px; line-height: 50px;">📦</span>
                                    </div>
                                    <h2 style="margin: 10px 0 0 0; color: #1a1a1a; font-size: 22px; letter-spacing: -0.5px;">LOBBY APP</h2>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="padding: 40px 30px; text-align: center;">
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
    
                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" align="center" style="margin: 0 auto;">
                                        <tr>
                                            <td align="center" style="background-color: #2d3748; color: #ffffff; font-family: 'Courier New', monospace; font-size: 32px; font-weight: bold; letter-spacing: 4px; padding: 20px; border-radius: 8px; border: 2px solid #4a5568;">
                                                $trackingCode
                                            </td>
                                        </tr>
                                    </table>
                                    <div style="height: 30px;"></div>
    
                                    <p style="color: #e53e3e; font-weight: 600; font-size: 14px; background-color: #fff5f5; padding: 10px; border-radius: 6px; display: inline-block;">
                                        ⚠️ A portaria está aguardando a retirada.
                                    </p>
                                    
                                    <p style="color: #718096; font-size: 13px; margin-top: 20px;">
                                        Dica: Leve um documento com foto caso o porteiro solicite.
                                    </p>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #f0f0f0;">
                                    <p style="color: #a0aec0; font-size: 12px; margin: 0;">
                                        © 2025 Lobby App - Gestão Inteligente de Condomínios<br>
                                        Não responda a este e-mail automático.
                                    </p>
                                </td>
                            </tr>
                        </table>
                        
                        </td>
                </tr>
            </table>
        </body>
        </html>
    """.trimIndent()

        val emailRequest = mapOf(
            "from" to "Lobby App <onboarding@resend.dev>",
            "to" to listOf(email),
            "subject" to "📦 Chegou uma encomenda para você!",
            "html" to htmlContent
        )

        try {
            logger.info("Tentando enviar email para: $email via API Spring...")

            val response = restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(emailRequest)
                .retrieve()
                .toBodilessEntity()

            logger.info("✅ Sucesso! Status Code: ${response.statusCode}")

        } catch (e: Exception) {
            logger.error("❌ Falha crítica ao enviar email via API: ${e.message}", e)
        }
    }
}