$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "JAVA_HOME set to: $env:JAVA_HOME"
java -version
mvn -v
mvn -DskipTests package
