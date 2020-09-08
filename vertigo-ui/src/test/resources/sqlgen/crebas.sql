-- ============================================================
--   SGBD      		  :  H2                     
-- ============================================================

-- ============================================================
--   Drop                                       
-- ============================================================
drop table IF EXISTS ProSro cascade;
drop table IF EXISTS APPLICATION_USER cascade;
drop sequence IF EXISTS SEQ_APPLICATION_USER;
drop table IF EXISTS CASTING cascade;
drop sequence IF EXISTS SEQ_CASTING;
drop table IF EXISTS COMMUNE cascade;
drop sequence IF EXISTS SEQ_COMMUNE;
drop table IF EXISTS MOVIE cascade;
drop sequence IF EXISTS SEQ_MOVIE;
drop table IF EXISTS OUI_NON_CHOICE cascade;
drop table IF EXISTS PEOPLE cascade;
drop sequence IF EXISTS SEQ_PEOPLE;
drop table IF EXISTS PROFIL cascade;
drop sequence IF EXISTS SEQ_PROFIL;
drop table IF EXISTS SECURITY_ROLE cascade;
drop table IF EXISTS USER_AUTHENTIFICATION cascade;
drop sequence IF EXISTS SEQ_USER_AUTHENTIFICATION;




-- ============================================================
--   Sequences                                      
-- ============================================================
create sequence SEQ_APPLICATION_USER
	start with 1000 cache 20; 

create sequence SEQ_CASTING
	start with 1000 cache 20; 

create sequence SEQ_COMMUNE
	start with 1000 cache 20; 

create sequence SEQ_MOVIE
	start with 1000 cache 20; 


create sequence SEQ_PEOPLE
	start with 1000 cache 20; 

create sequence SEQ_PROFIL
	start with 1000 cache 20; 


create sequence SEQ_USER_AUTHENTIFICATION
	start with 1000 cache 20; 


-- ============================================================
--   Table : APPLICATION_USER                                        
-- ============================================================
create table APPLICATION_USER
(
    USR_ID      	 BIGINT      	not null,
    LAST_NAME   	 VARCHAR(50) 	,
    FIRST_NAME  	 VARCHAR(50) 	,
    EMAIL       	 VARCHAR(255)	,
    PRO_ID      	 BIGINT      	,
    constraint PK_APPLICATION_USER primary key (USR_ID)
);

comment on column APPLICATION_USER.USR_ID is
'USR_ID';

comment on column APPLICATION_USER.LAST_NAME is
'Last Name';

comment on column APPLICATION_USER.FIRST_NAME is
'First Name';

comment on column APPLICATION_USER.EMAIL is
'email';

comment on column APPLICATION_USER.PRO_ID is
'Profil';

-- ============================================================
--   Table : CASTING                                        
-- ============================================================
create table CASTING
(
    CAST_ID     	 BIGINT      	not null,
    CHARACTER_NAME	 VARCHAR(250)	,
    PEO_ID      	 BIGINT      	not null,
    MOV_ID      	 BIGINT      	not null,
    constraint PK_CASTING primary key (CAST_ID)
);

comment on column CASTING.CAST_ID is
'Cast_id';

comment on column CASTING.CHARACTER_NAME is
'Character name';

comment on column CASTING.PEO_ID is
'People';

comment on column CASTING.MOV_ID is
'Movie';

-- ============================================================
--   Table : COMMUNE                                        
-- ============================================================
create table COMMUNE
(
    ID_INSEE    	 BIGINT      	not null,
    CODE_POSTAL 	 VARCHAR(5)  	,
    COMMUNE     	 VARCHAR(100)	,
    DEPARTEMENT 	 VARCHAR(100)	,
    constraint PK_COMMUNE primary key (ID_INSEE)
);

comment on column COMMUNE.ID_INSEE is
'idInsee';

comment on column COMMUNE.CODE_POSTAL is
'codePostal';

comment on column COMMUNE.COMMUNE is
'commune';

comment on column COMMUNE.DEPARTEMENT is
'departement';

-- ============================================================
--   Table : MOVIE                                        
-- ============================================================
create table MOVIE
(
    MOV_ID      	 BIGINT      	not null,
    TITLE       	 VARCHAR(250)	,
    RELEASED    	 DATE        	,
    YEAR        	 NUMERIC     	,
    RUNTIME     	 NUMERIC     	,
    DESCRIPTION 	 TEXT        	,
    POSTER      	 VARCHAR(250)	,
    TAGS	      	 VARCHAR(250)	,
    RATED       	 VARCHAR(250)	,
    LAST_MODIFIED	 TIMESTAMP   	,
    constraint PK_MOVIE primary key (MOV_ID)
);

comment on column MOVIE.MOV_ID is
'MOV_ID';

comment on column MOVIE.TITLE is
'TITLE';

comment on column MOVIE.RELEASED is
'Released';

comment on column MOVIE.YEAR is
'Year';

