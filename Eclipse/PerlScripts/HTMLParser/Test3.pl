use HTML::Parser ();
use Data::Dump ();
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Request::Common;
use HTTP::Response;
use HTTP::Headers;
# use Image::Grab;
  

sub handler {
    my($event, $line, $column, $text, $tagname, $attr) = @_;
#    my @d = (uc(substr($event,0,1)) . " L$line C$column");
#    my @d = ($event . " L$line C$column");
#    substr($text, 40) = "..." if length($text) > 40;
#    push(@d, $text);
#    push(@d, $tagname) if defined $tagname;
#    push(@d, $attr) if $attr;

#    print Data::Dump::dump(@d),"\n";
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

			 		print "url = ", $url, "\n";
		 		}
		 	}
#			 print "Text: ", $text, "\n";
		 }
#		 print "tag:  ", $tagname, "\n";
#		 print "attr:", $attr, "\n";
	 }
}


sub handler2 {
    my($event, $line, $column, $text, $tagname, $attr) = @_;
	 print "handler2:", $event, $text, "\n";
	 if ($event eq "start" && defined($tagname) && $tagname eq "a") {	
		 print $text, "\n";
		 if ($text =~ /phid=/) {
		 	if ($text=~/href=('.*%3Do')\s+/) {
		 		if ($1=~/url=(.+)'/) {
		 			$url = $1;
		 			$url=~s/%26/&/g;
		 			$url=~s/%2F/\//g;
		 			$url=~s/%3A/:/g;
		 			$url=~s/%3D/=/g;
		 			$url=~s/%3F/?/g;

			 		print "url = ", $url, "\n";
		 		}
		 	}
		 }
	 }
}

sub downloadURL {
	my($cameraModel, $lastPageself) = @_;
	my $p = HTML::Parser->new(api_version => 3);

	$p->handler(default => \&handler, "event, line, column, text, tagname, attr");
	
	$agent = new LWP::UserAgent;
	
	$piperURL = 'http://www.pixel-peeper.com/cameras/?camera=8';
	my $i;
	for( $i = 1; $i <= $lastPage; $i++ ) {
		$response = $agent->request(POST $piperURL,
			 [camera => $cameraModel,
			  perpage => '30',
			  iso_min => 'none',
			  iso_max => 'none',
			  exp_min => 'none',
			  exp_max => 'none',
			  res => '1',
			  digicam => '0',
			  p => $i]);
		
		if ($response->is_success) {
			print $response->base, " OK, parsing page:", $i, " contents:\n";
			$p->parse($response->content);		
		}
		elsif ($response->is_redirect) {
			print $response->as_string, "\n";
			print "redirect:\n";
			print $response->base, "\n";
			print $response->header( Location), "\n";
		}	
	} 
}

	$cameraModel = 8;	# 8 = canon A500
	$lastPage = 3079;	# come calcolare l'ultima pagina ???
#	downloadURL($cameraModel, $lastPage);
	
	my $p2 =  HTML::Parser->new(api_version => 3);
	$p2->handler(default => \&handler2, "event, line, column, text, tagname, attr");
	
	$urlAbs = 'http://farm3.static.flickr.com/2201/1956100724_51728fed6d_o_d.jpg';
	$url1 = 'http://www.flickr.com/photo_zoom.gne?id=1484821596&size=o';
	$url2 = 'http://www.flickr.com/photos/nosamk/1484821596/sizes/o/';
	$url3 = 'http://farm2.static.flickr.com/1178/1484821596_cd4ba2cc0a_o_d.jpg';
	$url4 = 'http://www.flickr.com/1178/1484821596_cd4ba2cc0a_o_d.jpg';
	$url = 'http://www.flickr.com/photo_zoom.gne';
	#$url = 'http://www.flickr.com';

	$agent = new LWP::UserAgent;
	#$req = HTTP::Request->new(GET => $url);
	
#	$response = $agent->request(POST $url, [id => '1484821596', size => 'o']);
	$response = $agent->request(GET $url1);
	if ($response->is_success) {
		print "OK, Contents:\n";
		print $response->as_string, "\n";
		$p2->parse($response->content);
	  }
	elsif ($response->is_redirect) {
		print $response->as_string, "\n";
		print "redirect:\n";
		print $response->redirects, "\n";
		print "content:\n";
		print $response->content, "\n";
		
		print $response->base, "\n";
#		$url2 = 'http://www.flickr.com' . $response->header( Location);
		print $response->header( Location), " -> ", $url2, "\n";
		$req = HTTP::Request->new(GET => $url2);
		$res2 = $agent->request($req);
		print "res2: ", $res2->as_string, "\n";
		$p2->parse($res2->content);	
	}
	elsif ($response->is_error){
	        print STDERR $response->status_line, "\n";
	        print STDERR $response->content, "\n";
	   }
	
	 $pic = new Image::Grab;
	 $pic->search_url($url2);
	 $pic->regexp(".*\.jpg");
	 $pic->index(0);
	 # $pic->url($urlAbs);
	 print "Fetching Images from: ", $url2, "\n";
	 $pic->grab;
	
	 # Now to save the image to disk
	 open(IMAGE, ">c:\\tmp\\image.jpg") || die"image.jpg: $!";
	 binmode IMAGE;  # ahi ahi  MSDOS ?!?!?
	 print IMAGE $pic->image;
	 close IMAGE;
	 print "fine\n";
	  