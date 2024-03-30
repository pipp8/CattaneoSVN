#! /usr/bin/perl

###########################################################
#
# script per il download delle immagini da flickr.com
# utilizzando pixel-peeper.com per accedere solo alle 
# immagini real size e non passare per l'autenticazione
#
# $Id: FetchFlickrImages.pl 336 2012-05-01 18:50:52Z cattaneo $
# $LastChangedDate: 2012-05-01 20:50:52 +0200(Mar, 01 Mag 2012) $
#
###########################################################


use HTML::Parser;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;
use Data::Dump;
use Getopt::Long;
use Class::Struct;

struct( UserInfo => {
	cnt => '$', user => '$',
	linkArray => '@'
});


BEGIN { $| = 1 }

$svnInfo = '$Id: FetchFlickrImages.pl 336 2012-05-01 18:50:52Z cattaneo $'; 

sub handler {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 my($ref_array, $obj, $c);
	 	 
     if ($Debug) {
#	     my @d = (uc(substr($event,0,1)) . " L$line C$column");
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print LOG Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/phid=(.*)\&url=http:\/\/www\.flickr\.com\/photos\/(.*)\/(.*)/) {
				print LOG "phid = ", $1, ", user = ", $2, ", phid2 = ", $3, "\n" if ($Debug);
				$photoid = $1;
				$UserID = $2;
				$link = "http://www.flickr.com/photos/" . $UserID . "/" . $photoid . "/sizes/o";
				$TotalLinkNumber++;
				if (! exists($UserLinks{$UserID})) {
					$obj = UserInfo->new;
					$obj->cnt( 1);
					$obj->user( $UserID);
					$ref_array = $obj->linkArray;
					push(@$ref_array, $link);
					$UserLinks{$UserID} = $obj;
				}
				else {
					$obj = $UserLinks{$UserID};
					$c = $obj->cnt + 1;
					$obj->cnt($c);
					$ref_array = $obj->linkArray;
					push(@$ref_array, $link);
					$UserLinks{$UserID} = $obj;					
				}
			}
		}
	 }
	 elsif ($event eq "text") {
	 	if ($TotalPageNumber == $UnknownNumber &&
	 			$text=~/There are (\d+) photos matching your search criteria. Showing page 1 \/ (\d+)/) {
	 		$TotalPhotoNumber = $1;
	 		$TotalPageNumber = $2;
 			printf($outFH "Got %d link(s) (%d pages) from Peeper for model: %s (ID = %d)\n",
 				$TotalPhotoNumber, $TotalPageNumber, $cameraLongName, $cameraModel);
	 	}
	 }
}

# <a href='/redirect/?phid=6013896609&url=http://www.flickr.com/photos/48972666@N02/6013896609'
# target='_blank'><img src='http://farm7.staticflickr.com/6029/6013896609_ce2a8850fe_m.jpg'></a>

# <a href='/redirect/?phid=6013896609&url=http%3A%2F%2Fwww.flickr.com%2Fphoto_zoom.gne%3Fid%3D6013896609%26size%3Do'
#Êtarget='_blank'>Go directly to the full-size image</a>

# http://www.flickr.com/photos/janutek/6013896609/sizes/o/ -> http://www.flickr.com/photos/48972666@N02/6013896609/sizes/o/

# <a href="http://farm7.staticflickr.com/6029/6013896609_314ea52f3c_o_d.jpg">
# Scarica questa foto in formato originale</a>

# http://farm7.staticflickr.com/6029/6013896609_314ea52f3c_o_d.jpg

sub DownloadPhoto {
	my($link, $UserID) = @_;
	
	$outDir = $DownloadDirectory . $cameraLongName . "/" . $UserID;
	system "mkdir -p '$outDir'";

	$link=~/(\/[0-9a-z_]*o_d\.jpg)/;
	$fileTarget = $outDir . $1;
	if (-s $fileTarget) {
		printf( $outFH "File %s exists, skipping\n", $fileTarget);
	}
	else {
		$cmd = "/usr/bin/wget --no-check-certificate --no-clobber " .
		 " --directory-prefix='" . $outDir . "'" .
		 " --server-response  --wait=5 --random-wait --append-output='" . $outDir . "/log.txt'" .
		 " --page-requisites --no-directories -t5 --span-hosts -A.jpg,.jpeg,.jpg.1,.jpg.2,.jpeg.1,.jpeg.2 -erobots=off " .
		 " --exclude-directories='https://login.yahoo.com/config/**,login\?.src*' " . $link; 
		 
		if ($Debug) {
			 print LOG "Command: ", $cmd, "\n";
		}
		my $ora = localtime(time);
		printf( $outFH "Downloading photo n. %d (%s) -> %s\n", ++$PhotoCnt, $link, $ora);
		system $cmd;
	}
}	 


sub DownloadModel( $$) {
	my($camera, $stat) = @_;
	
	$cameraLongName = $ID2ModelMap->{$camera};
	
	my $p = HTML::Parser->new(api_version => 3);
	my $url = '';
	
	$TotalPageNumber = $UnknownNumber;
	
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	%UserLinks = ();	# tutti i link estratti verranno messi in questa hash table
	$TotalLinkNumber = 0;
	
	$agent = new LWP::UserAgent;
	
	$piperURL = 'http://www.pixel-peeper.com/cameras/?';
	$tail = '&perpage=30&iso_min=none&iso_max=none&exp_min=none&exp_max=none&res=1&digicam=0';

	my $i;
	for( $i = 1; $i <= $TotalPageNumber; $i++ ) {
		$url = $piperURL . "camera=" . $camera . $tail . "&p=" . $i; 

		$response = $agent->request(GET $url);
		
		if ($response->is_success) {
			printf(LOG "OK, parsing %s page: %d contents\n", $url, $i) if ($Debug);
			$p->parse($response->content);		
		}
		elsif ($response->is_redirect) {
			print $outFH $response->as_string, "\n";
			print $outFH "redirect:\n";
			print $outFH $response->base, "\n";
			print $outFH $response->header( Location), "\n";
		}
		
		if ($TotalPageNumber == $UnknownNumber) {
 			printf($outFH "Could not find any photos matching your search criteria from %s (model: %s, ID = %d)\n",
 				 $piperURL, $cameraLongName, $cameraModel);
			last;			
		}
		else {
			print $outFH ".";
		}			
	}
	@AllUsers = keys %UserLinks;
	printf( $outFH "\nTrovati %d Links / %d Utenti\n", $TotalLinkNumber, scalar @AllUsers);
	if ($stat) {
		my ($TotaleLink) = 0;
		foreach $user (@AllUsers) {
			my $obj = $UserLinks{$user};
			my $ArrayRef = $obj->linkArray;
			printf( $outFH "\tUser: %s -> %d (%d) photos\n", $user, $obj->cnt, scalar( @$ArrayRef));
			$TotaleLink += $obj->cnt if ($obj->cnt > $MinimumPhotoExpected);
		}
		printf( $outFH "\tTotal Photos Expected: %d\n\n", $TotaleLink);		
	}
	else {
		DownloadList();
	} 
}	


sub handler2 {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($Debug) {
#	     my @d = (uc(substr($event,0,1)) . " L$line C$column");
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
	     print LOG Data::Dump::dump(@d), "\n";
     }
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		if (defined($attr) && exists($locAttr{'href'})) {
			$addr = $locAttr{'href'};
			if ($addr=~/(.*)_o_d.jpg/) {
				print $outFH $addr, "\n";
				DownloadPhoto( $addr, $UserID);
				$GotIt = 1;
			}
		}
	 }
}

	
sub DownloadList() {

	my $p = HTML::Parser->new(api_version => 3);	
	$p->handler(default => \&handler2, "event, line, column, text, tagname, attr");

	my $agent = new LWP::UserAgent;
	my($url, $user, $obj, $response);	
	
	
	foreach $user (keys %UserLinks) {
		$obj = $UserLinks{$user};
		if ($obj->cnt > $MinimumPhotoExpected) {
			
			$PhotoCnt = 0;
			$arrayRef = $obj->linkArray;
			foreach $url (@$arrayRef) {
				$UserID = $obj->user;
				
				print $outFH "Link: ", $url, " -> ";
				$response = $agent->request(GET $url);
				
				$GotIt = 0;
				if ($response->is_success) {
					print LOG "OK, parsing full size page:", $url, " contents:\n";
					$p->parse($response->content);		
				}
				elsif ($response->is_redirect) {
					print $outFH $response->as_string, "\n";
					print $outFH "redirect:\n";
					print $outFH $response->base, "\n";
					print $outFH $response->header( Location), "\n";
				}
				if ($GotIt eq 0) {
					print $outFH "not found\n";
				}
			}
			printf( $outFH "Got %d photos / %d links\n", $PhotoCnt,  $obj->cnt);
		}
		else {
			printf( $outFH "User: %s skipped, only %d (%d minimum) photos\n", $obj->user, $obj->cnt, $MinimumPhotoExpected);			
		}
	}
} 


sub PrintSiteStatistics() {

	if ($logfile) {
		if ($startModel) {
			open(OUT, ">>SiteStatistics.log") || die "Out file: $!";		# append to the exiting file	
		}
		else {
			open(OUT, ">SiteStatistics.log") || die "Out file: $!";
		}
		$outFH = *OUT;
	}
	else {
		$outFH = *STDOUT;
	}
	select( $outFH);
	$| = 1;		# make the file unbuffered
	
	@modelList =  sort( {$a <=> $b} keys( %$ID2ModelMap));
	if ($startModel) {
		defined($$ID2ModelMap{$startModel}) || die("$startModel unknown start model");
		do {
			$m = shift( @modelList) ; # skip models already downloaded
		} while ($m ne $startModel);
	}
	foreach $cameraModel (@modelList) {
		print $outFH $$ID2ModelMap{$cameraModel}, "\n";
				
		DownloadModel($cameraModel, 1);
	}	
}


sub usage
{
  print "Unknown option: @_\n" if ( @_ );
  print "usage: FetchFlickrImages.pl [--debug] [--version]Ê[--help|-?] [--log] [--compact|--reflex] --stat|--all--model MODELID [--model ...] [--startfter MODELID]\n";
  exit;
}


############################################################
#
# Main section
#
############################################################

	$DownloadDirectory = "/Users/pipp8/Downloads/Photos/";
#	$DownloadDirectory = "/home/ifnew/LOCAL_IMG_DATASE/Flickr/";
	
	$Debug = 0;
	$logfile = 0;
	$UnknownNumber = 1000000;
	$MinimumPhotoExpected = 30; # utenti con meno di 30 foto non saranno scaricati
	$lastCameraID = 934;
	$cameraModel = -1;
	$startModel = 0;
	
	
	InitMap();
	
	my ($help, $version, $stat, $compact, $reflex, @models, $all);
 
#-- prints usage if no command line parameters are passed or there is an unknown parameter or help option is passed
	usage() if ( @ARGV < 1 or
          ! GetOptions('debug' => \$Debug, 'version' => \$version, 'help|?' => \$help, 'log' => \$logfile,
           'compact' => \$compact, 'reflex' => \$reflex,
           'stat' => \$stat, 'all' => \$all, 'model=i' => \@models, 'startafter=i' => \$startModel)
          or defined $help );
 
 	if ($version) {
 		printf( "%s\n", substr( $svnInfo, 5));
 		exit;
 	}
 	
	if ($Debug) {
		open(LOG, ">FetchFlickrImages.log") || die "log file: $!";
	}
	
	if ($reflex) {
		$ModelsMap = \%Reflex2ID;
		$ID2ModelMap = \%ID2Reflex;
	}
	elsif ($compact) {
		$ModelsMap = \%Compact2ID;
		$ID2ModelMap = \%ID2Compact;		
	}
	else {
		# default compact models
		$ModelsMap = \%Reflex2ID;
		$ID2ModelMap = \%ID2Reflex;		
	}
	
	if ($stat) {
		PrintSiteStatistics();
	}
	else {
		@models = sort( {$a <=> $b} keys( %$ID2ModelMap)) if ($all);
		if (scalar @models == 0) {
			print scalar @models, "\n";
			usage();
		}
		else {
			if ($startModel) {
				defined($$ID2ModelMap{$startModel}) || die("$startModel unknown start model");
				do {
					$m = shift( @models) ; # skip models already downloaded
				} while ($m ne $startModel);
			}
			foreach $cameraModel (@models) {

				if ($logfile) {
					open(OUT, ">$$ID2ModelMap{$cameraModel}" . ".log") || die "Out file: $!";
					$outFH = *OUT;
				}
				else {
					$outFH = *STDOUT;
				}
				select( $outFH);
				$| = 1;		# make the file unbuffered

				if (!defined($$ID2ModelMap{$cameraModel})) {
					printf($outFH, "Unknown model id: %d, compact = %d, reflex = %d\n", $cameraModel, $compact. $reflex);
					close OUT if (defined($logfile));
					next;
				}					
				printf( $outFH  "Processing model: %s\n", $$ID2ModelMap{$cameraModel});
				
				DownloadModel($cameraModel, 0);
				
				close OUT if (defined($logfile));
			}
		}
	}

	exit;
	
# scarica tutti modelli nel db
#		foreach $cameraModel ( keys( %ID2ModelMap)) {
#			print OUT $ID2ModelMap{$cameraModel}, "\n";
#			open(OUT, ">parser-" . $cameraModel . ".txt") || die "Out file: $!";
#			
#			DownloadModel($cameraModel);
#			close OUT;
#		}
	
	
#	$url1 = 'http://www.flickr.com/photo_zoom.gne?id=1484821596&size=o';
#	$url2 = 'http://www.flickr.com/photos/nosamk/1484821596/sizes/o/';
#	$url3 = 'http://farm2.static.flickr.com/1178/1484821596_cd4ba2cc0a_o_d.jpg';
#	$url4 = 'http://www.flickr.com/1178/1484821596_cd4ba2cc0a_o_d.jpg';
#	$url = 'http://www.flickr.com/photo_zoom.gne';
#	$MainPage = "main.html";
	
