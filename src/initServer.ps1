Invoke-Expression -Command "start rmiregistry"
Start-Sleep -s 5
Invoke-Expression -Command "java rmiserver/Server MyServer 192.168.0.20 1099"