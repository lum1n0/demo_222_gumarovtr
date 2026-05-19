package com.example.demo_gumarov.model

import jakarta.persistence.*

@Entity
@Table(name = "rooms")
data class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String = "",

    @Column(nullable = false)
    val type: String = "",

    @Column(nullable = false, length = 1000)
    val description: String = "",

    @Column(nullable = false)
    val capacity: Int = 0
)