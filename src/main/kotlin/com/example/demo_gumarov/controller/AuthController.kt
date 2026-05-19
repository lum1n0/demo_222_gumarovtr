package com.example.demo_gumarov.controller

import com.example.demo_gumarov.model.User
import com.example.demo_gumarov.repository.UserRepository
import jakarta.servlet.http.HttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val loginPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")

    // открыть index.html
    @GetMapping("/")
    fun homePage(): String = "index"

    // открыть register.html
    @GetMapping("/register")
    fun registerPage(model: Model): String {
        model.addAttribute("values", emptyMap<String, String>())
        return "register"
    }

    // htubcnhfwbz
    @PostMapping("/register")
    fun register(
        @RequestParam fullName: String,
        @RequestParam phone: String,
        @RequestParam email: String,
        @RequestParam login: String,
        @RequestParam password: String,
        model: Model
    ): String {
        val normalizedPhone = normalizePhone(phone)
        val values = mapOf(
            "fullName" to fullName.trim(),
            "phone" to phone.trim(),
            "email" to email.trim(),
            "login" to login.trim()
        )
        val errors = mutableMapOf<String, String>()

        if (fullName.isBlank()) errors["fullName"] = "Поле обязательно для заполнения"
        if (phone.isBlank()) {
            errors["phone"] = "Поле обязательно для заполнения"
        } else if (normalizedPhone == null) {
            errors["phone"] = "Введите номер в формате +7 (999) 123-45-67"
        }
        if (email.isBlank()) {
            errors["email"] = "Поле обязательно для заполнения"
        } else if (!email.contains("@") || !email.contains(".")) {
            errors["email"] = "Укажите корректный email"
        }
        if (login.isBlank()) {
            errors["login"] = "Поле обязательно для заполнения"
        } else if (login.trim().length < 6) {
            errors["login"] = "Логин должен содержать минимум 6 символов"
        } else if (!loginPattern.matches(login.trim())) {
            errors["login"] = "Логин должен содержать латинские буквы и цифры"
        } else if (userRepository.existsByLogin(login.trim())) {
            errors["login"] = "Логин уже занят"
        }
        if (password.isBlank()) {
            errors["password"] = "Поле обязательно для заполнения"
        } else if (password.length < 8) {
            errors["password"] = "Пароль должен содержать минимум 8 символов"
        }

        if (errors.isNotEmpty()) {
            model.addAttribute("errors", errors)
            model.addAttribute("values", values)
            model.addAttribute("error", "Проверьте поля формы")
            return "register"
        }

        userRepository.save(
            User(
                fullName = fullName.trim(),
                phone = normalizedPhone!!,
                email = email.trim(),
                login = login.trim(),
                password = passwordEncoder.encode(password),
                role = "USER"
            )
        )
        return "redirect:/login?registered"
    }

    // открыть login.html
    @GetMapping("/login")
    fun loginPage(
        @RequestParam(required = false) registered: String?,
        @RequestParam(required = false) error: String?,
        @RequestParam(required = false) logout: String?,
        model: Model
    ): String {
        if (registered != null) model.addAttribute("success", "Регистрация успешно завершена")
        if (logout != null) model.addAttribute("success", "Вы вышли из системы")
        if (error != null) model.addAttribute("error", "Неверный логин или пароль")
        return "login"
    }

    // логин запрос
    @PostMapping("/login")
    fun login(
        @RequestParam login: String,
        @RequestParam password: String,
        session: HttpSession
    ): String {
        val user = userRepository.findByLogin(login.trim())
        if (login.isBlank() || password.isBlank() || user == null || !passwordEncoder.matches(password, user.password)) {
            return "redirect:/login?error"
        }

        session.setAttribute("userId", user.id)
        session.setAttribute("userRole", user.role)
        return if (user.role == "ADMIN") "redirect:/admin?loginSuccess" else "redirect:/requests?loginSuccess"
    }

    // логаут
    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login?logout"
    }

    // валидация телефона
    private fun normalizePhone(phone: String): String? {
        var digits = phone.filter(Char::isDigit)
        if (digits.length == 11 && digits.startsWith("8")) {
            digits = "7" + digits.drop(1)
        }
        if (digits.length != 11 || !digits.startsWith("7")) {
            return null
        }
        return "+7 (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7, 9)}-${digits.substring(9, 11)}"
    }
}
