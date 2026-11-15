"""Gera as telas do Crea Tec HUB em HTML (uma página por tela).

A estrutura parte do Framer (14/11/2026), com ajustes ao fluxo público atual.
Uso: python3 build.py  -> escreve os arquivos em telas/
"""
from pathlib import Path

RAIZ = Path(__file__).parent
SAIDA = RAIZ / "telas"
SAIDA.mkdir(exist_ok=True)

NAV_PROF = ["Espaços", "Nova solicitação", "Acompanhar pelo link individual"]
NAV_ADMIN = ["Visão geral", "Prioridade", "Reservas", "Espaços", "Relatórios", "Configurações"]


def topnav(itens):
    lis = "".join(f"<li>{i}</li>" for i in itens)
    return f'<header class="topnav"><div class="marca">Crea Tec HUB</div><ul>{lis}</ul></header>'


def pagina(titulo, corpo):
    return f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{titulo} — Crea Tec HUB</title>
<link rel="stylesheet" href="../css/crea-tec.css">
</head>
<body>
<div class="nota-prototipo">Referência visual estática. Nesta fase, a solicitação é sem conta e passa por análise manual. Painel pessoal, histórico e prioridade interna são referências futuras. Nenhuma ação desta página envia dados.</div>
{corpo}
</body>
</html>
"""


def img(nome, alt=""):
    return f'<img class="cover" src="../assets/{nome}.svg" alt="{alt}">'


def lateral(itens, ativo, admin=False):
    links = "".join(f'<a class="{"on" if i == ativo else ""}"><i></i>{i}</a>' for i in itens)
    rotulo = '<div class="rotulo-admin">Administração</div>' if admin else ""
    conta = "Recepção e Gestão" if admin else "Conta profissional"
    flex = "" if admin else '<div class="flex"></div>'
    return f"""<aside class="lateral">
  <div class="identidade"><i></i><b>Crea Tec HUB</b></div>
  {rotulo}
  <nav class="menu">{links}</nav>
  {flex}
  <div class="conta"><i></i><div><b>{conta}</b><span>Crea Tec HUB</span></div></div>
</aside>"""


def simples(nav, titulo, sub, ilu, cards):
    blocos = ""
    for h, p, extras in cards:
        lis = "".join(f"<li>{x}</li>" for x in extras)
        blocos += f'<section class="card"><h3>{h}</h3><p>{p}</p>{lis}</section>'
    return topnav(nav) + f"""
<main class="pagina">
  <div class="apresentacao">
    <div class="cab-padrao"><div class="eyebrow">Crea Tec HUB</div><h1>{titulo}</h1><p class="sub">{sub}</p></div>
    <div class="ilu">{img(ilu)}</div>
  </div>
  <div class="lista">{blocos}</div>
</main>"""


TELAS = {}

# O slug original é preservado para manter os links das referências visuais.
TELAS["01-home-cadastro"] = ("Dados da solicitação", topnav(NAV_PROF) + f"""
<main class="home">
  <div class="coluna">
    <div class="cabecalho">
      <div class="eyebrow">Crea Tec HUB</div>
      <h1>Seu acesso ao HUB começa aqui</h1>
      <p class="sub">Solicite um espaço sem criar conta. Informe seus dados na própria solicitação.</p>
    </div>
    <form class="card form">
      <div class="campo"><label>Nome completo</label><div class="input">Seu nome</div></div>
      <div class="campo"><label>Telefone</label><div class="input">(00) 00000-0000</div></div>
      <div class="campo"><label>E-mail</label><div class="input">Seu e-mail de contato</div></div>
      <p class="aviso">Estes dados identificam a solicitação. Não criam conta nem comprovam vínculo com Crea/Mútua.</p>
      <div class="btn">Continuar a solicitação</div>
      <p class="nota">Referência visual dos dados da reserva; envio ainda não implementado.</p>
    </form>
  </div>
  <div class="ilu">{img("mulher-mesa", "Profissional trabalhando em uma mesa")}</div>
