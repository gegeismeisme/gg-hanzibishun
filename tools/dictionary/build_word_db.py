#!/usr/bin/env python3
import argparse
import json
import sqlite3
from pathlib import Path
from typing import Iterable, Tuple

from pinyin_normalizer import normalize_pinyin


def build_rows(entries: Iterable[dict]):
    for obj in entries:
        word = (obj.get("word") or "").strip()
        if not word:
            continue
        oldword = (obj.get("oldword") or "").strip()
        strokes = (obj.get("strokes") or "").strip()
        pinyin = (obj.get("pinyin") or "").strip()
        radicals = (obj.get("radicals") or "").strip()
        explanation = (obj.get("explanation") or "").strip()
        more = (obj.get("more") or "").strip()
        pinyin_plain, pinyin_tone = normalize_pinyin(pinyin)
        yield (
            word,
            oldword,
            strokes,
            pinyin,
            radicals,
            explanation,
            more,
            pinyin_plain,
            pinyin_tone,
        )


def main() -> int:
    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parent.parent

    parser = argparse.ArgumentParser(description="Build pre-indexed SQLite dictionary database.")
    parser.add_argument(
        "--input",
        type=Path,
        default=script_dir / "word.json",
        help="Path to word.json source file.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=repo_root / "app/src/main/assets/word/word.db",
        help="Output SQLite database file path.",
    )
    args = parser.parse_args()

    input_path: Path = args.input
    output_path: Path = args.output

    if not input_path.exists():
        raise SystemExit(f"Input not found: {input_path}")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if output_path.exists():
        output_path.unlink()

    with input_path.open("r", encoding="utf-8", errors="replace") as fp:
        entries = json.load(fp)

    conn = sqlite3.connect(str(output_path))
    try:
        conn.execute("PRAGMA journal_mode=OFF")
        conn.execute("PRAGMA synchronous=OFF")
        conn.execute("PRAGMA temp_store=MEMORY")
        conn.execute("PRAGMA cache_size=200000")
        conn.execute(
            """
            CREATE TABLE words (
                word TEXT PRIMARY KEY,
                oldword TEXT,
                strokes TEXT,
                pinyin TEXT,
                radicals TEXT,
                explanation TEXT,
                more TEXT,
                pinyin_plain_compact TEXT,
                pinyin_tone_compact TEXT
            ) WITHOUT ROWID;
            """
        )
        conn.execute("BEGIN")
        conn.executemany(
            """
            INSERT OR REPLACE INTO words
            (word, oldword, strokes, pinyin, radicals, explanation, more, pinyin_plain_compact, pinyin_tone_compact)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """,
            build_rows(entries),
        )
        conn.execute("COMMIT")
        conn.execute("CREATE INDEX idx_pinyin_plain_compact ON words(pinyin_plain_compact);")
        conn.execute("CREATE INDEX idx_pinyin_tone_compact ON words(pinyin_tone_compact);")
        conn.execute("ANALYZE;")
        conn.execute("VACUUM;")
    finally:
        conn.close()

    print(f"Wrote: {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
