package com.dreamsbo.posapi.security.service

import java.util.UUID

open class ClientAuthorization {

    internal lateinit var userId: UUID
    internal lateinit var username: String
}