comment on column MOVIE.RUNTIME is
'Runtime';

comment on column MOVIE.DESCRIPTION is
'Description';

comment on column MOVIE.POSTER is
'Poster';

comment on column MOVIE.RATED is
'rated';

comment on column MOVIE.LAST_MODIFIED is
'lastModified';

-- ============================================================
--   Table : OUI_NON_CHOICE                                        
-- ============================================================
create table OUI_NON_CHOICE
(
    KEY         	 BOOLEAN     	not null,
    LIBELLE     	 VARCHAR(100)	,
    constraint PK_OUI_NON_CHOICE primary key (KEY)
);

comment on column OUI_NON_CHOICE.KEY is
'Valeur';

comment on column OUI_NON_CHOICE.LIBELLE is
'Libellé';

-- ============================================================
--   Table : PEOPLE                                        
-- ============================================================
create table PEOPLE
(
    PEO_ID      	 BIGINT      	not null,
    LAST_NAME   	 VARCHAR(50) 	,
    FIRST_NAME  	 VARCHAR(50) 	,
    PEO_NAME    	 VARCHAR(250)	,
    IMDBID      	 VARCHAR(100)	,
    constraint PK_PEOPLE primary key (PEO_ID)
);

comment on column PEOPLE.PEO_ID is
'PEO_ID';

comment on column PEOPLE.LAST_NAME is
'Last Name';

comment on column PEOPLE.FIRST_NAME is
'First Name';

comment on column PEOPLE.PEO_NAME is
'Peo Name';

comment on column PEOPLE.IMDBID is
'imdbID';

-- ============================================================
--   Table : PROFIL                                        
-- ============================================================
create table PROFIL
(
    PRO_ID      	 BIGINT      	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_PROFIL primary key (PRO_ID)
);

comment on column PROFIL.PRO_ID is
'PRO_ID';

comment on column PROFIL.LABEL is
'Label';

-- ============================================================
--   Table : SECURITY_ROLE                                        
-- ============================================================
create table SECURITY_ROLE
(
    SRO_CD      	 VARCHAR(30) 	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_SECURITY_ROLE primary key (SRO_CD)
);

comment on column SECURITY_ROLE.SRO_CD is
'SRO_CD';

comment on column SECURITY_ROLE.LABEL is
'Label';

-- ============================================================
--   Table : USER_AUTHENTIFICATION                                        
-- ============================================================
create table USER_AUTHENTIFICATION
(
    AUTH_ID     	 BIGINT      	not null,
    LOGIN       	 VARCHAR(50) 	,
    PASSWORD    	 VARCHAR(250)	,
    USR_ID      	 BIGINT      	not null,
    constraint PK_USER_AUTHENTIFICATION primary key (AUTH_ID)
);

comment on column USER_AUTHENTIFICATION.AUTH_ID is
'AUTH_ID';

comment on column USER_AUTHENTIFICATION.LOGIN is
'Login';

comment on column USER_AUTHENTIFICATION.PASSWORD is
'Password';

comment on column USER_AUTHENTIFICATION.USR_ID is
'Application user';


alter table USER_AUTHENTIFICATION
	add constraint FK__A_AUTH_USR_APPLICATION_USER foreign key (USR_ID)
	references APPLICATION_USER (USR_ID);

create index _A_AUTH_USR_APPLICATION_USER_FK on USER_AUTHENTIFICATION (USR_ID asc);

alter table CASTING
	add constraint FK__A_CAST_MOV_MOVIE foreign key (MOV_ID)
	references MOVIE (MOV_ID);

create index _A_CAST_MOV_MOVIE_FK on CASTING (MOV_ID asc);

alter table CASTING
	add constraint FK__A_CAST_PEO_PEOPLE foreign key (PEO_ID)
	references PEOPLE (PEO_ID);

create index _A_CAST_PEO_PEOPLE_FK on CASTING (PEO_ID asc);

alter table APPLICATION_USER
	add constraint FK__A_USR_PRO_PROFIL foreign key (PRO_ID)
	references PROFIL (PRO_ID);

create index _A_USR_PRO_PROFIL_FK on APPLICATION_USER (PRO_ID asc);


create table ProSro
(
	PRO_ID      	 BIGINT      	 not null,
	SRO_CD      	 VARCHAR(30) 	 not null,
	constraint PK_ProSro primary key (PRO_ID, SRO_CD),
	constraint FK_NN_PRO_SRO_PROFIL 
		foreign key(PRO_ID)
		references PROFIL (PRO_ID),
	constraint FK_NN_PRO_SRO_SECURITY_ROLE 
		foreign key(SRO_CD)
		references SECURITY_ROLE (SRO_CD)
);

create index NN_PRO_SRO_PROFIL_FK on ProSro (PRO_ID asc);

create index NN_PRO_SRO_SECURITY_ROLE_FK on ProSro (SRO_CD asc);

