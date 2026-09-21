// Gera SQL isolado; não conecta ao banco nem executa comandos.
import { readFile, mkdir, writeFile } from 'node:fs/promises';
const source=await readFile(new URL('../database/techhelp_v2_mariadb_limpo.sql',import.meta.url),'utf8');
if(!source.includes('CREATE DATABASE IF NOT EXISTS tech_help')||!source.includes('USE tech_help;'))throw new Error('Cabeçalho do SQL mudou; revise antes de gerar.');
const sql=source.replace('CREATE DATABASE IF NOT EXISTS tech_help','CREATE DATABASE tech_help_teste').replace('USE tech_help;','USE tech_help_teste;');
if(/\btech_help\b/.test(sql))throw new Error('Referência ao banco original encontrada; geração interrompida.');
const directory=new URL('../backend/target/',import.meta.url);
await mkdir(directory,{recursive:true});
const output=new URL('schema-test.sql',directory);
await writeFile(output,'-- Banco exclusivo de testes. Configure o editor para PARAR no primeiro erro.\n-- Se tech_help_teste já existir, NÃO continue nem apague o banco: revise sua origem.\n'+sql);
console.log('SQL gerado em backend/target/schema-test.sql. Ainda não foi executado.');
