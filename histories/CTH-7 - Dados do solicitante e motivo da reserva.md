# CTH-7 - Dados do solicitante e motivo da reserva

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF07 · RF08 · RN01 · RN02 · RN06 · RN07 · [Perfis de usuário](../docs/04-perfis-de-usuario.md) |
| **Depende de** | — (substitui dependência de CTH-19, fora do escopo atual) |

## Objetivo

Permitir que o solicitante se cadastre e faça login no site, informando seus dados (incluindo o ID/cartãozinho da Mútua, quando aplicável) e o motivo quando exigido.

## Quem usa

| Perfil | Como se identifica |
|---|---|
| Mutualista / profissional do Crea | Cadastro/login no site + nome completo, identificador/cartãozinho da Mútua (ou RNP), WhatsApp e e-mail |
| Público externo | Cadastro/login no site + nome completo, WhatsApp, e-mail e motivo da reserva (obrigatório) |

## Indicadores de sucesso (proposta da equipe)

- Percentual de reservas concluídas com dados completos, sem retrabalho da equipe.

## História

**Como** solicitante, **quero** me cadastrar/logar no site e informar meus dados e o motivo da reserva quando necessário, **para** acompanhar e gerenciar minhas reservas e permitir contato da equipe.

## Critérios de aceite

- [ ] O usuário **se cadastra e faz login** no site (duplicação de dados cadastrais é permitida se ocorrer).
- [ ] O formulário coleta: **perfil**, **nome completo**, **ID/cartãozinho da Mútua** (quando aplicável), **WhatsApp**, **e-mail** e **observação opcional**.
- [ ] Não há lista nem autenticação de instituições parceiras externas (o software é exclusivo da Mútua).
- [ ] O **motivo da reserva é obrigatório** para o **público externo** e para **reservas em nome de terceiros** (ex.: reunião de trabalho com clientes).
- [ ] O motivo fica visível à equipe, que **analisa a solicitação antes de confirmar** (CTH-14).
- [ ] Reservar em nome próprio para uso de terceiros é **permitido**, desde que o motivo seja informado.
- [ ] **Não há validação automática** do cadastro: a conferência de mutualista é manual, feita pela equipe (CTH-17 está fora do MVP).
- [ ] O sistema **não nega** a reserva por atraso do mutualista; a equipe entra em contato para regularizar.
- [ ] Só são coletados os **dados necessários**, com a finalidade informada (CTH-22).
