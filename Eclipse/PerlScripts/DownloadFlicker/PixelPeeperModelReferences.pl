#! /usr/bin/perl

###########################################################
#
# script per il download delle immagini da flickr.com
# utilizzando pixel-peeper.com per accedere solo alle 
# immagini real size e non passare per l'autenticazione
#
# $Id: FetchFlickrImages.pl 331 2012-04-28 14:41:57Z cattaneo $
# $LastChangedDate: 2012-04-28 16:41:57 +0200 (Sat, 28 Apr 2012) $
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
			if ($addr=~/\?camera=([0-9]+)/) {
				print "cameraid = ", $1, "\n";
				$cameraid = $1;
			}
		}
	 }
	 elsif ($event eq "text") {
	 	if ($cameraid) {
 			print "Got: ", $text, " for id: ", $cameraid, "\n";
		 	$ModelsMap{$text} = $cameraid;
		 	$ID2ModelMap{$cameraid} = $text;
	 		$cameraid = "";
		}	 	
	 }
	 elsif ($loaded == 0 && $event eq "start" && $tagname eq "option") {
	 	print "tag: ", $tagname, " text: ", $text, " attr: ", $locAttr{'value'}, "\n" if ($Debug);
	 	$nextText = 1;
	 	$cameraid = $locAttr{'value'};
	 }
}


sub DownloadModels() {

	my $p = HTML::Parser->new(api_version => 3);
	my($url) = '';
	
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
		
#	local $/;
#	open(FILE, 'Digicams.html') or die "Can't read html file 'filename' [$!]\n";  
#	my $document = <FILE>; 
#	close (FILE);  
#	$p->parse($document);
	
	$url = 'http://www.pixel-peeper.com/digicams';

	$agent = new LWP::UserAgent;
	$response = $agent->request(GET $url);
		
	if ($response->is_success) {
		printf("OK, parsing %s contents\n", $url);
		$p->parse($response->content);		
	}
	elsif ($response->is_redirect) {
		print $outFH $response->as_string, "\n";
		print $outFH "redirect:\n";
		print $outFH $response->base, "\n";
		print $outFH $response->header( Location), "\n";
	}

	foreach $m (keys %ModelsMap) {
#		$ModelsMap{ 'Canon EOS 5D'} = 19;
		printf( '$ModelsMap{ \'%s\'} = %d;', $m, $ModelsMap{$m});
		print("\n");
	} 
	foreach $m (keys %ID2ModelMap) {
#		$ID2ModelMap{ 714} = "Canon EOS 40D";
		printf( '$ID2ModelMap{ %d } = "%s";', $m, $ID2ModelMap{$m});
		print("\n");
	} 
}	


############################################################
#
# Main section
#
############################################################

	$DownloadDirectory = "/Users/pipp8/Downloads/Photos/";
#	$DownloadDirectory = "/home/ifnew/LOCAL_IMG_DATASE/Flickr/";
	
	%$ModelsMap = ();
	%ID2ModelMap = ();
	DownloadModels();
	exit;
	
