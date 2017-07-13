
    create table KALTURA_COLL (
        id number(19,0) not null,
        locationId varchar2(255 char) not null,
        ownerId varchar2(255 char) not null,
        CREATED_ON timestamp not null,
        LAST_MODIFIED timestamp not null,
        title varchar2(255 char) not null,
        description clob,
        hidden number(1,0) not null,
        SHARING_PERM varchar2(255 char) not null,
        MIGRATED number(1,0) default 0 not null,
        primary key (id)
    );

    create table KALTURA_ITEM (
        id number(19,0) not null,
        ownerId varchar2(255 char) not null,
        creatorId varchar2(255 char),
        kalturaId varchar2(255 char) not null,
        DISPLAY_POSITION number(10,0) not null,
        COLLECTION_FK number(19,0),
        locationId varchar2(255 char),
        CREATED_ON timestamp not null,
        LAST_MODIFIED timestamp not null,
        hidden number(1,0) not null,
        shared number(1,0) not null,
        remixable number(1,0) not null,
        MIGRATED number(1,0) default 0 not null,
        primary key (id)
    );

    alter table KALTURA_ITEM 
        add constraint ITEM_COLL_FKC 
        foreign key (COLLECTION_FK) 
        references KALTURA_COLL;

    create sequence KALTURA_COLL_ID_SEQ;

    create sequence KALTURA_ITEM_ID_SEQ;
