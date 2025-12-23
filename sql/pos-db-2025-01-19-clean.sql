CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS pos;

CREATE  TABLE pos.abac_function ( 
	function_id          uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar  NOT NULL  ,
	name                 varchar(100)    ,
	description          varchar    ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_tbl_1 PRIMARY KEY ( function_id ),
	CONSTRAINT unq_role_0 UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.abac_role ( 
	role_id              uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar  NOT NULL  ,
	name                 varchar(100)    ,
	description          varchar    ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_tbl PRIMARY KEY ( role_id ),
	CONSTRAINT unq_role UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.abac_role_function ( 
	role_function_id     uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	role_id              uuid  NOT NULL  ,
	function_id          uuid  NOT NULL  ,
	read_enabled         boolean DEFAULT false   ,
	create_enabled       boolean DEFAULT false   ,
	update_enabled       boolean DEFAULT false   ,
	delete_enabled       boolean DEFAULT false   ,
	CONSTRAINT pk_tbl_2 PRIMARY KEY ( role_function_id )
 );

CREATE  TABLE pos.cash_flow_type ( 
	cash_flow_type_id    uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_laboratory_type_11 PRIMARY KEY ( cash_flow_type_id ),
	CONSTRAINT unq_laboratory_type_3 UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.category ( 
	category_id          uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_category PRIMARY KEY ( category_id ),
	CONSTRAINT unq_category UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.civil_status_type ( 
	civil_status_type_id uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_civil_status_type PRIMARY KEY ( civil_status_type_id )
 );

CREATE  TABLE pos.client ( 
	client_id            uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	full_name            varchar(100)    ,
	client_identification_number varchar(20)    ,
	cellphone            varchar(100)    ,
	observation          varchar(500)    ,
	address              varchar(300)    ,
	CONSTRAINT pkey_client PRIMARY KEY ( client_id )
 );

CREATE  TABLE pos.country ( 
	country_id           uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar    ,
	description          varchar    ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL  ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_country PRIMARY KEY ( country_id ),
	CONSTRAINT unq_country UNIQUE ( code ) 
 );

CREATE  TABLE pos.gender_type ( 
	gender_type_id       uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_gender_type PRIMARY KEY ( gender_type_id )
 );

CREATE  TABLE pos.group_blood_type ( 
	group_blood_type_id  uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	description          varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_group_blood_type PRIMARY KEY ( group_blood_type_id )
 );

CREATE  TABLE pos.image ( 
	image_id             uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	url                  varchar    ,
	content_type         varchar    ,
	content_size_kb      varchar    ,
	description          varchar    ,
	CONSTRAINT pk_image PRIMARY KEY ( image_id )
 );

CREATE  TABLE pos.measurement_type ( 
	measurement_type_id  uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_laboratory_type_0 PRIMARY KEY ( measurement_type_id ),
	CONSTRAINT unq_measurement_type UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.order_type ( 
	order_type_id        uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_laboratory_type_12 PRIMARY KEY ( order_type_id ),
	CONSTRAINT unq_laboratory_type_4 UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.payment_type ( 
	payment_type_id      uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_laboratory_type_3 PRIMARY KEY ( payment_type_id ),
	CONSTRAINT unq_laboratory_type_1 UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.product ( 
	product_id           uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	category_id          uuid  NOT NULL  ,
	image_id             uuid    ,
	min_stock            numeric    ,
	max_stock            numeric    ,
	sku                  varchar(50)  NOT NULL  ,
	stock                numeric  NOT NULL  ,
	measurement_type_id  uuid  NOT NULL  ,
	price                numeric    ,
	cost                 numeric    ,
	name                 varchar(100)    ,
	CONSTRAINT pk_product PRIMARY KEY ( product_id ),
	CONSTRAINT unq_product_code UNIQUE ( sku ) 
 );

CREATE  TABLE pos.sale_status ( 
	sale_status_id       uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	CONSTRAINT pk_laboratory_type_7 PRIMARY KEY ( sale_status_id ),
	CONSTRAINT unq_laboratory_type_2 UNIQUE ( code, name ) 
 );

CREATE  TABLE pos.city ( 
	city_id              uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	code                 varchar    ,
	name                 varchar    ,
	description          varchar    ,
	country_id           uuid  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL  ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_city PRIMARY KEY ( city_id ),
	CONSTRAINT unq_city UNIQUE ( code ) 
 );

CREATE  TABLE pos.profile ( 
	profile_id           uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	dni                  varchar    ,
	name                 varchar(100)    ,
	lastname             varchar(100)    ,
	email                varchar    ,
	cellphone            varchar    ,
	telephone            varchar    ,
	cellphone_references varchar    ,
	address              varchar    ,
	birth_date           date    ,
	country_id           uuid  NOT NULL  ,
	city_id              uuid  NOT NULL  ,
	gender_type_id       uuid  NOT NULL  ,
	civil_status_type_id uuid  NOT NULL  ,
	image_id             uuid    ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	occupation           varchar(100)    ,
	CONSTRAINT pk_profile PRIMARY KEY ( profile_id ),
	CONSTRAINT unq_profile UNIQUE ( dni, email ) 
 );

CREATE  TABLE pos.abac_user ( 
	user_id              uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	username             varchar(100)    ,
	passwd               varchar(100)    ,
	profile_id           uuid  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_user PRIMARY KEY ( user_id ),
	CONSTRAINT unq_user UNIQUE ( username ) 
 );

CREATE  TABLE pos.abac_user_role ( 
	user_role_id         uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	user_id              uuid  NOT NULL  ,
	role_id              uuid  NOT NULL  ,
	CONSTRAINT pk_tbl_0 PRIMARY KEY ( user_role_id )
 );

CREATE  TABLE pos.box ( 
	box_id               uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	name                 varchar  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	user_id              uuid  NOT NULL  ,
	CONSTRAINT pk_laboratory_type_8 PRIMARY KEY ( box_id )
 );

CREATE  TABLE pos.cash_balance ( 
	cash_balance_id      uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	box_id               uuid  NOT NULL  ,
	open_time            timestamptz    ,
	close_time           timestamptz    ,
	initial_money        double precision    ,
	cash_in_box          double precision    ,
	cash_difference      double precision    ,
	CONSTRAINT pk_laboratory_type_9 PRIMARY KEY ( cash_balance_id )
 );

CREATE  TABLE pos.cash_flow ( 
	cash_flow_id         uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	description          varchar    ,
	cash_balance_id      uuid  NOT NULL  ,
	payment_type_id      uuid  NOT NULL  ,
	amount               numeric    ,
	cash_flow_type_id    uuid    ,
	CONSTRAINT pk_laboratory_type_10 PRIMARY KEY ( cash_flow_id )
 );

CREATE  TABLE pos.sale ( 
	sale_id              uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	payment_type_id      uuid  NOT NULL  ,
	observation          varchar(300)    ,
	user_id              uuid  NOT NULL  ,
	client_id            uuid  NOT NULL  ,
	sales_status_id      uuid  NOT NULL  ,
	discount             numeric    ,
	quantity_of_products numeric    ,
	sub_total            numeric    ,
	total                numeric    ,
	cash_balance_id      uuid  NOT NULL  ,
	money_to_back        numeric    ,
	order_number         integer,
	order_type_id        uuid    ,
	CONSTRAINT pk_sale_id PRIMARY KEY ( sale_id ),
	CONSTRAINT sale_order_number_key UNIQUE ( order_number ) 
 );

CREATE  TABLE pos.sale_detail ( 
	sale_detail_id       uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	sku                  varchar  NOT NULL  ,
	quantity             numeric    ,
	price                numeric    ,
	discount             numeric    ,
	sub_total            numeric    ,
	name                 varchar(100)    ,
	sale_id              uuid  NOT NULL  ,
	category             varchar(100)    ,
	CONSTRAINT pk_detail_sale_id PRIMARY KEY ( sale_detail_id )
 );

CREATE  TABLE pos.abac_refresh_token ( 
	refresh_token_id     uuid DEFAULT uuid_generate_v4() NOT NULL  ,
	expire_date          timestamp    ,
	user_id              uuid  NOT NULL  ,
	created_at           timestamptz DEFAULT CURRENT_TIMESTAMP   ,
	updated_at           timestamptz    ,
	active               boolean DEFAULT true   ,
	CONSTRAINT pk_user_0 PRIMARY KEY ( refresh_token_id )
 );

ALTER TABLE pos.abac_refresh_token ADD CONSTRAINT fk_abac_refresh_token_abac_user FOREIGN KEY ( user_id ) REFERENCES pos.abac_user( user_id );

ALTER TABLE pos.abac_role_function ADD CONSTRAINT fk_abac_role_function_abac_role FOREIGN KEY ( role_id ) REFERENCES pos.abac_role( role_id );

ALTER TABLE pos.abac_role_function ADD CONSTRAINT fk_abac_role_function_abac_function FOREIGN KEY ( function_id ) REFERENCES pos.abac_function( function_id );

ALTER TABLE pos.abac_user ADD CONSTRAINT fk_usr_profile FOREIGN KEY ( profile_id ) REFERENCES pos.profile( profile_id );

ALTER TABLE pos.abac_user_role ADD CONSTRAINT fk_abac_user_role_usr FOREIGN KEY ( user_id ) REFERENCES pos.abac_user( user_id );

ALTER TABLE pos.abac_user_role ADD CONSTRAINT fk_abac_user_role_abac_role FOREIGN KEY ( role_id ) REFERENCES pos.abac_role( role_id );

ALTER TABLE pos.box ADD CONSTRAINT fk_box_abac_user FOREIGN KEY ( user_id ) REFERENCES pos.abac_user( user_id );

ALTER TABLE pos.cash_balance ADD CONSTRAINT fk_cash_balance_box FOREIGN KEY ( box_id ) REFERENCES pos.box( box_id );

ALTER TABLE pos.cash_flow ADD CONSTRAINT fk_cash_flow_payment_type FOREIGN KEY ( payment_type_id ) REFERENCES pos.payment_type( payment_type_id );

ALTER TABLE pos.cash_flow ADD CONSTRAINT fk_cash_flow_cash_balance FOREIGN KEY ( cash_balance_id ) REFERENCES pos.cash_balance( cash_balance_id );

ALTER TABLE pos.city ADD CONSTRAINT fk_city_country FOREIGN KEY ( country_id ) REFERENCES pos.country( country_id );

ALTER TABLE pos.product ADD CONSTRAINT fk_product_measurement_type FOREIGN KEY ( measurement_type_id ) REFERENCES pos.measurement_type( measurement_type_id );

ALTER TABLE pos.product ADD CONSTRAINT fk_laboratory_image FOREIGN KEY ( image_id ) REFERENCES pos.image( image_id );

ALTER TABLE pos.product ADD CONSTRAINT fk_laboratory_laboratory_type FOREIGN KEY ( category_id ) REFERENCES pos.category( category_id );

ALTER TABLE pos.profile ADD CONSTRAINT fk_profile_image FOREIGN KEY ( image_id ) REFERENCES pos.image( image_id );

ALTER TABLE pos.profile ADD CONSTRAINT fk_profile_gender_type FOREIGN KEY ( gender_type_id ) REFERENCES pos.gender_type( gender_type_id );

ALTER TABLE pos.profile ADD CONSTRAINT fk_profile_country FOREIGN KEY ( country_id ) REFERENCES pos.country( country_id );

ALTER TABLE pos.profile ADD CONSTRAINT fk_profile_civil_status_type FOREIGN KEY ( civil_status_type_id ) REFERENCES pos.civil_status_type( civil_status_type_id );

ALTER TABLE pos.profile ADD CONSTRAINT fk_profile_city FOREIGN KEY ( city_id ) REFERENCES pos.city( city_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_cash_balance FOREIGN KEY ( cash_balance_id ) REFERENCES pos.cash_balance( cash_balance_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_sale_status FOREIGN KEY ( sales_status_id ) REFERENCES pos.sale_status( sale_status_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_client FOREIGN KEY ( client_id ) REFERENCES pos.client( client_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_abac_user FOREIGN KEY ( user_id ) REFERENCES pos.abac_user( user_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_sale_status_type FOREIGN KEY ( payment_type_id ) REFERENCES pos.payment_type( payment_type_id );

ALTER TABLE pos.sale ADD CONSTRAINT fk_sale_order_type FOREIGN KEY ( order_type_id ) REFERENCES pos.order_type( order_type_id );

ALTER TABLE pos.sale_detail ADD CONSTRAINT fk_detail_sale_sale FOREIGN KEY ( sale_id ) REFERENCES pos.sale( sale_id );

INSERT INTO pos.abac_function( function_id, code, name, description, created_at, updated_at, active ) VALUES ( '08638337-c992-4df4-a011-95212952d2b6', 'P', 'Manage Patients', 'Administrar pacientes', '2023-10-28 08:11:08 PM', null, true);
INSERT INTO pos.abac_role( role_id, code, name, description, created_at, updated_at, active ) VALUES ( '5a473b23-a66a-4c76-8860-2261aae65cf8', 'DR', 'DOCTOR', 'Fix health on the patients', '2023-10-23 05:03:41 AM', null, true);
INSERT INTO pos.abac_role( role_id, code, name, description, created_at, updated_at, active ) VALUES ( '7001a19a-377a-42cd-8308-5277cd62919f', 'NU', 'NURSE', 'Help to doctor', '2023-10-29 01:41:58 AM', null, true);
INSERT INTO pos.abac_role( role_id, code, name, description, created_at, updated_at, active ) VALUES ( 'e5025b8a-902c-4cf1-850b-4ca618655502', 'ADMIN', 'ADMIN', 'FULL permissions', '2023-10-29 04:58:19 AM', null, true);
INSERT INTO pos.abac_role_function( role_function_id, created_at, updated_at, active, role_id, function_id, read_enabled, create_enabled, update_enabled, delete_enabled ) VALUES ( '8182af71-8245-4858-a86c-997ac85b6e75', '2023-10-28 08:12:58 PM', null, true, '5a473b23-a66a-4c76-8860-2261aae65cf8', '08638337-c992-4df4-a011-95212952d2b6', true, false, false, false);
INSERT INTO pos.cash_flow_type( cash_flow_type_id, code, name, created_at, updated_at, active, description ) VALUES ( '4873feac-e8aa-435d-9b6a-d5f3432d14d8', 'IN', 'INGRESO', '2025-01-06 09:21:44 AM', null, true, 'Ingreso a cajas');
INSERT INTO pos.cash_flow_type( cash_flow_type_id, code, name, created_at, updated_at, active, description ) VALUES ( '83db6177-683d-49a0-918e-298385776b26', 'OUT', 'EGRESO', '2025-01-06 09:21:44 AM', null, true, 'Salida en cajas');
INSERT INTO pos.category( category_id, code, name, created_at, updated_at, active, description ) VALUES ( '6a627e23-7345-4c96-b033-9a2f749b6034', 'FULL', 'TODAS LAS CATEGORIAS', '2025-01-01 01:19:44 AM', null, true, 'TODAS LAS CATEGORIAS');
INSERT INTO pos.category( category_id, code, name, created_at, updated_at, active, description ) VALUES ( 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'BE', 'BEBIDA', '2025-01-04 01:20:26 AM', null, true, 'BEBIDAS');
INSERT INTO pos.category( category_id, code, name, created_at, updated_at, active, description ) VALUES ( 'eff38cfe-9efa-47c8-ab46-b618f4545ce9', 'BR', 'BROASTER', '2025-01-04 01:19:44 AM', null, true, 'BROASTERS');
INSERT INTO pos.category( category_id, code, name, created_at, updated_at, active, description ) VALUES ( '6c9bc370-f4c9-47ba-9487-a976a7f51c91', 'SP', 'SPIEDO', '2025-01-04 01:19:34 AM', null, true, 'SPIEDOS');
INSERT INTO pos.category( category_id, code, name, created_at, updated_at, active, description ) VALUES ( '3d08308e-3fa4-4eb4-bfb2-3b9e64230a6a', 'HB', 'HAMBURGUESA', '2025-01-04 01:19:56 AM', null, true, 'HAMBURGUESAS');
INSERT INTO pos.civil_status_type( civil_status_type_id, code, name, created_at, updated_at, active ) VALUES ( '45f6bc94-0009-42c3-976b-a4fc0e01931d', 'CA', 'CASADO(A)', '2023-11-27 06:02:05 PM', null, true);
INSERT INTO pos.civil_status_type( civil_status_type_id, code, name, created_at, updated_at, active ) VALUES ( 'ab90a945-783b-42f0-8043-c8910b787f56', 'VI', 'VIUDO(A)', '2023-11-27 06:02:05 PM', null, true);
INSERT INTO pos.civil_status_type( civil_status_type_id, code, name, created_at, updated_at, active ) VALUES ( '1c9cad8f-3bde-4977-a4f0-fa441c26801f', 'DI', 'DIVORCIADO(A)', '2023-11-27 06:02:05 PM', null, true);
INSERT INTO pos.civil_status_type( civil_status_type_id, code, name, created_at, updated_at, active ) VALUES ( '4cbdec33-169d-4c9f-ab1f-8ad4492be5d9', 'CO', 'CONCUBINATO', '2023-11-27 06:02:05 PM', null, true);
INSERT INTO pos.civil_status_type( civil_status_type_id, code, name, created_at, updated_at, active ) VALUES ( '890b281e-4f52-421f-b443-a13092215a33', 'SO', 'SOLTERO(A)', '2023-10-23 05:03:41 AM', null, true);
INSERT INTO pos.client( client_id, created_at, updated_at, active, full_name, client_identification_number, cellphone, observation, address ) VALUES ( '23f65987-a1e4-48f8-9774-f95a71e8ce22', '2025-01-04 08:24:01 PM', null, true, 'CONSUMIDOR FINAL', '0000000', null, null, null);
INSERT INTO pos.country( country_id, code, name, description, created_at, updated_at, active ) VALUES ( '8174cc0e-0cbb-4641-9a68-25e8b6c53850', 'BO', 'BOLIVIA', 'center of sud america', '2023-10-23 05:03:41 AM', null, true);
INSERT INTO pos.gender_type( gender_type_id, code, name, created_at, updated_at, active ) VALUES ( '7709e586-be34-4edf-8afe-7886d874e801', 'M', 'MASCULINO', '2023-10-23 05:03:41 AM', null, true);
INSERT INTO pos.gender_type( gender_type_id, code, name, created_at, updated_at, active ) VALUES ( '7a54e0f8-8af1-499a-98c3-1d1b62abfd89', 'F', 'FEMENINO', '2023-10-25 08:41:07 AM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '946716a1-2a07-4db2-8b55-4cfcdc8a2ace', 'O+', 'O+', 'positive', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( 'e208af47-f2ca-4dc1-939d-c1b042f54bba', 'O-', 'O-', 'negative', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( 'b6130ecb-ef83-4989-9874-0d4c414b8734', 'A+', 'A+', 'positive', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '689d54e9-8a2d-4011-be0c-e5b5c83abb9d', 'A-', 'A-', 'negative', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '074d8ca1-3167-4d5b-b2a4-941ab63ca088', 'B+', 'B+', 'positive', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '8e73ba16-d1f2-4368-9394-a4dbc3241b1a', 'B-', 'B-', 'negative', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( 'b336ce17-318f-4cde-8451-2862716e636d', 'AB+', 'AB+', 'positive', '2023-11-27 06:05:56 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '54b19e56-f4db-4346-954d-fc7c35c55394', 'AB-', 'AB-', 'negative', '2023-11-27 06:05:57 PM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( '4ca94efc-9d78-44bf-a09f-5ab732fe0499', 'UNKNOWN', 'DESCONOCIDO', 'Tipo de sangre desconocido', '2023-12-27 12:52:50 AM', null, true);
INSERT INTO pos.group_blood_type( group_blood_type_id, code, name, description, created_at, updated_at, active ) VALUES ( 'ace03724-1ecf-43c2-ae32-ddfde40a12ca', 'UNKNOWN', 'DESCONOCIDO', 'Tipo de sangre desconocido', '2023-12-28 05:03:19 PM', null, true);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '58237c62-26b1-45fa-9dbe-4c6058b18128', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/sprite.jpeg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'b185570d-6039-427e-bb4e-94ae40cf4eb3', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/ham-simple.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'a0a382d8-7dd9-4db8-80b8-1c8e110b4412', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/ham-doble.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'da5b8709-b317-495c-b925-1021ff6abaea', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/simba.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '4fa91ebe-631c-4d0f-9241-2e6a26ef28e4', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/spiedo1.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '198e8877-e5ff-4578-b4d1-771adcbf902e', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/spiedo2.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '7bc423ae-668e-4fe0-adcb-20521e1793e9', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/broaster1.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '95b46245-8fc9-49b6-aeb4-b1886545fedf', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/broaster2.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '1fbc5310-8080-4a6b-bcc5-b9bdf8f66d5a', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/cocacola-300ML.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'b316a617-f01e-4e2e-a6ba-9fc7de351ed1', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/cocacola-mini.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '0967bddc-9029-4552-8e36-1cfb37f1422c', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/cocacola-popular.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'fffcd6f0-f084-46db-93cc-9ac86b59d4ee', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/cocacola-2L.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'f290535b-117a-49cd-99a1-57dc281834af', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/cocacola-3L.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '044348af-3a30-446e-97f4-c7feb27f0b82', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/fanta-300ML.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '4170ab31-90a5-45e6-a545-dc778884c2c7', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/fanta-2L.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '3624411f-c034-4784-ac25-e9c93d6199de', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/fanta-3L.jpeg', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( '213190bc-dc0f-4625-b1ba-ac9e4ef174b9', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/fanta-mini.png', 'image/jpeg', '425624', null);
INSERT INTO pos.image( image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description ) VALUES ( 'cde4a51c-a5a7-4ec9-81ae-a9ef0e114a74', 'f7d3a1f8-6b69-41e9-b98d-879b1a6fe52aguido quispe ego20240104.jpg', '2024-01-04 02:15:06 AM', null, true, '/assets/images/product/fanta-popular.jpg', 'image/jpeg', '425624', null);
INSERT INTO pos.measurement_type( measurement_type_id, code, name, created_at, updated_at, active, description ) VALUES ( '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 'UN', 'UNIDAD', '2025-01-04 01:20:58 AM', null, true, 'UNIDAD');
INSERT INTO pos.order_type( order_type_id, code, name, created_at, updated_at, active, description ) VALUES ( '315efbbc-889d-45ac-a629-725a468dd1de', 'PL', 'PARA LLEVAR', '2025-01-11 10:34:16 AM', null, true, 'PARA LLEVAR PARA AQUI ...');
INSERT INTO pos.order_type( order_type_id, code, name, created_at, updated_at, active, description ) VALUES ( 'a3c60a0e-c471-40de-932c-532918c80b43', 'LL', 'PARA AQUI', '2025-01-11 10:34:03 AM', null, true, 'PARA AQUI ...');
INSERT INTO pos.payment_type( payment_type_id, code, name, created_at, updated_at, active, description ) VALUES ( 'acd30c4f-4a3a-468a-9330-a69d583ce445', 'EF', 'EFECTIVO', '2025-01-04 01:21:14 AM', null, true, 'EFECTIVO');
INSERT INTO pos.payment_type( payment_type_id, code, name, created_at, updated_at, active, description ) VALUES ( '7a7e408c-4301-4b19-829d-eebfef898b56', 'QR', 'QR', '2025-01-04 01:21:27 AM', null, true, 'QR');
INSERT INTO pos.payment_type( payment_type_id, code, name, created_at, updated_at, active, description ) VALUES ( 'e9a8c72a-cd29-43f2-b96d-b8133b9ee414', 'TR', 'TRANSFERENCIA', '2025-01-05 03:57:29 AM', null, true, 'TRANSFERENCIA');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '21ad0c53-75be-4f1b-98bd-155ce45bcc9a', '2025-01-19 12:52:57 PM', null, true, '3d08308e-3fa4-4eb4-bfb2-3b9e64230a6a', 'b185570d-6039-427e-bb4e-94ae40cf4eb3', 1, 22, 'HAM-01', 23, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 14, 10, 'Simple');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '724d70e6-1891-4c78-9f1c-d5167903d4d5', '2025-01-19 12:50:38 PM', null, true, '6c9bc370-f4c9-47ba-9487-a976a7f51c91', '4fa91ebe-631c-4d0f-9241-2e6a26ef28e4', 2, 20, 'SPI-01', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 18, 15, 'Economico');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '64796324-fa33-48f0-a6c8-2a321855875e', '2025-01-19 12:52:16 PM', null, true, 'eff38cfe-9efa-47c8-ab46-b618f4545ce9', '95b46245-8fc9-49b6-aeb4-b1886545fedf', 1, 22, 'BRO-02', 2, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 15, 12, '1/4 Pollo');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'a2f32d26-0ab0-457c-b04c-ddba70e5bf25', '2025-01-19 12:53:30 PM', null, true, '3d08308e-3fa4-4eb4-bfb2-3b9e64230a6a', 'a0a382d8-7dd9-4db8-80b8-1c8e110b4412', 2, 23, 'HAM-02', 22, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 25, 23, 'Doble');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'ca2cd0c8-624c-4245-87bb-7a26a4713b03', '2025-01-19 12:51:03 PM', null, true, '6c9bc370-f4c9-47ba-9487-a976a7f51c91', '198e8877-e5ff-4578-b4d1-771adcbf902e', 2, 22, 'SPI-02', 1, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 20, 13, '1/4 Pollo');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '5d40172c-1c64-43ad-9056-38020b81b417', '2025-01-19 12:51:42 PM', null, true, 'eff38cfe-9efa-47c8-ab46-b618f4545ce9', '7bc423ae-668e-4fe0-adcb-20521e1793e9', 1, 200, 'BRO-01', 19, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 12, 10, 'Economico');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '602b6c76-0f0c-4582-a4c6-4abfff70f1f3', '2025-01-18 10:40:58 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '0967bddc-9029-4552-8e36-1cfb37f1422c', 2, 3, 'BEB-03', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 2, 2, 'CocoCola - Popular');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'fb13667c-227e-48e4-86ed-a19da3bbd888', '2025-01-10 12:54:23 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'cde4a51c-a5a7-4ec9-81ae-a9ef0e114a74', 3, 30, 'BEB-08', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 10, 7, 'Fanta - Popular');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '348503b8-7bb9-4b75-b466-1a2a9e6d7ffb', '2025-01-19 12:23:38 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '3624411f-c034-4784-ac25-e9c93d6199de', 2, 4, 'BEB-10', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 30, 2, 'Fanta - 3L');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'ed5cab29-a334-4753-9bb7-1dc41745f32a', '2025-01-18 04:40:06 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'fffcd6f0-f084-46db-93cc-9ac86b59d4ee', 10, 16, 'BEB-04', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 10, 6, 'CocoCola - 2L');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '5db5eb9d-350f-4534-85d1-047fb4de5a28', '2025-01-11 11:39:50 AM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'f290535b-117a-49cd-99a1-57dc281834af', 2, 10, 'BEB-05', 10, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 16, 10, 'CocoCola - 3L');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'a8c832bd-d9b1-4818-97c5-ae0f1b3d89e2', '2025-01-10 12:49:31 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '4170ab31-90a5-45e6-a545-dc778884c2c7', 2, 10, 'BEB-09', 2, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 23.5, 12, 'Fanta - 2L');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '0b7fb255-c699-44a6-b2be-b27f630ae314', '2025-01-10 02:03:13 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '213190bc-dc0f-4625-b1ba-ac9e4ef174b9', 2, 10, 'BEB-07', 20, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 3, 2, 'Fanta - Mini');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'fc099e1d-f882-48da-9c18-6fba932bd0dc', '2025-01-19 12:54:53 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'da5b8709-b317-495c-b925-1021ff6abaea', 2, 11, 'BEB-11', 2, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 23, 2, 'Simba');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'cb63e5d1-8494-4f01-9ab5-535e3a845275', '2025-01-11 11:39:20 AM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '1fbc5310-8080-4a6b-bcc5-b9bdf8f66d5a', 2, 20, 'BEB-01', 2, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 4, 2, 'CocaCola - 300 ML');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( 'a05b31fe-f7c2-411d-9ae6-38c53d2790bf', '2025-01-19 11:42:35 AM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', 'b316a617-f01e-4e2e-a6ba-9fc7de351ed1', 2, 2, 'BEB-02', 19, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 2, 2, 'CocoCola - Mini');
INSERT INTO pos.product( product_id, created_at, updated_at, active, category_id, image_id, min_stock, max_stock, sku, stock, measurement_type_id, price, cost, name ) VALUES ( '812e4490-6cc9-480e-ad60-e093653fd001', '2025-01-10 04:26:48 PM', null, true, 'ea55745e-ba70-4edb-8a39-9a449ea434c1', '044348af-3a30-446e-97f4-c7feb27f0b82', 1, 8, 'BEB-06', 17, '4918acd9-ca1c-4c34-933d-afa9bc94a7f2', 3, 2, 'Fanta - 300 ML');
INSERT INTO pos.sale_status( sale_status_id, code, name, created_at, updated_at, active, description ) VALUES ( '55000608-034c-4c98-beba-6ed9e7004193', 'PA', 'PAGADO', '2025-01-04 01:21:59 AM', null, true, 'PAGADO');
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '5033b9e2-ff9f-42ce-b59c-4cc507fc9ead', 'SZ', 'SANTA CRUZ', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:43 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '39cb0cbb-6c00-42c0-a3b5-575593d02333', 'PD', 'PANDO', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:44 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( 'dfa597cf-6172-492c-aa39-541335600873', 'PO', 'POTOSI', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:44 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '90e9ea03-185b-4787-9667-ac4564f398bf', 'OR', 'ORURO', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:43 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '099fd06c-e039-4713-b167-c84714cd01e0', 'CH', 'CHUQUISACA', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:44 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '0cbda97d-9520-44ea-bbb0-6dff0e729897', 'LP', 'LA PAZ', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:43 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( 'cc1ed4c8-da60-422d-836c-e8aae8a2fbaf', 'BN', 'BENI', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:44 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '1794b3f6-b519-47bd-bd3c-d166188bf249', 'TR', 'TARIJA', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-27 03:25:43 AM', null, true);
INSERT INTO pos.city( city_id, code, name, description, country_id, created_at, updated_at, active ) VALUES ( '31a76a44-fc8b-493f-b020-196ca4d8f5a5', 'CBBA', 'COCHABAMBA', 'bolivia city', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '2023-11-28 03:25:44 AM', null, true);
INSERT INTO pos.profile( profile_id, dni, name, lastname, email, cellphone, telephone, cellphone_references, address, birth_date, country_id, city_id, gender_type_id, civil_status_type_id, image_id, created_at, updated_at, active, occupation ) VALUES ( '1db685f5-d080-44a3-ad40-128e48cd9aca', '0000000', 'ALDAIR', 'MERIDA', 'aldair@gmail.com', '00000000', '', '', 'SD', '1990-10-08', '8174cc0e-0cbb-4641-9a68-25e8b6c53850', '31a76a44-fc8b-493f-b020-196ca4d8f5a5', '7709e586-be34-4edf-8afe-7886d874e801', '890b281e-4f52-421f-b443-a13092215a33', null, '2023-11-27 10:19:43 AM', null, true, null);
INSERT INTO pos.abac_user( user_id, username, passwd, profile_id, created_at, updated_at, active ) VALUES ( '03f10951-9d34-4424-9fc5-1a5b6ef252ae', 'aldair', '$2a$10$TKVfUkFlMTNsfBdVeBpgcubK4CHyYyPXKax94PYZc5pP9rwntz2Ci', '1db685f5-d080-44a3-ad40-128e48cd9aca', '2023-11-27 10:19:44 AM', null, true);
INSERT INTO pos.abac_user_role( user_role_id, created_at, updated_at, active, user_id, role_id ) VALUES ( '8ac03969-ff03-42a4-b6fd-48488b87103a', '2023-11-27 10:19:44 AM', null, true, '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '5a473b23-a66a-4c76-8860-2261aae65cf8');
INSERT INTO pos.box( box_id, name, created_at, updated_at, active, description, user_id ) VALUES ( '3d70a08d-2ebc-4a16-961b-18ce095ce60b', 'CAJA PRINCIPAL', '2025-01-04 08:20:39 PM', null, true, 'CAJA PRINCIPAL DESCRIPTION', '03f10951-9d34-4424-9fc5-1a5b6ef252ae');
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'b15d186a-73a9-4b7a-9c8c-a8b11181f3b1', '2025-01-29 01:09:30 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 09:09:30 AM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '9301833f-db55-4f31-a2bd-889b38819f1c', '2025-01-29 08:13:13 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 04:13:13 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '9af9738a-7859-4cde-9023-71d73b872098', '2025-01-29 08:16:55 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 04:16:55 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '20bc5372-92a6-4253-b1c0-398f534b49e0', '2025-01-29 08:18:58 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 04:18:58 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'e773a2af-14bc-4f61-956e-2801ea0c6b86', '2025-01-29 08:34:58 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 04:34:58 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'ad8e139b-ea4e-451d-aea7-d5a3e3068344', '2025-01-29 09:01:14 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 05:01:14 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'c6a6a8f8-5227-4df3-a5e9-018c936954b9', '2025-01-29 09:06:04 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 05:06:04 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'e4d534bf-f77a-4a02-9e59-c9f7d048d100', '2025-01-29 10:49:13 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-18 06:49:13 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'ab631f5b-e954-4b0a-99cf-fd053fca8fa1', '2025-01-30 08:43:37 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-19 04:43:37 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '70a72196-e790-42b9-8556-469a09c7a63d', '2025-01-30 08:43:51 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-19 04:43:51 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '8af1a946-fe41-4346-b7c7-dbcea83bddb2', '2025-01-30 10:23:24 AM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-19 06:23:24 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( '879df818-bec6-4a20-9c39-56267371fe27', '2025-01-30 02:46:21 PM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-19 10:46:21 PM', null, true);
INSERT INTO pos.abac_refresh_token( refresh_token_id, expire_date, user_id, created_at, updated_at, active ) VALUES ( 'e03d8fb8-6fa1-4e38-a73f-66701105d968', '2025-01-30 03:04:22 PM', '03f10951-9d34-4424-9fc5-1a5b6ef252ae', '2025-01-19 11:04:22 PM', null, true);