sub InitMap {
# reflex
	$ID2Reflex{ 384} = "Panasonic Lumix DMC-L1";
 	$ID2Reflex{ 170} = "Leica M8";
 	$ID2Reflex{ 33} = "Canon EOS-1DS Mark II";
 	$ID2Reflex{ 1284} = "Nikon D5000";
 	$ID2Reflex{ 7} = "Canon EOS 20D";
	$ID2Reflex{ 604} = "Canon EOS-1D Mark III";
	$ID2Reflex{ 227} = "Sigma SD9";
	$ID2Reflex{ 26} = "Fujifilm FinePix S2 Pro";
	$ID2Reflex{ 1375} = "Sony Alpha DSLR-A500";
	$ID2Reflex{ 1301} = "Sony Alpha DSLR-A230";
	$ID2Reflex{ 433} = "Pentax Xist D";
	$ID2Reflex{ 767} = "Nikon D3";
	$ID2Reflex{ 16} = "Canon EOS D60";
	$ID2Reflex{ 55} = "Canon EOS-1D";
	$ID2Reflex{ 775} = "Olympus E-3";
	$ID2Reflex{ 108} = "Olympus E-500";
	$ID2Reflex{ 752} = "Samsung GX-10";
	$ID2Reflex{ 1251} = "Sony Alpha DSLR-A330";
	$ID2Reflex{ 714} = "Canon EOS 40D";
	$ID2Reflex{ 430} = "Pentax Xist DS";
	$ID2Reflex{ 109} = "Pentax Xist DL2";
	$ID2Reflex{ 871} = "Nikon D60";
	$ID2Reflex{ 879} = "Canon EOS 450D";
	$ID2Reflex{ 10} = "Nikon D70S";
	$ID2Reflex{ 584} = "Olympus E-410";
	$ID2Reflex{ 1338} = "Pentax K-x";
	$ID2Reflex{ 1263} = "Pentax K-7";
	$ID2Reflex{ 1319} = "Leica M9";
	$ID2Reflex{ 11} = "Nikon D100";
	$ID2Reflex{ 225} = "Nikon D1X";
	$ID2Reflex{ 934} = "Sony DSLR-A300";
	$ID2Reflex{ 726} = "Sony Alpha DSLR-A700";
	$ID2Reflex{ 1371} = "Sony Alpha DSLR-A550";
	$ID2Reflex{ 167} = "Olympus E-330";
	$ID2Reflex{ 48} = "Canon EOS-1Ds";
	$ID2Reflex{ 1324} = "Sony Alpha DSLR-A850";
	$ID2Reflex{ 174} = "Epson R-D1";
	$ID2Reflex{ 1181} = "Nikon D3X";
	$ID2Reflex{ 983} = "Canon EOS 1000D";
	$ID2Reflex{ 221} = "Nikon D2Xs";
	$ID2Reflex{ 429} = "Pentax Xist DL";
	$ID2Reflex{ 12} = "Canon EOS 30D";
	$ID2Reflex{ 668} = "Samsung GX-1S";
	$ID2Reflex{ 1335} = "Canon EOS 7D";
	$ID2Reflex{ 431} = "Pentax Xist DS2";
	$ID2Reflex{ 1077} = "Sony Alpha DSLR-A900";
	$ID2Reflex{ 774} = "Nikon D300";
	$ID2Reflex{ 1201} = "Canon EOS 500D";
	$ID2Reflex{ 499} = "Fujifilm FinePix S5 Pro";
	$ID2Reflex{ 886} = "Pentax K200D";
	$ID2Reflex{ 432} = "Pentax K110D";
	$ID2Reflex{ 19} = "Canon EOS 5D";
	$ID2Reflex{ 833} = "Canon EOS-1Ds Mark III";
	$ID2Reflex{ 370} = "Pentax K100D";
	$ID2Reflex{ 860} = "Sony Alpha DSLR-A200";
	$ID2Reflex{ 165} = "Olympus E-300";
	$ID2Reflex{ 17} = "Nikon D2H";
	$ID2Reflex{ 2} = "Nikon D200";
	$ID2Reflex{ 166} = "Olympus E-1";
	$ID2Reflex{ 1} = "Nikon D50";
	$ID2Reflex{ 567} = "Samsung GX-1L";
	$ID2Reflex{ 944} = "Olympus E-420";
	$ID2Reflex{ 859} = "Pentax K20D";
	$ID2Reflex{ 1113} = "Canon EOS 5D Mark II";
	$ID2Reflex{ 1039} = "Nikon D90";
	$ID2Reflex{ 25} = "Sigma SD10";
	$ID2Reflex{ 403} = "Nikon D40";
	$ID2Reflex{ 614} = "Nikon D40X";
	$ID2Reflex{ 14} = "Nikon D80";
	$ID2Reflex{ 172} = "Epson R-D1s";
	$ID2Reflex{ 24} = "Konica Minolta Dynax 7D";
	$ID2Reflex{ 224} = "Fujifilm FinePix S1 Pro";
	$ID2Reflex{ 1052} = "Canon EOS 50D";
	$ID2Reflex{ 1366} = "Nikon D3000";
	$ID2Reflex{ 104} = "Nikon D2X";
	$ID2Reflex{ 605} = "Olympus E-400";
	$ID2Reflex{ 385} = "Leica Digilux 3";
	$ID2Reflex{ 22} = "Konica Minolta Dynax 5D";
	$ID2Reflex{ 864} = "Sony Alpha DSLR-A350";
	$ID2Reflex{ 46} = "Sony Alpha DSLR-A100";
	$ID2Reflex{ 219} = "Nikon D2Hs";
	$ID2Reflex{ 23} = "Canon EOS D30";
	$ID2Reflex{ 13} = "Canon EOS 300D";
	$ID2Reflex{ 1309} = "Nikon D300S";
	$ID2Reflex{ 388} = "Pentax K10D";
	$ID2Reflex{ 1426} = "Canon EOS-1D Mark IV";
	$ID2Reflex{ 972} = "Nikon D700";
	$ID2Reflex{ 3} = "Fujifilm FinePix S3 Pro";
	$ID2Reflex{ 9} = "Canon EOS 10D";
	$ID2Reflex{ 1260} = "Pentax K2000";
	$ID2Reflex{ 38} = "Canon EOS-1D Mark II";
	$ID2Reflex{ 8} = "Canon EOS 400D";
	$ID2Reflex{ 568} = "Sigma SD14";
	$ID2Reflex{ 4} = "Nikon D70";
	$ID2Reflex{ 745} = "Pentax K100 Super";
	$ID2Reflex{ 629} = "Olympus E-510";
	$ID2Reflex{ 43} = "Canon EOS-1D Mark II N";
	$ID2Reflex{ 5} = "Canon EOS 350D";
	
	$Reflex2ID{ 'Sony Alpha DSLR-A900'} = 1077;
	$Reflex2ID{ 'Pentax K20D'} = 859;
	$Reflex2ID{ 'Nikon D80'} = 14;
	$Reflex2ID{ 'Canon EOS 20D'} = 7;
	$Reflex2ID{ 'Canon EOS-1Ds Mark III'} = 833;
	$Reflex2ID{ 'Nikon D3000'} = 1366;
	$Reflex2ID{ 'Nikon D200'} = 2;
	$Reflex2ID{ 'Canon EOS 40D'} = 714;
	$Reflex2ID{ 'Nikon D700'} = 972;
	$Reflex2ID{ 'Pentax K-x'} = 1338;
	$Reflex2ID{ 'Nikon D100'} = 11;
	$Reflex2ID{ 'Nikon D90'} = 1039;
	$Reflex2ID{ 'Nikon D300S'} = 1309;
	$Reflex2ID{ 'Nikon D3'} = 767;
	$Reflex2ID{ 'Sony Alpha DSLR-A850'} = 1324;
	$Reflex2ID{ 'Pentax K100 Super'} = 745;
	$Reflex2ID{ 'Canon EOS 5D Mark II'} = 1113;
	$Reflex2ID{ 'Olympus E-400'} = 605;
	$Reflex2ID{ 'Sigma SD9'} = 227;
	$Reflex2ID{ 'Nikon D2H'} = 17;
	$Reflex2ID{ 'Canon EOS 5D'} = 19;
	$Reflex2ID{ 'Canon EOS 500D'} = 1201;
	$Reflex2ID{ 'Epson R-D1s'} = 172;
	$Reflex2ID{ 'Samsung GX-10'} = 752;
	$Reflex2ID{ 'Nikon D60'} = 871;
	$Reflex2ID{ 'Canon EOS 7D'} = 1335;
	$Reflex2ID{ 'Canon EOS 10D'} = 9;
	$Reflex2ID{ 'Nikon D1X'} = 225;
	$Reflex2ID{ 'Pentax Xist DL2'} = 109;
	$Reflex2ID{ 'Canon EOS 1000D'} = 983;
	$Reflex2ID{ 'Sony Alpha DSLR-A230'} = 1301;
	$Reflex2ID{ 'Canon EOS 400D'} = 8;
	$Reflex2ID{ 'Fujifilm FinePix S2 Pro'} = 26;
	$Reflex2ID{ 'Pentax Xist DL'} = 429;
	$Reflex2ID{ 'Pentax Xist D'} = 433;
	$Reflex2ID{ 'Nikon D40'} = 403;
	$Reflex2ID{ 'Pentax K10D'} = 388;
	$Reflex2ID{ 'Pentax K200D'} = 886;
	$Reflex2ID{ 'Nikon D2X'} = 104;
	$Reflex2ID{ 'Olympus E-420'} = 944;
	$Reflex2ID{ 'Olympus E-410'} = 584;
	$Reflex2ID{ 'Canon EOS-1D Mark II'} = 38;
	$Reflex2ID{ 'Canon EOS D30'} = 23;
	$Reflex2ID{ 'Pentax K100D'} = 370;
	$Reflex2ID{ 'Nikon D2Xs'} = 221;
	$Reflex2ID{ 'Panasonic Lumix DMC-L1'} = 384;
	$Reflex2ID{ 'Leica M9'} = 1319;
	$Reflex2ID{ 'Canon EOS-1Ds'} = 48;
	$Reflex2ID{ 'Canon EOS 30D'} = 12;
	$Reflex2ID{ 'Fujifilm FinePix S1 Pro'} = 224;
	$Reflex2ID{ 'Olympus E-1'} = 166;
	$Reflex2ID{ 'Canon EOS-1DS Mark II'} = 33;
	$Reflex2ID{ 'Leica M8'} = 170;
	$Reflex2ID{ 'Nikon D40X'} = 614;
	$Reflex2ID{ 'Konica Minolta Dynax 5D'} = 22;
	$Reflex2ID{ 'Canon EOS D60'} = 16;
	$Reflex2ID{ 'Canon EOS-1D Mark IV'} = 1426;
	$Reflex2ID{ 'Olympus E-3'} = 775;
	$Reflex2ID{ 'Canon EOS 350D'} = 5;
	$Reflex2ID{ 'Olympus E-500'} = 108;
	$Reflex2ID{ 'Olympus E-300'} = 165;
	$Reflex2ID{ 'Pentax Xist DS'} = 430;
	$Reflex2ID{ 'Pentax Xist DS2'} = 431;
	$Reflex2ID{ 'Canon EOS 450D'} = 879;
	$Reflex2ID{ 'Olympus E-510'} = 629;
	$Reflex2ID{ 'Sony DSLR-A300'} = 934;
	$Reflex2ID{ 'Sony Alpha DSLR-A330'} = 1251;
	$Reflex2ID{ 'Pentax K-7'} = 1263;
	$Reflex2ID{ 'Nikon D70S'} = 10;
	$Reflex2ID{ 'Pentax K2000'} = 1260;
	$Reflex2ID{ 'Canon EOS 50D'} = 1052;
	$Reflex2ID{ 'Pentax K110D'} = 432;
	$Reflex2ID{ 'Canon EOS-1D Mark II N'} = 43;
	$Reflex2ID{ 'Fujifilm FinePix S5 Pro'} = 499;
	$Reflex2ID{ 'Olympus E-330'} = 167;
	$Reflex2ID{ 'Fujifilm FinePix S3 Pro'} = 3;
	$Reflex2ID{ 'Sigma SD10'} = 25;
	$Reflex2ID{ 'Konica Minolta Dynax 7D'} = 24;
	$Reflex2ID{ 'Canon EOS-1D Mark III'} = 604;
	$Reflex2ID{ 'Nikon D3X'} = 1181;
	$Reflex2ID{ 'Samsung GX-1S'} = 668;
	$Reflex2ID{ 'Sony Alpha DSLR-A550'} = 1371;
	$Reflex2ID{ 'Nikon D50'} = 1;
	$Reflex2ID{ 'Nikon D300'} = 774;
	$Reflex2ID{ 'Canon EOS-1D'} = 55;
	$Reflex2ID{ 'Nikon D70'} = 4;
	$Reflex2ID{ 'Sony Alpha DSLR-A350'} = 864;
	$Reflex2ID{ 'Nikon D2Hs'} = 219;
	$Reflex2ID{ 'Nikon D5000'} = 1284;
	$Reflex2ID{ 'Sony Alpha DSLR-A200'} = 860;
	$Reflex2ID{ 'Sony Alpha DSLR-A500'} = 1375;
	$Reflex2ID{ 'Leica Digilux 3'} = 385;
	$Reflex2ID{ 'Epson R-D1'} = 174;
	$Reflex2ID{ 'Sony Alpha DSLR-A700'} = 726;
	$Reflex2ID{ 'Sony Alpha DSLR-A100'} = 46;
	$Reflex2ID{ 'Samsung GX-1L'} = 567;
	$Reflex2ID{ 'Canon EOS 300D'} = 13;
	$Reflex2ID{ 'Sigma SD14'} = 568;
	
	# digicams
	$Compact2ID{ 'Leica Digilux 2'} = 260;
	$Compact2ID{ 'PowerShot S400'} = 40;
	$Compact2ID{ 'FinePix Z100fd'} = 1002;
	$Compact2ID{ 'EASYSHARE V570'} = 253;
	$Compact2ID{ 'Optio T10'} = 273;
	$Compact2ID{ 'PowerShot S50'} = 360;
	$Compact2ID{ 'EASYSHARE LS443'} = 252;
	$Compact2ID{ 'PowerShot A550'} = 716;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ7'} = 149;
	$Compact2ID{ 'Optio SV'} = 118;
	$Compact2ID{ 'Digimax V6'} = 267;
	$Compact2ID{ 'PowerShot G9'} = 751;
	$Compact2ID{ 'FinePix F60FD'} = 1185;
	$Compact2ID{ 'EASYSHARE DX7630'} = 324;
	$Compact2ID{ 'Caplio R8'} = 883;
	$Compact2ID{ 'FinePix E510'} = 672;
	$Compact2ID{ 'Panasonic Lumix DMC-LZ10'} = 921;
	$Compact2ID{ 'Digimax S800'} = 268;
	$Compact2ID{ 'Lumix DMC-LZ10'} = 921;
	$Compact2ID{ 'X1'} = 1434;
	$Compact2ID{ 'FinePix Z1'} = 362;
	$Compact2ID{ 'D540'} = 310;
	$Compact2ID{ 'DiMAGE A200'} = 461;
	$Compact2ID{ 'FinePix J150W'} = 1128;
	$Compact2ID{ 'Lumix DMC-TZ2'} = 675;
	$Compact2ID{ 'Lumix DMC-LS3'} = 349;
	$Compact2ID{ 'EASYSHARE C533'} = 594;
	$Compact2ID{ 'Exilim EX-S10'} = 967;
	$Compact2ID{ 'FinePix S9500'} = 45;
	$Compact2ID{ 'Optio A30'} = 590;
	$Compact2ID{ 'Canon PowerShot A590 IS'} = 948;
	$Compact2ID{ 'Canon PowerShot G9'} = 751;
	$Compact2ID{ 'EasyShare M863'} = 1008;
	$Compact2ID{ 'Fujifilm FinePix F200EXR'} = 1184;
	$Compact2ID{ 'Optio S60'} = 62;
	$Compact2ID{ 'EASYSHARE P712'} = 381;
	$Compact2ID{ 'Coolpix 7900'} = 292;
	$Compact2ID{ 'Lumix DMC-FX35'} = 870;
	$Compact2ID{ 'Lumix DMC-LZ7'} = 647;
	$Compact2ID{ 'PowerShot S410'} = 269;
	$Compact2ID{ 'C-LUX 2'} = 602;
	$Compact2ID{ 'Caplio GX100'} = 592;
	$Compact2ID{ 'Optio M20'} = 63;
	$Compact2ID{ 'DSC-V1'} = 114;
	$Compact2ID{ 'Digimax L85'} = 543;
	$Compact2ID{ 'Optio A40'} = 919;
	$Compact2ID{ 'Coolpix 4600'} = 133;
	$Compact2ID{ 'Caplio GX200'} = 998;
	$Compact2ID{ 'FinePix 6900 Zoom'} = 417;
	$Compact2ID{ 'Coolpix L4'} = 290;
	$Compact2ID{ 'FinePix S6000fd'} = 412;
	$Compact2ID{ 'S730'} = 765;
	$Compact2ID{ 'Stylus 9000'} = 1373;
	$Compact2ID{ 'Exilim EX-Z1200'} = 846;
	$Compact2ID{ 'Exilim EX-Z120'} = 147;
	$Compact2ID{ 'DiMAGE Z20'} = 470;
	$Compact2ID{ 'PowerShot S5 IS'} = 851;
	$Compact2ID{ 'Caplio R5'} = 96;
	$Compact2ID{ 'PowerShot A80'} = 237;
	$Compact2ID{ 'EASYSHARE Z612'} = 394;
	$Compact2ID{ 'EASYSHARE DX7440'} = 338;
	$Compact2ID{ 'Lumix DMC-FZ8'} = 545;
	$Compact2ID{ 'Z1285'} = 999;
	$Compact2ID{ 'DiMAGE Z5'} = 459;
	$Compact2ID{ 'Coolpix S1'} = 286;
	$Compact2ID{ 'FinePix S3000'} = 451;
	$Compact2ID{ 'Ricoh GR Digital III'} = 1297;
	$Compact2ID{ 'Digimax 370'} = 266;
	$Compact2ID{ 'Stylus 830'} = 836;
	$Compact2ID{ 'Coolpix 990'} = 283;
	$Compact2ID{ 'DSC-W170'} = 1062;
	$Compact2ID{ 'Exilim EX-Z1050'} = 601;
	$Compact2ID{ 'PowerShot S60'} = 238;
	$Compact2ID{ 'PowerShot A650 IS'} = 807;
	$Compact2ID{ 'DSC-T7'} = 352;
	$Compact2ID{ 'PowerShot SD890 IS'} = 1053;
	$Compact2ID{ 'FinePix J12'} = 1074;
	$Compact2ID{ 'Coolpix 885'} = 177;
	$Compact2ID{ 'PowerShot SD780 IS'} = 1221;
	$Compact2ID{ 'DSC-H5'} = 193;
	$Compact2ID{ 'EASYSHARE DX6490'} = 250;
	$Compact2ID{ 'Coolpix 8800'} = 72;
	$Compact2ID{ 'PowerShot SD400'} = 57;
	$Compact2ID{ 'Lumix DMC-FS5'} = 962;
	$Compact2ID{ 'Sony DSC-H5'} = 193;
	$Compact2ID{ 'EasyShare Z980'} = 1257;
	$Compact2ID{ 'PowerShot SD990 IS'} = 1136;
	$Compact2ID{ 'Digimax S1000'} = 656;
	$Compact2ID{ 'EASYSHARE DX6440'} = 333;
	$Compact2ID{ 'FinePix S5800'} = 805;
	$Compact2ID{ 'PowerShot G6'} = 27;
	$Compact2ID{ 'Optio W60'} = 966;
	$Compact2ID{ 'Coolpix L12'} = 722;
	$Compact2ID{ 'GR Digital III'} = 1297;
	$Compact2ID{ 'Canon PowerShot SD750'} = 570;
	$Compact2ID{ 'DMC-FX100'} = 725;
	$Compact2ID{ 'FinePix A600'} = 413;
	$Compact2ID{ 'EASYSHARE Z812'} = 743;
	$Compact2ID{ 'DSC-H1'} = 187;
	$Compact2ID{ 'QV-R4'} = 145;
	$Compact2ID{ 'Canon PowerShot A95'} = 233;
	$Compact2ID{ 'DSC-P43'} = 671;
	$Compact2ID{ 'Optio 555'} = 282;
	$Compact2ID{ 'EASYSHARE V550'} = 327;
	$Compact2ID{ 'EASYSHARE DX7590'} = 321;
	$Compact2ID{ 'Lumix DMC-TZ3'} = 555;
	$Compact2ID{ 'Coolpix 3500'} = 157;
	$Compact2ID{ 'FinePix F40fd'} = 564;
	$Compact2ID{ 'FinePix A510'} = 424;
	$Compact2ID{ 'DSC-W55'} = 559;
	$Compact2ID{ 'PowerShot SD880 IS'} = 1129;
	$Compact2ID{ 'Exilim EX-Z600'} = 90;
	$Compact2ID{ 'Exilim EX-P505'} = 443;
	$Compact2ID{ 'NV11'} = 574;
	$Compact2ID{ 'CoolPix S52'} = 1234;
	$Compact2ID{ 'PowerShot A540'} = 373;
	$Compact2ID{ 'Optio WP'} = 115;
	$Compact2ID{ 'Panasonic Lumix DMC-LX3'} = 1006;
	$Compact2ID{ 'Lumix DMC-FZ7'} = 149;
	$Compact2ID{ 'FinePix A303'} = 420;
	$Compact2ID{ 'FinePix F30'} = 49;
	$Compact2ID{ 'Coolpix 5200'} = 74;
	$Compact2ID{ 'FinePix S200EXR'} = 1348;
	$Compact2ID{ 'Samsung Pro815'} = 259;
	$Compact2ID{ 'Canon PowerShot A650 IS'} = 807;
	$Compact2ID{ 'EASYSHARE DX6340'} = 337;
	$Compact2ID{ 'FinePix F440'} = 571;
	$Compact2ID{ 'Coolpix L15'} = 799;
	$Compact2ID{ 'S85'} = 858;
	$Compact2ID{ 'Coolpix L11'} = 537;
	$Compact2ID{ 'DiMAGE X1'} = 455;
	$Compact2ID{ 'FinePix S9000'} = 52;
	$Compact2ID{ 'Optio S12'} = 1049;
	$Compact2ID{ 'PowerShot TX1'} = 717;
	$Compact2ID{ 'PowerShot SD 980 IS'} = 1385;
	$Compact2ID{ 'Nikon Coolpix 8700'} = 139;
	$Compact2ID{ 'GR Digital'} = 279;
	$Compact2ID{ 'DSC-S700'} = 959;
	$Compact2ID{ 'Caplio R2'} = 315;
	$Compact2ID{ 'Coolpix L18'} = 904;
	$Compact2ID{ 'Digilux 1'} = 319;
	$Compact2ID{ 'PowerShot S45'} = 213;
	$Compact2ID{ 'Stylus 840'} = 1194;
	$Compact2ID{ 'PowerShot A530'} = 244;
	$Compact2ID{ 'Fujifilm FinePix S5700'} = 573;
	$Compact2ID{ 'EasyShare Z1012 IS'} = 912;
	$Compact2ID{ 'FinePix S100fs'} = 852;
	$Compact2ID{ 'EASYSHARE CX7530'} = 335;
	$Compact2ID{ 'Optio W10'} = 65;
	$Compact2ID{ 'EASYSHARE Z760'} = 329;
	$Compact2ID{ 'EASYSHARE DC4800'} = 334;
	$Compact2ID{ 'QV-R61'} = 146;
	$Compact2ID{ 'Ricoh Caplio GX100'} = 592;
	$Compact2ID{ 'Stylus 1030 SW'} = 1075;
	$Compact2ID{ 'DSC-W1'} = 191;
	$Compact2ID{ 'DSC-W85'} = 731;
	$Compact2ID{ 'Z1275'} = 804;
	$Compact2ID{ 'Sony DSC-H10'} = 946;
	$Compact2ID{ 'Digimax S500'} = 261;
	$Compact2ID{ 'Optio 450'} = 284;
	$Compact2ID{ 'Coolpix 4200'} = 140;
	$Compact2ID{ 'FinePix S1000fd'} = 894;
	$Compact2ID{ 'Caplio R6'} = 538;
	$Compact2ID{ 'Optio S30'} = 123;
	$Compact2ID{ 'Exilim EX-S880'} = 875;
	$Compact2ID{ 'PowerShot SD850 IS'} = 653;
	$Compact2ID{ 'Sony DSC-F828'} = 204;
	$Compact2ID{ 'Panasonic Lumix DMC-TZ4'} = 909;
	$Compact2ID{ 'Lumix DMC-LZ2'} = 313;
	$Compact2ID{ 'Exilim EX-Z65'} = 662;
	$Compact2ID{ 'FinePix S5700'} = 573;
	$Compact2ID{ 'EASYSHARE CX7430'} = 328;
	$Compact2ID{ 'NV10'} = 249;
	$Compact2ID{ 'PowerShot SD870 IS'} = 733;
	$Compact2ID{ 'Exilim EX-Z700'} = 550;
	$Compact2ID{ 'Lumix DMC-LS75'} = 979;
	$Compact2ID{ 'DSC-P10'} = 464;
	$Compact2ID{ 'FinePix S1500'} = 1204;
	$Compact2ID{ 'Lumix DMC-FS15'} = 1294;
	$Compact2ID{ 'Coolpix P5000'} = 542;
	$Compact2ID{ 'Exilim EX-S770'} = 506;
	$Compact2ID{ 'Z1085 IS'} = 1000;
	$Compact2ID{ 'DSC-P52'} = 390;
	$Compact2ID{ 'V-LUX 1'} = 387;
	$Compact2ID{ 'EasyShare V1233'} = 891;
	$Compact2ID{ 'PowerShot SD900'} = 217;
	$Compact2ID{ 'Lumix DMC-LZ5'} = 154;
	$Compact2ID{ 'Caplio G4 Wide'} = 179;
	$Compact2ID{ 'FinePix S3500 Zoom'} = 593;
	$Compact2ID{ 'PowerShot A620'} = 78;
	$Compact2ID{ 'PowerShot SD950 IS'} = 908;
	$Compact2ID{ 'Optio A10'} = 278;
	$Compact2ID{ 'Coolpix 5400'} = 70;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ28'} = 1017;
	$Compact2ID{ 'DSC-W150'} = 951;
	$Compact2ID{ 'Optio S5Z'} = 127;
	$Compact2ID{ 'PowerShot G1'} = 32;
	$Compact2ID{ 'Exilim EX-Z55'} = 378;
	$Compact2ID{ 'EASYSHARE V610'} = 331;
	$Compact2ID{ 'NV24HD'} = 1085;
	$Compact2ID{ 'FinePix F31fd'} = 347;
	$Compact2ID{ 'EASYSHARE Z740'} = 226;
	$Compact2ID{ 'DSC-T2'} = 901;
	$Compact2ID{ 'EasyShare V1253'} = 877;
	$Compact2ID{ 'FinePix Z3'} = 718;
	$Compact2ID{ 'DiMAGE Z6'} = 469;
	$Compact2ID{ 'Coolpix 8700'} = 139;
	$Compact2ID{ 'Ricoh Caplio GX200'} = 998;
	$Compact2ID{ 'EasyShare M853'} = 884;
	$Compact2ID{ 'EASYSHARE V1003'} = 755;
	$Compact2ID{ 'EASYSHARE V603'} = 482;
	$Compact2ID{ 'Canon PowerShot SD1100 IS'} = 929;
	$Compact2ID{ 'Ricoh GR Digital II'} = 778;
	$Compact2ID{ 'Coolpix L6'} = 842;
	$Compact2ID{ 'FinePix A500'} = 669;
	$Compact2ID{ 'DP1'} = 963;
	$Compact2ID{ 'DSC-W200'} = 624;
	$Compact2ID{ 'Canon PowerShot A720 IS'} = 796;
	$Compact2ID{ 'CoolPix 8400'} = 501;
	$Compact2ID{ 'Exilim EX-FH20'} = 1222;
	$Compact2ID{ 'Caplio GX'} = 95;
	$Compact2ID{ 'CoolPix P60'} = 930;
	$Compact2ID{ 'EASYSHARE V705'} = 552;
	$Compact2ID{ 'Optio M30'} = 618;
	$Compact2ID{ 'Exilim EX-H10'} = 1318;
	$Compact2ID{ 'FinePix S2000HD'} = 1123;
	$Compact2ID{ 'Exilim EX-Z77'} = 811;
	$Compact2ID{ 'DMC-FX55'} = 770;
	$Compact2ID{ 'Coolpix L1'} = 66;
	$Compact2ID{ 'FinePix S8000fd'} = 801;
	$Compact2ID{ 'Ricoh Caplio R7'} = 738;
	$Compact2ID{ 'Coolpix P2'} = 67;
	$Compact2ID{ 'FinePix S9100'} = 418;
	$Compact2ID{ 'DSC-H50'} = 902;
	$Compact2ID{ 'Stylus 725 SW'} = 539;
	$Compact2ID{ 'PowerShot SD550'} = 47;
	$Compact2ID{ 'PowerShot SD770 IS'} = 1073;
	$Compact2ID{ 'Lumix DMC-ZS1'} = 1193;
	$Compact2ID{ 'Coolpix 4300'} = 130;
	$Compact2ID{ 'DSC-W35'} = 670;
	$Compact2ID{ 'DSC-T5'} = 18;
	$Compact2ID{ 'PowerShot S500'} = 305;
	$Compact2ID{ 'FinePix V10'} = 554;
	$Compact2ID{ 'Coolpix S5'} = 135;
	$Compact2ID{ 'Kodak EASYSHARE P880'} = 41;
	$Compact2ID{ 'DMC-GH1'} = 1205;
	$Compact2ID{ 'SP-570UZ'} = 899;
	$Compact2ID{ 'Lumix DMC-FX07'} = 93;
	$Compact2ID{ 'Fujifilm FinePix S5800'} = 805;
	$Compact2ID{ 'FinePix F610'} = 449;
	$Compact2ID{ 'EasyShare M873'} = 924;
	$Compact2ID{ 'DSC-S500'} = 473;
	$Compact2ID{ 'SP-500 UZ'} = 636;
	$Compact2ID{ 'Lumix DMC-LC50'} = 312;
	$Compact2ID{ 'Nikon CoolPix L100'} = 1189;
	$Compact2ID{ 'Canon PowerShot A570 IS'} = 562;
	$Compact2ID{ 'Lumix DMC-FZ38'} = 1327;
	$Compact2ID{ 'C360'} = 678;
	$Compact2ID{ 'Lumix DMC-FX3'} = 348;
	$Compact2ID{ 'Caplio G4'} = 180;
	$Compact2ID{ 'CoolPix S510'} = 945;
	$Compact2ID{ 'PowerShot SD30'} = 245;
	$Compact2ID{ 'Exilim EX-S500'} = 379;
	$Compact2ID{ 'Fujifilm FinePix S2000HD'} = 1123;
	$Compact2ID{ 'Lumix DMC-FX30'} = 512;
	$Compact2ID{ 'Optio S6'} = 61;
	$Compact2ID{ 'PowerShot A520'} = 236;
	$Compact2ID{ 'Mju 1040'} = 1198;
	$Compact2ID{ 'EASYSHARE Z730'} = 339;
	$Compact2ID{ 'CyberShot DSC-W290'} = 1252;
	$Compact2ID{ 'Stylus 1010'} = 926;
	$Compact2ID{ 'PowerShot SD630'} = 397;
	$Compact2ID{ 'DSC-H10'} = 946;
	$Compact2ID{ 'Optio 750Z'} = 276;
	$Compact2ID{ 'FinePix S602 Z'} = 99;
	$Compact2ID{ 'DSC-P200'} = 195;
	$Compact2ID{ 'Lumix DMC-LZ3'} = 158;
	$Compact2ID{ 'FE-280'} = 898;
	$Compact2ID{ 'Stylus Verve S'} = 354;
	$Compact2ID{ 'NV15'} = 848;
	$Compact2ID{ 'Lumix DMC-TZ1'} = 151;
	$Compact2ID{ 'QV-4000'} = 619;
	$Compact2ID{ 'PowerShot SD450'} = 29;
	$Compact2ID{ 'R10'} = 1083;
	$Compact2ID{ 'PowerShot A630'} = 81;
	$Compact2ID{ 'Coolpix 7600'} = 289;
	$Compact2ID{ 'PowerShot SD300'} = 235;
	$Compact2ID{ 'DSC-T1'} = 190;
	$Compact2ID{ 'Optio 33WR'} = 125;
	$Compact2ID{ 'DiMAGE X50'} = 406;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ8'} = 545;
	$Compact2ID{ 'DSC-TX1'} = 1323;
	$Compact2ID{ 'PowerShot A400'} = 361;
	$Compact2ID{ 'PowerShot A75'} = 211;
	$Compact2ID{ 'FinePix F470'} = 444;
	$Compact2ID{ 'FinePix S5600'} = 44;
	$Compact2ID{ 'Lumix DMC-FZ18'} = 737;
	$Compact2ID{ 'PowerShot S80'} = 36;
	$Compact2ID{ 'EASYSHARE P880'} = 41;
	$Compact2ID{ 'CyberShot DSC-H20'} = 1282;
	$Compact2ID{ 'Lumix DMC-FX33'} = 897;
	$Compact2ID{ 'Canon PowerShot S3 IS'} = 39;
	$Compact2ID{ 'PowerShot S2 IS'} = 228;
	$Compact2ID{ 'Coolpix S600'} = 997;
	$Compact2ID{ 'SD 980 IS'} = 1203;
	$Compact2ID{ 'Nikon Coolpix 3100'} = 288;
	$Compact2ID{ 'FinePix A370'} = 422;
	$Compact2ID{ 'Fujifilm FinePix F50 fd'} = 776;
	$Compact2ID{ 'DiMAGE 7'} = 210;
	$Compact2ID{ 'FinePix S9000 Z'} = 810;
	$Compact2ID{ 'Konica Minolta DiMAGE Z1'} = 458;
	$Compact2ID{ 'DSC-P8'} = 301;
	$Compact2ID{ 'Lumix DMC-LC33'} = 178;
	$Compact2ID{ 'Fujifilm FinePix F40fd'} = 564;
	$Compact2ID{ 'Stylus 770 SW'} = 627;
	$Compact2ID{ 'Exilim EX-Z500'} = 142;
	$Compact2ID{ 'Digimax A7'} = 685;
	$Compact2ID{ 'DSC-S90'} = 341;
	$Compact2ID{ 'Coolpix P1'} = 129;
	$Compact2ID{ 'Stylus 850 SW'} = 890;
	$Compact2ID{ 'FinePix Z10 fd'} = 764;
	$Compact2ID{ 'FE-140'} = 577;
	$Compact2ID{ 'DSC-W120'} = 975;
	$Compact2ID{ 'Coolpix L5'} = 478;
	$Compact2ID{ 'FinePix F50 fd'} = 776;
	$Compact2ID{ 'Fujifilm FinePix S1000fd'} = 894;
	$Compact2ID{ 'Optio 60'} = 64;
	$Compact2ID{ 'Optio S7'} = 281;
	$Compact2ID{ 'EasyShare C813'} = 942;
	$Compact2ID{ 'Coolpix 4800'} = 136;
	$Compact2ID{ 'Olympus E-520'} = 1071;
	$Compact2ID{ 'FinePix F455'} = 673;
	$Compact2ID{ 'Optio E40'} = 1003;
	$Compact2ID{ 'DSC-H3'} = 806;
	$Compact2ID{ 'FinePix A350'} = 684;
	$Compact2ID{ 'CoolPix S220'} = 1190;
	$Compact2ID{ 'Exilim EX-S600'} = 89;
	$Compact2ID{ 'Optio A20'} = 60;
	$Compact2ID{ 'Coolpix 3200'} = 297;
	$Compact2ID{ 'DSC-P100'} = 186;
	$Compact2ID{ 'Digimax A5'} = 264;
	$Compact2ID{ 'Stylus 600'} = 76;
	$Compact2ID{ 'Coolpix 4100'} = 303;
	$Compact2ID{ 'Fujifilm FinePix S9500'} = 45;
	$Compact2ID{ 'Fujifilm FinePix F30'} = 49;
	$Compact2ID{ 'Lumix DMC-TZ15'} = 984;
	$Compact2ID{ 'TL 320'} = 1214;
	$Compact2ID{ 'Fujifilm FinePix S6000fd'} = 412;
	$Compact2ID{ 'Coolpix S6'} = 131;
	$Compact2ID{ 'DSC-N1'} = 396;
	$Compact2ID{ 'Coolpix P80'} = 978;
	$Compact2ID{ 'Exilim EX-Z70'} = 453;
	$Compact2ID{ 'Fujifilm FinePix S200EXR'} = 1348;
	$Compact2ID{ 'Optio E10'} = 517;
	$Compact2ID{ 'FinePix S5000Z'} = 419;
	$Compact2ID{ 'DSC-W7'} = 182;
	$Compact2ID{ 'PowerShot S40'} = 353;
	$Compact2ID{ 'Coolpix P6000'} = 1192;
	$Compact2ID{ 'Optio S4I'} = 274;
	$Compact2ID{ 'Konica Minolta DiMAGE Z10'} = 471;
	$Compact2ID{ 'FinePix A345'} = 445;
	$Compact2ID{ 'EASYSHARE P850'} = 255;
	$Compact2ID{ 'Optio S4'} = 124;
	$Compact2ID{ 'DSC-W70'} = 197;
	$Compact2ID{ 'Lumix DMC-LS60'} = 813;
	$Compact2ID{ 'Optio S50'} = 122;
	$Compact2ID{ 'PowerShot A720 IS'} = 796;
	$Compact2ID{ 'DiMAGE Z10'} = 471;
	$Compact2ID{ 'Leica X1'} = 1434;
	$Compact2ID{ 'EASYSHARE C643'} = 251;
	$Compact2ID{ 'FinePix E900'} = 50;
	$Compact2ID{ 'FinePix F650'} = 415;
	$Compact2ID{ 'DSC-P92'} = 371;
	$Compact2ID{ 'PowerShot G11'} = 1412;
	$Compact2ID{ 'DSC-W100'} = 389;
	$Compact2ID{ 'Z885'} = 862;
	$Compact2ID{ 'EASYSHARE Z712'} = 742;
	$Compact2ID{ 'Stylus 760'} = 676;
	$Compact2ID{ 'Stylus 410'} = 248;
	$Compact2ID{ 'Leica D-LUX 3'} = 262;
	$Compact2ID{ 'DSC-WX1'} = 1395;
	$Compact2ID{ 'Exilim EX-Z110'} = 87;
	$Compact2ID{ 'Coolpix L10'} = 591;
	$Compact2ID{ 'Optio E30'} = 840;
	$Compact2ID{ 'DSC-W17'} = 340;
	$Compact2ID{ 'DSC-S600'} = 42;
	$Compact2ID{ 'FinePix A900'} = 809;
	$Compact2ID{ 'EasyShare V803'} = 701;
	$Compact2ID{ 'EasyShare Z1015 IS'} = 1131;
	$Compact2ID{ 'DSC-N2'} = 466;
	$Compact2ID{ 'FE-230'} = 849;
	$Compact2ID{ 'PowerShot S230'} = 199;
	$Compact2ID{ 'FinePix F100fd'} = 895;
	$Compact2ID{ 'PowerShot SD1100 IS'} = 929;
	$Compact2ID{ 'Nikon Coolpix P90'} = 1191;
	$Compact2ID{ 'PowerShot SD200'} = 241;
	$Compact2ID{ 'EASYSHARE C330'} = 485;
	$Compact2ID{ 'Coolpix S50C'} = 874;
	$Compact2ID{ 'PowerShot G3'} = 346;
	$Compact2ID{ 'EASYSHARE V530'} = 510;
	$Compact2ID{ 'D-LUX 4'} = 1057;
	$Compact2ID{ 'PowerShot A510'} = 692;
	$Compact2ID{ 'DiMAGE 7i'} = 21;
	$Compact2ID{ 'Lumix DMC-FS3'} = 961;
	$Compact2ID{ 'Caplio RR30'} = 706;
	$Compact2ID{ 'GR Digital II'} = 778;
	$Compact2ID{ 'Lumix DMC-LC1'} = 156;
	$Compact2ID{ 'DSC-W50'} = 188;
	$Compact2ID{ 'EASYSHARE Z7590'} = 323;
	$Compact2ID{ 'DSC-H7'} = 623;
	$Compact2ID{ 'NV3'} = 254;
	$Compact2ID{ 'Lumix DMC-FZ50'} = 155;
	$Compact2ID{ 'Optio 30'} = 126;
	$Compact2ID{ 'Exilim EX-P600'} = 566;
	$Compact2ID{ 'Lumix DMC-ZS3'} = 1305;
	$Compact2ID{ 'EasyShare Z8612 IS'} = 1242;
	$Compact2ID{ 'Lumix DSC-FX50'} = 493;
	$Compact2ID{ 'Exilim EX-Z850'} = 86;
	$Compact2ID{ 'QV-R51'} = 439;
	$Compact2ID{ 'DSC-P12'} = 610;
	$Compact2ID{ 'PowerShot A85'} = 247;
	$Compact2ID{ 'EasyShare C315'} = 704;
	$Compact2ID{ 'Pro815'} = 259;
	$Compact2ID{ 'Coolpix 5700'} = 138;
	$Compact2ID{ 'FinePix A920'} = 955;
	$Compact2ID{ 'Exilim EX-Z750'} = 84;
	$Compact2ID{ 'Leica V-LUX 1'} = 387;
	$Compact2ID{ 'PowerShot SX200 IS'} = 1212;
	$Compact2ID{ 'Exilim EX-S100'} = 440;
	$Compact2ID{ 'Casio Exilim EX-Z1080'} = 771;
	$Compact2ID{ 'FE-120'} = 519;
	$Compact2ID{ 'Canon PowerShot A540'} = 373;
	$Compact2ID{ 'Canon PowerShot G10'} = 1086;
	$Compact2ID{ 'Optio WPi'} = 120;
	$Compact2ID{ 'Canon PowerShot SX200 IS'} = 1212;
	$Compact2ID{ 'FinePix F710'} = 434;
	$Compact2ID{ 'Lumix DMC-LZ8'} = 1034;
	$Compact2ID{ 'DSC-S750'} = 988;
	$Compact2ID{ 'Caplio GX8'} = 97;
	$Compact2ID{ 'S630'} = 815;
	$Compact2ID{ 'DSC-H9'} = 691;
	$Compact2ID{ 'Caplio R7'} = 738;
	$Compact2ID{ 'DSC-S60'} = 208;
	$Compact2ID{ 'Lumix DMC-LS80'} = 1101;
	$Compact2ID{ 'Canon PowerShot G7'} = 56;
	$Compact2ID{ 'DSC-T20'} = 558;
	$Compact2ID{ 'Panasonic Lumix DMC-TZ5'} = 889;
	$Compact2ID{ 'DSC-V3'} = 203;
	$Compact2ID{ 'Exilim EX-Z40'} = 143;
	$Compact2ID{ 'FinePix F810'} = 477;
	$Compact2ID{ 'FinePix Z20'} = 976;
	$Compact2ID{ 'Coolpix L2'} = 291;
	$Compact2ID{ 'Lumix DMC-LX1'} = 159;
	$Compact2ID{ 'Coolpix P50'} = 779;
	$Compact2ID{ 'DSC-W90'} = 608;
	$Compact2ID{ 'EASYSHARE M753'} = 741;
	$Compact2ID{ 'FinePix F200EXR'} = 1184;
	$Compact2ID{ 'Fujifilm FinePix S8000fd'} = 801;
	$Compact2ID{ 'EX-Z11'} = 893;
	$Compact2ID{ 'Exilim EX-Z100'} = 896;
	$Compact2ID{ 'PowerShot SD800 IS'} = 58;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ38'} = 1327;
	$Compact2ID{ 'Lumix DMC-FZ3'} = 320;
	$Compact2ID{ 'PowerShot G2'} = 100;
	$Compact2ID{ 'Fujifilm FinePix F20'} = 376;
	$Compact2ID{ 'DSC-P120'} = 532;
	$Compact2ID{ 'Lumix DMC-FZ30'} = 92;
	$Compact2ID{ 'Exilim EX-Z57'} = 694;
	$Compact2ID{ 'Canon PowerShot S90'} = 1362;
	$Compact2ID{ 'PowerShot S70'} = 243;
	$Compact2ID{ 'EASYSHARE C875'} = 487;
	$Compact2ID{ 'Optio 50'} = 119;
	$Compact2ID{ 'EASYSHARE CX7300'} = 330;
	$Compact2ID{ 'PowerShot SD1000'} = 534;
	$Compact2ID{ 'CoolPix S630'} = 1233;
	$Compact2ID{ 'DSC-S650'} = 587;
	$Compact2ID{ 'FinePix S3100'} = 447;
	$Compact2ID{ 'NV7'} = 355;
	$Compact2ID{ 'Optio P70'} = 1293;
	$Compact2ID{ 'Lumix DMC-FX9'} = 91;
	$Compact2ID{ 'Digimax V800'} = 263;
	$Compact2ID{ 'CoolPix L100'} = 1189;
	$Compact2ID{ 'DSC-T300'} = 881;
	$Compact2ID{ 'Exilim EX-V7'} = 699;
	$Compact2ID{ 'C-LUX 3'} = 1150;
	$Compact2ID{ 'Lumix DMC-FX520'} = 989;
	$Compact2ID{ 'FinePix A330'} = 107;
	$Compact2ID{ 'FinePix F10'} = 375;
	$Compact2ID{ 'PowerShot A70'} = 232;
	$Compact2ID{ 'Stylus 720 SW'} = 350;
	$Compact2ID{ 'DSC-W80'} = 560;
	$Compact2ID{ 'Lumix DMC-FX500'} = 968;
	$Compact2ID{ 'DSC-T100'} = 599;
	$Compact2ID{ 'DSC-T10'} = 185;
	$Compact2ID{ 'Panasonic DMC-GH1'} = 1205;
	$Compact2ID{ 'Exilim EX-Z10'} = 441;
	$Compact2ID{ 'Coolpix S10'} = 613;
	$Compact2ID{ 'PowerShot A710 IS'} = 83;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ30'} = 92;
	$Compact2ID{ 'DiMAGE G500'} = 411;
	$Compact2ID{ 'DSC-P73'} = 202;
	$Compact2ID{ 'PowerShot Pro1'} = 242;
	$Compact2ID{ 'Coolpix P5100'} = 736;
	$Compact2ID{ 'Lumix DMC-LX2'} = 54;
	$Compact2ID{ 'Exilim EX-Z75'} = 769;
	$Compact2ID{ 'FinePix Z2'} = 53;
	$Compact2ID{ 'Caplio R1'} = 393;
	$Compact2ID{ 'Sony DSC-H50'} = 902;
	$Compact2ID{ 'EASYSHARE Z650'} = 257;
	$Compact2ID{ 'Digital IXUS 960 IS'} = 861;
	$Compact2ID{ 'DSC-W130'} = 1117;
	$Compact2ID{ 'FE-340'} = 1102;
	$Compact2ID{ 'EASYSHARE C433'} = 525;
	$Compact2ID{ 'Digimax 301'} = 265;
	$Compact2ID{ 'Lumix DMC-LS1'} = 314;
	$Compact2ID{ 'DiMAGE Z1'} = 458;
	$Compact2ID{ 'Coolpix P90'} = 1191;
	$Compact2ID{ 'Sony DSC-H9'} = 691;
	$Compact2ID{ 'Sony DSC-R1'} = 189;
	$Compact2ID{ 'Lumix DMC-FZ35'} = 1310;
	$Compact2ID{ 'Coolpix S3'} = 71;
	$Compact2ID{ 'PowerShot A590 IS'} = 948;
	$Compact2ID{ 'Coolpix S7c'} = 68;
	$Compact2ID{ 'Coolpix 5900'} = 438;
	$Compact2ID{ 'Coolpix L3'} = 293;
	$Compact2ID{ 'Leica D-LUX 4'} = 1057;
	$Compact2ID{ 'FinePix A400'} = 479;
	$Compact2ID{ 'Nikon Coolpix P5100'} = 736;
	$Compact2ID{ 'DSC-F828'} = 204;
	$Compact2ID{ 'Stylus 1000'} = 730;
	$Compact2ID{ 'DMC-LS2'} = 786;
	$Compact2ID{ 'EASYSHARE LS753'} = 326;
	$Compact2ID{ 'Lumix DMC-TZ4'} = 909;
	$Compact2ID{ 'QV-R62'} = 148;
	$Compact2ID{ 'Coolpix 3100'} = 288;
	$Compact2ID{ 'FinePix F11'} = 435;
	$Compact2ID{ 'DSC-T200'} = 762;
	$Compact2ID{ 'FE-210'} = 977;
	$Compact2ID{ 'FinePix A820'} = 620;
	$Compact2ID{ 'PowerShot A700'} = 215;
	$Compact2ID{ 'EASYSHARE DX4530'} = 336;
	$Compact2ID{ 'PowerShot SD20'} = 407;
	$Compact2ID{ 'DSC-H2'} = 206;
	$Compact2ID{ 'Coolpix S51'} = 802;
	$Compact2ID{ 'Lumix DMC-LZ6'} = 626;
	$Compact2ID{ 'EasyShare ZD710'} = 863;
	$Compact2ID{ 'EASYSHARE C300'} = 628;
	$Compact2ID{ 'DSC-T50'} = 457;
	$Compact2ID{ 'FinePix F700'} = 450;
	$Compact2ID{ 'PowerShot G7'} = 56;
	$Compact2ID{ 'Optio 330'} = 280;
	$Compact2ID{ 'Optio W30'} = 611;
	$Compact2ID{ 'PowerShot G5'} = 164;
	$Compact2ID{ 'DSC-W300'} = 958;
	$Compact2ID{ 'EASYSHARE C310'} = 325;
	$Compact2ID{ 'PowerShot SD750'} = 570;
	$Compact2ID{ 'SP-500'} = 569;
	$Compact2ID{ 'Optio 330GS'} = 377;
	$Compact2ID{ 'FinePix F20'} = 376;
	$Compact2ID{ 'Optio S5I'} = 117;
	$Compact2ID{ 'DMC-TZ7'} = 1235;
	$Compact2ID{ 'Optio MX'} = 116;
	$Compact2ID{ 'DSC-P93'} = 196;
	$Compact2ID{ 'EASYSHARE Z700'} = 322;
	$Compact2ID{ 'DiMAGE Z2'} = 401;
	$Compact2ID{ 'D-LUX 3'} = 262;
	$Compact2ID{ 'Coolpix P4'} = 132;
	$Compact2ID{ 'Lumix DMC-FX10'} = 686;
	$Compact2ID{ 'Canon PowerShot SD800 IS'} = 58;
	$Compact2ID{ 'Sony DSC-H7'} = 623;
	$Compact2ID{ 'PowerShot S3 IS'} = 39;
	$Compact2ID{ 'Exilim EX-Z60'} = 88;
	$Compact2ID{ 'Lumix DMC-FZ4'} = 495;
	$Compact2ID{ 'Fujifilm FinePix S9100'} = 418;
	$Compact2ID{ 'Exilim EX-V8'} = 800;
	$Compact2ID{ 'Panasonic Lumix DMC-FZ50'} = 155;
	$Compact2ID{ 'Optio S55'} = 437;
	$Compact2ID{ 'Exilim EX-Z1080'} = 771;
	$Compact2ID{ 'FinePix S7000 Z'} = 51;
	$Compact2ID{ 'EasyShare C613'} = 702;
	$Compact2ID{ 'FinePix S8100fd'} = 1149;
	$Compact2ID{ 'Lumix DMC-FZ5'} = 160;
	$Compact2ID{ 'NV8'} = 847;
	$Compact2ID{ 'Canon Digital IXUS 960 IS'} = 861;
	$Compact2ID{ 'DSC-P150'} = 356;
	$Compact2ID{ 'PowerShot SX100 IS'} = 857;
	$Compact2ID{ 'EasyShare M763'} = 1089;
	$Compact2ID{ 'PowerShot SD500'} = 216;
	$Compact2ID{ 'Optio S'} = 121;
	$Compact2ID{ 'Digimax L50'} = 256;
	$Compact2ID{ 'Canon PowerShot SX10 IS'} = 1239;
	$Compact2ID{ 'DiMAGE 7Hi'} = 209;
	$Compact2ID{ 'Lumix DMC-FX150'} = 1005;
	$Compact2ID{ 'Exilim EX-Z50'} = 452;
	$Compact2ID{ 'CoolPix S50'} = 1152;
	$Compact2ID{ 'Lumix DMC-FX37'} = 1094;
	$Compact2ID{ 'DSC-T3'} = 205;
	$Compact2ID{ 'Optio S5N'} = 277;
	$Compact2ID{ 'PowerShot G10'} = 1086;
	$Compact2ID{ 'Lumix DMC-FZ20'} = 150;
	$Compact2ID{ 'FinePix F601Z'} = 416;
	$Compact2ID{ 'Panasonic Lumix DMC-LX2'} = 54;
	$Compact2ID{ 'Coolpix S4'} = 69;
	$Compact2ID{ 'Coolpix 5600'} = 134;
	$Compact2ID{ 'Coolpix S9'} = 612;
	$Compact2ID{ 'PowerShot A560'} = 683;
	$Compact2ID{ 'FinePix S5500'} = 271;
	$Compact2ID{ 'EASYSHARE C340'} = 300;
	$Compact2ID{ 'PowerShot A570 IS'} = 562;
	$Compact2ID{ 'PowerShot S90'} = 1362;
	$Compact2ID{ 'EasyShare Z915'} = 1334;
	$Compact2ID{ 'DSC-P72'} = 192;
	$Compact2ID{ 'FinePix E550'} = 448;
	$Compact2ID{ 'DiMAGE Z3'} = 409;
	$Compact2ID{ 'Lumix DMC-LX3'} = 1006;
	$Compact2ID{ 'Coolpix 995'} = 302;
	$Compact2ID{ 'DSC-W30'} = 207;
	$Compact2ID{ 'Exilim EX-Z1000'} = 85;
	$Compact2ID{ 'Digimax A502'} = 304;
	$Compact2ID{ 'DSC-T33'} = 200;
	$Compact2ID{ 'Lumix DMC-TZ5'} = 889;
	$Compact2ID{ 'EASYSHARE C743'} = 332;
	$Compact2ID{ 'Exilim EX-Z80'} = 1044;
	$Compact2ID{ 'Digimax I6'} = 383;
	$Compact2ID{ 'FinePix S304'} = 272;
	$Compact2ID{ 'EASYSHARE Z710'} = 380;
	$Compact2ID{ 'C-LUX 1'} = 318;
	$Compact2ID{ 'Coolpix 4500'} = 287;
	$Compact2ID{ 'DSC-P41'} = 181;
	$Compact2ID{ 'D-595 Zoom'} = 392;
	$Compact2ID{ 'Lumix DMC-FX7'} = 153;
	$Compact2ID{ 'Canon PowerShot G11'} = 1412;
	$Compact2ID{ 'EASYSHARE DX3900'} = 382;
	$Compact2ID{ 'PowerShot A610'} = 79;
	$Compact2ID{ 'Coolpix S200'} = 693;
	$Compact2ID{ 'D-LUX 2'} = 316;
	$Compact2ID{ 'E-520'} = 1071;
	$Compact2ID{ 'EASYSHARE CX6330'} = 399;
	$Compact2ID{ 'PowerShot SD600'} = 214;
	$Compact2ID{ 'PowerShot A640'} = 80;
	$Compact2ID{ 'PowerShot S30'} = 359;
	$Compact2ID{ 'DSC-T30'} = 690;
	$Compact2ID{ 'Coolpix P3'} = 526;
	$Compact2ID{ 'PowerShot A95'} = 233;
	$Compact2ID{ 'Lumix DMC-L10'} = 964;
	$Compact2ID{ 'DSC-T9'} = 194;
	$Compact2ID{ 'Lumix DMC-FX01'} = 94;
	$Compact2ID{ 'PowerShot SX10 IS'} = 1239;
	$Compact2ID{ 'Coolpix S610'} = 1111;
	$Compact2ID{ 'Cyber-shot DSC-TX9'} = 1649;
	$Compact2ID{ 'Stylus 710'} = 677;
	$Compact2ID{ 'Optio 33LF'} = 398;
	$Compact2ID{ 'Lumix DMC-FX12'} = 674;
	$Compact2ID{ 'DSC-W5'} = 184;
	$Compact2ID{ 'DSC-R1'} = 189;
	$Compact2ID{ 'Optio 50L'} = 275;
	$Compact2ID{ 'EASYSHARE C653'} = 655;
	$Compact2ID{ 'PowerShot SD100'} = 366;
	$Compact2ID{ 'EeasyShare M1033'} = 911;
	$Compact2ID{ 'Optio M40'} = 1016;
	$Compact2ID{ 'EasyShare V1073'} = 1296;
	$Compact2ID{ 'Lumix DMC-FX8'} = 152;
	$Compact2ID{ 'Exilim EX-P700'} = 442;
	$Compact2ID{ 'Stylus 790 SW'} = 740;
	$Compact2ID{ 'PowerShot SD700 IS'} = 212;
	$Compact2ID{ 'Exilim EX-Z3'} = 311;
	$Compact2ID{ 'DSC-W40'} = 372;
	$Compact2ID{ 'Lumix DMC-FZ28'} = 1017;
	$Compact2ID{ 'DSC-S40'} = 201;
	$Compact2ID{ 'Caplio R4'} = 98;
	$Compact2ID{ 'Digilux 2'} = 260;
	$Compact2ID{ 'Fujifilm FinePix S5600'} = 44;
	$Compact2ID{ 'DSC-P32'} = 404;
	$Compact2ID{ 'Konica Minolta DiMAGE 7Hi'} = 209;
	$Compact2ID{ 'Stylus 800'} = 77;
	
	$ID2Compact{ 559 } = "DSC-W55";
	$ID2Compact{ 127 } = "Optio S5Z";
	$ID2Compact{ 1049 } = "Optio S12";
	$ID2Compact{ 32 } = "PowerShot G1";
	$ID2Compact{ 206 } = "DSC-H2";
	$ID2Compact{ 118 } = "Optio SV";
	$ID2Compact{ 71 } = "Coolpix S3";
	$ID2Compact{ 443 } = "Exilim EX-P505";
	$ID2Compact{ 959 } = "DSC-S700";
	$ID2Compact{ 331 } = "EASYSHARE V610";
	$ID2Compact{ 560 } = "DSC-W80";
	$ID2Compact{ 737 } = "Lumix DMC-FZ18";
	$ID2Compact{ 898 } = "FE-280";
	$ID2Compact{ 805 } = "Fujifilm FinePix S5800";
	$ID2Compact{ 84 } = "Exilim EX-Z750";
	$ID2Compact{ 512 } = "Lumix DMC-FX30";
	$ID2Compact{ 437 } = "Optio S55";
	$ID2Compact{ 194 } = "DSC-T9";
	$ID2Compact{ 517 } = "Optio E10";
	$ID2Compact{ 458 } = "Konica Minolta DiMAGE Z1";
	$ID2Compact{ 451 } = "FinePix S3000";
	$ID2Compact{ 722 } = "Coolpix L12";
	$ID2Compact{ 1257 } = "EasyShare Z980";
	$ID2Compact{ 846 } = "Exilim EX-Z1200";
	$ID2Compact{ 1296 } = "EasyShare V1073";
	$ID2Compact{ 978 } = "Coolpix P80";
	$ID2Compact{ 742 } = "EASYSHARE Z712";
	$ID2Compact{ 948 } = "Canon PowerShot A590 IS";
	$ID2Compact{ 325 } = "EASYSHARE C310";
	$ID2Compact{ 378 } = "Exilim EX-Z55";
	$ID2Compact{ 29 } = "PowerShot SD450";
	$ID2Compact{ 889 } = "Panasonic Lumix DMC-TZ5";
	$ID2Compact{ 350 } = "Stylus 720 SW";
	$ID2Compact{ 968 } = "Lumix DMC-FX500";
	$ID2Compact{ 675 } = "Lumix DMC-TZ2";
	$ID2Compact{ 1233 } = "CoolPix S630";
	$ID2Compact{ 226 } = "EASYSHARE Z740";
	$ID2Compact{ 58 } = "Canon PowerShot SD800 IS";
	$ID2Compact{ 153 } = "Lumix DMC-FX7";
	$ID2Compact{ 684 } = "FinePix A350";
	$ID2Compact{ 211 } = "PowerShot A75";
	$ID2Compact{ 861 } = "Canon Digital IXUS 960 IS";
	$ID2Compact{ 337 } = "EASYSHARE DX6340";
	$ID2Compact{ 382 } = "EASYSHARE DX3900";
	$ID2Compact{ 340 } = "DSC-W17";
	$ID2Compact{ 76 } = "Stylus 600";
	$ID2Compact{ 62 } = "Optio S60";
	$ID2Compact{ 311 } = "Exilim EX-Z3";
	$ID2Compact{ 571 } = "FinePix F440";
	$ID2Compact{ 1214 } = "TL 320";
	$ID2Compact{ 1190 } = "CoolPix S220";
	$ID2Compact{ 139 } = "Nikon Coolpix 8700";
	$ID2Compact{ 389 } = "DSC-W100";
	$ID2Compact{ 129 } = "Coolpix P1";
	$ID2Compact{ 495 } = "Lumix DMC-FZ4";
	$ID2Compact{ 926 } = "Stylus 1010";
	$ID2Compact{ 1083 } = "R10";
	$ID2Compact{ 1293 } = "Optio P70";
	$ID2Compact{ 966 } = "Optio W60";
	$ID2Compact{ 1123 } = "Fujifilm FinePix S2000HD";
	$ID2Compact{ 662 } = "Exilim EX-Z65";
	$ID2Compact{ 418 } = "Fujifilm FinePix S9100";
	$ID2Compact{ 236 } = "PowerShot A520";
	$ID2Compact{ 706 } = "Caplio RR30";
	$ID2Compact{ 135 } = "Coolpix S5";
	$ID2Compact{ 348 } = "Lumix DMC-FX3";
	$ID2Compact{ 1152 } = "CoolPix S50";
	$ID2Compact{ 145 } = "QV-R4";
	$ID2Compact{ 49 } = "Fujifilm FinePix F30";
	$ID2Compact{ 178 } = "Lumix DMC-LC33";
	$ID2Compact{ 1131 } = "EasyShare Z1015 IS";
	$ID2Compact{ 124 } = "Optio S4";
	$ID2Compact{ 627 } = "Stylus 770 SW";
	$ID2Compact{ 594 } = "EASYSHARE C533";
	$ID2Compact{ 1203 } = "SD 980 IS";
	$ID2Compact{ 96 } = "Caplio R5";
	$ID2Compact{ 653 } = "PowerShot SD850 IS";
	$ID2Compact{ 160 } = "Lumix DMC-FZ5";
	$ID2Compact{ 569 } = "SP-500";
	$ID2Compact{ 976 } = "FinePix Z20";
	$ID2Compact{ 98 } = "Caplio R4";
	$ID2Compact{ 716 } = "PowerShot A550";
	$ID2Compact{ 1000 } = "Z1085 IS";
	$ID2Compact{ 686 } = "Lumix DMC-FX10";
	$ID2Compact{ 485 } = "EASYSHARE C330";
	$ID2Compact{ 685 } = "Digimax A7";
	$ID2Compact{ 778 } = "Ricoh GR Digital II";
	$ID2Compact{ 743 } = "EASYSHARE Z812";
	$ID2Compact{ 21 } = "DiMAGE 7i";
	$ID2Compact{ 193 } = "Sony DSC-H5";
	$ID2Compact{ 288 } = "Nikon Coolpix 3100";
	$ID2Compact{ 119 } = "Optio 50";
	$ID2Compact{ 180 } = "Caplio G4";
	$ID2Compact{ 324 } = "EASYSHARE DX7630";
	$ID2Compact{ 453 } = "Exilim EX-Z70";
	$ID2Compact{ 244 } = "PowerShot A530";
	$ID2Compact{ 690 } = "DSC-T30";
	$ID2Compact{ 999 } = "Z1285";
	$ID2Compact{ 61 } = "Optio S6";
	$ID2Compact{ 1185 } = "FinePix F60FD";
	$ID2Compact{ 447 } = "FinePix S3100";
	$ID2Compact{ 379 } = "Exilim EX-S500";
	$ID2Compact{ 415 } = "FinePix F650";
	$ID2Compact{ 901 } = "DSC-T2";
	$ID2Compact{ 152 } = "Lumix DMC-FX8";
	$ID2Compact{ 189 } = "Sony DSC-R1";
	$ID2Compact{ 452 } = "Exilim EX-Z50";
	$ID2Compact{ 862 } = "Z885";
	$ID2Compact{ 341 } = "DSC-S90";
	$ID2Compact{ 438 } = "Coolpix 5900";
	$ID2Compact{ 107 } = "FinePix A330";
	$ID2Compact{ 87 } = "Exilim EX-Z110";
	$ID2Compact{ 77 } = "Stylus 800";
	$ID2Compact{ 683 } = "PowerShot A560";
	$ID2Compact{ 444 } = "FinePix F470";
	$ID2Compact{ 39 } = "Canon PowerShot S3 IS";
	$ID2Compact{ 64 } = "Optio 60";
	$ID2Compact{ 558 } = "DSC-T20";
	$ID2Compact{ 1089 } = "EasyShare M763";
	$ID2Compact{ 417 } = "FinePix 6900 Zoom";
	$ID2Compact{ 881 } = "DSC-T300";
	$ID2Compact{ 765 } = "S730";
	$ID2Compact{ 312 } = "Lumix DMC-LC50";
	$ID2Compact{ 897 } = "Lumix DMC-FX33";
	$ID2Compact{ 45 } = "Fujifilm FinePix S9500";
	$ID2Compact{ 260 } = "Leica Digilux 2";
	$ID2Compact{ 573 } = "Fujifilm FinePix S5700";
	$ID2Compact{ 1002 } = "FinePix Z100fd";
	$ID2Compact{ 237 } = "PowerShot A80";
	$ID2Compact{ 762 } = "DSC-T200";
	$ID2Compact{ 875 } = "Exilim EX-S880";
	$ID2Compact{ 116 } = "Optio MX";
	$ID2Compact{ 136 } = "Coolpix 4800";
	$ID2Compact{ 506 } = "Exilim EX-S770";
	$ID2Compact{ 416 } = "FinePix F601Z";
	$ID2Compact{ 380 } = "EASYSHARE Z710";
	$ID2Compact{ 100 } = "PowerShot G2";
	$ID2Compact{ 300 } = "EASYSHARE C340";
	$ID2Compact{ 1395 } = "DSC-WX1";
	$ID2Compact{ 120 } = "Optio WPi";
	$ID2Compact{ 286 } = "Coolpix S1";
	$ID2Compact{ 1075 } = "Stylus 1030 SW";
	$ID2Compact{ 381 } = "EASYSHARE P712";
	$ID2Compact{ 800 } = "Exilim EX-V8";
	$ID2Compact{ 254 } = "NV3";
	$ID2Compact{ 392 } = "D-595 Zoom";
	$ID2Compact{ 177 } = "Coolpix 885";
	$ID2Compact{ 678 } = "C360";
	$ID2Compact{ 373 } = "Canon PowerShot A540";
	$ID2Compact{ 813 } = "Lumix DMC-LS60";
	$ID2Compact{ 205 } = "DSC-T3";
	$ID2Compact{ 42 } = "DSC-S600";
	$ID2Compact{ 851 } = "PowerShot S5 IS";
	$ID2Compact{ 399 } = "EASYSHARE CX6330";
	$ID2Compact{ 1073 } = "PowerShot SD770 IS";
	$ID2Compact{ 235 } = "PowerShot SD300";
	$ID2Compact{ 301 } = "DSC-P8";
	$ID2Compact{ 694 } = "Exilim EX-Z57";
	$ID2Compact{ 94 } = "Lumix DMC-FX01";
	$ID2Compact{ 213 } = "PowerShot S45";
	$ID2Compact{ 51 } = "FinePix S7000 Z";
	$ID2Compact{ 1062 } = "DSC-W170";
	$ID2Compact{ 265 } = "Digimax 301";
	$ID2Compact{ 874 } = "Coolpix S50C";
	$ID2Compact{ 975 } = "DSC-W120";
	$ID2Compact{ 1129 } = "PowerShot SD880 IS";
	$ID2Compact{ 493 } = "Lumix DSC-FX50";
	$ID2Compact{ 796 } = "Canon PowerShot A720 IS";
	$ID2Compact{ 445 } = "FinePix A345";
	$ID2Compact{ 200 } = "DSC-T33";
	$ID2Compact{ 366 } = "PowerShot SD100";
	$ID2Compact{ 329 } = "EASYSHARE Z760";
	$ID2Compact{ 525 } = "EASYSHARE C433";
	$ID2Compact{ 27 } = "PowerShot G6";
	$ID2Compact{ 272 } = "FinePix S304";
	$ID2Compact{ 919 } = "Optio A40";
	$ID2Compact{ 534 } = "PowerShot SD1000";
	$ID2Compact{ 1310 } = "Lumix DMC-FZ35";
	$ID2Compact{ 151 } = "Lumix DMC-TZ1";
	$ID2Compact{ 718 } = "FinePix Z3";
	$ID2Compact{ 1239 } = "Canon PowerShot SX10 IS";
	$ID2Compact{ 287 } = "Coolpix 4500";
	$ID2Compact{ 78 } = "PowerShot A620";
	$ID2Compact{ 441 } = "Exilim EX-Z10";
	$ID2Compact{ 413 } = "FinePix A600";
	$ID2Compact{ 349 } = "Lumix DMC-LS3";
	$ID2Compact{ 672 } = "FinePix E510";
	$ID2Compact{ 764 } = "FinePix Z10 fd";
	$ID2Compact{ 769 } = "Exilim EX-Z75";
	$ID2Compact{ 275 } = "Optio 50L";
	$ID2Compact{ 197 } = "DSC-W70";
	$ID2Compact{ 1242 } = "EasyShare Z8612 IS";
	$ID2Compact{ 138 } = "Coolpix 5700";
	$ID2Compact{ 751 } = "Canon PowerShot G9";
	$ID2Compact{ 60 } = "Optio A20";
	$ID2Compact{ 691 } = "Sony DSC-H9";
	$ID2Compact{ 519 } = "FE-120";
	$ID2Compact{ 1074 } = "FinePix J12";
	$ID2Compact{ 346 } = "PowerShot G3";
	$ID2Compact{ 911 } = "EeasyShare M1033";
	$ID2Compact{ 1327 } = "Panasonic Lumix DMC-FZ38";
	$ID2Compact{ 677 } = "Stylus 710";
	$ID2Compact{ 1111 } = "Coolpix S610";
	$ID2Compact{ 333 } = "EASYSHARE DX6440";
	$ID2Compact{ 590 } = "Optio A30";
	$ID2Compact{ 895 } = "FinePix F100fd";
	$ID2Compact{ 323 } = "EASYSHARE Z7590";
	$ID2Compact{ 69 } = "Coolpix S4";
	$ID2Compact{ 191 } = "DSC-W1";
	$ID2Compact{ 545 } = "Panasonic Lumix DMC-FZ8";
	$ID2Compact{ 187 } = "DSC-H1";
	$ID2Compact{ 262 } = "Leica D-LUX 3";
	$ID2Compact{ 79 } = "PowerShot A610";
	$ID2Compact{ 212 } = "PowerShot SD700 IS";
	$ID2Compact{ 352 } = "DSC-T7";
	$ID2Compact{ 1006 } = "Panasonic Lumix DMC-LX3";
	$ID2Compact{ 126 } = "Optio 30";
	$ID2Compact{ 1149 } = "FinePix S8100fd";
	$ID2Compact{ 1034 } = "Lumix DMC-LZ8";
	$ID2Compact{ 251 } = "EASYSHARE C643";
	$ID2Compact{ 542 } = "Coolpix P5000";
	$ID2Compact{ 1305 } = "Lumix DMC-ZS3";
	$ID2Compact{ 279 } = "GR Digital";
	$ID2Compact{ 961 } = "Lumix DMC-FS3";
	$ID2Compact{ 256 } = "Digimax L50";
	$ID2Compact{ 372 } = "DSC-W40";
	$ID2Compact{ 810 } = "FinePix S9000 Z";
	$ID2Compact{ 574 } = "NV11";
	$ID2Compact{ 977 } = "FE-210";
	$ID2Compact{ 99 } = "FinePix S602 Z";
	$ID2Compact{ 526 } = "Coolpix P3";
	$ID2Compact{ 72 } = "Coolpix 8800";
	$ID2Compact{ 566 } = "Exilim EX-P600";
	$ID2Compact{ 806 } = "DSC-H3";
	$ID2Compact{ 264 } = "Digimax A5";
	$ID2Compact{ 255 } = "EASYSHARE P850";
	$ID2Compact{ 1221 } = "PowerShot SD780 IS";
	$ID2Compact{ 359 } = "PowerShot S30";
	$ID2Compact{ 182 } = "DSC-W7";
	$ID2Compact{ 717 } = "PowerShot TX1";
	$ID2Compact{ 1053 } = "PowerShot SD890 IS";
	$ID2Compact{ 232 } = "PowerShot A70";
	$ID2Compact{ 477 } = "FinePix F810";
	$ID2Compact{ 815 } = "S630";
	$ID2Compact{ 671 } = "DSC-P43";
	$ID2Compact{ 1362 } = "Canon PowerShot S90";
	$ID2Compact{ 207 } = "DSC-W30";
	$ID2Compact{ 142 } = "Exilim EX-Z500";
	$ID2Compact{ 330 } = "EASYSHARE CX7300";
	$ID2Compact{ 263 } = "Digimax V800";
	$ID2Compact{ 394 } = "EASYSHARE Z612";
	$ID2Compact{ 360 } = "PowerShot S50";
	$ID2Compact{ 610 } = "DSC-P12";
	$ID2Compact{ 692 } = "PowerShot A510";
	$ID2Compact{ 725 } = "DMC-FX100";
	$ID2Compact{ 50 } = "FinePix E900";
	$ID2Compact{ 1198 } = "Mju 1040";
	$ID2Compact{ 510 } = "EASYSHARE V530";
	$ID2Compact{ 393 } = "Caplio R1";
	$ID2Compact{ 1136 } = "PowerShot SD990 IS";
	$ID2Compact{ 449 } = "FinePix F610";
	$ID2Compact{ 293 } = "Coolpix L3";
	$ID2Compact{ 274 } = "Optio S4I";
	$ID2Compact{ 877 } = "EasyShare V1253";
	$ID2Compact{ 967 } = "Exilim EX-S10";
	$ID2Compact{ 322 } = "EASYSHARE Z700";
	$ID2Compact{ 469 } = "DiMAGE Z6";
	$ID2Compact{ 1128 } = "FinePix J150W";
	$ID2Compact{ 353 } = "PowerShot S40";
	$ID2Compact{ 375 } = "FinePix F10";
	$ID2Compact{ 984 } = "Lumix DMC-TZ15";
	$ID2Compact{ 946 } = "Sony DSC-H10";
	$ID2Compact{ 979 } = "Lumix DMC-LS75";
	$ID2Compact{ 1005 } = "Lumix DMC-FX150";
	$ID2Compact{ 310 } = "D540";
	$ID2Compact{ 40 } = "PowerShot S400";
	$ID2Compact{ 192 } = "DSC-P72";
	$ID2Compact{ 303 } = "Coolpix 4100";
	$ID2Compact{ 250 } = "EASYSHARE DX6490";
	$ID2Compact{ 501 } = "CoolPix 8400";
	$ID2Compact{ 699 } = "Exilim EX-V7";
	$ID2Compact{ 215 } = "PowerShot A700";
	$ID2Compact{ 278 } = "Optio A10";
	$ID2Compact{ 150 } = "Lumix DMC-FZ20";
	$ID2Compact{ 155 } = "Panasonic Lumix DMC-FZ50";
	$ID2Compact{ 130 } = "Coolpix 4300";
	$ID2Compact{ 1373 } = "Stylus 9000";
	$ID2Compact{ 387 } = "Leica V-LUX 1";
	$ID2Compact{ 53 } = "FinePix Z2";
	$ID2Compact{ 245 } = "PowerShot SD30";
	$ID2Compact{ 267 } = "Digimax V6";
	$ID2Compact{ 543 } = "Digimax L85";
	$ID2Compact{ 626 } = "Lumix DMC-LZ6";
	$ID2Compact{ 1294 } = "Lumix DMC-FS15";
	$ID2Compact{ 354 } = "Stylus Verve S";
	$ID2Compact{ 461 } = "DiMAGE A200";
	$ID2Compact{ 257 } = "EASYSHARE Z650";
	$ID2Compact{ 951 } = "DSC-W150";
	$ID2Compact{ 85 } = "Exilim EX-Z1000";
	$ID2Compact{ 809 } = "FinePix A900";
	$ID2Compact{ 332 } = "EASYSHARE C743";
	$ID2Compact{ 591 } = "Coolpix L10";
	$ID2Compact{ 736 } = "Nikon Coolpix P5100";
	$ID2Compact{ 539 } = "Stylus 725 SW";
	$ID2Compact{ 90 } = "Exilim EX-Z600";
	$ID2Compact{ 276 } = "Optio 750Z";
	$ID2Compact{ 620 } = "FinePix A820";
	$ID2Compact{ 1434 } = "Leica X1";
	$ID2Compact{ 891 } = "EasyShare V1233";
	$ID2Compact{ 532 } = "DSC-P120";
	$ID2Compact{ 730 } = "Stylus 1000";
	$ID2Compact{ 1193 } = "Lumix DMC-ZS1";
	$ID2Compact{ 921 } = "Panasonic Lumix DMC-LZ10";
	$ID2Compact{ 233 } = "Canon PowerShot A95";
	$ID2Compact{ 259 } = "Samsung Pro815";
	$ID2Compact{ 57 } = "PowerShot SD400";
	$ID2Compact{ 424 } = "FinePix A510";
	$ID2Compact{ 316 } = "D-LUX 2";
	$ID2Compact{ 89 } = "Exilim EX-S600";
	$ID2Compact{ 611 } = "Optio W30";
	$ID2Compact{ 988 } = "DSC-S750";
	$ID2Compact{ 849 } = "FE-230";
	$ID2Compact{ 208 } = "DSC-S60";
	$ID2Compact{ 347 } = "FinePix F31fd";
	$ID2Compact{ 842 } = "Coolpix L6";
	$ID2Compact{ 434 } = "FinePix F710";
	$ID2Compact{ 93 } = "Lumix DMC-FX07";
	$ID2Compact{ 899 } = "SP-570UZ";
	$ID2Compact{ 1184 } = "Fujifilm FinePix F200EXR";
	$ID2Compact{ 292 } = "Coolpix 7900";
	$ID2Compact{ 291 } = "Coolpix L2";
	$ID2Compact{ 904 } = "Coolpix L18";
	$ID2Compact{ 114 } = "DSC-V1";
	$ID2Compact{ 199 } = "PowerShot S230";
	$ID2Compact{ 442 } = "Exilim EX-P700";
	$ID2Compact{ 930 } = "CoolPix P60";
	$ID2Compact{ 1318 } = "Exilim EX-H10";
	$ID2Compact{ 409 } = "DiMAGE Z3";
	$ID2Compact{ 67 } = "Coolpix P2";
	$ID2Compact{ 241 } = "PowerShot SD200";
	$ID2Compact{ 327 } = "EASYSHARE V550";
	$ID2Compact{ 320 } = "Lumix DMC-FZ3";
	$ID2Compact{ 674 } = "Lumix DMC-FX12";
	$ID2Compact{ 811 } = "Exilim EX-Z77";
	$ID2Compact{ 280 } = "Optio 330";
	$ID2Compact{ 273 } = "Optio T10";
	$ID2Compact{ 471 } = "Konica Minolta DiMAGE Z10";
	$ID2Compact{ 202 } = "DSC-P73";
	$ID2Compact{ 361 } = "PowerShot A400";
	$ID2Compact{ 249 } = "NV10";
	$ID2Compact{ 184 } = "DSC-W5";
	$ID2Compact{ 1189 } = "Nikon CoolPix L100";
	$ID2Compact{ 776 } = "Fujifilm FinePix F50 fd";
	$ID2Compact{ 894 } = "Fujifilm FinePix S1000fd";
	$ID2Compact{ 858 } = "S85";
	$ID2Compact{ 140 } = "Coolpix 4200";
	$ID2Compact{ 131 } = "Coolpix S6";
	$ID2Compact{ 181 } = "DSC-P41";
	$ID2Compact{ 412 } = "Fujifilm FinePix S6000fd";
	$ID2Compact{ 314 } = "Lumix DMC-LS1";
	$ID2Compact{ 801 } = "Fujifilm FinePix S8000fd";
	$ID2Compact{ 154 } = "Lumix DMC-LZ5";
	$ID2Compact{ 355 } = "NV7";
	$ID2Compact{ 847 } = "NV8";
	$ID2Compact{ 479 } = "FinePix A400";
	$ID2Compact{ 159 } = "Lumix DMC-LX1";
	$ID2Compact{ 1101 } = "Lumix DMC-LS80";
	$ID2Compact{ 1252 } = "CyberShot DSC-W290";
	$ID2Compact{ 704 } = "EasyShare C315";
	$ID2Compact{ 326 } = "EASYSHARE LS753";
	$ID2Compact{ 1003 } = "Optio E40";
	$ID2Compact{ 555 } = "Lumix DMC-TZ3";
	$ID2Compact{ 47 } = "PowerShot SD550";
	$ID2Compact{ 619 } = "QV-4000";
	$ID2Compact{ 335 } = "EASYSHARE CX7530";
	$ID2Compact{ 1017 } = "Panasonic Lumix DMC-FZ28";
	$ID2Compact{ 195 } = "DSC-P200";
	$ID2Compact{ 1057 } = "Leica D-LUX 4";
	$ID2Compact{ 538 } = "Caplio R6";
	$ID2Compact{ 554 } = "FinePix V10";
	$ID2Compact{ 552 } = "EASYSHARE V705";
	$ID2Compact{ 647 } = "Lumix DMC-LZ7";
	$ID2Compact{ 74 } = "Coolpix 5200";
	$ID2Compact{ 334 } = "EASYSHARE DC4800";
	$ID2Compact{ 440 } = "Exilim EX-S100";
	$ID2Compact{ 890 } = "Stylus 850 SW";
	$ID2Compact{ 786 } = "DMC-LS2";
	$ID2Compact{ 1117 } = "DSC-W130";
	$ID2Compact{ 740 } = "Stylus 790 SW";
	$ID2Compact{ 1008 } = "EasyShare M863";
	$ID2Compact{ 115 } = "Optio WP";
	$ID2Compact{ 377 } = "Optio 330GS";
	$ID2Compact{ 1282 } = "CyberShot DSC-H20";
	$ID2Compact{ 201 } = "DSC-S40";
	$ID2Compact{ 602 } = "C-LUX 2";
	$ID2Compact{ 1044 } = "Exilim EX-Z80";
	$ID2Compact{ 612 } = "Coolpix S9";
	$ID2Compact{ 902 } = "Sony DSC-H50";
	$ID2Compact{ 1222 } = "Exilim EX-FH20";
	$ID2Compact{ 91 } = "Lumix DMC-FX9";
	$ID2Compact{ 266 } = "Digimax 370";
	$ID2Compact{ 1086 } = "Canon PowerShot G10";
	$ID2Compact{ 701 } = "EasyShare V803";
	$ID2Compact{ 214 } = "PowerShot SD600";
	$ID2Compact{ 422 } = "FinePix A370";
	$ID2Compact{ 564 } = "Fujifilm FinePix F40fd";
	$ID2Compact{ 1194 } = "Stylus 840";
	$ID2Compact{ 97 } = "Caplio GX8";
	$ID2Compact{ 731 } = "DSC-W85";
	$ID2Compact{ 41 } = "Kodak EASYSHARE P880";
	$ID2Compact{ 52 } = "FinePix S9000";
	$ID2Compact{ 302 } = "Coolpix 995";
	$ID2Compact{ 693 } = "Coolpix S200";
	$ID2Compact{ 673 } = "FinePix F455";
	$ID2Compact{ 593 } = "FinePix S3500 Zoom";
	$ID2Compact{ 770 } = "DMC-FX55";
	$ID2Compact{ 188 } = "DSC-W50";
	$ID2Compact{ 68 } = "Coolpix S7c";
	$ID2Compact{ 315 } = "Caplio R2";
	$ID2Compact{ 893 } = "EX-Z11";
	$ID2Compact{ 338 } = "EASYSHARE DX7440";
	$ID2Compact{ 738 } = "Ricoh Caplio R7";
	$ID2Compact{ 83 } = "PowerShot A710 IS";
	$ID2Compact{ 804 } = "Z1275";
	$ID2Compact{ 870 } = "Lumix DMC-FX35";
	$ID2Compact{ 305 } = "PowerShot S500";
	$ID2Compact{ 896 } = "Exilim EX-Z100";
	$ID2Compact{ 623 } = "Sony DSC-H7";
	$ID2Compact{ 1385 } = "PowerShot SD 980 IS";
	$ID2Compact{ 217 } = "PowerShot SD900";
	$ID2Compact{ 962 } = "Lumix DMC-FS5";
	$ID2Compact{ 328 } = "EASYSHARE CX7430";
	$ID2Compact{ 1085 } = "NV24HD";
	$ID2Compact{ 755 } = "EASYSHARE V1003";
	$ID2Compact{ 122 } = "Optio S50";
	$ID2Compact{ 143 } = "Exilim EX-Z40";
	$ID2Compact{ 464 } = "DSC-P10";
	$ID2Compact{ 269 } = "PowerShot S410";
	$ID2Compact{ 628 } = "EASYSHARE C300";
	$ID2Compact{ 158 } = "Lumix DMC-LZ3";
	$ID2Compact{ 281 } = "Optio S7";
	$ID2Compact{ 733 } = "PowerShot SD870 IS";
	$ID2Compact{ 562 } = "Canon PowerShot A570 IS";
	$ID2Compact{ 36 } = "PowerShot S80";
	$ID2Compact{ 997 } = "Coolpix S600";
	$ID2Compact{ 1102 } = "FE-340";
	$ID2Compact{ 439 } = "QV-R51";
	$ID2Compact{ 362 } = "FinePix Z1";
	$ID2Compact{ 608 } = "DSC-W90";
	$ID2Compact{ 411 } = "DiMAGE G500";
	$ID2Compact{ 132 } = "Coolpix P4";
	$ID2Compact{ 1649 } = "Cyber-shot DSC-TX9";
	$ID2Compact{ 1234 } = "CoolPix S52";
	$ID2Compact{ 478 } = "Coolpix L5";
	$ID2Compact{ 398 } = "Optio 33LF";
	$ID2Compact{ 836 } = "Stylus 830";
	$ID2Compact{ 942 } = "EasyShare C813";
	$ID2Compact{ 955 } = "FinePix A920";
	$ID2Compact{ 407 } = "PowerShot SD20";
	$ID2Compact{ 537 } = "Coolpix L11";
	$ID2Compact{ 18 } = "DSC-T5";
	$ID2Compact{ 912 } = "EasyShare Z1012 IS";
	$ID2Compact{ 376 } = "Fujifilm FinePix F20";
	$ID2Compact{ 125 } = "Optio 33WR";
	$ID2Compact{ 599 } = "DSC-T100";
	$ID2Compact{ 44 } = "Fujifilm FinePix S5600";
	$ID2Compact{ 587 } = "DSC-S650";
	$ID2Compact{ 1235 } = "DMC-TZ7";
	$ID2Compact{ 190 } = "DSC-T1";
	$ID2Compact{ 741 } = "EASYSHARE M753";
	$ID2Compact{ 95 } = "Caplio GX";
	$ID2Compact{ 998 } = "Ricoh Caplio GX200";
	$ID2Compact{ 601 } = "Exilim EX-Z1050";
	$ID2Compact{ 313 } = "Lumix DMC-LZ2";
	$ID2Compact{ 243 } = "PowerShot S70";
	$ID2Compact{ 779 } = "Coolpix P50";
	$ID2Compact{ 148 } = "QV-R62";
	$ID2Compact{ 397 } = "PowerShot SD630";
	$ID2Compact{ 857 } = "PowerShot SX100 IS";
	$ID2Compact{ 1150 } = "C-LUX 3";
	$ID2Compact{ 157 } = "Coolpix 3500";
	$ID2Compact{ 964 } = "Lumix DMC-L10";
	$ID2Compact{ 65 } = "Optio W10";
	$ID2Compact{ 203 } = "DSC-V3";
	$ID2Compact{ 261 } = "Digimax S500";
	$ID2Compact{ 908 } = "PowerShot SD950 IS";
	$ID2Compact{ 81 } = "PowerShot A630";
	$ID2Compact{ 1191 } = "Nikon Coolpix P90";
	$ID2Compact{ 321 } = "EASYSHARE DX7590";
	$ID2Compact{ 459 } = "DiMAGE Z5";
	$ID2Compact{ 624 } = "DSC-W200";
	$ID2Compact{ 86 } = "Exilim EX-Z850";
	$ID2Compact{ 284 } = "Optio 450";
	$ID2Compact{ 247 } = "PowerShot A85";
	$ID2Compact{ 371 } = "DSC-P92";
	$ID2Compact{ 656 } = "Digimax S1000";
	$ID2Compact{ 204 } = "Sony DSC-F828";
	$ID2Compact{ 289 } = "Coolpix 7600";
	$ID2Compact{ 771 } = "Casio Exilim EX-Z1080";
	$ID2Compact{ 435 } = "FinePix F11";
	$ID2Compact{ 401 } = "DiMAGE Z2";
	$ID2Compact{ 186 } = "DSC-P100";
	$ID2Compact{ 924 } = "EasyShare M873";
	$ID2Compact{ 884 } = "EasyShare M853";
	$ID2Compact{ 1412 } = "Canon PowerShot G11";
	$ID2Compact{ 147 } = "Exilim EX-Z120";
	$ID2Compact{ 339 } = "EASYSHARE Z730";
	$ID2Compact{ 228 } = "PowerShot S2 IS";
	$ID2Compact{ 863 } = "EasyShare ZD710";
	$ID2Compact{ 268 } = "Digimax S800";
	$ID2Compact{ 319 } = "Digilux 1";
	$ID2Compact{ 1297 } = "Ricoh GR Digital III";
	$ID2Compact{ 404 } = "DSC-P32";
	$ID2Compact{ 613 } = "Coolpix S10";
	$ID2Compact{ 669 } = "FinePix A500";
	$ID2Compact{ 282 } = "Optio 555";
	$ID2Compact{ 420 } = "FinePix A303";
	$ID2Compact{ 121 } = "Optio S";
	$ID2Compact{ 702 } = "EasyShare C613";
	$ID2Compact{ 989 } = "Lumix DMC-FX520";
	$ID2Compact{ 487 } = "EASYSHARE C875";
	$ID2Compact{ 1071 } = "Olympus E-520";
	$ID2Compact{ 1348 } = "Fujifilm FinePix S200EXR";
	$ID2Compact{ 238 } = "PowerShot S60";
	$ID2Compact{ 636 } = "SP-500 UZ";
	$ID2Compact{ 577 } = "FE-140";
	$ID2Compact{ 253 } = "EASYSHARE V570";
	$ID2Compact{ 448 } = "FinePix E550";
	$ID2Compact{ 209 } = "Konica Minolta DiMAGE 7Hi";
	$ID2Compact{ 216 } = "PowerShot SD500";
	$ID2Compact{ 117 } = "Optio S5I";
	$ID2Compact{ 852 } = "FinePix S100fs";
	$ID2Compact{ 63 } = "Optio M20";
	$ID2Compact{ 455 } = "DiMAGE X1";
	$ID2Compact{ 80 } = "PowerShot A640";
	$ID2Compact{ 336 } = "EASYSHARE DX4530";
	$ID2Compact{ 457 } = "DSC-T50";
	$ID2Compact{ 179 } = "Caplio G4 Wide";
	$ID2Compact{ 383 } = "Digimax I6";
	$ID2Compact{ 963 } = "DP1";
	$ID2Compact{ 297 } = "Coolpix 3200";
	$ID2Compact{ 676 } = "Stylus 760";
	$ID2Compact{ 1334 } = "EasyShare Z915";
	$ID2Compact{ 277 } = "Optio S5N";
	$ID2Compact{ 92 } = "Panasonic Lumix DMC-FZ30";
	$ID2Compact{ 1192 } = "Coolpix P6000";
	$ID2Compact{ 1212 } = "Canon PowerShot SX200 IS";
	$ID2Compact{ 550 } = "Exilim EX-Z700";
	$ID2Compact{ 670 } = "DSC-W35";
	$ID2Compact{ 929 } = "Canon PowerShot SD1100 IS";
	$ID2Compact{ 1094 } = "Lumix DMC-FX37";
	$ID2Compact{ 958 } = "DSC-W300";
	$ID2Compact{ 419 } = "FinePix S5000Z";
	$ID2Compact{ 133 } = "Coolpix 4600";
	$ID2Compact{ 290 } = "Coolpix L4";
	$ID2Compact{ 149 } = "Panasonic Lumix DMC-FZ7";
	$ID2Compact{ 123 } = "Optio S30";
	$ID2Compact{ 592 } = "Ricoh Caplio GX100";
	$ID2Compact{ 304 } = "Digimax A502";
	$ID2Compact{ 210 } = "DiMAGE 7";
	$ID2Compact{ 406 } = "DiMAGE X50";
	$ID2Compact{ 396 } = "DSC-N1";
	$ID2Compact{ 482 } = "EASYSHARE V603";
	$ID2Compact{ 56 } = "Canon PowerShot G7";
	$ID2Compact{ 66 } = "Coolpix L1";
	$ID2Compact{ 54 } = "Panasonic Lumix DMC-LX2";
	$ID2Compact{ 1204 } = "FinePix S1500";
	$ID2Compact{ 470 } = "DiMAGE Z20";
	$ID2Compact{ 70 } = "Coolpix 5400";
	$ID2Compact{ 1205 } = "Panasonic DMC-GH1";
	$ID2Compact{ 88 } = "Exilim EX-Z60";
	$ID2Compact{ 570 } = "Canon PowerShot SD750";
	$ID2Compact{ 909 } = "Panasonic Lumix DMC-TZ4";
	$ID2Compact{ 252 } = "EASYSHARE LS443";
	$ID2Compact{ 466 } = "DSC-N2";
	$ID2Compact{ 883 } = "Caplio R8";
	$ID2Compact{ 134 } = "Coolpix 5600";
	$ID2Compact{ 283 } = "Coolpix 990";
	$ID2Compact{ 156 } = "Lumix DMC-LC1";
	$ID2Compact{ 618 } = "Optio M30";
	$ID2Compact{ 848 } = "NV15";
	$ID2Compact{ 655 } = "EASYSHARE C653";
	$ID2Compact{ 450 } = "FinePix F700";
	$ID2Compact{ 1323 } = "DSC-TX1";
	$ID2Compact{ 271 } = "FinePix S5500";
	$ID2Compact{ 1016 } = "Optio M40";
	$ID2Compact{ 318 } = "C-LUX 1";
	$ID2Compact{ 799 } = "Coolpix L15";
	$ID2Compact{ 473 } = "DSC-S500";
	$ID2Compact{ 840 } = "Optio E30";
	$ID2Compact{ 185 } = "DSC-T10";
	$ID2Compact{ 390 } = "DSC-P52";
	$ID2Compact{ 248 } = "Stylus 410";
	$ID2Compact{ 146 } = "QV-R61";
	$ID2Compact{ 356 } = "DSC-P150";
	$ID2Compact{ 802 } = "Coolpix S51";
	$ID2Compact{ 164 } = "PowerShot G5";
	$ID2Compact{ 196 } = "DSC-P93";
	$ID2Compact{ 807 } = "Canon PowerShot A650 IS";
	$ID2Compact{ 242 } = "PowerShot Pro1";
	$ID2Compact{ 945 } = "CoolPix S510";
	
}
	  
