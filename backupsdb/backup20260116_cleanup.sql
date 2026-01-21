--
-- PostgreSQL database dump
--

-- Dumped from database version 16.0 (Debian 16.0-1.pgdg120+1)
-- Dumped by pg_dump version 16.0 (Debian 16.0-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pos; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pos;


ALTER SCHEMA pos OWNER TO postgres;

--
-- Name: water; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA water;


ALTER SCHEMA water OWNER TO postgres;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: abac_function; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_function (
    function_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying NOT NULL,
    name character varying(100),
    description character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.abac_function OWNER TO postgres;

--
-- Name: abac_refresh_token; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_refresh_token (
    refresh_token_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    expire_date timestamp without time zone,
    user_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.abac_refresh_token OWNER TO postgres;

--
-- Name: abac_role; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_role (
    role_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying NOT NULL,
    name character varying(100),
    description character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.abac_role OWNER TO postgres;

--
-- Name: abac_role_function; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_role_function (
    role_function_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    role_id uuid NOT NULL,
    function_id uuid NOT NULL,
    read_enabled boolean DEFAULT false,
    create_enabled boolean DEFAULT false,
    update_enabled boolean DEFAULT false,
    delete_enabled boolean DEFAULT false
);


ALTER TABLE pos.abac_role_function OWNER TO postgres;

--
-- Name: abac_user; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_user (
    user_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(100),
    passwd character varying(100),
    profile_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.abac_user OWNER TO postgres;

--
-- Name: abac_user_role; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.abac_user_role (
    user_role_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE pos.abac_user_role OWNER TO postgres;

--
-- Name: bill_concept_item; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.bill_concept_item (
    bill_concept_item_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    water_bill_id uuid NOT NULL,
    concept_name character varying(200) NOT NULL,
    assigned_date date NOT NULL,
    amount numeric(10,2) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.bill_concept_item OWNER TO postgres;

--
-- Name: bill_status_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.bill_status_type (
    bill_status_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying NOT NULL,
    name character varying(100) NOT NULL,
    description character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.bill_status_type OWNER TO postgres;

--
-- Name: TABLE bill_status_type; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.bill_status_type IS 'Catálogo de estados de facturación';


--
-- Name: billing_config; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.billing_config (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value numeric(10,2) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE pos.billing_config OWNER TO postgres;

--
-- Name: TABLE billing_config; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.billing_config IS 'Configuración de valores para facturación de agua';


--
-- Name: COLUMN billing_config.config_key; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.billing_config.config_key IS 'Clave única de configuración';


--
-- Name: COLUMN billing_config.config_value; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.billing_config.config_value IS 'Valor numérico de la configuración';


--
-- Name: COLUMN billing_config.description; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.billing_config.description IS 'Descripción del parámetro de configuración';


--
-- Name: COLUMN billing_config.active; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.billing_config.active IS 'Indica si la configuración está activa';


--
-- Name: box; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.box (
    box_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    description character varying,
    user_id uuid NOT NULL
);


ALTER TABLE pos.box OWNER TO postgres;

--
-- Name: cash_balance; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.cash_balance (
    cash_balance_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    description character varying,
    box_id uuid NOT NULL,
    open_time timestamp with time zone,
    close_time timestamp with time zone,
    initial_money double precision,
    cash_in_box double precision,
    cash_difference double precision,
    last_correlative integer DEFAULT 0
);


ALTER TABLE pos.cash_balance OWNER TO postgres;

--
-- Name: cash_flow; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.cash_flow (
    cash_flow_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    description character varying,
    cash_balance_id uuid NOT NULL,
    payment_type_id uuid NOT NULL,
    amount numeric,
    cash_flow_type_id uuid
);


ALTER TABLE pos.cash_flow OWNER TO postgres;

--
-- Name: cash_flow_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.cash_flow_type (
    cash_flow_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    description character varying
);


ALTER TABLE pos.cash_flow_type OWNER TO postgres;

--
-- Name: city; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.city (
    city_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying,
    description character varying,
    country_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.city OWNER TO postgres;

--
-- Name: civil_status_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.civil_status_type (
    civil_status_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.civil_status_type OWNER TO postgres;

--
-- Name: connection_status_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.connection_status_type (
    connection_status_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying NOT NULL,
    name character varying(100) NOT NULL,
    description character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.connection_status_type OWNER TO postgres;

--
-- Name: TABLE connection_status_type; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.connection_status_type IS 'Catálogo de estados de conexión de agua';


--
-- Name: country; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.country (
    country_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying,
    description character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.country OWNER TO postgres;

--
-- Name: gender_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.gender_type (
    gender_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.gender_type OWNER TO postgres;

--
-- Name: image; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.image (
    image_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    url character varying,
    content_type character varying,
    content_size_kb character varying,
    description character varying
);


ALTER TABLE pos.image OWNER TO postgres;

--
-- Name: job; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.job (
    job_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(200) NOT NULL,
    start_date date NOT NULL,
    description text DEFAULT ''::text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    fine numeric(19,2)
);


ALTER TABLE pos.job OWNER TO postgres;

--
-- Name: COLUMN job.fine; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.job.fine IS 'Multa asociada al trabajo (opcional)';


--
-- Name: job_attendance; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.job_attendance (
    attendance_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    job_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    attendance_date date NOT NULL,
    present boolean DEFAULT true NOT NULL,
    check_in_time timestamp with time zone,
    check_out_time timestamp with time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.job_attendance OWNER TO postgres;

--
-- Name: TABLE job_attendance; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.job_attendance IS 'Registros de asistencia de socios a trabajos. Reemplaza a job_partner. Un socio está asignado a un trabajo si tiene un registro de asistencia activo.';


--
-- Name: COLUMN job_attendance.attendance_date; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.job_attendance.attendance_date IS 'Fecha de la asistencia';


--
-- Name: COLUMN job_attendance.present; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.job_attendance.present IS 'Indica si el socio asistió (true) o faltó (false)';


--
-- Name: COLUMN job_attendance.check_in_time; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.job_attendance.check_in_time IS 'Hora de entrada del socio';


--
-- Name: COLUMN job_attendance.check_out_time; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.job_attendance.check_out_time IS 'Hora de salida del socio';


--
-- Name: meeting; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.meeting (
    meeting_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(255) NOT NULL,
    meeting_date date NOT NULL,
    hour integer NOT NULL,
    minute integer NOT NULL,
    am_pm character varying(2) NOT NULL,
    description text,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    meeting_type_id uuid,
    fine numeric(19,2),
    waiting_minutes integer DEFAULT 0,
    CONSTRAINT meeting_am_pm_check CHECK (((am_pm)::text = ANY (ARRAY[('AM'::character varying)::text, ('PM'::character varying)::text]))),
    CONSTRAINT meeting_hour_check CHECK (((hour >= 1) AND (hour <= 12))),
    CONSTRAINT meeting_minute_check CHECK (((minute >= 0) AND (minute <= 59)))
);


ALTER TABLE pos.meeting OWNER TO postgres;

--
-- Name: TABLE meeting; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.meeting IS 'Tabla de reuniones del sistema';


--
-- Name: COLUMN meeting.hour; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.meeting.hour IS 'Hora en formato 12 horas (1-12)';


--
-- Name: COLUMN meeting.minute; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.meeting.minute IS 'Minuto (0-59)';


--
-- Name: COLUMN meeting.am_pm; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.meeting.am_pm IS 'Indicador AM o PM';


--
-- Name: COLUMN meeting.meeting_type_id; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.meeting.meeting_type_id IS 'Tipo de reunión (AULL o CLASSIC)';


--
-- Name: COLUMN meeting.fine; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.meeting.fine IS 'Multa asociada a la reunión (opcional)';


--
-- Name: meeting_attendance; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.meeting_attendance (
    meeting_attendance_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    meeting_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    attendance_date date NOT NULL,
    present boolean DEFAULT false NOT NULL,
    check_in_time timestamp with time zone,
    check_out_time timestamp with time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.meeting_attendance OWNER TO postgres;

--
-- Name: TABLE meeting_attendance; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.meeting_attendance IS 'Reemplaza a meeting_partner. Un socio está asignado a una reunión si tiene un registro de asistencia activo.';


--
-- Name: meeting_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.meeting_type (
    meeting_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    active boolean DEFAULT true
);


ALTER TABLE pos.meeting_type OWNER TO postgres;

--
-- Name: TABLE meeting_type; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.meeting_type IS 'Tipos de reunión disponibles en el sistema';


--
-- Name: order_number_seq; Type: SEQUENCE; Schema: pos; Owner: postgres
--

CREATE SEQUENCE pos.order_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE pos.order_number_seq OWNER TO postgres;

--
-- Name: partner; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.partner (
    partner_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    full_name character varying(100),
    partner_identification_number character varying(20),
    cellphone character varying(100),
    observation character varying(500),
    address character varying(300),
    water_connection_number character varying(50),
    water_meter_number character varying(50),
    connection_status_type_id uuid,
    connection_date date,
    water_connection_address character varying(300),
    current_debt numeric DEFAULT 0,
    last_billing_date date,
    notes character varying(500) DEFAULT ''::character varying,
    partner_number bigint,
    is_elderly boolean DEFAULT false
);


ALTER TABLE pos.partner OWNER TO postgres;

--
-- Name: TABLE partner; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.partner IS 'Tabla de socios del sistema de cobranza de agua';


--
-- Name: COLUMN partner.water_connection_number; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.partner.water_connection_number IS 'Número único de conexión de agua';


--
-- Name: COLUMN partner.water_meter_number; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.partner.water_meter_number IS 'Número de medidor de agua';


--
-- Name: COLUMN partner.current_debt; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.partner.current_debt IS 'Deuda actual acumulada del socio';


--
-- Name: COLUMN partner.last_billing_date; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.partner.last_billing_date IS 'Fecha de última facturación';


--
-- Name: COLUMN partner.partner_number; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.partner.partner_number IS 'Número único incremental autogenerado del socio. Identificador visible en la UI.';


--
-- Name: payment_type; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.payment_type (
    payment_type_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    description character varying
);


ALTER TABLE pos.payment_type OWNER TO postgres;

--
-- Name: profile; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.profile (
    profile_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    dni character varying,
    name character varying(100),
    lastname character varying(100),
    email character varying,
    cellphone character varying,
    telephone character varying,
    cellphone_references character varying,
    address character varying,
    birth_date date,
    country_id uuid NOT NULL,
    city_id uuid NOT NULL,
    gender_type_id uuid NOT NULL,
    civil_status_type_id uuid NOT NULL,
    image_id uuid,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    occupation character varying(100)
);


ALTER TABLE pos.profile OWNER TO postgres;

--
-- Name: water_bill; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.water_bill (
    water_bill_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    bill_number character varying(100) NOT NULL,
    partner_id uuid NOT NULL,
    reading_id uuid,
    billing_period_start date NOT NULL,
    billing_period_end date NOT NULL,
    consumption_m3 numeric NOT NULL,
    rate_per_m3 numeric NOT NULL,
    base_amount numeric NOT NULL,
    total_amount numeric NOT NULL,
    paid_amount numeric DEFAULT 0,
    remaining_balance numeric NOT NULL,
    bill_status_type_id uuid NOT NULL,
    due_date date NOT NULL,
    paid_date date,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.water_bill OWNER TO postgres;

--
-- Name: TABLE water_bill; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.water_bill IS 'Facturas de consumo de agua';


--
-- Name: water_meter_reading; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.water_meter_reading (
    reading_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    partner_id uuid NOT NULL,
    reading_date date NOT NULL,
    previous_reading numeric NOT NULL,
    current_reading numeric NOT NULL,
    consumption numeric NOT NULL,
    reader_user_id uuid,
    observation character varying(500) DEFAULT ''::character varying,
    image_id uuid,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true
);


ALTER TABLE pos.water_meter_reading OWNER TO postgres;

--
-- Name: TABLE water_meter_reading; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.water_meter_reading IS 'Registro de lecturas de medidores de agua';


--
-- Name: water_payment; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.water_payment (
    water_payment_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    water_bill_id uuid NOT NULL,
    partner_id uuid NOT NULL,
    payment_date date NOT NULL,
    amount numeric NOT NULL,
    payment_type_id uuid NOT NULL,
    cash_balance_id uuid,
    user_id uuid NOT NULL,
    receipt_number character varying(100) NOT NULL,
    observation character varying(500) DEFAULT ''::character varying,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone,
    active boolean DEFAULT true,
    correlative_number integer
);


ALTER TABLE pos.water_payment OWNER TO postgres;

--
-- Name: TABLE water_payment; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.water_payment IS 'Pagos realizados sobre facturas de agua';


--
-- Name: water_payment_detail; Type: TABLE; Schema: pos; Owner: postgres
--

CREATE TABLE pos.water_payment_detail (
    water_payment_detail_id uuid DEFAULT gen_random_uuid() NOT NULL,
    water_payment_id uuid NOT NULL,
    fine_type character varying(20) NOT NULL,
    fine_id uuid NOT NULL,
    fine_name character varying(255) NOT NULL,
    fine_date date NOT NULL,
    fine_amount numeric(19,2) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_fine_type CHECK (((fine_type)::text = ANY (ARRAY[('JOB'::character varying)::text, ('MEETING'::character varying)::text])))
);


ALTER TABLE pos.water_payment_detail OWNER TO postgres;

--
-- Name: TABLE water_payment_detail; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON TABLE pos.water_payment_detail IS 'Detalle de multas incluidas en un pago de agua';


--
-- Name: COLUMN water_payment_detail.water_payment_id; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.water_payment_id IS 'Referencia al pago de agua';


--
-- Name: COLUMN water_payment_detail.fine_type; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.fine_type IS 'Tipo de multa: JOB (trabajo) o MEETING (reunión)';


--
-- Name: COLUMN water_payment_detail.fine_id; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.fine_id IS 'ID de la ausencia que generó la multa';


--
-- Name: COLUMN water_payment_detail.fine_name; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.fine_name IS 'Nombre del trabajo o reunión';


--
-- Name: COLUMN water_payment_detail.fine_date; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.fine_date IS 'Fecha de la ausencia';


--
-- Name: COLUMN water_payment_detail.fine_amount; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON COLUMN pos.water_payment_detail.fine_amount IS 'Monto de la multa';


--
-- Name: order_number_seq; Type: SEQUENCE; Schema: water; Owner: postgres
--

CREATE SEQUENCE water.order_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE water.order_number_seq OWNER TO postgres;

--
-- Name: partner_number_seq; Type: SEQUENCE; Schema: water; Owner: postgres
--

CREATE SEQUENCE water.partner_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE water.partner_number_seq OWNER TO postgres;

--
-- Name: SEQUENCE partner_number_seq; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON SEQUENCE water.partner_number_seq IS 'Secuencia para generar números únicos de socio';


--
-- Name: water_payment_detail; Type: TABLE; Schema: water; Owner: postgres
--

CREATE TABLE water.water_payment_detail (
    water_payment_detail_id uuid DEFAULT gen_random_uuid() NOT NULL,
    water_payment_id uuid NOT NULL,
    fine_type character varying(20) NOT NULL,
    fine_id uuid NOT NULL,
    fine_name character varying(255) NOT NULL,
    fine_date date NOT NULL,
    fine_amount numeric(19,2) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    active boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_fine_type CHECK (((fine_type)::text = ANY (ARRAY[('JOB'::character varying)::text, ('MEETING'::character varying)::text])))
);


ALTER TABLE water.water_payment_detail OWNER TO postgres;

--
-- Name: TABLE water_payment_detail; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON TABLE water.water_payment_detail IS 'Detalle de multas incluidas en un pago de agua';


--
-- Name: COLUMN water_payment_detail.water_payment_id; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.water_payment_id IS 'Referencia al pago de agua';


--
-- Name: COLUMN water_payment_detail.fine_type; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.fine_type IS 'Tipo de multa: JOB (trabajo) o MEETING (reunión)';


--
-- Name: COLUMN water_payment_detail.fine_id; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.fine_id IS 'ID de la ausencia que generó la multa';


--
-- Name: COLUMN water_payment_detail.fine_name; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.fine_name IS 'Nombre del trabajo o reunión';


--
-- Name: COLUMN water_payment_detail.fine_date; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.fine_date IS 'Fecha de la ausencia';


--
-- Name: COLUMN water_payment_detail.fine_amount; Type: COMMENT; Schema: water; Owner: postgres
--

COMMENT ON COLUMN water.water_payment_detail.fine_amount IS 'Monto de la multa';


--
-- Data for Name: abac_function; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_function (function_id, code, name, description, created_at, updated_at, active) FROM stdin;
08638337-c992-4df4-a011-95212952d2b6	P	Manage Patients	Administrar pacientes	2023-10-28 20:11:08+00	\N	t
\.


--
-- Data for Name: abac_refresh_token; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_refresh_token (refresh_token_id, expire_date, user_id, created_at, updated_at, active) FROM stdin;
483b8cc7-4cfb-487c-938c-58317d9a0838	2026-01-11 13:31:49.83329	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 01:31:49.833379+00	\N	t
4486ea86-6a8a-45f6-91db-ddeba40c4fc2	2026-01-11 13:47:26.12461	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 01:47:26.124636+00	\N	t
dbbbbcf3-823a-40ea-beca-63faaff05da4	2026-01-11 13:47:36.374139	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 01:47:36.374191+00	\N	t
005adc40-317e-4d91-9d0d-c449207f7f40	2026-01-01 17:13:50.281042	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 15:43:50.281239+00	\N	t
d139e4a2-9779-4125-a827-3f890813066d	2026-01-01 17:17:32.831554	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-01 15:47:32.831654+00	\N	t
b74a0281-bf9a-46d3-8131-a7fd6933c7a6	2026-01-01 17:22:18.027066	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-01 15:52:18.027134+00	\N	t
b3b0508c-80d7-4f3d-a3b8-696acc0db1e0	2026-01-01 17:26:03.487786	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-01 15:56:03.487866+00	\N	t
a823013a-e5b5-4b9d-a5f8-ce59dea08e30	2026-01-01 17:28:58.408472	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 15:58:58.408539+00	\N	t
96c865ec-84d9-4af0-8159-b52d53c87a7a	2026-01-01 20:20:09.409233	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 18:50:09.409296+00	\N	t
befe4643-7be5-4ef3-8671-4098f354db7d	2026-01-01 20:36:01.465598	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 19:06:01.465651+00	\N	t
17d0c4fb-fba3-454d-bca4-78cc0b583c10	2026-01-01 20:54:46.702442	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 19:24:46.702517+00	\N	t
f85b90ad-b6c9-492f-81e1-028fd8d76ec0	2026-01-01 21:48:24.174287	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-01 20:18:24.174495+00	\N	t
40e8b670-4151-44f5-9a0a-baccbbb96e22	2026-01-01 23:17:40.803583	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-01 21:47:40.803644+00	\N	t
0dc1cc46-f5a2-4ed0-8b03-3de4bae7017d	2026-01-02 02:31:44.530665	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 01:01:44.530737+00	\N	t
4e6899b2-6c43-47ea-9e97-43b8f39cadb1	2026-01-02 06:46:39.873905	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 05:16:39.87406+00	\N	t
36166d48-53d6-4427-8698-428a06ee3fd0	2026-01-02 06:46:49.458604	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-02 05:16:49.458696+00	\N	t
c4835bf3-dc43-4400-97ff-f1966b1fab93	2026-01-02 06:48:44.732533	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-02 05:18:44.73265+00	\N	t
48537e62-b068-4912-a5de-6f20d4380bc1	2026-01-02 06:48:53.319494	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 05:18:53.31959+00	\N	t
ca11427a-3a1d-4b90-9705-1ee77e508309	2026-01-02 06:51:28.455545	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 05:21:28.455624+00	\N	t
4de4df82-b4e3-4131-babc-67505f807b8c	2026-01-02 07:21:08.89605	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 05:51:08.896104+00	\N	t
f3adaa40-829e-46ac-b07c-a5dc8ac09362	2026-01-02 20:51:40.268171	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 19:21:40.26825+00	\N	t
36617bb0-7cb2-4c85-8b3c-5169e48f14a4	2026-01-02 21:47:57.263677	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-02 20:17:57.263744+00	\N	t
d432f711-6d4d-4ed3-bce2-861334210551	2026-01-03 02:51:30.200769	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-03 01:21:30.200846+00	\N	t
3cc25424-b5ce-497e-bcc1-9666bc05cbad	2026-01-03 14:49:20.942442	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 13:19:20.942509+00	\N	t
079c618c-c820-4920-957c-9f0605cadaf9	2026-01-03 14:50:46.9116	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 13:20:46.911668+00	\N	t
576bfd41-0f8d-4e99-9c6c-1565c15662e8	2026-01-03 15:05:26.205776	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 13:35:26.205883+00	\N	t
7d7cf4d4-5253-4c75-9bbe-8afc82f5f334	2026-01-03 15:06:49.582395	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 13:36:49.582447+00	\N	t
06d39a81-0431-4bb1-aacf-ea425f4f2e16	2026-01-03 15:33:25.722609	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 14:03:25.722681+00	\N	t
ad5f60ad-cbec-4637-b370-b1aa30bd6cc2	2026-01-03 16:05:07.114398	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-03 14:35:07.11448+00	\N	t
fcd3876a-e6d2-4a18-a629-046c91f6c32b	2026-01-04 05:26:15.796477	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-04 03:56:15.796548+00	\N	t
ba1de9f9-e754-452d-a8cc-1ec8b88530d1	2026-01-04 13:38:34.235647	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 12:08:34.235708+00	\N	t
b5513529-f27a-4fe4-9016-7b8b8c975534	2026-01-04 16:17:44.662069	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 14:47:44.662223+00	\N	t
fafa0ee8-ac30-4893-8631-35e6523ec5b9	2026-01-04 16:18:15.260143	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 14:48:15.260223+00	\N	t
93dfb1bc-b74e-4bf8-b5cd-aadf628b863e	2026-01-04 16:20:09.380832	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-04 14:50:09.38092+00	\N	t
2a991602-edf1-4780-b0b0-43088b73ec1b	2026-01-04 16:32:19.701646	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-04 15:02:19.701744+00	\N	t
17130704-f642-4b32-8fbc-bff9131f1bf5	2026-01-04 16:43:19.66925	f8bf0fd4-68cd-478a-8f6a-9f872e69528a	2026-01-04 15:13:19.669331+00	\N	t
2af0a22e-3263-4112-9b6a-c38622ad3575	2026-01-04 16:59:36.349889	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 15:29:36.349956+00	\N	t
8141b789-1d2f-40b4-abe2-250b4fbccc2d	2026-01-04 17:00:37.815419	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 15:30:37.815508+00	\N	t
09b30e47-78fd-496d-a78b-4713d4b20b6a	2026-01-04 19:39:57.211131	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-04 18:09:57.211182+00	\N	t
18f83a9f-d038-4568-9216-ea3e509a0e95	2026-01-04 23:15:12.759744	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-04 21:45:12.759807+00	\N	t
e733a8cb-06cd-4727-8687-b4b847dcb0d0	2026-01-05 00:24:30.661192	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-04 22:54:30.6613+00	\N	t
d415ba53-14dd-46fb-80d1-47c2d1a231dc	2026-01-06 00:19:11.951236	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-05 22:49:11.951314+00	\N	t
fe4fc5d0-f3f8-4a58-96dd-0f7c97befaaa	2026-01-06 00:21:41.420333	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-05 22:51:41.420456+00	\N	t
35fda108-a250-4e81-8e6e-514396645f73	2026-01-06 00:47:18.990034	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-05 23:17:18.990114+00	\N	t
769693ce-1928-4738-b649-4d9f92b06a4c	2026-01-06 02:07:49.435903	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-06 00:37:49.43597+00	\N	t
727687aa-32a2-4772-90f2-3a9bc66469ea	2026-01-06 03:57:46.543806	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-06 02:27:46.543878+00	\N	t
2b8792fd-846c-4836-8763-afda15a9fe8d	2026-01-06 06:22:54.86307	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-06 04:52:54.863143+00	\N	t
3021f589-1456-44a8-a924-6fa9d89bae7e	2026-01-06 06:49:20.281567	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-06 05:19:20.281734+00	\N	t
032b7c18-3861-4ba4-abde-20fc17cc8a12	2026-01-08 13:28:15.7077	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-08 11:58:15.707789+00	\N	t
e29a3c36-ab4b-4467-b405-238221ec8baa	2026-01-08 16:09:31.611315	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-08 14:39:31.61144+00	\N	t
70523e58-b287-4f73-86d8-101e7699acd7	2026-01-08 16:18:48.605532	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-08 14:48:48.605607+00	\N	t
58c7a472-ebea-4848-9b17-634b358e973e	2026-01-08 16:27:03.882917	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-08 14:57:03.882983+00	\N	t
82c31396-7983-4542-a871-851509d59b8b	2026-01-08 17:30:28.862149	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-08 16:00:28.862211+00	\N	t
253ddacc-26bf-40bd-b9db-0f5133be39d0	2026-01-08 17:31:03.420995	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-08 16:01:03.421102+00	\N	t
b4a3e645-f019-4edd-acdd-c429bb50e071	2026-01-08 17:43:58.802988	739b4943-5db4-4869-b3a3-2d86b03364de	2026-01-08 16:13:58.80307+00	\N	t
635e9058-9c26-4523-849f-49971dcd6e7d	2026-01-14 01:22:51.406003	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-13 23:52:51.406134+00	\N	t
6da21a3f-4689-4cf5-a356-350eda71ec58	2026-01-23 15:53:00.167351	03f10951-9d34-4424-9fc5-1a5b6ef252ae	2026-01-16 15:53:00.167423+00	\N	t
\.


--
-- Data for Name: abac_role; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_role (role_id, code, name, description, created_at, updated_at, active) FROM stdin;
7001a19a-377a-42cd-8308-5277cd62919f	BILLING_COLLECTOR	Cajero	Help to doctor	2023-10-29 01:41:58+00	\N	t
f57a643f-987e-47e5-914f-4c5fc8578034	WATER_METER_READER	Lector de Medidores	\N	2025-12-27 12:50:04.512016+00	\N	t
e5025b8a-902c-4cf1-850b-4ca618655502	ADMIN	Administrador	FULL permissions	2023-10-29 04:58:19+00	\N	t
\.


--
-- Data for Name: abac_role_function; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_role_function (role_function_id, created_at, updated_at, active, role_id, function_id, read_enabled, create_enabled, update_enabled, delete_enabled) FROM stdin;
\.


--
-- Data for Name: abac_user; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_user (user_id, username, passwd, profile_id, created_at, updated_at, active) FROM stdin;
03f10951-9d34-4424-9fc5-1a5b6ef252ae	rafa	$2a$10$TKVfUkFlMTNsfBdVeBpgcubK4CHyYyPXKax94PYZc5pP9rwntz2Ci	1db685f5-d080-44a3-ad40-128e48cd9aca	2023-11-27 10:19:44+00	\N	t
739b4943-5db4-4869-b3a3-2d86b03364de	pedro	$2a$10$TKVfUkFlMTNsfBdVeBpgcubK4CHyYyPXKax94PYZc5pP9rwntz2Ci	d2655d6e-ef80-4a43-b54b-ba0d15164dc7	2023-11-27 10:19:44+00	\N	t
d92736c4-d577-4de2-958f-6323c9bacc11	gimena	$2a$10$A8FKsUZwDTpslJA9GFeTtOYEzWXlWssLgip05m52kQIF75KnPNUAi	38676b54-4276-4287-a1e7-8b14854d3fe8	2026-01-02 05:20:51.277228+00	\N	t
f8bf0fd4-68cd-478a-8f6a-9f872e69528a	ivan	$2a$10$mb8WOSyYkCEqwEyZknwbme6ZG9ym1dkyy3ybH..Rw7YScHoZ7xe36	b0b654cb-436d-43d9-a834-237fcfa8a801	2026-01-04 15:13:02.045204+00	\N	t
\.


--
-- Data for Name: abac_user_role; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.abac_user_role (user_role_id, created_at, updated_at, active, user_id, role_id) FROM stdin;
8ac03969-ff03-42a4-b6fd-48488b87103a	2023-11-27 10:19:44+00	\N	t	03f10951-9d34-4424-9fc5-1a5b6ef252ae	e5025b8a-902c-4cf1-850b-4ca618655502
69780aa1-3831-4622-9250-53d21c9fcca6	2023-11-27 10:19:44+00	\N	t	739b4943-5db4-4869-b3a3-2d86b03364de	f57a643f-987e-47e5-914f-4c5fc8578034
9d407ced-7eeb-4635-836e-b0c85ac63742	2026-01-02 05:20:51.283673+00	\N	t	d92736c4-d577-4de2-958f-6323c9bacc11	e5025b8a-902c-4cf1-850b-4ca618655502
344203f7-2c18-414c-b657-2c3379991882	2026-01-04 15:13:02.048017+00	\N	t	f8bf0fd4-68cd-478a-8f6a-9f872e69528a	7001a19a-377a-42cd-8308-5277cd62919f
\.


--
-- Data for Name: bill_concept_item; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.bill_concept_item (bill_concept_item_id, water_bill_id, concept_name, assigned_date, amount, created_at, updated_at, active) FROM stdin;
\.


--
-- Data for Name: bill_status_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.bill_status_type (bill_status_type_id, code, name, description, created_at, updated_at, active) FROM stdin;
734d99f5-31ff-4d8f-8427-467dfcc5b76a	PENDING	PENDIENTE	Factura pendiente de pago	2025-12-22 19:39:05.920091+00	\N	t
b98eab78-6f41-4f93-af6f-10396d5d1f68	PAID	PAGADA	Factura pagada completamente	2025-12-22 19:39:05.920091+00	\N	t
645f2a91-c2cb-4fd0-ba33-3295f72c05c7	CANCELLED	ANULADA	Factura cancelada	2025-12-22 19:39:05.920091+00	\N	t
\.


--
-- Data for Name: billing_config; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.billing_config (id, config_key, config_value, description, active, created_at, updated_at) FROM stdin;
e8219664-08f4-40a6-9970-9f7662ccda2c	APORTE_DEPORTE	2.00	Monto fijo de aporte al deporte por factura (Bs)	t	2026-01-04 14:27:48.040703+00	2026-01-04 14:27:48.040703+00
8c84b73d-0ab9-44bf-b9d0-b7f050420b22	APORTE_OTB	3.00	Monto fijo de aporte a la OTB por factura (Bs)	t	2026-01-04 14:27:48.040703+00	2026-01-04 14:27:48.040703+00
219860c1-a424-455d-8d7a-79e7a48d134a	TARIFA_BASICA	15.00	Tarifa básica que incluye consumo hasta 15 m³ (Bs)	t	2026-01-04 14:27:48.040703+00	2026-01-04 14:27:48.040703+00
b59a9be1-51f9-4aa4-b182-ee4f0ce287ee	MULTA_EXCESO_M3	5.00	Multa por cada m³ de exceso sobre los 15 m³ base (Bs/m³)	t	2026-01-04 14:27:48.040703+00	2026-01-04 14:27:48.040703+00
90673bdb-14ed-4e12-8139-dfb7908141e7	MULTA_CORTE	50.00	Monto aplicado al cortar el servicio por mora acumulada (Bs)	t	2026-01-06 04:56:21.431208+00	2026-01-06 04:56:21.431208+00
63a92502-b6c3-4be8-b36f-b47cc59ad0f6	MANTENIMIENTO_SUSPENDIDA	5.00	Cuota de mantenimiento mensual para conexiones suspendidas (Bs)	t	2026-01-06 04:56:21.431208+00	2026-01-06 04:56:21.431208+00
\.


--
-- Data for Name: box; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.box (box_id, name, created_at, updated_at, active, description, user_id) FROM stdin;
3d70a08d-2ebc-4a16-961b-18ce095ce60b	CAJA PRINCIPAL	2025-01-04 20:20:39+00	\N	t	CAJA PRINCIPAL DESCRIPTION	03f10951-9d34-4424-9fc5-1a5b6ef252ae
96f4645b-64b7-4ea6-997e-0a693cc65a4a	CAJA PRINCIPAL	2026-01-02 05:20:51.288556+00	\N	t	Caja creada automáticamente para el usuario gimena	d92736c4-d577-4de2-958f-6323c9bacc11
a031844c-488f-4b60-ab59-87c24817f360	CAJA PRINCIPAL	2026-01-04 15:13:02.050307+00	\N	t	Caja creada automáticamente para el usuario ivan	f8bf0fd4-68cd-478a-8f6a-9f872e69528a
\.


--
-- Data for Name: cash_balance; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.cash_balance (cash_balance_id, created_at, updated_at, active, description, box_id, open_time, close_time, initial_money, cash_in_box, cash_difference, last_correlative) FROM stdin;
87b4cebb-1de6-46e4-9761-cc306c0076da	2026-01-16 15:53:10.999547+00	\N	t		3d70a08d-2ebc-4a16-961b-18ce095ce60b	2026-01-16 15:53:10.9995+00	\N	1	\N	\N	0
\.


--
-- Data for Name: cash_flow; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.cash_flow (cash_flow_id, created_at, updated_at, active, description, cash_balance_id, payment_type_id, amount, cash_flow_type_id) FROM stdin;
\.


--
-- Data for Name: cash_flow_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.cash_flow_type (cash_flow_type_id, code, name, created_at, updated_at, active, description) FROM stdin;
4873feac-e8aa-435d-9b6a-d5f3432d14d8	IN	INGRESO	2025-01-06 09:21:44+00	\N	t	Ingreso a cajas
83db6177-683d-49a0-918e-298385776b26	OUT	EGRESO	2025-01-06 09:21:44+00	\N	t	Salida en cajas
\.


--
-- Data for Name: city; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.city (city_id, code, name, description, country_id, created_at, updated_at, active) FROM stdin;
5033b9e2-ff9f-42ce-b59c-4cc507fc9ead	SZ	SANTA CRUZ	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:43+00	\N	t
39cb0cbb-6c00-42c0-a3b5-575593d02333	PD	PANDO	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:44+00	\N	t
dfa597cf-6172-492c-aa39-541335600873	PO	POTOSI	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:44+00	\N	t
90e9ea03-185b-4787-9667-ac4564f398bf	OR	ORURO	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:43+00	\N	t
099fd06c-e039-4713-b167-c84714cd01e0	CH	CHUQUISACA	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:44+00	\N	t
0cbda97d-9520-44ea-bbb0-6dff0e729897	LP	LA PAZ	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:43+00	\N	t
cc1ed4c8-da60-422d-836c-e8aae8a2fbaf	BN	BENI	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:44+00	\N	t
1794b3f6-b519-47bd-bd3c-d166188bf249	TR	TARIJA	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-27 03:25:43+00	\N	t
31a76a44-fc8b-493f-b020-196ca4d8f5a5	CBBA	COCHABAMBA	bolivia city	8174cc0e-0cbb-4641-9a68-25e8b6c53850	2023-11-28 03:25:44+00	\N	t
\.


--
-- Data for Name: civil_status_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.civil_status_type (civil_status_type_id, code, name, created_at, updated_at, active) FROM stdin;
45f6bc94-0009-42c3-976b-a4fc0e01931d	CA	CASADO(A)	2023-11-27 18:02:05+00	\N	t
ab90a945-783b-42f0-8043-c8910b787f56	VI	VIUDO(A)	2023-11-27 18:02:05+00	\N	t
1c9cad8f-3bde-4977-a4f0-fa441c26801f	DI	DIVORCIADO(A)	2023-11-27 18:02:05+00	\N	t
4cbdec33-169d-4c9f-ab1f-8ad4492be5d9	CO	CONCUBINATO	2023-11-27 18:02:05+00	\N	t
890b281e-4f52-421f-b443-a13092215a33	SO	SOLTERO(A)	2023-10-23 05:03:41+00	\N	t
\.


--
-- Data for Name: connection_status_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.connection_status_type (connection_status_type_id, code, name, description, created_at, updated_at, active) FROM stdin;
425a0052-b86d-47ed-af91-8a84cbfc7f65	ACTIVE	ACTIVA	Conexión de agua activa	2025-12-22 19:39:05.901974+00	\N	t
8a5eb98d-4188-4974-a052-b31d954fc2df	SUSPENDED	SUSPENDIDA	Conexión de agua suspendida temporalmente	2025-12-22 19:39:05.901974+00	\N	t
4f5eab7a-b7df-41fd-b929-b82eadf9c2b2	CUT_OFF	CORTADA	Conexión de agua cortada por falta de pago	2025-12-22 19:39:05.901974+00	\N	t
8ddb4942-c473-40a9-91db-552c391f8ce7	INACTIVE	PASIVO	Conexión de agua inactiva	2025-12-22 19:39:05.901974+00	\N	t
\.


--
-- Data for Name: country; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.country (country_id, code, name, description, created_at, updated_at, active) FROM stdin;
8174cc0e-0cbb-4641-9a68-25e8b6c53850	BO	BOLIVIA	center of sud america	2023-10-23 05:03:41+00	\N	t
\.


--
-- Data for Name: gender_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.gender_type (gender_type_id, code, name, created_at, updated_at, active) FROM stdin;
7709e586-be34-4edf-8afe-7886d874e801	M	MASCULINO	2023-10-23 05:03:41+00	\N	t
7a54e0f8-8af1-499a-98c3-1d1b62abfd89	F	FEMENINO	2023-10-25 08:41:07+00	\N	t
\.


--
-- Data for Name: image; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.image (image_id, name, created_at, updated_at, active, url, content_type, content_size_kb, description) FROM stdin;
\.


--
-- Data for Name: job; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.job (job_id, name, start_date, description, created_at, updated_at, active, fine) FROM stdin;
\.


--
-- Data for Name: job_attendance; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.job_attendance (attendance_id, job_id, partner_id, attendance_date, present, check_in_time, check_out_time, created_at, updated_at, active) FROM stdin;
\.


--
-- Data for Name: meeting; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.meeting (meeting_id, name, meeting_date, hour, minute, am_pm, description, created_at, updated_at, active, meeting_type_id, fine, waiting_minutes) FROM stdin;
\.


--
-- Data for Name: meeting_attendance; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.meeting_attendance (meeting_attendance_id, meeting_id, partner_id, attendance_date, present, check_in_time, check_out_time, created_at, updated_at, active) FROM stdin;
\.


--
-- Data for Name: meeting_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.meeting_type (meeting_type_id, code, name, active) FROM stdin;
632bb10a-0b00-4598-ad11-051910404a4a	AULL	AULL	t
7d49e514-45b3-48ec-8fb8-87585d35e77a	CLASSIC	CLASICO	t
d5faafdb-f746-4279-892e-b1b87bc95b02	OTHER_INCOME	OTROS INGRESOS	t
\.


--
-- Data for Name: partner; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.partner (partner_id, created_at, updated_at, active, full_name, partner_identification_number, cellphone, observation, address, water_connection_number, water_meter_number, connection_status_type_id, connection_date, water_connection_address, current_debt, last_billing_date, notes, partner_number, is_elderly) FROM stdin;
\.


--
-- Data for Name: payment_type; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.payment_type (payment_type_id, code, name, created_at, updated_at, active, description) FROM stdin;
acd30c4f-4a3a-468a-9330-a69d583ce445	EF	EFECTIVO	2025-01-04 01:21:14+00	\N	t	EFECTIVO
7a7e408c-4301-4b19-829d-eebfef898b56	QR	QR	2025-01-04 01:21:27+00	\N	t	QR
\.


--
-- Data for Name: profile; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.profile (profile_id, dni, name, lastname, email, cellphone, telephone, cellphone_references, address, birth_date, country_id, city_id, gender_type_id, civil_status_type_id, image_id, created_at, updated_at, active, occupation) FROM stdin;
10d21bc4-e0dc-41e7-a17c-45a76dffb482	0000000	GIMENA	GIMENA	gimena@gmail.com	00000000			SD	1990-10-08	8174cc0e-0cbb-4641-9a68-25e8b6c53850	31a76a44-fc8b-493f-b020-196ca4d8f5a5	7709e586-be34-4edf-8afe-7886d874e801	890b281e-4f52-421f-b443-a13092215a33	\N	2023-11-27 10:19:43+00	\N	t	\N
1db685f5-d080-44a3-ad40-128e48cd9aca	0000000	RAFAEL	ENCINAS	encinas008@gmail.com	00000000			SD	1990-10-08	8174cc0e-0cbb-4641-9a68-25e8b6c53850	31a76a44-fc8b-493f-b020-196ca4d8f5a5	7709e586-be34-4edf-8afe-7886d874e801	890b281e-4f52-421f-b443-a13092215a33	\N	2023-11-27 10:19:43+00	\N	t	\N
d2655d6e-ef80-4a43-b54b-ba0d15164dc7	0000000	PEDRO	PEDRO	pedro@gmail.com	00000000			SD	1990-10-08	8174cc0e-0cbb-4641-9a68-25e8b6c53850	31a76a44-fc8b-493f-b020-196ca4d8f5a5	7709e586-be34-4edf-8afe-7886d874e801	890b281e-4f52-421f-b443-a13092215a33	\N	2023-11-27 10:19:43+00	\N	t	\N
38676b54-4276-4287-a1e7-8b14854d3fe8	777777777	Gimena	Suaznabar						\N	8174cc0e-0cbb-4641-9a68-25e8b6c53850	31a76a44-fc8b-493f-b020-196ca4d8f5a5	7a54e0f8-8af1-499a-98c3-1d1b62abfd89	45f6bc94-0009-42c3-976b-a4fc0e01931d	\N	2026-01-02 05:20:51.199847+00	\N	t	
b0b654cb-436d-43d9-a834-237fcfa8a801	77777777	IVAN	COQUE COLQUE						\N	8174cc0e-0cbb-4641-9a68-25e8b6c53850	31a76a44-fc8b-493f-b020-196ca4d8f5a5	7709e586-be34-4edf-8afe-7886d874e801	890b281e-4f52-421f-b443-a13092215a33	\N	2026-01-04 15:13:01.972388+00	\N	t	
\.


--
-- Data for Name: water_bill; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.water_bill (water_bill_id, bill_number, partner_id, reading_id, billing_period_start, billing_period_end, consumption_m3, rate_per_m3, base_amount, total_amount, paid_amount, remaining_balance, bill_status_type_id, due_date, paid_date, created_at, updated_at, active) FROM stdin;
\.


--
-- Data for Name: water_meter_reading; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.water_meter_reading (reading_id, partner_id, reading_date, previous_reading, current_reading, consumption, reader_user_id, observation, image_id, created_at, updated_at, active) FROM stdin;
\.


--
-- Data for Name: water_payment; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.water_payment (water_payment_id, water_bill_id, partner_id, payment_date, amount, payment_type_id, cash_balance_id, user_id, receipt_number, observation, created_at, updated_at, active, correlative_number) FROM stdin;
\.


--
-- Data for Name: water_payment_detail; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.water_payment_detail (water_payment_detail_id, water_payment_id, fine_type, fine_id, fine_name, fine_date, fine_amount, created_at, active) FROM stdin;
\.


--
-- Data for Name: water_payment_detail; Type: TABLE DATA; Schema: water; Owner: postgres
--

COPY water.water_payment_detail (water_payment_detail_id, water_payment_id, fine_type, fine_id, fine_name, fine_date, fine_amount, created_at, active) FROM stdin;
\.


--
-- Name: order_number_seq; Type: SEQUENCE SET; Schema: pos; Owner: postgres
--

SELECT pg_catalog.setval('pos.order_number_seq', 1, true);


--
-- Name: order_number_seq; Type: SEQUENCE SET; Schema: water; Owner: postgres
--

SELECT pg_catalog.setval('water.order_number_seq', 1, true);


--
-- Name: partner_number_seq; Type: SEQUENCE SET; Schema: water; Owner: postgres
--

SELECT pg_catalog.setval('water.partner_number_seq', 1004, true);


--
-- Name: billing_config billing_config_config_key_key; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.billing_config
    ADD CONSTRAINT billing_config_config_key_key UNIQUE (config_key);


--
-- Name: billing_config billing_config_pkey; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.billing_config
    ADD CONSTRAINT billing_config_pkey PRIMARY KEY (id);


--
-- Name: job_attendance pk_attendance; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.job_attendance
    ADD CONSTRAINT pk_attendance PRIMARY KEY (attendance_id);


--
-- Name: bill_concept_item pk_bill_concept_item; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.bill_concept_item
    ADD CONSTRAINT pk_bill_concept_item PRIMARY KEY (bill_concept_item_id);


--
-- Name: bill_status_type pk_bill_status_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.bill_status_type
    ADD CONSTRAINT pk_bill_status_type PRIMARY KEY (bill_status_type_id);


--
-- Name: city pk_city; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.city
    ADD CONSTRAINT pk_city PRIMARY KEY (city_id);


--
-- Name: civil_status_type pk_civil_status_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.civil_status_type
    ADD CONSTRAINT pk_civil_status_type PRIMARY KEY (civil_status_type_id);


--
-- Name: connection_status_type pk_connection_status_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.connection_status_type
    ADD CONSTRAINT pk_connection_status_type PRIMARY KEY (connection_status_type_id);


--
-- Name: country pk_country; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.country
    ADD CONSTRAINT pk_country PRIMARY KEY (country_id);


--
-- Name: gender_type pk_gender_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.gender_type
    ADD CONSTRAINT pk_gender_type PRIMARY KEY (gender_type_id);


--
-- Name: image pk_image; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.image
    ADD CONSTRAINT pk_image PRIMARY KEY (image_id);


--
-- Name: job pk_job; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.job
    ADD CONSTRAINT pk_job PRIMARY KEY (job_id);


--
-- Name: cash_flow pk_laboratory_type_10; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_flow
    ADD CONSTRAINT pk_laboratory_type_10 PRIMARY KEY (cash_flow_id);


--
-- Name: cash_flow_type pk_laboratory_type_11; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_flow_type
    ADD CONSTRAINT pk_laboratory_type_11 PRIMARY KEY (cash_flow_type_id);


--
-- Name: payment_type pk_laboratory_type_3; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.payment_type
    ADD CONSTRAINT pk_laboratory_type_3 PRIMARY KEY (payment_type_id);


--
-- Name: box pk_laboratory_type_8; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.box
    ADD CONSTRAINT pk_laboratory_type_8 PRIMARY KEY (box_id);


--
-- Name: cash_balance pk_laboratory_type_9; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_balance
    ADD CONSTRAINT pk_laboratory_type_9 PRIMARY KEY (cash_balance_id);


--
-- Name: meeting pk_meeting; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting
    ADD CONSTRAINT pk_meeting PRIMARY KEY (meeting_id);


--
-- Name: meeting_attendance pk_meeting_attendance; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_attendance
    ADD CONSTRAINT pk_meeting_attendance PRIMARY KEY (meeting_attendance_id);


--
-- Name: meeting_type pk_meeting_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_type
    ADD CONSTRAINT pk_meeting_type PRIMARY KEY (meeting_type_id);


--
-- Name: profile pk_profile; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT pk_profile PRIMARY KEY (profile_id);


--
-- Name: abac_role pk_tbl; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_role
    ADD CONSTRAINT pk_tbl PRIMARY KEY (role_id);


--
-- Name: abac_user_role pk_tbl_0; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user_role
    ADD CONSTRAINT pk_tbl_0 PRIMARY KEY (user_role_id);


--
-- Name: abac_function pk_tbl_1; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_function
    ADD CONSTRAINT pk_tbl_1 PRIMARY KEY (function_id);


--
-- Name: abac_role_function pk_tbl_2; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_role_function
    ADD CONSTRAINT pk_tbl_2 PRIMARY KEY (role_function_id);


--
-- Name: abac_user pk_user; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user
    ADD CONSTRAINT pk_user PRIMARY KEY (user_id);


--
-- Name: abac_refresh_token pk_user_0; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_refresh_token
    ADD CONSTRAINT pk_user_0 PRIMARY KEY (refresh_token_id);


--
-- Name: water_bill pk_water_bill; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_bill
    ADD CONSTRAINT pk_water_bill PRIMARY KEY (water_bill_id);


--
-- Name: water_meter_reading pk_water_meter_reading; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_meter_reading
    ADD CONSTRAINT pk_water_meter_reading PRIMARY KEY (reading_id);


--
-- Name: water_payment pk_water_payment; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT pk_water_payment PRIMARY KEY (water_payment_id);


--
-- Name: partner pkey_client; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.partner
    ADD CONSTRAINT pkey_client PRIMARY KEY (partner_id);


--
-- Name: job_attendance uk_attendance_unique; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.job_attendance
    ADD CONSTRAINT uk_attendance_unique UNIQUE (job_id, partner_id, attendance_date);


--
-- Name: meeting_attendance uk_meeting_attendance_unique; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_attendance
    ADD CONSTRAINT uk_meeting_attendance_unique UNIQUE (meeting_id, partner_id, attendance_date);


--
-- Name: meeting_type uk_meeting_type_code; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_type
    ADD CONSTRAINT uk_meeting_type_code UNIQUE (code);


--
-- Name: bill_status_type unq_bill_status_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.bill_status_type
    ADD CONSTRAINT unq_bill_status_type UNIQUE (code, name);


--
-- Name: city unq_city; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.city
    ADD CONSTRAINT unq_city UNIQUE (code);


--
-- Name: connection_status_type unq_connection_status_type; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.connection_status_type
    ADD CONSTRAINT unq_connection_status_type UNIQUE (code, name);


--
-- Name: country unq_country; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.country
    ADD CONSTRAINT unq_country UNIQUE (code);


--
-- Name: payment_type unq_laboratory_type_1; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.payment_type
    ADD CONSTRAINT unq_laboratory_type_1 UNIQUE (code, name);


--
-- Name: cash_flow_type unq_laboratory_type_3; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_flow_type
    ADD CONSTRAINT unq_laboratory_type_3 UNIQUE (code, name);


--
-- Name: profile unq_profile; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT unq_profile UNIQUE (dni, email);


--
-- Name: abac_role unq_role; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_role
    ADD CONSTRAINT unq_role UNIQUE (code, name);


--
-- Name: abac_function unq_role_0; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_function
    ADD CONSTRAINT unq_role_0 UNIQUE (code, name);


--
-- Name: abac_user unq_user; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user
    ADD CONSTRAINT unq_user UNIQUE (username);


--
-- Name: water_bill unq_water_bill_number; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_bill
    ADD CONSTRAINT unq_water_bill_number UNIQUE (bill_number);


--
-- Name: water_payment unq_water_payment_receipt; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT unq_water_payment_receipt UNIQUE (receipt_number);


--
-- Name: water_payment_detail uq_water_payment_detail_payment_fine; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment_detail
    ADD CONSTRAINT uq_water_payment_detail_payment_fine UNIQUE (water_payment_id, fine_type, fine_id);


--
-- Name: water_payment_detail water_payment_detail_pkey; Type: CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment_detail
    ADD CONSTRAINT water_payment_detail_pkey PRIMARY KEY (water_payment_detail_id);


--
-- Name: water_payment_detail uq_water_payment_detail_payment_fine; Type: CONSTRAINT; Schema: water; Owner: postgres
--

ALTER TABLE ONLY water.water_payment_detail
    ADD CONSTRAINT uq_water_payment_detail_payment_fine UNIQUE (water_payment_id, fine_type, fine_id);


--
-- Name: water_payment_detail water_payment_detail_pkey; Type: CONSTRAINT; Schema: water; Owner: postgres
--

ALTER TABLE ONLY water.water_payment_detail
    ADD CONSTRAINT water_payment_detail_pkey PRIMARY KEY (water_payment_detail_id);


--
-- Name: idx_attendance_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_attendance_active ON pos.job_attendance USING btree (active) WHERE (active = true);


--
-- Name: idx_attendance_job_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_attendance_job_date ON pos.job_attendance USING btree (job_id, attendance_date);


--
-- Name: idx_bill_concept_item_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_bill_concept_item_active ON pos.bill_concept_item USING btree (active) WHERE (active = true);


--
-- Name: idx_bill_concept_item_water_bill_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_bill_concept_item_water_bill_id ON pos.bill_concept_item USING btree (water_bill_id);


--
-- Name: idx_billing_config_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_billing_config_active ON pos.billing_config USING btree (active);


--
-- Name: idx_billing_config_key; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_billing_config_key ON pos.billing_config USING btree (config_key);


--
-- Name: idx_job_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_active ON pos.job USING btree (active) WHERE (active = true);


--
-- Name: idx_job_attendance_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_attendance_date ON pos.job_attendance USING btree (attendance_date);


--
-- Name: idx_job_attendance_job_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_attendance_job_id ON pos.job_attendance USING btree (job_id);


--
-- Name: idx_job_attendance_partner_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_attendance_partner_id ON pos.job_attendance USING btree (partner_id);


--
-- Name: idx_job_created_at; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_created_at ON pos.job USING btree (created_at DESC);


--
-- Name: idx_job_name; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_name ON pos.job USING btree (name);


--
-- Name: idx_job_start_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_job_start_date ON pos.job USING btree (start_date);


--
-- Name: idx_meeting_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_active ON pos.meeting USING btree (active) WHERE (active = true);


--
-- Name: idx_meeting_attendance_active; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_attendance_active ON pos.meeting_attendance USING btree (active) WHERE (active = true);


--
-- Name: idx_meeting_attendance_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_attendance_date ON pos.meeting_attendance USING btree (attendance_date);


--
-- Name: idx_meeting_attendance_meeting_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_attendance_meeting_id ON pos.meeting_attendance USING btree (meeting_id);


--
-- Name: idx_meeting_attendance_partner_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_attendance_partner_id ON pos.meeting_attendance USING btree (partner_id);


--
-- Name: idx_meeting_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_date ON pos.meeting USING btree (meeting_date);


--
-- Name: idx_meeting_meeting_type_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_meeting_meeting_type_id ON pos.meeting USING btree (meeting_type_id);


--
-- Name: idx_partner_connection_status; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_partner_connection_status ON pos.partner USING btree (connection_status_type_id);


--
-- Name: idx_partner_debt; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_partner_debt ON pos.partner USING btree (current_debt) WHERE (current_debt > (0)::numeric);


--
-- Name: idx_partner_number_unique; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE UNIQUE INDEX idx_partner_number_unique ON pos.partner USING btree (partner_number) WHERE (partner_number IS NOT NULL);


--
-- Name: idx_partner_water_connection; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_partner_water_connection ON pos.partner USING btree (water_connection_number);


--
-- Name: idx_partner_water_meter; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_partner_water_meter ON pos.partner USING btree (water_meter_number);


--
-- Name: idx_water_bill_due_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_bill_due_date ON pos.water_bill USING btree (due_date);


--
-- Name: idx_water_bill_partner; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_bill_partner ON pos.water_bill USING btree (partner_id);


--
-- Name: idx_water_bill_period; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_bill_period ON pos.water_bill USING btree (billing_period_start, billing_period_end);


--
-- Name: idx_water_bill_status; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_bill_status ON pos.water_bill USING btree (bill_status_type_id);


--
-- Name: idx_water_payment_bill; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_bill ON pos.water_payment USING btree (water_bill_id);


--
-- Name: idx_water_payment_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_date ON pos.water_payment USING btree (payment_date);


--
-- Name: idx_water_payment_detail_fine_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_fine_id ON pos.water_payment_detail USING btree (fine_id);


--
-- Name: idx_water_payment_detail_fine_type; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_fine_type ON pos.water_payment_detail USING btree (fine_type);


--
-- Name: idx_water_payment_detail_payment_id; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_payment_id ON pos.water_payment_detail USING btree (water_payment_id);


--
-- Name: idx_water_payment_partner; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_payment_partner ON pos.water_payment USING btree (partner_id);


--
-- Name: idx_water_reading_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_reading_date ON pos.water_meter_reading USING btree (reading_date);


--
-- Name: idx_water_reading_partner; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_reading_partner ON pos.water_meter_reading USING btree (partner_id);


--
-- Name: idx_water_reading_partner_date; Type: INDEX; Schema: pos; Owner: postgres
--

CREATE INDEX idx_water_reading_partner_date ON pos.water_meter_reading USING btree (partner_id, reading_date DESC);


--
-- Name: idx_water_payment_detail_fine_id; Type: INDEX; Schema: water; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_fine_id ON water.water_payment_detail USING btree (fine_id);


--
-- Name: idx_water_payment_detail_fine_type; Type: INDEX; Schema: water; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_fine_type ON water.water_payment_detail USING btree (fine_type);


--
-- Name: idx_water_payment_detail_payment_id; Type: INDEX; Schema: water; Owner: postgres
--

CREATE INDEX idx_water_payment_detail_payment_id ON water.water_payment_detail USING btree (water_payment_id);


--
-- Name: abac_refresh_token fk_abac_refresh_token_abac_user; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_refresh_token
    ADD CONSTRAINT fk_abac_refresh_token_abac_user FOREIGN KEY (user_id) REFERENCES pos.abac_user(user_id);


--
-- Name: abac_role_function fk_abac_role_function_abac_function; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_role_function
    ADD CONSTRAINT fk_abac_role_function_abac_function FOREIGN KEY (function_id) REFERENCES pos.abac_function(function_id);


--
-- Name: abac_role_function fk_abac_role_function_abac_role; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_role_function
    ADD CONSTRAINT fk_abac_role_function_abac_role FOREIGN KEY (role_id) REFERENCES pos.abac_role(role_id);


--
-- Name: abac_user_role fk_abac_user_role_abac_role; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user_role
    ADD CONSTRAINT fk_abac_user_role_abac_role FOREIGN KEY (role_id) REFERENCES pos.abac_role(role_id);


--
-- Name: abac_user_role fk_abac_user_role_usr; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user_role
    ADD CONSTRAINT fk_abac_user_role_usr FOREIGN KEY (user_id) REFERENCES pos.abac_user(user_id);


--
-- Name: job_attendance fk_attendance_job; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.job_attendance
    ADD CONSTRAINT fk_attendance_job FOREIGN KEY (job_id) REFERENCES pos.job(job_id) ON DELETE CASCADE;


--
-- Name: job_attendance fk_attendance_partner; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.job_attendance
    ADD CONSTRAINT fk_attendance_partner FOREIGN KEY (partner_id) REFERENCES pos.partner(partner_id) ON DELETE CASCADE;


--
-- Name: bill_concept_item fk_bill_concept_item_water_bill; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.bill_concept_item
    ADD CONSTRAINT fk_bill_concept_item_water_bill FOREIGN KEY (water_bill_id) REFERENCES pos.water_bill(water_bill_id) ON DELETE CASCADE;


--
-- Name: box fk_box_abac_user; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.box
    ADD CONSTRAINT fk_box_abac_user FOREIGN KEY (user_id) REFERENCES pos.abac_user(user_id);


--
-- Name: cash_balance fk_cash_balance_box; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_balance
    ADD CONSTRAINT fk_cash_balance_box FOREIGN KEY (box_id) REFERENCES pos.box(box_id);


--
-- Name: cash_flow fk_cash_flow_cash_balance; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_flow
    ADD CONSTRAINT fk_cash_flow_cash_balance FOREIGN KEY (cash_balance_id) REFERENCES pos.cash_balance(cash_balance_id);


--
-- Name: cash_flow fk_cash_flow_payment_type; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.cash_flow
    ADD CONSTRAINT fk_cash_flow_payment_type FOREIGN KEY (payment_type_id) REFERENCES pos.payment_type(payment_type_id);


--
-- Name: city fk_city_country; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.city
    ADD CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES pos.country(country_id);


--
-- Name: meeting_attendance fk_meeting_attendance_meeting; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_attendance
    ADD CONSTRAINT fk_meeting_attendance_meeting FOREIGN KEY (meeting_id) REFERENCES pos.meeting(meeting_id) ON DELETE CASCADE;


--
-- Name: meeting_attendance fk_meeting_attendance_partner; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting_attendance
    ADD CONSTRAINT fk_meeting_attendance_partner FOREIGN KEY (partner_id) REFERENCES pos.partner(partner_id) ON DELETE CASCADE;


--
-- Name: meeting fk_meeting_meeting_type; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.meeting
    ADD CONSTRAINT fk_meeting_meeting_type FOREIGN KEY (meeting_type_id) REFERENCES pos.meeting_type(meeting_type_id) ON DELETE SET NULL;


--
-- Name: partner fk_partner_connection_status; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.partner
    ADD CONSTRAINT fk_partner_connection_status FOREIGN KEY (connection_status_type_id) REFERENCES pos.connection_status_type(connection_status_type_id);


--
-- Name: profile fk_profile_city; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT fk_profile_city FOREIGN KEY (city_id) REFERENCES pos.city(city_id);


--
-- Name: profile fk_profile_civil_status_type; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT fk_profile_civil_status_type FOREIGN KEY (civil_status_type_id) REFERENCES pos.civil_status_type(civil_status_type_id);


--
-- Name: profile fk_profile_country; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT fk_profile_country FOREIGN KEY (country_id) REFERENCES pos.country(country_id);


--
-- Name: profile fk_profile_gender_type; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT fk_profile_gender_type FOREIGN KEY (gender_type_id) REFERENCES pos.gender_type(gender_type_id);


--
-- Name: profile fk_profile_image; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.profile
    ADD CONSTRAINT fk_profile_image FOREIGN KEY (image_id) REFERENCES pos.image(image_id);


--
-- Name: abac_user fk_usr_profile; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.abac_user
    ADD CONSTRAINT fk_usr_profile FOREIGN KEY (profile_id) REFERENCES pos.profile(profile_id);


--
-- Name: water_bill fk_water_bill_partner; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_bill
    ADD CONSTRAINT fk_water_bill_partner FOREIGN KEY (partner_id) REFERENCES pos.partner(partner_id);


--
-- Name: water_bill fk_water_bill_reading; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_bill
    ADD CONSTRAINT fk_water_bill_reading FOREIGN KEY (reading_id) REFERENCES pos.water_meter_reading(reading_id);


--
-- Name: water_bill fk_water_bill_status; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_bill
    ADD CONSTRAINT fk_water_bill_status FOREIGN KEY (bill_status_type_id) REFERENCES pos.bill_status_type(bill_status_type_id);


--
-- Name: water_payment fk_water_payment_bill; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT fk_water_payment_bill FOREIGN KEY (water_bill_id) REFERENCES pos.water_bill(water_bill_id);


--
-- Name: water_payment fk_water_payment_cash_balance; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT fk_water_payment_cash_balance FOREIGN KEY (cash_balance_id) REFERENCES pos.cash_balance(cash_balance_id);


--
-- Name: water_payment_detail fk_water_payment_detail_payment; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment_detail
    ADD CONSTRAINT fk_water_payment_detail_payment FOREIGN KEY (water_payment_id) REFERENCES pos.water_payment(water_payment_id) ON DELETE CASCADE;


--
-- Name: water_payment fk_water_payment_partner; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT fk_water_payment_partner FOREIGN KEY (partner_id) REFERENCES pos.partner(partner_id);


--
-- Name: water_payment fk_water_payment_type; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT fk_water_payment_type FOREIGN KEY (payment_type_id) REFERENCES pos.payment_type(payment_type_id);


--
-- Name: water_payment fk_water_payment_user; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_payment
    ADD CONSTRAINT fk_water_payment_user FOREIGN KEY (user_id) REFERENCES pos.abac_user(user_id);


--
-- Name: water_meter_reading fk_water_reading_image; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_image FOREIGN KEY (image_id) REFERENCES pos.image(image_id);


--
-- Name: water_meter_reading fk_water_reading_partner; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_partner FOREIGN KEY (partner_id) REFERENCES pos.partner(partner_id);


--
-- Name: water_meter_reading fk_water_reading_user; Type: FK CONSTRAINT; Schema: pos; Owner: postgres
--

ALTER TABLE ONLY pos.water_meter_reading
    ADD CONSTRAINT fk_water_reading_user FOREIGN KEY (reader_user_id) REFERENCES pos.abac_user(user_id);


--
-- PostgreSQL database dump complete
--

