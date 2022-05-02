Invoke-Expression -Command "javac rmiinterface/*.java" | Add-Content outputlog.txt
Invoke-Expression -Command "javac rmiserver/*.java" | Add-Content outputlog.txt
Invoke-Expression -Command "javac rmiclient/*.java" | Add-Content outputlog.txt