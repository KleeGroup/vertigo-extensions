-- ============================================================
--   SGBD      		  :  H2                     
-- ============================================================




-- ============================================================
--   Sequences                                      
-- ============================================================
create sequence SEQ_O_ACTIVITY
	start with 1000 cache 20; 

create sequence SEQ_O_ACTIVITY_EXECUTION
	start with 1000 cache 20; 

create sequence SEQ_O_ACTIVITY_LOG
	start with 1000 cache 20; 

create sequence SEQ_O_ACTIVITY_WORKSPACE
	start with 1000 cache 20; 


create sequence SEQ_O_NODE
	start with 1000 cache 20; 

create sequence SEQ_O_PROCESS
	start with 1000 cache 20; 

create sequence SEQ_O_PROCESS_EXECUTION
	start with 1000 cache 20; 

create sequence SEQ_O_PROCESS_PLANIFICATION
	start with 1000 cache 20; 



create sequence SEQ_O_USER
	start with 1000 cache 20; 



-- ============================================================
--   Table : O_ACTIVITY                                        
-- ============================================================
create table O_ACTIVITY
(
    ACT_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	,
    LABEL       	 VARCHAR(100)	,
    NUMBER      	 NUMERIC     	,
    MILESTONE   	 BOOL        	,
    ENGINE      	 VARCHAR(200)	,
    PRO_ID      	 NUMERIC     	not null,
    constraint PK_O_ACTIVITY primary key (ACT_ID)
);

comment on column O_ACTIVITY.ACT_ID is
'Id Activité';

comment on column O_ACTIVITY.NAME is
'Nom de l''activité';

comment on column O_ACTIVITY.LABEL is
'Libellé de l''activité';

comment on column O_ACTIVITY.NUMBER is
'Numéro de l''activité';

comment on column O_ACTIVITY.MILESTONE is
'Jalon';

comment on column O_ACTIVITY.ENGINE is
'Implémentation de l''activité';

comment on column O_ACTIVITY.PRO_ID is
'Processus';

-- ============================================================
--   Table : O_ACTIVITY_EXECUTION                                        
-- ============================================================
create table O_ACTIVITY_EXECUTION
(
    ACE_ID      	 NUMERIC     	not null,
    CREATION_TIME	 TIMESTAMP   	not null,
    BEGIN_TIME  	 TIMESTAMP   	,
    END_TIME    	 TIMESTAMP   	,
    ENGINE      	 VARCHAR(200)	,
    TOKEN       	 VARCHAR(100)	,
    ACT_ID      	 NUMERIC     	not null,
    PRE_ID      	 NUMERIC     	not null,
    NOD_ID      	 NUMERIC     	not null,
    EST_CD      	 VARCHAR(20) 	not null,
    constraint PK_O_ACTIVITY_EXECUTION primary key (ACE_ID)
);

comment on column O_ACTIVITY_EXECUTION.ACE_ID is
'Id de l''execution d''un processus';

comment on column O_ACTIVITY_EXECUTION.CREATION_TIME is
'Date de création';

comment on column O_ACTIVITY_EXECUTION.BEGIN_TIME is
'Date de début';

comment on column O_ACTIVITY_EXECUTION.END_TIME is
'Date de fin';

comment on column O_ACTIVITY_EXECUTION.ENGINE is
'Implémentation effective de l''execution';

comment on column O_ACTIVITY_EXECUTION.TOKEN is
'Token d''identification';

comment on column O_ACTIVITY_EXECUTION.ACT_ID is
'Activity';

comment on column O_ACTIVITY_EXECUTION.PRE_ID is
'Processus';

comment on column O_ACTIVITY_EXECUTION.NOD_ID is
'Node';

comment on column O_ACTIVITY_EXECUTION.EST_CD is
'ExecutionState';

-- ============================================================
--   Table : O_ACTIVITY_LOG                                        
-- ============================================================
create table O_ACTIVITY_LOG
(
    ACL_ID      	 NUMERIC     	not null,
    LOG         	 TEXT        	,
    ATTACHMENT  	 TEXT        	,
    ACE_ID      	 NUMERIC     	not null,
    constraint PK_O_ACTIVITY_LOG primary key (ACL_ID)
);

comment on column O_ACTIVITY_LOG.ACL_ID is
'Id du log';

comment on column O_ACTIVITY_LOG.LOG is
'Contenu du log';

comment on column O_ACTIVITY_LOG.ATTACHMENT is
'Fichier joint';

comment on column O_ACTIVITY_LOG.ACE_ID is
'ActivityExecution';

