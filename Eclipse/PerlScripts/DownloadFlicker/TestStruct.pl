#! /usr/bin/perl

use Class::Struct;

struct( UserObj => {
	cnt => '$', user => '$',
	linkArray => '@'
});

$obj = UserObj->new;

$obj->cnt( 1);
$a = $obj->cnt + 1;
$obj->cnt($a);
$obj->linkArray(0, 'primo');
$ref_array = $obj->linkArray;

push(@$ref_array, "secondo");

print $obj->cnt, ", ", $obj->linkArray(1), "\n";

@a = ( 1, 2, 3);

$ref = \@a;

push( @$ref, 4);

print $$ref[3], "\n";
