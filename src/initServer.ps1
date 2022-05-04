Invoke-Expression -Command "start rmiregistry"
Start-Sleep -s 10
Invoke-Expression -Command "java rmiserver/Server"