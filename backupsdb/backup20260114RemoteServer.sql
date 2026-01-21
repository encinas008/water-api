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
-- Name: partner_number_seq; Type: SEQUENCE; Schema: pos; Owner: postgres
--

CREATE SEQUENCE pos.partner_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE pos.partner_number_seq OWNER TO postgres;

--
-- Name: SEQUENCE partner_number_seq; Type: COMMENT; Schema: pos; Owner: postgres
--

COMMENT ON SEQUENCE pos.partner_number_seq IS 'Secuencia para generar números únicos de socio';


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
    partner_number bigint DEFAULT nextval('pos.partner_number_seq'::regclass),
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
3476c953-8eca-4430-8d5a-8f41d9dc315b	cc7ca9ca-4926-48de-9de5-e21bdd078c9e	Multa por exceso de consumo de agua (2632.41 m³ × 5 Bs/m³)	2026-01-01	13162.05	2026-01-03 13:20:11.166796+00	\N	t
3e4a362b-e119-4867-8789-d8cda248630b	cc7ca9ca-4926-48de-9de5-e21bdd078c9e	Aporte al deporte	2026-01-01	2.00	2026-01-03 13:20:11.166841+00	\N	t
12d8a36e-83f3-4d50-b76a-e049ebae9a26	cc7ca9ca-4926-48de-9de5-e21bdd078c9e	Tarifa básica	2026-01-01	15.00	2026-01-03 13:20:11.166849+00	\N	t
bf6b17fe-c71d-4df4-8e6a-b49623ce15e3	cc7ca9ca-4926-48de-9de5-e21bdd078c9e	Aporte a la OTB	2026-01-01	3.00	2026-01-03 13:20:11.166854+00	\N	t
e42023a6-c2c9-4431-b909-5e1a85606b6d	342308a6-bae8-4290-938e-806f1977c1d7	Aporte al deporte	2026-01-01	2.00	2026-01-03 13:23:45.480425+00	\N	t
2e32e757-7f8c-4027-9202-2438c0b2dc96	342308a6-bae8-4290-938e-806f1977c1d7	Tarifa básica	2026-01-01	15.00	2026-01-03 13:23:45.480434+00	\N	t
b03a8477-1730-489b-ab15-21901882da3d	342308a6-bae8-4290-938e-806f1977c1d7	Aporte a la OTB	2026-01-01	3.00	2026-01-03 13:23:45.480437+00	\N	t
96c611de-acb3-426f-92d8-41b2120e2dc2	ff3aa27d-1dc3-451e-9517-b4367e960ad0	Aporte al deporte	2026-01-01	2.00	2026-01-03 13:26:21.042647+00	\N	t
a8d90041-0e34-470c-b341-ca06f11003af	ff3aa27d-1dc3-451e-9517-b4367e960ad0	Tarifa básica	2026-01-01	15.00	2026-01-03 13:26:21.042654+00	\N	t
fcd91b64-d934-4677-8257-9e46510f331e	ff3aa27d-1dc3-451e-9517-b4367e960ad0	Aporte a la OTB	2026-01-01	3.00	2026-01-03 13:26:21.042657+00	\N	t
3880e180-0a07-4080-9d30-ff074bfb6d0b	7f4e453d-c0a4-467b-861c-628adeccda76	Aporte al deporte	2026-01-01	2.00	2026-01-03 13:27:10.106254+00	\N	t
368cc8bc-2476-48ff-bfcf-8aa635d6756e	7f4e453d-c0a4-467b-861c-628adeccda76	Tarifa básica	2026-01-01	15.00	2026-01-03 13:27:10.106259+00	\N	t
db7e076f-4d87-4831-a8dd-3a2cc0731d3c	7f4e453d-c0a4-467b-861c-628adeccda76	Aporte a la OTB	2026-01-01	3.00	2026-01-03 13:27:10.106262+00	\N	t
f7b70562-645a-4fec-a754-81165a7bd4fa	18252f16-7172-4440-926a-9e9e05d9f29e	Aporte al deporte	2026-01-01	2.00	2026-01-03 13:27:48.690801+00	\N	t
2bb370f0-6809-47e1-ba7d-6ce313a738ab	18252f16-7172-4440-926a-9e9e05d9f29e	Tarifa básica	2026-01-01	15.00	2026-01-03 13:27:48.690808+00	\N	t
d85b0ae1-d96d-4c92-8a99-9217cc297951	18252f16-7172-4440-926a-9e9e05d9f29e	Aporte a la OTB	2026-01-01	3.00	2026-01-03 13:27:48.69081+00	\N	t
a7e49e04-0268-42ab-b2fc-b20ae3f10259	83332343-c49e-4048-a582-edd12acf9f9a	Aporte al deporte	2026-01-01	2.00	2026-01-03 14:04:53.426868+00	\N	t
e3dbb8b5-88b6-4058-97ca-d043b7bec2e6	83332343-c49e-4048-a582-edd12acf9f9a	Tarifa básica	2026-01-01	15.00	2026-01-03 14:04:53.426879+00	\N	t
ff37929d-ca6d-4da9-9dda-d24c7c7693dd	83332343-c49e-4048-a582-edd12acf9f9a	Aporte a la OTB	2026-01-01	3.00	2026-01-03 14:04:53.426921+00	\N	t
e2615b5c-1020-4a07-985e-6a62ed6de443	552f5ea4-25b0-40e7-9ef5-e3b8030563ca	Multa por exceso de consumo de agua (1388.66 m³ × 5.00 Bs/m³)	2026-01-01	6943.30	2026-01-08 16:14:28.975051+00	\N	t
deaae379-bcf9-4554-9ff8-fb4319afbeba	552f5ea4-25b0-40e7-9ef5-e3b8030563ca	Aporte al deporte	2026-01-01	2.00	2026-01-08 16:14:28.975101+00	\N	t
975a2ab9-499c-4a81-801c-f4c8caf44e56	552f5ea4-25b0-40e7-9ef5-e3b8030563ca	Tarifa Básica (Consumo hasta 15.00 m³)	2026-01-01	15.00	2026-01-08 16:14:28.976186+00	\N	t
0c60784f-7c97-4ea6-8b6a-6cdc2bf8a0f8	552f5ea4-25b0-40e7-9ef5-e3b8030563ca	Aporte a la OTB	2026-01-01	3.00	2026-01-08 16:14:28.9762+00	\N	t
7544f762-b2a3-44bd-8ba8-7513140f639b	656a4d6b-4280-4550-b406-37cdd4668ecb	Multa por exceso de consumo de agua (95.19 m³ × 5.00 Bs/m³)	2026-01-01	475.95	2026-01-08 16:15:35.371634+00	\N	t
e369edbe-b886-4f5e-a54d-b763d1a883be	656a4d6b-4280-4550-b406-37cdd4668ecb	Aporte al deporte	2026-01-01	2.00	2026-01-08 16:15:35.371659+00	\N	t
b00046d6-a66a-4a8b-b8e6-d29a84c2583e	656a4d6b-4280-4550-b406-37cdd4668ecb	Tarifa Básica (Consumo hasta 15.00 m³)	2026-01-01	15.00	2026-01-08 16:15:35.371668+00	\N	t
c2bc4cb6-0a9b-4d9b-bcea-78c44acf4447	656a4d6b-4280-4550-b406-37cdd4668ecb	Aporte a la OTB	2026-01-01	3.00	2026-01-08 16:15:35.37167+00	\N	t
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
c1539da0-e1ca-4ce2-a45c-7b709cc23359	2026-01-01 01:32:40.703809+00	\N	t	ROXANA VEDIA MONTAÑO	7777777			7777777	\N	282	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2377.8750	2025-12-31		282	f
b34fc2fc-5827-4c6e-b3ba-fd011d78ad6c	2026-01-01 01:32:04.3685+00	\N	t	MAXIMILIANO HEREDIA	7777777			7777777	\N	3	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	543.2000	2025-12-31		3	f
c8e4b9ad-e890-4311-bc4a-d4c8890ffab7	2026-01-01 01:32:04.502752+00	\N	t	RUFINO FERMIN	7777777			7777777	\N	4	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9455.2000	2025-12-31		4	f
111d0472-ada6-443f-b82a-9c0e29bc88e7	2026-01-01 01:32:04.662051+00	\N	t	ANCELMO HEREDIA	7777777			7777777	\N	5	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1916.1750	2025-12-31		5	f
119c77aa-c357-4751-9516-ee6ec030c013	2026-01-01 01:32:04.798754+00	\N	t	JOSE CORRALES	7777777			7777777	\N	6	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13000.5750	2025-12-31		6	f
f58b90c9-ef7f-479f-b5d8-31b206aefc1d	2026-01-01 01:32:04.949686+00	\N	t	ROBERTO COLQUE	7777777			7777777	\N	7	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2788.4250	2025-12-31		7	f
c0e73fa2-ed82-49d3-bc51-ecf60b5f87a5	2026-01-01 01:32:05.095941+00	\N	t	HILARIA ARRAZOLA ARNEZ	7777777			7777777	\N	8	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9899.0250	2025-12-31		8	f
2fa81894-8f73-4435-8e29-4e03e1e475ea	2026-01-01 01:32:05.2229+00	\N	t	MARCELINO GUTIERREZ	7777777			7777777	\N	9	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1544.8750	2025-12-31		9	f
c4b66620-f193-4f78-84df-de97145ec27c	2026-01-01 01:32:05.351646+00	\N	t	TOMAS HEREDIA	7777777			7777777	\N	10	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3296.0250	2025-12-31		10	f
5376b072-28aa-4aca-93c8-b26130afce32	2026-01-01 01:32:05.470514+00	\N	t	VILMA HEREDIA ORELLANA	7777777			7777777	\N	11	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		11	f
c5d1d29e-5bce-49eb-bcfd-d45f094d9fc1	2026-01-01 01:32:05.599774+00	\N	t	AIDA HEREDIA ORELLANA	7777777			7777777	\N	12	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1855.5750	2025-12-31		12	f
e6cae507-9810-40af-95be-fc4cfc4c859b	2026-01-01 01:32:05.878118+00	\N	t	GLORIA HEREDIA ORELLANA	7777777			7777777	\N	14	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2760.3750	2025-12-31		14	f
c5a01f14-61df-4835-9ab3-f12e2bf0919a	2026-01-01 01:32:06.01232+00	\N	t	GROVER OROZCO	7777777			7777777	\N	15	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		15	f
99c8bdd0-3083-4c4f-9df6-171112b3fa71	2026-01-01 01:32:06.144852+00	\N	t	LEONARDO MARTINEZ	7777777			7777777	\N	16	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3142.5750	2025-12-31		16	f
46f7c726-c7bc-4c56-a309-2c72c7d3c335	2026-01-01 01:32:06.272951+00	\N	t	FREDDY RODRIGUEZ	7777777			7777777	\N	17	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5210.8000	2025-12-31		17	f
a867640a-c4c7-430f-8cc8-cc7d306932dc	2026-01-01 01:32:06.40638+00	\N	t	JUAN RODRIGUEZ	7777777			7777777	\N	18	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9390.000	2025-12-31		18	f
4942d54b-f6d7-44c0-ae8f-25d4e8bb6749	2026-01-01 01:32:06.536453+00	\N	t	ILARION ANTEZANA	7777777			7777777	\N	19	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	172.500	2025-12-31		19	f
1a9ff5d6-2223-4313-b850-d1315f639186	2026-01-01 01:32:06.666634+00	\N	t	LIMBERT SOLIS ALMENDRAS	7777777			7777777	\N	20	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		20	f
22849e8e-9d74-4fc1-895a-8aa040310b4e	2026-01-01 01:32:06.792316+00	\N	t	MARGARITA IBARRA	7777777			7777777	\N	21	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		21	f
a85e619e-528e-4283-a60f-d72ebe28e8ef	2026-01-01 01:32:06.924841+00	\N	t	NICOLAS IBARRA	7777777			7777777	\N	22	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5239.7750	2025-12-31		22	f
df76d6e1-6f7d-463e-a035-54d38ec57b29	2026-01-01 01:32:07.081944+00	\N	t	CLAUDIA FABIOLA SOLIZ	7777777			7777777	\N	23	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3677.6000	2025-12-31		23	f
58edd11b-de70-4596-ad18-6b14a5e8d713	2026-01-01 01:32:07.35128+00	\N	t	JAVIER LOPEZ	7777777			7777777	\N	25	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	745.4500	2025-12-31		25	f
9dea2bce-7d73-41f3-8026-2ff9984794e5	2026-01-01 01:32:07.479923+00	\N	t	PAULINA CLAROS SOLIS	7777777			7777777	\N	26	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13070.5500	2025-12-31		26	f
6ef02d11-3e42-4f80-907f-2ff1cbb0b00a	2026-01-01 01:32:07.63042+00	\N	t	FELICIDAD ORELLANA	7777777			7777777	\N	27	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4567.4250	2025-12-31		27	f
38735a89-6ab6-4a35-9f95-4eb80ba607be	2026-01-01 01:32:07.764132+00	\N	t	FRIDA GARCIA	7777777			7777777	\N	28	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		28	f
bd195a4f-49c6-4de5-abb8-216f487baf8a	2026-01-01 01:32:07.92047+00	\N	t	ASTERIA SANCHEZ	7777777			7777777	\N	29	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5124.3250	2025-12-31		29	f
e5e3f711-4862-48fd-9485-ac1c88d68c09	2026-01-01 01:32:08.047946+00	\N	t	SEBASTIAN NINA MARTINEZ	7777777			7777777	\N	30	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	11770.1750	2025-12-31		30	f
c3752fba-3e0e-4bdd-bf49-9f8bab9d234c	2026-01-01 01:32:08.199452+00	\N	t	BERNARDINO BUITRAGO	7777777			7777777	\N	31	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		31	f
1a938e19-1bd4-4ee1-a978-962079c33e92	2026-01-01 01:32:08.329449+00	\N	t	PANFILO SIACARA	7777777			7777777	\N	32	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1885.6500	2025-12-31		32	f
87e69c11-d375-4058-9e13-348eaa3a7c89	2026-01-01 01:32:08.585824+00	\N	t	JULIA SANCHEZ	7777777			7777777	\N	34	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2133.3000	2025-12-31		34	f
29e49179-a27a-4fd4-be12-eb734052fcc1	2026-01-01 01:32:08.842498+00	\N	t	JULIANA VARGAS	7777777			7777777	\N	36	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4216.1750	2025-12-31		36	f
e555c1d9-4ecb-4256-9413-9760d1c206ab	2026-01-01 01:32:08.964414+00	\N	t	CLARA VDA. DE SUAZNABAR	7777777			7777777	\N	37	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4379.2750	2025-12-31		37	f
1853bd5c-292b-4cb5-b6d9-c1ae14a2f31a	2026-01-01 01:32:09.110676+00	\N	t	MAXIMILIANO SUAZNABAR	7777777			7777777	\N	38	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5655.4500	2025-12-31		38	f
108ed11b-9c7b-48e1-8fd3-60fa6aade281	2026-01-01 01:32:09.271537+00	\N	t	SIXTA HUALLPA ESPINOZA	7777777			7777777	\N	39	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	11015.3500	2025-12-31		39	f
4a2e6366-c506-4309-af3c-455c215a2386	2026-01-01 01:32:09.410315+00	\N	t	JULIANA VDA. DE SOLIS	7777777			7777777	\N	40	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		40	f
12675072-3c83-45f3-9f31-8fd5f86493b1	2026-01-01 01:32:09.556091+00	\N	t	SUSANA ACUÑA DE FUENTES	7777777			7777777	\N	41	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5592.5750	2025-12-31		41	f
9adf6fb2-abf9-4f6a-8485-53397141721b	2026-01-01 01:32:09.69085+00	\N	t	ELIZABETH CHALLAPA	7777777			7777777	\N	42	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	155.9500	2025-12-31		42	f
2d340055-3728-4e62-9a54-e21addd373f1	2026-01-01 01:32:09.840203+00	\N	t	NORMA VALENCIA	7777777			7777777	\N	43	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7723.0250	2025-12-31		43	f
72229f65-903c-4e87-ade0-1e85867ff514	2026-01-01 01:32:09.957842+00	\N	t	PABLO VALENCIA	7777777			7777777	\N	44	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7550.6500	2025-12-31		44	f
b9a467ef-5bde-4bc7-8349-1634b587ed8c	2026-01-01 01:32:10.107685+00	\N	t	DIONICIO MONTAÑO	7777777			7777777	\N	45	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2617.1500	2025-12-31		45	f
5da33db7-cf43-4c0f-90fd-4eeaa564f1f5	2026-01-01 01:32:10.421603+00	\N	t	ENRIQUE ZURITA	7777777			7777777	\N	47	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		47	f
f25998d6-92a7-4421-ba65-b055e55f9ed5	2026-01-01 01:32:10.580526+00	\N	t	JOSE LUIS LOPEZ	7777777			7777777	\N	48	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1135.0250	2025-12-31		48	f
cd4d18ac-ef4b-412f-b47f-6a900fcfb305	2026-01-01 01:32:10.733012+00	\N	t	JUSTINA LOPEZ	7777777			7777777	\N	49	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3003.5750	2025-12-31		49	f
50478413-12b5-4401-a8fe-38f045c599af	2026-01-01 01:32:10.876913+00	\N	t	FLORENCIA SANCHEZ	7777777			7777777	\N	50	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8380.9250	2025-12-31		50	f
0d775a1b-74a9-4361-9524-f5e53cc032b9	2026-01-01 01:32:11.029011+00	\N	t	ISIDRO LOPEZ	7777777			7777777	\N	51	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		51	f
58416bda-80e9-4373-898d-8ea896a3b546	2026-01-01 01:32:11.17687+00	\N	t	ORLANDO DELGADILLO	7777777			7777777	\N	52	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7504.3250	2025-12-31		52	f
4d243594-c0e3-4bdc-95e5-5ab0bbde5f7c	2026-01-01 01:32:11.30096+00	\N	t	FIDEL COLQUE BARRO	7777777			7777777	\N	53	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9626.4000	2025-12-31		53	f
9009ee8f-8bbe-468b-a46b-f46026f03c35	2026-01-01 01:32:11.434471+00	\N	t	CECILIA SALVATIERRA MAITA	7777777			7777777	\N	54	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2716.6500	2025-12-31		54	f
77a73766-25c1-4d67-a586-5944f3a25d18	2026-01-01 01:32:11.565564+00	\N	t	FANOR AYOROA	7777777			7777777	\N	55	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8310.7750	2025-12-31		55	f
14dc53c8-faa1-4dae-85e0-a5da22675e57	2026-01-01 01:32:11.816901+00	\N	t	VALENTINA AMPUERO (2)	7777777			7777777	\N	57	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5222.7250	2025-12-31		57	f
99240cce-fc1c-4827-90c7-bdc658cbc291	2026-01-01 01:32:11.94363+00	\N	t	FELIPE FERNANDEZ (1)	7777777			7777777	\N	58	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1820.3250	2025-12-31		58	f
41f1f4a3-5aab-4f89-8ac6-005344077dd4	2026-01-01 01:32:43.333844+00	\N	t	MARIO ARRAZOLA HEREDIA	7777777			7777777	\N	304	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	590.250	2025-12-31		304	f
bdcbf4d3-ee79-45c8-9177-93464cf9c143	2026-01-01 01:32:12.333512+00	\N	t	FRANCISCO HEREDIA (2)	7777777			7777777	\N	61	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7833.7000	2025-12-31		61	f
836891d4-e95f-4476-bf2d-df6833c78582	2026-01-01 01:32:12.460952+00	\N	t	FRANCISCO HEREDIA (3)	7777777			7777777	\N	62	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		62	f
f4f87ab5-6c62-4223-8397-2dc0f3a1ada2	2026-01-01 01:32:12.587897+00	\N	t	NESTOR HEREDIA	7777777			7777777	\N	63	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9495.3750	2025-12-31		63	f
656508b3-00fe-4827-b4b5-d04a582dffee	2026-01-01 01:32:12.714912+00	\N	t	MERCEDES SOLIS	7777777			7777777	\N	64	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3707.8000	2025-12-31		64	f
041e4ea3-059a-470d-8347-a7818a6e0aab	2026-01-01 01:32:12.842887+00	\N	t	JUAN VEDIA	7777777			7777777	\N	65	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5755.3000	2025-12-31		65	f
68d04ada-0e07-45b5-bd2c-f7e71ee64a69	2026-01-01 01:32:12.967967+00	\N	t	ALEJANDRO CESPEDES	7777777			7777777	\N	66	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		66	f
221e00e7-c334-4c82-8070-dec0598f7ca6	2026-01-01 01:32:13.092693+00	\N	t	ALFREDO HEREDIA	7777777			7777777	\N	67	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6078.000	2025-12-31		67	f
a075d2fa-902e-4675-9738-6b59b73ec71b	2026-01-01 01:32:13.217354+00	\N	t	JOSE HEREDIA	7777777			7777777	\N	68	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	973.6250	2025-12-31		68	f
f48046c1-97f2-4757-be45-c00731f05ed8	2026-01-01 01:32:13.338713+00	\N	t	EFRAIN IBARRA	7777777			7777777	\N	69	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6601.7250	2025-12-31		69	f
4241ed5a-303e-497c-82f7-9595d88dd60c	2026-01-01 01:32:13.60533+00	\N	t	NEMECIO VARGAS (1)	7777777			7777777	\N	71	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5098.7750	2025-12-31		71	f
a1ce02e0-65ad-411a-b064-c07b5fda155f	2026-01-01 01:32:13.736804+00	\N	t	MARGARITA HEREDIA	7777777			7777777	\N	72	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		72	f
d5867083-0b87-4636-bdc3-1a97c69efbce	2026-01-01 01:32:13.873206+00	\N	t	SINFORIANO LOPEZ	7777777			7777777	\N	73	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5541.1500	2025-12-31		73	f
117e9224-965a-403a-be08-b0a51bf04335	2026-01-01 01:32:14.004896+00	\N	t	INES CESPEDES	7777777			7777777	\N	74	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2613.0250	2025-12-31		74	f
f1626518-4b5b-43be-b97e-aee49da80dca	2026-01-01 01:32:14.125374+00	\N	t	BASILIO MIRANDA	7777777			7777777	\N	75	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	196.8500	2025-12-31		75	f
133950bb-218a-47d9-93d1-4ed54952a75d	2026-01-01 01:32:14.249529+00	\N	t	ABELINO IBARRA	7777777			7777777	\N	76	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5202.8250	2025-12-31		76	f
bf7620c0-e75a-435d-ac85-ff4eebe24212	2026-01-01 01:32:14.365934+00	\N	t	LOURDES HERRERA OROSCO	7777777			7777777	\N	77	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		77	f
4ddaa508-5149-487c-a0e2-0fb8e87a1f8b	2026-01-01 01:32:14.478966+00	\N	t	ELIZABETH GOMEZ VELAZQUES VDA DE ANARATA	7777777			7777777	\N	78	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		78	f
f6806217-18ee-4231-92ec-5f78630ea340	2026-01-01 01:32:14.599942+00	\N	t	JULIO PEREDO	7777777			7777777	\N	79	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2668.1250	2025-12-31		79	f
da9f08dd-7867-4694-9cf5-ea182d03eda4	2026-01-01 01:32:14.731339+00	\N	t	DOLORES MONTAÑO	7777777			7777777	\N	80	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8120.0250	2025-12-31		80	f
48083230-2479-4e1e-9304-7d7d031edfc6	2026-01-01 01:32:14.992721+00	\N	t	WALTER CONDORI	7777777			7777777	\N	82	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		82	f
a3c24276-e409-4330-84e0-567c251368f9	2026-01-01 01:32:15.147876+00	\N	t	EFRAIN LEDEZMA	7777777			7777777	\N	83	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1259.3000	2025-12-31		83	f
6fcd1230-4812-450c-b8ac-a027a20ff4ed	2026-01-01 01:32:15.286818+00	\N	t	MARIA ROSA ZURITA	7777777			7777777	\N	84	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		84	f
cdc68de6-a687-4ccd-8c7c-294523dfdcbe	2026-01-01 01:32:15.429211+00	\N	t	AREA VERDE	7777777			7777777	\N	85	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		85	f
6cbc3b55-bc53-452f-894a-2a7bb7929c0e	2026-01-01 01:32:15.561455+00	\N	t	ANDRES RODRIGUEZ	7777777			7777777	\N	86	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2570.6250	2025-12-31		86	f
8444180b-c453-462b-8de9-c16d4a5880f7	2026-01-01 01:32:15.684768+00	\N	t	EUGENIA VDA.D MONTAÑO	7777777			7777777	\N	87	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5233.0750	2025-12-31		87	f
2ebc7637-7c00-4e83-b41d-87013f82c547	2026-01-01 01:32:15.812843+00	\N	t	VALERIANA CASTRO	7777777			7777777	\N	88	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3845.2250	2025-12-31		88	f
c94367fe-6d92-43e4-b392-e48c62b21bf7	2026-01-01 01:32:15.933333+00	\N	t	CLEMENTE CESPEDES	7777777			7777777	\N	89	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8412.9750	2025-12-31		89	f
5809cb87-55d0-4f86-944f-016467a37a95	2026-01-01 01:32:16.061202+00	\N	t	JOSE GUALBERTO ROCHA	7777777			7777777	\N	90	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		90	f
0907a9db-e3eb-4157-ae95-d1cfe35389ad	2026-01-01 01:32:16.182539+00	\N	t	MARIELA VALENCIA CLAROS	7777777			7777777	\N	91	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5866.250	2025-12-31		91	f
4a6897a3-40f6-4ade-8dae-9e5a97a3faee	2026-01-01 01:32:16.309677+00	\N	t	RICHARD MUÑOZ VARGAS	7777777			7777777	\N	92	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5908.0750	2025-12-31		92	f
731c8963-6270-43e7-8881-43fec925144f	2026-01-01 01:32:16.556529+00	\N	t	SEVERINA VDA. D PEREDO	7777777			7777777	\N	94	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1727.1000	2025-12-31		94	f
313cb6cc-4b02-4d77-9e3f-8281fd1c86f7	2026-01-01 01:32:16.677432+00	\N	t	FELICIANO SANDIVAR	7777777			7777777	\N	95	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	12846.6000	2025-12-31		95	f
de4639a1-50ed-4920-ae5c-5dd1e9a2dbd0	2026-01-01 01:32:16.803157+00	\N	t	CIRILA OVIDIO (1)	7777777			7777777	\N	96	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		96	f
391d3c92-20b2-4a86-8e74-9ed7583553d4	2026-01-01 01:32:16.924796+00	\N	t	TANIA ALVARADO CESPEDES	7777777			7777777	\N	97	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4600.9500	2025-12-31		97	f
e7539002-c942-490a-ac6a-7f3303ed7e1e	2026-01-01 01:32:17.037752+00	\N	t	GROVER RODRIGUEZ	7777777			7777777	\N	98	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	12338.5250	2025-12-31		98	f
53969684-a7b0-488b-8fe0-c9da510f32e4	2026-01-01 01:32:17.161072+00	\N	t	DIONICIO LOPEZ	7777777			7777777	\N	99	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2735.2000	2025-12-31		99	f
efbe794a-d10c-48da-b03b-0455f55b935c	2026-01-01 01:32:17.288926+00	\N	t	ALEX ZAMBRANA	7777777			7777777	\N	100	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		100	f
e3bd2d8c-8cba-4d0b-94db-3c42619e1f2f	2026-01-01 01:32:17.411182+00	\N	t	BENITO CORRALES	7777777			7777777	\N	101	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6287.6750	2025-12-31		101	f
4157e6d2-ecfa-43c7-a491-c1ae7ff17857	2026-01-01 01:32:17.537296+00	\N	t	MARIA ELIZABETH RODRIGUEZ CAMACHO	7777777			7777777	\N	102	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7552.000	2025-12-31		102	f
9d3a60f0-49ad-412e-83af-c23b942416cc	2026-01-01 01:32:17.657644+00	\N	t	HONORATO LOPEZ	7777777			7777777	\N	103	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1365.4500	2025-12-31		103	f
a4f95e66-4855-4156-a2c5-b37362e0b4c6	2026-01-01 01:32:17.88988+00	\N	t	MARINA CESPEDES	7777777			7777777	\N	105	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13415.6250	2025-12-31		105	f
a805f4b9-0a0f-4479-9b70-77945dba7288	2026-01-01 01:32:18.016264+00	\N	t	ROSALIA VARGAS	7777777			7777777	\N	106	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8695.4750	2025-12-31		106	f
8c44635e-2f57-432a-87c2-6915a463d59f	2026-01-01 01:32:18.136314+00	\N	t	JULIA HEREDIA	7777777			7777777	\N	107	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7112.9250	2025-12-31		107	f
ccf6baf6-291c-4b5e-b967-ca4e0c4ec282	2026-01-01 01:32:18.261303+00	\N	t	JUAN CESPEDES	7777777			7777777	\N	108	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7480.9000	2025-12-31		108	f
97e8ebd2-61b2-455c-ba37-ef460d2e26f5	2026-01-01 01:32:18.381251+00	\N	t	SABINA HEREDIA	7777777			7777777	\N	109	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5480.8000	2025-12-31		109	f
c9ed9cab-c356-4503-ae92-9aedc174ee14	2026-01-01 01:32:18.510154+00	\N	t	LUIS VARGAS	7777777			7777777	\N	110	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9510.4250	2025-12-31		110	f
77981414-5a3f-484f-b1ed-19a21fa77c6c	2026-01-01 01:32:18.630507+00	\N	t	JOSE A GARCIA MORALES	7777777			7777777	\N	111	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		111	f
9328a169-c2a4-4422-a348-becf27395975	2026-01-01 01:32:18.757229+00	\N	t	ALEJANDRINA VAZQUEZ	7777777			7777777	\N	112	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2661.3500	2025-12-31		112	f
4fddcb04-934a-4b43-a5c5-4b7a0119a197	2026-01-01 01:32:18.878556+00	\N	t	ANDREA CASTRO	7777777			7777777	\N	113	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	11389.2750	2025-12-31		113	f
d9494a7b-33e3-48b2-9978-3dbb03bce479	2026-01-01 01:32:19.004114+00	\N	t	LUCIA BALDERRAMA	7777777			7777777	\N	114	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	344.500	2025-12-31		114	f
7ecd1b98-758a-4ac1-a89a-decbca721653	2026-01-01 01:32:19.251784+00	\N	t	GERMAN ANTONIO MAMANI	7777777			7777777	\N	116	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2966.000	2025-12-31		116	f
ee3878d3-45da-473e-b2f4-ca61f488b3ac	2026-01-01 01:32:19.499174+00	\N	t	JULIO RIVERA	7777777			7777777	\N	118	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		118	f
07894568-d4d0-49d8-9b79-90ad72f642ae	2026-01-01 01:32:19.619684+00	\N	t	FELICIANO SANDIVAR VARGAS	7777777			7777777	\N	119	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4335.000	2025-12-31		119	f
19f406a8-a473-4e0e-8911-b5d8c4c5ffb3	2026-01-01 01:32:19.731159+00	\N	t	JENNY CESPEDES QUIROGA	7777777			7777777	\N	120	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		120	f
61b3367a-67d1-47c0-9a76-af2a0a26bebb	2026-01-01 01:32:19.842137+00	\N	t	JOVITA CRISTINA PLAZA	7777777			7777777	\N	121	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		121	f
9cd9c2b6-7703-422c-b01f-73aedc313ce8	2026-01-01 01:32:19.967129+00	\N	t	JAVIER VALVERDE	7777777			7777777	\N	122	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		122	f
1033de75-9f74-45c3-b290-926bfb0e0733	2026-01-01 01:32:20.081868+00	\N	t	HOGAR DE NIÑOS	7777777			7777777	\N	123	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		123	f
a51a35bf-5142-44bd-88bb-545c7c2d464e	2026-01-01 01:32:20.399379+00	\N	t	ROBERTA MITMA	7777777			7777777	\N	125	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7256.2750	2025-12-31		125	f
2deb4f8d-8c1c-458b-975f-02d1381ae47d	2026-01-01 01:32:20.54802+00	\N	t	DECIDERIO MAMANI	7777777			7777777	\N	126	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3226.000	2025-12-31		126	f
2af411d5-195a-41bd-9deb-4e65ba826c3a	2026-01-01 01:32:20.690844+00	\N	t	WILMA AYDEE MONTAÑO L.	7777777			7777777	\N	127	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3152.000	2025-12-31		127	f
3fc20c14-109f-46bb-b151-5ea324436308	2026-01-01 01:32:20.845413+00	\N	t	EULOGIA LUIZAGA CANDIA	7777777			7777777	\N	128	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7011.000	2025-12-31		128	f
a5dc22c9-eaa8-4e84-be3c-88aae70b72c2	2026-01-01 01:32:20.989718+00	\N	t	MARIO TUSCO	7777777			7777777	\N	129	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		129	f
bd4ea028-2819-4e7b-ba4a-1e616b76b6cf	2026-01-01 01:32:21.140391+00	\N	t	ALBINA SALAZAR	7777777			7777777	\N	130	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6437.8000	2025-12-31		130	f
50d362fb-b3ea-4ad7-b711-2be844a22c66	2026-01-01 01:32:21.283004+00	\N	t	FRANCISCO CANDIA	7777777			7777777	\N	131	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	14482.9000	2025-12-31		131	f
73a58e0a-78c4-4c5c-87cf-78684aa5b6d1	2026-01-01 01:32:21.425992+00	\N	t	TRIFON CLAROS	7777777			7777777	\N	132	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	10392.2250	2025-12-31		132	f
dbab45aa-6069-4e42-a9ab-b705d3a31b62	2026-01-01 01:32:21.547946+00	\N	t	JUAN PEDRO GARCIA	7777777			7777777	\N	133	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		133	f
4b906399-28f2-45e3-8edb-9e5c6620f849	2026-01-01 01:32:21.670744+00	\N	t	NEMECIO VARGAS (2)	7777777			7777777	\N	134	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	255.2000	2025-12-31		134	f
c4add11e-6368-4208-bcdc-9cd132e494f1	2026-01-01 01:32:21.804334+00	\N	t	CIRILO CESPEDES	7777777			7777777	\N	135	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		135	f
c3ece66f-8b75-4e3c-8805-82312cb26061	2026-01-01 01:32:22.06859+00	\N	t	PASTOR PEREDO	7777777			7777777	\N	137	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3690.4250	2025-12-31		137	f
a7d5fc83-ed5a-4bb2-aa37-c7a688a3f3b2	2026-01-01 01:32:22.194822+00	\N	t	CARMEN HEREDIA DE BERNAL	7777777			7777777	\N	138	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6902.8000	2025-12-31		138	f
74f2df01-9d33-47f4-8e3a-660bd2795c45	2026-01-01 01:32:22.317496+00	\N	t	JULIAN HEREDIA	7777777			7777777	\N	139	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8183.250	2025-12-31		139	f
24efb0d3-d30f-4cfd-8f17-e9cff6db6c20	2026-01-01 01:32:22.44507+00	\N	t	DANIELA VALENCIA	7777777			7777777	\N	140	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5247.7000	2025-12-31		140	f
9f428deb-9b0e-4f0f-8c98-00232fc387e7	2026-01-01 01:32:22.566817+00	\N	t	RENAN CHUNGARA	7777777			7777777	\N	141	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		141	f
b6d390e3-2b49-4ed0-860d-a67fe6a651fb	2026-01-01 01:32:22.689204+00	\N	t	EMILIANA COLQUE	7777777			7777777	\N	142	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2477.6000	2025-12-31		142	f
794792ed-9ab2-45db-ba9a-58df0e3ed1d9	2026-01-01 01:32:22.809342+00	\N	t	BRADEN ELOY TORRES	7777777			7777777	\N	143	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		143	f
72d1bb1f-508d-407f-ad27-d0f49615f161	2026-01-01 01:32:22.930996+00	\N	t	YOLA CHOQUE	7777777			7777777	\N	144	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2316.000	2025-12-31		144	f
987ed432-3fb7-422d-b0c3-7aeebbdda82c	2026-01-01 01:32:23.054142+00	\N	t	DANIEL FLORES	7777777			7777777	\N	145	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		145	f
2272bc6b-372e-49f7-ab1a-3b7ad6cc0910	2026-01-01 01:32:23.192551+00	\N	t	DAYANA LUZMILA FLORES ALVARADO	7777777			7777777	\N	146	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4923.1000	2025-12-31		146	f
8c14decd-fc88-4562-846d-28c3b32906f5	2026-01-01 01:32:23.324571+00	\N	t	JUANA HILDA DE VARGAS	7777777			7777777	\N	147	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2099.4500	2025-12-31		147	f
e09b13f4-c949-490e-a4d0-cc3edd28ac7f	2026-01-01 01:32:23.474282+00	\N	t	PEDRO VARGAS OVIDIO	7777777			7777777	\N	148	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3296.9500	2025-12-31		148	f
84098601-de8d-44d0-9479-2a4a5b518399	2026-01-01 01:32:23.771608+00	\N	t	ELMER SUAZNABAR	7777777			7777777	\N	150	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8211.2750	2025-12-31		150	f
821083a5-4020-4e9b-83ac-db461dae75b1	2026-01-01 01:32:23.918617+00	\N	t	ERASMO FERNANDEZ ALARCON	7777777			7777777	\N	151	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	544.1250	2025-12-31		151	f
a998c60c-38ab-41f6-9280-63b4d2b5a9a8	2026-01-01 01:32:24.064366+00	\N	t	MARIA ELVIRA MORON	7777777			7777777	\N	152	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		152	f
58952a9c-aac7-4b6b-a838-9022734852f5	2026-01-01 01:32:24.185897+00	\N	t	JUAN JOSE HEREDIA	7777777			7777777	\N	153	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6656.7750	2025-12-31		153	f
162074a5-2821-491d-a7ba-bf43b2a49830	2026-01-01 01:32:24.314367+00	\N	t	DEMETRIO RIVERA	7777777			7777777	\N	154	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5534.8250	2025-12-31		154	f
64a93fc5-ab81-4e01-8601-f5d3fdd9c152	2026-01-01 01:32:24.449216+00	\N	t	BENITO RODRIGUEZ PONCE	7777777			7777777	\N	155	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6752.7000	2025-12-31		155	f
a64c8bb2-05c1-4089-8301-bdeb91880476	2026-01-01 01:32:24.578579+00	\N	t	JAIME JUYARI	7777777			7777777	\N	156	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5796.000	2025-12-31		156	f
acfda353-2d7b-47e7-ae81-7ee537f8a8f1	2026-01-01 01:32:24.70581+00	\N	t	JOSE LUIS FERNANDEZ	7777777			7777777	\N	157	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13323.5250	2025-12-31		157	f
d0c81df8-fa06-4361-8c9f-1df970fb1773	2026-01-01 01:32:24.823958+00	\N	t	FELIX VALENCIA	7777777			7777777	\N	158	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		158	f
eab74f48-c2dd-4e8e-bb64-04d6def24de5	2026-01-01 01:32:24.938208+00	\N	t	RUBEN BARRIGAS ZURITA	7777777			7777777	\N	159	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		159	f
5f98c2aa-178c-4c75-852f-420894c15d00	2026-01-01 01:32:25.174505+00	\N	t	JULIA SOLIS SOTO	7777777			7777777	\N	161	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4517.000	2025-12-31		161	f
0f97b28d-1b2c-481b-8dfe-55fc3194c67d	2026-01-01 01:32:25.299936+00	\N	t	BEATRIZ HEREDIA	7777777			7777777	\N	162	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2519.8250	2025-12-31		162	f
a5ced6a4-6673-46af-8210-66cdc3f2c18f	2026-01-01 01:32:25.424726+00	\N	t	EDWARD GUZMAN	7777777			7777777	\N	163	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	570.7750	2025-12-31		163	f
966f2cec-edd4-498a-9a31-9a9888c28abe	2026-01-01 01:32:25.543651+00	\N	t	DANIEL OROSCO	7777777			7777777	\N	164	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2578.5750	2025-12-31		164	f
21186c91-b175-4b79-885f-9926f6652a7e	2026-01-01 01:32:25.66899+00	\N	t	ESTEBAN PACO GARCIA	7777777			7777777	\N	165	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5528.5500	2025-12-31		165	f
d0b4237b-3727-48ec-8dee-4cb5d418a7e7	2026-01-01 01:32:25.914988+00	\N	t	DIONICIO HEREDIA	7777777			7777777	\N	167	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	226.8250	2025-12-31		167	f
761f7375-1ffa-4de8-89b4-6e7b93cb86ec	2026-01-01 01:32:26.066262+00	\N	t	JOSE LUIS GARCIA	7777777			7777777	\N	168	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13665.7750	2025-12-31		168	f
35ce813b-a08b-452e-b2d0-d4b78049568a	2026-01-01 01:32:26.21049+00	\N	t	JESUS GONZALES	7777777			7777777	\N	169	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1849.5250	2025-12-31		169	f
59c6e8b8-b9aa-4ff8-a84c-f33949c401db	2026-01-01 01:32:26.339758+00	\N	t	JAIME TRUJILLO	7777777			7777777	\N	170	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1647.5750	2025-12-31		170	f
2af9ee51-c6f1-412f-b685-c2ac156a8d66	2026-01-01 01:32:26.598946+00	\N	t	TERESA ROMERO	7777777			7777777	\N	172	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5770.500	2025-12-31		172	f
45beac96-25a6-47eb-ab48-0d0cbffbfae6	2026-01-01 01:32:26.762865+00	\N	t	JOSE LUIS TRUJILLO	7777777			7777777	\N	173	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		173	f
bc19e0f1-28c8-4801-a772-78aaa1b6aa07	2026-01-01 01:32:27.280739+00	\N	t	DEMICIANO VALLEJOS ROJAS	7777777			7777777	\N	177	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		177	f
a3785978-1ff3-403c-b9a1-e3b4df710361	2026-01-01 01:32:27.542531+00	\N	t	ESCUELA GUADALUPE	7777777			7777777	\N	179	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	522.5500	2025-12-31		179	f
517d1480-d360-44fc-8c34-6a2db7395458	2026-01-01 01:32:27.679009+00	\N	t	GREGORIA ENRIQUEZ	7777777			7777777	\N	180	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2565.6750	2025-12-31		180	f
84214b74-5bf3-4cc9-9ffb-e03918069693	2026-01-01 01:32:27.803989+00	\N	t	MARCELINO FLORES VARGAS	7777777			7777777	\N	181	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2828.4000	2025-12-31		181	f
b8956f1a-541f-4b33-a3f1-ed276d6be720	2026-01-01 01:32:27.935332+00	\N	t	ASTERIA VARGAS O.	7777777			7777777	\N	182	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3954.5750	2025-12-31		182	f
bbea725d-b15a-46cb-9c53-568581423fd6	2026-01-01 01:32:27.028364+00	\N	t	FABIOLA CLAROS	7777777			7777777	\N	175	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2325.6500	2026-01-03		175	f
8c35ac64-abc4-4c83-abca-a839e49577db	2026-01-01 01:32:27.149801+00	\N	t	OLIVIA CLAROS	7777777			7777777	\N	176	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7466.4500	2026-01-08		176	f
e117b18f-ddff-4e59-a7f8-6bb13844346b	2026-01-01 01:32:28.198557+00	\N	t	GERARDO LUNA PINTO	7777777			7777777	\N	184	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6105.000	2025-12-31		184	f
69d8d8d8-0168-475a-9e0e-4405f8de3e8f	2026-01-01 01:32:28.443457+00	\N	t	JAIME BERMUDEZ LA FUENTE	7777777			7777777	\N	186	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1013.1000	2025-12-31		186	f
ad1f558a-f835-4154-a582-6248d8214779	2026-01-01 01:32:28.562931+00	\N	t	ANA MARIA HEREDIA	7777777			7777777	\N	187	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	352.1500	2025-12-31		187	f
c46499e4-668f-4bff-b593-9700ab1b80bc	2026-01-01 01:32:28.807103+00	\N	t	JAIME CHOQUE	7777777			7777777	\N	189	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		189	f
bdbac954-2c78-47cc-a12f-9dab4fecc29a	2026-01-01 01:32:28.929079+00	\N	t	MARINA CHALLAPA S.	7777777			7777777	\N	190	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		190	f
19b846fd-6611-4ec9-8c77-ab7aaf2ba10a	2026-01-01 01:32:29.050853+00	\N	t	RAUL MAMANI BARCO	7777777			7777777	\N	191	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7962.1750	2025-12-31		191	f
77300937-e22d-4284-965a-3cc07a69a9c6	2026-01-01 01:32:29.173186+00	\N	t	GUILLERMO CLAROS	7777777			7777777	\N	192	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4099.1250	2025-12-31		192	f
a3138b77-8dd4-4156-b07a-61d20f4e550b	2026-01-01 01:32:29.447815+00	\N	t	EUSEBIO MALDONADO	7777777			7777777	\N	194	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5903.500	2025-12-31		194	f
392cd88e-7f43-44c6-bcbe-36cc2f18693a	2026-01-01 01:32:29.56757+00	\N	t	SEBASTIAN NINA MARTINEZ	7777777			7777777	\N	195	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4310.5750	2025-12-31		195	f
bde75a98-00f8-403f-ae61-547cc90dffbd	2026-01-01 01:32:29.689506+00	\N	t	JOSE RAMIRO MEJIA	7777777			7777777	\N	196	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5802.250	2025-12-31		196	f
8afeef67-edf2-4ed1-895d-201b87d978f2	2026-01-01 01:32:29.920179+00	\N	t	XIMENA ZAMORA GARCIA	7777777			7777777	\N	198	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6321.3000	2025-12-31		198	f
2533c34b-443d-4bfb-b7dc-a7120f9ff806	2026-01-01 01:32:30.041707+00	\N	t	VIRGINIA ZURITA	7777777			7777777	\N	199	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2903.2250	2025-12-31		199	f
7b5089a5-d561-45df-a2c1-aec19a17b592	2026-01-01 01:32:30.453526+00	\N	t	SERAFINA AGUILAR MERIDA	7777777			7777777	\N	202	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		202	f
958f8067-4c6b-41ee-bdf3-b35ef9bdc719	2026-01-01 01:32:30.595795+00	\N	t	CONCEPCION HEREDIA	7777777			7777777	\N	203	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		203	f
ec99581a-43f8-4737-83b6-bc1ed7e29ea9	2026-01-01 01:32:30.728431+00	\N	t	EFRAIN IBARRA (2)	7777777			7777777	\N	204	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4585.2000	2025-12-31		204	f
3b444922-b8bb-4f5a-90a6-401179b210dc	2026-01-01 01:32:30.868704+00	\N	t	RICARDO CESPEDES	7777777			7777777	\N	205	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7345.3750	2025-12-31		205	f
05d5b252-668e-4f36-ab50-8c8100316232	2026-01-01 01:32:30.996604+00	\N	t	SUSANA SANDAGORGA CRESPO	7777777			7777777	\N	206	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		206	f
a5dee7eb-677f-4727-9421-6dae7c083f96	2026-01-01 01:32:31.126271+00	\N	t	NARCISO CORRALES OROZCO	7777777			7777777	\N	207	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3115.8000	2025-12-31		207	f
89a71600-2bce-4c58-85b4-c9b684f97678	2026-01-01 01:32:31.250752+00	\N	t	NAYRA IBARRA INTURIAS	7777777			7777777	\N	208	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		208	f
20a93627-492e-4256-9b63-4309a5db8ed3	2026-01-01 01:32:31.393633+00	\N	t	JIMMY  SUAZNABAR LICONA	7777777			7777777	\N	209	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		209	f
aa76c5c1-da5c-4d96-9195-ad493f2221b5	2026-01-01 01:32:31.522295+00	\N	t	EDSON ZUASNABAR LICONA	7777777			7777777	\N	210	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		210	f
2124bb26-189d-43f0-8cc8-dca3732ad496	2026-01-01 01:32:31.661482+00	\N	t	MARIA ANTONIETA POVEDA SORIA	7777777			7777777	\N	211	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3226.750	2025-12-31		211	f
b6d5525b-7b28-49d1-869c-63571087871a	2026-01-01 01:32:31.791107+00	\N	t	FELIX CHOQUE GONZALES	7777777			7777777	\N	212	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4090.250	2025-12-31		212	f
c9b909e0-2425-4895-ac16-eee302405944	2026-01-01 01:32:32.031335+00	\N	t	ALBINA ACUÑA LOPEZ	7777777			7777777	\N	214	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2651.8000	2025-12-31		214	f
35f02c20-09ec-4951-a29c-acaf2d6ab78f	2026-01-01 01:32:32.150304+00	\N	t	ADAN VALLEJOS ROJAS	7777777			7777777	\N	215	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		215	f
951c3c89-5b52-4692-8d7d-7e615c2d689f	2026-01-01 01:32:32.393531+00	\N	t	JUAN C LESCANO GONZALES	7777777			7777777	\N	217	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2089.500	2025-12-31		217	f
de340ddf-9321-4c4f-9f2f-877fb36ed075	2026-01-01 01:32:32.528783+00	\N	t	ENRIQUE SOLIS MARQUINA	7777777			7777777	\N	218	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5445.7000	2025-12-31		218	f
6b16ef08-cfc4-4463-89be-3336c1678c4e	2026-01-01 01:32:32.653877+00	\N	t	GUALBERTO TICONA SOTO	7777777			7777777	\N	219	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5772.7250	2025-12-31		219	f
d0e77232-5f5a-4574-8407-d4c74b498384	2026-01-01 01:32:32.919065+00	\N	t	JESUS HINOJOSA ORELLANA	7777777			7777777	\N	221	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4768.7000	2025-12-31		221	f
292bee02-4bae-48b1-9e2f-c1533e3acfd3	2026-01-01 01:32:33.168448+00	\N	t	JUAN RAUL MENDEZ MORALES	7777777			7777777	\N	223	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2075.4500	2025-12-31		223	f
56b05966-e289-4c4c-bc12-a29357b081c0	2026-01-01 01:32:33.284937+00	\N	t	MARINA VELASQUEZ DE MENDEZ	7777777			7777777	\N	224	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2099.2000	2025-12-31		224	f
c7be1ebc-c4a6-44e4-9704-0fe4f83a5670	2026-01-01 01:32:33.409538+00	\N	t	VILMA ANDRADE VELIZ	7777777			7777777	\N	225	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3360.6000	2025-12-31		225	f
04bc5062-f6ba-45d7-9d2c-ac65aa697014	2026-01-01 01:32:33.775841+00	\N	t	ALICIA ARISPE MOSQUERA	7777777			7777777	\N	228	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	12220.9500	2025-12-31		228	f
02dd7e48-28c9-4aaf-a673-83e86612945e	2026-01-01 01:32:34.139311+00	\N	t	SAMUEL ARRAZOLA ARNEZ	7777777			7777777	\N	231	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4632.000	2025-12-31		231	f
bf4eead3-de35-482c-b4e3-3634dce2151f	2026-01-01 01:32:34.260913+00	\N	t	FERNANDO HEREDIA	7777777			7777777	\N	232	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2875.000	2025-12-31		232	f
1bfd624e-dd51-41de-8cb2-fa4533ae4b08	2026-01-01 01:32:34.376103+00	\N	t	FELIX AGUILAR	7777777			7777777	\N	233	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		233	f
0d617991-f0a4-4228-8be4-99d4ab29bea5	2026-01-01 01:32:34.760031+00	\N	t	LOURDES CAMACHO GARCIA	7777777			7777777	\N	236	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1328.4250	2025-12-31		236	f
94d7665a-4fc1-4647-9189-490938c136dc	2026-01-01 01:32:34.882447+00	\N	t	ELSA MARIA GARNICA DE OJEDA	7777777			7777777	\N	237	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		237	f
148e7cb9-3638-4730-8ae2-5bd49603e90c	2026-01-01 01:32:35.424268+00	\N	t	GERMAN MENDOZA M.	7777777			7777777	\N	241	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		241	f
256d569c-1833-4e43-9bdd-ec8974188157	2026-01-01 01:32:35.923808+00	\N	t	JUAN ROGELIO VILLEGAS	7777777			7777777	\N	245	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		245	f
31d2666b-3aeb-4129-9e79-d65b58d2ed33	2026-01-01 01:32:36.472227+00	\N	t	VILMA JANY PACARA TUMIRI	7777777			7777777	\N	249	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3151.6000	2025-12-31		249	f
92edee2c-d66c-4e8a-8ced-ce2af34a8822	2026-01-01 01:32:36.593202+00	\N	t	MARIA INES CESPEDES HEREDIA	7777777			7777777	\N	250	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1162.1500	2025-12-31		250	f
4c3e1a6f-3b6a-4784-99ef-7a128ae6419e	2026-01-01 01:32:37.119367+00	\N	t	ORLANDO ESCOBAR POMA	7777777			7777777	\N	254	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	459.4500	2025-12-31		254	f
c49f0400-9143-41a6-a304-149544866e4b	2026-01-01 01:32:38.733405+00	\N	t	JUANA ORELLANA SOLIS	7777777			7777777	\N	267	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2840.3750	2025-12-31		267	f
05e9e835-2816-44d7-8fb3-16acd4d2bf29	2026-01-01 01:32:38.993629+00	\N	t	CARLOS CUISARA HINOJOSA	7777777			7777777	\N	269	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	870.3750	2025-12-31		269	f
871a64bc-21e0-4033-9fca-bfd5f7b8dbf3	2026-01-01 01:32:39.12245+00	\N	t	BEATRIZ TERCEROS ZAPATA	7777777			7777777	\N	270	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		270	f
7bfd9507-2c8f-4504-b5f0-213c4a33b39c	2026-01-01 01:32:39.265672+00	\N	t	TEREZA BEDOYA NAVARRO	7777777			7777777	\N	271	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	660.7750	2025-12-31		271	f
50f7dc6b-91fe-46d7-88de-566078e57d3f	2026-01-01 01:32:39.6834+00	\N	t	SERGIO VALLEJOS HEREDIA	7777777			7777777	\N	274	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	188.7750	2025-12-31		274	f
c9c8b0d8-99e7-4745-89b0-9483a6d45d46	2026-01-01 01:32:39.798643+00	\N	t	MARIEL AGUILAR BERNABE DE CUELLAR	7777777			7777777	\N	275	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	398.2000	2025-12-31		275	f
a2e5f2e6-8d32-4f1a-a22d-42bcadd0ef8d	2026-01-01 01:32:40.059795+00	\N	t	EDIWIN COLQUE LUIS	7777777			7777777	\N	277	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2460.3250	2025-12-31		277	f
ab231d82-741d-4fd8-bdf0-8c3560a2b4bf	2026-01-01 01:32:40.319315+00	\N	t	MIRIAN VARGAS HEREDIA	7777777			7777777	\N	279	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	841.1750	2025-12-31		279	f
c23b54f9-212f-44e7-9e06-0e88307caa81	2026-01-01 01:32:40.444403+00	\N	t	LIDIA CLAROS HEREDIA	7777777			7777777	\N	280	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2453.4250	2025-12-31		280	f
b3a51ed9-5106-4fe9-a08c-c9ee2e879503	2026-01-01 01:32:40.960108+00	\N	t	SANTIAGO GONZALES RAMOS	7777777			7777777	\N	284	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	412.1750	2025-12-31		284	f
e2db9406-3ba0-4e7b-ad88-046f905b5ce5	2026-01-01 01:32:41.077567+00	\N	t	GIMENA SUASNABAR LEDEZMA	7777777			7777777	\N	285	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2700.3750	2025-12-31		285	f
9eaf7134-42d4-4f23-80f8-617ef4495386	2026-01-01 01:32:41.305739+00	\N	t	SONIA VALENCIA IBARRA	7777777			7777777	\N	287	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	350.4500	2025-12-31		287	f
27028ad5-d3b1-424f-86cc-8e349d804005	2026-01-01 01:32:41.545455+00	\N	t	IVAN VLADIMIR VILLCA FERNANDEZ	7777777			7777777	\N	289	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1238.3500	2025-12-31		289	f
c94ae4cc-4ab6-4014-aa17-7cd30212e230	2026-01-01 01:32:41.663795+00	\N	t	ESPERANZA CHURA	7777777			7777777	\N	290	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2251.5250	2025-12-31		290	f
2e1ca09c-1e51-46df-b6cf-e3573276d7eb	2026-01-01 01:32:41.898172+00	\N	t	NIMER RIONY CHOQUE FLORES	7777777			7777777	\N	292	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1760.500	2025-12-31		292	f
de54fa8f-5989-4edd-b432-540bdad6d159	2026-01-01 01:32:42.129114+00	\N	t	ARTURO CLAROS BORDA	7777777			7777777	\N	294	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1222.5750	2025-12-31		294	f
d9b65b49-db9f-4732-82e1-d67ee884fd90	2026-01-01 01:32:42.251546+00	\N	t	ALEJANDRA LANCHIPA	7777777			7777777	\N	295	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1023.0500	2025-12-31		295	f
b5e938d1-2a77-4d13-896e-9a6449d7f664	2026-01-01 01:32:42.49253+00	\N	t	ALBERTA ROCHA DE RODRIGUEZ	7777777			7777777	\N	297	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	627.750	2025-12-31		297	f
970c9cc0-2684-403b-89a0-b358214f28db	2026-01-01 01:32:42.837345+00	\N	t	JOSE EDUARDO MORALES PEÑARRIETA	7777777			7777777	\N	300	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	538.3000	2025-12-31		300	f
93e411d3-a0ef-48a8-8380-306136e25f76	2026-01-01 01:32:43.088545+00	\N	t	GEOVANA HUANCA ESCALERA	7777777			7777777	\N	302	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	753.250	2025-12-31		302	f
d869c705-b0a9-4c69-b73c-e9a3330ad9b4	2026-01-01 01:32:28.075692+00	\N	t	PIO GUZMAN RODRIGUEZ	7777777			7777777	\N	183	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2173.3000	2025-12-31		183	f
dd67b949-f8de-43dc-a2e9-753e44e9c71d	2026-01-01 01:32:28.682636+00	\N	t	EDGAR MERUBIA ROBLES	7777777			7777777	\N	188	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5125.7750	2025-12-31		188	f
57d776b1-a5a4-4f02-8d59-892236f056fe	2026-01-01 01:32:29.318075+00	\N	t	ANGELICA MORON	7777777			7777777	\N	193	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3238.6000	2025-12-31		193	f
10fb500b-55fe-4955-8018-bd8ddeab44f6	2026-01-01 01:32:29.806071+00	\N	t	JOSE OMAR PRADO ORTIZ	7777777			7777777	\N	197	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7422.3250	2025-12-31		197	f
e1f8ddec-3ac3-4230-b6b2-f97ab4c73981	2026-01-01 01:32:30.329562+00	\N	t	FRANCISCA CESPEDES	7777777			7777777	\N	201	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		201	f
e4f95041-cff4-4b73-b364-3232336debd8	2026-01-01 01:32:32.27135+00	\N	t	FELIPE FLORES BLAS	7777777			7777777	\N	216	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6443.4500	2025-12-31		216	f
0bb64877-98aa-4ada-9608-30781f8ad1bc	2026-01-01 01:32:32.789273+00	\N	t	EUGENIO PACARA CALIZAYA	7777777			7777777	\N	220	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5111.2000	2025-12-31		220	f
1b10e99e-bf9f-4385-bb3b-3cb5d4bccc50	2026-01-01 01:32:33.530385+00	\N	t	PAOLA TERÀN BALDERRAMA	7777777			7777777	\N	226	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2673.3250	2025-12-31		226	f
029f4c2d-9e8c-4ffa-8f6c-ac0a6cc8382b	2026-01-01 01:32:33.898162+00	\N	t	ALEXANDER CLAROS LEDEZMA	7777777			7777777	\N	229	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4254.9000	2025-12-31		229	f
d1bbb3e6-e17b-4b20-8787-93b3fb8ee07d	2026-01-01 01:32:34.017234+00	\N	t	HERMOGENES PATZI JALLAZA	7777777			7777777	\N	230	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6138.250	2025-12-31		230	f
57d1be27-92b6-4751-82a5-a8456d5e05ea	2026-01-01 01:32:34.498926+00	\N	t	TEODOCIA ARCE ESPINOZA	7777777			7777777	\N	234	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5796.0500	2025-12-31		234	f
1889aaac-9be8-4ef7-a38f-f1ec1bc1833f	2026-01-01 01:32:34.638111+00	\N	t	JOSE GONZALO CASTRO SOLIS	7777777			7777777	\N	235	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4308.0500	2025-12-31		235	f
12c6d6d3-4cd9-4fdc-9fb7-8c291a19ea2d	2026-01-01 01:32:35.002277+00	\N	t	FORTUNATA LOPEZ CALICHO	7777777			7777777	\N	238	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1904.6000	2025-12-31		238	f
7fa11f6e-62db-40cf-b010-944017372f40	2026-01-01 01:32:35.151006+00	\N	t	WALTER CHALLAPA LOPEZ	7777777			7777777	\N	239	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2919.0750	2025-12-31		239	f
70e4a285-118d-4195-b91b-1f348cbce1d9	2026-01-01 01:32:35.279586+00	\N	t	EDMUNDO VILLARROEL VILLAROEL	7777777			7777777	\N	240	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4190.9500	2025-12-31		240	f
8963f78c-8da3-4abd-a9cf-61b9156efe3b	2026-01-01 01:32:35.550074+00	\N	t	MARIA JULIA VALLEJOS	7777777			7777777	\N	242	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		242	f
2f5e1f68-2725-49b5-b5b8-7afd33495e0b	2026-01-01 01:32:35.669373+00	\N	t	ARMINDA GALINDO ARNEZ DE HEREDIA	7777777			7777777	\N	243	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3595.0750	2025-12-31		243	f
1dd944f1-4b29-41cc-9fd4-2369e7237d96	2026-01-01 01:32:36.057289+00	\N	t	EUGENIA CUTILI CALSINA VDA DE VILLA	7777777			7777777	\N	246	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3338.6000	2025-12-31		246	f
bedd99e7-edf2-49c4-8d32-1ea5a1ac1d8e	2026-01-01 01:32:36.19506+00	\N	t	MARIO TERCEROS TRUJILLO	7777777			7777777	\N	247	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	277.000	2025-12-31		247	f
4eb35e69-b497-43ef-99a7-b8e879c68782	2026-01-01 01:32:36.327721+00	\N	t	MARLENE ROMERO ROMERO	7777777			7777777	\N	248	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4115.3500	2025-12-31		248	f
6974e585-1075-4724-819f-c70fa88b9ba5	2026-01-01 01:32:36.732844+00	\N	t	EDGAR CAMACHO HEREDIA	7777777			7777777	\N	251	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1204.6500	2025-12-31		251	f
00df6a96-9796-4db0-94ee-0b50a8704174	2026-01-01 01:32:36.862788+00	\N	t	ARIEL VELA MONTERO	7777777			7777777	\N	252	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3172.250	2025-12-31		252	f
5d6d6501-5dc4-4435-87aa-28bceee8e115	2026-01-01 01:32:36.993531+00	\N	t	NESTOR E VELASQUEZ BARRO	7777777			7777777	\N	253	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	75.4000	2025-12-31		253	f
04754f25-3ef9-4368-a90c-7068cbd3ac29	2026-01-01 01:32:37.366055+00	\N	t	TEODORO ANTONIO ANTEZANA A.	7777777			7777777	\N	256	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2850.3000	2025-12-31		256	f
236e0a90-ee4e-4814-ad6c-ddcf9812aae1	2026-01-01 01:32:37.49106+00	\N	t	EDGAR A HOYOS ROMERO	7777777			7777777	\N	257	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1903.0250	2025-12-31		257	f
54700469-b483-438d-8711-317c6056ccb0	2026-01-01 01:32:37.637381+00	\N	t	JUDIT VALLEJOS VALDIVIA	7777777			7777777	\N	258	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1298.3000	2025-12-31		258	f
21e74e47-7a80-4ffb-a85f-5c0dba49fea9	2026-01-01 01:32:38.005894+00	\N	t	JAVIER FELIPE FLORES	7777777			7777777	\N	261	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2268.5750	2025-12-31		261	f
c86fd546-c600-444f-b5b6-ac48873ff39d	2026-01-01 01:32:38.478158+00	\N	t	FREDDY CARLOS RAMOS BARRAL	7777777			7777777	\N	265	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6225.9500	2025-12-31		265	f
97e70dcd-0bb6-4933-b821-d53252a1b07d	2026-01-01 01:32:38.604936+00	\N	t	CARLOS SAIBA CHAMBI	7777777			7777777	\N	266	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3403.000	2025-12-31		266	f
fa9fc52c-fee8-4979-8051-69b33af511c6	2026-01-01 01:32:38.852387+00	\N	t	ARMANDO FAVIAN RIVERO	7777777			7777777	\N	268	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3173.2000	2025-12-31		268	f
cdf6ac46-9801-43b5-92dc-87237e51e585	2026-01-01 01:32:39.413547+00	\N	t	NANCY QUIROGA CAMACHO	7777777			7777777	\N	272	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		272	f
f14f3b11-1352-48a4-945c-6d6336138be0	2026-01-01 01:32:39.565122+00	\N	t	IGLESIA DE DIOS DE LA PROFESIA	7777777			7777777	\N	273	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	940.8000	2025-12-31		273	f
42c79df4-8bad-45c2-bc1b-d70838a6714e	2026-01-01 01:32:39.918454+00	\N	t	JHOVANA TRUJILLO VALENCIA	7777777			7777777	\N	276	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		276	f
6e90b307-b942-4697-8331-98d19ff48813	2026-01-01 01:32:37.773151+00	\N	t	LUIS LANCEA UGARTE	7777777			7777777	\N	259	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	465.8000	2025-12-31		259	f
1a333401-5c17-4858-8358-e4934bdb573c	2026-01-01 01:32:37.89215+00	\N	t	OLGUIN REYES JIMENEZ	7777777			7777777	\N	260	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2850.7750	2025-12-31		260	f
17373416-012e-4753-ac46-a8480b15969a	2026-01-01 01:32:38.234079+00	\N	t	OMAR BERNAL SOLIS	7777777			7777777	\N	263	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		263	f
c69e8b29-fcc9-4552-8d9e-6869d5187a6b	2026-01-01 01:32:38.345778+00	\N	t	SERGIO VALLEJOS HEREDIA	7777777			7777777	\N	264	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3915.8000	2025-12-31		264	f
1f4cc66d-40ed-441b-9e7c-92001e493e35	2026-01-01 01:32:40.184507+00	\N	t	GUSTAVO MARCELO ROJAS ROJAS	7777777			7777777	\N	278	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	802.0750	2025-12-31		278	f
d40ad2a6-0b6c-4cde-86a6-463b128bad67	2026-01-01 01:32:40.576821+00	\N	t	EDUARDO DELGADILLO PRADO	7777777			7777777	\N	281	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		281	f
e6ee918b-0d8d-4d83-975e-9ff25bc5c471	2026-01-01 01:32:40.832924+00	\N	t	MARIA J ESCALANTE GARCIA	7777777			7777777	\N	283	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1338.5500	2025-12-31		283	f
b8bcd5b2-27ea-4a1e-b2d7-6238913dee2e	2026-01-01 01:32:41.195047+00	\N	t	JIMMY SUASNABAR LEDEZMA	7777777			7777777	\N	286	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2090.4500	2025-12-31		286	f
45245773-b59a-401e-8c40-f36105205a4c	2026-01-01 01:32:41.428293+00	\N	t	CARMEN ROSA VARGAS SANTOS	7777777			7777777	\N	288	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1590.7750	2025-12-31		288	f
c9cd2b07-7338-45f8-8ce5-bea27f7b926e	2026-01-01 01:32:41.785208+00	\N	t	OMAR QUIROGA SOLIZ	7777777			7777777	\N	291	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	288.3000	2025-12-31		291	f
78a0ba41-b5b0-4fda-9186-e1c67401a9cd	2026-01-01 01:32:42.012677+00	\N	t	RIDER MAMANI RASGUIDO	7777777			7777777	\N	293	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	753.250	2025-12-31		293	f
b77280e7-af7b-4734-93bf-e0ecc30cd5e3	2026-01-01 01:32:42.367584+00	\N	t	JIMENA ZURITA LOZADA	7777777			7777777	\N	296	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2745.6500	2025-12-31		296	f
c5534908-a6dc-4744-8028-4b82c6cfc41d	2026-01-01 01:32:42.610757+00	\N	t	WENDY NANCY NAVARRO DORADO	7777777			7777777	\N	298	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1509.0750	2025-12-31		298	f
929905b9-0cce-47c5-a4e7-802dc015fddb	2026-01-01 01:32:43.215932+00	\N	t	BASILIO HUANCA RODRIGUEZ	7777777			7777777	\N	303	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	696.000	2025-12-31		303	f
879ec923-ac42-42e5-93f6-cfa03e2defd1	2026-01-01 01:32:43.571513+00	\N	t	MARIA ROSARIO TERRAZAS RIVERA	7777777			7777777	\N	306	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	426.9500	2025-12-31		306	f
c3853103-45a2-4e01-9a2c-0160ca693148	2026-01-01 01:32:43.808761+00	\N	t	IRMA CASTRO SOLIZ	7777777			7777777	\N	308	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	796.250	2025-12-31		308	f
1d7a5391-bf88-4c2e-b6be-acd5460ccdb6	2026-01-01 01:32:43.455034+00	\N	t	DELIA PAREDES CHOQUE	7777777			7777777	\N	305	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	969.8750	2025-12-31		305	f
3c29d80a-ffc6-46c6-8470-7acef6748a6b	2026-01-01 01:32:43.929839+00	\N	t	OVIDIO POMA CORANI	7777777			7777777	\N	309	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1146.3500	2025-12-31		309	f
b9b86116-765a-408d-8a00-5787ef56b10a	2026-01-01 01:32:44.045068+00	\N	t	ROGER MUÑOZ VARGAS	7777777			7777777	\N	310	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	452.000	2025-12-31		310	f
1e8d5045-b779-4393-b332-6956fce23305	2026-01-01 01:32:44.169843+00	\N	t	JOSE GARCIA SUAREZ	7777777			7777777	\N	311	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		311	f
9ccd19d5-103e-4c0e-b0ff-9c4840ff3cbf	2026-01-01 01:32:44.282772+00	\N	t	IGNACIO FERNANDEZ LOZA	7777777			7777777	\N	312	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	217.6750	2025-12-31		312	f
a7ce0074-e633-46b9-b827-f33992454045	2026-01-01 01:32:44.409249+00	\N	t	ROSMERY CLAROS	7777777			7777777	\N	313	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1464.750	2025-12-31		313	f
d6b95c19-2878-401e-bcc3-ba8f9a022041	2026-01-01 01:32:44.52467+00	\N	t	JUVENAL GARCIA MURIEL	7777777			7777777	\N	314	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	295.2750	2025-12-31		314	f
9d29645b-8fbd-4696-bbab-6f757f177e59	2026-01-01 01:32:44.644522+00	\N	t	NATALY ANGULO SALAZAR	7777777			7777777	\N	315	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	108.500	2025-12-31		315	f
e973e4a6-2ba2-435e-9eb9-00409a2e6da1	2026-01-01 01:32:44.766075+00	\N	t	DANIELA LOPEZ	7777777			7777777	\N	316	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	293.0750	2025-12-31		316	f
44d326a5-a313-4eea-8440-57e3db0188b2	2026-01-01 01:32:44.882598+00	\N	t	JUAN CESPEDES ANDIA	7777777			7777777	\N	317	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	343.3000	2025-12-31		317	f
f8351303-e530-4418-8928-a10533ff5b4a	2026-01-01 01:32:44.992879+00	\N	t	MARIO FELIX CLAROS CESPEDES	7777777			7777777	\N	318	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	160.8000	2025-12-31		318	f
b0a7f24c-53f4-4c76-9299-9cd5ef7300ae	2026-01-01 01:32:45.122986+00	\N	t	NEMECIO VARGAS	7777777			7777777	\N	319	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	76.6250	2025-12-31		319	f
60d93965-e4cd-4bfb-9c74-03ad952d751a	2026-01-01 01:32:45.238435+00	\N	t	VILMA HEREDIA ORELLANA	7777777			7777777	\N	320	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	130.2250	2025-12-31		320	f
3f630a42-4cfc-4b33-b530-75efc6628541	2026-01-01 01:32:45.362384+00	\N	t	GLORIA HEREDIA ORELLANA	7777777			7777777	\N	321	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	40.000	2025-12-31		321	f
cbc45fb0-c556-471a-87ee-88d0c196ae31	2026-01-01 01:32:45.493755+00	\N	t	LIDIA BERNAL HEREDIA	7777777			7777777	\N	322	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	40.000	2025-12-31		322	f
84d71f62-9553-45ec-8f62-95bd7f07f3cc	2026-01-01 01:13:28.502209+00	\N	t	DOMINGO NUÑES	0000000	0000000		0000000	\N	1	425a0052-b86d-47ed-af91-8a84cbfc7f65	2025-12-31	\N	7772.9000	2025-12-31		1	f
a037b101-2fd5-480f-bff7-1438fd369b08	2026-01-01 01:32:04.202018+00	\N	t	ARMINDA GALINDO ARNEZ DE HEREDIA	7777777			7777777	\N	2	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	9000.6000	2025-12-31		2	f
c141276e-c18d-4898-a061-6d78b6f5fcd2	2026-01-01 01:32:05.729919+00	\N	t	WILSON HEREDIA	7777777			7777777	\N	13	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1463.0250	2025-12-31		13	f
cb3b0c7d-f02e-493c-bdbb-a35405261ee7	2026-01-01 01:32:07.201968+00	\N	t	MARIO SUAZNABAR	7777777			7777777	\N	24	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		24	f
3ae5ebe8-9b06-492d-8f16-76516ae7fef9	2026-01-01 01:32:08.7119+00	\N	t	ZACARIAS VARGAS	7777777			7777777	\N	35	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7900.8000	2025-12-31		35	f
e691f807-f1b6-4ab0-b1e4-c52548ef48cc	2026-01-01 01:32:10.270836+00	\N	t	SANDRA ZURITA	7777777			7777777	\N	46	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	8407.0750	2025-12-31		46	f
716a3898-cd98-4c41-b56f-b96eba12151b	2026-01-01 01:32:11.694498+00	\N	t	VALENTINA AMPUERO (1)	7777777			7777777	\N	56	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	11260.750	2025-12-31		56	f
5124a60d-66c1-49a9-b367-9f16b9bc2b8d	2026-01-01 01:32:12.072199+00	\N	t	FELIPE FERNANDEZ (2)	7777777			7777777	\N	59	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	6698.7000	2025-12-31		59	f
5bd36050-3598-480d-b427-1cf1e04d2b49	2026-01-01 01:32:12.203263+00	\N	t	FRANCISCO HEREDIA (1)	7777777			7777777	\N	60	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	840.500	2025-12-31		60	f
6a6d9b07-40d2-4136-93eb-a6c9ec790e0e	2026-01-01 01:32:13.464158+00	\N	t	MARIA ARRIETA DE VISCARRA	7777777			7777777	\N	70	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		70	f
d556cddc-a54f-474e-8811-6097b11a2fe1	2026-01-01 01:32:14.856235+00	\N	t	ALBERTO GONZALES	7777777			7777777	\N	81	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7666.750	2025-12-31		81	f
e3a9e9f8-da11-47f6-b848-ce5bd9157062	2026-01-01 01:32:16.432243+00	\N	t	GLORIA QUIROZ IBARRA	7777777			7777777	\N	93	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	7206.1250	2025-12-31		93	f
4f17dfb8-1ef0-4285-8c00-5811784c1458	2026-01-01 01:32:17.769319+00	\N	t	RIDER LEDEZMA	7777777			7777777	\N	104	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		104	f
ad6a7a56-d898-48dc-850a-331643ba2acd	2026-01-01 01:32:19.126296+00	\N	t	REMBERTO ZURITA	7777777			7777777	\N	115	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		115	f
5fb5ebab-5fb8-4f32-b013-d5bb0247b7fc	2026-01-01 01:32:19.373756+00	\N	t	ELI ACUÑA	7777777			7777777	\N	117	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		117	f
6f02e6f2-4f9b-45be-b2bb-5bc3358f1228	2026-01-01 01:32:20.253613+00	\N	t	BEATRIZ VDA.D VALENCIA	7777777			7777777	\N	124	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4099.6500	2025-12-31		124	f
dbf8a46a-0504-407e-9de7-0842a2530510	2026-01-01 01:32:21.92898+00	\N	t	GLADIS IVONNE PEÑARRIETA DE LUIZAGA	7777777			7777777	\N	136	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2559.0500	2025-12-31		136	f
78012f64-4635-4c1c-89d6-f394e2f1b891	2026-01-01 01:32:23.61674+00	\N	t	ZACARIAS FERNANDEZ	7777777			7777777	\N	149	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4114.750	2025-12-31		149	f
dcec64de-4291-498f-a381-592e2ee8ce07	2026-01-01 01:32:25.060936+00	\N	t	CIRILA OVIDIO (2)	7777777			7777777	\N	160	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	5415.8250	2025-12-31		160	f
c19484d7-4558-4138-b041-bbee5e0c7638	2026-01-01 01:32:26.461309+00	\N	t	RUTH BAUTISTA MAMANI	7777777			7777777	\N	171	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		171	f
a362d206-869e-4f98-9cd6-4943054cfbc1	2026-01-01 01:32:45.971429+00	\N	t	ZULMA PEÑA BLANCO	7777777			7777777	\N	326	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		326	f
cbcd0238-446a-449b-a705-de93bb3df029	2026-01-01 01:32:45.85821+00	\N	t	LIZET HEREDIA GALINDO	7777777			7777777	\N	325	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		325	f
86d6ae14-8b49-40e3-8d98-079e91b5c12b	2026-01-01 01:32:45.738102+00	\N	t	PATRICIA SANCHEZ PERALES	7777777			7777777	\N	324	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		324	f
3c212b2b-74b3-4070-88d7-18dbc1d6f391	2026-01-01 01:32:45.61522+00	\N	t	MARIA ALEJANDRA RIVAS	7777777			7777777	\N	323	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		323	f
bdb668fb-6178-4f71-8972-ad048d9d5604	2026-01-01 01:32:42.954705+00	\N	t	LAURA LORENA SANCHEZ JALDIN	7777777			7777777	\N	301	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	659.4250	2026-01-03		301	f
84991163-096d-4832-ba8c-61b10b0e1161	2026-01-01 01:32:43.690902+00	\N	t	LIZETH CLAROS HEREDIA	7777777			7777777	\N	307	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1167.950	2026-01-08		307	f
83bd28f4-082c-4f54-bd09-eb82283870a1	2026-01-01 01:32:26.888264+00	\N	t	JUAN GUALBERTO NUÑEZ ARZE	7777777			7777777	\N	174	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1288.3000	2025-12-31		174	f
3d306413-0c0f-4560-9069-cd615c1a37b4	2026-01-01 01:32:27.419474+00	\N	t	WILSON JOHNY VARGAS MUÑOZ	7777777			7777777	\N	178	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2060.8000	2025-12-31		178	f
12527f53-ac93-462b-96b8-26d10933def8	2026-01-01 01:32:28.323104+00	\N	t	MARCOS ANTEZANA A.	7777777			7777777	\N	185	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	4991.750	2025-12-31		185	f
c57fc358-95fe-4cd0-9107-447e9c3b13ba	2026-01-01 01:32:33.045206+00	\N	t	DELICIA ROJAS LOPEZ	7777777			7777777	\N	222	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1827.5250	2025-12-31		222	f
19a4de36-6310-4a17-9f0e-30574fd244c7	2026-01-01 01:32:33.653119+00	\N	t	GROBER ARRAZOLA ZURITA	7777777			7777777	\N	227	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2754.8250	2025-12-31		227	f
306d009e-ca42-42d8-8af7-53d496c5cd93	2026-01-01 01:32:37.242675+00	\N	t	FERNANDO ZENZANO ORELLANA	7777777			7777777	\N	255	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2660.7000	2025-12-31		255	f
490a1fd0-d181-4a82-a603-5ed5d93761d3	2026-01-01 01:32:38.121296+00	\N	t	JOSE JAVIER ARRAZOLA ARNEZ	7777777			7777777	\N	262	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	3424.5250	2025-12-31		262	f
6cce134a-3a78-47d2-ac58-7b938ab6eadf	2026-01-01 01:32:31.911732+00	\N	t	BASILIO ROCHA GOMEZ	7777777			7777777	\N	213	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2453.5250	2025-12-31		213	f
ec194603-b597-4559-ba7d-bf10c3ee08e1	2026-01-01 01:32:35.787264+00	\N	t	CARLOS MAMANI AVENDAÑO	7777777			7777777	\N	244	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		244	f
30dbc470-ebf1-4f82-87f6-0707a7034643	2026-01-01 01:32:46.935241+00	\N	t	VITALIA RODRIGUEZ	7777777			7777777	\N	334	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		334	f
60cf574a-68e7-463e-a1e4-398f9cd6548e	2026-01-01 01:32:46.813271+00	\N	t	JUANA ORELLANA SOLIS	7777777			7777777	\N	333	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		333	f
af4bb08a-1917-4cfb-b483-5605a9ad5321	2026-01-01 01:32:46.686034+00	\N	t	GUSTAVO RAUL CASTRO ESCALANTE 2	7777777			7777777	\N	332	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		332	f
9f309b5c-0b02-47ba-8119-74fad863bacc	2026-01-01 01:32:46.568933+00	\N	t	TANIA TATIANACAYO SOTO	7777777			7777777	\N	331	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		331	f
86bdcf36-477a-41fe-a7fc-e3991412f7fe	2026-01-01 01:32:46.433667+00	\N	t	WILMA VASQUEZ OVANDO	7777777			7777777	\N	330	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		330	f
8ea24889-2143-4840-ab29-f3f7f130f2d2	2026-01-01 01:32:46.317485+00	\N	t	GUSTAVO RAUL CASTRO ESCALANTE	7777777			7777777	\N	329	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		329	f
7c956972-1ea1-4bd7-90f2-870fc40b2d4a	2026-01-01 01:32:46.201625+00	\N	t	JUAN JOSE PEREZ MONTAÑO	7777777			7777777	\N	328	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		328	f
1b620f70-cf94-44c5-991b-f5a4a8b49f21	2026-01-01 01:32:46.08912+00	\N	t	ROSARIO PEREZ MONTAÑO	7777777			7777777	\N	327	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	20.000	2025-12-31		327	f
eee5b2e6-4a4b-4db7-a61a-4aad2f1a5d49	2026-01-01 01:32:25.79053+00	\N	t	ROBERTO CHOQUE COLQUE	7777777			7777777	\N	166	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	135.6000	2026-01-03		166	f
eb09516e-cd04-4f0c-ba4d-927b517978f9	2026-01-01 01:32:30.179821+00	\N	t	OSVALDO GUEVARA ENCINAS	7777777			7777777	\N	200	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	2204.1500	2026-01-03		200	f
13002414-6170-4f66-8467-aedd23034df5	2026-01-01 01:32:08.459437+00	\N	t	JUAN IBAÑEZ	7777777			7777777	\N	33	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	13202.050	2026-01-03		33	f
2d56bf21-d022-49aa-936e-59d94e985b3d	2026-01-01 01:32:42.726597+00	\N	t	BEATRIZ JHOANNA CORRALES VILLANUEVA	7777777			7777777	\N	299	425a0052-b86d-47ed-af91-8a84cbfc7f65	\N	\N	1313.9750	2026-01-03		299	f
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
cc7ca9ca-4926-48de-9de5-e21bdd078c9e	WB-13002414-1767446411161	13002414-6170-4f66-8467-aedd23034df5	3ef05658-fed7-4c72-b1ba-ed105fb95c15	2026-01-01	2026-01-31	2647.41	2.50	13162.05	13182.05	0	13182.05	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 13:20:11.162962+00	\N	t
342308a6-bae8-4290-938e-806f1977c1d7	WB-eee5b2e6-1767446625478	eee5b2e6-4a4b-4db7-a61a-4aad2f1a5d49	34a29941-9e27-4fa1-8585-d7c4f3fe9d15	2026-01-01	2026-01-31	10.69	2.50	0	20.0	0	20.0	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 13:23:45.478321+00	\N	t
ff3aa27d-1dc3-451e-9517-b4367e960ad0	WB-2d56bf21-1767446781041	2d56bf21-d022-49aa-936e-59d94e985b3d	29c23f66-ed8f-4c2b-86b3-5be2ed750d9a	2026-01-01	2026-01-31	12.32	2.50	0	20.0	0	20.0	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 13:26:21.041099+00	\N	t
7f4e453d-c0a4-467b-861c-628adeccda76	WB-bdb668fb-1767446830104	bdb668fb-6178-4f71-8972-ad048d9d5604	1c6afbec-c2ba-4b72-8210-b603f911a6d4	2026-01-01	2026-01-31	0.47	2.50	0	20.0	0	20.0	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 13:27:10.105003+00	\N	t
18252f16-7172-4440-926a-9e9e05d9f29e	WB-eb09516e-1767446868689	eb09516e-cd04-4f0c-ba4d-927b517978f9	29f0f083-03de-41e0-a5d2-3b2164a91072	2026-01-01	2026-01-31	4.94	2.50	0	20.0	0	20.0	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 13:27:48.689461+00	\N	t
83332343-c49e-4048-a582-edd12acf9f9a	WB-bbea725d-1767449093424	bbea725d-b15a-46cb-9c53-568581423fd6	2f21e6cf-35ba-4ab4-83fa-322021e8dd84	2026-01-01	2026-01-31	1.80	2.50	0	20.0	0	20.0	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-18	\N	2026-01-03 14:04:53.424818+00	\N	t
552f5ea4-25b0-40e7-9ef5-e3b8030563ca	WB-8c35ac64-1767888868949	8c35ac64-abc4-4c83-abca-a839e49577db	cd5479b5-e2ed-48cb-a8b0-296cff3fa789	2026-01-01	2026-01-31	1403.66	2.50	6943.30	6963.30	0	6963.30	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-23	\N	2026-01-08 16:14:28.951767+00	\N	t
656a4d6b-4280-4550-b406-37cdd4668ecb	WB-84991163-1767888935355	84991163-096d-4832-ba8c-61b10b0e1161	f7b4ac80-112c-4d9d-b0d3-40cebbc26d0e	2026-01-01	2026-01-31	110.19	2.50	475.95	495.95	0	495.95	734d99f5-31ff-4d8f-8427-467dfcc5b76a	2026-01-23	\N	2026-01-08 16:15:35.355942+00	\N	t
\.


