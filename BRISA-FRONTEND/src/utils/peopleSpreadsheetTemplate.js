import * as XLSX from 'xlsx';

export const PEOPLE_SPREADSHEET_COLUMNS = [
  'Nome do aluno',
  'Data de nascimento',
  'Gênero',
  'Cota',
  'CPF',
  'E-mail',
  'Telefone',
  'Estado de residência',
  'Cidade de residência',
  'Tipo de formação',
  'Instituição',
  'Curso',
  'Status da formação',
  'Data de conclusão',
  'Programa ID',
  'Turma ID',
  'Etapa inicial ID',
  'Status inicial'
];

const EXAMPLE_ROW = [
  'Maria Silva',
  '2001-05-20',
  'Feminino',
  'Ampla Concorrência',
  '12345678901',
  'maria.silva@email.com',
  '(82) 99999-9999',
  'AL',
  'Maceió',
  'Engenharia de Computação, Ciência da Computação ou outros cursos relacionados a TIC',
  'UFAL',
  'Ciência da Computação',
  'Cursando',
  '',
  '',
  '',
  '',
  'Inscrito'
];

export function downloadPeopleSpreadsheetTemplate(fileName = 'Modelo_Planilha_Pessoas.xlsx') {
  const worksheet = XLSX.utils.aoa_to_sheet([PEOPLE_SPREADSHEET_COLUMNS, EXAMPLE_ROW]);
  worksheet['!cols'] = PEOPLE_SPREADSHEET_COLUMNS.map((column) => ({
    wch: Math.max(14, Math.min(34, column.length + 6))
  }));

  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Pessoas');
  XLSX.writeFile(workbook, fileName);
}
