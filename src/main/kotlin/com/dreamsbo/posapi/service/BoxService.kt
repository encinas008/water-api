package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.BoxOutputDto
import com.dreamsbo.posapi.persistence.repository.BoxRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class BoxService(
    private val boxRepository: BoxRepository,
) {

    fun findAll(): List<BoxOutputDto> {

        val boxes = mutableListOf<BoxOutputDto>()

        boxRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {

            boxes.add(
                BoxOutputDto(
                    id = it.id,
                    name = it.name,
                    documentIdentifier = it.user.profile.dni,
                    assignee = "${it.user.profile.name} ${it.user.profile.lastname}",
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active,
                )
            )
        }

        return boxes
    }
}
