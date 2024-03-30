#! /usr/bin/perl


###########################################################
#
# script per il download delle pratiche pubblicate sul
# albo pretorio on-line di CST Sistemi Sud
#
# $Id: AlboPretorio.pl 83 2011-07-07 17:52:58Z cattaneo $
# $LastChangedDate: 2011-07-07 19:52:58 +0200(Gio, 07 Lug 2011) $
#
###########################################################




use HTML::Parser;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;
use Storable;
use Data::Dump;
  

sub handler {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($DumpHtml) {
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/javascript\:Dettagli\(([0-9]+)/) {
				$Found = 1;
				printf("ID: %d, ", $1) if ($Debug);
				if ($FirstPage < $1) {
					$FirstPage = $1;
				}
			}

		}
	 }
	 elsif ($event eq "start" && defined($tagname) && $tagname eq "input") {
	 	if (defined($attr) && exists($locAttr{'name'}) && $locAttr{'name'} eq "NumPubblicazioni") {
			if (exists($locAttr{'value'})) {
				$NumPubblicazioni = $locAttr{'value'};
				print( "Totale Pubblicazioni: ", $NumPubblicazioni, "\n") if ($Debug) ;		
				print( LOG "Totale Pubblicazioni: ", $NumPubblicazioni, "\n");		
			}
		}
	 }
}

sub handler1 {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($DumpHtml) {
#	     my @d = (uc(substr($event,0,1)) . " L$line C$column");
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/javascript\:Dettagli\(([0-9]+)/) {
				printf("ID %d pushed, ", $1) if ($Debug);
				push( @RefList, $1);
				$RefFound++;
			}

		}
	 }
}


sub handler2 {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($DumpHtml) {
#	     my @d = (uc(substr($event,0,1)) . " L$line C$column");
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/fecore01.*/) {
				printf("found documento allegato\n", $addr) if ($Debug);
				if ($addr=~/FNFH=(.*)$/) {
					$FoundAllegato = 1;
					&DownloadFile( $addr, $DestDir, $1);					
				}
			}
		}
	 }
}

sub handler3 {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($DumpHtml) {
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/javascript\:Dettagli\(([0-9]+)/) {
				printf("ID %d pushed, ", $1) if ($Debug);
				push( @RefList, $1);
				$RefFound++;
			}

		}
	 }
	 elsif ($event eq "start" && defined($tagname) && $tagname eq "input") {
	 	if (defined($attr) && exists($locAttr{'name'}) && $locAttr{'name'} eq "NumRecordMax") {
			if (exists($locAttr{'value'})) {
				$TotPagine = $locAttr{'value'};
				print( "Totale Pagine: ", $TotPagine, "\n") if ($Debug) ;		
				print( LOG "Totale Pagine: ", $TotPagine, "\n");		
			}
		}
		elsif (defined($attr) && exists($locAttr{'name'}) && $locAttr{'name'} eq "PaginaCorrente") {
			if (exists($locAttr{'value'})) {
				$PaginaCorrente = $locAttr{'value'};
				print( "Pagina Corrente: ", $PaginaCorrente, "\n") if ($Debug) ;		
				print( LOG "Pagina Corrente: ", $PaginaCorrente, "\n");		
			}
		}
		elsif (defined($attr) && exists($locAttr{'name'}) && $locAttr{'name'} eq "NumPubblicazioni") {
			if (exists($locAttr{'value'})) {
				$PNumPubblicazioni = $locAttr{'value'};
				print( "NumPubblicazioni: ", $NumPubblicazioni, "\n") if ($Debug) ;		
				print( LOG "NumPubblicazioni: ", $NumPubblicazioni, "\n");		
			}
		}
	 }
}


