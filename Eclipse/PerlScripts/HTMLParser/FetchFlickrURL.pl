use HTML::Parser ();
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;
  

sub handler {
	 my($event, $line, $column, $text, $tagname, $attr) = @_;
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
#		 print $text, "\n";
		 if ($text =~ /phid=/) {
		 	if ($text=~/href=('.*%3Do')\s+/) {
		 		if ($1=~/url=(.+)'/) {
		 			$url = $1;
		 			$url=~s/%26/&/g;
		 			$url=~s/%2F/\//g;
		 			$url=~s/%3A/:/g;
		 			$url=~s/%3D/=/g;
		 			$url=~s/%3F/?/g;

			 		print LOG $url, "\n";
		 		}
		 	}
#			 print "Text: ", $text, "\n";
		 }
	 }
}



	my $p = HTML::Parser->new(api_version => 3);
	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	my $agent = new LWP::UserAgent;
	
	# come calcolare l'ultima pagina ???
	# 8 = canon A500
	$cameraModel = 8;
	$lastPage = 3079;
	open(LOG, ">c:\\tmp\\flickrURL-". $cameraModel . ".txt") || die "log file: $!";
	$piperURL = 'http://www.pixel-peeper.com/cameras/';

	for( $i = 1; $i <= $lastPage; $i++ ) {
#		http://www.pixel-peeper.com/cameras/?camera=8&perpage=30&iso_min=none&iso_max=none&exp_min=none&exp_max=none&res=1&digicam=0
		$urlF = $piperURL . "?camera=" . $cameraModel . "&perpage=30&iso_min=none&iso_max=none&exp_min=none&exp_max=none&res=1&digicam=0" . "&p=" . $i;  
		$response = $agent->request(GET $urlF);
#			 [camera => $cameraModel,
#			  perpage => '30',
#			  iso_min => 'none',
#			  iso_max => 'none',
#			  exp_min => 'none',
#			  exp_max => 'none',
#			  res => '1',
#			  digicam => '0',
#			  p => $i]);
		
		if ($response->is_success) {
			print $response->base, " OK, parsing page:", $i, " contents:\n";
			$p->parse($response->content);		
		}
		elsif ($response->is_redirect) {
			print $response->as_string, "\n";
			print "redirect:\n";
			print $response->base, "\n";
#			print $response->header( Location), "\n";
		}	
	} 
	close LOG;
	exit;

	  