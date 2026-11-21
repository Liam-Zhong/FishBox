$ErrorActionPreference = "Stop"

# 从站点每条鱼的详情页抓取「可用鱼竿」，生成 data/fish_rods.json。Windows PowerShell 5.1 与 PowerShell 7 通用。
$root = Split-Path -Parent $PSScriptRoot
$fishJson = Get-Content -Raw -Encoding UTF8 (Join-Path $root "data/fish.json")
$fish = @((ConvertFrom-Json $fishJson) | ForEach-Object { $_ })
if ($fish.Count -lt 2) { throw "fish.json was not parsed as a list (count = $($fish.Count))" }
$rodJson = Get-Content -Raw -Encoding UTF8 (Join-Path $root "data/rods.json")
$rods = @((ConvertFrom-Json $rodJson) | ForEach-Object { $_ })
$known = @{}
foreach ($r in $rods) { $known[$r.id] = $true }
Write-Host "Fish: $($fish.Count), known rods: $($known.Count)"

$headers = @{ "User-Agent" = "Mozilla/5.0" }
$result = [ordered]@{}
$failed = @()
$n = 0
foreach ($f in $fish) {
    $n++
    $id = $f.id
    Write-Host ("[{0}/{1}] {2}" -f $n, $fish.Count, $id)
    try {
        $resp = Invoke-WebRequest -Uri "https://animals.party/zh-CN/fish/$id" -Headers $headers -UseBasicParsing
        $html = [string]$resp.Content
        $ids = @()
        foreach ($m in [regex]::Matches($html, 'images/gear/items/rod_([a-z_]+)\.webp')) {
            $rid = $m.Groups[1].Value
            if ($known.ContainsKey($rid) -and ($ids -notcontains $rid)) { $ids += $rid }
        }
        if ($ids.Count -gt 0) { $result[$id] = @($ids) } else { $failed += "$id (no rods found)" }
        Start-Sleep -Milliseconds 300
    } catch {
        $failed += "$id ($($_.Exception.Message))"
    }
}

$doc = [ordered]@{
    note = "每条鱼可用的具体鱼竿（鱼竿 id 见 rods.json），由 tools/fetch_fish_rods 从站点详情页抓取生成。"
    rods = $result
}
$text = $doc | ConvertTo-Json -Depth 6
$path = Join-Path $root "data/fish_rods.json"
[System.IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding $false))
Write-Host ""
Write-Host "Done. $($result.Count) of $($fish.Count) fish written to data/fish_rods.json"
if ($failed.Count -gt 0) {
    Write-Host "Problems ($($failed.Count)):"
    $failed | ForEach-Object { Write-Host "  $_" }
}
