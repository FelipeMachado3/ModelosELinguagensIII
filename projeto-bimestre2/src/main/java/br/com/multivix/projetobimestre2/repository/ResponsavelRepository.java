package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {
    List<Responsavel> findAllByOrderBySetorNomeAscNomeAsc();
    List<Responsavel> findBySetorIdOrderByNomeAsc(Long setorId);
}
