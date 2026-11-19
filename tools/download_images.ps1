$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
# 改用正斜杠 /，Windows 和 Linux 通用
$json = Get-Content -Raw -Encoding UTF8 (Join-Path $root "data/fish.json")
$fish = @((ConvertFrom-Json $json) | ForEach-Object { $_ })
if ($fish.Count -lt 2) { throw "fish.json was not parsed as a list (count = $($fish.Count))" }
Write-Host "Fish found: $($fish.Count)"

# 改用正斜杠 /
$out = Join-Path $root "app/src/main/assets/fish_images"
New-Item -ItemType Directory -Force -Path $out | Out-Null

$qualities = "standard","uncommon","rare","epic","legendary"
$headers = @{ "User-Agent" = "Mozilla/5.0" }
$failed = @()
$firstError = $null
$n = 0

foreach ($f in $fish) {
    $n++
    Write-Host ("[{0}/{1}] {2}" -f $n, $fish.Count, $f.id)
    foreach ($q in $qualities) {
        $name = "$($f.id)_$q.webp"
        $path = Join-Path $out $name
        if ((Test-Path $path) -and ((Get-Item $path).Length -gt 0)) { continue }
        try {
            Invoke-WebRequest -Uri "https://animals.party/images/fish/$name" -OutFile $path -Headers $headers
            Start-Sleep -Milliseconds 300
        } catch {
            if (Test-Path $path) { Remove-Item $path }
            $failed += $name
            if (-not $firstError) { $firstError = $_.Exception.Message }
        }
    }
}

$have = (Get-ChildItem $out -Filter *.webp).Count
Write-Host ""
Write-Host "Done. $have of $($fish.Count * 5) images present."
if ($failed.Count -gt 0) {
    Write-Host "Failed: $($failed.Count). First error: $firstError"
    $failed | Out-File (Join-Path $PSScriptRoot "failed.txt")
    Write-Host "Names saved to tools/failed.txt"
}
