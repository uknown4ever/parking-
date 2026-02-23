; ============================================================
; Script Inno Setup – Gestion de Parking v1.0
; Génère : GestionParking_Setup.exe
; ============================================================

#define MyAppName      "Gestion de Parking"
#define MyAppVersion   "1.0.0"
#define MyAppPublisher "Mini-Projet Java 20"
#define MyAppExeName   "GestionParking.exe"
#define MyJarName      "GestionParking.jar"

[Setup]
AppId={{F3A2B1C0-8D4E-4F5A-9B6C-1D2E3F4A5B6C}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputDir=dist
OutputBaseFilename=GestionParking_Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
SetupIconFile=assets\icon.ico
UninstallDisplayIcon={app}\assets\icon.ico
MinVersion=10.0

[Languages]
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Tasks]
Name: "desktopicon"; Description: "Créer un raccourci sur le Bureau"; GroupDescription: "Raccourcis :"; Flags: unchecked

[Files]
; JAR principal
Source: "target\{#MyJarName}"; DestDir: "{app}"; Flags: ignoreversion

; Lanceur batch
Source: "assets\launch.bat"; DestDir: "{app}"; Flags: ignoreversion

; Icône et assets
Source: "assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs

; Script SQL (pour référence)
Source: "sql\init.sql"; DestDir: "{app}\sql"; Flags: ignoreversion

; README
Source: "docs\README.pdf"; DestDir: "{app}\docs"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}";   Filename: "{app}\assets\launch.bat"; IconFilename: "{app}\assets\icon.ico"
Name: "{group}\Désinstaller";   Filename: "{uninstallexe}"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\assets\launch.bat"; IconFilename: "{app}\assets\icon.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\assets\launch.bat"; Description: "Lancer l'application"; Flags: nowait postinstall skipifsilent shellexec

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Code]
function JavaInstalled(): Boolean;
var
  ResultCode: Integer;
begin
  Result := ShellExec('', 'java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) and (ResultCode = 0);
end;

function InitializeSetup(): Boolean;
begin
  if not JavaInstalled() then
  begin
    MsgBox('Java 11 ou supérieur est requis.' + #13#10 +
           'Veuillez installer Java depuis https://adoptium.net avant de continuer.',
           mbError, MB_OK);
    Result := False;
  end else
    Result := True;
end;
