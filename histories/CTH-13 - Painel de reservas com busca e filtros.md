# CTH-13 - Painel de reservas com busca e filtros

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF13 · [Fluxo principal de reserva](../docs/08-fluxo-de-reserva.md) (passo 6) |
| **Depende de** | CTH-12 |

## Objetivo

Dar à equipe uma visão única de todas as reservas, para acompanhar o uso do Hub e agir quando necessário.

## Quem usa

| Perfil | O que faz |
|---|---|
| Administrador / operador | Acompanha e filtra todas as reservas |
| Equipe administrativa da Mútua-PI | Acompanha reservas, confere cadastro e entra em contato |
| Diretoria | Consulta reservas, em especial das salas de reunião |

## Indicadores de sucesso (proposta da equipe)

- Tempo médio para a equipe localizar uma reserva.

## História

**Como** equipe da Mútua-PI, **quero** ver todas as reservas por dia, ambiente e status, com busca e filtros, **para** acompanhar o uso dos espaços e responder rapidamente aos usuários.

## Critérios de aceite

A fila administrativa usa `GET /api/v1/admin/reservations` e exige
`MANAGE_SPACES`. A consulta é paginada e filtra pelo dia de início, ambiente
ou situação. A busca encontra nomes e protocolos. Os detalhes mostram
contatos, motivo e observações. A resposta não inclui CPF nem o token privado
de acompanhamento. Confirmar, recusar e cancelar pelo painel pertence à CTH-14.

- [x] O painel lista **todas as reservas**, com dia, ambiente e status.
- [x] Há **busca** por nome ou protocolo e **filtros** por data, ambiente e status.
- [x] Cada reserva mostra os **contatos do solicitante** e o **motivo** (quando houver).
- [ ] A partir da reserva, a equipe acessa as ações de **confirmar, recusar e cancelar** (CTH-14).
- [x] A equipe pode ver os contatos de WhatsApp e e-mail nos detalhes.
- [x] Reservas **canceladas** continuam consultáveis, com a indicação de cancelamento tardio, quando for o caso.
