# Design

O design oficial do projeto está no Framer:

**[Functional Technology — projeto no Framer](https://framer.com/projects/Functional-Technology--oPvtlPE6L7zY6TVoD80Q-4pu9H)**

## Regras

- O Framer é a referência visual para componentes e identidade aplicada.
- Regras de negócio e escopo seguem as decisões atuais em `histories/` e em
  [Acesso administrativo e fluxo público](15-autenticacao.md). Um fluxo antigo do
  Framer não altera essas decisões.
- O protótipo do MVP e as telas incluídas no PDF da análise de requisitos não são referência de design.
- O acesso ao projeto depende de permissão no workspace do Framer.

## Telas do protótipo

Para quem não tem acesso ao Framer, as telas do protótipo foram recriadas em HTML a
partir do projeto no Framer (14/11/2026) e exportadas em PNG de alta resolução
(1200px de largura em 2x, página inteira). Medidas, espaçamentos, raios, cores e
tipografia foram lidos camada a camada no painel do Framer; as ilustrações são os SVGs
originais do projeto. Em 15/11/2026, a cópia local recebeu ajustes ao escopo atual:
solicitação sem conta, início e fim livres por minuto, análise manual, acompanhamento
por link individual e menu administrativo. Os slugs originais foram preservados.
Esses ajustes não alteram o projeto remoto no Framer.

**São referências estáticas, sem integração com a API.** O painel pessoal e o histórico
dependem de autenticação futura do solicitante; prioridade interna e relatórios estão
fora do MVP. Nenhuma tela comprova que uma funcionalidade já foi implementada.

![Canvas com todas as telas](design/telas/00-canvas.png)

### Fluxo público e referências futuras

| Tela | Rota no Framer | Imagem |
|---|---|---|
| Dados da solicitação, sem conta | `/` (Home; originalmente cadastro) | [01-home-cadastro](design/telas/01-home-cadastro.png) |
| Meu painel · referência futura | `/painel` | [02-meu-painel](design/telas/02-meu-painel.png) |
| Nova reserva · etapa 1 (espaço) | `/reservas` | [04-nova-reserva-etapa1-espacos](design/telas/04-nova-reserva-etapa1-espacos.png) |
| Nova reserva · etapa 2 (data e horário) | `/reservar/horario` | [05-nova-reserva-etapa2-horario](design/telas/05-nova-reserva-etapa2-horario.png) |
| Nova reserva · etapa 3 (revisão) | `/reservar/revisao` | [06-nova-reserva-etapa3-revisao](design/telas/06-nova-reserva-etapa3-revisao.png) |
| Acompanhamento de uma solicitação pelo link individual | `/minhas-reservas` (rota original do Framer) | [07-minhas-reservas](design/telas/07-minhas-reservas.png) |
| Histórico · referência futura | `/historico` | [08-historico](design/telas/08-historico.png) |

A etapa 2 local mostra um espaço de exemplo e campos de início e fim com precisão
de minutos. A implementação deverá carregar o recurso selecionado, seu funcionamento
e seus bloqueios; não deve oferecer apenas os horários fixos do Framer. As rotas
listadas acima são referências de design, não contratos HTTP definidos para o produto.

### Administração

| Tela | Rota no Framer | Imagem |
|---|---|---|
| Visão geral | `/admin` | [09-admin-visao-geral](design/telas/09-admin-visao-geral.png) |
| Prioridade interna · fora do MVP | `/admin/prioridade` | [11-admin-prioridade](design/telas/11-admin-prioridade.png) |
| Reservas | `/admin/reservas` | [12-admin-reservas](design/telas/12-admin-reservas.png) |
| Espaços | `/admin/espacos` | [13-admin-espacos](design/telas/13-admin-espacos.png) |
| Relatórios · fora do MVP | `/admin/relatorios` | [14-admin-relatorios](design/telas/14-admin-relatorios.png) |
| Configurações | `/admin/configuracoes` | [15-admin-configuracoes](design/telas/15-admin-configuracoes.png) |

O menu de `/admin` foi corrigido na cópia local para usar a navegação administrativa.
A divergência no Framer permanece registrada em D1 de [Pendências](10-pendencias.md).

### Outras páginas

| Tela | Rota no Framer | Imagem |
|---|---|---|
| Página não encontrada | `/404` | [16-erro-404](design/telas/16-erro-404.png) |

### Tokens

| Token | Valor | Origem no Framer |
|---|---|---|
| Azul elétrico | `#2563EB` | estilo de cor "Crea Tec — Azul elétrico" |
| Gelo | `#F8F9FA` | estilo de cor "Crea Tec — Gelo" |
| Preto profundo | `#0A0A0A` | estilo de cor "Crea Tec — Preto profundo" |
| Texto | `#111827` | estilo de cor "Crea Tec — Texto" |
| Texto secundário | `#64748B` | estilo de cor "Crea Tec — Texto secundário" |
| Borda de cartão | `#E1E7EE`, 1px | borda dos cartões |
| Fonte | Inter | todas as camadas de texto |
| Rótulo (eyebrow) | Inter Bold 11px, espaçamento 0,08em, maiúsculas | rótulos das páginas (a `/404` usa o estilo de texto "Crea Tec — Rótulo") |
| Título de página | Inter Bold 36px, altura 1,1, espaçamento −0,03em | cabeçalhos |
| Raios | cartões 14px (formulário 16px, admin 12px), botões 8px (10px nos grandes) | camadas |

Os tokens ficam em `frontend/src/shared/styles/tokens.css`. Para estados de interação, o
frontend deriva três valores do azul e do texto do Framer, que não existem como estilos no
projeto: azul de hover `#1D4ED8`, anel de foco `rgba(37, 99, 235, 0.25)` e uma sombra leve
para superfícies elevadas (formulários e resumo da solicitação).

A logomarca oficial está em [`design/logo/logo-mutua.png`](design/logo/logo-mutua.png). Nas
barras pretas o frontend usa a versão negativa (texto branco, como no rodapé do site da
Mútua), gerada a partir desse arquivo em `frontend/src/shared/assets/logo-mutua-negativa.png`.
O ícone da aba (`frontend/public/favicon.png`) é o emblema recortado da mesma imagem.

### Código da recriação

O HTML fica em [`design/prototipo/`](design/prototipo/) e serve de referência visual.
O frontend funcional será construído em sua própria pasta; estas telas não têm
validação de formulário, navegação funcional, persistência ou garantia de responsividade:

- `css/crea-tec.css` — tokens e estilos, com os valores lidos no Framer.
- `assets/` — as sete ilustrações SVG originais do projeto no Framer.
- `telas/` — uma página HTML por tela, gerada por `build.py`.
- `render.py` e `canvas.py` — regeram os PNGs e o canvas em `design/telas/`
  (Python com Playwright e Pillow).

```bash
cd docs/design/prototipo
python3 -m pip install -r requirements.txt
python3 -m playwright install chromium
python3 build.py && python3 render.py && python3 canvas.py
```

No Windows, os mesmos scripts funcionam com `python` ou `python3`, conforme a
instalação; execute cada comando em uma linha no Windows PowerShell 5.1.
O canvas procura Inter, DejaVu Sans ou Arial em caminhos locais de Linux, Windows e
macOS, com fallback do Pillow. Para usar sua instalação de Inter, defina
`PROTOTYPE_FONT_DIR` apontando para a pasta dos arquivos `Inter-*.otf` ou `.ttf`.
Fontes substitutas podem alterar a tipografia dos rótulos do canvas.

## Requisitos que o design precisa atender

| ID | Requisito |
|---|---|
| RNF01 | Web responsivo, para celular e computador |
| RNF02 | Reserva em poucos passos, com linguagem simples |
| RNF03 | Tons de azul, amadeirado e branco gelo, evitando branco puro |
| RNF06 | Contraste, navegação por teclado e textos alternativos |
