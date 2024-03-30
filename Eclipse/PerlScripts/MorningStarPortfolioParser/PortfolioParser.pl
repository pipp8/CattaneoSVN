#! /usr/local/bin/perl


###########################################################
#
# script per la conversione della pagina html di morningstar 
# nel formato CSV separato da tab
#
# $Id: PortfolioParser.pl 847 2016-07-31 17:34:52Z cattaneo $
# $LastChangedDate: 2016-07-31 19:34:52 +0200(Dom, 31 Lug 2016) $
#
###########################################################




use HTML::Parser;

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
     
	 if ($event eq "start" && defined($tagname) && $tagname eq "table") {
		if (defined($attr) && exists($locAttr{'id'}) && $locAttr{'id'}=~/ctl00\_ctl00/) {
			$Found = 1;
			printf("Found ID: %s\n", $locAttr{'id'}) if ($Debug);
		}
	 }
 	 elsif ($Found == 1 && $event eq "start" && defined($tagname) && $tagname eq "td") {
 	 	# printf("TD: ", $text);
 	 	# printf(OUT "TD: ", $text); 	 
 	 }
 	 elsif ($Found == 1 && $event eq "text") {
 	 	if ($text !~ /[\t\n]+/) {
 	 	 	printf("%s ", $text);
 	 	 	printf(OUT "%s ", $text);
 		 }
 	 }
 	 elsif ($Found == 1 && $event eq "end" && defined($tagname) && ($tagname eq "td" || $tagname eq "th")) {
 	 	 	printf("\t", $text);
 	 	 	printf(OUT "\t", $text);
 	 }
 	 elsif ($Found == 1 && $event eq "end" && defined($tagname) && $tagname eq "tr") {
 	 	print("\n");
 	 	print(OUT "\n");
 	 	$Count++;
 	 }
 	 elsif ($Found == 1 && $event eq "end" && defined($tagname) && $tagname eq "table") {
	 	$Found = 0;
	 }
}




############################################################
#
# Main section
#
############################################################

	$Debug = 1;
	$DumpHtml = 0;
	$Count = 0;
	$inFile = "portfolio.aspx.html";
	
	if ($#ARGV >= 0) {
		for ($n=0 ; $n<=$#ARGV ; $n++) {
			print("ARGV[$n]=$ARGV[$n]\n");
			$inFile = $ARGV[0]; # input file is the first paramenter
		}
	}
	else {
		printf("No command line parameters found. Using default input file: %s\n", $inFile);
	}
	
	
	
	my $p = HTML::Parser->new(api_version => 3);
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	open(OUT, ">portfolio.txt") || die "output file: $!";
	
	open(INFILE, "<$inFile") || die "Can't read input file $inFile : $!";

	undef $/;
	$htmlCode = <INFILE>;	# read the whole file
	
	printf( "Parsing input file: %s\n", $inFile);

	$p->parse($htmlCode);		
	
	if ($Count > 1) {
		printf("Totale Titoli Scaricati: %d\n", $Count);
	}
	else {
		print "No Page Found\n" if ($Debug);
	}
	exit;
	
	  
	  