</main>""")

# /painel
TELAS["02-meu-painel"] = ("Meu painel", topnav(NAV_PROF) + f"""
<div class="app">
  {lateral(["Meu Painel", "Nova Reserva", "Minhas Reservas", "Histórico"], "Meu Painel")}
  <main class="conteudo">
    <div class="cab-painel"><h2>Meu Painel</h2><span class="data">Sábado, 7 de novembro de 2026</span></div>
    <section class="card boas-vindas">
      <div class="msg">
        <div class="rotulo">Seu espaço, sob seu controle</div>
        <h3>Olá, Engenheiro. Sua próxima estação de trabalho te espera.</h3>
        <p>Encontre espaços e recursos para manter seus projetos em movimento.</p>
        <span class="btn">Nova reserva</span>
      </div>
      <div class="ilu">{img("mulher-mesa")}</div>
    </section>
    <div class="linha">
      <section class="card minhas">
        <div class="cab"><h4>Minhas Reservas</h4><span class="badge">1 próxima</span></div>
        <div class="proxima">
          <div class="rotulo">Próxima reserva</div>
          <div class="det"><div><b>Sala de Reunião B</b><small>Reserva confirmada</small></div><strong>Hoje, 14h</strong></div>
        </div>
        <div class="disponiveis">
          <div><b>Espaços disponíveis</b><small>Estações individuais, sala de podcast e mesa colaborativa.</small></div>
          <span>Ver</span>
        </div>
        <div class="link-hist">Ver histórico de reservas</div>
      </section>
      <section class="card agendar">
        <div class="rotulo">Pronto para o próximo projeto?</div>
        <div class="ilu">{img("calendario")}</div>
        <h4>Agendar um espaço</h4>
        <p>Escolha entre estações com computador, sala de podcast ou mesa para grupos.</p>
        <span class="btn">Ver espaços</span>
      </section>
    </div>
  </main>
</div>""")

# /reservas — etapa 1
espacos = [
    ("mulher-mesa", 180, 180, "Estação individual", "Seu posto com computador, reservado só para você.", "1 PESSOA · COMPUTADOR"),
    ("podcast", 160, 180, "Sala de podcast", "Um espaço dedicado à gravação de podcast.", "SALA EXCLUSIVA"),
    ("aperto-de-maos", 200, 144, "Mesa colaborativa", "Uma mesa ampla para encontros e trabalho em equipe.", "SOMENTE PARA GRUPOS"),
]
cards = "".join(
    f'<section class="card"><div class="area"><div style="width:{w}px;height:{h}px">{img(i)}</div></div>'
    f'<h3>{t}</h3><p>{d}</p><div class="tag">{tag}</div><div class="btn">Ver horários</div></section>'
    for i, w, h, t, d, tag in espacos
)
TELAS["04-nova-reserva-etapa1-espacos"] = ("Nova reserva", topnav(NAV_PROF) + f"""
<main class="pagina">
  <div class="cab-padrao">
    <div class="eyebrow">Crea Tec HUB</div>
    <h1>Qual espaço acompanha seu próximo projeto?</h1>
    <p class="sub">Escolha o ambiente certo e veja as opções de horário.</p>
  </div>
  <div class="espacos-grid">{cards}</div>
</main>""")

# /reservar/horario — etapa 2 (a mesma página para os três espaços)
TELAS["05-nova-reserva-etapa2-horario"] = ("Escolha uma data e um horário", topnav(NAV_PROF) + """
<main class="pagina etapa">
  <div class="eyebrow">Nova reserva · Etapa 2 de 3</div>
  <h2>Escolha uma data e um horário</h2>
  <p class="sub">Exemplo: Estação individual 04. A disponibilidade deve considerar o espaço escolhido.</p>
  <section class="card selecao">
    <h4>Data</h4>
    <div class="chips"><span class="chip on">Seg · 09</span><span class="chip">Ter · 10</span><span class="chip">Qua · 11</span></div>
    <h4>Início e fim</h4>
    <div class="campo"><label for="inicio">Início</label><input class="input" id="inicio" type="time" step="60" value="09:15"></div>
    <div class="campo"><label for="fim">Fim</label><input class="input" id="fim" type="time" step="60" value="10:45"></div>
    <p class="nota">Informe início e fim com precisão de minutos, respeitando o funcionamento e os bloqueios do espaço. Nenhuma reserva será criada neste protótipo.</p>
  </section>
  <span class="btn branco">Voltar aos espaços</span>
  <span class="btn lg">Revisar reserva</span>
