package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.*
import com.dreamsbo.posapi.service.CommonService
import com.dreamsbo.posapi.service.UserService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class AdminController(val userService: UserService, val commonService: CommonService) {

    @PostMapping("/users")
    fun createUser(@RequestBody userInputDto: UserInputDto): UserOutputDto {

        return userService.create(userInputDto)
    }

    @PutMapping("/users/{userId}")
    fun updateUser(
        @PathVariable("userId") userId: UUID,
        @RequestBody userInputDto: UpdateUserInputDto,
    ): UpdateUserOutputDto {

        return userService.update(userId, userInputDto)
    }

    @GetMapping("/users/{userId}")
    fun getUserById(@PathVariable("userId") userId: UUID): UserDetailsOutputDto {

        return userService.getUserById(userId)
    }

    @GetMapping("/users")
    fun getAll(): List<UserDetailsOutputDto> {

        return userService.getAll()
    }

    @PatchMapping("/users/{userId}")
    fun updateUserStatus(
        @PathVariable("userId") userId: UUID,
        @RequestBody userStatus: UpdateUserStatusInputDto,
    ): UpdateUserOutputDto {

        return userService.updateStatus(userId, userStatus)
    }
}
