
    create table KALTURA_COLL (
        id bigint not null auto_increment,
        locationId varchar(255) not null,
        ownerId varchar(255) not null,
        CREATED_ON datetime not null,
        LAST_MODIFIED datetime not null,
        title varchar(255) not null,
        description text,
        hidden bit not null,
        SHARING_PERM varchar(255) not null,
        MIGRATED bit default false not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table KALTURA_ITEM (
        id bigint not null auto_increment,
        ownerId varchar(255) not null,
        creatorId varchar(255),
        kalturaId varchar(255) not null,
        DISPLAY_POSITION integer not null,
        COLLECTION_FK bigint,
        locationId varchar(255),
        CREATED_ON datetime not null,
        LAST_MODIFIED datetime not null,
        hidden bit not null,
        shared bit not null,
        remixable bit not null,
        MIGRATED bit default false not null,
        primary key (id)
    ) ENGINE=InnoDB;

    alter table KALTURA_ITEM 
        add index ITEM_COLL_FKC (COLLECTION_FK), 
        add constraint ITEM_COLL_FKC 
        foreign key (COLLECTION_FK) 
        references KALTURA_COLL (id);
