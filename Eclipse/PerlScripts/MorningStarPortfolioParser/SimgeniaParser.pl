#! /usr/bin/perl


###########################################################
#
# script per la conversione della pagina html di morningstar 
# nel formato CSV separato da tab
#
# $Id: PortfolioParser.pl 233 2011-10-30 17:29:51Z cattaneo $
# $LastChangedDate: 2011-10-30 18:29:51 +0100 (Sun, 30 Oct 2011) $
#
###########################################################



use LWP::UserAgent;

use HTTP::Cookies;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;

use Storable;
use Data::Dump;

use HTML::Parser;

  

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
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "table") {
	 	if (defined($attr) && exists($locAttr{'id'})) {
	 		print "Id: $locAttr{'id'}\n";
	 	}
		if (defined($attr) && exists($locAttr{'id'}) && $locAttr{'id'} eq "_dettaglioPtfCliente_WAR_dettaglioptfclienteportlet_tab_ges_id_FON") {
			$Found = 1;
			printf("Found ID1: %s\n", $locAttr{'id'}) if ($Debug);
		}
		if (defined($attr) && exists($locAttr{'id'}) && $locAttr{'id'} eq "_dettaglioPtfCliente_WAR_dettaglioptfclienteportlet_tab_ges_id_SIC") {
			$Found = 2;
			printf("Found ID2: %s\n", $locAttr{'id'}) if ($Debug);
		}
	 }
 	 elsif ($Found > 0 && $event eq "start" && defined($tagname) && $tagname eq "td") {
 	 	printf("TD: ", $text);
 	 	#Êprintf(OUT "TD: ", $text); 	 
 	 }
 	 elsif ($Found > 0 && $event eq "text") {
 	 	if ($text !~ /[\t\n]+/) {
 	 	 	printf("%s ", $text);
 	 	 	printf(OUT "%s ", $text);
 		 }
 	 }
 	 elsif ($Found > 0 && $event eq "end" && defined($tagname) && ($tagname eq "td" || $tagname eq "th")) {
 	 	 	printf("\t", $text);
 	 	 	printf(OUT "\t", $text);
 	 }
 	 elsif ($Found == 1 && $event eq "end" && defined($tagname) && $tagname eq "tr") {
 	 	print("\n");
 	 	print(OUT "\n");
 	 	$Count1++;
 	 }
 	 elsif ($Found == 2 && $event eq "end" && defined($tagname) && $tagname eq "tr") {
 	 	print("\n");
 	 	print(OUT "\n");
 	 	$Count2++;
 	 }
 	 elsif ($Found > 0 && $event eq "end" && defined($tagname) && $tagname eq "table") {
	 	$Found = 0;
	 }
}


sub handler1 {

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
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "input") {
	 	if (defined($attr)) {
	 		print "name: $locAttr{'name'}\n" if exists($locAttr{'name'});
	 		print "value: $locAttr{'value'}\n" if exists($locAttr{'value'});
	 	}
		if (defined($attr) && exists($locAttr{'name'}) && $locAttr{'name'} eq "lt") {
			$FoundName = 1;
			printf("FoundName ID1: %s\n", $locAttr{'name'}) if ($Debug);
		}
		if ($FoundName && defined($attr) && exists($locAttr{'value'})) {
			$FoundValue = 1;
			$KeyValue = $locAttr{'value'};
			printf("Found value: %s\n", $locAttr{'value'}) if ($Debug);
		}
	 }
}


