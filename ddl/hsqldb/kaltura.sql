
    create table KALTURA_COLL (
        id bigint generated by default as identity (start with 1),
        locationId varchar(255) not null,
        ownerId varchar(255) not null,
        CREATED_ON timestamp not null,
        LAST_MODIFIED timestamp not null,
        title varchar(255) not null,
        description longvarchar,
        hidden bit not null,
        SHARING_PERM varchar(255) not null,
        MIGRATED bit default false not null,
        primary key (id)
    );

    create table KALTURA_ITEM (
        id bigint generated by default as identity (start with 1),
        ownerId varchar(255) not null,
        creatorId varchar(255),
        kalturaId varchar(255) not null,
        DISPLAY_POSITION integer not null,
        COLLECTION_FK bigint,
        locationId varchar(255),
        CREATED_ON timestamp not null,
        LAST_MODIFIED timestamp not null,
        hidden bit not null,
        shared bit not null,
        remixable bit not null,
        MIGRATED bit default false not null,
        primary key (id)
    );

    alter table KALTURA_ITEM 
        add constraint ITEM_COLL_FKC 
        foreign key (COLLECTION_FK) 
        references KALTURA_COLL;
