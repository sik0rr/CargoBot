--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3
-- Dumped by pg_dump version 15.3

-- Started on 2025-01-22 17:06:17

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 24713)
-- Name: cargolist; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cargolist (
    id integer NOT NULL,
    wherefrom text,
    whereto text,
    price text,
    weight text,
    size text,
    commentary text,
    dateadded date,
    username text
);


ALTER TABLE public.cargolist OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 24712)
-- Name: cargolist_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cargolist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cargolist_id_seq OWNER TO postgres;

--
-- TOC entry 3343 (class 0 OID 0)
-- Dependencies: 214
-- Name: cargolist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cargolist_id_seq OWNED BY public.cargolist.id;


--
-- TOC entry 217 (class 1259 OID 24722)
-- Name: userlist; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.userlist (
    id integer NOT NULL,
    username text,
    accesslevel integer
);


ALTER TABLE public.userlist OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 24721)
-- Name: userlist_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.userlist_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userlist_id_seq OWNER TO postgres;

--
-- TOC entry 3344 (class 0 OID 0)
-- Dependencies: 216
-- Name: userlist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.userlist_id_seq OWNED BY public.userlist.id;


--
-- TOC entry 219 (class 1259 OID 32911)
-- Name: userrating; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.userrating (
    id integer NOT NULL,
    username text,
    rating integer
);


ALTER TABLE public.userrating OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 32910)
-- Name: userrating_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.userrating_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userrating_id_seq OWNER TO postgres;

--
-- TOC entry 3345 (class 0 OID 0)
-- Dependencies: 218
-- Name: userrating_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.userrating_id_seq OWNED BY public.userrating.id;


--
-- TOC entry 3183 (class 2604 OID 24716)
-- Name: cargolist id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cargolist ALTER COLUMN id SET DEFAULT nextval('public.cargolist_id_seq'::regclass);


--
-- TOC entry 3184 (class 2604 OID 24725)
-- Name: userlist id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userlist ALTER COLUMN id SET DEFAULT nextval('public.userlist_id_seq'::regclass);


--
-- TOC entry 3185 (class 2604 OID 32914)
-- Name: userrating id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userrating ALTER COLUMN id SET DEFAULT nextval('public.userrating_id_seq'::regclass);


--
-- TOC entry 3333 (class 0 OID 24713)
-- Dependencies: 215
-- Data for Name: cargolist; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cargolist (id, wherefrom, whereto, price, weight, size, commentary, dateadded, username) FROM stdin;
178	Самара	Заинск	12000 с ндс 10000 без ндс	1.2	4	груз готов	2025-01-22	samara_121
179	набережные челны	Самара	13000 с ндс	1.2	5	груз готов	2025-01-22	samara_121
\.


--
-- TOC entry 3335 (class 0 OID 24722)
-- Dependencies: 217
-- Data for Name: userlist; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.userlist (id, username, accesslevel) FROM stdin;
4	sik0rr	3
5	samara_121	3
6	shredin	2
7	kathar28	2
9	CargoBot1	2
\.


--
-- TOC entry 3337 (class 0 OID 32911)
-- Dependencies: 219
-- Data for Name: userrating; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.userrating (id, username, rating) FROM stdin;
2	sik0rr	3
4	samara_121	100
5	CargoBot1	99
\.


--
-- TOC entry 3346 (class 0 OID 0)
-- Dependencies: 214
-- Name: cargolist_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cargolist_id_seq', 181, true);


--
-- TOC entry 3347 (class 0 OID 0)
-- Dependencies: 216
-- Name: userlist_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.userlist_id_seq', 10, true);


--
-- TOC entry 3348 (class 0 OID 0)
-- Dependencies: 218
-- Name: userrating_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.userrating_id_seq', 5, true);


--
-- TOC entry 3187 (class 2606 OID 24720)
-- Name: cargolist cargolist_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cargolist
    ADD CONSTRAINT cargolist_pkey PRIMARY KEY (id);


--
-- TOC entry 3189 (class 2606 OID 24729)
-- Name: userlist userlist_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userlist
    ADD CONSTRAINT userlist_pkey PRIMARY KEY (id);


-- Completed on 2025-01-22 17:06:17

--
-- PostgreSQL database dump complete
--

