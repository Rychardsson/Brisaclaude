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
            if (file.endsWith('.java')) {
                results.push(file);
            }
        }
    });
    return results;
}

const files = walk('C:\\Users\\ResTIC16\\Documents\\GitHub\\Brisa-ONE\\BRISA-BACKEND\\src\\main\\java');

const stringPattern = /"([^"\\]*(?:\\.[^"\\]*)*)"/g;
const commentPattern = /(\/\/.*?$|\/\*[\s\S]*?\*\/)/gm;
const words = /\b(usuario|usuarios|inscricao|selecao|imersao|acao|acoes|avaliacao|avaliacoes|modulo|modulos|relatorio|relatorios|instituicao|instituicoes|matricula|matriculas|estagio|concluido|comite|lider|orientacao|analise|botao|botoes|opcoes)\b/i;

let totalFound = 0;

files.forEach(file => {
    let content = fs.readFileSync(file, 'utf-8');
    
    // Check strings
    let match;
    while ((match = stringPattern.exec(content)) !== null) {
        let text = match[1];
        let wordMatch = text.match(words);
        // Ignore if it looks like a variable or URL (no spaces, camelCase, etc)
        if (wordMatch && text.includes(' ')) {
            console.log(`File: ${file}`);
            console.log(`  Found unaccented word "${wordMatch[0]}" in String: "${text.trim()}"`);
            totalFound++;
        }
    }
    
    // Check comments
    while ((match = commentPattern.exec(content)) !== null) {
        let text = match[1];
        let wordMatch = text.match(words);
        if (wordMatch) {
            console.log(`File: ${file}`);
            console.log(`  Found unaccented word "${wordMatch[0]}" in Comment: "${text.trim().replace(/\n/g, ' ')}"`);
            totalFound++;
        }
    }
});

console.log(`\nTotal unaccented words in Java strings/comments: ${totalFound}`);
