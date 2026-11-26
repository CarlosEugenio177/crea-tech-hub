# CTH-18 - Faltas e bloqueio temporário de reservas

| Campo | Valor |
|---|---|
| **Prioridade** | Registro de faltas e bloqueio manual incluídos |
| **Requisitos** | RF19 · RN04 |
| **Depende de** | CTH-9, CTH-13 |

## Objetivo

Registrar faltas para que a equipe possa aplicar um bloqueio temporário quando houver reincidência.

## Quem usa

| Perfil | O que faz |
|---|---|
| Administrador / operador | Registra faltas e aplica ou remove bloqueios |
| Mutualista, instituição e público externo | São impactados pelo bloqueio |

## Indicadores de sucesso (proposta da equipe)

- Redução de faltas em reservas confirmadas.

## História

**Como** equipe da Mútua-PI, **quero** registrar faltas e impedir temporariamente novas reservas em caso de reincidência, **para** reduzir horários perdidos.

## Registro de falta e bloqueio

Após o término de uma reserva confirmada, a equipe pode registrar ou corrigir
uma falta pelo painel. A primeira falta fica no histórico. A partir da segunda
falta registrada para o mesmo CPF, a equipe pode definir o bloqueio em dias
e removê-lo pelo painel. O prazo expira automaticamente. Corrigir um registro
e deixar apenas uma falta também retira o bloqueio ativo.

O cancelamento continua disponível com aviso quando faltam menos de 24 h para
o início. Ele fica registrado como tardio e não entra na contagem de faltas.

## Critérios de aceite

- [x] Cancelamentos com menos de 24 h são registrados como tardios.
- [x] A equipe registra ou corrige a falta após o fim de uma reserva confirmada.
- [x] A partir da segunda falta, a equipe pode definir o bloqueio em dias.
- [x] A pessoa bloqueada recebe o motivo e o prazo ao tentar reservar.
- [x] A equipe pode remover o bloqueio antes do prazo.
- [x] O bloqueio expira automaticamente.