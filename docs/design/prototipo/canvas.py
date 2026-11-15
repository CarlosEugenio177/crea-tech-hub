"""Monta o canvas geral: todas as telas lado a lado, agrupadas por fluxo."""
from pathlib import Path
import os
from PIL import Image, ImageDraw, ImageFont

RAIZ = Path(__file__).parent

def fonte(tamanho, peso):
    diretorios = [
        Path(os.environ.get("PROTOTYPE_FONT_DIR", "/usr/share/fonts/opentype/inter")),
        Path("/usr/share/fonts/truetype/dejavu"),
        Path(os.environ.get("WINDIR", "C:/Windows")) / "Fonts",
        Path("/System/Library/Fonts/Supplemental"),
    ]
    nomes = [f"Inter-{peso}.otf", f"Inter-{peso}.ttf", "DejaVuSans.ttf", "arial.ttf", "Arial.ttf"]
    for nome in nomes:
        for diretorio in diretorios:
            try:
                return ImageFont.truetype(str(diretorio / nome), tamanho)
            except OSError:
                continue
    return ImageFont.load_default(size=tamanho)


f_tit = fonte(56, "Bold")
f_sec = fonte(34, "SemiBold")
f_lab = fonte(24, "Medium")

GRUPOS = [
    ("Fluxo público e referências futuras", [
        ("01-home-cadastro", "Home · solicitação sem conta"), ("02-meu-painel", "/painel · futuro"),
        ("04-nova-reserva-etapa1-espacos", "/reservas · etapa 1"), ("05-nova-reserva-etapa2-horario", "/reservar/horario · etapa 2"),
        ("06-nova-reserva-etapa3-revisao", "/reservar/revisao · etapa 3"), ("07-minhas-reservas", "/minhas-reservas"),
        ("08-historico", "/historico · futuro")]),
    ("Administração", [
        ("09-admin-visao-geral", "/admin · visão geral"), ("11-admin-prioridade", "/admin/prioridade"),
        ("12-admin-reservas", "/admin/reservas"), ("13-admin-espacos", "/admin/espacos"),
        ("14-admin-relatorios", "/admin/relatorios"), ("15-admin-configuracoes", "/admin/configuracoes")]),
    ("Outras páginas", [("16-erro-404", "/404")]),
]
ESC = 0.5
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
