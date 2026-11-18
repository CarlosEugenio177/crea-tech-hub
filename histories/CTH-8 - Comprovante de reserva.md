# CTH-8 - Comprovante de reserva

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF09 · [Fluxo principal de reserva](../docs/08-fluxo-de-reserva.md) (passo 5) |
| **Depende de** | CTH-3, CTH-4, CTH-6, CTH-7 |

## Objetivo

Entregar ao usuário um comprovante com os dados do agendamento e as orientações de uso, assim que a reserva for registrada.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Recebe e guarda o comprovante |
| Instituição parceira | Recebe e guarda o comprovante |
| Público externo | Recebe o comprovante ou a confirmação de recebimento da solicitação |

## Indicadores de sucesso (proposta da equipe)

- Redução de contatos à equipe para confirmar se a reserva foi feita.

## História

**Como** usuário, **quero** receber um comprovante da minha reserva, **para** ter registro do agendamento e saber o que devo cumprir.

## Critérios de aceite

A entrega de solicitação expõe `POST /api/v1/reservations` sem login e com
CSRF. O backend valida CPF e aceite, verifica novamente a disponibilidade sob
bloqueio transacional dos recursos e grava o pedido pendente. O comprovante
exibe protocolo, espaço, estação quando aplicável, data e horário. O token
privado fica no fragmento do endereço e é enviado no cabeçalho
`X-Reservation-Token` ao consultar `GET /api/v1/reservations/receipt`.
CPF e contatos não saem na resposta pública. Decisões, cancelamento e
notificações seguem nas histórias correspondentes.

- [ ] Ao registrar a solicitação, o sistema **exibe o comprovante / protocolo na tela** informando que a reserva foi **encaminhada para análise** e que o status será confirmado via **WhatsApp e e-mail**.
- [ ] O comprovante traz os **dados do agendamento**: ambiente (e estação, quando houver), data, horário e dados do solicitante.
- [ ] O comprovante traz os **textos de orientação**: **compromisso de uso** e **política de cancelamento**.
- [ ] O solicitante recebe um **link individual e imprevisível** para acompanhar ou cancelar sua solicitação (CTH-9), sem precisar de conta nesta fase.
- [ ] O comprovante e o status final após análise são enviados por **WhatsApp e/ou e-mail** (CTH-10).
