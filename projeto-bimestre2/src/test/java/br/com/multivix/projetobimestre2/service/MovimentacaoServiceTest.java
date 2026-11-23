package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.*;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import br.com.multivix.projetobimestre2.repository.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Equipamento equipamentoDisponivel;
    private Responsavel responsavelSetor;

    @BeforeEach
    void setUp() {
        equipamentoDisponivel = new Equipamento();
        equipamentoDisponivel.setId(1L);
        equipamentoDisponivel.setPatrimonio("PAT-2026-0010");
        equipamentoDisponivel.setNome("Notebook Técnico Dell");
        equipamentoDisponivel.setSituacao(StatusEquipamento.DISPONIVEL);

        Setor setor = new Setor("CTI", "Coordenação de TI", "Bloco A", "2100");
        setor.setId(1L);

        responsavelSetor = new Responsavel("Ana Paula", "20261", "Analista", "ana@orgao.gov.br", setor);
        responsavelSetor.setId(1L);
    }

    @Test
    @DisplayName("Deve registrar cautela com sucesso e alterar status do equipamento para EMPRESTADO")
    void deveRegistrarEmprestimoComSucesso() {
        Movimentacao nova = new Movimentacao();
        nova.setEquipamento(equipamentoDisponivel);
        nova.setResponsavel(responsavelSetor);
        nova.setDataRetirada(LocalDate.of(2026, 11, 20));
        nova.setDataPrevistaDevolucao(LocalDate.of(2026, 11, 27));

        when(equipamentoRepository.findById(1L)).thenReturn(Optional.of(equipamentoDisponivel));
        when(movimentacaoRepository.existsByEquipamentoIdAndDataDevolucaoIsNull(1L)).thenReturn(false);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> {
            Movimentacao m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(15L);
            }
            return m;
        });

        Movimentacao resultado = movimentacaoService.registrarEmprestimo(nova);

        assertNotNull(resultado);
        assertEquals(StatusEquipamento.EMPRESTADO, equipamentoDisponivel.getSituacao());
        assertEquals("TC-2026-0015", resultado.getNumeroTermo());
        verify(equipamentoRepository, times(1)).save(equipamentoDisponivel);
    }

    @Test
    @DisplayName("Deve bloquear cautela quando o equipamento já estiver EMPRESTADO")
    void deveBloquearEmprestimoDeEquipamentoJaEmprestado() {
        equipamentoDisponivel.setSituacao(StatusEquipamento.EMPRESTADO);

        Movimentacao nova = new Movimentacao();
        nova.setEquipamento(equipamentoDisponivel);
        nova.setResponsavel(responsavelSetor);
        nova.setDataRetirada(LocalDate.now());
        nova.setDataPrevistaDevolucao(LocalDate.now().plusDays(3));

        when(equipamentoRepository.findById(1L)).thenReturn(Optional.of(equipamentoDisponivel));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> movimentacaoService.registrarEmprestimo(nova));

        assertTrue(ex.getMessage().contains("não está disponível"));
        verify(movimentacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar devolução e retornar status do equipamento para DISPONIVEL")
    void deveRegistrarDevolucaoComSucesso() {
        equipamentoDisponivel.setSituacao(StatusEquipamento.EMPRESTADO);

        Movimentacao ativa = new Movimentacao();
        ativa.setId(5L);
        ativa.setNumeroTermo("TC-2026-0005");
        ativa.setEquipamento(equipamentoDisponivel);
        ativa.setResponsavel(responsavelSetor);
        ativa.setDataRetirada(LocalDate.of(2026, 11, 10));
        ativa.setDataPrevistaDevolucao(LocalDate.of(2026, 11, 17));

        when(movimentacaoRepository.findById(5L)).thenReturn(Optional.of(ativa));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimentacao devolvida = movimentacaoService.registrarDevolucao(5L, LocalDate.of(2026, 11, 16), "Sem avarias");

        assertNotNull(devolvida.getDataDevolucao());
        assertEquals(StatusEquipamento.DISPONIVEL, equipamentoDisponivel.getSituacao());
        verify(equipamentoRepository, times(1)).save(equipamentoDisponivel);
    }
}
