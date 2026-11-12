# CTH-17 - Situação do solicitante após conferência manual

| Campo | Valor |
|---|---|
| **Prioridade** | Fora do MVP por ora (a Mútua quer só o MVP) |
| **Requisitos** | RF18 · RN01 · RN02 |
| **Depende de** | CTH-13 |
| **Relacionada a** | CTH-23 (integração futura com a base Mútua/Crea) |

## Objetivo

Permitir que a equipe registre o resultado da conferência manual do cadastro do solicitante (mutualista e registro no Crea), já que não há validação automática na fase inicial.

## Quem usa

| Perfil | O que faz |
|---|---|
| Equipe administrativa da Mútua-PI | Confere cadastros e marca a situação |
| Administrador / operador | Consulta a situação nas reservas |

## Indicadores de sucesso (proposta da equipe)

- Percentual de reservas com situação do solicitante conferida.

## História

**Como** equipe da Mútua-PI, **quero** marcar a situação do solicitante após conferir manualmente, **para** saber quem precisa de contato e manter o histórico.

## Critérios de aceite

- [ ] A equipe marca a situação do solicitante como: **mutualista em dia**, **pendente**, **só Crea** ou **externo**.
- [ ] A situação aparece no **painel de reservas** (CTH-13).
- [ ] Solicitantes **pendentes** ficam destacados para a equipe **entrar em contato** e regularizar.
- [ ] **Nenhuma reserva é negada automaticamente** por causa da situação (RN02).
- [ ] A marcação fica **registrada**, com data e quem a fez.
- [ ] O acesso à situação é **restrito à equipe** (CTH-12).