-- ============================================================
--   Table : O_ACTIVITY_WORKSPACE                                        
-- ============================================================
create table O_ACTIVITY_WORKSPACE
(
    ACW_ID      	 NUMERIC     	not null,
    IS_IN       	 BOOL        	not null,
    WORKSPACE   	 TEXT        	,
    ACE_ID      	 NUMERIC     	not null,
    constraint PK_O_ACTIVITY_WORKSPACE primary key (ACW_ID)
);

comment on column O_ACTIVITY_WORKSPACE.ACW_ID is
'Id de l''execution d''un processus';

comment on column O_ACTIVITY_WORKSPACE.IS_IN is
'Workspace in/out';

comment on column O_ACTIVITY_WORKSPACE.WORKSPACE is
'Contenu du workspace';

comment on column O_ACTIVITY_WORKSPACE.ACE_ID is
'ActivityExecution';

-- ============================================================
--   Table : O_EXECUTION_STATE                                        
-- ============================================================
create table O_EXECUTION_STATE
(
    EST_CD      	 VARCHAR(20) 	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_O_EXECUTION_STATE primary key (EST_CD)
);

comment on column O_EXECUTION_STATE.EST_CD is
'Code';

comment on column O_EXECUTION_STATE.LABEL is
'Libellé';

-- ============================================================
--   Table : O_NODE                                        
-- ============================================================
create table O_NODE
(
    NOD_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	not null,
    HEARTBEAT   	 TIMESTAMP   	,
    constraint PK_O_NODE primary key (NOD_ID)
);

comment on column O_NODE.NOD_ID is
'Id du noeud';

comment on column O_NODE.NAME is
'Nom du noeud';

comment on column O_NODE.HEARTBEAT is
'Date de dernière activité';

-- ============================================================
--   Table : O_PROCESS                                        
-- ============================================================
create table O_PROCESS
(
    PRO_ID      	 NUMERIC     	not null,
    NAME        	 VARCHAR(100)	,
    LABEL       	 VARCHAR(100)	,
    CRON_EXPRESSION	 VARCHAR(100)	,
    INITIAL_PARAMS	 TEXT        	,
    MULTIEXECUTION	 BOOL        	,
    ACTIVE_VERSION	 BOOL        	not null,
    ACTIVE      	 BOOL        	not null,
    RESCUE_PERIOD	 NUMERIC     	not null,
    METADATAS   	 TEXT        	,
    NEED_UPDATE 	 BOOL        	not null,
    TRT_CD      	 VARCHAR(20) 	not null,
    PRT_CD      	 VARCHAR(20) 	,
    constraint PK_O_PROCESS primary key (PRO_ID)
);

comment on column O_PROCESS.PRO_ID is
'Id de la definition du processus';

comment on column O_PROCESS.NAME is
'Nom du processus';

comment on column O_PROCESS.LABEL is
'Libellé du processus';

comment on column O_PROCESS.CRON_EXPRESSION is
'Expression récurrence du processus';

comment on column O_PROCESS.INITIAL_PARAMS is
'Paramètres initiaux sous forme de JSON';

comment on column O_PROCESS.MULTIEXECUTION is
'Accepte la multi-execution';

comment on column O_PROCESS.ACTIVE_VERSION is
'Version active';

comment on column O_PROCESS.ACTIVE is
'Processus actif';

comment on column O_PROCESS.RESCUE_PERIOD is
'Temps de validité d''une planification';

comment on column O_PROCESS.METADATAS is
'Meta-données du processus';

comment on column O_PROCESS.NEED_UPDATE is
'Doit être mise à jour lors du démarrage';

comment on column O_PROCESS.TRT_CD is
'TriggerType';

comment on column O_PROCESS.PRT_CD is
'ProcessType';

-- ============================================================
--   Table : O_PROCESS_EXECUTION                                        
-- ============================================================
create table O_PROCESS_EXECUTION
(
    PRE_ID      	 NUMERIC     	not null,
    BEGIN_TIME  	 TIMESTAMP   	not null,
    END_TIME    	 TIMESTAMP   	,
    ENGINE      	 VARCHAR(200)	,
    CHECKED     	 BOOL        	,
    CHECKING_DATE	 TIMESTAMP   	,
    CHECKING_COMMENT	 TEXT        	,
    PRO_ID      	 NUMERIC     	not null,
    EST_CD      	 VARCHAR(20) 	,
    USR_ID      	 NUMERIC     	,
    constraint PK_O_PROCESS_EXECUTION primary key (PRE_ID)
);

comment on column O_PROCESS_EXECUTION.PRE_ID is
'Id de l''execution d''un processus';

