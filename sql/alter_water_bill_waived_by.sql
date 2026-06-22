ALTER TABLE pos.water_bill ADD COLUMN waived_by UUID REFERENCES pos.users(user_id);
