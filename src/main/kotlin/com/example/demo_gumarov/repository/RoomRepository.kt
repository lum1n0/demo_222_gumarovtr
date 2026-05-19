package com.example.demo_gumarov.repository

import com.example.demo_gumarov.model.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long> {
    fun existsByName(name: String): Boolean
}