comment on column O_PROCESS_EXECUTION.BEGIN_TIME is
'Date de début';

comment on column O_PROCESS_EXECUTION.END_TIME is
'Date de fin';

comment on column O_PROCESS_EXECUTION.ENGINE is
'Implémentation effective de l''execution';

comment on column O_PROCESS_EXECUTION.CHECKED is
'Pris en charge';

comment on column O_PROCESS_EXECUTION.CHECKING_DATE is
'Date de prise en charge';

comment on column O_PROCESS_EXECUTION.CHECKING_COMMENT is
'Commentaire';

comment on column O_PROCESS_EXECUTION.PRO_ID is
'Processus';

comment on column O_PROCESS_EXECUTION.EST_CD is
'ExecutionState';

comment on column O_PROCESS_EXECUTION.USR_ID is
'User';

-- ============================================================
--   Table : O_PROCESS_PLANIFICATION                                        
-- ============================================================
create table O_PROCESS_PLANIFICATION
(
    PRP_ID      	 NUMERIC     	not null,
    EXPECTED_TIME	 TIMESTAMP   	,
    INITIAL_PARAMS	 TEXT        	,
    PRO_ID      	 NUMERIC     	not null,
    NOD_ID      	 NUMERIC     	,
    SST_CD      	 VARCHAR(20) 	,
    constraint PK_O_PROCESS_PLANIFICATION primary key (PRP_ID)
);

comment on column O_PROCESS_PLANIFICATION.PRP_ID is
'Id Planification';

comment on column O_PROCESS_PLANIFICATION.EXPECTED_TIME is
'Date d''execution prévue';

comment on column O_PROCESS_PLANIFICATION.INITIAL_PARAMS is
'Paramètres initiaux sous forme de JSON';

comment on column O_PROCESS_PLANIFICATION.PRO_ID is
'Processus';

comment on column O_PROCESS_PLANIFICATION.NOD_ID is
'Node';

comment on column O_PROCESS_PLANIFICATION.SST_CD is
'PlanificationState';

-- ============================================================
--   Table : O_PROCESS_TYPE                                        
-- ============================================================
create table O_PROCESS_TYPE
(
    PRT_CD      	 VARCHAR(20) 	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_O_PROCESS_TYPE primary key (PRT_CD)
);

comment on column O_PROCESS_TYPE.PRT_CD is
'Code';

comment on column O_PROCESS_TYPE.LABEL is
'Libellé';

-- ============================================================
--   Table : O_SCHEDULER_STATE                                        
-- ============================================================
create table O_SCHEDULER_STATE
(
    SST_CD      	 VARCHAR(20) 	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_O_SCHEDULER_STATE primary key (SST_CD)
);

comment on column O_SCHEDULER_STATE.SST_CD is
'Code';

comment on column O_SCHEDULER_STATE.LABEL is
'Libellé';

-- ============================================================
--   Table : O_USER                                        
-- ============================================================
create table O_USER
(
    USR_ID      	 NUMERIC     	not null,
    FIRST_NAME  	 VARCHAR(100)	,
    LAST_NAME   	 VARCHAR(100)	,
    EMAIL       	 VARCHAR(100)	,
    PASSWORD    	 VARCHAR(100)	,
    MAIL_ALERT  	 BOOL        	,
    ACTIVE      	 BOOL        	,
    constraint PK_O_USER primary key (USR_ID)
);

comment on column O_USER.USR_ID is
'Id';

comment on column O_USER.FIRST_NAME is
'Nom';

comment on column O_USER.LAST_NAME is
'Prénom';

comment on column O_USER.EMAIL is
'Email';

comment on column O_USER.PASSWORD is
'Mot de passe';

comment on column O_USER.MAIL_ALERT is
'Alerté en cas d''erreur';

comment on column O_USER.ACTIVE is
'Compte Actif';

-- ============================================================
--   Table : TRIGGER_TYPE                                        
-- ============================================================
create table TRIGGER_TYPE
(
    TRT_CD      	 VARCHAR(20) 	not null,
    LABEL       	 VARCHAR(100)	,
    constraint PK_TRIGGER_TYPE primary key (TRT_CD)
);

comment on column TRIGGER_TYPE.TRT_CD is
'Code';

comment on column TRIGGER_TYPE.LABEL is
'Libellé';


alter table O_ACTIVITY_EXECUTION
	add constraint FK_A_ACE_ACT_O_ACTIVITY foreign key (ACT_ID)
	references O_ACTIVITY (ACT_ID);

create index A_ACE_ACT_O_ACTIVITY_FK on O_ACTIVITY_EXECUTION (ACT_ID asc);

