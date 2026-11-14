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

- [ ] O solicitante usa o **link individual e imprevisível** associado à sua reserva para acompanhar ou cancelar, sem precisar de login nesta fase.
- [ ] O código público do protocolo, isoladamente, não autoriza o cancelamento; o link permite gerenciar somente a reserva correspondente.
- [ ] Se o cancelamento ocorrer com **menos de 24 h de antecedência**, o sistema **avisa** o usuário antes de confirmar o cancelamento.
- [ ] O cancelamento com menos de 24 h é **marcado como tardio** na reserva (histórico por usuário e bloqueio ficam no CTH-18, fora do MVP).
- [ ] Após cancelar, o **horário é liberado** na disponibilidade (CTH-2) e a reserva aparece como cancelada no painel (CTH-13).
- [ ] O usuário recebe a **confirmação do cancelamento**.
