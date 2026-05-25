package com.example.brisa.dtos.program;

public record ProgramOverviewItemDTO(
        Long programId,
        Long classId,
        String nome,
        String turma,
        String parceiro,
        String localidade,
        String periodo,
        String executor,
        String status,
        String etapaAtual,
        long inscricao,
        long selecao,
        long inscritos,
        long ativos,
        long nivelamento,
        long imersao,
        long projetos,
        long orientadores,
        String proximoMarco,
        int progresso
) {
}
