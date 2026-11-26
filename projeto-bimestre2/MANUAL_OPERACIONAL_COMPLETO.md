# Manual Operacional Completo — Sistema de Controle Patrimonial e Cautelas

## 1. Visão Geral e Ambiente Localhost
Sistema web desenvolvido em **Java 21 + Spring Boot + Spring Security + Thymeleaf + MySQL**, projetado para execução local (`http://localhost:8081`).

### Como Executar em Localhost
1. Suba o banco de dados MySQL local via Docker Compose dentro da pasta `projeto-bimestre2`:
   ```powershell
   docker compose up -d
   ```
2. Inicie a aplicação Spring Boot:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. Acesse no navegador: **`http://localhost:8081`**

---

## 2. Gestão de Usuários, Permissões e Secretarias
O sistema utiliza autenticação baseada em sessão com criptografia **BCrypt** e controle de acesso por perfis (**RBAC**).

### Contas Pré-Cadastradas (Senha padrão: `123456`)
| Perfil | E-mail de Acesso | Escopo e Permissões |
| :--- | :--- | :--- |
| **Manager (`MANAGER`)** | `manager@patrimonio.local` | Superusuário com acesso irrestrito a todos os módulos, permissões, secretarias e configurações. |
| **Administrador Geral (`ADMIN`)** | `admin@patrimonio.local` | Controle total: gerencia usuários, permissões, secretarias, bens tombados, empréstimos e configurações. |
| **Gestor de Secretaria (`SECRETARIA`)** | `secretaria@patrimonio.local` | Gerencia unidades e servidores da secretaria, registra empréstimos, prorroga prazos e gera relatórios. |
| **Operador de Almoxarifado (`OPERADOR`)** | `operador@patrimonio.local` | Operação diária: registra retiradas e devoluções com vistoria, imprime termos e confere inventário físico. |
| **Consulta / Auditoria (`AUDITOR`)** | `auditor@patrimonio.local` | Modo somente leitura: visualiza indicadores, inventário, cautelas e relatórios setoriais/gerais. |

---

## 3. Funcionalidades por Módulo

### 3.1. Visão Geral (Dashboard)
* Exibe indicadores em tempo real: total de bens tombados, disponíveis, emprestados, cautelas em atraso e itens pendentes de inventário.
* Permite marcar conferência de inventário diretamente pela tela inicial.

### 3.2. Secretarias e Setores (`/setores`)
* Cadastro de **Secretarias Principais** e **Departamentos/Coordenações** vinculados a uma secretaria superior.
* Acesso direto ao relatório setorial e ao cadastro rápido de servidores lotados na unidade.

### 3.3. Servidores Responsáveis (`/responsaveis`)
* Registro dos agentes públicos autorizados a retirar bens e assinar termos de responsabilidade patrimonial, vinculados por matrícula funcional e setor.

### 3.4. Patrimônio e Tombamento (`/equipamentos`)
* Cadastro completo com código patrimonial único (`PAT-AAAA-NNNN`), marca, modelo, número de série, categoria e estado de conservação.
* **Atalho de Empréstimo Direto:** Bens *Disponíveis* exibem o botão **Emprestar** na tabela, preenchendo automaticamente o formulário de saída.
* **Conferência Rápida de Inventário:** Botão de checklist na tabela que alterna o status *Conferido / Pendente* com 1 clique.

### 3.5. Empréstimos, Prorrogação e Devolução (`/movimentacoes`)
* **Trava de Segurança:** Bens já emprestados ou em manutenção não podem ser retirados novamente até a devolução.
* **Prorrogação de Prazo:** Permite estender a data prevista de devolução de uma cautela ativa mediante justificativa registrada no histórico.
* **Devolução com Vistoria:** Ao dar baixa no almoxarifado, o operador registra o estado de conservação atual; caso seja marcado como *Ruim* ou *Péssimo*, o equipamento passa automaticamente para o status *Manutenção*.

### 3.6. Termo Oficial de Cautela (`/movimentacoes/termo/{id}`)
* Gera documento padronizado (`TC-AAAA-NNNN`) com dados técnicos do bem, identificação do servidor/secretaria, prazos e campos de assinatura prontos para impressão ou PDF.

### 3.7. Relatórios e Alertas (`/relatorios`)
* **Sino de Alertas:** Destaca automaticamente na barra superior todas as devoluções vencidas.
* **Relatório Setorial e Geral:** Gera o extrato detalhado por secretaria/setor ou o inventário geral consolidado pronto para exportação em PDF.
