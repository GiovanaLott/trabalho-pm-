package com.hospital.repositorios;

import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    Optional<Quarto> findByNumeroIdentificacao(String numeroIdentificacao);

    boolean existsByNumeroIdentificacao(String numeroIdentificacao);

    /** Usado na atualização: existe outro quarto (id diferente) com este número? */
    boolean existsByNumeroIdentificacaoAndIdNot(String numeroIdentificacao, Long id);

    List<Quarto> findBySituacao(StatusQuarto situacao);

    List<Quarto> findAllByOrderByAndarAscNumeroIdentificacaoAsc();

}
