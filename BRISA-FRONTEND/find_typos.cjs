const fs = require('fs');
const path = require('path');

function walk(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(file => {
        file = path.join(dir, file);
        const stat = fs.statSync(file);
        if (stat && stat.isDirectory()) {
            results = results.concat(walk(file));
        } else {
            if (file.endsWith('.vue') || file.endsWith('.js')) {
                results.push(file);
            }
        }
    });
    return results;
}

const files = walk('C:\\Users\\ResTIC16\\Documents\\GitHub\\Brisa-ONE\\BRISA-FRONTEND\\src');

const pattern = /\b(Usuario|Usuarios|Inscricao|Selecao|Imersao|Acao|Acoes|Avaliacao|Avaliacoes|Modulo|Modulos|Relatorio|Relatorios|Instituicao|Instituicoes|Matricula|Matriculas|Estagio|Concluido|Comite|Lider|Orientacao|Analitico|Botao|Botoes|Opcoes)\b/g;

let totalFound = 0;

files.forEach(file => {
    let content = fs.readFileSync(file, 'utf-8');
    let match;
    let fileLogged = false;
    while ((match = pattern.exec(content)) !== null) {
        // Simple heuristic: if it's in a comment or looks like a title/label
        // Check surrounding characters
        const start = Math.max(0, match.index - 20);
        const end = Math.min(content.length, match.index + match[0].length + 20);
        const context = content.substring(start, end);
        
        // Ignore if it's clearly inside a variable like `const Inscricao =` or import
        if (context.includes('import ') || context.includes('export ') || context.includes('name:')) continue;
        
        if (!fileLogged) {
            console.log(`\nFile: ${file}`);
            fileLogged = true;
        }
        console.log(`  Found: ${match[0]} -> Context: "${context.trim().replace(/\n/g, ' ')}"`);
        totalFound++;
    }
});

console.log(`\nTotal potentially unaccented capitalized words found: ${totalFound}`);
