# CTH-16 - Bloqueios de horário e reuniões da diretoria

| Campo | Valor |
|---|---|
| **Prioridade** | Alta (MVP) |
| **Requisitos** | RF16 · RN05 |
| **Depende de** | CTH-12, CTH-15 |

## Objetivo

Travar horários em que um ambiente não pode ser reservado, inclusive de forma recorrente, como as tardes de reunião da diretoria.

## Quem usa

| Perfil | O que faz |
|---|---|
| Diretoria | Mantém travados os horários fixos de reunião |
| Administrador / operador | Cria, edita e remove bloqueios |

## Indicadores de sucesso (proposta da equipe)

- Nenhuma reserva pública em conflito com os horários fixos da diretoria.

## História

**Como** equipe da Mútua-PI, **quero** criar bloqueios de horário, inclusive recorrentes, **para** garantir a prioridade da diretoria nas salas de reunião e impedir reservas em horários indisponíveis.

## Critérios de aceite

O painel permite criar, consultar e cancelar bloqueios pontuais, com histórico
e herança dos bloqueios da sala para suas estações. A disponibilidade pública
considera os bloqueios ativos. O painel avisa e preserva a reserva se houver
conflito. O painel também cria séries semanais até uma data e permite cancelar uma
ocorrência ou o restante da série. Para editar um intervalo, cancele e crie outro. Ver [Bloqueios de horário](../docs/21-bloqueios-de-horario.md).

- [x] Posso criar um bloqueio informando **ambiente**, **data(s)** e **horário**.
- [x] Posso criar bloqueios **recorrentes** (ex.: as tardes de reunião da diretoria definidas no início do ano).
- [x] Horários bloqueados aparecem como **indisponíveis** na consulta e **não aceitam reservas** (CTH-2, CTH-4).
- [ ] **Fora** dos horários fixos, as salas da diretoria **podem ser liberadas** ao público quando estiverem livres.
- [ ] Posso **editar e remover** bloqueios; remover libera o horário.
- [x] Ao criar um bloqueio sobre **reservas já existentes**, o sistema **avisa** a equipe do conflito antes de salvar.
