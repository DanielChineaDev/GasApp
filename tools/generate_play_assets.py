# -*- coding: utf-8 -*-
"""Genera los recursos gráficos de Google Play para GasApp:
- Capturas de móvil (1080x1920) y tablet (2560x1600)
- Feature graphic (1024x500)
- Gráfico de funciones (infografía)
Usa capturas reales de la app + marcos y fondos de marca.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SH = os.path.join(ROOT, "landing", "assets")
LOGO = os.path.join(SH, "logo.png")
OUT = os.path.join(ROOT, "play-assets")
os.makedirs(os.path.join(OUT, "phone"), exist_ok=True)
os.makedirs(os.path.join(OUT, "tablet"), exist_ok=True)

SCREENS = {
    "inicio": os.path.join(SH, "screen-inicio.png"),
    "mapa": os.path.join(SH, "screen-mapa.png"),
    "favoritas": os.path.join(SH, "screen-favoritas.png"),
}

# ── Paleta ────────────────────────────────────────────────────────────────
BLUE   = (25, 118, 210)
BLUE_D = (18, 86, 196)
BLUE_L = (46, 144, 232)
SKY    = (224, 240, 255)
SKY2   = (198, 226, 255)
NAVY   = (11, 27, 42)
ORANGE = (245, 124, 0)
WHITE  = (255, 255, 255)

FB = "C:/Windows/Fonts/seguibl.ttf"   # Segoe UI Black
FH = "C:/Windows/Fonts/segoeuib.ttf"  # Segoe UI Bold
FR = "C:/Windows/Fonts/segoeui.ttf"   # Segoe UI
FE = "C:/Windows/Fonts/seguiemj.ttf"  # emoji color

def font(path, size): return ImageFont.truetype(path, size)

# ── Utilidades ──────────────────────────────────────────────────────────────
def gradient(size, c1, c2, vertical=True):
    w, h = size
    base = Image.new("RGB", size, c1)
    top = Image.new("RGB", size, c2)
    mask = Image.new("L", size)
    md = mask.load()
    n = h if vertical else w
    for i in range(n):
        v = int(255 * i / max(1, n - 1))
        for j in range(w if vertical else h):
            if vertical: md[j, i] = v
            else: md[i, j] = v
    base.paste(top, (0, 0), mask)
    return base.convert("RGBA")

def rounded_mask(size, radius):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0]-1, size[1]-1], radius=radius, fill=255)
    return m

def round_image(img, radius):
    img = img.convert("RGBA")
    img.putalpha(rounded_mask(img.size, radius))
    return img

def phone_frame(screen_path, target_w):
    """Marco de teléfono oscuro con la captura y sombra. Devuelve RGBA."""
    shot = Image.open(screen_path).convert("RGBA")
    ratio = shot.height / shot.width
    inner_w = target_w
    inner_h = int(inner_w * ratio)
    shot = shot.resize((inner_w, inner_h), Image.LANCZOS)
    rad = int(inner_w * 0.085)
    shot = round_image(shot, rad)

    pad = max(10, int(inner_w * 0.035))
    fw, fh = inner_w + pad * 2, inner_h + pad * 2
    frame = Image.new("RGBA", (fw, fh), (0, 0, 0, 0))
    body = Image.new("RGBA", (fw, fh), (12, 20, 32, 255))
    body.putalpha(rounded_mask((fw, fh), rad + pad))
    frame.alpha_composite(body)
    frame.alpha_composite(shot, (pad, pad))

    # sombra
    margin = int(fw * 0.22)
    canvas = Image.new("RGBA", (fw + margin * 2, fh + margin * 2), (0, 0, 0, 0))
    shadow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    sd = Image.new("RGBA", (fw, fh), (0, 0, 0, 150))
    sd.putalpha(Image.eval(rounded_mask((fw, fh), rad + pad), lambda v: int(v * 0.6)))
    shadow.alpha_composite(sd, (margin, margin + int(margin * 0.25)))
    shadow = shadow.filter(ImageFilter.GaussianBlur(margin * 0.4))
    canvas.alpha_composite(shadow)
    canvas.alpha_composite(frame, (margin, margin))
    return canvas

def draw_multiline_center(d, cx, y, lines, fnt, fill, leading=1.12):
    asc, desc = fnt.getmetrics()
    lh = int((asc + desc) * leading)
    for i, ln in enumerate(lines):
        d.text((cx, y + i * lh), ln, font=fnt, fill=fill, anchor="ma")
    return y + len(lines) * lh

def text_w(d, s, fnt):
    return d.textbbox((0, 0), s, font=fnt)[2]

def wrap(d, text, fnt, max_w):
    words = text.split()
    lines, cur = [], ""
    for w in words:
        t = (cur + " " + w).strip()
        if text_w(d, t, fnt) <= max_w: cur = t
        else: lines.append(cur); cur = w
    if cur: lines.append(cur)
    return lines

def emoji_badge(emoji, size, bg):
    """Círculo de color con un emoji centrado."""
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([0, 0, size-1, size-1], fill=bg)
    ef = font(FE, int(size * 0.5))
    # render emoji en su propia capa y centrar
    el = Image.new("RGBA", (int(size*0.7), int(size*0.7)), (0,0,0,0))
    ed = ImageDraw.Draw(el)
    ed.text((el.size[0]//2, el.size[1]//2), emoji, font=ef, embedded_color=True, anchor="mm")
    im.alpha_composite(el, ((size-el.size[0])//2, (size-el.size[1])//2))
    return im

# ── 1. CAPTURAS DE MÓVIL (1080x1920) ────────────────────────────────────────
PHONE = [
    ("inicio",    "Encuentra la gasolina\nmás barata cerca de ti", "Precios oficiales actualizados a diario", "dark"),
    ("mapa",      "Todo el mapa de\nprecios en tu mano",           "Mira las gasolineras y su precio al instante", "light"),
    ("favoritas", "Tus favoritas,\nsiempre contigo",               "Guárdalas y sincronízalas entre dispositivos", "dark"),
    ("inicio",    "Filtra y ordena\ncomo tú quieras",              "Por combustible, distancia o ahorro", "light"),
    ("mapa",      "Datos oficiales,\ntoda España",                 "Gratis · Sin registro obligatorio", "dark"),
]

def make_phone(idx, screen, title, sub, mode):
    W, H = 1080, 1920
    if mode == "dark":
        bg = gradient((W, H), BLUE_L, BLUE_D)
        tcol, scol = WHITE, (225, 238, 252)
    else:
        bg = gradient((W, H), SKY, SKY2)
        tcol, scol = NAVY, (60, 84, 110)
    d = ImageDraw.Draw(bg)

    # textos arriba
    tf = font(FB, 78)
    cx = W // 2
    y = 150
    lines = title.split("\n")
    y = draw_multiline_center(d, cx, y, lines, tf, tcol, leading=1.05)
    sf = font(FR, 40)
    d.text((cx, y + 26), sub, font=sf, fill=scol, anchor="ma")

    # teléfono
    ph = phone_frame(SCREENS[screen], 560)
    px = (W - ph.size[0]) // 2
    py = 520
    bg.alpha_composite(ph, (px, py))

    bg.convert("RGB").save(os.path.join(OUT, "phone", f"phone-{idx}-{screen}.png"), quality=95)
    print("phone", idx, screen)

for i, (s, t, sub, m) in enumerate(PHONE, 1):
    make_phone(i, s, t, sub, m)

# ── 2. CAPTURAS DE TABLET (2560x1600) ───────────────────────────────────────
TABLET = [
    ("Ahorra en cada repostaje",
     ["Las gasolineras más baratas a tu alrededor",
      "Mapa de precios en tiempo real",
      "Filtra por combustible, distancia y ahorro"],
     ["mapa", "inicio"], "dark"),
    ("Todo bajo control",
     ["Guarda y sincroniza tus favoritas",
      "Historial de precios y alertas de bajada",
      "Estadísticas de gasto y consumo real"],
     ["favoritas", "inicio"], "light"),
    ("Gratis y con datos oficiales",
     ["Precios oficiales de toda España",
      "Sin registro obligatorio",
      "Diseño limpio, rápido y fácil de usar"],
     ["inicio", "mapa"], "dark"),
]

def make_tablet(idx, title, bullets, screens, mode):
    W, H = 2560, 1600
    if mode == "dark":
        bg = gradient((W, H), BLUE_L, BLUE_D); tcol, scol = WHITE, (224, 238, 252)
    else:
        bg = gradient((W, H), SKY, SKY2); tcol, scol = NAVY, (60, 84, 110)
    d = ImageDraw.Draw(bg)

    # Lado izquierdo: logo + título + bullets
    lx = 150
    logo = Image.open(LOGO).convert("RGBA").resize((140, 140), Image.LANCZOS)
    bg.alpha_composite(logo, (lx, 170))
    d.text((lx + 165, 200), "GasApp", font=font(FB, 96), fill=tcol)

    tf = font(FB, 104)
    lines = wrap(d, title, tf, 1150)
    y = 420
    for ln in lines:
        d.text((lx, y), ln, font=tf, fill=tcol); y += 120
    y += 40
    bf = font(FR, 50)
    for b in bullets:
        # punto naranja
        d.ellipse([lx, y + 18, lx + 22, y + 40], fill=ORANGE)
        for j, ln in enumerate(wrap(d, b, bf, 1050)):
            d.text((lx + 50, y + j * 64), ln, font=bf, fill=scol)
        y += 64 * max(1, len(wrap(d, b, bf, 1050))) + 34

    # Lado derecho: dos teléfonos solapados
    ph1 = phone_frame(SCREENS[screens[0]], 540)
    ph2 = phone_frame(SCREENS[screens[1]], 540)
    rx = 1520
    bg.alpha_composite(ph2, (rx + 360, 250))
    bg.alpha_composite(ph1, (rx, 120))

    bg.convert("RGB").save(os.path.join(OUT, "tablet", f"tablet-{idx}.png"), quality=95)
    print("tablet", idx)

for i, (t, b, s, m) in enumerate(TABLET, 1):
    make_tablet(i, t, b, s, m)

# ── 3. FEATURE GRAPHIC (1024x500) ───────────────────────────────────────────
def make_feature():
    W, H = 1024, 500
    bg = gradient((W, H), BLUE_L, BLUE_D)
    d = ImageDraw.Draw(bg)
    # gota decorativa grande translúcida
    drop = Image.open(LOGO).convert("RGBA")
    r, g, b, a = drop.split()
    from PIL import ImageChops
    m = ImageChops.darker(ImageChops.darker(r, g), b).point(lambda v: 0 if v < 60 else min(255, int((v-60)*255/175)))
    white = Image.new("RGBA", drop.size, (255, 255, 255, 0)); white.putalpha(ImageChops.multiply(m, a))
    big = white.resize((520, 520), Image.LANCZOS)
    faded = Image.new("RGBA", big.size, (0, 0, 0, 0))
    faded = Image.blend(faded, big, 0.12)
    bg.alpha_composite(faded, (W - 360, -40))

    logo = Image.open(LOGO).convert("RGBA").resize((118, 118), Image.LANCZOS)
    bg.alpha_composite(logo, (70, 78))
    d.text((205, 94), "GasApp", font=font(FB, 100), fill=WHITE)

    # Titular con autoajuste para no chocar con el teléfono (máx ancho 640)
    maxw = 640
    def fit(lines, hi=64, lo=40):
        for s in range(hi, lo - 1, -2):
            f = font(FB, s)
            if all(text_w(d, ln, f) <= maxw for ln in lines):
                return f
        return font(FB, lo)
    l1, l2 = "La gasolina más", "barata cerca de ti"
    hf = fit([l1, l2])
    d.text((74, 248), l1, font=hf, fill=WHITE)
    d.text((74, 248 + hf.size + 8), l2, font=hf, fill=(255, 214, 153))
    d.text((76, 408), "Precios oficiales · Toda España · Gratis", font=font(FH, 32), fill=(222, 236, 252))

    # teléfono a la derecha
    ph = phone_frame(SCREENS["inicio"], 250)
    bg.alpha_composite(ph, (W - ph.size[0] + 50, 158))
    bg.convert("RGB").save(os.path.join(OUT, "feature-graphic-1024x500.png"), quality=95)
    print("feature graphic")

make_feature()

# ── 4. GRÁFICO DE FUNCIONES (infografía) ────────────────────────────────────
def make_features_infographic():
    W, H = 1400, 1500
    bg = gradient((W, H), (247, 250, 254), (224, 238, 255))
    d = ImageDraw.Draw(bg)

    logo = Image.open(LOGO).convert("RGBA").resize((92, 92), Image.LANCZOS)
    bg.alpha_composite(logo, (W // 2 - 200, 70))
    d.text((W // 2 - 95, 92), "GasApp", font=font(FB, 70), fill=NAVY)
    d.text((W // 2, 200), "Todo lo que puedes hacer", font=font(FB, 58), fill=NAVY, anchor="ma")
    d.text((W // 2, 278), "Una sola app para ahorrar en cada repostaje", font=font(FR, 36), fill=(80, 100, 120), anchor="ma")

    feats = [
        ("📍", "Mapa inteligente", "Gasolineras y precios a tu alrededor", BLUE),
        ("❤️", "Favoritas", "Sincronizadas en todos tus dispositivos", ORANGE),
        ("🔔", "Alertas de bajada", "Avisos cuando baja el precio", (46, 158, 91)),
        ("📈", "Historial de precios", "La evolución de cada gasolinera", BLUE),
        ("🛣️", "Modo ahorro", "Calcula el ahorro neto por desvío", ORANGE),
        ("🚗", "Multi-vehículo", "Cada coche con su combustible", (46, 158, 91)),
        ("📊", "Estadísticas", "Gasto mensual, consumo y CSV", BLUE),
        ("🧾", "Escaneo de ticket", "Registra el repostaje con la cámara", ORANGE),
    ]
    cols, cw, ch = 2, 600, 240
    gap_x, gap_y = 40, 32
    start_x = (W - (cols * cw + (cols - 1) * gap_x)) // 2
    start_y = 360
    for i, (emo, title, desc, col) in enumerate(feats):
        cxx = start_x + (i % cols) * (cw + gap_x)
        cyy = start_y + (i // cols) * (ch + gap_y)
        card = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
        cd = ImageDraw.Draw(card)
        cd.rounded_rectangle([0, 0, cw - 1, ch - 1], radius=34, fill=(255, 255, 255, 255))
        card.alpha_composite(emoji_badge(emo, 110, col + (255,)), (40, 40))
        cd.text((180, 56), title, font=font(FB, 44), fill=NAVY)
        for j, ln in enumerate(wrap(cd, desc, font(FR, 32), cw - 220)):
            cd.text((180, 118 + j * 42), ln, font=font(FR, 32), fill=(90, 108, 128))
        # sombra simple
        sh = Image.new("RGBA", (cw + 60, ch + 60), (0, 0, 0, 0))
        ImageDraw.Draw(sh).rounded_rectangle([30, 36, 30 + cw, 36 + ch], radius=34, fill=(20, 50, 90, 60))
        sh = sh.filter(ImageFilter.GaussianBlur(18))
        bg.alpha_composite(sh, (cxx - 30, cyy - 30))
        bg.alpha_composite(card, (cxx, cyy))

    d.text((W // 2, H - 70), "Precios oficiales · Toda España · Gratis", font=font(FH, 34), fill=BLUE, anchor="ma")
    bg.convert("RGB").save(os.path.join(OUT, "grafico-funciones.png"), quality=95)
    print("features infographic")

make_features_infographic()
print("\nTODO LISTO en", OUT)
