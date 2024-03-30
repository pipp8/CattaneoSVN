#! perl

use HTML::Parser ();

  sub start_handler
  {
    return if shift ne "href";
    my $self = shift;
    $self->handler(text => sub { print shift }, "dtext");
    $self->handler(end  => sub { shift->eof if shift eq "title"; },
		           "tagname,self");
  }

  my $p = HTML::Parser->new(api_version => 3);
  $p->handler( start => \&start_handler, "tagname,self");
  $p->parse_file(shift || die) || die $!;
  print "\n";  