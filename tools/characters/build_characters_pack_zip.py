import argparse
import pathlib
import zipfile


def _hex_code_from_stem(stem: str) -> str | None:
    if not stem:
        return None
    if stem.startswith("u") and len(stem) > 1:
        hex_part = stem[1:]
        if all(ch in "0123456789abcdefABCDEF" for ch in hex_part):
            return hex_part.lower()
    if len(stem) != 1:
        return None
    return format(ord(stem), "x")


def main() -> int:
    parser = argparse.ArgumentParser(description="Build a single Zip pack for hanzi JSON assets.")
    parser.add_argument(
        "--assets-dir",
        default="app/src/main/assets/characters",
        help="Directory containing per-character JSON files (uXXXX.json or literal-name.json).",
    )
    parser.add_argument(
        "--output",
        default="app/src/main/assets/characters/characters.pack.zip",
        help="Output Zip path.",
    )
    args = parser.parse_args()

    assets_dir = pathlib.Path(args.assets_dir).resolve()
    output_path = pathlib.Path(args.output).resolve()
    output_name = output_path.name

    if not assets_dir.exists():
        raise SystemExit(f"assets dir not found: {assets_dir}")

    candidates: dict[str, pathlib.Path] = {}
    for file_path in sorted(assets_dir.glob("*.json")):
        if file_path.name == output_name:
            continue
        stem = file_path.stem
        if stem.lower() == "all":
            continue
        hex_code = _hex_code_from_stem(stem)
        if hex_code is None:
            continue
        arcname = f"characters/u{hex_code}.json"
        candidates.setdefault(arcname, file_path)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    if output_path.exists():
        output_path.unlink()

    with zipfile.ZipFile(output_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for arcname, file_path in candidates.items():
            zf.write(file_path, arcname)

    size = output_path.stat().st_size if output_path.exists() else 0
    print(f"Wrote {len(candidates)} entries to {output_path} ({size} bytes).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

