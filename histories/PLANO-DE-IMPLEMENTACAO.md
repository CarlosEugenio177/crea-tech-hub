# Plano de implementação — Mútua (Crea Tech Hub)

Ordem dos cards com base nas dependências e decisões do projeto. Cards da mesma etapa podem andar em paralelo. A Mútua quer **somente o MVP por ora**.

## MVP

| Etapa | Cards | Depende de |
|---|---|---|
| 0 | **CTH-12** Acesso administrativo (login CRUD + migração do BD) · **CTH-21** Interface (prints do Framer + marca Mútua) | — |
| 1 | **CTH-15** Cadastro de salas, estações e funcionamento (horário e dias) · **CTH-13** Painel de reservas · **CTH-22** LGPD | 12 |
| 2 | **CTH-1** Catálogo · **CTH-6** Regras e aceite · **CTH-16** Bloqueios · **CTH-14** Análise e decisão da equipe · **CTH-7** Dados do solicitante sem conta nesta fase | 15, 13 |
| 3 | **CTH-2** Disponibilidade em calendário (8h às 18h, seg a sex por padrão; horário e dias configuráveis) | 1, 16 |
| 4 | **CTH-3** Estação individual · **CTH-4** Salas e auditório · **CTH-5** Recomendação | 2 |
| 5 | **CTH-8** Comprovante / aviso de encaminhamento para análise | 3, 4, 6, 7 |
| 6 | **CTH-9** Acompanhamento/cancelamento por link individual · **CTH-18** Registro de faltas e bloqueio manual | 8, 13 |
| 7 | **CTH-10** WhatsApp e e-mail (também envia as notificações de decisão do CTH-14) | 8 |

**Marco do MVP:** fim da etapa 7.

## Fora do MVP por ora

| Card | Motivo |
|---|---|
| CTH-11 Reserva por período · CTH-17 Situação do solicitante · CTH-20 Relatórios | Mútua quer só o MVP |
| CTH-23 Integração Mútua/Crea · CTH-24 Prioridade e cobrança | Fase futura |
| CTH-19 Instituições parceiras · reserva de equipamentos | Fora do escopo (software só para a Mútua; foco em salas) |

## Caminho crítico

`CTH-12 → 15 → 1 → 2 → 3/4 → 8 → 9/10`

## Impedimentos restantes

Nenhum impedimento bloqueia o MVP. Ver [IMPEDIMENTOS.md](IMPEDIMENTOS.md).
