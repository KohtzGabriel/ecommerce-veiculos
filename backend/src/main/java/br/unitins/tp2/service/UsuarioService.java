package br.unitins.tp2.service;

import br.unitins.tp2.dto.UsuarioRequestDTO;
import br.unitins.tp2.dto.UsuarioUpdateDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UsuarioService {

    Usuario create(@NotNull @Valid UsuarioRequestDTO dto);

    UsuarioResponseDTO update(long id, @NotNull @Valid UsuarioUpdateDTO dto);

    void delete(long id);

    UsuarioResponseDTO findById(long id);

    UsuarioResponseDTO findByCpf(String cpf);

    PageResponse<UsuarioResponseDTO> findAll(int page, int pageSize);

    PageResponse<UsuarioResponseDTO> findByNome(String nome, int page, int pageSize);

    void validarAlteracaoEmail(Long pessoaId, String email);
}
