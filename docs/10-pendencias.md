# Pendências

## Decisões que já substituem o levantamento original

As tabelas abaixo preservam o histórico da análise. Para a implementação atual,
valem as histórias em `histories/` e [Acesso administrativo e fluxo público](15-autenticacao.md):

- Aprovação é manual para todas as solicitações. Pendentes e confirmadas bloqueiam
  o intervalo, sem expiração automática da pendência; o comprovante inicial informa
  que a solicitação aguarda análise (pendência 4 e E3 resolvidas).
- O público solicita sem conta e acompanha ou cancela por link individual
  imprevisível, sem listagem pública. O protocolo sozinho não autoriza operações
  (E1 resolvida). Login local é somente administrativo nesta fase.
- O solicitante informa início e fim por minuto; não há escolha obrigatória de slots
  fixos. Limites e funcionamento são tratados por ambiente conforme as histórias.
- Notificações estão no plano do MVP. Instituições parceiras e reserva de equipamentos
  estão fora do escopo atual. Prioridade e cobrança ficam para fases futuras.
- O cancelamento tardio é registrado sem contar como falta. A equipe decide o
  prazo de bloqueio a partir da segunda falta registrada para o mesmo CPF.
- Hospedagem e CD continuam a definir. Desenvolvimento local usa Compose/devcontainer;
  não existe ambiente de deploy provisionado por esta configuração.

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

## Design no Framer

> Encontrados ao recriar as telas do protótipo (14/11/2026). Ver [Design](09-design.md).

A cópia HTML local corrige o menu administrativo (D1) e ilustra início/fim por minuto
com o espaço selecionado (D2). Os ajustes do projeto remoto no Framer continuam
pendentes. O HTML segue demonstrativo e não implementa disponibilidade por recurso.

| # | Ponto | Por que importa | Onde |
|---|---|---|---|
| D1 | A página `/admin` (Visão Geral) usa o template de layout **Navegação do profissional**, então o menu superior mostra Meu painel, Nova reserva, Minhas reservas e Histórico. As demais páginas `/admin/*` usam **Navegação da administração** (Visão geral, Prioridade, Reservas, Espaços, Relatórios, Configurações). | A equipe administrativa veria, na tela inicial dela, o menu do profissional. Provável descuido: basta trocar o template de layout da página `/admin` no Framer. | Framer → página `/admin` → Template → Layout |
| D2 | A etapa 2 da reserva é uma página única (`/reservar/horario`) para os três espaços: Estação individual, Sala de podcast e Mesa colaborativa levam à mesma tela, com os mesmos horários. | Se os espaços tiverem regras diferentes (duração, mínimo de pessoas, horários), a etapa precisa mostrar o espaço escolhido e opções próprias. Depende das pendências 3 e 5. | Framer → `/reservas` → botões "Ver horários" |
