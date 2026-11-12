# CTH-5 - Recomendação de ambiente por número de participantes

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF05 · [Fluxo principal de reserva](../docs/08-fluxo-de-reserva.md) (passos 1 e 2) |
| **Depende de** | CTH-1, CTH-2 |

## Objetivo

Ajudar quem não sabe qual espaço escolher: o usuário informa quantas pessoas vão participar e o sistema recomenda o ambiente disponível mais adequado.

## Quem usa

| Perfil | O que faz |
|---|---|
| Mutualista / profissional do Crea | Descobre o ambiente adequado para o seu grupo |
| Instituição parceira | Descobre o ambiente adequado para o seu evento |
| Público externo | Descobre o ambiente adequado para o seu grupo |

## Indicadores de sucesso (proposta da equipe)

- Percentual de reservas iniciadas pelo número de participantes que são concluídas.

## História

**Como** usuário, **quero** informar quantas pessoas vão participar, **para** receber a recomendação do ambiente disponível mais adequado.

## Critérios de aceite

- [ ] O usuário pode iniciar a reserva **escolhendo o tipo de espaço** ou **informando o número de participantes**.
- [ ] Com a data e o horário escolhidos, o sistema lista os **ambientes disponíveis** e **recomenda o mais adequado** à quantidade de pessoas.
- [ ] A recomendação considera a **capacidade** do ambiente (definida no Framer); trava por **mínimo de pessoas** não precisa ser exigida por enquanto.
- [ ] Ambientes **indisponíveis ou bloqueados** não são recomendados.
- [ ] O usuário pode **escolher outro ambiente** da lista em vez do recomendado.
- [ ] Se nenhum ambiente atende, o sistema informa isso com clareza e sugere ajustar data, horário ou número de pessoas.
