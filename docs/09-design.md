# Design

O design oficial do projeto está no Framer:

**[Functional Technology — projeto no Framer](https://framer.com/projects/Functional-Technology--oPvtlPE6L7zY6TVoD80Q-4pu9H)**

## Regras

- O Framer é a fonte de verdade para telas, componentes e identidade visual aplicada.
- O protótipo do MVP e as telas incluídas no PDF da análise de requisitos não são referência de design.
- O acesso ao projeto depende de permissão no workspace do Framer.

## Telas do protótipo

Para quem não tem acesso ao Framer, as telas do protótipo foram recriadas em HTML a
partir do projeto no Framer (14/11/2026) e exportadas em PNG de alta resolução
(1200px de largura em 2x, página inteira). São referência rápida: em caso de
divergência, vale o que está no Framer.

![Canvas com todas as telas](design/telas/00-canvas.png)

### Fluxo do profissional

| Tela | Rota no Framer | Imagem |
|---|---|---|
| Cadastro | `/` (Home) | [01-home-cadastro](design/telas/01-home-cadastro.png) |
| Meu painel | `/painel` | [02-meu-painel](design/telas/02-meu-painel.png) |
| Nova reserva · etapa 1 (espaço) | `/reservas` | [04-nova-reserva-etapa1-espacos](design/telas/04-nova-reserva-etapa1-espacos.png) |
| Nova reserva · etapa 2 (data e horário) | `/reservar/...` | [05-nova-reserva-etapa2-horario](design/telas/05-nova-reserva-etapa2-horario.png) |
| Nova reserva · etapa 3 (revisão) | `/reservar/...` | [06-nova-reserva-etapa3-revisao](design/telas/06-nova-reserva-etapa3-revisao.png) |
| Minhas reservas | `/minhas-reservas` | [07-minhas-reservas](design/telas/07-minhas-reservas.png) |
| Histórico | `/historico` | [08-historico](design/telas/08-historico.png) |

### Administração

| Tela | Rota no Framer | Imagem |
|---|---|---|
| Visão geral | `/admin` | [09-admin-visao-geral](design/telas/09-admin-visao-geral.png) |
| Prioridade interna | `/admin/prioridade` | [11-admin-prioridade](design/telas/11-admin-prioridade.png) |
| Reservas | `/admin/reservas` | [12-admin-reservas](design/telas/12-admin-reservas.png) |
| Espaços | `/admin/espacos` | [13-admin-espacos](design/telas/13-admin-espacos.png) |
| Relatórios | `/admin/relatorios` | [14-admin-relatorios](design/telas/14-admin-relatorios.png) |
| Configurações | `/admin/configuracoes` | [15-admin-configuracoes](design/telas/15-admin-configuracoes.png) |

Não recriadas: a página `/404` e a etapa 2 da Sala de podcast e da Mesa colaborativa.

### Código da recriação

O HTML fica em [`design/prototipo/`](design/prototipo/) e pode servir de ponto de partida
para o frontend:

- `css/crea-tec.css` — tokens e estilos. As cores seguem os estilos de cor do Framer
  (Azul elétrico `#2563EB`, Gelo `#F8F9FA`, Preto profundo `#0A0A0A`, Texto `#111827`,
  Texto secundário `#64748B`); a fonte é Inter.
- `assets/` — as seis ilustrações SVG originais do projeto no Framer.
- `telas/` — uma página HTML por tela, gerada por `build.py`.
- `render.py` e `canvas.py` — regeram os PNGs e o canvas em `design/telas/`
  (Python com Playwright).

```bash
cd docs/design/prototipo
python3 build.py && python3 render.py && python3 canvas.py
```

## Requisitos que o design precisa atender

| ID | Requisito |
|---|---|
| RNF01 | Web responsivo, para celular e computador |
| RNF02 | Reserva em poucos passos, com linguagem simples |
| RNF03 | Tons de azul, amadeirado e branco gelo, evitando branco puro |
| RNF06 | Contraste, navegação por teclado e textos alternativos |
