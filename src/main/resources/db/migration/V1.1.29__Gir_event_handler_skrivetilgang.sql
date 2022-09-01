DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 FROM pg_roles WHERE rolname = 'eventhandler')
        THEN
            GRANT USAGE ON SCHEMA public TO eventhandler;
            GRANT ALL ON ALL TABLES IN SCHEMA public TO eventhandler;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO eventhandler;
        END IF;
    END
$$;