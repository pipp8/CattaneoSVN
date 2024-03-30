#! perl

# Extract all plain text from an HTML file

use strict;
use HTML::Parser 3.00 ();

my %inside;

sub tag
{
   my($tag, $num) = @_;
   $inside{$tag} += $num;
   
   print "tag: $tag \n";  # not for all tags
}

sub text
{
#    return if $inside{script} || $inside{style};
    print $_[0];
}

print "starting\n";

HTML::Parser->new(api_version => 3,
		  handlers    => [start => [\&tag, "tagname, '+1'"],
				  			end => [\&tag, "tagname, '-1'"],
				  		   text => [\&text, "dtext"],
				 ],
		  marked_sections => 1,
	)->parse_file(shift) || die "Can't open file: $!\n";;
