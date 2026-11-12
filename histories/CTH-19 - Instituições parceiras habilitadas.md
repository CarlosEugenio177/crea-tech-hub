# CTH-19 - Instituições parceiras habilitadas

| Campo | Valor |
|---|---|
| **Prioridade** | Fora do escopo atual (decisão do áudio) |
| **Requisitos** | RF20 · [Perfis de usuário](../docs/04-perfis-de-usuario.md) |
| **Depende de** | CTH-12, CTH-15 |

## Status

> **Fora do escopo:** na validação de impedimentos, foi definido que o software é exclusivo da **Mútua** (sem lista de instituições parceiras nem Crea Júnior). No cadastro/reserva, solicita-se apenas o identificador/cartãozinho da Mútua (CTH-7). Card mantido apenas como histórico.

## Objetivo

Manter a lista de instituições parceiras habilitadas a reservar o auditório e salas, para que o perfil "instituição" selecione a sua em uma lista cadastrada pela Mútua.

## Quem usa

| Perfil | O que faz |
|---|---|
| Administrador / operador | Cadastra, edita e desativa instituições |
| Instituição parceira | Seleciona a instituição na reserva |

## Indicadores de sucesso (proposta da equipe)

- Número de eventos e cursos de instituições parceiras realizados no auditório.

## História

**Como** administrador, **quero** manter a lista de instituições parceiras habilitadas, **para** que só elas reservem o auditório e as salas destinadas a parceiros.

## Critérios de aceite

- [ ] Posso **cadastrar, editar e desativar** instituições parceiras.
- [ ] Na reserva, o perfil **instituição parceira** **seleciona a instituição na lista** (CTH-7).
- [ ] Só instituições **ativas** aparecem na lista.
- [ ] O **auditório** e as salas habilitadas para parceiros só são reserváveis por quem escolhe uma instituição da lista.
- [ ] Desativar uma instituição **não apaga** o histórico de suas reservas.
