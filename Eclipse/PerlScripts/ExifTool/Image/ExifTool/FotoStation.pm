#------------------------------------------------------------------------------
# File:         FotoStation.pm
#
# Description:  Read/write FotoWare FotoStation trailer
#
# Revisions:    10/28/2006 - P. Harvey Created
#------------------------------------------------------------------------------

package Image::ExifTool::FotoStation;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.01';

sub ProcessFotoStation($$);

%Image::ExifTool::FotoStation::Main = (
    PROCESS_PROC => \&ProcessFotoStation,
    WRITE_PROC => \&ProcessFotoStation,
    GROUPS => { 2 => 'Image' },
    NOTES => q{
        The following tables define information found in the FotoWare FotoStation
        trailer.
    },
    0x01 => {
        Name => 'IPTC',
        SubDirectory => {
            TagTable => 'Image::ExifTool::IPTC::Main',
        },
    },
    0x02 => {
        Name => 'SoftEdit',
        SubDirectory => {
            TagTable => 'Image::ExifTool::FotoStation::SoftEdit',
        },
    },
    0x03 => {
        Name => 'ThumbnailImage',
        Writable => 1,
        RawConv => '$self->ValidateImage(\$val,$tag)',
    },
    0x04 => {
        Name => 'PreviewImage',
        Writable => 1,
        RawConv => '$self->ValidateImage(\$val,$tag)',
    },
);

# crop coordinate conversions
my %cropConv = (
    ValueConv => '$val / 1000',
    ValueConvInv => '$val * 1000',
    PrintConv => '"$val%"',
    PrintConvInv => '$val=~tr/ %//d; $val',
);

# soft crop record
%Image::ExifTool::FotoStation::SoftEdit = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    WRITE_PROC => \&Image::ExifTool::WriteBinaryData,
    CHECK_PROC => \&Image::ExifTool::CheckBinaryData,
    WRITABLE => 1,
    FORMAT => 'int32s',
    FIRST_ENTRY => 0,
    GROUPS => { 2 => 'Image' },
    0 => {
        Name => 'OriginalImageWidth',
    },
    1 => 'OriginalImageHeight',
    2 => 'ColorPlanes',
    3 => {
        Name => 'XYResolution',
        ValueConv => '$val / 1000',
        ValueConvInv => '$val * 1000',
    },
    4 => {
        Name => 'Rotation',
        Notes => q{
            rotations are stored as degrees CCW * 100, but converted to degrees CW by
            ExifTool
        },
        # raw value is 0, 9000, 18000 or 27000
        ValueConv => '$val ? 360 - $val / 100 : 0',
        ValueConvInv => '$val ? (360 - $val) * 100 : 0',
    },
    # 5 Validity Check (0x11222211)
    6 => {
        Name => 'CropLeft',
        %cropConv,
    },
    7 => {
        Name => 'CropTop',
        %cropConv,
    },
    8 => {
        Name => 'CropRight',
        %cropConv,
    },
    9 => {
        Name => 'CropBottom',
        %cropConv,
    },
    11 => {
        Name => 'CropRotation',
        # raw value in the range -4500 to 4500
        ValueConv => '-$val / 100',
        ValueConvInv => '-$val * 100',
    },
);

