"""Monta o canvas geral: todas as telas lado a lado, agrupadas por fluxo."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

RAIZ = Path(__file__).parent
F = "/usr/share/fonts/opentype/inter/"
f_tit = ImageFont.truetype(F + "Inter-Bold.otf", 56)
f_sec = ImageFont.truetype(F + "Inter-SemiBold.otf", 34)
f_lab = ImageFont.truetype(F + "Inter-Medium.otf", 24)

GRUPOS = [
    ("Fluxo do profissional", [
        ("01-home-cadastro", "Home · cadastro"), ("02-meu-painel", "/painel"),
        ("04-nova-reserva-etapa1-espacos", "/reservas · etapa 1"), ("05-nova-reserva-etapa2-horario", "/reservar · etapa 2"),
        ("06-nova-reserva-etapa3-revisao", "/reservar · etapa 3"), ("07-minhas-reservas", "/minhas-reservas"),
        ("08-historico", "/historico")]),
    ("Administração", [
        ("09-admin-visao-geral", "/admin · visão geral"), ("11-admin-prioridade", "/admin/prioridade"),
        ("12-admin-reservas", "/admin/reservas"), ("13-admin-espacos", "/admin/espacos"),
        ("14-admin-relatorios", "/admin/relatorios"), ("15-admin-configuracoes", "/admin/configuracoes")]),
]
ESC = 0.5            # telas a 1200px (2x) -> 1200px no canvas
COLS, GAP, MARG, LAB = 4, 80, 120, 50
W_TELA = 1200

linhas = []
y = MARG + 120
for nome, telas in GRUPOS:
    y += 70
    blocos = []
    for i in range(0, len(telas), COLS):
        fila = telas[i:i + COLS]
        alt = max(int(Image.open(RAIZ.parent / "telas" / f"{t}.png").height * ESC) for t, _ in fila)
        blocos.append((y, fila)); y += LAB + alt + GAP
    linhas.append((nome, blocos))
W = MARG * 2 + COLS * W_TELA + (COLS - 1) * GAP
H = y + MARG - GAP
cv = Image.new("RGB", (W, H), "#ececec")
d = ImageDraw.Draw(cv)
d.text((MARG, MARG), "Crea Tec HUB — telas do protótipo (recriadas do Framer)", font=f_tit, fill="#111827")
for nome, blocos in linhas:
    d.text((MARG, blocos[0][0] - 70), nome, font=f_sec, fill="#64748b")
    for by, fila in blocos:
        for c, (t, rot) in enumerate(fila):
            x = MARG + c * (W_TELA + GAP)
            im = Image.open(RAIZ.parent / "telas" / f"{t}.png").convert("RGB")
            im = im.resize((int(im.width * ESC), int(im.height * ESC)), Image.LANCZOS)
            d.text((x, by), rot, font=f_lab, fill="#677281")
            cv.paste(im, (x, by + LAB))
            d.rectangle([x - 1, by + LAB - 1, x + im.width, by + LAB + im.height], outline="#d4dce5")
cv.save(RAIZ.parent / "telas" / "00-canvas.png", optimize=True)
print(cv.size)
