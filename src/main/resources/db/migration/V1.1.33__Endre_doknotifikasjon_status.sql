alter table doknotifikasjon_status_beskjed drop column melding;
alter table doknotifikasjon_status_beskjed drop column distribusjonsid;
alter table doknotifikasjon_status_beskjed drop column antall_oppdateringer;
alter table doknotifikasjon_status_beskjed rename column tidspunkt to sistOppdatert;
alter table doknotifikasjon_status_beskjed rename column status to sistMottattStatus;

alter table doknotifikasjon_status_oppgave drop column melding;
alter table doknotifikasjon_status_oppgave drop column distribusjonsid;
alter table doknotifikasjon_status_oppgave drop column antall_oppdateringer;
alter table doknotifikasjon_status_oppgave rename column tidspunkt to sistOppdatert;
alter table doknotifikasjon_status_oppgave rename column status to sistMottattStatus;

alter table doknotifikasjon_status_innboks drop column melding;
alter table doknotifikasjon_status_innboks drop column distribusjonsid;
alter table doknotifikasjon_status_innboks drop column antall_oppdateringer;
alter table doknotifikasjon_status_innboks rename column tidspunkt to sistOppdatert;
alter table doknotifikasjon_status_innboks rename column status to sistMottattStatus;

alter table doknotifikasjon_status_beskjed add column eksternVarslingSendt bool;
alter table doknotifikasjon_status_oppgave add column eksternVarslingSendt bool;
alter table doknotifikasjon_status_innboks add column eksternVarslingSendt bool;

alter table doknotifikasjon_status_beskjed add column renotifikasjonSendt bool;
alter table doknotifikasjon_status_oppgave add column renotifikasjonSendt bool;
alter table doknotifikasjon_status_innboks add column renotifikasjonSendt bool;

alter table doknotifikasjon_status_beskjed add column historikk jsonb;
alter table doknotifikasjon_status_oppgave add column historikk jsonb;
alter table doknotifikasjon_status_innboks add column historikk jsonb;

alter table doknotifikasjon_status_beskjed rename to ekstern_varsling_status_beskjed;
alter table doknotifikasjon_status_oppgave rename to ekstern_varsling_status_oppgave;
alter table doknotifikasjon_status_innboks rename to ekstern_varsling_status_innboks;

create view doknotifikasjon_status_beskjed as select *, sistMottattStatus as status from ekstern_varsling_status_beskjed;
create view doknotifikasjon_status_oppgave as select *, sistMottattStatus as status from ekstern_varsling_status_oppgave;
create view doknotifikasjon_status_innboks as select *, sistMottattStatus as status from ekstern_varsling_status_innboks;
