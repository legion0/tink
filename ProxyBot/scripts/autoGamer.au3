Dim $chaosTitle = "Chaoslauncher"
Dim $gameTitle = "Brood War"
Dim $chaosExec = "C:\Program Files\Starcraft\Chaoslauncher\Chaoslauncher.exe"
Dim $chaosPath = "C:\Program Files\Starcraft\Chaoslauncher"

Opt("WinTitleMatchMode", 2) ;1=start, 2=subStr, 3=exact, 4=advanced, -1 to -4=Nocase
dim $arr
While 1==1
   $arr = WinList($chaosTitle)
   if $arr[0][0] == 0 Then
	  Run($chaosExec,$chaosPath)
   EndIf
   WinWait($chaosTitle)
   WinActivate($chaosTitle)
   Send("{ENTER}")
   WinWait("Success")
   WinActivate("Success")
   Send("{ENTER}")
   WinWait($gameTitle)
   WinActivate($gameTitle)
   Sleep(1000)
   Send("s")
   Sleep(1000)
   Send("e")
   Sleep(1000)
   Send("{ENTER}")
   Sleep(1000)
   Send("u")
   Sleep(1000)
   Send("{END}")
   Sleep(1000)
   Send("{ENTER}")
   Sleep(1000)
   Send("{ENTER}")
   Sleep(5000)
   while 1==1
	  Send("{ENTER}")
	  Sleep(2000)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(3000)
	  if checkError() > 0 Then exitLoop
	  Send("u")
	  Sleep(2000)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(2000)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(2000)
	  if checkError() > 0 Then exitLoop
   wend
WEnd

func checkError()
   $arr = WinList($gameTitle)
   if $arr[0][0] == 0 Then
	  return 1
   EndIf
   return 0
endfunc