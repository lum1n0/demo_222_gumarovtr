package com.example.demo_gumarov.controller

import com.example.demo_gumarov.model.Request
import com.example.demo_gumarov.model.RequestStatus
import com.example.demo_gumarov.model.Review
import com.example.demo_gumarov.model.User
import com.example.demo_gumarov.repository.RequestRepository
import com.example.demo_gumarov.repository.ReviewRepository
import com.example.demo_gumarov.repository.RoomRepository
import com.example.demo_gumarov.repository.UserRepository
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.LocalTime

@Controller
class RequestController(
    private val requestRepository: RequestRepository,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val reviewRepository: ReviewRepository
) {
    private val paymentMethods = listOf("наличными", "переводом", "банковской картой")

    // мои заявки
    @GetMapping("/requests")
    fun requestsPage(
        session: HttpSession,
        model: Model,
        @RequestParam(required = false) loginSuccess: String?,
        @RequestParam(required = false) created: String?,
        @RequestParam(required = false) reviewSuccess: String?,
        @RequestParam(required = false) reviewError: String?
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val requests = requestRepository.findByUserOrderByCreatedAtDesc(user)
        val reviewedRequestIds = java.util.HashSet(
            requests
                .filter { reviewRepository.existsByRequest(it) }
                .map { it.id }
        )

        model.addAttribute("requests", requests)
        model.addAttribute("reviewedRequestIds", reviewedRequestIds)
        model.addAttribute("user", user)
        if (loginSuccess != null) model.addAttribute("success", "Вход выполнен успешно")
        if (created != null) model.addAttribute("success", "Заявка успешно создана")
        if (reviewSuccess != null) model.addAttribute("success", "Отзыв успешно отправлен")
        if (reviewError != null) {
            val message = when (reviewError) {
                "empty" -> "Заполните текст отзыва"
                "exists" -> "Отзыв уже отправлен"
                else -> "Отзыв пока недоступен для этой заявки"
            }
            model.addAttribute("error", message)
        }
        return "requests"
    }

    // показать new-request.html
    @GetMapping("/requests/new")
    fun newRequestPage(session: HttpSession, model: Model): String {
        currentUser(session) ?: return "redirect:/login"
        fillRequestFormModel(model)
        return "new-request"
    }

    // создание заявки
    @PostMapping("/requests/new")
    fun createRequest(
        @RequestParam roomId: String,
        @RequestParam conferenceDate: String,
        @RequestParam startTime: String,
        @RequestParam paymentMethod: String,
        session: HttpSession,
        model: Model
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val errors = mutableMapOf<String, String>()

        val room = roomId.toLongOrNull()?.let { roomRepository.findById(it).orElse(null) }
        if (room == null) errors["roomId"] = "Поле обязательно для заполнения"

        val parsedDate = runCatching { LocalDate.parse(conferenceDate) }.getOrNull()
        if (parsedDate == null) {
            errors["conferenceDate"] = "Выберите дату проведения конференции"
        } else if (parsedDate.isBefore(LocalDate.now())) {
            errors["conferenceDate"] = "Дата конференции не может быть в прошлом"
        }

        val parsedTime = runCatching { LocalTime.parse(startTime) }.getOrNull()
        if (parsedTime == null) errors["startTime"] = "Выберите время начала конференции"

        if (paymentMethod !in paymentMethods) errors["paymentMethod"] = "Выберите способ оплаты"

        if (errors.isNotEmpty()) {
            fillRequestFormModel(model)
            model.addAttribute("errors", errors)
            model.addAttribute("selectedRoomId", roomId.toLongOrNull())
            model.addAttribute("selectedTime", startTime)
            model.addAttribute("selectedPaymentMethod", paymentMethod)
            model.addAttribute("conferenceDateValue", conferenceDate)
            model.addAttribute("error", "Заполните все обязательные поля")
            return "new-request"
        }

        requestRepository.save(
            Request(
                user = user,
                room = room!!,
                conferenceDate = parsedDate!!,
                startTime = parsedTime!!,
                paymentMethod = paymentMethod,
                status = RequestStatus.NEW
            )
        )
        return "redirect:/requests?created"
    }

    // создать отзыв
    @PostMapping("/reviews")
    fun createReview(
        @RequestParam requestId: Long,
        @RequestParam text: String,
        @RequestParam rating: Int,
        session: HttpSession
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val request = requestRepository.findById(requestId).orElse(null)
            ?: return "redirect:/requests?reviewError=unavailable"

        if (request.user.id != user.id || !request.statusChanged || request.status == RequestStatus.NEW) {
            return "redirect:/requests?reviewError=unavailable"
        }
        if (text.isBlank()) {
            return "redirect:/requests?reviewError=empty"
        }
        if (reviewRepository.existsByRequest(request)) {
            return "redirect:/requests?reviewError=exists"
        }

        reviewRepository.save(
            Review(
                user = user,
                request = request,
                text = text.trim(),
                rating = rating.coerceIn(1, 5)
            )
        )
        return "redirect:/requests?reviewSuccess"
    }

    // данные для заявки
    private fun fillRequestFormModel(model: Model) {
        model.addAttribute("rooms", roomRepository.findAll())
        model.addAttribute("paymentMethods", paymentMethods)
        model.addAttribute("times", (8..21).map { "%02d:00".format(it) })
        model.addAttribute("selectedRoomId", null)
        model.addAttribute("selectedTime", "")
        model.addAttribute("selectedPaymentMethod", "")
        model.addAttribute("conferenceDateValue", "")
    }

    // получить пользователя
    private fun currentUser(session: HttpSession): User? {
        val userId = session.getAttribute("userId") as? Long ?: return null
        return userRepository.findById(userId).orElse(null)
    }
}
