$ErrorActionPreference = 'Stop'

# Login to get JWT
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body (ConvertTo-Json @{email='admin@tienda.com'; password='admin123'})
$token = $login.token
Write-Output "Got token"

# Call setup-mfa to create secret and save it in DB
$setup = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/setup-mfa' -Method Post -ContentType 'application/json' -Body (ConvertTo-Json @{email='admin@tienda.com'; password='admin123'}) -Headers @{ Authorization = 'Bearer ' + $token }
$secret = $setup.secret
Write-Output ("Secret: " + $secret)

function Base32Decode([string]$input){
    $alphabet='ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'
    $input = $input.ToUpper().Replace('=','')
    $bits=0
    $value=0
    $out = New-Object System.Collections.Generic.List[byte]
    foreach($c in $input.ToCharArray()){
        $value = ($value -shl 5) -bor ($alphabet.IndexOf($c))
        $bits += 5
        if($bits -ge 8){
            $bits -=8
            $b = ($value -shr $bits) -band 0xFF
            $out.Add([byte]$b)
        }
    }
    return ,$out.ToArray()
}

$keyBytes = Base32Decode $secret
$epoch = [math]::Floor((Get-Date).ToUniversalTime().Subtract([datetime]'1970-01-01').TotalSeconds)
$timestep = [int]([math]::Floor($epoch / 30))
function Get-BeBytes([long]$v){
    $b = [System.BitConverter]::GetBytes([long]$v)
    if([System.BitConverter]::IsLittleEndian){ [Array]::Reverse($b) }
    return $b
}

$counter = Get-BeBytes $timestep
$hmac = New-Object System.Security.Cryptography.HMACSHA1
$hmac.Key = $keyBytes
$hash = $hmac.ComputeHash($counter)
$offset = $hash[$hash.Length - 1] -band 0x0F
$binary = ((($hash[$offset] -band 0x7F) -shl 24) -bor (($hash[$offset+1] -band 0xFF) -shl 16) -bor (($hash[$offset+2] -band 0xFF) -shl 8) -bor ($hash[$offset+3] -band 0xFF))
$otp = $binary % 1000000
$code = $otp.ToString('D6')
Write-Output ("TOTP: " + $code)

# Call enable-mfa with computed code
$enable = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/enable-mfa' -Method Post -ContentType 'application/json' -Body (ConvertTo-Json @{email='admin@tienda.com'; method='totp'; code=$code}) -Headers @{ Authorization = 'Bearer ' + $token }
Write-Output 'Enable response:'
$enable | ConvertTo-Json -Depth 5