sub ParseHome {

	$cookie_jar = HTTP::Cookies->new(
    	file => "$ENV{'HOME'}/lwp_cookies.dat",
    	autosave => 1);

	my ($request, $response);
	my $url = "https://www.simgenia.it/web/guest/login"; 
	
	my $ua = LWP::UserAgent->new;
	$ua->cookie_jar($cookie_jar);
	
	$FoundName = 0;
	$FoundValue = 0;
	$KeyValue = '';
	
	my $p1 = HTML::Parser->new(api_version => 3);
	$p1->handler(default => \&handler1, "event, line, column, text, tagname, attr");
		
	$response = $ua->request(GET $url);
	if ($response->is_success) {
		print "Getting Home Page: " . $response->as_string . "\n\n" if ($Debug);
		$cookie_jar->extract_cookies($response);
		print $cookie_jar->as_string() . "\n";
		$cookie_jar->save();
#		$p1->parse($response->content);		
	}
	else {
		die("errore");
	}

	$request = HTTP::Request->new( GET 'https://www.simgenia.it/html/js/barebone.jsp?browserId=firefox&themeId=simgenia_WAR_simgeniatheme&colorSchemeId=01&minifierType=js&minifierBundleId=javascript.barebone.files&t=1294419207000');
	$cookie_jar->add_cookie_header($request);
	
	$response = $ua->request( $request);
	if ($response->is_success) {
		print "Getting Page 1: " . $response->as_string . "\n\n" if ($Debug);
		$p1->parse($response->content);		
	}
	else {
		die("errore");
	}
	
	$request = HTTP::Request->new( GET 'https://cas.simgenia.it/cas/login?service=https://www.simgenia.it/web/sim/scrivania-personale&appId=0000000200/login.html');
	$cookie_jar->add_cookie_header($request);
	
	$response = $ua->request($request);
	if ($response->is_success) {
		print "Getting cas Page: " . $response->as_string . "\n\n" if ($Debug);
		$p1->parse($response->content);		
	}
	else {
		die("errore");
	}
}


sub SimgeniaLogin {
	my $home = "https://cas.simgenia.it"; 
	my $ua = LWP::UserAgent->new;
	$ua->cookie_jar($cookie_jar);
	 
	my $p1 = HTML::Parser->new(api_version => 3);
	$p1->handler(default => \&handler, "event, line, column, text, tagname, attr");
		
	my $req = POST $home . '/cas/login?service=https://www.simgenia.it/web/sim/scrivania-personale&appId=0000000200',
			[
				lt			=>	'62E0188D092424A15AA1F18CFBFDEA02E7AE50BE95B59EC7A61C2DC63730FFEEFB26DC444DE71C79546EF9D9EC42169E',
				_eventId	=>	'submit',
				submit		=>	'LOGIN',
				username	=>	'c0174624',
				password	=>	'scEccA201'
			];

	my $response = $ua->request( $req);
	if ($response->is_success) {
		print "Getting Home Page: " . $response->as_string . "\n\n" if ($Debug);
		$p1->parse($response->content);		
	}
	else {
		die("errore");
	}
}



# https://www.simgenia.it/web/guest/login
# https://www.simgenia.it/web/sim/scrivania-personale?ticket=ST-526-6ERlmAsiril3Kta0AGtX-cas
# https://www.simgenia.it/web/sim/risparmio-gestito


############################################################
#
# Main section
#
############################################################

	$Debug = 1;
	$DumpHtml = 0;
	$Count1 = 0;
	$Count2 = 0;
	$inFile = "risparmio-gestito-p?.html";
	$home = "https://cas.simgenia.it"; 
	
	
	if ($#ARGV >= 0) {
		for ($n=0 ; $n<=$#ARGV ; $n++) {
			print("ARGV[$n]=$ARGV[$n]\n");
			$inFile = $ARGV[0]; # input file is the first paramenter
		}
	}
	else {
		printf("No command line parameters found. Using default input file: %s\n", $inFile);
	}
	
#	ParseHome;
#	SimgeniaLogin;
	
	my @files = glob($inFile);
	print $#files+1 . " files found\n" if ($Debug);
	my $i;
	
	my $p = HTML::Parser->new(api_version => 3);
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	open(OUT, ">portfolio.txt") || die "output file: $!";
	
	if ($#files > 0) {
		for($i = 0; $i <= $#files; $i++) {
					
			open(INFILE, "<$files[$i]") || die "Can't read input file $files[$i] : $!";
		
			undef $/;
			$htmlCode = <INFILE>;	# read the whole file
			
			printf( "Parsing input file: %s\n", $files[$i]);
			$p->parse($htmlCode);	
		}
			
		if ($Count1 > 0 || $Count2 > 0) {
			printf("Totale Titoli Scaricati: %d\n", $Count1 + $Count2);
		}
		else {
			print "No Data\n" if ($Debug);
		}
	}
	else {
		print "Nessun file dati trovato ($inFile)\n";
	}
	exit;
	
	  
	  
