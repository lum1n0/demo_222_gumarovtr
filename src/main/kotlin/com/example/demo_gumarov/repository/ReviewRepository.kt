package com.example.demo_gumarov.repository

import com.example.demo_gumarov.model.Request
import com.example.demo_gumarov.model.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {
    fun existsByRequest(request: Request): Boolean
    fun findByRequest(request: Request): Review?
}