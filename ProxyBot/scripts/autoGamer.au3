Dim $chaosTitle = "Chaoslauncher"
Dim $gameTitle = "Brood War"
Dim $chaosExec = "C:\Program Files\Starcraft\Chaoslauncher\Chaoslauncher.exe"
Dim $chaosPath = "C:\Program Files\Starcraft\Chaoslauncher"
Dim $begin

Opt("WinTitleMatchMode", 2) ;1=start, 2=subStr, 3=exact, 4=advanced, -1 to -4=Nocase
dim $arr
While 1==1
   $begin = TimerInit()
   WinClose($gameTitle)
   Sleep(500)
   $arr = WinList($chaosTitle)
   if $arr[0][0] == 0 Then
	  Run($chaosExec,$chaosPath)
   EndIf
   if (WinWait($chaosTitle, "", 5) == 0) Then ContinueLoop
   WinActivate($chaosTitle)
   Sleep(1000)
   Send("{ENTER}")
   if (WinWait("Success", "", 5) == 0) Then ContinueLoop
   WinActivate("Success")
   Sleep(1000)
   Send("{ENTER}")
   if (WinWait($gameTitle, "", 5) == 0) Then ContinueLoop
   WinActivate($gameTitle)
   Sleep(2000)
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
   Sleep(1
   
   000)
   while 1==1
	  Send("{ENTER}")
	  Sleep(500)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(1000)
	  if checkError() > 0 Then exitLoop
	  Send("u")
	  Sleep(500)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(500)
	  if checkError() > 0 Then exitLoop
	  Send("{ENTER}")
	  Sleep(500)
	  if checkError() > 0 Then exitLoop
   wend
WEnd

func checkError()
   $diff = TimerDiff($begin)/1000/60
   ConsoleWrite($diff & @CRLF)
   If ($diff > 15) Then Return 1
   $arr = WinList($gameTitle)
   if $arr[0][0] == 0 Then Return 1
   Return 0
endfunc