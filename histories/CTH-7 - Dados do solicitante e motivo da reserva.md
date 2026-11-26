# CTH-7 - Dados do solicitante e motivo da reserva

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF07 · RF08 · RN01 · RN02 · RN06 · RN07 · [Perfis de usuário](../docs/04-perfis-de-usuario.md) |
| **Depende de** | — (substitui dependência de CTH-19, fora do escopo atual) |

## Objetivo

**Decisão do beta:** acesso aberto, sem conta ou verificação de vínculo Crea/Mútua.
O CPF será usado para identificar o solicitante nos dados do pedido, junto de nome e
contatos. A coleta será implementada na entrega da solicitação; a verificação de
vínculo ficará para uma etapa futura. Essa decisão prevalece sobre as referências
abaixo ao ID/cartão da Mútua.

A Mútua verifica presencialmente a identidade do visitante na portaria, na chegada.
O CPF informado no pedido serve para identificar o solicitante no registro. A conferência
presencial da identidade é separada da análise do pedido e da verificação de vínculo.

Permitir que o solicitante informe seus dados na própria solicitação, sem criar conta nesta fase, incluindo o ID/cartãozinho da Mútua quando aplicável e o motivo quando exigido.

## Quem usa

| Perfil | Como se identifica |
|---|---|
| Mutualista / profissional do Crea | Nome completo, identificador/cartãozinho da Mútua (ou RNP), WhatsApp e e-mail |
| Público externo | Nome completo, WhatsApp, e-mail e motivo da reserva (obrigatório) |

## Indicadores de sucesso (proposta da equipe)

- Percentual de reservas concluídas com dados completos, sem retrabalho da equipe.

## História

**Como** solicitante, **quero** informar meus dados e o motivo quando necessário, **para** solicitar uma reserva e permitir contato da equipe sem criar conta nesta fase.

## Critérios de aceite

- [ ] O solicitante **não precisa criar conta nem fazer login** nesta fase; informa os dados na própria solicitação.
- [ ] Os dados do solicitante são independentes das contas de acesso administrativo e podem se repetir entre solicitações.
- [ ] O acesso futuro de solicitantes deverá considerar a integração com Crea/Mútua; vínculo e benefícios são separados das roles `ADMIN` e `USER`.
- [ ] O formulário coleta: **perfil**, **nome completo**, **ID/cartãozinho da Mútua** (quando aplicável), **WhatsApp**, **e-mail** e **observação opcional**.
- [ ] Não há lista nem autenticação de instituições parceiras externas (o software é exclusivo da Mútua).
- [ ] O **motivo da reserva é obrigatório** para o **público externo** e para **reservas em nome de terceiros** (ex.: reunião de trabalho com clientes).
- [ ] O motivo fica visível à equipe, que **analisa a solicitação antes de confirmar** (CTH-14).
- [ ] Reservar em nome próprio para uso de terceiros é **permitido**, desde que o motivo seja informado.
- [ ] **Não há validação automática** do cadastro: a conferência de mutualista é manual, feita pela equipe (CTH-17 está fora do MVP).
- [ ] O sistema **não nega** a reserva por atraso do mutualista; a equipe entra em contato para regularizar.
- [ ] Só são coletados os **dados necessários**, com a finalidade informada (CTH-22).
