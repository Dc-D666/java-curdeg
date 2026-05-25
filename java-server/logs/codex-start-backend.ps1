Set-Location 'C:\Users\25559\java-curdeg\java-server'
$env:JAVA_HOME = 'D:\IntelliJ IDEA 2025.3.3\jbr'
$env:Path = 'D:\IntelliJ IDEA 2025.3.3\jbr\bin;' + $env:Path
& .\mvnw.cmd spring-boot:run *>> 'C:\Users\25559\java-curdeg\java-server\logs\codex-backend-start.out.log'
