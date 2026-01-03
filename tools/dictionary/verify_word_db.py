#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import random
import sqlite3
from pathlib import Path
from typing import Dict, Iterable

from pinyin_normalizer import normalize_pinyin


def load_entries(json_path: Path) -> Dict[str, dict]:
    with json_path.open("r", encoding="utf-8", errors="replace") as fp:
        entries = json.load(fp)
    if not isinstance(entries, list):
        raise SystemExit(f"Expected a JSON array in {json_path}")

    # word.json should be "last write wins", matching INSERT OR REPLACE.
    mapping: Dict[str, dict] = {}
    for obj in entries:
        if not isinstance(obj, dict):
            continue
        word = (obj.get("word") or "").strip()
        if not word:
            continue
        mapping[word] = obj
    return mapping


def get_columns(conn: sqlite3.Connection, table: str) -> Iterable[str]:
    rows = conn.execute(f"PRAGMA table_info({table});").fetchall()
    return [row[1] for row in rows]


def main() -> int:
    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parent.parent

    parser = argparse.ArgumentParser(description="Verify dictionary SQLite database matches word.json source.")
    parser.add_argument(
        "--input",
        type=Path,
        default=script_dir / "word.json",
        help="Path to word.json source file.",
    )
    parser.add_argument(
        "--db",
        type=Path,
        default=repo_root / "app/src/main/assets/word/word.db",
        help="Path to generated SQLite database file (word.db).",
    )
    parser.add_argument(
        "--sample-size",
        type=int,
        default=200,
        help="Random sample size for row-by-row verification (0 disables).",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=0,
        help="Random seed for sampling.",
    )
    parser.add_argument(
        "--max-errors",
        type=int,
        default=20,
        help="Stop after this many mismatches.",
    )
    args = parser.parse_args()

    json_path: Path = args.input
    db_path: Path = args.db
    sample_size: int = max(0, args.sample_size)
    max_errors: int = max(1, args.max_errors)

    if not json_path.exists():
        raise SystemExit(f"Input not found: {json_path}")
    if not db_path.exists():
        raise SystemExit(f"DB not found: {db_path}")

    entries_by_word = load_entries(json_path)
    expected_count = len(entries_by_word)

    conn = sqlite3.connect(str(db_path))
    try:
        columns = set(get_columns(conn, "words"))
        required = {
            "word",
            "oldword",
            "strokes",
            "pinyin",
            "radicals",
            "explanation",
            "more",
            "pinyin_plain_compact",
            "pinyin_tone_compact",
        }
        missing = sorted(required - columns)
        if missing:
            raise SystemExit(f"Missing columns in words table: {missing}")

        db_count = conn.execute("SELECT COUNT(*) FROM words;").fetchone()[0]
        if db_count != expected_count:
            raise SystemExit(f"Row count mismatch: db={db_count} json(unique)={expected_count}")

        if sample_size == 0:
            print(f"OK: {db_path} rows={db_count} (sampling disabled)")
            return 0

        rng = random.Random(args.seed)
        words = list(entries_by_word.keys())
        sample_size = min(sample_size, len(words))
        sample = rng.sample(words, sample_size)

        errors = 0
        for word in sample:
            obj = entries_by_word[word]
            expected = {
                "word": (obj.get("word") or "").strip(),
                "oldword": (obj.get("oldword") or "").strip(),
                "strokes": (obj.get("strokes") or "").strip(),
                "pinyin": (obj.get("pinyin") or "").strip(),
                "radicals": (obj.get("radicals") or "").strip(),
                "explanation": (obj.get("explanation") or "").strip(),
                "more": (obj.get("more") or "").strip(),
            }
            expected_plain, expected_tone = normalize_pinyin(expected["pinyin"])

            row = conn.execute(
                """
                SELECT
                    word, oldword, strokes, pinyin, radicals, explanation, more,
                    pinyin_plain_compact, pinyin_tone_compact
                FROM words
                WHERE word = ?
                LIMIT 1;
                """,
                (word,),
            ).fetchone()
            if not row:
                errors += 1
                print(f"[MISS] word={word!r}")
                if errors >= max_errors:
                    break
                continue

            (
                db_word,
                db_oldword,
                db_strokes,
                db_pinyin,
                db_radicals,
                db_explanation,
                db_more,
                db_plain,
                db_tone,
            ) = row
            actual = {
                "word": db_word or "",
                "oldword": db_oldword or "",
                "strokes": db_strokes or "",
                "pinyin": db_pinyin or "",
                "radicals": db_radicals or "",
                "explanation": db_explanation or "",
                "more": db_more or "",
            }

            mismatch = []
            for key in ("word", "oldword", "strokes", "pinyin", "radicals", "explanation", "more"):
                if actual[key] != expected[key]:
                    mismatch.append(f"{key}: expected={expected[key]!r} actual={actual[key]!r}")
            if (db_plain or "") != expected_plain:
                mismatch.append(f"pinyin_plain_compact: expected={expected_plain!r} actual={(db_plain or '')!r}")
            if (db_tone or "") != expected_tone:
                mismatch.append(f"pinyin_tone_compact: expected={expected_tone!r} actual={(db_tone or '')!r}")

            if mismatch:
                errors += 1
                print(f"[MISMATCH] word={word!r}")
                for item in mismatch[:6]:
                    print("  -", item)
                if errors >= max_errors:
                    break

        if errors:
            raise SystemExit(f"FAILED: {errors} mismatches (sample_size={sample_size})")

        print(f"OK: {db_path} rows={db_count} sample_size={sample_size}")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())