</main>""")

# /reservar/revisao — etapa 3
TELAS["06-nova-reserva-etapa3-revisao"] = ("Revise sua reserva", topnav(NAV_PROF) + """
<main class="pagina etapa revisao">
  <div class="eyebrow">Nova reserva · Etapa 3 de 3</div>
  <h2>Revise sua reserva</h2>
  <p class="sub">Confira os dados antes de enviar para análise da equipe.</p>
  <section class="card resumo">
    <h4>Detalhes da reserva</h4>
    <p>Espaço, data e horário aparecerão aqui após a seleção.</p>
    <small>A mesa colaborativa é destinada a grupos.</small>
  </section>
  <span class="btn branco">Voltar aos horários</span>
  <span class="btn lg">Enviar solicitação para análise</span>
  <h3 class="estados">Estados do fluxo · exemplos</h3>
  <div class="estados-grid">
    <section class="card"><h4>Sem horários disponíveis</h4><p>Tente outra data ou escolha outro espaço.</p></section>
    <section class="card"><h4>Solicitação recebida · pendente</h4><p>Aguarde a análise manual. O comprovante terá protocolo e link individual para acompanhar ou cancelar esta solicitação, sem login. O intervalo fica bloqueado enquanto estiver pendente.</p></section>
    <section class="card"><h4>Não foi possível concluir</h4><p>Revise as informações e tente novamente.</p></section>
  </div>
</main>""")

TELAS["07-minhas-reservas"] = ("Acompanhar solicitação", simples(
    NAV_PROF, "Sua solicitação", "Acesso pelo link individual recebido no comprovante, sem conta.", "calendario",
    [("Status de exemplo: pendente", "Sala de Reunião B · 09:15 às 10:45 · Aguardando análise da equipe.", []),
     ("Acompanhamento e cancelamento", "O link individual autoriza acesso somente a esta solicitação. O protocolo sozinho não permite consultar dados ou cancelar. Não compartilhe o link.", [])]))
TELAS["08-historico"] = ("Histórico", simples(
    NAV_PROF, "Histórico · referência futura", "Tela de referência para uma fase com autenticação do solicitante.", "documento",
    [("Reservas recentes", "Aqui você poderá consultar os espaços utilizados e os detalhes de cada agendamento.",
      ["Estações de trabalho", "Sala de podcast", "Mesa colaborativa"]),
     ("Acesso em protótipo", "Os registros apresentados não estão conectados a uma conta real.", [])]))

# O menu administrativo corrige a divergência D1 registrada no Framer.
agenda = [("Marina Silva", "14:00", "Sala de Reunião B"), ("Lucas Azevedo", "14:30", "Estação PC 04"),
          ("Rafael Costa", "15:00", "Sala de podcast")]
itens = "".join(
    f'<div class="item"><div class="det"><div class="quem"><b>{n}</b><em>{h}</em></div><span class="rec">{e}</span>'
    f'<span class="status">Confirmada</span></div><span class="btn">Ver reserva</span></div>' for n, h, e in agenda)
TELAS["09-admin-visao-geral"] = ("Visão geral", topnav(NAV_ADMIN) + f"""
<div class="app admin">
  {lateral(["Visão Geral", "Prioridade interna", "Reservas", "Espaços", "Relatórios", "Configurações"], "Visão Geral", admin=True)}
  <main class="conteudo adm">
    <div class="cab-painel"><h2>Visão Geral</h2>
      <div class="estado"><span class="data">Sáb, 7 nov 2026</span><span class="pill-ok"><i></i>Exemplo</span></div></div>
    <div class="kpis">
      <div class="card"><small>Salas ocupadas</small><b>8 / 10</b><span>Ocupação atual</span></div>
      <div class="card"><small>Estações reservadas</small><b>12 / 20</b><span>Estações individuais</span></div>
      <div class="card"><small>No-shows</small><b class="vermelho">2</b><span>Requerem acompanhamento</span></div>
      <div class="card"><small>Solicitações pendentes</small><b class="azul">1</b><span>Aguardando decisão manual</span></div>
    </div>
    <div class="agenda-prior">
      <section class="card agenda">
        <div class="cab"><h4>Agenda do Dia</h4><span>Hoje · 07 NOV</span></div>
        <p class="desc">Horário, espaço e profissional em uma leitura rápida.</p>
        <div class="itens">{itens}</div>
      </section>
      <section class="card prior">
        <div class="cab"><h4>Análise de solicitações</h4><span class="cont">1</span></div>
        <p>Solicitações pendentes bloqueiam o intervalo até a decisão manual da equipe.</p>
        <div class="caixa"><img src="../assets/grafico.svg" alt=""></div>
      </section>
    </div>
    <p class="rodape-demo">Dados de demonstração · Crea Tec HUB</p>
  </main>
