# CTH-11 - Reserva de estações por período

| Campo | Valor |
|---|---|
| **Prioridade** | Fora do MVP por ora (a Mútua quer só o MVP) |
| **Requisitos** | RF12 · RN08 |
| **Depende de** | CTH-3 |

## Objetivo

Permitir que o usuário reserve uma estação por um período contínuo (diário, semanal ou mensal), em vez de reservar dia a dia.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Reserva a mesma estação por vários dias |
| Público externo | Solicita estação por período, mediante justificativa |
| Administrador / operador | Acompanha reservas longas |

## Indicadores de sucesso (proposta da equipe)

- Quantidade de reservas por período e taxa de uso efetivo dessas reservas.

## História

**Como** profissional, **quero** reservar uma estação por um período (diário, semanal ou mensal), **para** ter meu lugar garantido sem refazer a reserva todos os dias.

## Critérios de aceite

- [ ] O usuário escolhe o período: **diário**, **semanal** ou **mensal**.
- [ ] A estação fica reservada em **todos os dias do período**, e o sistema impede a reserva se algum dia já estiver ocupado ou bloqueado.
- [ ] Cada estação continua sendo uma **reserva independente** (RN08).
- [ ] O comprovante mostra o **período completo** (CTH-8).
- [ ] O usuário pode **cancelar** a reserva do período (CTH-9), com regras de prazo aplicáveis.
- [ ] A equipe enxerga as reservas por período no painel (CTH-13).
