ALTER TABLE pos.partner ADD COLUMN elderly_pays_meeting_fines BOOLEAN DEFAULT TRUE;
ALTER TABLE pos.partner ADD COLUMN elderly_meeting_fine_explanation VARCHAR(1000);
ALTER TABLE pos.partner ADD COLUMN elderly_pays_job_fines BOOLEAN DEFAULT TRUE;
ALTER TABLE pos.partner ADD COLUMN elderly_job_fine_explanation VARCHAR(1000);
