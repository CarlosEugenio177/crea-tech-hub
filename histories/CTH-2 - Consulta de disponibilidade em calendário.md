# CTH-2 - Consulta de disponibilidade em calendário

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF02 · RNF08 · RN05 |
| **Depende de** | CTH-1, CTH-16 (bloqueios) |

## Objetivo

Mostrar, em visão de calendário, quando cada ambiente, estação ou equipamento está livre, para que o usuário reserve sem precisar falar com a equipe.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Consulta datas e horários livres |
| Instituição parceira | Consulta a agenda do auditório e salas |
| Público externo | Consulta datas e horários livres |

## Indicadores de sucesso (proposta da equipe)

- Percentual de consultas de disponibilidade que terminam em reserva.

## História

**Como** usuário, **quero** consultar a disponibilidade por data e horário em um calendário, **para** escolher um momento livre sem depender de contato com a Mútua-PI.

## Critérios de aceite

A agenda diária mostra períodos livres e indisponíveis no horário local do Hub.
O visitante pode trocar de dia pelo calendário ou informar a data no campo.
Ao selecionar um período livre, pode ajustar início e fim por minuto antes de
conferir a disponibilidade. Para salas por estação, cada período oferecido
cabe inteiro em pelo menos uma estação. O dia mostra somente horários, sem
dados dos ocupantes ou motivos de bloqueios.

- [x] A consulta é feita por **data e horário**, em **visão de calendário**.
- [x] Horários já reservados, **bloqueados** (ex.: reuniões fixas da diretoria) ou de ambientes **indisponíveis ao público** aparecem como não disponíveis.
- [ ] As salas da diretoria só aparecem como livres **fora dos horários fixos** e quando liberadas.
- [x] Só são oferecidos para reserva os horários e dias dentro do **funcionamento do Hub**: **8h às 18h, de segunda a sexta, por padrão**, com **horário e dias da semana configuráveis** pela equipe (CTH-15).
- [ ] O site pode ser acessado e a solicitação enviada **a qualquer hora**; o que é limitado é o período reservável.
- [x] A disponibilidade reflete reservas **confirmadas e canceladas** em tempo real: um cancelamento libera o horário.
- [x] A visão é utilizável no **celular** e no computador (CTH-21).