sub DownloadFile {
	my($url, $Dir, $FileName) = @_;
	
	my $Target = $Dir . "/" . $FileName; 
	$TotDownload++; # incrementa comunque il conteggio dei file scaricati
	if (-s $Target) {
		printf( "File %s exist, skipping download\n", $Target);
		printf( LOG "File %s exist, skipping download\n", $Target);		
	}
	else {
		my $cmd = "/usr/bin/wget --no-check-certificate --no-clobber " .
		 " --directory-prefix='" . $Dir . "' --output-document=" . '"' . $Target . '"' .
		 " --server-response  --wait=5 --random-wait --append-output='" . $Dir . "/log.txt'" .
		 " --page-requisites --no-directories -t5 --span-hosts -erobots=off --quiet " .
		 '"' . $home . "/urbi/progs/urp/" . $url . '"'; 
		
		 printf( "Downloading allegato n.%d: %s -> %s\n", $TotDownload, $FileName, $Dir);
		 printf( LOG "Downloading allegato n.%d: %s -> %s\n", $TotDownload, $FileName, $Dir);
		 # printf("cmd: %s\n", $cmd) if ($Debug);
		 system $cmd;
	}
}


sub DownloadDettagli {
	my($NumPub, $Dir) = @_;
	$DestDir = $Dir;
	$params = "/urbi/progs/urp/ur1ME001.sto?StwEvent=102&DB_NAME=l202191&IdMePubblica=";
	my $url = $home . $params . $NumPub . "&Archivio=";
	my $retry = 0;
	
	do {	
		print( "Download: ", $url, " -> ", $Dir, "\n") if ($Debug);
	
		my $p2 = HTML::Parser->new(api_version => 3);
		$p2->handler(default => \&handler2, "event, line, column, text, tagname, attr");
			
		my $agent = new LWP::UserAgent;
		
		my $response = $agent->request(GET $url);
		
		if ($response->is_success) {
			print "Parsing pagina dettagli pratica n.", $NumPub, " " if ($Debug);
			$FoundAllegato = 0;
			$p2->parse($response->content);
			
			if ($FoundAllegato) {
				$htmlFile = $Dir . "/" . $NumPub . ".html";
				open(OUT, "> $htmlFile") || die "Out file: $!";
				print OUT $response->content, "\n";
				close OUT;
				$retry = 0;
			}
			else {
				printf(LOG "Pagina %d, Dettagli non trovati ... retring\n", $NumPub);
				printf("Pagina %d, Dettagli non trovati ... retring\n", $NumPub);
				$retry++;
			}
		}
		else {
			print "HTTP request failed\n";
			print LOG "HTTP request failed\n";
			die("HTTP request for dettagli failed\n");
		}
	} while ($retry > 0 && $retry < 6);
	
	sleep(rand(60)); # attende un tempo random per evitare di essere bannati	
}	 





sub GetAllRefs {
	my($TreeNode, $TotPubs) = @_;
	
	my $ua = LWP::UserAgent->new;
	 
	my $p1 = HTML::Parser->new(api_version => 3);
	$p1->handler(default => \&handler1, "event, line, column, text, tagname, attr");
		
	my $response, $curPage, $itemXPage, $firstPub, $retry;

	do {
		$curPage = 0;
		$itemXPage = 10;
		$firstPub = 0;
		@RefList = (); # stack di tutte le pratiche del folder
			
		while( $firstPub < $TotPubs ) {
			
			my $req = POST $home . '/urbi/progs/urp/ur1ME001.sto',
		              [	StwEvent => '101',
						StwSubEvent => '',
						StwData => '',
						StwFldsName => '',
						StwOldRecord => '',
						QUERY_STRING => 'DB_NAME=l202191&DB_NAME=l202191&StwEvent=101&OpenTree=' . $TreeNode . '&Archivio=',
						urbi_return => '',
						urbi_fail => '',
						iter_univoco => '',
						iter_codice => '',
						indirizzo_wf => '',
						codice_attivita => '',
						WFDAT => '',
						WFCALL => '',
						AnnoAttivoOri => '',
						SOLCodice1 => '',
						DB_NAME => 'l202191',
						SOLCodice => '',
						SOLCodici => '',
						Archivio => '',
						OpenTree => $TreeNode,
						Tipologia => '',
						DaData => '',
						AData => '',
						OggettoType => '=',
						Oggetto => '',
						inizio => $firstPub,
						nxtini => $firstPub + $itemXPage * ($curPage + 1),
						preini => $firstPub - $itemXPage * ($curPage - 1),
						PRIMA => '',
						NumPagine => $TotPubs / $itemXPage + 1,
						passo => $itemXPage,
						NumRows_Pubblicazioni => $itemXPage,
						NumPubblicazioni => $TotPubs,
						PaginaCorrente => $curPage,
						NumRecordMax => $itemXPage,
		               ];
			
			$response = $ua->request( $req);
			if ($response->is_success) {
				print "Parsing List Page n.", $curPage, " (da: ", $firstPub, "): " if ($Debug);
				print LOG "Parsing List Page n.", $curPage, " (da: ", $firstPub, "): " if ($Debug);
				$RefFound = 0;
				$p1->parse($response->content);		
				if ($RefFound) {
					printf("got %d references\n", $RefFound);
					$retry = 0;
				}
				else {
					printf("Condizione anomala, nessuna referenza trovata per: %s ... retring\n", $NodeName[$i]);
					printf(LOG "Condizione anomala, nessuna referenza trovata per: %s ... retring\n", $NodeName[$i]);
					$retry++;
					sleep(180); # attende 3 min. nel caso sia stata bannata la connessione			
					last;
				}
			}
			else {
				print "HTTP request failed\n";
				print LOG "HTTP request failed\n";
				dir("HTTP request for references failed");
			}
			
			# step
			$firstPub = $firstPub + $itemXPage;
			$curPage++;
			sleep(rand(60)); # attende un tempo random per evitare di essere bannati			
		}
	} while ($retry > 0 && $retry < 6);
}


