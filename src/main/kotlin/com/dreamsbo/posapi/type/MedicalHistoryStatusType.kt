package com.dreamsbo.posapi.type

enum class MedicalHistoryStatusType(val statusText: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("EnProgreso"),
    COMPLETED("Atendido"),
    CLOSED_BY_SYSTEM("CerradoPorSistema")
}
