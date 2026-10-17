param(
    [string]$PgHost = 'localhost',
    [int]$PgPort = 5432,
    [string]$AdminUser = 'postgres',
    [switch]$SetupOnly,
    [switch]$Demo
)

$ErrorActionPreference = 'Stop'

# Localiza o cliente instalado pelo instalador padrão do PostgreSQL no Windows.
$psql = Get-Command psql -ErrorAction SilentlyContinue
if ($psql) {
    $psqlPath = $psql.Source
} else {
    $psqlPath = Get-ChildItem "$env:ProgramFiles/PostgreSQL/*/bin/psql.exe" -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psqlPath) {
    throw 'psql não encontrado. Instale o cliente PostgreSQL ou adicione sua pasta bin ao PATH.'
}

# Variáveis do psql fazem o escape dos valores usados como literais e identificadores SQL.
$dbUser = if ($Demo -and $env:DEMO_DB_USERNAME) { $env:DEMO_DB_USERNAME } elseif ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'topicos2' }
$dbPassword = if ($Demo -and $env:DEMO_DB_PASSWORD) { $env:DEMO_DB_PASSWORD } elseif ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { '123456' }
$dbName = if ($Demo) { 'ecommerce_veiculos_demo' } else { 'topicos2db' }
$configuredUrl = if ($Demo) { $env:DEMO_DB_URL } else { $env:DB_URL }
if ($configuredUrl -and $configuredUrl -ne "jdbc:postgresql://${PgHost}:${PgPort}/$dbName") {
    throw 'A URL configurada não corresponde ao banco local deste perfil. Ajuste PgHost/PgPort ou remova a variável de URL correspondente.'
}

$sql = @'
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'app_user', :'app_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'app_user')
\gexec
SELECT format('CREATE DATABASE %I OWNER %I', :'app_db', :'app_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'app_db')
\gexec
'@
$sql | & $psqlPath -X -h $PgHost -p $PgPort -U $AdminUser -d postgres `
    -v ON_ERROR_STOP=1 -v "app_user=$dbUser" -v "app_password=$dbPassword" -v "app_db=$dbName"
if ($LASTEXITCODE -ne 0) {
    throw 'Não foi possível preparar o banco. Confira o serviço PostgreSQL e o acesso do administrador (PGPASSWORD ou pgpass.conf).'
}
Write-Host "Usuário e banco local verificados: $dbUser / $dbName"
if ($SetupOnly) { return }

Push-Location $PSScriptRoot
try {
    $mavenArgs = @('-Dquarkus.hibernate-orm.schema-management.strategy=update')
    if ($Demo) { $mavenArgs += '-Dquarkus.profile=demo' }
    $mavenArgs += "-Dquarkus.datasource.jdbc.url=jdbc:postgresql://${PgHost}:${PgPort}/$dbName"
    & ./mvnw.cmd @mavenArgs quarkus:dev
    if ($LASTEXITCODE -ne 0) { throw 'O Quarkus encerrou com erro.' }
} finally {
    Pop-Location
}
