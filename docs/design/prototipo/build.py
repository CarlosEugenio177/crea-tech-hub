"""Gera as telas do Crea Tec HUB em HTML (uma página por tela).

Uso: python3 build.py  -> escreve os arquivos em telas/
"""
from pathlib import Path

RAIZ = Path(__file__).parent
SAIDA = RAIZ / "telas"
SAIDA.mkdir(exist_ok=True)

NAV_PROF = ["Meu painel", "Nova reserva", "Minhas reservas", "Histórico"]
NAV_ADMIN = ["Visão geral", "Prioridade", "Reservas", "Espaços", "Relatórios", "Configurações"]


def topnav(itens):
    lis = "".join(f"<li>{i}</li>" for i in itens)
    return f'<header class="topnav"><div class="marca">Crea Tec HUB</div><ul>{lis}</ul></header>'


def pagina(titulo, corpo):
    return f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<title>{titulo} — Crea Tec HUB</title>
<link rel="stylesheet" href="../css/crea-tec.css">
</head>
<body>
{corpo}
</body>
</html>
"""


def ilu(nome, alt=""):
    return f'<img src="../assets/{nome}.svg" alt="{alt}">'


def lateral(itens, ativo, secao=None, rodape=("Conta profissional", "Crea Tec HUB"), classe=""):
    links = "".join(f'<a class="{"on" if i == ativo else ""}">{i}</a>' for i in itens)
    sec = f'<div class="secao">{secao}</div>' if secao else ""
    return f"""<aside class="lateral {classe}">
  <div class="logo"><i></i><b>Crea Tec HUB</b></div>
  {sec}
  <nav>{links}</nav>
  <div class="rodape"><i></i><div><b>{rodape[0]}</b><span>{rodape[1]}</span></div></div>
</aside>"""


def simples(nav, titulo, sub, img, cards):
    blocos = ""
    for h, p, extras in cards:
        lis = "".join(f"<li>{x}</li>" for x in extras)
        blocos += f'<div class="card"><h3>{h}</h3><p>{p}</p>{lis}</div>'
    return topnav(nav) + f"""
<main class="pagina">
  <div class="cab">
    <div><div class="eyebrow">Crea Tec HUB</div><h1>{titulo}</h1><p class="sub">{sub}</p></div>
    {ilu(img)}
  </div>
  <div class="lista">{blocos}</div>
</main>"""


TELAS = {}

# 01 — Home / cadastro
TELAS["01-home-cadastro"] = ("Cadastro", topnav(NAV_PROF) + f"""
<main class="home">
  <section class="cabecalho">
    <div class="eyebrow">Crea Tec HUB</div>
    <h1>Seu acesso ao HUB começa aqui</h1>
    <p class="sub">Cadastro rápido para profissionais: nome, telefone e registro CREA.</p>
    <form class="card form">
      <div class="campo"><label>Nome completo</label><div class="input">Seu nome</div></div>
      <div class="campo"><label>Telefone</label><div class="input">(00) 00000-0000</div></div>
      <div class="campo"><label>Registro CREA</label><div class="input">Número do registro</div></div>
      <p class="aviso">Usamos esses dados somente para identificar sua conta e validar seu acesso profissional.</p>
      <div class="btn lg">Continuar para o painel</div>
      <p class="nota">Protótipo de interface — o envio do cadastro e a validação CREA ainda não estão conectados.</p>
    </form>
  </section>
  <div class="ilu-home">{ilu("mulher-mesa", "Profissional trabalhando em uma mesa")}</div>
</main>""")

# 02 — Meu painel
TELAS["02-meu-painel"] = ("Meu painel", topnav(NAV_PROF) + f"""
<div class="shell">
  {lateral(["Meu Painel", "Nova Reserva", "Minhas Reservas", "Histórico"], "Meu Painel")}
  <main class="conteudo">
    <div class="topo"><h2>Meu Painel</h2><span class="data">Sábado, 7 de novembro de 2026</span></div>
    <section class="card hero">
      <div>
        <div class="eyebrow">Seu espaço, sob seu controle</div>
        <h3>Olá, Engenheiro. Sua próxima estação de trabalho te espera.</h3>
        <p>Encontre espaços e recursos para manter seus projetos em movimento.</p>
        <span class="btn">Nova reserva</span>
      </div>
      <div style="height:170px;display:flex;justify-content:center">{ilu("mulher-mesa")}</div>
    </section>
    <div class="duas">
      <section class="card">
        <div class="linha-topo"><h4>Minhas Reservas</h4><span class="badge azul">1 próxima</span></div>
        <div class="proxima">
          <div><div class="eyebrow">Próxima reserva</div><b>Sala de Reunião B</b><small>Reserva confirmada</small></div>
          <strong>Hoje, 14h</strong>
        </div>
        <div class="divisor"></div>
        <div class="espacos">
          <div><b>Espaços disponíveis</b><small>Estações individuais, sala de podcast e mesa colaborativa.</small></div>
          <span class="link">Ver</span>
        </div>
        <p class="link" style="margin-top:32px;font-size:11px">Ver histórico de reservas</p>
      </section>
      <section class="card cta">
        <div class="eyebrow">Pronto para o próximo projeto?</div>
        <div style="height:150px">{ilu("calendario")}</div>
        <h4>Agendar um espaço</h4>
        <p>Escolha entre estações com computador, sala de podcast ou mesa para grupos.</p>
        <span class="btn sm">Ver espaços</span>
      </section>
    </div>
  </main>
