CREATE TABLE public.melodies
(
    id_melody serial NOT NULL PRIMARY KEY,
    artist varchar(1024),
    title varchar(2048),
    tags varchar[]
);

CREATE TABLE public.lyrics
(
    id_melody integer NOT NULL PRIMARY KEY,
    text varchar
);