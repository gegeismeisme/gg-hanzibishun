from __future__ import annotations

from typing import Tuple

TONE_MAP = {
    "ā": ("a", 1),
    "á": ("a", 2),
    "ǎ": ("a", 3),
    "à": ("a", 4),
    "ē": ("e", 1),
    "é": ("e", 2),
    "ě": ("e", 3),
    "è": ("e", 4),
    "ī": ("i", 1),
    "í": ("i", 2),
    "ǐ": ("i", 3),
    "ì": ("i", 4),
    "ō": ("o", 1),
    "ó": ("o", 2),
    "ǒ": ("o", 3),
    "ò": ("o", 4),
    "ū": ("u", 1),
    "ú": ("u", 2),
    "ǔ": ("u", 3),
    "ù": ("u", 4),
    "ǖ": ("v", 1),
    "ǘ": ("v", 2),
    "ǚ": ("v", 3),
    "ǜ": ("v", 4),
    "ü": ("v", 0),
    "\u0261": ("g", 0),  # LATIN SMALL LETTER SCRIPT G, used in some pinyin datasets
}


def normalize_pinyin_syllable(raw: str) -> Tuple[str, str]:
    tone = 0
    base = []
    for ch in raw.strip().lower():
        if ch in "12345":
            tone = int(ch)
            continue
        if ch == ":":
            if base and base[-1] == "u":
                base[-1] = "v"
            continue
        mapped = TONE_MAP.get(ch)
        if mapped is not None:
            base_char, mapped_tone = mapped
            base.append(base_char)
            if mapped_tone:
                tone = mapped_tone
            continue
        if "a" <= ch <= "z":
            base.append(ch)
            continue
    plain = "".join(base)
    tone_suffix = str(tone) if 1 <= tone <= 5 else ""
    return plain, plain + tone_suffix


def normalize_pinyin(raw: str) -> Tuple[str, str]:
    if not raw:
        return "", ""
    parts = raw.strip().lower().replace(",", " ").split()
    plain_parts = []
    tone_parts = []
    for part in parts:
        plain, tone = normalize_pinyin_syllable(part)
        if plain:
            plain_parts.append(plain)
            tone_parts.append(tone)
    return "".join(plain_parts), "".join(tone_parts)

