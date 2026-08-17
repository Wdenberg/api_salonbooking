-- Necessário para gen_random_uuid() e para a futura exclusion constraint (GiST)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;