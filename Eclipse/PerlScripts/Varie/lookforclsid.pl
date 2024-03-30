#! d:\perl\bin\perl

$pat = "xxxxxx";
$min = 1;

open( fh, "<D:/temp/srm.mdl") || die "non posso aprire il file input: $!";
#     	"{{90591B0C-24F7-11CF-920A-00AA00A1EB95},00000024}")

while (<fh>) {
    @clsid = ();

    if (/(\{\{[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}\}\,[0-9A-F]{8}\})/)
    {
	if (!defined( $HClsid{ $1}))
	{
#	    printf( "Adding clsid: %s\n", $1);
	    $HClsid{ $1} = 1;
	}
	else {
#	    printf("incrementing clsid: %s\n", $1);
	    $HClsid{ $1} = $HClsid{ $1} + 1;
	    if ( $HClsid{ $1} >= $min) {
		printf( "!!!! found %d occurrences of clsid: %s\n",
			$HClsid{ $1}, $1);
	    }
	}
    }
}
