package com.example.demo_gumarov.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "requests")
data class Request(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    val room: Room = Room(),

    @Column(nullable = false)
    val conferenceDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val startTime: LocalTime = LocalTime.of(9, 0),

    @Column(nullable = false)
    val paymentMethod: String = "",

    @Column(nullable = false)
    var status: String = RequestStatus.NEW,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var statusChanged: Boolean = false
)

object RequestStatus {
    const val NEW = "Новая"
    const val SCHEDULED = "Мероприятие назначено"
    const val COMPLETED = "Мероприятие завершено"

    val ALL = listOf(NEW, SCHEDULED, COMPLETED)
}