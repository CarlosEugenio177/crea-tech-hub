"""Renderiza cada tela em PNG @2x (1200px de largura, página inteira)."""
import sys
from pathlib import Path
from playwright.sync_api import sync_playwright

RAIZ = Path(__file__).parent
SAIDA = RAIZ.parent / "telas"
SAIDA.mkdir(exist_ok=True)
alvos = sys.argv[1:] or [p.stem for p in sorted((RAIZ / "telas").glob("*.html"))]

with sync_playwright() as pw:
    chrome = Path("/usr/bin/google-chrome")
    nav = pw.chromium.launch(executable_path=str(chrome) if chrome.exists() else None)
    pg = nav.new_page(viewport={"width": 1200, "height": 900}, device_scale_factor=2)
    for nome in alvos:
        pg.goto((RAIZ / "telas" / f"{nome}.html").as_uri())
        pg.wait_for_load_state("networkidle")
        pg.screenshot(path=str(SAIDA / f"{nome}.png"), full_page=True)
        print("ok", nome)
    nav.close()
