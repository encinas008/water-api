package com.dreamsbo.posapi.service

import com.dreamsbo.posapi.dto.ClientOutputDto
import com.dreamsbo.posapi.persistence.repository.ClientRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ClientsService(
    private val clientRepository: ClientRepository,
) {

    fun findAll(): List<ClientOutputDto> {

        val clients = mutableListOf<ClientOutputDto>()

        clientRepository.findAllByActive(true, Sort.by(Sort.Direction.DESC, "createdAt")).forEach {

            clients.add(
                ClientOutputDto(
                    id = it.id,
                    name = it.fullName,
                    documentIdentifier = it.clientIdentificationNumber,
                    cel = it.cellphone,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    active = it.active
                )
            )
        }

        return clients
    }
}
