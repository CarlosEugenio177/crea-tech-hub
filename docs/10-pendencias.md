# Pendências

## Para validar com a Mútua-PI

Lista original da análise de requisitos.

| # | Ponto em aberto | Responsável |
|---|---|---|
| 1 | Envio do layout do andar, com numeração das estações e capacidade de cada ambiente. | Mútua-PI |
| 2 | Lista dos equipamentos disponíveis para reserva e regras de uso de cada um. | Mútua-PI |
| 3 | Mínimo de pessoas e máximo de horas por sala; texto dos avisos de cada ambiente. | Mútua-PI |
| 4 | Modelo de aprovação: reservas confirmadas automaticamente ou após análise da equipe? Valer o mesmo para todos os perfis? | Mútua-PI + equipe |
| 5 | Períodos de reserva das estações (diário, semanal, mensal) e o que fazer com reservas longas pouco usadas. | Mútua-PI |
| 6 | Regra final de bloqueio por cancelamento tardio: automático ou manual, quantas ocorrências e por quanto tempo. | Mútua-PI |
| 7 | Horário de funcionamento do Hub (dias da semana, noite e fim de semana). | Mútua-PI |
| 8 | Lista de instituições parceiras habilitadas e quais ambientes cada perfil pode reservar. | Mútua-PI |
| 9 | Nome oficial do Hub e da área de apoio multiuso; manual de marca ou cores oficiais. | Mútua-PI |
| 10 | Onde o sistema será hospedado e quem será o responsável técnico após a entrega. | Mútua-PI + iCEV |

## Dúvidas da equipe

> Estes pontos **não constam da análise original**. Foram identificados na revisão do documento e ainda precisam de decisão.

| # | Ponto | Por que importa | Referências |
|---|---|---|---|
| E1 | Sem login definido para o solicitante, como ele comprova que a reserva é dele na hora de cancelar? | O cadastro acontece na própria reserva, mas o cancelamento é restrito às próprias reservas. | RF07, RF10 |
| E2 | Qual dado identifica um usuário bloqueado por cancelamento tardio? | O público externo não tem RNP, e WhatsApp ou e-mail podem mudar entre reservas. | RF19, RN04 |
| E3 | O que o público externo recebe ao enviar a solicitação, antes da análise da equipe? | O fluxo emite comprovante e confirmação logo após o registro, mas a RN06 exige análise antes de confirmar. Depende da pendência 4. | RF09, RN06, fluxo passo 5 |
| E4 | A confirmação por WhatsApp/e-mail entra no MVP? | O fluxo principal a inclui, mas o RF11 tem prioridade Média e o MVP cobre os requisitos Alta. | RF11, fluxo passo 5 |
| E5 | A lista de instituições parceiras entra no MVP? | O perfil de instituição seleciona a instituição em uma lista, mas o RF20 tem prioridade Média. | RF07, RF20 |
