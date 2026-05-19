package com.example.demo_gumarov.controller


import com.example.demo_gumarov.model.Request
import com.example.demo_gumarov.model.RequestStatus
import com.example.demo_gumarov.model.Room
import com.example.demo_gumarov.repository.RequestRepository
import com.example.demo_gumarov.repository.ReviewRepository
import com.example.demo_gumarov.repository.RoomRepository
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AdminController(
    private val requestRepository: RequestRepository,
    private val roomRepository: RoomRepository,
    private val reviewRepository: ReviewRepository
) {
    private val pageSize = 8

    // панель администратора целикои
    @GetMapping("/admin")
    fun adminPage(
        session: HttpSession,
        model: Model,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) roomId: Long?,
        @RequestParam(defaultValue = "createdDesc") sort: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) loginSuccess: String?,
        @RequestParam(required = false) statusChanged: String?,
        @RequestParam(required = false) statusError: String?,
        @RequestParam(required = false) roomCreated: String?,
        @RequestParam(required = false) roomDeleted: String?,
        @RequestParam(required = false) roomError: String?
    ): String {
        if (!isAdmin(session)) return "redirect:/login"

        fillAdminModel(model, status, roomId, sort, page)
        if (loginSuccess != null) model.addAttribute("success", "Вход выполнен успешно")
        if (statusChanged != null) model.addAttribute("success", "Статус заявки изменён")
        if (statusError != null) model.addAttribute("error", "Ошибка при изменении статуса")
        if (roomCreated != null) model.addAttribute("success", "Помещение успешно создано")
        if (roomDeleted != null) model.addAttribute("success", "Помещение удалено")
        if (roomError != null) model.addAttribute("error", roomErrorMessage(roomError))
        return "admin"
    }

    // изменяет статус заявки
    @PostMapping("/admin/status")
    fun updateStatus(
        @RequestParam id: Long,
        @RequestParam status: String,
        session: HttpSession
    ): String {
        if (!isAdmin(session)) return "redirect:/login"
        if (status !in RequestStatus.ALL) return "redirect:/admin?statusError"

        val request = requestRepository.findById(id).orElse(null) ?: return "redirect:/admin?statusError"
        request.status = status
        request.statusChanged = true
        requestRepository.save(request)
        return "redirect:/admin?statusChanged"
    }

    // создание помещений
    @PostMapping("/admin/rooms")
    fun createRoom(
        @RequestParam name: String,
        @RequestParam type: String,
        @RequestParam description: String,
        @RequestParam capacity: String,
        session: HttpSession
    ): String {
        if (!isAdmin(session)) return "redirect:/login"

        val parsedCapacity = capacity.toIntOrNull()
        if (name.isBlank() || type.isBlank() || description.isBlank() || capacity.isBlank()) {
            return "redirect:/admin?roomError=empty"
        }
        if (parsedCapacity == null || parsedCapacity <= 0) {
            return "redirect:/admin?roomError=capacity"
        }
        if (roomRepository.existsByName(name.trim())) {
            return "redirect:/admin?roomError=exists"
        }

        roomRepository.save(
            Room(
                name = name.trim(),
                type = type.trim(),
                description = description.trim(),
                capacity = parsedCapacity
            )
        )
        return "redirect:/admin?roomCreated"
    }

    // удаление помещение
    @PostMapping("/admin/rooms/delete")
    fun deleteRoom(
        @RequestParam id: Long,
        session: HttpSession
    ): String {
        if (!isAdmin(session)) return "redirect:/login"

        val room = roomRepository.findById(id).orElse(null) ?: return "redirect:/admin?roomError=notFound"
        if (requestRepository.existsByRoom(room)) {
            return "redirect:/admin?roomError=used"
        }

        roomRepository.delete(room)
        return "redirect:/admin?roomDeleted"
    }

    // данные для admin.html
    private fun fillAdminModel(
        model: Model,
        status: String?,
        roomId: Long?,
        sort: String,
        page: Int
    ) {
        val allRequests = requestRepository.findAll()
        val rooms = roomRepository.findAll()
        val filtered = allRequests
            .asSequence()
            .filter { status.isNullOrBlank() || it.status == status }
            .filter { roomId == null || it.room.id == roomId }
            .toList()
            .sortedByMode(sort)

        val totalPages = (filtered.size + pageSize - 1) / pageSize
        val safePage = page.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        val pageItems = filtered.drop(safePage * pageSize).take(pageSize)
        val reviewsByRequestId = reviewRepository.findAll().associateBy { it.request.id }

        model.addAttribute("requests", pageItems)
        model.addAttribute("reviewsByRequestId", reviewsByRequestId)
        model.addAttribute("rooms", rooms)
        model.addAttribute("roomRequestCounts", rooms.associate { room ->
            room.id to allRequests.count { it.room.id == room.id }
        })
        model.addAttribute("statuses", RequestStatus.ALL)
        model.addAttribute("selectedStatus", status ?: "")
        model.addAttribute("selectedRoomId", roomId)
        model.addAttribute("sort", sort)
        model.addAttribute("page", safePage)
        model.addAttribute("totalPages", totalPages.coerceAtLeast(1))
        model.addAttribute("countAll", allRequests.size)
        model.addAttribute("countNew", allRequests.count { it.status == RequestStatus.NEW })
        model.addAttribute("countScheduled", allRequests.count { it.status == RequestStatus.SCHEDULED })
        model.addAttribute("countCompleted", allRequests.count { it.status == RequestStatus.COMPLETED })
    }

    // текст ошибки
    private fun roomErrorMessage(code: String): String =
        when (code) {
            "empty" -> "Заполните все поля помещения"
            "capacity" -> "Вместимость должна быть положительным числом"
            "exists" -> "Помещение с таким названием уже существует"
            "used" -> "Нельзя удалить помещение, которое уже используется в заявках"
            "notFound" -> "Помещение не найдено"
            else -> "Ошибка при работе с помещением"
        }

    // проверка на админа
    private fun isAdmin(session: HttpSession): Boolean =
        session.getAttribute("userRole") == "ADMIN"

    // сортировка заявок
    private fun List<Request>.sortedByMode(sort: String): List<Request> =
        when (sort) {
            "createdAsc" -> sortedBy { it.createdAt }
            "dateAsc" -> sortedWith(compareBy<Request> { it.conferenceDate }.thenBy { it.startTime })
            "dateDesc" -> sortedWith(compareByDescending<Request> { it.conferenceDate }.thenByDescending { it.startTime })
            "status" -> sortedBy { it.status }
            "user" -> sortedBy { it.user.fullName.lowercase() }
            else -> sortedByDescending { it.createdAt }
        }
}
