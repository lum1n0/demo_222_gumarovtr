package com.example.demo_gumarov

import com.example.demo_gumarov.model.Room
import com.example.demo_gumarov.model.User
import com.example.demo_gumarov.repository.RoomRepository
import com.example.demo_gumarov.repository.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInit {

    @Bean
    fun initData(
        userRepository: UserRepository,
        roomRepository: RoomRepository,
        passwordEncoder: PasswordEncoder
    ) = ApplicationRunner {
        if (!userRepository.existsByLogin("Admin26")) {
            userRepository.save(
                User(
                    fullName = "Администратор портала",
                    login = "Admin26",
                    password = passwordEncoder.encode("Demo20"),
                    phone = "+7 (000) 000-00-00",
                    email = "admin@conference.local",
                    role = "ADMIN"
                )
            )
        }

        val rooms = listOf(
            Room(
                name = "Аудитория",
                type = "аудитория",
                description = "Компактное помещение для лекций, семинаров и учебных встреч.",
                capacity = 40
            ),
            Room(
                name = "Коворкинг",
                type = "коворкинг",
                description = "Гибкое пространство для рабочих сессий, мозговых штурмов и встреч команд.",
                capacity = 30
            ),
            Room(
                name = "Кинозал",
                type = "кинозал",
                description = "Зал с большим экраном для презентаций, показов и публичных выступлений.",
                capacity = 120
            ),
            Room(
                name = "Конференц-зал",
                type = "конференц-зал",
                description = "Представительный зал для деловых конференций и крупных мероприятий.",
                capacity = 80
            )
        )

        rooms
            .filterNot { roomRepository.existsByName(it.name) }
            .forEach(roomRepository::save)
    }
}
