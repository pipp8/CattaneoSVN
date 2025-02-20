#NoTrayIcon
#Region ;**** Directives created by AutoIt3Wrapper_GUI ****
#AutoIt3Wrapper_Outfile=resource-merged.exe
#AutoIt3Wrapper_Outfile_x64=resource-mergedx64.exe
#AutoIt3Wrapper_Compile_Both=y
#AutoIt3Wrapper_UseX64=y
#AutoIt3Wrapper_Change2CUI=y
#AutoIt3Wrapper_Res_requestedExecutionLevel=asInvoker
#EndRegion ;**** Directives created by AutoIt3Wrapper_GUI ****
#region ---Au3Recorder generated code Start ---
Opt("WinWaitDelay",100)
Opt("WinDetectHiddenText",1)
Opt("MouseCoordMode", 1) ;1=absolute, 0=relative, 2=client
Opt("SendKeyDelay", Random(150, 250, 1))
Opt("WinTitleMatchMode", 2)     ;1=start, 2=subStr, 3=exact, 4=advanced, -1 to -4=Nocase

MouseClick("left",400,300,1)
OpenFirefox()
OpenFBAccount()
PostFBMessage()
OpenGMail()
OpenFBUserAlbum()
OpenDocWordPad()

Sleep(Random(5000, 10000, 1))
_WinWaitActivate("Firefox","", 5000)
Sleep(Random(1000, 3000, 1))
Send("{ALTDOWN}{F4}{ALTUP}") ;;; chiude firefox
Sleep(Random(1000, 2000, 1))
Send("{ENTER}")

; while True
;	Sleep(Random(1000, 2000, 1))
;	Send("{ALTDOWN}{TAB}{ALTUP}")
;	_WinWaitActivate("Firefox","", 2000)
;	Sleep(Random(1000, 5000, 1))
;	_WinWaitActivate("Internet Explorer","", 2000)
; WEnd
;~ Run(@ComSpec & " /c taskkill /F /IM " & @ScriptName & " /T", @SystemDir, @SW_HIDE)
Exit 0

