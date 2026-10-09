package com.hospital.repositorios;

import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    Optional<Quarto> findByNumeroIdentificacao(String numeroIdentificacao);

    boolean existsByNumeroIdentificacao(String numeroIdentificacao);

    List<Quarto> findBySituacao(StatusQuarto situacao);

    List<Quarto> findByAndarOrderByNumeroIdentificacaoAsc(Integer andar);

    /**
     * Quartos que ainda possuem pelo menos um leito livre (ocupacao atual menor que a
     * capacidade maxima), ordenados por andar e numero de identificacao.
     */
    @Query("SELECT q FROM Quarto q WHERE q.ocupacaoAtual < q.capacidadeMaxima ORDER BY q.andar, q.numeroIdentificacao")
    List<Quarto> findComVagaDisponivel();
}
