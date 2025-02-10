SET schema 'ssm';

-- FUNCTION: RowCreateTimestamp()

-- DROP FUNCTION IF EXISTS "RowCreateTimestamp"();

CREATE OR REPLACE FUNCTION "RowCreateTimestamp"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
  NEW.create_instant = NOW();
  RETURN NEW;
END;
$BODY$;

COMMENT ON FUNCTION "RowCreateTimestamp"()
    IS 'Sets the create_instant to the current time on row creation';


-- FUNCTION: RowUpdateTimestamp()

-- DROP FUNCTION IF EXISTS "RowUpdateTimestamp"();

CREATE OR REPLACE FUNCTION "RowUpdateTimestamp"()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
  NEW.update_instant = NOW();
  RETURN NEW;
END;
$BODY$;

COMMENT ON FUNCTION "RowUpdateTimestamp"()
    IS 'Sets the update_instant column to the current time on row update';


-- Table: images

-- DROP TABLE IF EXISTS images;

CREATE TABLE IF NOT EXISTS images
(
    image_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    image_oid oid NOT NULL,
    create_instant timestamp with time zone,
    update_instant timestamp with time zone,
    CONSTRAINT images_pkey PRIMARY KEY (image_name)
)

TABLESPACE pg_default;



-- Table: users

-- DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
    id bigint NOT NULL,
    username character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    account_expiration timestamp without time zone,
    credential_expiration timestamp without time zone,
    locked boolean,
    enabled boolean,
    create_instant timestamp without time zone,
    update_instant timestamp without time zone,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT username UNIQUE (username)
)

TABLESPACE pg_default;

CREATE SEQUENCE IF NOT EXISTS users_id_seq1
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    OWNED BY users.id;

ALTER TABLE IF EXISTS users
    ALTER COLUMN id SET DEFAULT nextval('users_id_seq1'::regclass);


-- Table: component_type

-- DROP TABLE IF EXISTS component_type;

CREATE TABLE IF NOT EXISTS component_type
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    encrypted boolean,
    ql_name character varying(64) COLLATE pg_catalog."default",
    CONSTRAINT component_type_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;


-- Table: key_type

-- DROP TABLE IF EXISTS key_type;

CREATE TABLE IF NOT EXISTS key_type
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(256) COLLATE pg_catalog."default",
    abbr character varying(64) COLLATE pg_catalog."default",
    CONSTRAINT key_type_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;



-- Table: secret_type

-- DROP TABLE IF EXISTS secret_type;

CREATE TABLE IF NOT EXISTS secret_type
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    abbr character varying(64) COLLATE pg_catalog."default",
    CONSTRAINT secret_type_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;




-- Table: keys

-- DROP TABLE IF EXISTS keys;

CREATE TABLE IF NOT EXISTS keys
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(64) COLLATE pg_catalog."default",
    comments character varying(255) COLLATE pg_catalog."default",
    type_id character varying(64) COLLATE pg_catalog."default",
    key_password character varying(255) COLLATE pg_catalog."default",
    image_name character varying(64) COLLATE pg_catalog."default",
    owner character varying(64) COLLATE pg_catalog."default",
    salt character varying(64) COLLATE pg_catalog."default",
    algorithm character varying(64) COLLATE pg_catalog."default",
    create_instant timestamp with time zone,
    update_instant timestamp with time zone,
    CONSTRAINT keys_pkey PRIMARY KEY (id),
    CONSTRAINT key_type_id FOREIGN KEY (type_id)
        REFERENCES key_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT owner FOREIGN KEY (owner)
        REFERENCES users (username) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)

TABLESPACE pg_default;


-- Trigger: RowCreateTimestamp

-- DROP TRIGGER IF EXISTS "RowCreateTimestamp" ON keys;

CREATE OR REPLACE TRIGGER "RowCreateTimestamp"
    BEFORE INSERT
    ON keys
    FOR EACH ROW
    EXECUTE FUNCTION "RowCreateTimestamp"();

-- Trigger: RowUpdateTimestamp

-- DROP TRIGGER IF EXISTS "RowUpdateTimestamp" ON keys;

CREATE OR REPLACE TRIGGER "RowUpdateTimestamp"
    BEFORE INSERT OR UPDATE
    ON keys
    FOR EACH ROW
    EXECUTE FUNCTION "RowUpdateTimestamp"();


-- Table: encrypted_keys

-- DROP TABLE IF EXISTS encrypted_keys;

