package com.example.demo_gumarov.repository

import com.example.demo_gumarov.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): User?
    fun existsByLogin(login: String): Boolean
}