$env:JAVA_HOME='C:\Users\25559\.jdks\ms-21.0.10'
$env:Path='C:\Users\25559\.jdks\ms-21.0.10\bin;' + $env:Path
Set-Location 'C:\Users\25559\java-curdeg\java-server'
.\mvnw.cmd spring-boot:run *> 'C:\Users\25559\java-curdeg\java-server\logs\codex-backend.combined.log'
