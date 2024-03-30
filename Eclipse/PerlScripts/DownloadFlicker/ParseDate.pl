  #!/usr/bin/perl

  # turn on perl's safety features
  # use strict;
  use warnings;
  use Date::Parse;
  use Date::Language;
  
  
  @a = ('0' .. '9');
  
  $b = join( ' ', @a[2..$#a]);
  
  $lang = Date::Language->new('Italian');

  # get what was specified on the command line
  my $time = join ' ', @ARGV;

  # convert that to seconds
  my $secs = $lang->str2time($time, "CEST");

  # check we got something useful back
  unless (defined $secs)
   { die "Can't parse '$time' as a time or date\n" }

  # print out the result
  print "'$time' parses as:\n",
        "'$secs' (".gmtime($secs).")\n";
        