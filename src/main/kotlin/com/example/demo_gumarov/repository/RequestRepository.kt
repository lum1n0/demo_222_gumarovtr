package com.example.demo_gumarov.repository

import com.example.demo_gumarov.model.Request
import com.example.demo_gumarov.model.Room
import com.example.demo_gumarov.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface RequestRepository : JpaRepository<Request, Long> {
    fun findByUserOrderByCreatedAtDesc(user: User): List<Request>
    fun existsByRoom(room: Room): Boolean
}