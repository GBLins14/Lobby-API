package com.lobby.services

import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class ForgotPasswordService(
    private val mailSender: JavaMailSender,
    @Value("\${app.password-recovery.token-expiration-minutes}") private val TOKEN_EXPIRATION_MINUTES: Long,
) {

    fun send(email: String, username: String, link: String) {
        val message: MimeMessage = mailSender.createMimeMessage()

        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom("onboarding@resend.dev")
        helper.setTo(email)
        helper.setSubject("Lobby: Recuperação de senha")

        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                /* Reset básico */
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f6f9fc; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
                
                /* Container Principal */
                .wrapper { width: 100%; background-color: #f6f9fc; padding: 40px 0; }
                .container { max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #e1e4e8; }
                
                /* Header */
                .header { background-color: #ffffff; padding: 30px 20px 10px 20px; text-align: center; border-bottom: 1px solid #f0f0f0; }
                .app-name { color: #1a1a1a; font-size: 24px; font-weight: 800; letter-spacing: -0.5px; margin: 0; }
                .icon { font-size: 28px; vertical-align: middle; margin-right: 5px; }
    
                /* Conteúdo */
                .content { padding: 40px 30px; text-align: center; color: #4a5568; }
                h1 { margin: 0 0 20px 0; color: #1a202c; font-size: 22px; font-weight: 700; }
                p { font-size: 16px; line-height: 1.6; margin-bottom: 25px; color: #4a5568; }
                
                /* O Botão Mágico */
                .btn-container { margin: 35px 0; }
                .btn { background-color: #4F46E5; color: #ffffff !important; font-size: 16px; font-weight: 600; padding: 14px 32px; border-radius: 8px; text-decoration: none; display: inline-block; box-shadow: 0 4px 6px rgba(79, 70, 229, 0.2); transition: background-color 0.2s; }
                .btn:hover { background-color: #4338ca; }
                
                /* Link Secundário (Fallback) */
                .fallback-link { font-size: 12px; color: #718096; margin-top: 20px; word-break: break-all; }
                .fallback-link a { color: #4F46E5; text-decoration: none; }
                
                /* Aviso */
                .warning { background-color: #fff5f5; color: #c53030; font-size: 14px; padding: 12px; border-radius: 6px; margin-top: 30px; display: inline-block; }
    
                /* Footer */
                .footer { background-color: #f6f9fc; padding: 20px; text-align: center; font-size: 12px; color: #a0aec0; }
            </style>
        </head>
        <body>
            <div class="wrapper">
                <div class="container">
                    <div class="header">
                        <h2 class="app-name"><span class="icon">🏢</span> LOBBY</h2>
                    </div>
    
                    <div class="content">
                        <h1>Recuperação de Senha</h1>
                        
                        <p>Olá, <strong>$username</strong>!</p>
                        
                        <p>Recebemos uma solicitação para redefinir a senha da sua conta no Lobby App. Para prosseguir, clique no botão abaixo:</p>
    
                        <div class="btn-container">
                            <a href="$link" class="btn" target="_blank">Redefinir Minha Senha</a>
                        </div>
    
                        <p>Este link expira em $TOKEN_EXPIRATION_MINUTES minutos por motivos de segurança.</p>
    
                        <div class="warning">
                            Se não foi você quem solicitou, nenhuma ação é necessária. Sua conta está segura.
                        </div>
    
                        <div class="fallback-link">
                            <p>O botão não funcionou? Copie e cole o link abaixo no seu navegador:<br>
                            <a href="$link">$link</a></p>
                        </div>
                    </div>
                </div>
                
                <div class="footer">
                    <p>&copy; 2025 Lobby App. Todos os direitos reservados.<br>
                    Este é um e-mail automático, por favor não responda.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

        helper.setText(htmlContent, true)

        mailSender.send(message)
    }
}