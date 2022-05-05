Invoke-Expression -Command "start rmiregistry"
Start-Sleep -s 5
Invoke-Expression -Command "java rmiserver/Server MyServer"