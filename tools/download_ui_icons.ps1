$ErrorActionPreference = "Stop"

# 按 data/ui_icon_sources.json 下载网站的筛选图标到 app/src/main/assets/ui_icons/。地址为空的会跳过。
$root = Split-Path -Parent $PSScriptRoot
$doc = ConvertFrom-Json (Get-Content -Raw -Encoding UTF8 (Join-Path $root "data/ui_icon_sources.json"))
$out = Join-Path $root "app/src/main/assets/ui_icons"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$headers = @{ "User-Agent" = "Mozilla/5.0" }
$done = 0; $skipped = 0; $failed = @()
foreach ($cat in $doc.icons.PSObject.Properties) {
    foreach ($opt in $cat.Value.PSObject.Properties) {
        $url = [string]$opt.Value
        if ([string]::IsNullOrWhiteSpace($url)) { $skipped++; continue }
        $ext = [System.IO.Path]::GetExtension(([Uri]$url).AbsolutePath).ToLower()
        if ($ext -ne ".webp" -and $ext -ne ".png") { $failed += "$($cat.Name)_$($opt.Name) (unsupported type '$ext', use webp or png)"; continue }
        $path = Join-Path $out ("{0}_{1}{2}" -f $cat.Name, $opt.Name, $ext)
        try {
            Invoke-WebRequest -Uri $url -OutFile $path -Headers $headers -UseBasicParsing
            $done++
            Start-Sleep -Milliseconds 200
        } catch {
            if (Test-Path $path) { Remove-Item $path }
            $failed += "$($cat.Name)_$($opt.Name) ($($_.Exception.Message))"
        }
    }
}
Write-Host "Downloaded: $done, skipped (no URL): $skipped"
if ($failed.Count -gt 0) {
    Write-Host "Failed ($($failed.Count)):"
    $failed | ForEach-Object { Write-Host "  $_" }
}