--
-- Data for Name: water_meter_reading; Type: TABLE DATA; Schema: pos; Owner: postgres
--

COPY pos.water_meter_reading (reading_id, partner_id, reading_date, previous_reading, current_reading, consumption, reader_user_id, observation, image_id, created_at, updated_at, active) FROM stdin;
d0a31807-0025-4ac4-8864-694861c5bc8c	84d71f62-9553-45ec-8f62-95bd7f07f3cc	2025-11-02	0	3101.16	3101.16	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #1	\N	2026-01-01 01:55:38.02344+00	\N	t
96eb5cd6-0ffe-431b-954e-b4b1d53335f5	a037b101-2fd5-480f-bff7-1438fd369b08	2025-11-02	0	3592.24	3592.24	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #2	\N	2026-01-01 01:55:38.150466+00	\N	t
54b0c1d5-35ba-4713-a741-2dd4e188bd24	b34fc2fc-5827-4c6e-b3ba-fd011d78ad6c	2025-11-02	0	209.28	209.28	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #3	\N	2026-01-01 01:55:38.287278+00	\N	t
b1606384-8a5b-410d-9df3-0400fee9f637	c8e4b9ad-e890-4311-bc4a-d4c8890ffab7	2025-11-02	0	3774.08	3774.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #4	\N	2026-01-01 01:55:38.426407+00	\N	t
5e9399f7-88dc-40a3-ba23-a9926a7e86d2	111d0472-ada6-443f-b82a-9c0e29bc88e7	2025-11-02	0	758.47	758.47	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #5	\N	2026-01-01 01:55:38.56727+00	\N	t
e07833e5-8f2f-4c3f-9cb5-e17b07d858bd	119c77aa-c357-4751-9516-ee6ec030c013	2025-11-02	0	5192.23	5192.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #6	\N	2026-01-01 01:55:38.699908+00	\N	t
92461317-0e4b-45c3-aa23-734d66aaa86b	f58b90c9-ef7f-479f-b5d8-31b206aefc1d	2025-11-02	0	1107.37	1107.37	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #7	\N	2026-01-01 01:55:38.844631+00	\N	t
f889ce86-6ede-4f21-8dac-4c1e595c082b	c0e73fa2-ed82-49d3-bc51-ecf60b5f87a5	2025-11-02	0	3951.61	3951.61	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #8	\N	2026-01-01 01:55:39.016694+00	\N	t
ef62d475-2bda-47d0-ad2f-e7347f5745d0	2fa81894-8f73-4435-8e29-4e03e1e475ea	2025-11-02	0	609.95	609.95	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #9	\N	2026-01-01 01:55:39.150507+00	\N	t
044a35ff-d1e2-4153-b998-35422d452f54	c4b66620-f193-4f78-84df-de97145ec27c	2025-11-02	0	1310.41	1310.41	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #10	\N	2026-01-01 01:55:39.287948+00	\N	t
f9bc6005-0929-40db-80df-d57d81f942bd	5376b072-28aa-4aca-93c8-b26130afce32	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #11	\N	2026-01-01 01:55:39.417809+00	\N	t
67f8317a-dde1-4f29-8213-4320021e4d1f	c5d1d29e-5bce-49eb-bcfd-d45f094d9fc1	2025-11-02	0	734.23	734.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #12	\N	2026-01-01 01:55:39.544323+00	\N	t
5f706608-8b7e-4cdb-9fea-6daea97316ad	c141276e-c18d-4898-a061-6d78b6f5fcd2	2025-11-02	0	577.21	577.21	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #13	\N	2026-01-01 01:55:39.675657+00	\N	t
86493d48-a4ac-4c3e-8b19-e15e3dfb99b5	e6cae507-9810-40af-95be-fc4cfc4c859b	2025-11-02	0	1096.15	1096.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #14	\N	2026-01-01 01:55:39.817315+00	\N	t
4c7c0a0b-3632-4448-b3bf-946b57a2ea65	c5a01f14-61df-4835-9ab3-f12e2bf0919a	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #15	\N	2026-01-01 01:55:39.975526+00	\N	t
5c8689c6-c8e2-4748-b6fa-a202109e5a94	99c8bdd0-3083-4c4f-9df6-171112b3fa71	2025-11-02	0	1249.03	1249.03	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #16	\N	2026-01-01 01:55:40.160964+00	\N	t
6d91dd4b-b9c8-406b-a2bf-56537df953af	46f7c726-c7bc-4c56-a309-2c72c7d3c335	2025-11-02	0	2076.32	2076.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #17	\N	2026-01-01 01:55:40.401118+00	\N	t
b5e5f8f0-3cd5-4a2f-853e-c4e7bbf4d7b6	a867640a-c4c7-430f-8cc8-cc7d306932dc	2025-11-02	0	3748.0	3748.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #18	\N	2026-01-01 01:55:40.551853+00	\N	t
0b2efb88-3f1f-4bba-a88a-c80c11133874	4942d54b-f6d7-44c0-ae8f-25d4e8bb6749	2025-11-02	0	61.0	61.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #19	\N	2026-01-01 01:55:40.689274+00	\N	t
bfad46bf-c182-45d6-8595-05644261f2df	1a9ff5d6-2223-4313-b850-d1315f639186	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #20	\N	2026-01-01 01:55:40.827751+00	\N	t
3d4d9d19-07df-4b57-a656-c35fe174dbce	22849e8e-9d74-4fc1-895a-8aa040310b4e	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #21	\N	2026-01-01 01:55:40.965538+00	\N	t
adf6847c-e45d-4017-946d-daf21a46f952	a85e619e-528e-4283-a60f-d72ebe28e8ef	2025-11-02	0	2087.91	2087.91	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #22	\N	2026-01-01 01:55:41.145189+00	\N	t
72be4e93-3e2d-444a-9274-b01d6084b150	df76d6e1-6f7d-463e-a035-54d38ec57b29	2025-11-02	0	1463.04	1463.04	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #23	\N	2026-01-01 01:55:41.353662+00	\N	t
926ab584-0c20-48dd-b93e-f6bfe9e03c35	cb3b0c7d-f02e-493c-bdbb-a35405261ee7	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #24	\N	2026-01-01 01:55:41.497293+00	\N	t
09a39ff9-1365-4006-84a6-7fcb557185b4	58edd11b-de70-4596-ad18-6b14a5e8d713	2025-11-02	0	290.18	290.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #25	\N	2026-01-01 01:55:41.653022+00	\N	t
afffa6bd-34a4-4433-8718-ed137c15ee56	9dea2bce-7d73-41f3-8026-2ff9984794e5	2025-11-02	0	5220.22	5220.22	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #26	\N	2026-01-01 01:55:41.885561+00	\N	t
429819b7-0efb-4675-8438-e015d22e2f1f	6ef02d11-3e42-4f80-907f-2ff1cbb0b00a	2025-11-02	0	1818.97	1818.97	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #27	\N	2026-01-01 01:55:42.035729+00	\N	t
d9a428fb-e4f3-42f9-86ac-8407c0b2d9af	38735a89-6ab6-4a35-9f95-4eb80ba607be	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #28	\N	2026-01-01 01:55:42.192039+00	\N	t
a3a9019e-85d3-4e8a-b936-b91e51ccfe67	bd195a4f-49c6-4de5-abb8-216f487baf8a	2025-11-02	0	2041.73	2041.73	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #29	\N	2026-01-01 01:55:42.358698+00	\N	t
6cbc1799-4de2-469b-89d6-45723b534315	e5e3f711-4862-48fd-9485-ac1c88d68c09	2025-11-02	0	4700.07	4700.07	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #30	\N	2026-01-01 01:55:42.503152+00	\N	t
7b85bedb-48f0-4d3a-9e36-f30a64360680	c3752fba-3e0e-4bdd-bf49-9f8bab9d234c	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #31	\N	2026-01-01 01:55:42.645367+00	\N	t
0b9776b1-473d-4835-b301-a3ff08bc932d	1a938e19-1bd4-4ee1-a978-962079c33e92	2025-11-02	0	746.26	746.26	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #32	\N	2026-01-01 01:55:42.790479+00	\N	t
0b01c1db-a3e8-47de-a5d1-4499ab992877	13002414-6170-4f66-8467-aedd23034df5	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #33	\N	2026-01-01 01:55:42.931336+00	\N	t
d4521bc5-2fbb-4f57-bc8d-b85d69238224	87e69c11-d375-4058-9e13-348eaa3a7c89	2025-11-02	0	845.32	845.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #34	\N	2026-01-01 01:55:43.090455+00	\N	t
05fd42b6-d068-4f21-8807-f63ac93daf2f	3ae5ebe8-9b06-492d-8f16-76516ae7fef9	2025-11-02	0	3152.32	3152.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #35	\N	2026-01-01 01:55:43.287175+00	\N	t
dde9f871-2850-41a0-bb47-a329a90540d8	29e49179-a27a-4fd4-be12-eb734052fcc1	2025-11-02	0	1678.47	1678.47	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #36	\N	2026-01-01 01:55:43.426621+00	\N	t
8c441086-09be-413d-8e71-2e7b2cc52020	e555c1d9-4ecb-4256-9413-9760d1c206ab	2025-11-02	0	1743.71	1743.71	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #37	\N	2026-01-01 01:55:43.570079+00	\N	t
244dd266-c57e-470e-94fe-5a48d9117b8d	1853bd5c-292b-4cb5-b6d9-c1ae14a2f31a	2025-11-02	0	2254.18	2254.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #38	\N	2026-01-01 01:55:43.709386+00	\N	t
a6b75a60-d684-4883-ad8b-0c6152c5b067	108ed11b-9c7b-48e1-8fd3-60fa6aade281	2025-11-02	0	4398.14	4398.14	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #39	\N	2026-01-01 01:55:43.871166+00	\N	t
7653c8cc-5d72-4783-9034-fa0565e564bc	4a2e6366-c506-4309-af3c-455c215a2386	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #40	\N	2026-01-01 01:55:44.066967+00	\N	t
8954cb0e-e3d1-43ba-87df-c76b6b2ee9d7	12675072-3c83-45f3-9f31-8fd5f86493b1	2025-11-02	0	2229.03	2229.03	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #41	\N	2026-01-01 01:55:44.225347+00	\N	t
bbf6a1ea-bb0e-4ed8-9d48-6141401208e4	9adf6fb2-abf9-4f6a-8485-53397141721b	2025-11-02	0	54.38	54.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #42	\N	2026-01-01 01:55:44.380123+00	\N	t
e26dfb03-eb03-4b77-9b52-e2d1a0bb77f1	2d340055-3728-4e62-9a54-e21addd373f1	2025-11-02	0	3081.21	3081.21	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #43	\N	2026-01-01 01:55:44.526355+00	\N	t
b01fadd1-9950-43ce-aa80-1ba42250044d	72229f65-903c-4e87-ade0-1e85867ff514	2025-11-02	0	3012.26	3012.26	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #44	\N	2026-01-01 01:55:44.698649+00	\N	t
19c03907-925b-4500-90c5-807b2b762279	b9a467ef-5bde-4bc7-8349-1634b587ed8c	2025-11-02	0	1038.86	1038.86	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #45	\N	2026-01-01 01:55:44.891158+00	\N	t
339f1653-3f0e-46fd-a097-707d251ad219	e691f807-f1b6-4ab0-b1e4-c52548ef48cc	2025-11-02	0	3354.83	3354.83	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #46	\N	2026-01-01 01:55:45.046284+00	\N	t
0baa8f5b-2c9f-4149-8b73-dc6b9668e4a2	5da33db7-cf43-4c0f-90fd-4eeaa564f1f5	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #47	\N	2026-01-01 01:55:45.190507+00	\N	t
00c545eb-c70b-4752-accd-fba4893d6c6b	f25998d6-92a7-4421-ba65-b055e55f9ed5	2025-11-02	0	446.01	446.01	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #48	\N	2026-01-01 01:55:45.354615+00	\N	t
36f651bf-1e33-42bd-9373-dff701da7067	cd4d18ac-ef4b-412f-b47f-6a900fcfb305	2025-11-02	0	1193.43	1193.43	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #49	\N	2026-01-01 01:55:45.567157+00	\N	t
c07ca2d3-9014-4c3f-98d2-2870f93712bd	50478413-12b5-4401-a8fe-38f045c599af	2025-11-02	0	3344.37	3344.37	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #50	\N	2026-01-01 01:55:45.725474+00	\N	t
6fa1fbe3-ad72-43ea-a5f3-99153808fd32	0d775a1b-74a9-4361-9524-f5e53cc032b9	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #51	\N	2026-01-01 01:55:45.87319+00	\N	t
30e6eeac-5175-435a-8200-aa06bbaef3eb	58416bda-80e9-4373-898d-8ea896a3b546	2025-11-02	0	2993.73	2993.73	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #52	\N	2026-01-01 01:55:46.028486+00	\N	t
3ef05658-fed7-4c72-b1ba-ed105fb95c15	13002414-6170-4f66-8467-aedd23034df5	2026-01-03	0.0	2647.41	2647.41	\N		\N	2026-01-03 13:20:11.148378+00	\N	t
0b1dea1f-1466-43fd-8c95-4b1ea8ccbc25	4d243594-c0e3-4bdc-95e5-5ab0bbde5f7c	2025-11-02	0	3842.56	3842.56	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #53	\N	2026-01-01 01:55:46.194633+00	\N	t
fbf31e6f-db9c-48db-9a00-bf6ddcdea61c	9009ee8f-8bbe-468b-a46b-f46026f03c35	2025-11-02	0	1078.66	1078.66	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #54	\N	2026-01-01 01:55:46.34299+00	\N	t
10e15d3f-750e-4172-8c51-711e7b03af68	77a73766-25c1-4d67-a586-5944f3a25d18	2025-11-02	0	3316.31	3316.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #55	\N	2026-01-01 01:55:46.494471+00	\N	t
711bb660-dba8-447f-a2e3-1338cad9b051	716a3898-cd98-4c41-b56f-b96eba12151b	2025-11-02	0	4496.3	4496.3	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #56	\N	2026-01-01 01:55:46.640289+00	\N	t
458ffa9f-f86f-4f4c-8039-a5b029e29ad3	14dc53c8-faa1-4dae-85e0-a5da22675e57	2025-11-02	0	2081.09	2081.09	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #57	\N	2026-01-01 01:55:46.79311+00	\N	t
d8defc28-5fff-4d6a-8c08-1a7f6818631b	99240cce-fc1c-4827-90c7-bdc658cbc291	2025-11-02	0	720.13	720.13	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #58	\N	2026-01-01 01:55:46.954413+00	\N	t
ab79aa92-e569-4e99-b3d8-293cc731a1b5	5124a60d-66c1-49a9-b367-9f16b9bc2b8d	2025-11-02	0	2671.48	2671.48	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #59	\N	2026-01-01 01:55:47.12469+00	\N	t
148f0549-3bd9-4ff1-8203-78d1e74194ca	5bd36050-3598-480d-b427-1cf1e04d2b49	2025-11-02	0	328.2	328.2	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #60	\N	2026-01-01 01:55:47.325264+00	\N	t
a5dc625c-799b-4452-9d09-e2312b6eb64e	bdcbf4d3-ee79-45c8-9177-93464cf9c143	2025-11-02	0	3125.48	3125.48	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #61	\N	2026-01-01 01:55:47.575234+00	\N	t
50cc346f-4744-48ea-9d97-9d19f52ed400	836891d4-e95f-4476-bf2d-df6833c78582	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #62	\N	2026-01-01 01:55:47.733113+00	\N	t
1961d8ff-f088-4b62-b8d7-34df2d883ca1	f4f87ab5-6c62-4223-8397-2dc0f3a1ada2	2025-11-02	0	3790.15	3790.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #63	\N	2026-01-01 01:55:47.924466+00	\N	t
2f2ea03d-b012-4233-bdda-4a75c589cdcc	656508b3-00fe-4827-b4b5-d04a582dffee	2025-11-02	0	1475.12	1475.12	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #64	\N	2026-01-01 01:55:48.137521+00	\N	t
fbc3c693-3518-47b3-8482-6883d821531c	041e4ea3-059a-470d-8347-a7818a6e0aab	2025-11-02	0	2294.12	2294.12	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #65	\N	2026-01-01 01:55:48.296536+00	\N	t
fd79f274-2a19-4d44-bb9b-42ff17d8d468	68d04ada-0e07-45b5-bd2c-f7e71ee64a69	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #66	\N	2026-01-01 01:55:48.480133+00	\N	t
bd1f48b6-0051-48b2-a140-ccb27ee9a963	221e00e7-c334-4c82-8070-dec0598f7ca6	2025-11-02	0	2423.2	2423.2	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #67	\N	2026-01-01 01:55:48.692937+00	\N	t
fd48946c-4401-46bc-bc48-90772be2e060	a075d2fa-902e-4675-9738-6b59b73ec71b	2025-11-02	0	381.45	381.45	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #68	\N	2026-01-01 01:55:48.94492+00	\N	t
2c4d79d9-9664-415a-aa2a-6ba1afe4d07d	f48046c1-97f2-4757-be45-c00731f05ed8	2025-11-02	0	2632.69	2632.69	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #69	\N	2026-01-01 01:55:49.113587+00	\N	t
c6e4948a-6637-42a2-9284-dae10a2a8b88	6a6d9b07-40d2-4136-93eb-a6c9ec790e0e	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #70	\N	2026-01-01 01:55:49.271006+00	\N	t
113b1109-56d6-4a48-9b1d-78bec31330a8	4241ed5a-303e-497c-82f7-9595d88dd60c	2025-11-02	0	2031.51	2031.51	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #71	\N	2026-01-01 01:55:49.430271+00	\N	t
42b20ad3-1c56-44a0-98b8-9d9a62049b52	a1ce02e0-65ad-411a-b064-c07b5fda155f	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #72	\N	2026-01-01 01:55:49.615056+00	\N	t
1ea9b8f3-4768-4bee-b965-644ec6bb2bd2	d5867083-0b87-4636-bdc3-1a97c69efbce	2025-11-02	0	2208.46	2208.46	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #73	\N	2026-01-01 01:55:49.812538+00	\N	t
e861b5f8-4d5c-4645-87d1-a8da97dfbc7d	117e9224-965a-403a-be08-b0a51bf04335	2025-11-02	0	1037.21	1037.21	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #74	\N	2026-01-01 01:55:49.971882+00	\N	t
ffd38348-ff5e-450c-ba2b-caad8f0cac4a	f1626518-4b5b-43be-b97e-aee49da80dca	2025-11-02	0	70.74	70.74	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #75	\N	2026-01-01 01:55:50.14699+00	\N	t
2a1dd0a2-8ecc-4599-babc-07b642b53a57	133950bb-218a-47d9-93d1-4ed54952a75d	2025-11-02	0	2073.13	2073.13	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #76	\N	2026-01-01 01:55:50.461134+00	\N	t
7a2e1941-8f1c-470f-a7f8-d09e475df5fb	bf7620c0-e75a-435d-ac85-ff4eebe24212	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #77	\N	2026-01-01 01:55:50.777126+00	\N	t
d5559c40-6338-4c3e-bd14-3f62f21afe81	4ddaa508-5149-487c-a0e2-0fb8e87a1f8b	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #78	\N	2026-01-01 01:55:51.009059+00	\N	t
d117e911-48a0-4df7-aec1-e03c0cdaf001	f6806217-18ee-4231-92ec-5f78630ea340	2025-11-02	0	1059.25	1059.25	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #79	\N	2026-01-01 01:55:51.188921+00	\N	t
e51d86f8-27e1-4da2-9766-8a80e83c730f	da9f08dd-7867-4694-9cf5-ea182d03eda4	2025-11-02	0	3240.01	3240.01	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #80	\N	2026-01-01 01:55:51.347043+00	\N	t
c2edd4eb-c8c5-471c-80da-1c30908dade6	d556cddc-a54f-474e-8811-6097b11a2fe1	2025-11-02	0	3058.7	3058.7	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #81	\N	2026-01-01 01:55:51.510682+00	\N	t
dbaca7ff-fe66-4e16-a9a7-26d72ed9d7e9	48083230-2479-4e1e-9304-7d7d031edfc6	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #82	\N	2026-01-01 01:55:51.68213+00	\N	t
a81f2e78-c84b-4609-b5b0-82e009d051a2	a3c24276-e409-4330-84e0-567c251368f9	2025-11-02	0	495.72	495.72	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #83	\N	2026-01-01 01:55:51.866602+00	\N	t
8054aa42-940c-4a1d-96cc-f827f95740b1	6fcd1230-4812-450c-b8ac-a027a20ff4ed	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #84	\N	2026-01-01 01:55:52.058579+00	\N	t
502d598d-a61a-4650-8451-bfddb55839e2	cdc68de6-a687-4ccd-8c7c-294523dfdcbe	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #85	\N	2026-01-01 01:55:52.250383+00	\N	t
cbf2fd43-ba1c-45e6-9f02-b0af33982e54	6cbc3b55-bc53-452f-894a-2a7bb7929c0e	2025-11-02	0	1020.25	1020.25	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #86	\N	2026-01-01 01:55:52.507684+00	\N	t
5f5c8246-fc03-4809-9403-d24abffd38a3	8444180b-c453-462b-8de9-c16d4a5880f7	2025-11-02	0	2085.23	2085.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #87	\N	2026-01-01 01:55:52.664071+00	\N	t
3cd7d06a-fd29-4fae-847b-ac02aa061316	2ebc7637-7c00-4e83-b41d-87013f82c547	2025-11-02	0	1530.09	1530.09	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #88	\N	2026-01-01 01:55:52.858651+00	\N	t
aa9121cf-8133-428e-83d6-84b7f1084555	c94367fe-6d92-43e4-b392-e48c62b21bf7	2025-11-02	0	3357.19	3357.19	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #89	\N	2026-01-01 01:55:53.058316+00	\N	t
b66655fc-8c86-4178-bcca-b1b55dd93e46	5809cb87-55d0-4f86-944f-016467a37a95	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #90	\N	2026-01-01 01:55:53.243717+00	\N	t
0994d3d8-47e9-4750-afc9-d3ac75171ebc	0907a9db-e3eb-4157-ae95-d1cfe35389ad	2025-11-02	0	2338.5	2338.5	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #91	\N	2026-01-01 01:55:53.406186+00	\N	t
78d1630e-ada3-4eb3-b802-632ddfc6ea16	4a6897a3-40f6-4ade-8dae-9e5a97a3faee	2025-11-02	0	2355.23	2355.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #92	\N	2026-01-01 01:55:53.578053+00	\N	t
f1454b04-ee96-45b2-893f-baf5472a0413	e3a9e9f8-da11-47f6-b848-ce5bd9157062	2025-11-02	0	2874.45	2874.45	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #93	\N	2026-01-01 01:55:53.785138+00	\N	t
fa9798e7-4b23-4284-900d-445b8f7f04c9	731c8963-6270-43e7-8881-43fec925144f	2025-11-02	0	682.84	682.84	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #94	\N	2026-01-01 01:55:53.974477+00	\N	t
e4bc7685-d6e2-4d6f-9756-37d4b5c1ecd4	313cb6cc-4b02-4d77-9e3f-8281fd1c86f7	2025-11-02	0	5130.64	5130.64	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #95	\N	2026-01-01 01:55:54.19317+00	\N	t
8c687cfd-ab91-49d2-bd41-9cce375e9729	de4639a1-50ed-4920-ae5c-5dd1e9a2dbd0	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #96	\N	2026-01-01 01:55:54.372642+00	\N	t
b2d4b8df-531e-4ab4-8660-63f11eccba67	391d3c92-20b2-4a86-8e74-9ed7583553d4	2025-11-02	0	1832.38	1832.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #97	\N	2026-01-01 01:55:54.555456+00	\N	t
202f0a19-88b0-4859-ad16-227cf34a07cf	e7539002-c942-490a-ac6a-7f3303ed7e1e	2025-11-02	0	4927.41	4927.41	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #98	\N	2026-01-01 01:55:54.787859+00	\N	t
39b24bf7-0184-430a-8236-fedf3a462ef3	53969684-a7b0-488b-8fe0-c9da510f32e4	2025-11-02	0	1086.08	1086.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #99	\N	2026-01-01 01:55:54.990959+00	\N	t
15d54a22-e97a-4021-b7cb-a4bedc7b3ee2	efbe794a-d10c-48da-b03b-0455f55b935c	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #100	\N	2026-01-01 01:55:55.172913+00	\N	t
e60e793b-28b8-481c-8d83-43276ef3082f	e3bd2d8c-8cba-4d0b-94db-3c42619e1f2f	2025-11-02	0	2507.07	2507.07	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #101	\N	2026-01-01 01:55:55.454345+00	\N	t
6dac958d-b737-4052-87a0-e92a11eccfd8	4157e6d2-ecfa-43c7-a491-c1ae7ff17857	2025-11-02	0	3012.8	3012.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #102	\N	2026-01-01 01:55:55.796329+00	\N	t
1cddc588-4917-49bf-8afc-a0204edfb441	9d3a60f0-49ad-412e-83af-c23b942416cc	2025-11-02	0	538.18	538.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #103	\N	2026-01-01 01:55:56.0147+00	\N	t
13aaefb8-a126-45f2-a3a3-82f2cc86b9fa	4f17dfb8-1ef0-4285-8c00-5811784c1458	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #104	\N	2026-01-01 01:55:56.199242+00	\N	t
2cc33964-8e97-476f-acbc-238100a96152	a4f95e66-4855-4156-a2c5-b37362e0b4c6	2025-11-02	0	5358.25	5358.25	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #105	\N	2026-01-01 01:55:56.392785+00	\N	t
008524d0-7436-4562-8ead-be4c7400a16e	a805f4b9-0a0f-4479-9b70-77945dba7288	2025-11-02	0	3470.19	3470.19	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #106	\N	2026-01-01 01:55:56.579672+00	\N	t
598a738a-3a49-4c04-903a-50d779eb3b06	8c44635e-2f57-432a-87c2-6915a463d59f	2025-11-02	0	2837.17	2837.17	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #107	\N	2026-01-01 01:55:56.804156+00	\N	t
ca73d2ea-c834-4b4d-9d92-90bcde25d15a	ccf6baf6-291c-4b5e-b967-ca4e0c4ec282	2025-11-02	0	2984.36	2984.36	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #108	\N	2026-01-01 01:55:57.021726+00	\N	t
f6915f8e-b002-4c67-b682-f12c6950fd7a	97e8ebd2-61b2-455c-ba37-ef460d2e26f5	2025-11-02	0	2184.32	2184.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #109	\N	2026-01-01 01:55:57.245183+00	\N	t
ee011a1b-46dc-4bb5-b02c-be39dc06163d	c9ed9cab-c356-4503-ae92-9aedc174ee14	2025-11-02	0	3796.17	3796.17	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #110	\N	2026-01-01 01:55:57.505384+00	\N	t
111066de-5a13-46d9-928b-79efa7ec7221	77981414-5a3f-484f-b1ed-19a21fa77c6c	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #111	\N	2026-01-01 01:55:57.719893+00	\N	t
8c439530-861e-4acf-aed3-2fcae1098fea	9328a169-c2a4-4422-a348-becf27395975	2025-11-02	0	1056.54	1056.54	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #112	\N	2026-01-01 01:55:57.981775+00	\N	t
964c2ddd-3048-4508-b3cb-a797d8b36414	4fddcb04-934a-4b43-a5c5-4b7a0119a197	2025-11-02	0	4547.71	4547.71	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #113	\N	2026-01-01 01:55:58.234303+00	\N	t
1e1e7a81-fe34-4352-a3a5-f7a86b35b148	d9494a7b-33e3-48b2-9978-3dbb03bce479	2025-11-02	0	129.8	129.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #114	\N	2026-01-01 01:55:58.499674+00	\N	t
517abed7-45f9-4a06-85b3-3fe8a3f3ed59	ad6a7a56-d898-48dc-850a-331643ba2acd	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #115	\N	2026-01-01 01:55:58.76399+00	\N	t
e1214c58-8060-4f12-816e-24e3653fb0b8	7ecd1b98-758a-4ac1-a89a-decbca721653	2025-11-02	0	1178.4	1178.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #116	\N	2026-01-01 01:55:59.025852+00	\N	t
b62b701b-d75c-4812-93cf-90adbc159959	5fb5ebab-5fb8-4f32-b013-d5bb0247b7fc	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #117	\N	2026-01-01 01:55:59.289388+00	\N	t
e27fc821-21e7-4237-8d5c-d0c0cc38f1a8	ee3878d3-45da-473e-b2f4-ca61f488b3ac	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #118	\N	2026-01-01 01:55:59.544395+00	\N	t
f27ee65d-9db4-4c0f-b13b-da352e2c69f4	07894568-d4d0-49d8-9b79-90ad72f642ae	2025-11-02	0	1726.0	1726.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #119	\N	2026-01-01 01:55:59.81034+00	\N	t
e0a70952-57ca-493c-98fd-48f67109c1ae	19f406a8-a473-4e0e-8911-b5d8c4c5ffb3	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #120	\N	2026-01-01 01:56:00.038643+00	\N	t
0ba5bffb-433b-419b-b894-c57b24eed586	61b3367a-67d1-47c0-9a76-af2a0a26bebb	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #121	\N	2026-01-01 01:56:00.254862+00	\N	t
bf19cf78-93e8-4ae3-a44d-c718b60cbd1f	9cd9c2b6-7703-422c-b01f-73aedc313ce8	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #122	\N	2026-01-01 01:56:00.525061+00	\N	t
d61d5884-ab99-49dc-a982-0b008ac831f2	1033de75-9f74-45c3-b290-926bfb0e0733	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #123	\N	2026-01-01 01:56:00.743737+00	\N	t
2539954c-37fb-4db9-98b3-a3e2da0e7c34	6f02e6f2-4f9b-45be-b2bb-5bc3358f1228	2025-11-02	0	1631.86	1631.86	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #124	\N	2026-01-01 01:56:01.039662+00	\N	t
b1260b60-b265-47ca-b090-f0f5b9eddad4	a51a35bf-5142-44bd-88bb-545c7c2d464e	2025-11-02	0	2894.51	2894.51	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #125	\N	2026-01-01 01:56:01.230166+00	\N	t
61a8830d-b0ff-42db-82c8-2220585a7b16	2deb4f8d-8c1c-458b-975f-02d1381ae47d	2025-11-02	0	1282.4	1282.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #126	\N	2026-01-01 01:56:01.484463+00	\N	t
a67b5084-c40e-4069-a027-c58d633535c8	2af411d5-195a-41bd-9deb-4e65ba826c3a	2025-11-02	0	1252.8	1252.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #127	\N	2026-01-01 01:56:01.885312+00	\N	t
ff58f8f7-11de-4cdd-a76b-3352a45b5ec4	3fc20c14-109f-46bb-b151-5ea324436308	2025-11-02	0	2796.4	2796.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #128	\N	2026-01-01 01:56:02.213558+00	\N	t
355c1db8-02ca-43d0-8b4c-d6d2b3ff4d4e	a5dc22c9-eaa8-4e84-be3c-88aae70b72c2	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #129	\N	2026-01-01 01:56:02.57902+00	\N	t
6c7b6091-0d8c-4bb9-98cb-0842b080028e	bd4ea028-2819-4e7b-ba4a-1e616b76b6cf	2025-11-02	0	2567.12	2567.12	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #130	\N	2026-01-01 01:56:02.851529+00	\N	t
b3187fa4-8473-4a38-b8fe-4314e68c1b90	50d362fb-b3ea-4ad7-b711-2be844a22c66	2025-11-02	0	5785.16	5785.16	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #131	\N	2026-01-01 01:56:03.082274+00	\N	t
51bc03db-6748-4f6c-bb11-efac596cf95a	73a58e0a-78c4-4c5c-87cf-78684aa5b6d1	2025-11-02	0	4148.89	4148.89	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #132	\N	2026-01-01 01:56:03.275643+00	\N	t
fbc8d67e-8f91-4b52-9e3d-a18f085b1c5c	dbab45aa-6069-4e42-a9ab-b705d3a31b62	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #133	\N	2026-01-01 01:56:03.47944+00	\N	t
07c02489-95ac-46ff-9ec3-44e55bc0a1cb	4b906399-28f2-45e3-8edb-9e5c6620f849	2025-11-02	0	94.08	94.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #134	\N	2026-01-01 01:56:03.672113+00	\N	t
faf89493-7bdb-4c55-b2b1-3e77faac090b	c4add11e-6368-4208-bcdc-9cd132e494f1	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #135	\N	2026-01-01 01:56:03.903666+00	\N	t
184e710a-51b2-44d4-87b2-a6e29a7391d8	dbf8a46a-0504-407e-9de7-0842a2530510	2025-11-02	0	1015.62	1015.62	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #136	\N	2026-01-01 01:56:04.150107+00	\N	t
b22ce8dc-f091-4ed5-8331-6824910efb39	c3ece66f-8b75-4e3c-8805-82312cb26061	2025-11-02	0	1468.17	1468.17	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #137	\N	2026-01-01 01:56:04.384392+00	\N	t
8e9cf6a8-aa3c-4100-9e02-377150efdb38	a7d5fc83-ed5a-4bb2-aa37-c7a688a3f3b2	2025-11-02	0	2753.12	2753.12	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #138	\N	2026-01-01 01:56:04.582012+00	\N	t
4eacdd18-4c87-487a-bbf5-d019b6a1651e	74f2df01-9d33-47f4-8e3a-660bd2795c45	2025-11-02	0	3265.3	3265.3	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #139	\N	2026-01-01 01:56:04.772063+00	\N	t
a61d125d-331a-4f1e-af62-7ac65d03ca05	24efb0d3-d30f-4cfd-8f17-e9cff6db6c20	2025-11-02	0	2091.08	2091.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #140	\N	2026-01-01 01:56:04.987101+00	\N	t
12f01152-c492-42d0-90b7-b3c602f9dea7	9f428deb-9b0e-4f0f-8c98-00232fc387e7	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #141	\N	2026-01-01 01:56:05.154784+00	\N	t
ed8552d5-a5d7-4a3d-8912-cb7ac46def60	b6d390e3-2b49-4ed0-860d-a67fe6a651fb	2025-11-02	0	983.04	983.04	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #142	\N	2026-01-01 01:56:05.394559+00	\N	t
36742f9c-ebf5-4a63-bff3-b76a6e7cb6d6	794792ed-9ab2-45db-ba9a-58df0e3ed1d9	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #143	\N	2026-01-01 01:56:05.655443+00	\N	t
eb5243ae-f1a1-44c8-9e3b-fb2315906127	72d1bb1f-508d-407f-ad27-d0f49615f161	2025-11-02	0	918.4	918.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #144	\N	2026-01-01 01:56:05.905878+00	\N	t
a0211c7e-80ec-4b57-9318-abb04ec4a48f	987ed432-3fb7-422d-b0c3-7aeebbdda82c	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #145	\N	2026-01-01 01:56:06.118433+00	\N	t
02e8f5c2-7baf-4f08-bc54-9b0eb3b0b624	2272bc6b-372e-49f7-ab1a-3b7ad6cc0910	2025-11-02	0	1961.24	1961.24	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #146	\N	2026-01-01 01:56:06.440845+00	\N	t
17f8db10-25d1-4c1a-ab37-39aa1e9d4b81	8c14decd-fc88-4562-846d-28c3b32906f5	2025-11-02	0	831.78	831.78	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #147	\N	2026-01-01 01:56:06.733194+00	\N	t
647bf330-5bd6-48e3-9657-f0f24a221bff	e09b13f4-c949-490e-a4d0-cc3edd28ac7f	2025-11-02	0	1310.78	1310.78	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #148	\N	2026-01-01 01:56:06.937399+00	\N	t
43f86c1e-1281-48c4-a535-eb7bd9bacd97	78012f64-4635-4c1c-89d6-f394e2f1b891	2025-11-02	0	1637.9	1637.9	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #149	\N	2026-01-01 01:56:07.315407+00	\N	t
8faa5983-6c19-4e7f-a50f-64c613664e08	84098601-de8d-44d0-9479-2a4a5b518399	2025-11-02	0	3276.51	3276.51	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #150	\N	2026-01-01 01:56:07.557759+00	\N	t
f6c3ba83-5838-4539-b21c-0628e002bbe4	821083a5-4020-4e9b-83ac-db461dae75b1	2025-11-02	0	209.65	209.65	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #151	\N	2026-01-01 01:56:07.858283+00	\N	t
ad875bb8-2826-4f1a-b44d-07f3845a0eac	a998c60c-38ab-41f6-9280-63b4d2b5a9a8	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #152	\N	2026-01-01 01:56:08.28603+00	\N	t
3f28fb19-ec2f-4104-8ecc-0c0eea0c0755	58952a9c-aac7-4b6b-a838-9022734852f5	2025-11-02	0	2654.71	2654.71	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #153	\N	2026-01-01 01:56:08.595463+00	\N	t
3e60d7d9-93e3-4bfc-83e1-c18f4c8de556	162074a5-2821-491d-a7ba-bf43b2a49830	2025-11-02	0	2205.93	2205.93	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #154	\N	2026-01-01 01:56:08.801502+00	\N	t
a4e9b895-9c3f-471b-8261-52ade70017f8	64a93fc5-ab81-4e01-8601-f5d3fdd9c152	2025-11-02	0	2693.08	2693.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #155	\N	2026-01-01 01:56:09.01698+00	\N	t
b80f7624-6ea7-4420-930d-3c409143a2c0	a64c8bb2-05c1-4089-8301-bdeb91880476	2025-11-02	0	2310.4	2310.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #156	\N	2026-01-01 01:56:09.205264+00	\N	t
30c010dc-1db6-4a8d-a055-7803e126e942	acfda353-2d7b-47e7-ae81-7ee537f8a8f1	2025-11-02	0	5321.41	5321.41	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #157	\N	2026-01-01 01:56:09.405639+00	\N	t
c71f2655-e439-4199-aaea-ccbd20f1e8a7	d0c81df8-fa06-4361-8c9f-1df970fb1773	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #158	\N	2026-01-01 01:56:09.590136+00	\N	t
5e18a597-dc74-4259-97c3-fe3adeca566d	eab74f48-c2dd-4e8e-bb64-04d6def24de5	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #159	\N	2026-01-01 01:56:09.785652+00	\N	t
33dab907-5a05-4071-82ff-83604baff979	dcec64de-4291-498f-a381-592e2ee8ce07	2025-11-02	0	2158.33	2158.33	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #160	\N	2026-01-01 01:56:09.991672+00	\N	t
0ab047bd-a2d2-4935-ad9f-057b25fca959	5f98c2aa-178c-4c75-852f-420894c15d00	2025-11-02	0	1798.8	1798.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #161	\N	2026-01-01 01:56:10.172824+00	\N	t
89c8dca7-802e-4983-a0d8-1db5c12b9aa3	0f97b28d-1b2c-481b-8dfe-55fc3194c67d	2025-11-02	0	999.93	999.93	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #162	\N	2026-01-01 01:56:10.408418+00	\N	t
a198b94d-3e4c-46b1-938e-cfe87e01c89e	a5ced6a4-6673-46af-8210-66cdc3f2c18f	2025-11-02	0	220.31	220.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #163	\N	2026-01-01 01:56:10.649219+00	\N	t
57d09257-e92e-4b1f-93c2-1569e93dc704	966f2cec-edd4-498a-9a31-9a9888c28abe	2025-11-02	0	1023.43	1023.43	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #164	\N	2026-01-01 01:56:10.881494+00	\N	t
cf659384-ea34-4092-abbd-93dabf90bd88	21186c91-b175-4b79-885f-9926f6652a7e	2025-11-02	0	2203.42	2203.42	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #165	\N	2026-01-01 01:56:11.145501+00	\N	t
fcf425e7-6a4f-411a-9b90-d57245362aaa	eee5b2e6-4a4b-4db7-a61a-4aad2f1a5d49	2025-11-02	0	38.24	38.24	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #166	\N	2026-01-01 01:56:11.393068+00	\N	t
13784d8e-398a-4fca-8f32-daa1ce17d264	d0b4237b-3727-48ec-8dee-4cb5d418a7e7	2025-11-02	0	82.73	82.73	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #167	\N	2026-01-01 01:56:11.684393+00	\N	t
dbc23d3c-a07a-4208-b97d-4c8e35d8ceef	761f7375-1ffa-4de8-89b4-6e7b93cb86ec	2025-11-02	0	5458.31	5458.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #168	\N	2026-01-01 01:56:11.927156+00	\N	t
4c517227-5efb-4f37-b6e4-98b1dcd15837	35ce813b-a08b-452e-b2d0-d4b78049568a	2025-11-02	0	731.81	731.81	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #169	\N	2026-01-01 01:56:12.207301+00	\N	t
c1220869-02f8-4bb2-8bca-5065c78cec82	59c6e8b8-b9aa-4ff8-a84c-f33949c401db	2025-11-02	0	651.03	651.03	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #170	\N	2026-01-01 01:56:12.441563+00	\N	t
dc90a4f3-cbaa-49e4-b73d-02002b6769b4	c19484d7-4558-4138-b041-bbee5e0c7638	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #171	\N	2026-01-01 01:56:12.760444+00	\N	t
9ffd5e36-8573-48ad-9172-ddad6749ce76	2af9ee51-c6f1-412f-b685-c2ac156a8d66	2025-11-02	0	2300.2	2300.2	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #172	\N	2026-01-01 01:56:12.956959+00	\N	t
69a413fa-d0f4-4394-91c0-787f928fcf4c	45beac96-25a6-47eb-ab48-0d0cbffbfae6	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #173	\N	2026-01-01 01:56:13.173138+00	\N	t
422b7e77-ebef-42cb-9c05-b14a5292eebb	83bd28f4-082c-4f54-bd09-eb82283870a1	2025-11-02	0	507.32	507.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #174	\N	2026-01-01 01:56:13.377239+00	\N	t
fc6b3cf9-2197-48c4-87c2-95241fe5013b	bbea725d-b15a-46cb-9c53-568581423fd6	2025-11-02	0	914.26	914.26	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #175	\N	2026-01-01 01:56:13.582745+00	\N	t
671ea580-26f6-442d-8f2f-455da21a21c4	8c35ac64-abc4-4c83-abca-a839e49577db	2025-11-02	0	193.26	193.26	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #176	\N	2026-01-01 01:56:13.78447+00	\N	t
07579b96-18d3-415d-b557-ff4e3c9df521	bc19e0f1-28c8-4801-a772-78aaa1b6aa07	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #177	\N	2026-01-01 01:56:13.975853+00	\N	t
2b62935f-d040-4420-bc12-2b35d3cb2c77	3d306413-0c0f-4560-9069-cd615c1a37b4	2025-11-02	0	816.32	816.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #178	\N	2026-01-01 01:56:14.178547+00	\N	t
15d5b852-dc7b-41c9-b476-13c3cd1424fe	a3785978-1ff3-403c-b9a1-e3b4df710361	2025-11-02	0	201.02	201.02	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #179	\N	2026-01-01 01:56:14.429843+00	\N	t
dd739513-7ba0-4fe8-b0d7-cd3157058085	517d1480-d360-44fc-8c34-6a2db7395458	2025-11-02	0	1018.27	1018.27	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #180	\N	2026-01-01 01:56:14.671795+00	\N	t
0c8c97d8-3a1c-424d-9687-0ae284974a89	84214b74-5bf3-4cc9-9ffb-e03918069693	2025-11-02	0	1123.36	1123.36	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #181	\N	2026-01-01 01:56:15.052608+00	\N	t
e359cefe-56f9-4a92-89fa-e68d497fddda	b8956f1a-541f-4b33-a3f1-ed276d6be720	2025-11-02	0	1573.83	1573.83	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #182	\N	2026-01-01 01:56:15.357055+00	\N	t
b6a2e7cf-093b-4cf5-a5a8-4cd8876a9404	d869c705-b0a9-4c69-b73c-e9a3330ad9b4	2025-11-02	0	861.32	861.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #183	\N	2026-01-01 01:56:15.747617+00	\N	t
7785da5c-856b-4033-a672-693f39cebc19	e117b18f-ddff-4e59-a7f8-6bb13844346b	2025-11-02	0	2434.0	2434.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #184	\N	2026-01-01 01:56:16.135805+00	\N	t
caab7fe3-1286-4df6-87a0-76eafc1100de	12527f53-ac93-462b-96b8-26d10933def8	2025-11-02	0	1988.7	1988.7	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #185	\N	2026-01-01 01:56:16.573843+00	\N	t
ec602710-d6c2-49d0-9406-1ffa311e501f	69d8d8d8-0168-475a-9e0e-4405f8de3e8f	2025-11-02	0	397.24	397.24	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #186	\N	2026-01-01 01:56:16.922206+00	\N	t
3de6b496-9ce8-4b69-aefd-5e9fd7b28c1c	ad1f558a-f835-4154-a582-6248d8214779	2025-11-02	0	132.86	132.86	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #187	\N	2026-01-01 01:56:17.252577+00	\N	t
4faba1b1-29d6-470d-bf3d-cef1877951e9	dd67b949-f8de-43dc-a2e9-753e44e9c71d	2025-11-02	0	2042.31	2042.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #188	\N	2026-01-01 01:56:17.69545+00	\N	t
38f3d31e-58b4-4601-8f55-4d43a6f10eb7	c46499e4-668f-4bff-b593-9700ab1b80bc	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #189	\N	2026-01-01 01:56:18.091883+00	\N	t
26bf5012-4f6a-476e-b2a8-211cf2dc4eec	bdbac954-2c78-47cc-a12f-9dab4fecc29a	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #190	\N	2026-01-01 01:56:18.453359+00	\N	t
bd687945-6384-451b-b5ab-83cb4f5b8b74	19b846fd-6611-4ec9-8c77-ab7aaf2ba10a	2025-11-02	0	3176.87	3176.87	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #191	\N	2026-01-01 01:56:18.709821+00	\N	t
abc26f4f-cac8-4736-9011-d592d17f58b9	77300937-e22d-4284-965a-3cc07a69a9c6	2025-11-02	0	1631.65	1631.65	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #192	\N	2026-01-01 01:56:18.92789+00	\N	t
2abd4b1c-5594-4674-a0ee-11847b8fd098	57d776b1-a5a4-4f02-8d59-892236f056fe	2025-11-02	0	1287.44	1287.44	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #193	\N	2026-01-01 01:56:19.207455+00	\N	t
1f6753de-999e-478d-9196-a1fdf9d68377	a3138b77-8dd4-4156-b07a-61d20f4e550b	2025-11-02	0	2353.4	2353.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #194	\N	2026-01-01 01:56:19.511878+00	\N	t
c9291b4a-e6fe-45a2-aec6-0079a632136f	392cd88e-7f43-44c6-bcbe-36cc2f18693a	2025-11-02	0	1716.23	1716.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #195	\N	2026-01-01 01:56:19.769329+00	\N	t
75198f26-e52b-4b1c-ae86-05a3edfe4b05	bde75a98-00f8-403f-ae61-547cc90dffbd	2025-11-02	0	2312.9	2312.9	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #196	\N	2026-01-01 01:56:20.143237+00	\N	t
8c1dc8a6-95d4-4749-a12b-90128375054c	10fb500b-55fe-4955-8018-bd8ddeab44f6	2025-11-02	0	2960.93	2960.93	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #197	\N	2026-01-01 01:56:20.409248+00	\N	t
50c41960-c110-4422-a598-3f2f63cf0efd	8afeef67-edf2-4ed1-895d-201b87d978f2	2025-11-02	0	2520.52	2520.52	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #198	\N	2026-01-01 01:56:20.663568+00	\N	t
54c13eeb-a4ce-4bd2-b72d-a474c2f06634	2533c34b-443d-4bfb-b7dc-a7120f9ff806	2025-11-02	0	1153.29	1153.29	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #199	\N	2026-01-01 01:56:21.058043+00	\N	t
0ad1c402-e01e-40f4-9c8b-2cbd40d911be	eb09516e-cd04-4f0c-ba4d-927b517978f9	2025-11-02	0	865.66	865.66	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #200	\N	2026-01-01 01:56:21.420528+00	\N	t
1be06cc0-7104-43b6-bd64-79b7985d3039	e1f8ddec-3ac3-4230-b6b2-f97ab4c73981	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #201	\N	2026-01-01 01:56:21.801038+00	\N	t
4b8733d3-6837-4ca2-bf19-e698164bc7c0	7b5089a5-d561-45df-a2c1-aec19a17b592	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #202	\N	2026-01-01 01:56:22.157919+00	\N	t
257a27c5-3eec-451c-9a2a-c31b56817ada	958f8067-4c6b-41ee-bdf3-b35ef9bdc719	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #203	\N	2026-01-01 01:56:22.410196+00	\N	t
8ff37221-f883-4437-ae33-4b74b4b67722	ec99581a-43f8-4737-83b6-bc1ed7e29ea9	2025-11-02	0	1826.08	1826.08	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #204	\N	2026-01-01 01:56:22.719372+00	\N	t
6635e749-6a85-4258-a7af-aaf687e7cd5b	3b444922-b8bb-4f5a-90a6-401179b210dc	2025-11-02	0	2930.15	2930.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #205	\N	2026-01-01 01:56:22.973882+00	\N	t
8efc7fff-0f23-46aa-83b6-8e93e141a66c	05d5b252-668e-4f36-ab50-8c8100316232	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #206	\N	2026-01-01 01:56:23.245063+00	\N	t
5e1f1e6b-78bb-40ce-8317-b6165d8caa22	a5dee7eb-677f-4727-9421-6dae7c083f96	2025-11-02	0	1238.32	1238.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #207	\N	2026-01-01 01:56:23.510447+00	\N	t
8b328f1d-7f2f-4b0f-a988-06526293fdd1	89a71600-2bce-4c58-85b4-c9b684f97678	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #208	\N	2026-01-01 01:56:23.7479+00	\N	t
599fb8ad-247b-4599-9372-e8f21575e9f5	20a93627-492e-4256-9b63-4309a5db8ed3	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #209	\N	2026-01-01 01:56:24.004216+00	\N	t
3b3cd199-dd19-4115-9dbc-c2eeb92f064c	aa76c5c1-da5c-4d96-9195-ad493f2221b5	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #210	\N	2026-01-01 01:56:24.256608+00	\N	t
4c8500b4-932b-488e-b4cf-c8c8807d2e55	2124bb26-189d-43f0-8cc8-dca3732ad496	2025-11-02	0	1282.7	1282.7	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #211	\N	2026-01-01 01:56:24.504248+00	\N	t
007d3bfd-9c62-4c4a-b1d9-5df7b035b8f3	b6d5525b-7b28-49d1-869c-63571087871a	2025-11-02	0	1628.1	1628.1	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #212	\N	2026-01-01 01:56:24.737969+00	\N	t
de9538f0-c817-4047-92fe-59746a33ad1a	35f02c20-09ec-4951-a29c-acaf2d6ab78f	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #215	\N	2026-01-01 01:56:25.489656+00	\N	t
617e2747-39f7-4678-9464-8bdf9889d62b	e4f95041-cff4-4b73-b364-3232336debd8	2025-11-02	0	2569.38	2569.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #216	\N	2026-01-01 01:56:25.704821+00	\N	t
6060186c-58e1-483d-83c5-1fb315486414	951c3c89-5b52-4692-8d7d-7e615c2d689f	2025-11-02	0	827.8	827.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #217	\N	2026-01-01 01:56:25.965133+00	\N	t
61cbcf8d-91d9-4738-921d-5534bdf97f42	0bb64877-98aa-4ada-9608-30781f8ad1bc	2025-11-02	0	2036.48	2036.48	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #220	\N	2026-01-01 01:56:26.834815+00	\N	t
bcfefc60-c66f-412e-8aba-ed1f64d3cb54	d0e77232-5f5a-4574-8407-d4c74b498384	2025-11-02	0	1899.48	1899.48	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #221	\N	2026-01-01 01:56:27.090361+00	\N	t
5e141d62-9a2e-4e69-b791-872a74f5a49b	c57fc358-95fe-4cd0-9107-447e9c3b13ba	2025-11-02	0	723.01	723.01	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #222	\N	2026-01-01 01:56:27.383561+00	\N	t
4cba7aae-008d-41f1-99ed-52e980da62bf	c7be1ebc-c4a6-44e4-9704-0fe4f83a5670	2025-11-02	0	1336.24	1336.24	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #225	\N	2026-01-01 01:56:28.399368+00	\N	t
aad5ec37-8ca6-4b06-ada8-60fef86d5681	1b10e99e-bf9f-4385-bb3b-3cb5d4bccc50	2025-11-02	0	1061.33	1061.33	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #226	\N	2026-01-01 01:56:28.819339+00	\N	t
9ba5efed-5d33-4d1a-98a1-9f0d77ffd179	19a4de36-6310-4a17-9f0e-30574fd244c7	2025-11-02	0	1093.93	1093.93	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #227	\N	2026-01-01 01:56:29.193868+00	\N	t
7b9ad6d8-bca4-42d7-9a84-67a303ab5845	d1bbb3e6-e17b-4b20-8787-93b3fb8ee07d	2025-11-02	0	2447.3	2447.3	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #230	\N	2026-01-01 01:56:30.237184+00	\N	t
36bbaec5-2a6b-4df3-9a28-94b49b27380e	02dd7e48-28c9-4aaf-a673-83e86612945e	2025-11-02	0	1844.8	1844.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #231	\N	2026-01-01 01:56:30.674551+00	\N	t
1eb2a78b-f570-4741-830d-ddd1fb6ec7bd	bf4eead3-de35-482c-b4e3-3634dce2151f	2025-11-02	0	1142.0	1142.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #232	\N	2026-01-01 01:56:31.060803+00	\N	t
d03d0f77-bffc-43d6-a081-2ec23d509c9d	1889aaac-9be8-4ef7-a38f-f1ec1bc1833f	2025-11-02	0	1715.22	1715.22	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #235	\N	2026-01-01 01:56:32.080206+00	\N	t
6b5751d4-19a6-46e3-a3df-232acf22c595	0d617991-f0a4-4228-8be4-99d4ab29bea5	2025-11-02	0	523.37	523.37	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #236	\N	2026-01-01 01:56:32.47503+00	\N	t
ad50c15b-4a47-4ac6-8f7e-14300135e61a	94d7665a-4fc1-4647-9189-490938c136dc	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #237	\N	2026-01-01 01:56:32.804117+00	\N	t
55ffa2b8-fb0e-4e20-9d05-5cd3696e52e5	70e4a285-118d-4195-b91b-1f348cbce1d9	2025-11-02	0	1668.38	1668.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #240	\N	2026-01-01 01:56:33.694513+00	\N	t
c55281d2-b7cd-46f4-8bee-27653ef3c055	148e7cb9-3638-4730-8ae2-5bd49603e90c	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #241	\N	2026-01-01 01:56:33.982202+00	\N	t
96453977-10a9-47b5-89b9-dae9454e12ed	8963f78c-8da3-4abd-a9cf-61b9156efe3b	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #242	\N	2026-01-01 01:56:34.335699+00	\N	t
16aed48c-81b5-44f9-b293-dd759df49acc	256d569c-1833-4e43-9bdd-ec8974188157	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #245	\N	2026-01-01 01:56:35.249915+00	\N	t
9a1cda59-7b6e-4683-a905-500afe7ae5e3	1dd944f1-4b29-41cc-9fd4-2369e7237d96	2025-11-02	0	1327.44	1327.44	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #246	\N	2026-01-01 01:56:35.576213+00	\N	t
2744c998-0aca-447b-95be-27fc404dfca8	bedd99e7-edf2-49c4-8d32-1ea5a1ac1d8e	2025-11-02	0	102.8	102.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #247	\N	2026-01-01 01:56:35.935877+00	\N	t
3a609eee-d570-4bd8-a650-37a257c49fe4	92edee2c-d66c-4e8a-8ced-ce2af34a8822	2025-11-02	0	456.86	456.86	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #250	\N	2026-01-01 01:56:37.219406+00	\N	t
c921ff8a-00fa-492f-beed-b2459120d9fc	6974e585-1075-4724-819f-c70fa88b9ba5	2025-11-02	0	473.86	473.86	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #251	\N	2026-01-01 01:56:37.613675+00	\N	t
1503afaf-e42c-45ca-b342-354293ed17f1	00df6a96-9796-4db0-94ee-0b50a8704174	2025-11-02	0	1260.9	1260.9	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #252	\N	2026-01-01 01:56:38.020877+00	\N	t
a03715c6-eaf4-4af3-abbf-69115d34eb29	4c3e1a6f-3b6a-4784-99ef-7a128ae6419e	2025-11-02	0	175.78	175.78	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #254	\N	2026-01-01 01:56:38.985236+00	\N	t
05bb7e54-2d96-44d9-a6e5-0570d8ce9ccd	306d009e-ca42-42d8-8af7-53d496c5cd93	2025-11-02	0	1056.28	1056.28	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #255	\N	2026-01-01 01:56:39.445518+00	\N	t
bb73192d-b182-472e-8750-3037858f80bf	236e0a90-ee4e-4814-ad6c-ddcf9812aae1	2025-11-02	0	753.21	753.21	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #257	\N	2026-01-01 01:56:40.234187+00	\N	t
de80b93b-2f06-4d95-a5fe-c09dc34e5e72	490a1fd0-d181-4a82-a603-5ed5d93761d3	2025-11-02	0	1361.81	1361.81	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #262	\N	2026-01-01 01:56:42.475466+00	\N	t
acec87e1-d33d-450e-a4d2-54ba2c09d546	c86fd546-c600-444f-b5b6-ac48873ff39d	2025-11-02	0	2482.38	2482.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #265	\N	2026-01-01 01:56:43.774742+00	\N	t
cefc894a-a9bf-4c20-9d2d-87cf9a8151e0	97e70dcd-0bb6-4933-b821-d53252a1b07d	2025-11-02	0	1353.2	1353.2	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #266	\N	2026-01-01 01:56:44.121403+00	\N	t
5553d2f9-f7f0-4e06-b92c-aca7104f2d36	6cce134a-3a78-47d2-ac58-7b938ab6eadf	2025-11-02	0	973.41	973.41	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #213	\N	2026-01-01 01:56:25.014258+00	\N	t
02d7459a-5651-49b1-83a1-568b686c46fe	c9b909e0-2425-4895-ac16-eee302405944	2025-11-02	0	1052.72	1052.72	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #214	\N	2026-01-01 01:56:25.270863+00	\N	t
31602c5a-aebc-4b17-8698-728db63ea0bc	de340ddf-9321-4c4f-9f2f-877fb36ed075	2025-11-02	0	2170.28	2170.28	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #218	\N	2026-01-01 01:56:26.255696+00	\N	t
2dfa3681-da6a-47c2-92ef-22054531bd77	6b16ef08-cfc4-4463-89be-3336c1678c4e	2025-11-02	0	2301.09	2301.09	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #219	\N	2026-01-01 01:56:26.522867+00	\N	t
f1580d7e-c860-48db-b27e-b53e538721e0	292bee02-4bae-48b1-9e2f-c1533e3acfd3	2025-11-02	0	822.18	822.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #223	\N	2026-01-01 01:56:27.732629+00	\N	t
66ae06cb-3a42-47a4-806d-bb9edfe6f30b	56b05966-e289-4c4c-bc12-a29357b081c0	2025-11-02	0	831.68	831.68	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #224	\N	2026-01-01 01:56:28.157983+00	\N	t
d77d5509-d1b2-4cf3-bdca-4577eaa33795	04bc5062-f6ba-45d7-9d2c-ac65aa697014	2025-11-02	0	4880.38	4880.38	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #228	\N	2026-01-01 01:56:29.54966+00	\N	t
0be984e5-4a5a-4f22-8de3-18a01204c3c2	029f4c2d-9e8c-4ffa-8f6c-ac0a6cc8382b	2025-11-02	0	1693.96	1693.96	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #229	\N	2026-01-01 01:56:29.937865+00	\N	t
1192263f-c45d-4ca6-ade2-45e566eed410	1bfd624e-dd51-41de-8cb2-fa4533ae4b08	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #233	\N	2026-01-01 01:56:31.497612+00	\N	t
1846ed7b-e954-42b0-bdd3-84bbb37e6cd5	57d1be27-92b6-4751-82a5-a8456d5e05ea	2025-11-02	0	2310.42	2310.42	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #234	\N	2026-01-01 01:56:31.769778+00	\N	t
c2866a2e-0152-473d-8dcb-d13ac3eb9fac	12c6d6d3-4cd9-4fdc-9fb7-8c291a19ea2d	2025-11-02	0	753.84	753.84	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #238	\N	2026-01-01 01:56:33.139983+00	\N	t
9baff7c1-2bbb-41ca-afa9-d2c3cb6405c6	7fa11f6e-62db-40cf-b010-944017372f40	2025-11-02	0	1159.63	1159.63	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #239	\N	2026-01-01 01:56:33.415961+00	\N	t
05d38d08-0bbf-413e-86da-ba62f73d47e5	2f5e1f68-2725-49b5-b5b8-7afd33495e0b	2025-11-02	0	1430.03	1430.03	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #243	\N	2026-01-01 01:56:34.727824+00	\N	t
5ee00912-59df-4af0-8b76-99ddd0215660	ec194603-b597-4559-ba7d-bf10c3ee08e1	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #244	\N	2026-01-01 01:56:34.991815+00	\N	t
7b4e9417-ea9b-4297-b0c3-668bc467612b	4eb35e69-b497-43ef-99a7-b8e879c68782	2025-11-02	0	1638.14	1638.14	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #248	\N	2026-01-01 01:56:36.362132+00	\N	t
66e24b14-cbba-4532-b2ae-5202b9979980	31d2666b-3aeb-4129-9e79-d65b58d2ed33	2025-11-02	0	1252.64	1252.64	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #249	\N	2026-01-01 01:56:36.804638+00	\N	t
2a012fdd-1805-4484-8388-fa5e91cdb977	5d6d6501-5dc4-4435-87aa-28bceee8e115	2025-11-02	0	22.16	22.16	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #253	\N	2026-01-01 01:56:38.453032+00	\N	t
7085fd3e-ad2f-44a4-9c56-af70ca66d620	04754f25-3ef9-4368-a90c-7068cbd3ac29	2025-11-02	0	1132.12	1132.12	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #256	\N	2026-01-01 01:56:39.848663+00	\N	t
20380a13-6cb5-4c7f-88b1-e9ab2c5c3569	54700469-b483-438d-8711-317c6056ccb0	2025-11-02	0	511.32	511.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #258	\N	2026-01-01 01:56:40.647437+00	\N	t
43bed636-080e-450c-bf2b-8e1042c86697	6e90b307-b942-4697-8331-98d19ff48813	2025-11-02	0	178.32	178.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #259	\N	2026-01-01 01:56:41.186867+00	\N	t
80d23496-340d-43c1-a11b-2a9b2095970d	1a333401-5c17-4858-8358-e4934bdb573c	2025-11-02	0	1132.31	1132.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #260	\N	2026-01-01 01:56:41.642043+00	\N	t
4cf1da33-0c18-4ea1-878a-42483cec759b	21e74e47-7a80-4ffb-a85f-5c0dba49fea9	2025-11-02	0	899.43	899.43	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #261	\N	2026-01-01 01:56:42.105023+00	\N	t
cda7ede1-a719-4f4e-8ff1-a9c4923d40db	17373416-012e-4753-ac46-a8480b15969a	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #263	\N	2026-01-01 01:56:42.914163+00	\N	t
be3984d6-3b81-4c0f-97ff-912f6593eed8	c69e8b29-fcc9-4552-8d9e-6869d5187a6b	2025-11-02	0	1558.32	1558.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #264	\N	2026-01-01 01:56:43.34936+00	\N	t
506094f4-9ace-498f-8f14-11e7279d41e9	c49f0400-9143-41a6-a304-149544866e4b	2025-11-02	0	1128.15	1128.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #267	\N	2026-01-01 01:56:44.537377+00	\N	t
08c64271-0faf-4841-86c2-9c620457f763	fa9fc52c-fee8-4979-8051-69b33af511c6	2025-11-02	0	1261.28	1261.28	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #268	\N	2026-01-01 01:56:44.971752+00	\N	t
dc6630d6-a094-42cf-bb75-478c7d91d845	05e9e835-2816-44d7-8fb3-16acd4d2bf29	2025-11-02	0	340.15	340.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #269	\N	2026-01-01 01:56:45.314119+00	\N	t
ae7c18dc-ce33-4b5f-b2cf-4f2cafbdec50	871a64bc-21e0-4033-9fca-bfd5f7b8dbf3	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #270	\N	2026-01-01 01:56:45.643954+00	\N	t
35717849-f75e-49f8-9388-a59cb4c046bd	7bfd9507-2c8f-4504-b5f0-213c4a33b39c	2025-11-02	0	256.31	256.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #271	\N	2026-01-01 01:56:46.149638+00	\N	t
5b683c1d-e93e-4547-bd77-e2365a3c706b	cdf6ac46-9801-43b5-92dc-87237e51e585	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #272	\N	2026-01-01 01:56:46.511896+00	\N	t
34cfe2cb-9407-4f65-b33c-baabd5f19738	f14f3b11-1352-48a4-945c-6d6336138be0	2025-11-02	0	368.32	368.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #273	\N	2026-01-01 01:56:46.838673+00	\N	t
fbe90f34-9491-4784-bf31-0440c42ff4e3	50f7dc6b-91fe-46d7-88de-566078e57d3f	2025-11-02	0	67.51	67.51	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #274	\N	2026-01-01 01:56:47.165061+00	\N	t
482fc45d-d973-474c-8906-dd75ebd598e0	c9c8b0d8-99e7-4745-89b0-9483a6d45d46	2025-11-02	0	151.28	151.28	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #275	\N	2026-01-01 01:56:47.542399+00	\N	t
70e72ac3-501b-4c29-9364-1ee0194c2272	42c79df4-8bad-45c2-bc1b-d70838a6714e	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #276	\N	2026-01-01 01:56:47.881212+00	\N	t
cdaef568-a4de-4a43-9fac-1faef0fd6509	a2e5f2e6-8d32-4f1a-a22d-42bcadd0ef8d	2025-11-02	0	976.13	976.13	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #277	\N	2026-01-01 01:56:48.204786+00	\N	t
ef2bda5b-baad-46e6-8ef2-14c9e4d8f3af	1f4cc66d-40ed-441b-9e7c-92001e493e35	2025-11-02	0	312.83	312.83	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #278	\N	2026-01-01 01:56:48.498426+00	\N	t
d0b3e33a-46a9-4bf6-bcab-45d62b00b434	ab231d82-741d-4fd8-bdf0-8c3560a2b4bf	2025-11-02	0	328.47	328.47	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #279	\N	2026-01-01 01:56:48.80173+00	\N	t
e076dd31-daa4-4014-bc35-ecee4e1d5158	c23b54f9-212f-44e7-9e06-0e88307caa81	2025-11-02	0	973.37	973.37	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #280	\N	2026-01-01 01:56:49.121636+00	\N	t
dcdc0f28-bb6a-4dc0-bdae-66a23ca8ac2e	d40ad2a6-0b6c-4cde-86a6-463b128bad67	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #281	\N	2026-01-01 01:56:49.441894+00	\N	t
51d633e6-4173-48a1-884d-111e3cbb305b	c1539da0-e1ca-4ce2-a45c-7b709cc23359	2025-11-02	0	943.15	943.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #282	\N	2026-01-01 01:56:49.747959+00	\N	t
671adbd0-345d-42a6-8b61-5be58221a48b	e6ee918b-0d8d-4d83-975e-9ff25bc5c471	2025-11-02	0	527.42	527.42	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #283	\N	2026-01-01 01:56:50.059272+00	\N	t
abec2c7d-78a5-4e69-8f6e-9e8030d142fa	b3a51ed9-5106-4fe9-a08c-c9ee2e879503	2025-11-02	0	156.87	156.87	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #284	\N	2026-01-01 01:56:50.414423+00	\N	t
b980fee6-948c-4e07-a61f-64d5139b9189	e2db9406-3ba0-4e7b-ad88-046f905b5ce5	2025-11-02	0	1072.15	1072.15	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #285	\N	2026-01-01 01:56:50.802901+00	\N	t
ea92323b-cb0d-4462-b08d-f3ad7ac7cabc	b8bcd5b2-27ea-4a1e-b2d7-6238913dee2e	2025-11-02	0	828.18	828.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #286	\N	2026-01-01 01:56:51.091632+00	\N	t
2ea83c4a-dd62-4e58-bec7-7b89ab5f6c8d	9eaf7134-42d4-4f23-80f8-617ef4495386	2025-11-02	0	132.18	132.18	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #287	\N	2026-01-01 01:56:51.383464+00	\N	t
0a81529f-4098-46c2-a574-85de9c1ee867	45245773-b59a-401e-8c40-f36105205a4c	2025-11-02	0	628.31	628.31	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #288	\N	2026-01-01 01:56:51.675226+00	\N	t
a16ee2d4-5cef-4449-b169-8cef6b6cb832	27028ad5-d3b1-424f-86cc-8e349d804005	2025-11-02	0	487.34	487.34	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #289	\N	2026-01-01 01:56:51.993172+00	\N	t
5adbd775-a140-4666-a077-cc90b20ba50c	c94ae4cc-4ab6-4014-aa17-7cd30212e230	2025-11-02	0	892.61	892.61	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #290	\N	2026-01-01 01:56:52.304005+00	\N	t
2bc4f638-f0da-4d43-b19e-3d9e033fc5ee	c9cd2b07-7338-45f8-8ce5-bea27f7b926e	2025-11-02	0	107.32	107.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #291	\N	2026-01-01 01:56:52.606725+00	\N	t
c1709200-29d1-4927-8417-3b424a6c5bb0	2e1ca09c-1e51-46df-b6cf-e3573276d7eb	2025-11-02	0	696.2	696.2	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #292	\N	2026-01-01 01:56:52.912862+00	\N	t
eb37bcce-4881-4ed6-93bc-ae6354ce174c	78a0ba41-b5b0-4fda-9186-e1c67401a9cd	2025-11-02	0	293.3	293.3	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #293	\N	2026-01-01 01:56:53.243189+00	\N	t
2503b8d5-a4a0-4708-adb6-10aa1065ae71	de54fa8f-5989-4edd-b432-540bdad6d159	2025-11-02	0	481.03	481.03	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #294	\N	2026-01-01 01:56:53.668957+00	\N	t
f7217846-ece4-4f2b-8d01-2c32fd378f87	d9b65b49-db9f-4732-82e1-d67ee884fd90	2025-11-02	0	401.22	401.22	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #295	\N	2026-01-01 01:56:54.083361+00	\N	t
ea370101-fbda-458b-ab1b-e36d930e8893	b77280e7-af7b-4734-93bf-e0ecc30cd5e3	2025-11-02	0	1090.26	1090.26	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #296	\N	2026-01-01 01:56:54.394769+00	\N	t
a3332aa6-7e7b-41a6-b479-3a4fa0785a4b	b5e938d1-2a77-4d13-896e-9a6449d7f664	2025-11-02	0	243.1	243.1	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #297	\N	2026-01-01 01:56:54.802895+00	\N	t
c7e1e65f-5336-4371-a7e3-5c30aff0d708	c5534908-a6dc-4744-8028-4b82c6cfc41d	2025-11-02	0	595.63	595.63	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #298	\N	2026-01-01 01:56:55.333109+00	\N	t
b763ecc2-7ab1-4e59-9374-a954e16d6a85	970c9cc0-2684-403b-89a0-b358214f28db	2025-11-02	0	207.32	207.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #300	\N	2026-01-01 01:56:56.05225+00	\N	t
f2b67d66-a980-4212-8c1d-99e25d45ed15	bdb668fb-6178-4f71-8972-ad048d9d5604	2025-11-02	0	247.77	247.77	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #301	\N	2026-01-01 01:56:56.477871+00	\N	t
c744e43f-41e3-440c-8988-6714a0b41598	93e411d3-a0ef-48a8-8380-306136e25f76	2025-11-02	0	293.3	293.3	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #302	\N	2026-01-01 01:56:56.788392+00	\N	t
99d55668-75e7-4e8f-a7b0-3339fae8d1b6	929905b9-0cce-47c5-a4e7-802dc015fddb	2025-11-02	0	270.4	270.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #303	\N	2026-01-01 01:56:57.19715+00	\N	t
73a4a4eb-9e4d-4a86-bceb-30dfb21051dc	1d7a5391-bf88-4c2e-b6be-acd5460ccdb6	2025-11-02	0	379.95	379.95	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #305	\N	2026-01-01 01:56:57.96031+00	\N	t
6dceebf1-1ee6-4a31-b52e-cf64b39a21f4	879ec923-ac42-42e5-93f6-cfa03e2defd1	2025-11-02	0	162.78	162.78	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #306	\N	2026-01-01 01:56:58.352743+00	\N	t
e9a0eb88-32b5-441c-a0b9-1add15258aca	84991163-096d-4832-ba8c-61b10b0e1161	2025-11-02	0	260.8	260.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #307	\N	2026-01-01 01:56:58.831417+00	\N	t
af2eaf05-e007-46c9-a228-cf4c21e03001	c3853103-45a2-4e01-9a2c-0160ca693148	2025-11-02	0	310.5	310.5	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #308	\N	2026-01-01 01:56:59.213428+00	\N	t
433e3659-7a7d-45c3-ac97-76cf10198e90	b9b86116-765a-408d-8a00-5787ef56b10a	2025-11-02	0	172.8	172.8	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #310	\N	2026-01-01 01:56:59.865233+00	\N	t
43f0b8ec-df1a-4319-9da9-189d2dafccdb	1e8d5045-b779-4393-b332-6956fce23305	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #311	\N	2026-01-01 01:57:00.203834+00	\N	t
18525b63-32aa-442e-a6fd-87ae2953758c	9ccd19d5-103e-4c0e-b0ff-9c4840ff3cbf	2025-11-02	0	79.07	79.07	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #312	\N	2026-01-01 01:57:00.680305+00	\N	t
4d9ee365-18a2-4417-86d8-e57e12de1b25	a7ce0074-e633-46b9-b827-f33992454045	2025-11-02	0	577.9	577.9	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #313	\N	2026-01-01 01:57:01.161482+00	\N	t
c32a0f9a-522e-4e1b-9953-72ce0ceea087	9d29645b-8fbd-4696-bbab-6f757f177e59	2025-11-02	0	27.4	27.4	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #315	\N	2026-01-01 01:57:01.998214+00	\N	t
b0bd066d-799f-4ba6-a0fe-8ff67d3809b7	e973e4a6-2ba2-435e-9eb9-00409a2e6da1	2025-11-02	0	101.23	101.23	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #316	\N	2026-01-01 01:57:02.426771+00	\N	t
a642df5b-b396-4716-84c4-bcae1465448c	44d326a5-a313-4eea-8440-57e3db0188b2	2025-11-02	0	121.32	121.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #317	\N	2026-01-01 01:57:02.845183+00	\N	t
7fff3478-9bd1-45bd-ad2e-0dd227b3d2bc	f8351303-e530-4418-8928-a10533ff5b4a	2025-11-02	0	48.32	48.32	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #318	\N	2026-01-01 01:57:03.286282+00	\N	t
1527e519-c5a7-4222-aac4-105a4e44c9c5	60d93965-e4cd-4bfb-9c74-03ad952d751a	2025-11-02	0	36.09	36.09	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #320	\N	2026-01-01 01:57:04.064524+00	\N	t
fe8bacae-e101-4a0c-be5a-d65d39af8dc7	3f630a42-4cfc-4b33-b530-75efc6628541	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #321	\N	2026-01-01 01:57:04.353834+00	\N	t
d02e88e2-7b66-45fe-ba37-ef0a8995fdf6	cbc45fb0-c556-471a-87ee-88d0c196ae31	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #322	\N	2026-01-01 01:57:04.810999+00	\N	t
aeab16ae-1772-4c15-8296-c6822f271a63	3c212b2b-74b3-4070-88d7-18dbc1d6f391	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #323	\N	2026-01-01 01:57:05.239597+00	\N	t
39b6431f-e8af-41d4-a3a1-d77823b49b73	cbcd0238-446a-449b-a705-de93bb3df029	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #325	\N	2026-01-01 01:57:06.053581+00	\N	t
ef3dfb56-a42b-459a-9b07-bf8b1eda8907	a362d206-869e-4f98-9cd6-4943054cfbc1	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #326	\N	2026-01-01 01:57:06.40718+00	\N	t
5c0ab2e0-932b-491f-9cc8-9dbd24da38c5	1b620f70-cf94-44c5-991b-f5a4a8b49f21	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #327	\N	2026-01-01 01:57:06.741123+00	\N	t
5abbf011-2982-458f-a267-a0ceec8e480b	7c956972-1ea1-4bd7-90f2-870fc40b2d4a	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #328	\N	2026-01-01 01:57:07.068405+00	\N	t
4c38f349-39d5-4c5b-9afe-24960c218c59	86bdcf36-477a-41fe-a7fc-e3991412f7fe	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #330	\N	2026-01-01 01:57:07.734339+00	\N	t
409e513a-82b5-4491-83a0-cff9619d385c	9f309b5c-0b02-47ba-8119-74fad863bacc	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #331	\N	2026-01-01 01:57:08.07763+00	\N	t
7e531b97-b5de-40c9-83fa-446ecf482352	af4bb08a-1917-4cfb-b483-5605a9ad5321	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #332	\N	2026-01-01 01:57:08.554131+00	\N	t
6b9a4395-aec5-4068-b668-2c8aa24177c1	60cf574a-68e7-463e-a1e4-398f9cd6548e	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #333	\N	2026-01-01 01:57:08.888559+00	\N	t
1319675b-3046-4f15-9eda-3edd12bdc3fc	2d56bf21-d022-49aa-936e-59d94e985b3d	2025-11-02	0	509.59	509.59	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #299	\N	2026-01-01 01:56:55.593951+00	\N	t
848ef6c2-4b9b-48f6-a869-94e1eb079056	41f1f4a3-5aab-4f89-8ac6-005344077dd4	2025-11-02	0	228.1	228.1	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #304	\N	2026-01-01 01:56:57.535949+00	\N	t
05677686-2b4b-4745-8213-6a99024afac8	3c29d80a-ffc6-46c6-8470-7acef6748a6b	2025-11-02	0	450.54	450.54	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #309	\N	2026-01-01 01:56:59.507285+00	\N	t
3c8ea1f5-c7ca-43c2-81c4-8eca91f0689b	d6b95c19-2878-401e-bcc3-ba8f9a022041	2025-11-02	0	110.11	110.11	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #314	\N	2026-01-01 01:57:01.616029+00	\N	t
ecc4aca0-31f9-407d-92fa-81a3815dad32	b0a7f24c-53f4-4c76-9299-9cd5ef7300ae	2025-11-02	0	14.65	14.65	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #319	\N	2026-01-01 01:57:03.725437+00	\N	t
68a0954a-ccdd-489f-9a59-8a42fe83aa1a	86d6ae14-8b49-40e3-8d98-079e91b5c12b	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #324	\N	2026-01-01 01:57:05.717887+00	\N	t
6d6d2430-46f7-4d27-bef1-3c4d870d6f57	8ea24889-2143-4840-ab29-f3f7f130f2d2	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #329	\N	2026-01-01 01:57:07.405885+00	\N	t
50695d48-420a-4f8c-87a4-1f01721ff07b	30dbc470-ebf1-4f82-87f6-0707a7034643	2025-11-02	0	0.0	0.0	03f10951-9d34-4424-9fc5-1a5b6ef252ae	Importación masiva - Socio #334	\N	2026-01-01 01:57:09.37982+00	\N	t
34a29941-9e27-4fa1-8585-d7c4f3fe9d15	eee5b2e6-4a4b-4db7-a61a-4aad2f1a5d49	2026-01-03	38.24	48.93	10.69	\N		\N	2026-01-03 13:23:45.465907+00	\N	t
2f21e6cf-35ba-4ab4-83fa-322021e8dd84	bbea725d-b15a-46cb-9c53-568581423fd6	2026-01-03	914.26	916.06	1.80	\N		\N	2026-01-03 14:04:53.406253+00	\N	t
29c23f66-ed8f-4c2b-86b3-5be2ed750d9a	2d56bf21-d022-49aa-936e-59d94e985b3d	2026-01-03	509.59	521.91	12.32	\N		\N	2026-01-03 13:26:21.033322+00	\N	t
1c6afbec-c2ba-4b72-8210-b603f911a6d4	bdb668fb-6178-4f71-8972-ad048d9d5604	2026-01-03	247.77	248.24	0.47	\N		\N	2026-01-03 13:27:10.095178+00	\N	t
29f0f083-03de-41e0-a5d2-3b2164a91072	eb09516e-cd04-4f0c-ba4d-927b517978f9	2026-01-03	865.66	870.60	4.94	\N		\N	2026-01-03 13:27:48.680262+00	\N	t
cd5479b5-e2ed-48cb-a8b0-296cff3fa789	8c35ac64-abc4-4c83-abca-a839e49577db	2026-01-08	193.26	1596.92	1403.66	\N		\N	2026-01-08 16:14:28.918762+00	\N	t
f7b4ac80-112c-4d9d-b0d3-40cebbc26d0e	84991163-096d-4832-ba8c-61b10b0e1161	2026-01-08	260.8	370.99	110.19	\N		\N	2026-01-08 16:15:35.33714+00	\N	t
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
-- Name: partner_number_seq; Type: SEQUENCE SET; Schema: pos; Owner: postgres
--

SELECT pg_catalog.setval('pos.partner_number_seq', 334, true);


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

