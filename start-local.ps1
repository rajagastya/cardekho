$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Starting backend and frontend from $root"

if (-not (Test-Path "$root\frontend\node_modules")) {
  Write-Host "Installing frontend dependencies first..."
  Push-Location "$root\frontend"
  npm install
  Pop-Location
}

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\backend'; mvn ""-Dmaven.repo.local=.m2repo"" spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm run dev"

Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend: http://localhost:5173"
