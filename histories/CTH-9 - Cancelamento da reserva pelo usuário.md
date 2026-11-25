# CTH-9 - Cancelamento da reserva pelo usuário

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF10 · RN04 |
| **Depende de** | CTH-8 |
| **Relacionada a** | CTH-18 (cancelamentos tardios e bloqueio) |

## Objetivo

Permitir que o usuário cancele a própria reserva a qualquer momento, sendo avisado quando o cancelamento for tardio.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Cancela as próprias reservas |
| Instituição parceira | Cancela as próprias reservas |
| Público externo | Cancela as próprias reservas |

## Indicadores de sucesso (proposta da equipe)

- Percentual de horários liberados por cancelamento com mais de 24 h de antecedência.
- Redução de não comparecimentos.

## História

**Como** usuário, **quero** cancelar minha reserva quando não puder comparecer, **para** liberar o espaço para outras pessoas.

## Critérios de aceite

O cancelamento público expõe `POST /api/v1/reservations/cancel` sem login e com
CSRF, autenticado somente pelo token privado enviado em `X-Reservation-Token`
(mantido no fragmento `#<token>` de `/acompanhar`). O backend bloqueia a linha
da reserva no PostgreSQL (`FOR UPDATE`), permite cancelar solicitações `PENDING`
e `CONFIRMED` enquanto o período ainda não terminou (`now < endsAt` no fuso
`America/Fortaleza`), exige confirmação explícita quando faltam menos de 24 h
(`confirmLateCancellation = true`, caso contrário responde `409`) e grava
`status = 'CANCELLED'`, `cancelled_at` e `late_cancellation` preservando o
histórico para consulta no painel administrativo (CTH-13).

- [x] O solicitante usa o **link individual e imprevisível** associado à sua reserva para acompanhar ou cancelar, sem precisar de login nesta fase.
- [x] O código público do protocolo, isoladamente, não autoriza o cancelamento; o link permite gerenciar somente a reserva correspondente.
- [x] Se o cancelamento ocorrer com **menos de 24 h de antecedência**, o sistema **avisa** o usuário antes de confirmar o cancelamento.
- [x] O cancelamento com menos de 24 h é **marcado como tardio** na reserva (histórico por usuário e bloqueio ficam no CTH-18, fora do MVP).
- [x] Após cancelar, o **horário é liberado** na disponibilidade (CTH-2) e a reserva fica gravada como cancelada para o painel (CTH-13).
- [x] O usuário recebe a **confirmação do cancelamento**.