</div>""")

admin = [
    ("11-admin-prioridade", "Prioridade interna · fase futura", "Referência visual fora do MVP; regras de prioridade ainda não são implementadas.", "aperto-de-maos",
     [("Regra atual", "Reservas pendentes e confirmadas bloqueiam o espaço no intervalo solicitado. Uma nova reserva não substitui automaticamente outra.", []),
      ("Evolução futura", "Qualquer política de prioridade depende de definição e de decisão explícita da equipe.", [])]),
    ("12-admin-reservas", "Reservas do HUB", "Visão de agenda para a recepção e gestão.", "calendario",
     [("Agenda de hoje", "Acompanhe os agendamentos por horário e tipo de espaço.",
       ["14:00 · Sala de reunião · Profissional", "14:30 · Estação individual · Profissional", "15:00 · Sala de podcast · Profissional"]),
      ("Análise manual", "Confirmar ou recusar solicitações pendentes. Não sobrepor reservas pendentes ou confirmadas nem substituí-las automaticamente.", [])]),
    ("13-admin-espacos", "Espaços disponíveis", "Gerencie as categorias e a capacidade dos espaços reserváveis.", "mulher-mesa",
     [("Estações individuais", "Até 20 posições com computador. Cada reserva ocupa uma estação individual.", []),
      ("Sala de podcast", "Sala exclusiva para gravação e produção de conteúdo.", []),
      ("Mesa colaborativa", "Reservada para grupos; não é indicada para uso individual.", [])]),
    ("14-admin-relatorios", "Relatórios · fase futura", "Referência visual de indicadores fora do MVP atual.", "grafico",
     [("Ocupação", "Acompanhe a utilização das salas e estações por período.", []),
      ("Reservas", "Consulte volume de agendamentos e conflitos de horário.", [])]),
    ("15-admin-configuracoes", "Configurações do HUB", "Preferências administrativas do sistema.", "documento",
     [("Regras de reservas", "Defina disponibilidade, horários e condições de uso dos espaços.", []),
      ("Acesso da equipe", "Área demonstrativa para permissões da recepção e gestão.", [])]),
]
for slug, titulo, sub, ilu, cards_ in admin:
    TELAS[slug] = (titulo, simples(NAV_ADMIN, titulo, sub, ilu, cards_))

# /404
TELAS["16-erro-404"] = ("Página não encontrada", topnav(NAV_PROF) + f"""
<main class="e404">
  <div class="msg">
    <div class="eyebrow">Crea Tec HUB · Erro 404</div>
    <h1>Este espaço não está disponível.</h1>
    <p>A página pode ter sido movida ou o endereço está incorreto. Volte ao painel e encontre o espaço certo para o seu próximo projeto.</p>
    <span class="btn">Voltar ao painel</span>
  </div>
  <div class="ilu">{img("binoculo-404", "Pessoa procurando com binóculo ao lado do número 404")}</div>
</main>""")

for slug, (titulo, corpo) in TELAS.items():
    (SAIDA / f"{slug}.html").write_text(pagina(titulo, corpo), encoding="utf-8")
print(len(TELAS), "telas geradas em", SAIDA)
