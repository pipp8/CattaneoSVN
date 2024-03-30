#! /usr/local/bin/perl


###########################################################
#
# script per il download delle immagini da flickr.com
# utilizzando pixel-peeper.com per accedere solo alle 
# immagini real size e non passare per l'autenticazione
#
# $Header: /cvsroot/Cattaneo/Sources/PerlScripts/SearchPhoneNumber.pl,v 1.1 2010/09/21 18:27:56 cattaneo Exp $
#
#
###########################################################


use HTML::Parser;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;
use Switch;
use Storable;
use Data::Dump;
  

sub handler {
     my($event, $line, $column, $text, $tagname, $attr) = @_;
     my(%locAttr) = %{$attr} if $attr;
	 
     if ($Debug) {
#	     my @d = (uc(substr($event,0,1)) . " L$line C$column");
    	 my @d = ($event . " L$line C$column");
#	     substr($text, 80) = "..." if length($text) > 80;
	     push(@d, $text);
	     push(@d, $tagname) if defined $tagname;
	     push(@d, $attr) if $attr;
	
#	     print LOG Data::Dump::dump(@d), "\n";
	     print Data::Dump::dump(@d), "\n";
     }
     
     if ($attr) {
     	if (exists $locAttr{'class'}) {
	     	$Started = $locAttr{'class'} ;
     	}
     }
     
     if ($event eq "text" && $Started) {
     	switch ($Started) {
     		case "org"					{ $p1 = $text}
     		case "street-address"		{ $p2 = $text}
     		case "tel"					{ $p3 = $text; print $p1, " -> Address: ", $p2, ",tel: ", $p3, "\n";}
     	}
     	$Started = null;	
     }
     elsif ($event eq "end") {
		;
     }
}

sub downloadURL {
	my ($name, $citta) = @_;
	my $maxPP = 100;
	my $p = HTML::Parser->new(api_version => 3);
		
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");

	open(OUT, ">parser-" . name . "-" . citta . ".txt") || die "Out file: $!";

	$agent = new LWP::UserAgent;
	
	@words = split(/ /, $name);
	$par = "";
	for ($i = 0; $i < $#words; $i++){
		$par = $par . $words[$i] . "+";
	}
	$par = $par . $words[$#words];
	
	# $DirectoryProviderURL = sprintf( "http://www.paginegialle.it/pgol/5-%s/3-%s", $name, $citta);
	$DirectoryProviderURL = "http://www.paginebianche.it/execute.cgi";
	$RequestContent = sprintf("iq=&ver=default&font=default&btt=1&cb=8&l=it&mr=%s&rk=&om=&srd=&srm=&qs=%s&dv=%s", $maxPP, $par, $citta);
	
	print "URL:", $DirectoryProviderURL, "\n", "parameters:", $RequestContent, "\n";
	my $req = HTTP::Request->new(POST => $DirectoryProviderURL);
	$req->content_type('application/x-www-form-urlencoded');
  	$req->content($RequestContent);
	
	$response = $agent->request( $req);
	
	if ($response->is_success) {
		print " OK, parsing page contents:\n";
		$p->parse($response->content);		
	}
	elsif ($response->is_redirect) {
		print $response->as_string, "\n";
		print "redirect:\n";
		print $response->base, "\n";
		print $response->header( Location), "\n";
	}
} 






############################################################
#
# Main section
#
############################################################

	my ($nome,$citta) = ("giuseppe cattaneo", "salerno");
	$Debug = 0;

	if (@ARGV >= 2) {
		downloadURL($ARGV[0], $ARGV[1]);
	}
	else {
		downloadURL($nome, $citta);
	}
	exit;
	
	
	  
