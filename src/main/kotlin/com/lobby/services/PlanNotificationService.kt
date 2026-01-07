package com.lobby.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Service
class PlanNotificationService(
    @Value("\${resend.resend-api-key}") private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(PlanNotificationService::class.java)

    private val restClient = RestClient.builder()
        .baseUrl("https://api.resend.com")
        .defaultHeader("Authorization", "Bearer $apiKey")
        .build()

    @Async
    fun sendPaymentSuccess(email: String, username: String, planName: String, dashboardUrl: String) {
        val htmlContent = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Pagamento Confirmado: Lobby</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f6f9fc; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased;">
            
            <div style="display: none; max-height: 0px; overflow: hidden;">
                Seu pagamento foi confirmado com sucesso. Seu acesso ao plano $planName está liberado.
            </div>
    
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f6f9fc;">
                <tr>
                    <td align="center" style="padding: 40px 10px;">
                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e1e4e8; margin: 0 auto;">
                            
                            <tr>
                                <td align="center" style="background-color: #ffffff; padding: 40px 0 20px 0; border-bottom: 1px solid #f0f0f0;">
                                    
                                    <div style="background-color: #F0FDF4; width: 60px; height: 60px; border-radius: 50%; margin: 0 auto 15px auto; border: 1px solid #BBF7D0;">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
                                            <tr>
                                                <td align="center" valign="middle">
                                                    <span style="font-size: 28px; line-height: 1; display: block;">✅</span>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                            
                                    <h2 style="margin: 5px 0 0 0; color: #1a1a1a; font-size: 22px; letter-spacing: -0.5px;">Pagamento Confirmado!</h2>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="padding: 40px 30px; text-align: left;">
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 20px;">
                                        Olá, <strong>$username</strong>.
                                    </p>
                                    
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 20px;">
                                        Temos ótimas notícias! O pagamento da sua assinatura do plano <strong>$planName</strong> foi processado com sucesso.
                                    </p>
                                    
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                                        Todos os recursos do seu plano já estão ativos e prontos para uso no seu condomínio.
                                    </p>
    
                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 35px 0;">
                                        <tr>
                                            <td align="center">
                                                <a href="$dashboardUrl" target="_blank" style="background-color: #C084FC; color: #ffffff; font-size: 16px; font-weight: 600; padding: 14px 32px; border-radius: 8px; text-decoration: none; display: inline-block; box-shadow: 0 4px 6px rgba(192, 132, 252, 0.25);">
                                                    Acessar Plataforma
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
    
                                    <div style="background-color: #F0FDF4; border-left: 4px solid #4ADE80; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
                                        <p style="color: #166534; font-size: 13px; margin: 0; line-height: 1.5;">
                                            <strong>Nota fiscal:</strong> Se precisar do comprovante, você pode baixá-lo diretamente na área de faturas nas configurações da sua conta.
                                        </p>
                                    </div>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #f0f0f0;">
                                    <p style="color: #a0aec0; font-size: 12px; margin: 0;">
                                        © 2025 Lobby App<br>
                                        Obrigado por confiar no Lobby.
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
            "from" to "Lobby Financeiro <billing@resend.dev>",
            "to" to listOf(email),
            "subject" to "Lobby: Pagamento confirmado!",
            "html" to htmlContent
        )

        try {
            restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(emailRequest)
                .retrieve()
                .toBodilessEntity()

        } catch (e: Exception) {
            logger.error("❌ Erro ao enviar confirmação de pagamento: ${e.message}", e)
        }
    }

    @Async
    fun sendPaymentFailure(email: String, username: String, planName: String, billingUrl: String) {
        val htmlContent = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Ação Necessária: Assinatura Lobby</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #f6f9fc; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased;">
            
            <div style="display: none; max-height: 0px; overflow: hidden;">
                Não conseguimos processar a renovação do seu plano. Atualize seus dados para manter o acesso.
            </div>
    
            <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #f6f9fc;">
                <tr>
                    <td align="center" style="padding: 40px 10px;">
                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e1e4e8; margin: 0 auto;">
                            
                            <tr>
                                <td align="center" style="background-color: #ffffff; padding: 40px 0 20px 0; border-bottom: 1px solid #f0f0f0;">
                                    
                                    <div style="background-color: #FFF7ED; width: 60px; height: 60px; border-radius: 50%; margin: 0 auto 15px auto; border: 1px solid #FFEDD5;">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
                                            <tr>
                                                <td align="center" valign="middle">
                                                    <span style="font-size: 28px; line-height: 1; display: block;">💳</span>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                            
                                    <h2 style="margin: 5px 0 0 0; color: #1a1a1a; font-size: 22px; letter-spacing: -0.5px;">Pagamento Pendente</h2>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="padding: 40px 30px; text-align: left;">
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 20px;">
                                        Olá, <strong>$username</strong>.
                                    </p>
                                    
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 20px;">
                                        Tivemos um problema ao processar a renovação da sua assinatura do plano <strong>$planName</strong>. Isso geralmente acontece por cartão expirado ou falta de limite temporária.
                                    </p>
                                    
                                    <p style="color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                                        Para evitar a interrupção dos serviços do seu condomínio e o bloqueio de novas entregas, por favor, atualize sua forma de pagamento.
                                    </p>
    
                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="margin: 35px 0;">
                                        <tr>
                                            <td align="center">
                                                <a href="$billingUrl" target="_blank" style="background-color: #C084FC; color: #ffffff; font-size: 16px; font-weight: 600; padding: 14px 32px; border-radius: 8px; text-decoration: none; display: inline-block; box-shadow: 0 4px 6px rgba(192, 132, 252, 0.25);">
                                                    Atualizar Pagamento
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
    
                                    <div style="background-color: #F8FAFC; border-left: 4px solid #CBD5E0; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
                                        <p style="color: #4A5568; font-size: 13px; margin: 0; line-height: 1.5;">
                                            <strong>Nota:</strong> Tentaremos processar o pagamento novamente nos próximos dias. Seus dados e históricos continuam salvos e seguros.
                                        </p>
                                    </div>
                                </td>
                            </tr>
    
                            <tr>
                                <td align="center" style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #f0f0f0;">
                                    <p style="color: #a0aec0; font-size: 12px; margin: 0;">
                                        © 2025 Lobby App - Financeiro<br>
                                        Precisa de ajuda? Responda este e-mail.
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
            "from" to "Lobby <nao-responda@mail.uselobby.com.br>",
            "to" to listOf(email),
            "subject" to "Lobby: Ação necessária na sua assinatura",
            "html" to htmlContent
        )

        try {
            restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(emailRequest)
                .retrieve()
                .toBodilessEntity()

        } catch (e: Exception) {
            logger.error("❌ Erro ao enviar aviso de pagamento: ${e.message}", e)
        }
    }
}