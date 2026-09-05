export interface Categoria {
  idCategoria: number;
  nome: string;
  descricao: string | null;
  ativo: boolean;
  dataCadastro: string | null;
}