#------------------------------------------------------------------------------
# Read/write FotoStation information in a file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this file didn't contain FotoStation information
# - updates DataPos to point to start of FotoStation information
# - updates DirLen to trailer length
sub ProcessFotoStation($$)
{
    my ($exifTool, $dirInfo) = @_;
    $exifTool or return 1;    # allow dummy access to autoload this package
    my ($buff, $footer, $dirBuff, $tagTablePtr);
    my $raf = $$dirInfo{RAF};
    my $outfile = $$dirInfo{OutFile};
    my $offset = $$dirInfo{Offset} || 0;
    my $verbose = $exifTool->Options('Verbose');
    my $out = $exifTool->Options('TextOut');
    my $rtnVal = 0;

    $$dirInfo{DirLen} = 0;      # initialize returned trailer length
    $raf->Seek(-$offset, 2);    # seek to specified offset from end of file

    # loop through FotoStation records
    for (;;) {
        # look for trailer signature
        last unless $raf->Seek(-10, 1) and $raf->Read($footer, 10) == 10;
        my ($tag, $size, $sig) = unpack('nNN', $footer);
        last unless $sig == 0xa1b2c3d4 and $size >= 10 and $raf->Seek(-$size, 1);
        $size -= 10; # size of data only
        last unless $raf->Read($buff, $size) == $size;
        $raf->Seek(-$size, 1);
        # set variables returned in dirInfo hash
        $$dirInfo{DataPos} = $raf->Tell();
        $$dirInfo{DirLen} += $size + 10;

        unless ($tagTablePtr) {
            $tagTablePtr = GetTagTable('Image::ExifTool::FotoStation::Main');
            SetByteOrder('MM'); # necessary for the binary data
            $rtnVal = 1;        # we found a valid FotoStation trailer
        }
        unless ($outfile) {
            # print verbose trailer information
            if ($verbose or $exifTool->{HTML_DUMP}) {
                $exifTool->DumpTrailer({
                    RAF => $raf,
                    DataPos => $$dirInfo{DataPos},
                    DirLen => $size + 10,
                    DirName => "FotoStation_$tag",
                });
            }
            # extract information for this tag
            $exifTool->HandleTag($tagTablePtr, $tag, $buff,
                                 DataPt => \$buff,
                                 Start => 0,
                                 Size => $size,
                                 DataPos => $$dirInfo{DataPos});
            next;
        }
        if ($exifTool->{DEL_GROUP}->{FotoStation}) {
            $verbose and printf $out "  Deleting FotoStation trailer\n";
            $verbose = 0;   # no more verbose messages after this
            ++$exifTool->{CHANGED};
            next;
        }
        # rewrite this information
        my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tag);
        if ($tagInfo) {
            my $newVal;
            my $tagName = $$tagInfo{Name};
            if ($$tagInfo{SubDirectory}) {
                my %subdirInfo = (
                    DataPt => \$buff,
                    DirStart => 0,
                    DirLen => $size,
                    DataPos => $$dirInfo{DataPos},
                    DirName => $tagName,
                    Parent => 'FotoStation',
                );
                my $subTable = GetTagTable($tagInfo->{SubDirectory}->{TagTable});
                $newVal = $exifTool->WriteDirectory(\%subdirInfo, $subTable);
            } else {
                my $nvHash = $exifTool->GetNewValueHash($tagInfo);
                if ($exifTool->IsOverwriting($nvHash) > 0) {
                    $newVal = $exifTool->GetNewValues($nvHash);
                    $newVal = '' unless defined $newVal;
                    if ($verbose > 1) {
                        my $n = length $newVal;
                        print $out "    - FotoStation:$tagName ($size bytes)\n" if $size;
                        print $out "    + FotoStation:$tagName ($n bytes)\n" if $n;
                    }
                    ++$exifTool->{CHANGED};
                }
            }
            if (defined $newVal) {
                # note: length may be 0 here, but we write the empty record anyway
                $buff = $newVal;
                $size = length($newVal) + 10;
                $footer = pack('nNN', $tag, $size, $sig);
            }
        }
        if (defined $dirBuff) {
            # maintain original record order
            $dirBuff = $buff . $footer . $dirBuff;
        } else {
            $dirBuff = $buff . $footer;
        }
    }
    # write the modified FotoStation trailer
    Write($outfile, $dirBuff) or $rtnVal = -1 if $dirBuff;
    return $rtnVal;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::FotoStation - Read/write FotoWare FotoStation trailer

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to read and
write information from the FotoWare FotoStation trailer.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 ACKNOWLEDGEMENTS

Thanks to Mark Tate for information about the FotoStation data format.

=head1 SEE ALSO

L<Image::ExifTool::TagNames/FotoStation Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

