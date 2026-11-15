# Crea Tech Hub — Sistema de Reservas

Sistema web para que profissionais, instituições e público externo reservem os ambientes e equipamentos do Crea Tech Hub, e para que a equipe da Mútua-PI acompanhe e controle essas reservas em um painel administrativo.

Projeto do curso de Engenharia de Software do iCEV (Laboratório de Inovação), em parceria com a Mútua-PI.

> **Status:** esta documentação parte da Análise de Requisitos **v1.0 (04/11/2026)**, um rascunho para validação com o cliente. O documento precisa ser validado e assinado pela Mútua-PI antes do início da implementação.

## Desenvolvimento

O backend está em [`backend/`](backend/README.md), com Java 25, Spring Boot e Maven Wrapper.
As convenções de código e as decisões atuais estão em [`AGENTS.md`](AGENTS.md).
Consulte o README dessa pasta para executar e testar a fundação inicial.
O fluxo de branches e a CI estão descritos em [CI/CD do backend](docs/12-ci-cd.md).
O banco local e a primeira migração estão descritos em [Persistência](docs/13-persistencia.md).
O tratamento centralizado de exceções está descrito em [Erros da API](docs/14-erros-da-api.md).
O login exclusivo da administração está descrito em [Acesso administrativo](docs/15-autenticacao.md).
O cadastro administrativo básico de espaços está em [Espaços](docs/16-espacos.md).
A gestão da equipe e a recuperação estão em [Permissões administrativas](docs/17-equipe-permissoes-recuperacao.md).
As histórias em [`histories/`](histories/PLANO-DE-IMPLEMENTACAO.md) registram as decisões
mais recentes de escopo; a documentação abaixo preserva o levantamento original.

## Documentação

| # | Documento | Conteúdo |
|---|-----------|----------|
| 1 | [Contexto e objetivo](docs/01-contexto-e-objetivo.md) | Motivação, objetivo e partes interessadas |
| 2 | [Escopo](docs/02-escopo.md) | O que entra na 1ª entrega e o que fica para fases futuras |
| 3 | [Ambientes e recursos](docs/03-ambientes-e-recursos.md) | Inventário do que pode ser reservado |
| 4 | [Perfis de usuário](docs/04-perfis-de-usuario.md) | Quem usa o sistema e o que cada perfil pode fazer |
| 5 | [Requisitos funcionais](docs/05-requisitos-funcionais.md) | RF01 a RF23, com prioridade |
| 6 | [Regras de negócio](docs/06-regras-de-negocio.md) | RN01 a RN09, com origem |
| 7 | [Requisitos não funcionais](docs/07-requisitos-nao-funcionais.md) | RNF01 a RNF08 |
| 8 | [Fluxo principal de reserva](docs/08-fluxo-de-reserva.md) | Jornada de ponta a ponta |
| 9 | [Design](docs/09-design.md) | Referência ao projeto oficial de UX/UI no Framer |
| 10 | [Pendências](docs/10-pendencias.md) | Pontos a validar com a Mútua-PI e dúvidas da equipe |
| 11 | [Glossário](docs/11-glossario.md) | Termos do domínio |

## Como ler

- Os requisitos vêm da entrevista gravada durante a visita às instalações da Mútua-PI, transcrita para ata.
- Onde o cliente não fechou uma regra, o item aparece como **proposta da equipe** ou fica listado nas [pendências](docs/10-pendencias.md).
- **Prioridades:** Alta = indispensável na 1ª entrega · Média = desejável na 1ª entrega · Futura = fases seguintes.
- **Design:** o projeto no Framer é a referência oficial de UX/UI. O protótipo incluído no PDF da análise não é usado.

## Equipe e parceria

- **Cliente:** Mútua – Caixa de Assistência dos Profissionais do Crea (Piauí)
- **Parceria:** termo de cooperação Mútua-PI × iCEV (curso de Engenharia de Software)
- **Desenvolvimento:** alunos de Engenharia de Software do iCEV, com orientação do Prof. Joselito e coordenação do curso
