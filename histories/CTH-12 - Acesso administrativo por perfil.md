# CTH-12 - Acesso administrativo por perfil

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | [Perfis de usuário](../docs/04-perfis-de-usuario.md) · RN05 · RNF05 |
| **Depende de** | — (base para CTH-13 a CTH-20) |

## Objetivo

Restringir o painel administrativo à equipe autorizada, e liberar para cada perfil administrativo apenas o que ele pode fazer.

## Quem usa

| Perfil | O que faz |
|---|---|
| Diretoria | Acesso administrativo: reserva salas de reunião e mantém horários fixos travados |
| Administrador / operador | Acesso administrativo: gerencia ambientes, regras, reservas, bloqueios e usuários |
| Equipe administrativa da Mútua-PI | Confere cadastros e acompanha reservas |

## Indicadores de sucesso (proposta da equipe)

- Nenhum acesso indevido a dados de solicitantes por quem não é da equipe.

## História

**Como** membro da equipe da Mútua-PI, **quero** entrar no painel com o acesso do meu perfil, **para** executar só as tarefas que me cabem e proteger os dados dos solicitantes.

## Critérios de aceite

- [ ] O painel administrativo usa **login padrão (CRUD)** com autenticação por usuário e senha; visitantes e solicitantes **não acessam** a área administrativa.
- [ ] Suporte à **migração da base de usuários/dados** fornecida pela Mútua.
- [ ] A **Diretoria** reserva salas de reunião e mantém os horários fixos de reunião (CTH-16).
- [ ] O **Administrador / operador** gerencia ambientes, regras, reservas, bloqueios e usuários.
- [ ] Uma tela ou ação fora do perfil **não abre nem executa**.
- [ ] O acesso aos dados dos solicitantes fica **restrito à equipe da Mútua-PI**.
