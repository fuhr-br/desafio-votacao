delete from assembleia_pauta;
delete from assembleia;
ALTER TABLE assembleia ALTER COLUMN ID RESTART WITH 1;
delete from pauta_votacao;
delete from voto;
ALTER TABLE voto ALTER COLUMN ID RESTART WITH 1;
delete from associado;
ALTER TABLE associado ALTER COLUMN ID RESTART WITH 1;
delete from pauta;
ALTER TABLE pauta ALTER COLUMN ID RESTART WITH 1;

