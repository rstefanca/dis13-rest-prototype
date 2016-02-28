CREATE TABLE "public"."hlaseni13"
(
   id bigserial NOT NULL PRIMARY KEY,
   verze INTEGER NOT NULL,
   typ VARCHAR(11) NOT NULL,
   distributor VARCHAR(11) NOT NULL,
   rok smallint NOT NULL,
   mesic smallint NOT NULL,
   typ_odberatele VARCHAR(100),
   kod_sukl VARCHAR(7),
   mnozstvi integer,
   sarze VARCHAR(20)
)