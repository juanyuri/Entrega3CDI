Invoke-Expression -Command ".\c.ps1"
Invoke-Expression -Command "start rmiregistry"
Start-Sleep -s 2
Invoke-Expression -Command "java rmiserver/Server MyServer"