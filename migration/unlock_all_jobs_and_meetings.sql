-- Desbloquear todos los trabajos y reuniones que hayan sido bloqueados previamente
-- por la tarea programada o por el antiguo límite de 2 días

UPDATE pos.job 
SET locked = false, updated_at = NOW() 
WHERE locked = true;

UPDATE pos.meeting 
SET locked = false, updated_at = NOW() 
WHERE locked = true;
