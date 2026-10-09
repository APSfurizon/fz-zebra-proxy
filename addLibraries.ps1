$libDir = "C:/Users/Stran/Desktop/shit/programming/Furizon/zebra-sdk"

$cmds = @()
$deps = @()
Get-ChildItem $libDir -Filter *.jar | ForEach-Object {
    $id   = "zebra-" + ($_.BaseName.ToLower() -replace '[^a-z0-9.-]', '-')
    $file = $_.FullName -replace '\\', '/'
    $cmds += "install:install-file `"-Dfile=$file`" -DgroupId=com.zebra.sdk -DartifactId=$id -Dversion=1.0 -Dpackaging=jar"
    $deps += "<dependency><groupId>com.zebra.sdk</groupId><artifactId>$id</artifactId><version>1.0</version></dependency>"
}
$cmds | Set-Content "zebra-commands.txt"
$deps | Set-Content "zebra-deps.xml"
$cmds