alter table O_ACTIVITY_EXECUTION
	add constraint FK_A_ACE_EST_O_EXECUTION_STATE foreign key (EST_CD)
	references O_EXECUTION_STATE (EST_CD);

create index A_ACE_EST_O_EXECUTION_STATE_FK on O_ACTIVITY_EXECUTION (EST_CD asc);

alter table O_ACTIVITY_EXECUTION
	add constraint FK_A_ACE_NOD_O_NODE foreign key (NOD_ID)
	references O_NODE (NOD_ID);

create index A_ACE_NOD_O_NODE_FK on O_ACTIVITY_EXECUTION (NOD_ID asc);

alter table O_ACTIVITY_EXECUTION
	add constraint FK_A_ACE_PRE_O_PROCESS_EXECUTION foreign key (PRE_ID)
	references O_PROCESS_EXECUTION (PRE_ID);

create index A_ACE_PRE_O_PROCESS_EXECUTION_FK on O_ACTIVITY_EXECUTION (PRE_ID asc);

alter table O_ACTIVITY_LOG
	add constraint FK_A_ACL_ACE_O_ACTIVITY_EXECUTION foreign key (ACE_ID)
	references O_ACTIVITY_EXECUTION (ACE_ID);

create index A_ACL_ACE_O_ACTIVITY_EXECUTION_FK on O_ACTIVITY_LOG (ACE_ID asc);

alter table O_ACTIVITY
	add constraint FK_A_ACT_PRO_O_PROCESS foreign key (PRO_ID)
	references O_PROCESS (PRO_ID);

create index A_ACT_PRO_O_PROCESS_FK on O_ACTIVITY (PRO_ID asc);

alter table O_PROCESS_EXECUTION
	add constraint FK_A_PRE_EST_O_EXECUTION_STATE foreign key (EST_CD)
	references O_EXECUTION_STATE (EST_CD);

create index A_PRE_EST_O_EXECUTION_STATE_FK on O_PROCESS_EXECUTION (EST_CD asc);

alter table O_PROCESS_EXECUTION
	add constraint FK_A_PRE_PRO_O_PROCESS foreign key (PRO_ID)
	references O_PROCESS (PRO_ID);

create index A_PRE_PRO_O_PROCESS_FK on O_PROCESS_EXECUTION (PRO_ID asc);

alter table O_PROCESS_EXECUTION
	add constraint FK_A_PRE_USR_O_USER foreign key (USR_ID)
	references O_USER (USR_ID);

create index A_PRE_USR_O_USER_FK on O_PROCESS_EXECUTION (USR_ID asc);

alter table O_PROCESS
	add constraint FK_A_PRO_PRT_O_PROCESS_TYPE foreign key (PRT_CD)
	references O_PROCESS_TYPE (PRT_CD);

create index A_PRO_PRT_O_PROCESS_TYPE_FK on O_PROCESS (PRT_CD asc);

alter table O_PROCESS
	add constraint FK_A_PRO_TRT_TRIGGER_TYPE foreign key (TRT_CD)
	references TRIGGER_TYPE (TRT_CD);

create index A_PRO_TRT_TRIGGER_TYPE_FK on O_PROCESS (TRT_CD asc);

alter table O_PROCESS_PLANIFICATION
	add constraint FK_A_PRP_NOD_O_NODE foreign key (NOD_ID)
	references O_NODE (NOD_ID);

create index A_PRP_NOD_O_NODE_FK on O_PROCESS_PLANIFICATION (NOD_ID asc);

alter table O_PROCESS_PLANIFICATION
	add constraint FK_A_PRP_PRO_O_PROCESS foreign key (PRO_ID)
	references O_PROCESS (PRO_ID);

create index A_PRP_PRO_O_PROCESS_FK on O_PROCESS_PLANIFICATION (PRO_ID asc);

alter table O_PROCESS_PLANIFICATION
	add constraint FK_A_PRP_PST_O_SCHEDULER_STATE foreign key (SST_CD)
	references O_SCHEDULER_STATE (SST_CD);

create index A_PRP_PST_O_SCHEDULER_STATE_FK on O_PROCESS_PLANIFICATION (SST_CD asc);

alter table O_ACTIVITY_WORKSPACE
	add constraint FK_A_TKW_TKE_O_ACTIVITY_EXECUTION foreign key (ACE_ID)
	references O_ACTIVITY_EXECUTION (ACE_ID);

create index A_TKW_TKE_O_ACTIVITY_EXECUTION_FK on O_ACTIVITY_WORKSPACE (ACE_ID asc);


