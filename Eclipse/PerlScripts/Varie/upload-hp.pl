#!/usr/bin/perl

use Expect;
use IO::Tty;

$MORE = "-- MORE --, next page: Space, next line: Enter, quit: Control-C";
$EOF  = "-- End of configuration file --";

# $DEBUG = 1;

($hostname,$username,$password) = @ARGV;

if ( ! length ($hostname) ) {
  printf ("usage: upload-hp hostname [login [passwd]]\n");
  exit;
}

$username = length ($username) ? $usernane : "admin";
$password = length ($password) ? $password : "admin";

$| = 0;
$ENV{'TERM'} = "xterm";

#&show_config (); exit;
open (config, "-|") || &show_config ();
#open (config, "smaz.mne");

# Konfigurace prepinace:

$begin = $end = 0;
$name  = "unknown";
$image = "unknown";

while (<config>) {
#  print if $DEBUG;
  s/[\n\r]//g;
  $begin = 1 if /^Startup configuration:/;
  last if $begin;
}

$_ = <config>;

while (<config>) {
  s/[\r\n]//g;
  s/\33\[[0-9]+\;[0-9]+[a-zA-Z]//g;
  s/\33\[[0-9][A-Z]//g;
#  s/\33\[\?[0-9]+[a-zA-Z]//g;
  s/\33E//g;
  printf ("LINE: \"%s\"\n", $_) if $DEBUG;
  s/$MORE//;
  $end = 1 if /^Press any key when done.../;
  $end = 1 if /$EOF/;
  printf ("BYLO END %d!!!\n", $end) if $DEBUG;
  last if $end;
  if ( length == 0 ) {
	$n++;
	$end = 1 if $n > 1;
	last if $end;
  } else {
	$n = 0;
  }
  if ( length == 80 ) {
	$last = $_;
  } else {
	$oline = $last . $_;
	$last = "";
	# preskoceni pravedil acces listu 20000 az 30000
	if (! ($oline =~ / + (\d+) (deny|permit) ip [\d\.]{7,15} [\d\.]{7,15} [\d\.]{7,15} [\d\.]{7,15}/ && $1 > 20000 && $1 < 50000)) {

	  	printf ("%s\n", $oline);
	}
  }
}

printf ("; End of configuration file for %s\n", $hostname) if $begin and $end;
while (<config>) {
  print if $DEBUG;
}

sub show_config () {
  $exp = new Expect;

#  $exp->raw_pty(1);
#  $exp->log_stdout(10);

  $exp->spawn("ssh $username\@$hostname") or die "Cannot spawn $command: $!\n";
  $exp->expect(30, [ qr/login:/i,      sub { $exp->send ("$username\n"); exp_continue; } ],
                   [ qr/name:/i,       sub { $exp->send ("$username\n"); exp_continue; } ],
                   [ qr/password:/i,   sub { $exp->send ("$password\n"); exp_continue; } ],
                   [ qr/to continue/i, sub { sleep(1); $exp->send(" ");  exp_continue; } ],
                   [ qr/\> /i,         sub { $exp->send("enable\n");     exp_continue; } ],
                   [ qr/# /i,          sub { $ok = 1; } ]);

  if ( $ok ) {
#   $exp->send ("print \"show config\"\n");
    $exp->send ("terminal length 1000\n");
    $exp->expect ( 3, [ qr/# /i,				sub { $exp->send("show config\n"); } ] );
    $exp->expect (20, [ qr/Press any key when done.../i,	sub { sleep(1); $exp->send(" "); exp_continue; }  ],
    		      [ qr/$MORE/i,				sub { sleep(1); $exp->send(" "); exp_continue; }  ],
		      [ qr/# /i,				sub { printf ("%s\n", $EOF); $exp->send("\n logout\n"); } ]  );

    $exp->expect ( 5, [ qr/log out \[y\/n\]\?/i, 		sub { $exp->send ("y"); exp_continue; } ],
    		      [ qr/configuration \[y\/n\/\^C\]?/i,	sub { $exp->send ("n"); exp_continue; } ],
                      [ qr/connection closed/i,			sub { $ok = 2; } ]);
  }
  $exp->soft_close();
  exit;
}

