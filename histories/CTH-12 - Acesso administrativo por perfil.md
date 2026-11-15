# CTH-12 - Acesso administrativo por perfil

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | [Perfis de usuário](../docs/04-perfis-de-usuario.md) · RN05 · RNF05 |
| **Depende de** | — (base para CTH-13 a CTH-20) |

## Objetivo

**Evolução implementada no backend:** cadastro de funcionários por gestor, permissões
independentes da role, promoção/revogação de gestão de acessos, auditoria, troca da
senha inicial e recuperação por e-mail ou procedimento operacional. Superadministrador
continua sendo `ADMIN`, distinguido por `MANAGE_ACCESS`. Ver o
[contrato atualizado](../docs/17-equipe-permissoes-recuperacao.md).

Restringir o painel administrativo às contas `ADMIN` autorizadas. Os solicitantes usam o fluxo público sem conta nesta fase; a role `USER` fica reservada para a evolução com autenticação pelo Crea/Mútua.

## Quem usa

| Perfil | O que faz |
|---|---|
| Equipe autorizada da Mútua-PI (`ADMIN`) | Gerencia ambientes, regras, reservas, bloqueios e contas administrativas |

## Indicadores de sucesso (proposta da equipe)

- Nenhum acesso indevido a dados de solicitantes por quem não é da equipe.

## História

**Como** membro da equipe da Mútua-PI, **quero** entrar no painel com o acesso do meu perfil, **para** executar só as tarefas que me cabem e proteger os dados dos solicitantes.

## Critérios de aceite

- [ ] O painel administrativo usa autenticação por **e-mail único e senha**; somente contas `ADMIN` ativas entram na área administrativa.
- [ ] As roles são `ADMIN` e `USER`; vínculo de mutualista/profissional do Crea e benefícios são tratados separadamente.
- [ ] A primeira conta administrativa exige inicialização explícita e senha definida pelo responsável; não existe senha padrão nem cadastro administrativo público.
- [ ] Suporte à **migração da base de usuários/dados** fornecida pela Mútua.
- [ ] A **Diretoria** reserva salas de reunião e mantém os horários fixos de reunião (CTH-16).
- [ ] O **Administrador / operador** gerencia ambientes, regras, reservas, bloqueios e usuários.
- [ ] Uma tela ou ação fora do perfil **não abre nem executa**.
- [ ] O acesso aos dados dos solicitantes fica **restrito à equipe da Mútua-PI**.
