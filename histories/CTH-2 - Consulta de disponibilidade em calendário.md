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
O visitante pode escolher um dia nos atalhos da semana, abrir **Ver mês** ou
informar a data no campo. A visão mensal permite navegar sem consultar horários.
Ao escolher uma data, são carregados os horários daquele dia. Dias passados
ficam desabilitados.
A consulta do dia entrega as janelas livres da sala ou de cada estação.
O visitante toca em um intervalo livre ou ajusta início e fim por minuto
entre as opções disponíveis, dentro dos limites de duração configurados.
Em salas por estação, o mapa habilita as
estações livres durante todo o período escolhido usando a mesma consulta.
O backend confere conflitos novamente no envio do pedido. A agenda só
expõe horários e dados públicos das estações.

- [x] A consulta é feita por **data e horário**, em **visão de calendário**.
- [x] Horários já reservados, **bloqueados** (ex.: reuniões fixas da diretoria) ou de ambientes **indisponíveis ao público** aparecem como não disponíveis.
- [ ] As salas da diretoria só aparecem como livres **fora dos horários fixos** e quando liberadas.
- [x] Só são oferecidos para reserva os horários e dias dentro do **funcionamento do Hub**: **8h às 18h, de segunda a sexta, por padrão**, com **horário e dias da semana configuráveis** pela equipe (CTH-15).
- [ ] O site pode ser acessado e a solicitação enviada **a qualquer hora**; o que é limitado é o período reservável.
- [x] A disponibilidade reflete reservas **confirmadas e canceladas** em tempo real: um cancelamento libera o horário.
- [x] A visão é utilizável no **celular** e no computador (CTH-21).
