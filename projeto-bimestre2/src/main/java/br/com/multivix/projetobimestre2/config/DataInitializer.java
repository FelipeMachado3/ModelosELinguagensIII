package br.com.multivix.projetobimestre2.config;

import br.com.multivix.projetobimestre2.entity.*;
import br.com.multivix.projetobimestre2.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final SetorRepository setorRepository;
    private final ResponsavelRepository responsavelRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoriaRepository categoriaRepository,
                           SetorRepository setorRepository,
                           ResponsavelRepository responsavelRepository,
                           EquipamentoRepository equipamentoRepository,
                           MovimentacaoRepository movimentacaoRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.setorRepository = setorRepository;
        this.responsavelRepository = responsavelRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            Categoria catInformatica = categoriaRepository.save(
                    new Categoria("Informática", "Notebooks, desktops, monitores e periféricos"));
            Categoria catAudiovisual = categoriaRepository.save(
                    new Categoria("Audiovisual", "Projetores multimídia, caixas de som, microfones e webcams"));
            Categoria catDispositivo = categoriaRepository.save(
                    new Categoria("Dispositivo móvel", "Tablets institucionais, coletores de dados e smartphones"));
            Categoria catImpressao = categoriaRepository.save(
                    new Categoria("Impressão", "Impressoras laser, multifuncionais e scanners"));

            Setor setorAdm = setorRepository.save(
                    new Setor("SECADM", "Secretaria de Administração", "Bloco A - 2º Andar", "2101"));
            Setor setorGab = setorRepository.save(
                    new Setor("GAB", "Gabinete do Prefeito", "Palácio Sede - 3º Andar", "2001"));
            Setor setorEdu = setorRepository.save(
                    new Setor("SEMED", "Secretaria de Educação", "Anexo II - Térreo", "2310"));
            Setor setorFin = new Setor("COFIN", "Coordenação de Finanças", "Bloco B - 1º Andar", "2205");
            setorFin.setTipoUnidade("DEPARTAMENTO");
            setorFin.setSecretariaPai(setorAdm);
            setorFin = setorRepository.save(setorFin);

            Responsavel resp1 = responsavelRepository.save(
                    new Responsavel("Carlos Henrique Almeida", "10458-2", "Analista de Sistemas", "carlos.adm@orgao.gov.br", setorAdm));
            Responsavel resp2 = responsavelRepository.save(
                    new Responsavel("Roberto Mendes Silva", "10211-5", "Chefe de Gabinete", "roberto.gab@orgao.gov.br", setorGab));
            Responsavel resp3 = responsavelRepository.save(
                    new Responsavel("Mariana Souza Lima", "10892-0", "Coordenadora Pedagógica", "mariana.edu@orgao.gov.br", setorEdu));
            Responsavel resp4 = responsavelRepository.save(
                    new Responsavel("Fernanda Costa Ribeiro", "11034-8", "Contadora Pública", "fernanda.fin@orgao.gov.br", setorFin));

            setorAdm.setResponsavelTitularId(resp1.getId());
            setorAdm.setDataTransferenciaResponsavel(LocalDate.now());
            setorRepository.save(setorAdm);

            Equipamento eq1 = new Equipamento();
            eq1.setPatrimonio("PAT-2026-0189");
            eq1.setNome("Notebook Dell Latitude 5440");
            eq1.setMarca("Dell");
            eq1.setModelo("Latitude 5440");
            eq1.setNumeroSerie("BR-DL-99812A");
            eq1.setDataAquisicao(LocalDate.of(2025, 3, 15));
            eq1.setEstadoConservacao(EstadoConservacao.NOVO);
            eq1.setSituacao(StatusEquipamento.EMPRESTADO);
            eq1.setConferidoInventario(true);
            eq1.setCategoria(catInformatica);
            eq1.setSetor(setorAdm);
            equipamentoRepository.save(eq1);

            Equipamento eq2 = new Equipamento();
            eq2.setPatrimonio("PAT-2026-0721");
            eq2.setNome("Projetor Epson PowerLite E20");
            eq2.setMarca("Epson");
            eq2.setModelo("PowerLite E20");
            eq2.setNumeroSerie("EP-445109X");
            eq2.setDataAquisicao(LocalDate.of(2024, 8, 10));
            eq2.setEstadoConservacao(EstadoConservacao.BOM);
            eq2.setSituacao(StatusEquipamento.EMPRESTADO);
            eq2.setConferidoInventario(false);
            eq2.setCategoria(catAudiovisual);
            eq2.setSetor(setorGab);
            equipamentoRepository.save(eq2);

            Equipamento eq3 = new Equipamento();
            eq3.setPatrimonio("PAT-2026-0334");
            eq3.setNome("Tablet Samsung Galaxy Tab A9");
            eq3.setMarca("Samsung");
            eq3.setModelo("Galaxy Tab A9");
            eq3.setNumeroSerie("SM-771203M");
            eq3.setDataAquisicao(LocalDate.of(2025, 1, 20));
            eq3.setEstadoConservacao(EstadoConservacao.BOM);
            eq3.setSituacao(StatusEquipamento.EMPRESTADO);
            eq3.setConferidoInventario(false);
            eq3.setCategoria(catDispositivo);
            eq3.setSetor(setorEdu);
            equipamentoRepository.save(eq3);

            Equipamento eq4 = new Equipamento();
            eq4.setPatrimonio("PAT-2026-0456");
            eq4.setNome("Impressora HP LaserJet Pro");
            eq4.setMarca("HP");
            eq4.setModelo("LaserJet Pro M404dw");
            eq4.setNumeroSerie("HP-889012K");
            eq4.setDataAquisicao(LocalDate.of(2024, 11, 5));
            eq4.setEstadoConservacao(EstadoConservacao.BOM);
            eq4.setSituacao(StatusEquipamento.DISPONIVEL);
            eq4.setConferidoInventario(false);
            eq4.setCategoria(catImpressao);
            eq4.setSetor(setorFin);
            equipamentoRepository.save(eq4);

            Equipamento eq5 = new Equipamento();
            eq5.setPatrimonio("PAT-2026-0108");
            eq5.setNome("Monitor LG 24 pol. Full HD");
            eq5.setMarca("LG");
            eq5.setModelo("24MK430H");
            eq5.setNumeroSerie("LG-551290P");
            eq5.setDataAquisicao(LocalDate.of(2025, 2, 12));
            eq5.setEstadoConservacao(EstadoConservacao.NOVO);
            eq5.setSituacao(StatusEquipamento.DISPONIVEL);
            eq5.setConferidoInventario(true);
            eq5.setCategoria(catInformatica);
            eq5.setSetor(setorAdm);
            equipamentoRepository.save(eq5);

            Movimentacao m1 = new Movimentacao();
            m1.setNumeroTermo("TC-2026-0001");
            m1.setEquipamento(eq2);
            m1.setResponsavel(resp2);
            m1.setDataRetirada(LocalDate.now().minusDays(6));
            m1.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
            m1.setObservacao("Audiência pública no salão nobre.");
            movimentacaoRepository.save(m1);

            Movimentacao m2 = new Movimentacao();
            m2.setNumeroTermo("TC-2026-0002");
            m2.setEquipamento(eq1);
            m2.setResponsavel(resp1);
            m2.setDataRetirada(LocalDate.now().minusDays(2));
            m2.setDataPrevistaDevolucao(LocalDate.now().plusDays(4));
            m2.setObservacao("Treinamento interno de servidores.");
            movimentacaoRepository.save(m2);

            Movimentacao m3 = new Movimentacao();
            m3.setNumeroTermo("TC-2026-0003");
            m3.setEquipamento(eq3);
            m3.setResponsavel(resp3);
            m3.setDataRetirada(LocalDate.now().minusDays(1));
            m3.setDataPrevistaDevolucao(LocalDate.now().plusDays(10));
            m3.setObservacao("Vistoria técnica nas escolas municipais.");
            movimentacaoRepository.save(m3);
        }

        Setor setorAdm = setorRepository.findAll().stream().findFirst().orElse(null);

        if (!usuarioRepository.existsByEmailIgnoreCase("manager@patrimonio.local")) {
            Usuario manager = new Usuario();
            manager.setNome("Gestor Geral (Manager)");
            manager.setEmail("manager@patrimonio.local");
            manager.setSenha(passwordEncoder.encode("123456"));
            manager.setMatricula("MGR-000");
            manager.setPerfil(PerfilAcesso.MANAGER);
            manager.setAtivo(true);
            manager.setPrimeiroAcesso(false);
            usuarioRepository.save(manager);
        }

        usuarioRepository.findByEmailIgnoreCase("admin@patrimonio.local").ifPresentOrElse(
            u -> {
                if ("Mariana Alves".equals(u.getNome())) {
                    u.setNome("Administrador Geral");
                    usuarioRepository.save(u);
                }
            },
            () -> {
                Usuario admin = new Usuario();
                admin.setNome("Administrador Geral");
                admin.setEmail("admin@patrimonio.local");
                admin.setSenha(passwordEncoder.encode("123456"));
                admin.setMatricula("ADM-001");
                admin.setPerfil(PerfilAcesso.ADMIN);
                admin.setAtivo(true);
                admin.setPrimeiroAcesso(false);
                usuarioRepository.save(admin);
            }
        );

        if (!usuarioRepository.existsByEmailIgnoreCase("secretaria@patrimonio.local")) {
            Usuario secretaria = new Usuario();
            secretaria.setNome("Carlos Henrique (Secretaria)");
            secretaria.setEmail("secretaria@patrimonio.local");
            secretaria.setSenha(passwordEncoder.encode("123456"));
            secretaria.setMatricula("SEC-102");
            secretaria.setPerfil(PerfilAcesso.SECRETARIA);
            secretaria.setSetor(setorAdm);
            secretaria.setAtivo(true);
            secretaria.setPrimeiroAcesso(false);
            usuarioRepository.save(secretaria);
        }

        if (!usuarioRepository.existsByEmailIgnoreCase("operador@patrimonio.local")) {
            Usuario operador = new Usuario();
            operador.setNome("Lucas Operador");
            operador.setEmail("operador@patrimonio.local");
            operador.setSenha(passwordEncoder.encode("123456"));
            operador.setMatricula("ALM-205");
            operador.setPerfil(PerfilAcesso.OPERADOR);
            operador.setAtivo(true);
            operador.setPrimeiroAcesso(false);
            usuarioRepository.save(operador);
        }

        if (!usuarioRepository.existsByEmailIgnoreCase("auditor@patrimonio.local")) {
            Usuario auditor = new Usuario();
            auditor.setNome("Fernanda Auditoria");
            auditor.setEmail("auditor@patrimonio.local");
            auditor.setSenha(passwordEncoder.encode("123456"));
            auditor.setMatricula("AUD-309");
            auditor.setPerfil(PerfilAcesso.AUDITOR);
            auditor.setAtivo(true);
            auditor.setPrimeiroAcesso(false);
            usuarioRepository.save(auditor);
        }
    }
}