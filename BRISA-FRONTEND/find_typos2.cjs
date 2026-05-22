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
            if (file.endsWith('.vue')) {
                results.push(file);
            }
        }
    });
    return results;
}

const files = walk('C:\\Users\\ResTIC16\\Documents\\GitHub\\Brisa-ONE\\BRISA-FRONTEND\\src');

const pattern = />([^<]+)</g;
const words = /\b(usuario|usuarios|inscricao|selecao|imersao|acao|acoes|avaliacao|avaliacoes|modulo|modulos|relatorio|relatorios|instituicao|instituicoes|matricula|matriculas|estagio|concluido|comite|lider|orientacao|analise|botao|botoes|opcoes)\b/i;

let totalFound = 0;

files.forEach(file => {
    let content = fs.readFileSync(file, 'utf-8');
    let match;
    while ((match = pattern.exec(content)) !== null) {
        let text = match[1];
        // Ignore scripts and template logic that use > and <
        if (text.includes('{') || text.includes('}') || text.includes('=')) continue;
        
        let wordMatch = text.match(words);
        if (wordMatch) {
            console.log(`File: ${file}`);
            console.log(`  Found unaccented word "${wordMatch[0]}" in text node: "${text.trim()}"`);
            totalFound++;
        }
    }
});

console.log(`\nTotal unaccented words in text nodes: ${totalFound}`);
