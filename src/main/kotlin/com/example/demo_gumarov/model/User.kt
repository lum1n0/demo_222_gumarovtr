package com.example.demo_gumarov.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val fullName: String = "",

    @Column(nullable = false, unique = true)
    val login: String = "",

    @Column(nullable = false)
    val password: String? = "",

    @Column(nullable = false)
    val phone: String = "",

    @Column(nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val role: String = "USER"
)