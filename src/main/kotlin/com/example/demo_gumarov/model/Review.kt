package com.example.demo.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @OneToOne
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    val request: Request = Request(),

    @Column(nullable = false, length = 2000)
    val text: String = "",

    @Column(nullable = false)
    val rating: Int = 5,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)