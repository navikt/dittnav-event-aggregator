DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO eventhandler;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO eventhandler;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO eventhandler;
        END IF;
    END
$$;