</div>""")

# 04 — Nova reserva, etapa 1
espacos = [
    ("mulher-mesa", "Estação individual", "Seu posto com computador, reservado só para você.", "1 PESSOA · COMPUTADOR"),
    ("podcast", "Sala de podcast", "Um espaço dedicado à gravação de podcast.", "SALA EXCLUSIVA"),
    ("aperto-de-maos", "Mesa colaborativa", "Uma mesa ampla para encontros e trabalho em equipe.", "SOMENTE PARA GRUPOS"),
]
cards = "".join(
    f'<div class="card"><div class="ilu">{ilu(i)}</div><h3>{t}</h3><p>{d}</p>'
    f'<div class="tag">{tag}</div><div class="btn">Ver horários</div></div>'
    for i, t, d, tag in espacos
)
TELAS["04-nova-reserva-etapa1-espacos"] = ("Nova reserva", topnav(NAV_PROF) + f"""
<main class="etapa1">
  <div class="cab">
    <div class="eyebrow">Crea Tec HUB</div>
    <h1>Qual espaço acompanha seu próximo projeto?</h1>
    <p class="sub">Escolha o ambiente certo e veja as opções de horário.</p>
  </div>
  <div class="espacos-grid">{cards}</div>
</main>""")

# 05 — etapa 2
TELAS["05-nova-reserva-etapa2-horario"] = ("Escolha uma data e um horário", topnav(NAV_PROF) + """
<main class="etapa">
  <div class="eyebrow">Nova reserva · Etapa 2 de 3</div>
  <h2>Escolha uma data e um horário</h2>
  <p class="sub">Prévia da experiência de reserva. Os horários abaixo são exemplos.</p>
  <div class="card">
    <h4>Data</h4>
    <div class="chips"><span class="chip on">Seg · 09</span><span class="chip">Ter · 10</span><span class="chip">Qua · 11</span></div>
    <h4>Horário</h4>
    <div class="chips"><span class="chip">09:00</span><span class="chip">10:00</span><span class="chip">14:00</span><span class="chip">15:00</span></div>
    <p class="nota">Seleção visual de exemplo — nenhuma reserva será criada.</p>
  </div>
  <div class="acoes"><span class="btn claro">Voltar aos espaços</span><span class="btn">Revisar reserva</span></div>
</main>""")

# 06 — etapa 3
TELAS["06-nova-reserva-etapa3-revisao"] = ("Revise sua reserva", topnav(NAV_PROF) + """
<main class="etapa">
  <div class="eyebrow">Nova reserva · Etapa 3 de 3</div>
  <h2>Revise sua reserva</h2>
  <p class="sub">Uma prévia do resumo antes de confirmar.</p>
  <div class="card resumo">
    <h4>Detalhes da reserva</h4>
    <p>Espaço, data e horário aparecerão aqui após a seleção.</p>
    <small>A mesa colaborativa é destinada a grupos.</small>
  </div>
  <div class="acoes"><span class="btn claro">Voltar aos horários</span></div>
  <h3 class="estados">Estados do fluxo · exemplos</h3>
  <div class="estados-grid">
    <div class="card"><h4>Sem horários disponíveis</h4><p>Tente outra data ou escolha outro espaço.</p></div>
    <div class="card"><h4>Reserva confirmada</h4><p>Os detalhes da reserva aparecerão em Minhas reservas.</p></div>
    <div class="card"><h4>Não foi possível concluir</h4><p>Revise as informações e tente novamente.</p></div>
  </div>
