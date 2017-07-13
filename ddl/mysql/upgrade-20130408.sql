-- adds the migration flag which is needed for version 2.2 (after Nov 2012)
alter table KALTURA_COLL add MIGRATED bit default false not null;
alter table KALTURA_ITEM add MIGRATED bit default false not null;
