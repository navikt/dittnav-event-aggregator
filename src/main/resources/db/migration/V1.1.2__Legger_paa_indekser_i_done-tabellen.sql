CREATE INDEX IF NOT EXISTS done_index_for_fields_used_in_delete_query
    ON done (eventid, systembruker, fodselsnummer);