sub GetAllRefsStorico {
	
	my $ua = LWP::UserAgent->new;
	 
	my $p1 = HTML::Parser->new(api_version => 3);
	$p1->handler(default => \&handler3, "event, line, column, text, tagname, attr");
		
	my $response, $curPage, $itemXPage, $firstPub, $retry;

	do {
		$curPage = 0;
		$itemXPage = 10;
		$firstPub = 0;
		@RefList = (); # stack di tutte le pratiche del folder

		my $req = POST $home . '/urbi/progs/urp/ur1ME001.sto',
	              [	StwEvent => '101', StwSubEvent => '', StwData => '', StwFldsName => '', StwOldRecord => '',
					QUERY_STRING => 'DB_NAME=l202191', urbi_return => '', urbi_fail => '', iter_univoco => '',
					iter_codice => '', indirizzo_wf => '', codice_attivita => '', WFDAT => '', WFCALL => '',
					AnnoAttivoOri => '', SOLCodice1 => '', DB_NAME => 'l202191', SOLCodice => '', SOLCodici => '',
					Archivio => 'S', OpenTree => '', Tipologia => '', DaData => '', AData => '', OggettoType => '=',
					Oggetto => '', PaginaCorrente => '1', inizio => '0'];			

		$response = $ua->request( $req);
		if ($response->is_success) {
			print "Getting Archivio Storico. da: ", $PaginaCorrente, " su: ", $TotPagine, "): " if ($Debug);
			print LOG "Getting Archivio Storico. da: ", $PaginaCorrente, " su: ", $TotPagine, "): ";
			$TotPagine = 0;
			$p1->parse($response->content);		
			if ($TotPagine) {
				printf("got %d references\n", $RefFound);
				$retry = 0;
			}
		}
		else {
			die("errore");
		}
		
		while( $PaginaCorrente <= $TotPagine ) {
			
			my $req = POST $home . '/urbi/progs/urp/ur1ME001.sto',
		              [	StwEvent => '101', StwSubEvent => '', StwData => '', StwFldsName => '', StwOldRecord => '', QUERY_STRING => 'DB_NAME=l202191',
						urbi_return => '', urbi_fail => '', iter_univoco => '', iter_codice => '', indirizzo_wf => '', codice_attivita => '', 
						WFDAT => '', WFCALL => '', AnnoAttivoOr => '', SOLCodice1 => '', DB_NAME => 'l202191', SOLCodice => '', SOLCodici => '', 
						Archivio => 'S', OpenTree => '0', Tipologia => '', DaData => '', AData => '', OggettoType => '=', Oggetto => '',
						inizio => $PaginaCorrente * $itemXPage, nxtini => ($PaginaCorrente + 1) * $itemXPage, preini => $PaginaCorrente * $itemXPage - 20,
						PRIMA => '', NumPagine => $TotPagine, passo => '10', NumRows_Pubblicazioni => '10',
						NumPubblicazioni => $NumPubblicazioni, PaginaCorrente => $PaginaCorrente, NumRecordMax => $TotPagine];		              
		              
			$response = $ua->request( $req);
			if ($response->is_success) {
				print "Parsing List Page n.", $curPage, " (da: ", $firstPub, "): " if ($Debug);
				print LOG "Parsing List Page n.", $curPage, " (da: ", $firstPub, "): " if ($Debug);
				$RefFound = 0;
				$p1->parse($response->content);		
				if ($RefFound) {
					printf("got %d references\n", $RefFound);
					$retry = 0;
				}
				else {
					printf("Condizione anomala, nessuna referenza trovata per: %s ... retring\n", $NodeName[$i]);
					printf(LOG "Condizione anomala, nessuna referenza trovata per: %s ... retring\n", $NodeName[$i]);
					$retry++;
					sleep(180); # attende 3 min. nel caso sia stata bannata la connessione			
					last;
				}
			}
			else {
				print "HTTP request failed\n";
				print LOG "HTTP request failed\n";
				dir("HTTP request for references failed");
			}
			
			# step
			sleep(rand(60)); # attende un tempo random per evitare di essere bannati			
		}
	} while ($retry > 0 && $retry < 6);
}


