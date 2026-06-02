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
        sendEmail(
            to = to,
            subject = "Your Tourly Verification Code",
            templateName = "email-verification",
            variables = mapOf("code" to code)
        )
    }

    fun sendPasswordResetCode(to: String, code: String) {
        sendEmail(
            to = to,
            subject = "Your Tourly Password Reset Code",
            templateName = "email-password-reset",
            variables = mapOf("code" to code)
        )
    }

    private fun sendEmail(
        to: String,
        subject: String,
        templateName: String,
        variables: Map<String, Any>
    ) {
        val context = Context().apply {
            variables.forEach { (key, value) -> setVariable(key, value) }
        }

        val htmlContent = templateEngine.process(templateName, context)

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")

        helper.setText(htmlContent, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setFrom("no-reply@tourly.com")

        mailSender.send(mimeMessage)
    }
}

