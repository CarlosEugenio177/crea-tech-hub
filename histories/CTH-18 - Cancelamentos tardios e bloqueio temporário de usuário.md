# CTH-18 - Cancelamentos tardios e bloqueio temporário de usuário

| Campo | Valor |
|---|---|
| **Prioridade** | Fora do MVP por ora (a Mútua quer só o MVP) |
| **Requisitos** | RF19 · RN04 |
| **Depende de** | CTH-9, CTH-13 |

## Objetivo

Registrar cancelamentos feitos com menos de 24 h de antecedência e permitir bloquear temporariamente quem abusa, para proteger a disponibilidade dos espaços.

## Quem usa

| Perfil | O que faz |
|---|---|
| Administrador / operador | Consulta cancelamentos tardios e aplica ou remove bloqueios |
| Mutualista, instituição e público externo | São impactados pelo bloqueio |

## Indicadores de sucesso (proposta da equipe)

- Redução de cancelamentos tardios ao longo do tempo.

## História

**Como** equipe da Mútua-PI, **quero** registrar cancelamentos tardios e bloquear temporariamente um usuário de fazer novas reservas, **para** reduzir horários perdidos.

## Critérios de aceite

- [ ] Todo cancelamento com **menos de 24 h** de antecedência é **registrado como tardio**.
- [ ] A equipe consulta o **histórico de cancelamentos tardios** por usuário.
- [ ] A equipe pode **bloquear temporariamente** um usuário de fazer novas reservas (proposta: **1 semana**).
- [ ] Um usuário bloqueado, ao tentar reservar, **vê o aviso** com o motivo e o prazo do bloqueio.
- [ ] A equipe pode **remover o bloqueio** antes do prazo.
- [ ] O bloqueio **expira automaticamente** ao fim do prazo.
