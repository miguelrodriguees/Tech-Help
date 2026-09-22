import {readFile,writeFile,mkdir} from 'node:fs/promises';
const sql=await readFile(new URL('../database/migrations/001_aluguel_transporte.sql',import.meta.url),'utf8');
const out=new URL('../backend/target/',import.meta.url);
await mkdir(out,{recursive:true});
await writeFile(new URL('migracao-aluguel-teste.sql',out),'-- Exclusivo de tech_help_teste. Não execute novamente.\nUSE tech_help_teste;\n'+sql);
console.log('Gerado backend/target/migracao-aluguel-teste.sql. Ainda não executado. Não recrie o banco.');