</main>""")

# 07 / 08 — páginas simples do profissional
TELAS["07-minhas-reservas"] = ("Minhas reservas", simples(
    NAV_PROF, "Minhas reservas", "Acompanhe seus próximos horários e os espaços que você reservou.", "calendario",
    [("Próximo agendamento", "Sala de Reunião B · Hoje, 14h · Reserva confirmada.", []),
     ("Gerencie seu tempo", "Confira o horário e o espaço antes de chegar. Se precisar, escolha outro horário disponível em Nova Reserva.", [])]))
TELAS["08-historico"] = ("Histórico", simples(
    NAV_PROF, "Seu histórico", "Reservas anteriores do seu perfil.", "documento",
    [("Reservas recentes", "Aqui você poderá consultar os espaços utilizados e os detalhes de cada agendamento.",
      ["Estações de trabalho", "Sala de podcast", "Mesa colaborativa"]),
     ("Acesso em protótipo", "Os registros apresentados não estão conectados a uma conta real.", [])]))

# 09 — admin visão geral
agenda = [("Marina Silva", "14:00", "Sala de Reunião B"), ("Lucas Azevedo", "14:30", "Estação PC 04"),
          ("Rafael Costa", "15:00", "Sala de podcast")]
itens = "".join(
    f'<div class="item"><b>{n}<em>{h}</em></b><small>{e}</small><span class="badge azul">Confirmada</span><br>'
    f'<span class="btn sm">Ver reserva</span></div>' for n, h, e in agenda)
TELAS["09-admin-visao-geral"] = ("Visão geral", topnav(NAV_PROF) + f"""
<div class="shell">
  {lateral(["Visão Geral", "Prioridade interna", "Reservas", "Espaços", "Relatórios", "Configurações"], "Visão Geral",
           secao="Administração", rodape=("Recepção e Gestão", "Crea Tec HUB"), classe="admin")}
  <main class="conteudo">
    <div class="topo"><h2>Visão Geral</h2><span class="data">Sáb, 7 nov 2026 <span class="badge verde">Exemplo</span></span></div>
    <div class="kpis">
      <div class="card"><small>Salas ocupadas</small><b>8 / 10</b><span>Ocupação atual</span></div>
      <div class="card"><small>Estações reservadas</small><b>12 / 20</b><span>Estações individuais</span></div>
      <div class="card"><small>No-shows</small><b class="vermelho">2</b><span>Requerem acompanhamento</span></div>
      <div class="card"><small>Conflitos de agenda</small><b class="azul">1</b><span>Prioridade interna aplicada</span></div>
    </div>
    <div class="duas admin">
      <section class="card agenda">
        <div class="linha-topo"><h4>Agenda do Dia</h4><span class="hoje">Hoje · 07 NOV</span></div>
        <p class="desc">Horário, espaço e profissional em uma leitura rápida.</p>
        {itens}
      </section>
      <section class="card prioridade">
        <div class="linha-topo"><h4>Prioridade de reserva</h4><span class="badge azul">1</span></div>
        <p>A prioridade interna se aplica apenas quando há conflito de horário.</p>
        <div class="tracejado"><img src="../assets/grafico.svg" alt="" style="height:330px;margin-bottom:-64px"></div>
      </section>
    </div>
    <p class="rodape-demo">Dados de demonstração · Crea Tec HUB</p>
  </main>
</div>""")

# 11–15 — páginas simples do admin
admin = [
    ("11-admin-prioridade", "Prioridade interna", "Reservas da equipe interna têm prioridade somente quando há conflito de horário.", "aperto-de-maos",
     [("Regra de prioridade", "Sem conflito, as reservas seguem normalmente. Se houver sobreposição, o agendamento interno tem prioridade.", []),
      ("Conflitos para análise", "A lista abaixo é demonstrativa. Em produção, cada conflito deve mostrar espaço, horário e responsáveis para a equipe decidir.", [])]),
    ("12-admin-reservas", "Reservas do HUB", "Visão de agenda para a recepção e gestão.", "calendario",
     [("Agenda de hoje", "Acompanhe os agendamentos por horário e tipo de espaço.",
       ["14:00 · Sala de reunião · Profissional", "14:30 · Estação individual · Profissional", "15:00 · Sala de podcast · Profissional"]),
      ("Prioridade", "Reservas internas só substituem reservas de usuário final quando houver conflito.", [])]),
    ("13-admin-espacos", "Espaços disponíveis", "Gerencie as categorias e a capacidade dos espaços reserváveis.", "mulher-mesa",
     [("Estações individuais", "Até 20 posições com computador. Cada reserva ocupa uma estação individual.", []),
      ("Sala de podcast", "Sala exclusiva para gravação e produção de conteúdo.", []),
      ("Mesa colaborativa", "Reservada para grupos; não é indicada para uso individual.", [])]),
    ("14-admin-relatorios", "Relatórios do HUB", "Indicadores para acompanhar ocupação e uso dos espaços.", "grafico",
     [("Ocupação", "Acompanhe a utilização das salas e estações por período.", []),
      ("Reservas", "Consulte volume de agendamentos e conflitos de horário.", [])]),
    ("15-admin-configuracoes", "Configurações do HUB", "Preferências administrativas do sistema.", "documento",
     [("Regras de reservas", "Defina disponibilidade, horários e condições de uso dos espaços.", []),
      ("Acesso da equipe", "Área demonstrativa para permissões da recepção e gestão.", [])]),
]
for slug, titulo, sub, img, cards_ in admin:
    TELAS[slug] = (titulo, simples(NAV_ADMIN, titulo, sub, img, cards_))

for slug, (titulo, corpo) in TELAS.items():
    (SAIDA / f"{slug}.html").write_text(pagina(titulo, corpo), encoding="utf-8")
print(len(TELAS), "telas geradas em", SAIDA)
