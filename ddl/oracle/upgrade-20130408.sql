-- adds the migration flag which is needed for version 2.2 (after Nov 2012)
alter table KALTURA_COLL add (MIGRATED number(1,0) default 0 not null);
alter table KALTURA_ITEM add (MIGRATED number(1,0) default 0 not null);
