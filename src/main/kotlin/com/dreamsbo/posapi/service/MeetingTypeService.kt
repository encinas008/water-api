package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.MeetingTypeOutputDto
import com.dreamsbo.posapi.persistence.repository.MeetingTypeRepository
import org.springframework.stereotype.Service

@Service
class MeetingTypeService(
    private val meetingTypeRepository: MeetingTypeRepository
) {
    fun findAll(): List<MeetingTypeOutputDto> {
        return meetingTypeRepository.findAllByActive(true)
            .map { MeetingTypeOutputDto(it.id, it.code, it.name, it.active) }
    }
}


