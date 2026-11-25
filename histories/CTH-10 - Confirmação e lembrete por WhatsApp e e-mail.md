# CTH-10 - Confirmação e lembrete por WhatsApp e e-mail

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) — único card da antiga Release 2 que entra no MVP |
| **Requisitos** | RF11 · RNF04 · [Fluxo principal de reserva](../docs/08-fluxo-de-reserva.md) (passo 5) |
| **Depende de** | CTH-8 |

## Objetivo

Avisar o usuário sobre a reserva por WhatsApp (canal principal) e e-mail, com confirmação ao reservar e lembrete antes do horário.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Recebe confirmação e lembrete |
| Instituição parceira | Recebe confirmação e lembrete |
| Público externo | Recebe confirmação e lembrete |

## Indicadores de sucesso (proposta da equipe)

- Redução de não comparecimentos.
- Percentual de mensagens entregues.

## História

**Como** usuário, **quero** receber a confirmação e um lembrete da minha reserva por WhatsApp e/ou e-mail, **para** não esquecer o horário e ter o comprovante à mão.

## Critérios de aceite

A fila e a integração com Resend cobrem avisos de solicitação e decisão por
**e-mail**. O envio real aguarda a verificação do domínio. A equipe consulta o
resultado do último aviso no painel. Ver [E-mail de reservas](../docs/23-email-de-reservas.md).
Lembretes e WhatsApp ficam pendentes.

- [ ] Ao registrar a reserva, o sistema envia a **confirmação** por **WhatsApp** e/ou **e-mail**.
- [ ] O sistema envia um **lembrete** antes do horário da reserva.
- [ ] O **WhatsApp é o canal principal**; o **e-mail** serve para registro formal (ex.: ocorrências).
- [ ] A mensagem traz os **dados do agendamento** e como cancelar.
- [ ] Reservas **canceladas** não geram lembrete.
- [ ] Falha no envio **não impede** o registro da reserva e fica visível para a equipe.
