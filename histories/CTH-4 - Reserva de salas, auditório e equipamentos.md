# CTH-4 - Reserva de salas, auditório e equipamentos

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF04 · RN03 · RN05 |
| **Depende de** | CTH-2, CTH-15, CTH-16 |

## Objetivo

Permitir reservar salas e o auditório por data e intervalo de horário, respeitando as regras de disponibilidade. (Reserva de equipamentos está fora do escopo atual.)

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Reserva salas e auditório |
| Público externo | Solicita salas, mediante justificativa |
| Diretoria | Reserva salas de reunião |

## Indicadores de sucesso (proposta da equipe)

- Número de reservas de salas e auditório por mês.
- Taxa de ocupação do auditório.

## História

**Como** usuário, **quero** reservar uma sala ou o auditório por data e intervalo de horário, **para** realizar minha reunião, evento ou trabalho no Hub.

## Critérios de aceite

- [ ] A reserva é feita por **data** e **intervalo de horário** (início e fim).
- [ ] O sistema **não permite reservas sobrepostas** do mesmo ambiente.
- [ ] Reserva de **equipamentos** fica fora do escopo atual (foco exclusivo em salas/espaços).
- [ ] Trava de **mínimo de pessoas** e **máximo de horas** por sala **não precisa** ser exigida por enquanto (pode ser configurável futuramente).
- [ ] As **salas de reunião da diretoria** só ficam reserváveis fora dos horários fixos da diretoria e quando liberadas ao público.
- [ ] A reserva passa por **análise da equipe** (CTH-14) e segue as etapas de regras de uso (CTH-6) e dados do solicitante (CTH-7).
