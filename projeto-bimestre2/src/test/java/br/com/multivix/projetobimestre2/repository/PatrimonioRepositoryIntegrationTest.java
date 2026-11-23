package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PatrimonioRepositoryIntegrationTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ResponsavelRepository responsavelRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Test
    @DisplayName("Deve persistir e consultar equipamentos, setores e movimentações no repositório JPA")
    void devePersistirEConsultarFluxoPatrimonial() {
        Categoria cat = categoriaRepository.save(new Categoria("Informática Teste", "Equipamentos de TI"));
        Setor setor = setorRepository.save(new Setor("REDES", "Setor de Redes", "Bloco C", "3010"));

        Equipamento eq = new Equipamento();
        eq.setPatrimonio("PAT-TEST-999");
        eq.setNome("Switch 24 Portas");
        eq.setEstadoConservacao(EstadoConservacao.NOVO);
        eq.setSituacao(StatusEquipamento.DISPONIVEL);
        eq.setCategoria(cat);
        eq = equipamentoRepository.save(eq);

        assertTrue(equipamentoRepository.existsByPatrimonioIgnoreCase("pat-test-999"));

        Responsavel resp = responsavelRepository.save(
                new Responsavel("João Silva", "99887", "Técnico de Redes", "joao@orgao.gov.br", setor));

        Movimentacao mov = new Movimentacao();
        mov.setNumeroTermo("TC-TEST-0001");
        mov.setEquipamento(eq);
        mov.setResponsavel(resp);
        mov.setDataRetirada(LocalDate.now());
        mov.setDataPrevistaDevolucao(LocalDate.now().plusDays(5));
        movimentacaoRepository.save(mov);

        List<Movimentacao> ativas = movimentacaoRepository.findByResponsavelSetorIdAndDataDevolucaoIsNullOrderByDataPrevistaDevolucaoAsc(setor.getId());
        assertEquals(1, ativas.size());
        assertEquals("PAT-TEST-999", ativas.get(0).getEquipamento().getPatrimonio());
    }
}
