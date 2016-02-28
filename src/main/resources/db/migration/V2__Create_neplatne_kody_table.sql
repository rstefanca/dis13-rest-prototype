CREATE TABLE "public"."neplatne_kody"
(
   id bigserial NOT NULL PRIMARY KEY, 
   id_hlaseni BIGINT REFERENCES hlaseni13 (id) ON DELETE CASCADE
)