CREATE TABLE IF NOT EXISTS encrypted_keys
(
    id bigint NOT NULL,
    owner character varying(64) COLLATE pg_catalog."default",
    user_key_id character varying(64) COLLATE pg_catalog."default",
    encrypted_key character varying(256) COLLATE pg_catalog."default",
    encryption_algorithm character varying(64) COLLATE pg_catalog."default",
    iv character varying(64) COLLATE pg_catalog."default",
    create_instant timestamp with time zone,
    update_instant timestamp with time zone,
    CONSTRAINT encrypted_keys_pkey PRIMARY KEY (id),
    CONSTRAINT user_key FOREIGN KEY (user_key_id)
        REFERENCES keys (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)

TABLESPACE pg_default;

CREATE SEQUENCE IF NOT EXISTS encrypted_keys_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1
    OWNED BY encrypted_keys.id;

ALTER TABLE IF EXISTS encrypted_keys
    ALTER COLUMN id SET DEFAULT nextval('encrypted_keys_id_seq'::regclass);

-- Trigger: RowCreateTimestamp

-- DROP TRIGGER IF EXISTS "RowCreateTimestamp" ON encrypted_keys;

CREATE OR REPLACE TRIGGER "RowCreateTimestamp"
    BEFORE INSERT
    ON encrypted_keys
    FOR EACH ROW
    EXECUTE FUNCTION "RowCreateTimestamp"();

-- Trigger: RowUpdateTimestamp

-- DROP TRIGGER IF EXISTS "RowUpdateTimestamp" ON encrypted_keys;

CREATE OR REPLACE TRIGGER "RowUpdateTimestamp"
    BEFORE INSERT OR UPDATE
    ON encrypted_keys
    FOR EACH ROW
    EXECUTE FUNCTION "RowUpdateTimestamp"();




-- Table: secrets

-- DROP TABLE IF EXISTS secrets;

CREATE TABLE IF NOT EXISTS secrets
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    secret_type character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(64) COLLATE pg_catalog."default" NOT NULL,
    comments character varying(1024) COLLATE pg_catalog."default",
    key_id character varying COLLATE pg_catalog."default",
    owner character varying(64) COLLATE pg_catalog."default" NOT NULL,
    image_name character varying(64) COLLATE pg_catalog."default",
    create_instant timestamp with time zone,
    update_instant timestamp with time zone,
    CONSTRAINT secrets_pkey PRIMARY KEY (id),
    CONSTRAINT secret_type FOREIGN KEY (secret_type)
        REFERENCES secret_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)

TABLESPACE pg_default;


-- Trigger: RowCreateTimestamp

-- DROP TRIGGER IF EXISTS "RowCreateTimestamp" ON secrets;

CREATE OR REPLACE TRIGGER "RowCreateTimestamp"
    BEFORE INSERT
    ON secrets
    FOR EACH ROW
    EXECUTE FUNCTION "RowCreateTimestamp"();

-- Trigger: RowUpdateTimestamp

-- DROP TRIGGER IF EXISTS "RowUpdateTimestamp" ON secrets;

CREATE OR REPLACE TRIGGER "RowUpdateTimestamp"
    BEFORE INSERT OR UPDATE
    ON secrets
    FOR EACH ROW
    EXECUTE FUNCTION "RowUpdateTimestamp"();


-- Table: secret_components

-- DROP TABLE IF EXISTS secret_components;

CREATE TABLE IF NOT EXISTS secret_components
(
    id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    secret_id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    component_type character varying(64) COLLATE pg_catalog."default" NOT NULL,
    value character varying(1048576) COLLATE pg_catalog."default",
    encrypted boolean,
    encryption_algorithm character varying(64) COLLATE pg_catalog."default",
    create_instant timestamp with time zone,
    update_instant timestamp with time zone,
    CONSTRAINT secret_components_pkey PRIMARY KEY (id),
    CONSTRAINT component_type FOREIGN KEY (component_type)
        REFERENCES component_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT secret_id FOREIGN KEY (secret_id)
        REFERENCES secrets (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;


-- Trigger: RowCreateTimestamp

-- DROP TRIGGER IF EXISTS "RowCreateTimestamp" ON secret_components;

CREATE OR REPLACE TRIGGER "RowCreateTimestamp"
    BEFORE INSERT
    ON secret_components
    FOR EACH ROW
    EXECUTE FUNCTION "RowCreateTimestamp"();

-- Trigger: RowUpdateTimestamp

-- DROP TRIGGER IF EXISTS "RowUpdateTimestamp" ON secret_components;

CREATE OR REPLACE TRIGGER "RowUpdateTimestamp"
    BEFORE INSERT OR UPDATE
    ON secret_components
    FOR EACH ROW
    EXECUTE FUNCTION "RowUpdateTimestamp"();