;  Open Firefox e clear caches
Func OpenFirefox()
	; _WinWaitActivate("Program Manager","", 1000)
	; MouseClick("left",35,135,2)
	; MouseUp("left")
	Sleep(Random(1000, 2000, 1))
	Send("{CTRLDOWN}{ESC}{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("firefox")
	Sleep(Random(2000, 3000, 1))
	Send("{ENTER}")
	_WinWaitActivate("Firefox","", 2000)
	Sleep(Random(1000, 3000, 1))
	Send("{CTRLDOWN}{SHIFTDOWN}{DEL}{CTRLUP}{SHIFTUP}")
	Sleep(Random(1000, 3000, 1))
	Send("t")
	Sleep(Random(500, 1000, 1))
	Send("{ENTER}")
EndFunc

Func OpenFBAccount()
	_WinWaitActivate("Firefox","", 2000) ; firefox è già aperto
	Sleep(Random(2000, 3000, 1))
	Send("{CTRLDOWN}l{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("http://www.facebook.com{ENTER}")
	_WinWaitActivate("Ti diamo il benvenuto su Facebook","", 10000)
	Sleep(Random(5000, 10000, 1))
	Send("falsoalibidigitale@gmail.com{TAB}")
	Sleep(Random(1000, 2000, 1))
	Send("f4ls04l1b1{ENTER}")

	;;; Simula un click sul logo di Facebook, per essere sicuri di caricare la pagina principale
	Sleep(Random(5000, 10000, 1))
	Send("{CTRLDOWN}l{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("http://www.facebook.com/?ref=logo{ENTER}")
	_WinWaitActivate("Facebook","", 10000)

	; Opt("WinTitleMatchMode", 1)
	; _WinWaitActivate("Trova i tuoi amici","", 10000)
	; Opt("WinTitleMatchMode", 2)
	; _WinWaitActivate("Facebook","", 10000)
	Sleep(Random(2000, 5000, 1))
	Send("{CTRLDOWN}f{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("Notizie")
	Sleep(Random(1000, 2000, 1))
	Send("{ESC}")
	Sleep(Random(1000, 2000, 1))
	Send("{ENTER}")
EndFunc


Func PostFBMessage()
	;;; post a status changed message
	Sleep(Random(5000, 10000, 1))
	Send("{CTRLDOWN}f{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("A{SPACE}cosa{SPACE}stai{SPACE}pensando?")
	Sleep(Random(1000, 2000, 1))
	Send("{ESC}")
	Sleep(Random(1000, 2000, 1))
	Send("Non sono mai stato così bene. ore: " & Random(0, 100000000, 1) & "{TAB}{TAB}{ENTER}")
	Sleep(Random(1000, 10000, 1))
EndFunc


Func  OpenGMail()
	;;; Riattiva Firefox e accede a Gmail
	WinSetState("Firefox", "", @SW_SHOW)
	WinActivate("Firefox", "")
	Sleep(Random(3000, 6000, 1))
	Send("{CTRLDOWN}t{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("mail.google.com{ENTER}")
	_WinWaitActivate("Gmail: l'email di Google","", 10000)
	Sleep(Random(5000, 10000, 1))
	Send("falsoalibidigitale@gmail.com{TAB}")
	Sleep(Random(1000, 2000, 1))
	Send("f4ls04l1b1{ENTER}")
	_WinWaitActivate("Gmail - Posta in arrivo","", 10000)
	Sleep(Random(3000, 5000, 1))
	Send("{SHIFTDOWN}c{SHIFTUP}")
	Sleep(Random(3000, 5000, 1))
	_WinWaitActivate("Gmail - Scrivi","", 10000)
	Sleep(Random(3000, 5000, 1))
	Send("falsoalibidigitale@gmail.com{ESC}") ;;; destinatario
	Sleep(Random(1000, 2000, 1))
	Send("{ESC}{TAB}prova") ;;; subject
	Sleep(Random(1000, 2000, 1))
	Send("{TAB}Questo messaggio costituisce una prova inconfutabile della mia presenza al computer") ;;; testo del messaggio
	Sleep(Random(1000, 2000, 1))
	Send("{TAB}{ENTER}")
	Sleep(Random(5000, 10000, 1))
	Send("{ENTER}")
	_WinWaitActivate("Gmail","",10000)
	Send("{CTRLDOWN}w{CTRLUP}") ; chiude la finestra
	Sleep(Random(1000, 3000, 1))
	Send("{ENTER}") ;; per confermare la chiusura
	Sleep(Random(3000, 5000, 1))
EndFunc

Func OpenFBUserAlbum()
	_WinWaitActivate("Firefox","",1000)
	Sleep(Random(2000, 3000, 1))
	;;; in un altra finestra visita pipp8
	Send("{CTRLDOWN}t{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("http://www.facebok.com/pipp8{ENTER}")
	Sleep(Random(3000, 5000, 1))
	Send("{CTRLDOWN}f{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("Foto")
	Sleep(Random(1000, 2000, 1))
	Send("{ESC}")
	Sleep(Random(1000, 2000, 1))
	Send("{ENTER}")
	Sleep(Random(1000, 3000, 1))
	Send("{CTRLDOWN}f{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("privatissimo")
	Sleep(Random(1000, 2000, 1))
	Send("{ESC}")
	Sleep(Random(1000, 2000, 1))
	Send("{ENTER}")
	Sleep(Random(1000, 4000, 1))

	Sleep(Random(1000, 3000, 1))
	;Opt("MouseCoordMode", 0) ;1=absolute, 0=relative, 2=client
	;MouseClick("left",100,265,1)
	MouseClick("left",279,274,1)
	;;; Sfoglia un numero random di foto dall'album con attesa casuale l'una dall'altra
	$r=Random(1, 10, 1)
	;;; 47, 304 bottone indietro
	;;; 874, 304 bottone avanti ... dipende dalla taglia della finestra ???
	For $i = 0 To $r
		Sleep(Random(1000, 5000, 1))
		; MouseClick("left",680,364,1)
		Send("{RIGHT}")
	Next
	;;; Copia l'ultima immagine nella clipboard
	Sleep(Random(1000, 2000, 1))
	MouseClick("right",500,325,1) ;;; seleziona l'immagine
	Sleep(Random(1000, 2000, 1))
	Send("o")
	Sleep(Random(1000, 2000, 1))
	;;; Nasconde la fisnestra
	WinSetState("Firefox", "", @SW_HIDE)
	Opt("MouseCoordMode", 1) ;1=absolute, 0=relative, 2=client
	Sleep(Random(2000, 6000, 1))

EndFunc


Func OpenDocWordPad()
;	_WinWaitActivate("Program Manager","", 10000)
	Sleep(Random(3000, 5000, 1))
	Send("{CTRLDOWN}{ESC}{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("wordpad")
	Sleep(Random(2000, 3000, 1))
	Send("{ENTER}")
	Sleep(Random(3000, 5000, 1))
	Send("{CTRLDOWN}{END}{CTRLUP}")
	Sleep(Random(1000, 2000, 1))
	Send("L'immagine seguente{SPACE}è{SPACE}appena stata scaricata{SPACE}dall'album online di pipp8 su FaceBook:{ENTER}{ENTER}")
	Send("{CTRLDOWN}v{CTRLUP}{ENTER}{ENTER}")
	Sleep(Random(1000, 2000, 1))
	Send("{SHIFTDOWN}{F12}{SHIFTUP}")
	Sleep(Random(1000, 3000, 1))
	Send("Documento.rtf{ENTER}s") ; salva il documento eventualmente sovrascrivendo il file con lo stesso nome
	Sleep(Random(1000, 2000, 1))
	Send("{ALTDOWN}{F4}{ALTUP}")
	Sleep(Random(1000, 3000, 1))
	Send("{ENTER}s")
EndFunc

#region --- Internal functions Au3Recorder Start ---
Func _WinWaitActivate($title,$text,$timeout=0)
	WinWait($title,$text,$timeout)
	If Not WinActive($title,$text) Then WinActivate($title,$text)
	WinWaitActive($title,$text,$timeout)
EndFunc
#endregion --- Internal functions Au3Recorder End ---

#endregion --- Au3Recorder generated code End ---
