#!/usr/bin/env python3
import argparse
import base64
import os
import sys


def normalize_metadata(meta):
    if meta is None:
        return {}
    if hasattr(meta, "items"):
        items = list(meta.items())
    else:
        return {}
    normalized = {}
    for key, value in items:
        key_str = str(key)
        if key_str.startswith("/"):
            key_str = key_str[1:]
        if value is None:
            value_str = ""
        else:
            value_str = str(value)
        normalized[key_str] = value_str
    return normalized


def extract_with_fitz(path):
    import fitz

    doc = fitz.open(path)
    metadata = normalize_metadata(doc.metadata)
    text_parts = []
    images = []

    for page_index in range(len(doc)):
        page = doc[page_index]
        text_parts.append(page.get_text())
        for img_index, img in enumerate(page.get_images(full=True)):
            xref = img[0]
            base = doc.extract_image(xref)
            image_bytes = base.get("image")
            ext = base.get("ext", "bin")
            if image_bytes:
                b64 = base64.b64encode(image_bytes).decode("ascii")
            else:
                b64 = ""
            images.append(
                {
                    "page": page_index + 1,
                    "index": img_index + 1,
                    "ext": ext,
                    "base64": b64,
                }
            )

    return {
        "engine": "pymupdf",
        "page_count": len(doc),
        "metadata": metadata,
        "text": "\n\n".join(text_parts).strip(),
        "images": images,
    }


def extract_with_pypdf(path):
    from pypdf import PdfReader

    reader = PdfReader(path)
    metadata = normalize_metadata(reader.metadata)
    text_parts = []
    images = []

    for page_index, page in enumerate(reader.pages):
        page_text = page.extract_text() or ""
        text_parts.append(page_text)
        page_images = getattr(page, "images", None)
        if page_images:
            for img_index, img in enumerate(page_images):
                img_data = getattr(img, "data", None)
                ext = getattr(img, "extension", None) or "bin"
                if img_data:
                    b64 = base64.b64encode(img_data).decode("ascii")
                else:
                    b64 = ""
                images.append(
                    {
                        "page": page_index + 1,
                        "index": img_index + 1,
                        "ext": ext,
                        "base64": b64,
                    }
                )

    return {
        "engine": "pypdf",
        "page_count": len(reader.pages),
        "metadata": metadata,
        "text": "\n\n".join(text_parts).strip(),
        "images": images,
    }


def extract_with_pdfminer(path):
    from pdfminer.high_level import extract_text

    text = extract_text(path) or ""
    return {
        "engine": "pdfminer",
        "page_count": 0,
        "metadata": {},
        "text": text.strip(),
        "images": [],
    }


def build_markdown(path, result, notes):
    lines = []
    lines.append("# PDF Extraction")
    lines.append("")
    lines.append("## Source")
    lines.append(f"- Path: {path}")
    lines.append(f"- Engine: {result['engine']}")
    lines.append(f"- Pages: {result['page_count']}")
    lines.append("")
    lines.append("## Metadata")
    metadata = result.get("metadata", {})
    if metadata:
        for key in sorted(metadata.keys()):
            value = metadata[key]
            lines.append(f"- {key}: {value}")
    else:
        lines.append("- None")

    lines.append("")
    lines.append("## Text")
    lines.append("```text")
    lines.append(result.get("text", ""))
    lines.append("```")

    lines.append("")
    lines.append("## Images")
    images = result.get("images", [])
    if not images:
        lines.append("- None")
    else:
        for idx, image in enumerate(images, start=1):
            page = image.get("page", "?")
            ext = image.get("ext", "bin")
            b64 = image.get("base64", "")
            lines.append("")
            lines.append(f"### Image {idx} (page {page}, {ext})")
            lines.append("```text")
            lines.append(f"data:image/{ext};base64,{b64}")
            lines.append("```")

    if notes:
        lines.append("")
        lines.append("## Notes")
        for note in notes:
            lines.append(f"- {note}")

    return "\n".join(lines).rstrip() + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("path")
    args = parser.parse_args()

    path = args.path
    if not os.path.exists(path):
        print(f"File not found: {path}", file=sys.stderr)
        sys.exit(1)

    notes = []
    try:
        result = extract_with_fitz(path)
    except Exception as exc:
        notes.append(f"PyMuPDF unavailable: {exc}")
        try:
            result = extract_with_pypdf(path)
        except Exception as exc_pypdf:
            notes.append(f"pypdf unavailable: {exc_pypdf}")
            try:
                result = extract_with_pdfminer(path)
            except Exception as exc_pdfminer:
                notes.append(f"pdfminer failed: {exc_pdfminer}")
                print("Failed to extract PDF.", file=sys.stderr)
                for note in notes:
                    print(note, file=sys.stderr)
                sys.exit(1)

    output = build_markdown(path, result, notes)
    print(output)


if __name__ == "__main__":
    main()
