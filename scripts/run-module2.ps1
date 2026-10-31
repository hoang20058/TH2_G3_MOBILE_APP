param(
    [string]$DeviceId,
    [string]$JavaHome
)
$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
Push-Location $projectDirectory
try {
    if ($JavaHome) { $env:JAVA_HOME = $JavaHome }
    elseif (Test-Path (Join-Path $env:USERPROFILE '.jdks\jbr-17.0.14\bin\java.exe')) {
        $env:JAVA_HOME = Join-Path $env:USERPROFILE '.jdks\jbr-17.0.14'
    }
    $sdkPath = $env:ANDROID_HOME
    $properties = Join-Path $projectDirectory 'local.properties'
    if (Test-Path $properties) {
        $sdkLine = Get-Content $properties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) { $sdkPath = $sdkLine.Substring(8).Replace('\:', ':').Replace('\\', '\') }
    }
    if (-not $sdkPath) { $sdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $adb = Join-Path $sdkPath 'platform-tools\adb.exe'
    if (-not (Test-Path $adb)) { throw 'Android SDK / adb not found. Configure local.properties.' }
    $connected = @(& $adb devices | Select-String '^(\S+)\s+device$' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    if (-not $DeviceId) {
        if ($connected.Count -ne 1) { throw 'Start one emulator, or specify -DeviceId emulator-5554.' }
        $DeviceId = $connected[0]
    }
    if ($DeviceId -notin $connected) { throw "Device $DeviceId is not connected." }
    $gradle = Join-Path $projectDirectory 'gradlew.bat'
    # Use the project's declared Gradle distribution if already extracted locally.
    $wrapperConfig = Get-Content 'gradle\wrapper\gradle-wrapper.properties' -Raw
    if ($wrapperConfig -match 'gradle-([0-9.]+)-bin\.zip') {
        $distDirectory = Join-Path $env:USERPROFILE ('.gradle\wrapper\dists\gradle-' + $Matches[1] + '-bin')
        $installed = Get-ChildItem $distDirectory -Filter gradle.bat -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($installed) { $gradle = $installed.FullName }
    }
    & $gradle --no-daemon :app:assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'Module 2 build failed.' }
    & $adb -s $DeviceId install -r 'app\build\outputs\apk\debug\app-debug.apk'
    if ($LASTEXITCODE -ne 0) { throw 'APK installation failed.' }
    & $adb -s $DeviceId shell am start -n 'com.example.studyschedule/.activities.StudentModuleDemoActivity'
    if ($LASTEXITCODE -ne 0) { throw 'Could not start the module 2 demo.' }
}
finally { Pop-Location }
