package com.dreamsbo.posapi.controller

import com.dreamsbo.posapi.dto.CommonOutputDto
import com.dreamsbo.posapi.service.CommonService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CommonController(
    val commonService: CommonService,
) {

    @GetMapping("/commons")
    fun findCommonInformation(): CommonOutputDto {

        return commonService.find()
    }
}
