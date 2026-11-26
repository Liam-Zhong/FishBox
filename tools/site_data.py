#!/usr/bin/env python3
"""从 animals.party 抓取构建所需的站点数据（只用标准库，CI 的 ubuntu runner 自带 python3）。

用法：
  python3 tools/site_data.py rods            抓取每条鱼可用的鱼竿 -> data/fish_rods.json
  python3 tools/site_data.py icons           按 data/ui_icon_sources.json 下载筛选图标 -> app/src/main/assets/ui_icons/
  python3 tools/site_data.py discover        扫描站点页面，列出可能的筛选图标地址（只报告，不改任何文件）

安全措施：rods 只有在抓到足够多的鱼（默认 >= 90%）时才会覆盖 data/fish_rods.json，
否则保留原文件并以非 0 退出码结束，避免用残缺数据替换已有数据。
"""
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = "https://animals.party"
HEADERS = {"User-Agent": "Mozilla/5.0"}
ROD_RE = re.compile(r"images/gear/items/rod_([a-z_]+)\.webp")
MIN_RATIO = 0.9


def http_get(url, binary=False, retries=3):
    last = None
    for i in range(retries):
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=30) as r:
                data = r.read()
            return data if binary else data.decode("utf-8", "replace")
        except Exception as e:  # noqa: BLE001
            last = e
            time.sleep(1 + i)
    raise last


def load_json(rel):
    with open(os.path.join(ROOT, rel), encoding="utf-8") as f:
        return json.load(f)


def extract_rods(html, known):
    """从详情页 HTML 里按出现顺序取出已知鱼竿 id（去重）。"""
    ids = []
    for m in ROD_RE.finditer(html):
        rid = m.group(1)
        if rid in known and rid not in ids:
            ids.append(rid)
    return ids


def cmd_rods(fetch=http_get, sleep=time.sleep):
    fish = load_json("data/fish.json")
    known = {r["id"] for r in load_json("data/rods.json")}
    result, failed = {}, []
    for n, f in enumerate(fish, 1):
        fid = f["id"]
        print(f"[{n}/{len(fish)}] {fid}", flush=True)
        try:
            ids = extract_rods(fetch(f"{BASE}/zh-CN/fish/{fid}"), known)
            if ids:
                result[fid] = ids
            else:
                failed.append(f"{fid} (no rods found)")
        except Exception as e:  # noqa: BLE001
            failed.append(f"{fid} ({e})")
        sleep(0.3)
    print(f"Got rods for {len(result)} of {len(fish)} fish")
    for p in failed:
        print("  problem:", p)
    if len(result) < len(fish) * MIN_RATIO:
        print("Too few fish fetched; keeping the existing data/fish_rods.json")
        return 1
    path = os.path.join(ROOT, "data/fish_rods.json")
    doc = {
        "note": "每条鱼可用的具体鱼竿（鱼竿 id 见 rods.json），由 tools/site_data.py rods 从站点详情页抓取生成。",
        "rods": result,
    }
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(doc, f, ensure_ascii=False, indent=1)
        f.write("\n")
    print("Wrote", path)
    return 0


def cmd_icons(fetch=http_get):
    doc = load_json("data/ui_icon_sources.json")
    out = os.path.join(ROOT, "app/src/main/assets/ui_icons")
    os.makedirs(out, exist_ok=True)
    done = skipped = 0
    failed = []
    for cat, options in doc["icons"].items():
        for opt, url in options.items():
            if not url.strip():
                skipped += 1
                continue
            ext = os.path.splitext(urllib.parse.urlparse(url).path)[1].lower()
            if ext not in (".webp", ".png"):
                failed.append(f"{cat}_{opt} (unsupported type '{ext}', use webp or png)")
                continue
            try:
                data = fetch(url, binary=True)
                with open(os.path.join(out, f"{cat}_{opt}{ext}"), "wb") as f:
                    f.write(data)
                done += 1
            except Exception as e:  # noqa: BLE001
                failed.append(f"{cat}_{opt} ({e})")
    print(f"Downloaded: {done}, skipped (no URL): {skipped}")
    for p in failed:
        print("  failed:", p)
    return 1 if failed else 0


IMG_RE = re.compile(r"""["'(=\s]((?:https?:)?//[^"'()\s]+|/[^"'()\s]+)\.(webp|png|svg)""", re.I)
KEYWORDS = {
    "weather": ["sunny", "rain", "snow", "fog", "meteor", "weather"],
    "time": ["dawn", "morning", "afternoon", "dusk", "night", "midnight", "time"],
    "season": ["spring", "summer", "autumn", "winter", "season"],
    "location": ["riverside", "dock", "bend", "midstream", "boulder", "wreck", "pond", "ice", "location"],
    "gear": ["light", "heavy", "gear"],
}


def find_image_urls(text):
    urls = set()
    for m in IMG_RE.finditer(text.replace("\\u002F", "/").replace("\\/", "/")):
        u = m.group(1) + "." + m.group(2).lower()
        urls.add(urllib.parse.urljoin(BASE, u))
    return sorted(urls)


def cmd_discover(fetch=http_get):
    """只报告：把站点里的图片地址按关键词归类，供人工核对后填进 data/ui_icon_sources.json。"""
    pages = [f"{BASE}/zh-CN/fish", f"{BASE}/zh-CN/fish/shimmerfin", f"{BASE}/zh-CN/fish/shimmerfin/_payload.json"]
    found = set()
    for p in pages:
        try:
            found.update(find_image_urls(fetch(p)))
        except Exception as e:  # noqa: BLE001
            print("  could not read", p, e)
    print(f"{len(found)} image URLs found")
    for cat, words in KEYWORDS.items():
        hits = [u for u in sorted(found) if any(w in u.lower() for w in words) and "/fish/" not in u.lower()]
        print(f"\n[{cat}] {len(hits)} candidate(s)")
        for u in hits:
            print("  ", u)
    print("\n以上只是按文件名关键词归类的候选，不会自动写入任何文件；请核对后再填入 data/ui_icon_sources.json。")
    return 0


def main(argv):
    cmds = {"rods": cmd_rods, "icons": cmd_icons, "discover": cmd_discover}
    if len(argv) != 2 or argv[1] not in cmds:
        print(__doc__)
        return 2
    return cmds[argv[1]]()


if __name__ == "__main__":
    sys.exit(main(sys.argv))
