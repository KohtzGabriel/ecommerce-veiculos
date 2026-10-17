package br.unitins.tp2.service;

import br.unitins.tp2.dto.NotaFiscalRequest;
import br.unitins.tp2.model.NotaFiscal;

public interface NotaFiscalService {
    NotaFiscal criar(NotaFiscalRequest request);
    NotaFiscal buscar(long id);
    void excluir(long id);
}
