param(
  [string]$BaseUrl = "http://localhost:8080/backend",
  [string]$Username = "zhangsan",
  [string]$Password = "password",
  [int]$ToUserId = 2
)

$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/login" -ContentType "application/json" -Body $loginBody
$token = $loginResponse.data.token

if ([string]::IsNullOrWhiteSpace($token)) {
  Write-Error "Login failed: token not found in response."
  exit 1
}

$tokenFile = Join-Path $PSScriptRoot "token.txt"
$token | Out-File -FilePath $tokenFile -Encoding utf8
Write-Host "Token saved to $tokenFile"

$headers = @{ Authorization = "Bearer $token" }

Write-Host "Calling message list..."
Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/message/list" -Headers $headers | ConvertTo-Json -Depth 6

$messageContent = "API test message $(Get-Date -Format 'yyyyMMddHHmmss')"
$sendBody = @{ toUserId = $ToUserId; content = $messageContent } | ConvertTo-Json
$sendResponse = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/message/send" -Headers $headers -ContentType "application/json" -Body $sendBody
$messageId = $sendResponse.data.messageId

Write-Host "Message sent with ID: $messageId"

Write-Host "Calling message list with withUserId=$ToUserId..."
Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/message/list?withUserId=$ToUserId" -Headers $headers | ConvertTo-Json -Depth 6

$readBody = @{ messageId = $messageId } | ConvertTo-Json
Write-Host "Marking message as read..."
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/message/read" -Headers $headers -ContentType "application/json" -Body $readBody | ConvertTo-Json -Depth 6