############################################################
#
# Main section
#
############################################################

	# $homePage = "http://albo.comune.battipaglia.sa.it/urbi/progs/urp/ur1ME001.sto?DB_NAME=L202191";
	# $home = "http://albo.comune.battipaglia.sa.it";
	$home = "http://localhost:8001"; # for TCPDump
	$Debug = 1;
	$DumpHtml = 0;
	$TotDownload = 0;
	
	&InitData;
	
	my $p = HTML::Parser->new(api_version => 3);
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	open(LOG, ">AlboPretorio.log") || die "log file: $!";
		
	$agent = new LWP::UserAgent;
	
	GetAllRefsStorico;
	exit;
	
	for( $i = 0; $i <= $#NodeName; $i++) {
		printf( "i = %d  -> %s -> ", $i, $NodeName[$i]);
		printf( LOG "i = %d  -> %s -> ", $i, $NodeName[$i]);
		
		$url = $home . $SubSec[$i]; 

		$response = $agent->request(GET $url);
		
		$NumPubblicazioni = -1;
		$FirstPage = -1;
		$Found = 0;
		if ($response->is_success) {
			print "Parsing node:", $i, " contents: " if ($Debug);
			print LOG "Parsing node:", $i, " contents: ";
			$p->parse($response->content);		
		}
		else {
			print "HTTP request failed\n";
			print LOG "HTTP request failed\n";
			die("Failed processing node ", $i);
		}
		
		if ($Found) {
			printf("Totale Pubblicazioni %d (last: %d)\n", $NumPubblicazioni, $FirstPage);
			printf(LOG "Totale Pubblicazioni %d (last: %d)\n", $NumPubblicazioni, $FirstPage);
			
			GetAllRefs( $TreeNode[$i], $NumPubblicazioni);
			printf("Got %d references\n", $#RefList+1);
			printf(LOG "Got %d references\n", $#RefList+1);
			
			system "mkdir -p '$NodeName[$i]'";
			
			for($pag = 0; $pag <= $#RefList; $pag++) {
				DownloadDettagli( $RefList[$pag], $NodeName[$i]);
			}
		}
		else {
			print "No Page Found\n" if ($Debug);
			print LOG "No Page Found\n";
		}
	}
	
	printf("Scaricati %d documenti in totale\n", $TotDownload);
	
	exit;
	
	  
sub InitData {
		$TreeNode[0] = 38;
		$NodeName[0]  = "avvisi di accertamento";
		$SubSec[0] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=38&Archivio=";
				
		$TreeNode[1] = 9;
		$NodeName[1]  = "Avvisi di Gara";
		$SubSec[1] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=9&Archivio=";
	
		$TreeNode[2] = 10;
		$NodeName[2]  = "Bandi di Concorso";
		$SubSec[2] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=10&Archivio=";
	
		$TreeNode[3] = 7;
		$NodeName[3]  = "DETERMINAZIONI";
		$SubSec[3] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=7&Archivio=";
	
		$TreeNode[4] = 15;
		$NodeName[4]  = "ORDINANZE SINDACALI";
		$SubSec[4] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=15&Archivio=";
							
		$TreeNode[5] = 3;
		$NodeName[5]  = "Convocazione Consiglio Comunale";
		$SubSec[5] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=3&Archivio=";
	
		$TreeNode[6] = 17;
		$NodeName[6]  = "Convocazioni Commissioni Consiliari";
		$SubSec[6] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=17&Archivio=";
	
		$TreeNode[7] = 31;
		$NodeName[7]  = "Verbali Commissioni Consiliari";
		$SubSec[7] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=31&Archivio=";
	
		$TreeNode[8] = 39;
		$NodeName[8]  = "Delibere Azienda Pignatelli";
		$SubSec[8] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=39&Archivio=";
	
		$TreeNode[9] = 5;
		$NodeName[9]  = "Delibere di Consiglio";
		$SubSec[9] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=5&Archivio=";
	
		$TreeNode[10] = 6;
		$NodeName[10]  = "Delibere di Giunta";
		$SubSec[10] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=6&Archivio=";
	
		$TreeNode[11] = 45;
		$NodeName[11]  = "Cambio di nome e cognome";
		$SubSec[11] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=45&Archivio=";
	
		$TreeNode[12] = 37;
		$NodeName[12]  = "Cancellazione anagrafica per irreperi...";
		$SubSec[12] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=37&Archivio=";
	
		$TreeNode[13] = 12;
		$NodeName[13]  = "Pubblicazioni di Matrimonio";
		$SubSec[13] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=12&Archivio=";
	
		$TreeNode[14] = 18;
		$NodeName[14]  = "Altri avvisi";
		$SubSec[14] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=18&Archivio=";
	
		$TreeNode[15] = 30;
		$NodeName[15]  = "Avvisi provenienti da enti esterni";
		$SubSec[15] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=30&Archivio=";
	
		$TreeNode[16] = 29;
		$NodeName[16]  = "Elenco abusi edilizi";
		$SubSec[16] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=29&Archivio=";
	
		$TreeNode[17] = 43;
		$NodeName[17]  = "Incarichi e nomine";
		$SubSec[17] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=43&Archivio=";
	
		$TreeNode[18] = 28;
		$NodeName[18]  = "Patrocini gratuiti";
		$SubSec[18] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=28&Archivio=";
	
		$TreeNode[19] = 25;
		$NodeName[19]  = "Permessi di costruire";
		$SubSec[19] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=25&Archivio=";
	
		$TreeNode[20] = 27;
		$NodeName[20]  = "Permessi di installazione";
		$SubSec[20] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=27&Archivio=";
	
		$TreeNode[21] = 26;
		$NodeName[21]  = "Sanatoria opere edilizie";
		$SubSec[21] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=26&Archivio=";
	
		$TreeNode[22] = 33;
		$NodeName[22]  = "Provvedimenti Unici";
		$SubSec[22] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=33&Archivio=";
	
		$TreeNode[23] = 40;
		$NodeName[23]  = "Equitalia Polis";
		$SubSec[23] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=40&Archivio=";
	
		$TreeNode[24] = 44;
		$NodeName[24]  = "Vari";
		$SubSec[24] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=44&Archivio=";
	
		$TreeNode[25] = 36;
		$NodeName[25]  = "VIOLAZIONI C.D.S.";
		$SubSec[25] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=36&Archivio=";
	
		$TreeNode[26] = 42;
		$NodeName[26]  = "Consulta disabili";
		$SubSec[26] = "/urbi/progs/urp/ur1ME001.sto?DB_NAME=l202191&StwEvent=101&OpenTree=42&Archivio=";
}
	  