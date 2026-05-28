package com.tourly.core.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {

    fun sendVerificationCode(to: String, code: String) {
        val subject = "Your Tourly Verification Code"
        
        // Prepare Thymeleaf context
        val context = Context().apply {
            setVariable("code", code)
        }
        
        // Render HTML content using the template
        val htmlContent = templateEngine.process("email-verification", context)

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")
        
        helper.setText(htmlContent, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setFrom("no-reply@tourly.com")

        mailSender.send(mimeMessage)
    }
}
