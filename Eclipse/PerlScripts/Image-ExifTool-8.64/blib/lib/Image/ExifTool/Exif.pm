#------------------------------------------------------------------------------
# File:         Exif.pm
#
# Description:  Read EXIF/TIFF meta information
#
# Revisions:    11/25/2003 - P. Harvey Created
#               02/06/2004 - P. Harvey Moved processing functions from ExifTool
#               03/19/2004 - P. Harvey Check PreviewImage for validity
#               11/11/2004 - P. Harvey Split off maker notes into MakerNotes.pm
#               12/13/2004 - P. Harvey Added AUTOLOAD to load write routines
#
# References:   0) http://www.exif.org/Exif2-2.PDF
#               1) http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf
#               2) http://www.adobe.com/products/dng/pdfs/dng_spec_1_3_0_0.pdf
#               3) http://www.awaresystems.be/imaging/tiff/tifftags.html
#               4) http://www.remotesensing.org/libtiff/TIFFTechNote2.html
#               5) http://www.exif.org/dcf.PDF
#               6) http://park2.wakwak.com/~tsuruzoh/Computer/Digicams/exif-e.html
#               7) http://www.fine-view.com/jp/lab/doc/ps6ffspecsv2.pdf
#               8) http://www.ozhiker.com/electronics/pjmt/jpeg_info/meta.html
#               9) http://hul.harvard.edu/jhove/tiff-tags.html
#              10) http://partners.adobe.com/public/developer/en/tiff/TIFFPM6.pdf
#              11) Robert Mucke private communication
#              12) http://www.broomscloset.com/closet/photo/exif/TAG2000-22_DIS12234-2.PDF
#              13) http://www.microsoft.com/whdc/xps/wmphoto.mspx
#              14) http://www.asmail.be/msg0054681802.html
#              15) http://crousseau.free.fr/imgfmt_raw.htm
#              16) http://www.cybercom.net/~dcoffin/dcraw/
#              17) http://www.digitalpreservation.gov/formats/content/tiff_tags.shtml
#              18) http://www.asmail.be/msg0055568584.html
#              19) http://libpsd.graphest.com/files/Photoshop%20File%20Formats.pdf
#              20) http://tiki-lounge.com/~raf/tiff/fields.html
#              21) http://community.roxen.com/developers/idocs/rfc/rfc3949.html
#              22) http://tools.ietf.org/html/draft-ietf-fax-tiff-fx-extension1-01
#              23) MetaMorph Stack (STK) Image File Format:
#                  --> ftp://ftp.meta.moleculardevices.com/support/stack/STK.doc
#              24) http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-008-2010_E.pdf (Exif 2.3)
#              25) Vesa Kivisto private communication (7D)
#              26) Jeremy Brown private communication
#              JD) Jens Duttke private communication
#------------------------------------------------------------------------------

package Image::ExifTool::Exif;

use strict;
use vars qw($VERSION $AUTOLOAD @formatSize @formatName %formatNumber %intFormat
            %lightSource %flash %compression %photometricInterpretation %orientation
            %subfileType);
use Image::ExifTool qw(:DataAccess :Utils);
use Image::ExifTool::MakerNotes;

$VERSION = '3.25';

sub ProcessExif($$$);
sub WriteExif($$$);
sub CheckExif($$$);
sub RebuildMakerNotes($$$);
sub EncodeExifText($$);
sub ValidateIFD($;$);
sub ProcessTiffIFD($$$);
sub PrintParameter($$$);
sub GetOffList($$$$$);
sub PrintLensInfo($);
sub ConvertLensInfo($);

# size limit for loading binary data block into memory
sub BINARY_DATA_LIMIT { return 10 * 1024 * 1024; }

# byte sizes for the various EXIF format types below
@formatSize = (undef,1,1,2,4,8,1,1,2,4,8,4,8,4,2,8,8,8,8);

@formatName = (
    undef, 'int8u', 'string', 'int16u',
    'int32u', 'rational64u', 'int8s', 'undef',
    'int16s', 'int32s', 'rational64s', 'float',
    'double', 'ifd', 'unicode', 'complex',
    'int64u', 'int64s', 'ifd64', # (new BigTIFF formats)
);

# hash to look up EXIF format numbers by name
# (format types are all lower case)
%formatNumber = (
    'int8u'       => 1,  # BYTE
    'string'      => 2,  # ASCII
    'int16u'      => 3,  # SHORT
    'int32u'      => 4,  # LONG
    'rational64u' => 5,  # RATIONAL
    'int8s'       => 6,  # SBYTE
    'undef'       => 7,  # UNDEFINED
    'binary'      => 7,  # (treat binary data as undef)
    'int16s'      => 8,  # SSHORT
    'int32s'      => 9,  # SLONG
    'rational64s' => 10, # SRATIONAL
    'float'       => 11, # FLOAT
    'double'      => 12, # DOUBLE
    'ifd'         => 13, # IFD (with int32u format)
    'unicode'     => 14, # UNICODE [see Note below]
    'complex'     => 15, # COMPLEX [see Note below]
    'int64u'      => 16, # LONG8 [BigTIFF]
    'int64s'      => 17, # SLONG8 [BigTIFF]
    'ifd64'       => 18, # IFD8 (with int64u format) [BigTIFF]
    # Note: unicode and complex types are not yet properly supported by ExifTool.
    # These are types which have been observed in the Adobe DNG SDK code, but
    # aren't fully supported there either.  We know the sizes, but that's about it.
    # We don't know if the unicode is null terminated, or the format for complex
    # (although I suspect it would be two 4-byte floats, real and imaginary).
);

# lookup for integer format strings
%intFormat = (
    'int8u'  => 1,
    'int16u' => 3,
    'int32u' => 4,
    'int8s'  => 6,
    'int16s' => 8,
    'int32s' => 9,
    'ifd'    => 13,
    'int64u' => 16,
    'int64s' => 17,
    'ifd64'  => 18,
);

# EXIF LightSource PrintConv values
%lightSource = (
    0 => 'Unknown',
    1 => 'Daylight',
    2 => 'Fluorescent',
    3 => 'Tungsten (Incandescent)',
    4 => 'Flash',
    9 => 'Fine Weather',
    10 => 'Cloudy',
    11 => 'Shade',
    12 => 'Daylight Fluorescent',   # (D 5700 - 7100K)
    13 => 'Day White Fluorescent',  # (N 4600 - 5500K)
    14 => 'Cool White Fluorescent', # (W 3800 - 4500K)
    15 => 'White Fluorescent',      # (WW 3250 - 3800K)
    16 => 'Warm White Fluorescent', # (L 2600 - 3250K)
    17 => 'Standard Light A',
    18 => 'Standard Light B',
    19 => 'Standard Light C',
    20 => 'D55',
    21 => 'D65',
    22 => 'D75',
    23 => 'D50',
    24 => 'ISO Studio Tungsten',
    255 => 'Other',
);

# EXIF Flash values
%flash = (
    OTHER => sub {
        # translate "Off" and "On" when writing
        my ($val, $inv) = @_;
        return undef unless $inv and $val =~ /^(off|on)$/i;
        return lc $val eq 'off' ? 0x00 : 0x01;
    },
    0x00 => 'No Flash',
    0x01 => 'Fired',
    0x05 => 'Fired, Return not detected',
    0x07 => 'Fired, Return detected',
    0x08 => 'On, Did not fire', # not charged up?
    0x09 => 'On, Fired',
    0x0d => 'On, Return not detected',
    0x0f => 'On, Return detected',
    0x10 => 'Off, Did not fire',
    0x14 => 'Off, Did not fire, Return not detected',
    0x18 => 'Auto, Did not fire',
    0x19 => 'Auto, Fired',
    0x1d => 'Auto, Fired, Return not detected',
    0x1f => 'Auto, Fired, Return detected',
    0x20 => 'No flash function',
    0x30 => 'Off, No flash function',
    0x41 => 'Fired, Red-eye reduction',
    0x45 => 'Fired, Red-eye reduction, Return not detected',
    0x47 => 'Fired, Red-eye reduction, Return detected',
    0x49 => 'On, Red-eye reduction',
    0x4d => 'On, Red-eye reduction, Return not detected',
    0x4f => 'On, Red-eye reduction, Return detected',
    0x50 => 'Off, Red-eye reduction',
    0x58 => 'Auto, Did not fire, Red-eye reduction',
    0x59 => 'Auto, Fired, Red-eye reduction',
    0x5d => 'Auto, Fired, Red-eye reduction, Return not detected',
    0x5f => 'Auto, Fired, Red-eye reduction, Return detected',
);

# TIFF Compression values
# (values with format "Xxxxx XXX Compressed" are used to identify RAW file types)
%compression = (
    1 => 'Uncompressed',
    2 => 'CCITT 1D',
    3 => 'T4/Group 3 Fax',
    4 => 'T6/Group 4 Fax',
    5 => 'LZW',
    6 => 'JPEG (old-style)', #3
    7 => 'JPEG', #4
    8 => 'Adobe Deflate', #3
    9 => 'JBIG B&W', #3
    10 => 'JBIG Color', #3
    99 => 'JPEG', #16
    262 => 'Kodak 262', #16
    32766 => 'Next', #3
    32767 => 'Sony ARW Compressed', #16
    32769 => 'Packed RAW', #PH (used by Epson, Nikon, Samsung)
    32770 => 'Samsung SRW Compressed', #PH
    32771 => 'CCIRLEW', #3
    32773 => 'PackBits',
    32809 => 'Thunderscan', #3
    32867 => 'Kodak KDC Compressed', #PH
    32895 => 'IT8CTPAD', #3
    32896 => 'IT8LW', #3
    32897 => 'IT8MP', #3
    32898 => 'IT8BL', #3
    32908 => 'PixarFilm', #3
    32909 => 'PixarLog', #3
    32946 => 'Deflate', #3
    32947 => 'DCS', #3
    34661 => 'JBIG', #3
    34676 => 'SGILog', #3
    34677 => 'SGILog24', #3
    34712 => 'JPEG 2000', #3
    34713 => 'Nikon NEF Compressed', #PH
    34715 => 'JBIG2 TIFF FX', #20
    34718 => 'Microsoft Document Imaging (MDI) Binary Level Codec', #18
    34719 => 'Microsoft Document Imaging (MDI) Progressive Transform Codec', #18
    34720 => 'Microsoft Document Imaging (MDI) Vector', #18
    65000 => 'Kodak DCR Compressed', #PH
    65535 => 'Pentax PEF Compressed', #Jens
);

%photometricInterpretation = (
    0 => 'WhiteIsZero',
    1 => 'BlackIsZero',
    2 => 'RGB',
    3 => 'RGB Palette',
    4 => 'Transparency Mask',
    5 => 'CMYK',
    6 => 'YCbCr',
    8 => 'CIELab',
    9 => 'ICCLab', #3
    10 => 'ITULab', #3
    32803 => 'Color Filter Array', #2
    32844 => 'Pixar LogL', #3
    32845 => 'Pixar LogLuv', #3
    34892 => 'Linear Raw', #2
);

%orientation = (
    1 => 'Horizontal (normal)',
    2 => 'Mirror horizontal',
    3 => 'Rotate 180',
    4 => 'Mirror vertical',
    5 => 'Mirror horizontal and rotate 270 CW',
    6 => 'Rotate 90 CW',
    7 => 'Mirror horizontal and rotate 90 CW',
    8 => 'Rotate 270 CW',
);

%subfileType = (
    0 => 'Full-resolution Image',
    1 => 'Reduced-resolution image',
    2 => 'Single page of multi-page image',
    3 => 'Single page of multi-page reduced-resolution image',
    4 => 'Transparency mask',
    5 => 'Transparency mask of reduced-resolution image',
    6 => 'Transparency mask of multi-page image',
    7 => 'Transparency mask of reduced-resolution multi-page image',
    0xffffffff => 'invalid', #(found in E5700 NEF's)
    BITMASK => {
        0 => 'Reduced resolution',
        1 => 'Single page',
        2 => 'Transparency mask',
        3 => 'TIFF/IT final page', #20
        4 => 'TIFF-FX mixed raster content', #20
    },
);

# PrintConv for parameter tags
%Image::ExifTool::Exif::printParameter = (
    PrintConv => {
        0 => 'Normal',
        OTHER => \&Image::ExifTool::Exif::PrintParameter,
    },
);

# ValueConv that makes long values binary type
my %longBin = (
    ValueConv => 'length($val) > 64 ? \$val : $val',
    ValueConvInv => '$val',
);

# PrintConv for SampleFormat (0x153)
my %sampleFormat = (
    1 => 'Unsigned',        # unsigned integer
    2 => 'Signed',          # two's complement signed integer
    3 => 'Float',           # IEEE floating point
    4 => 'Undefined',
    5 => 'Complex int',     # complex integer (ref 3)
    6 => 'Complex float',   # complex IEEE floating point (ref 3)
);

# main EXIF tag table
%Image::ExifTool::Exif::Main = (
    GROUPS => { 0 => 'EXIF', 1 => 'IFD0', 2 => 'Image'},
    WRITE_PROC => \&WriteExif,
    WRITE_GROUP => 'ExifIFD',   # default write group
    SET_GROUP1 => 1, # set group1 name to directory name for all tags in table
    0x1 => {
        Name => 'InteropIndex',
        Description => 'Interoperability Index',
        PrintConv => {
            R98 => 'R98 - DCF basic file (sRGB)',
            R03 => 'R03 - DCF option file (Adobe RGB)',
            THM => 'THM - DCF thumbnail file',
        },
    },
    0x2 => { #5
        Name => 'InteropVersion',
        Description => 'Interoperability Version',
        RawConv => '$val=~s/\0+$//; $val',  # (some idiots add null terminators)
    },
    0x0b => { #PH
        Name => 'ProcessingSoftware',
        Notes => 'used by ACD Systems Digital Imaging',
    },
    0xfe => {
        Name => 'SubfileType',
        # set priority directory if this is the full resolution image
        DataMember => 'SubfileType',
        RawConv => '$self->SetPriorityDir() if $val eq "0"; $$self{SubfileType} = $val',
        PrintConv => \%subfileType,
    },
    0xff => {
        Name => 'OldSubfileType',
        # set priority directory if this is the full resolution image
        RawConv => '$self->SetPriorityDir() if $val eq "1"; $val',
        PrintConv => {
            1 => 'Full-resolution image',
            2 => 'Reduced-resolution image',
            3 => 'Single page of multi-page image',
        },
    },
    0x100 => {
        Name => 'ImageWidth',
        # even though Group 1 is set dynamically we need to register IFD1 once
        # so it will show up in the group lists
        Groups => { 1 => 'IFD1' },
        # Note: priority 0 tags automatically have their priority increased for the
        # priority direcory (the directory with a SubfileType of "Full-resolution image")
        Priority => 0,
    },
    0x101 => {
        Name => 'ImageHeight',
        Notes => 'called ImageLength by the EXIF spec.',
        Priority => 0,
    },
    0x102 => {
        Name => 'BitsPerSample',
        Priority => 0,
    },
    0x103 => {
        Name => 'Compression',
        DataMember => 'Compression',
        SeparateTable => 'Compression',
        RawConv => q{
            Image::ExifTool::Exif::IdentifyRawFile($self, $val);
            return $$self{Compression} = $val;
        },
        PrintConv => \%compression,
        Priority => 0,
    },
    0x106 => {
        Name => 'PhotometricInterpretation',
        PrintConv => \%photometricInterpretation,
        Priority => 0,
    },
    0x107 => {
        Name => 'Thresholding',
        PrintConv => {
            1 => 'No dithering or halftoning',
            2 => 'Ordered dither or halftone',
            3 => 'Randomized dither',
        },
    },
    0x108 => 'CellWidth',
    0x109 => 'CellLength',
    0x10a => {
        Name => 'FillOrder',
        PrintConv => {
            1 => 'Normal',
            2 => 'Reversed',
        },
    },
    0x10d => 'DocumentName',
    0x10e => {
        Name => 'ImageDescription',
        Priority => 0,
    },
    0x10f => {
        Name => 'Make',
        Groups => { 2 => 'Camera' },
        DataMember => 'Make',
        # remove trailing blanks and save as an ExifTool member variable
        RawConv => '$val =~ s/\s+$//; $$self{Make} = $val',
        # NOTE: trailing "blanks" (spaces) are removed from all EXIF tags which
        # may be "unknown" (filled with spaces) according to the EXIF spec.
        # This allows conditional replacement with "exiftool -TAG-= -TAG=VALUE".
        # - also removed are any other trailing whitespace characters
    },
    0x110 => {
        Name => 'Model',
        Description => 'Camera Model Name',
        Groups => { 2 => 'Camera' },
        DataMember => 'Model',
        # remove trailing blanks and save as an ExifTool member variable
        RawConv => '$val =~ s/\s+$//; $$self{Model} = $val',
    },
    0x111 => [
        {
            Condition => q[
                $$self{TIFF_TYPE} eq 'MRW' and $$self{DIR_NAME} eq 'IFD0' and
                $$self{Model} =~ /^DiMAGE A200/
            ],
            Name => 'StripOffsets',
            IsOffset => 1,
            OffsetPair => 0x117,  # point to associated byte counts
            # A200 stores this information in the wrong byte order!!
            ValueConv => '$val=join(" ",unpack("N*",pack("V*",split(" ",$val))));\$val',
            ByteOrder => 'LittleEndian',
        },
        {
            Condition => q[
                ($$self{TIFF_TYPE} ne 'CR2' or $$self{DIR_NAME} ne 'IFD0') and
                ($$self{TIFF_TYPE} ne 'DNG' or $$self{DIR_NAME} !~ /^SubIFD[12]$/)
            ],
            Name => 'StripOffsets',
            IsOffset => 1,
            OffsetPair => 0x117,  # point to associated byte counts
            ValueConv => 'length($val) > 32 ? \$val : $val',
        },
        {
            Condition => '$$self{DIR_NAME} eq "IFD0"',
            Name => 'PreviewImageStart',
            IsOffset => 1,
            OffsetPair => 0x117,
            Notes => q{
                PreviewImageStart in IFD0 of CR2 images and SubIFD1 of DNG images, and
                JpgFromRawStart in SubIFD2 of DNG images
            },
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{TIFF_TYPE} eq "CR2"',
            Protected => 2,
        },
        {
            Condition => '$$self{DIR_NAME} eq "SubIFD1"',
            Name => 'PreviewImageStart',
            IsOffset => 1,
            OffsetPair => 0x117,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'SubIFD1',
            WriteCondition => '$$self{TIFF_TYPE} eq "DNG"',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawStart',
            IsOffset => 1,
            OffsetPair => 0x117,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'SubIFD2',
            WriteCondition => '$$self{TIFF_TYPE} eq "DNG"',
            Protected => 2,
        },
    ],
    0x112 => {
        Name => 'Orientation',
        PrintConv => \%orientation,
        Priority => 0,  # so PRIORITY_DIR takes precedence
    },
    0x115 => {
        Name => 'SamplesPerPixel',
        Priority => 0,
    },
    0x116 => {
        Name => 'RowsPerStrip',
        Priority => 0,
    },
    0x117 => [
        {
            Condition => q[
                $$self{TIFF_TYPE} eq 'MRW' and $$self{DIR_NAME} eq 'IFD0' and
                $$self{Model} =~ /^DiMAGE A200/
            ],
            Name => 'StripByteCounts',
            OffsetPair => 0x111,   # point to associated offset
            # A200 stores this information in the wrong byte order!!
            ValueConv => '$val=join(" ",unpack("N*",pack("V*",split(" ",$val))));\$val',
            ByteOrder => 'LittleEndian',
        },
        {
            Condition => q[
                ($$self{TIFF_TYPE} ne 'CR2' or $$self{DIR_NAME} ne 'IFD0') and
                ($$self{TIFF_TYPE} ne 'DNG' or $$self{DIR_NAME} !~ /^SubIFD[12]$/)
            ],
            Name => 'StripByteCounts',
            OffsetPair => 0x111,   # point to associated offset
            ValueConv => 'length($val) > 32 ? \$val : $val',
        },
        {
            Condition => '$$self{DIR_NAME} eq "IFD0"',
            Name => 'PreviewImageLength',
            OffsetPair => 0x111,
            Notes => q{
                PreviewImageLength in IFD0 of CR2 images and SubIFD1 of DNG images, and
                JpgFromRawLength in SubIFD2 of DNG images
            },
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{TIFF_TYPE} eq "CR2"',
            Protected => 2,
        },
        {
            Condition => '$$self{DIR_NAME} eq "SubIFD1"',
            Name => 'PreviewImageLength',
            OffsetPair => 0x111,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'SubIFD1',
            WriteCondition => '$$self{TIFF_TYPE} eq "DNG"',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawLength',
            OffsetPair => 0x111,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'SubIFD2',
            WriteCondition => '$$self{TIFF_TYPE} eq "DNG"',
            Protected => 2,
        },
    ],
    0x118 => 'MinSampleValue',
    0x119 => 'MaxSampleValue',
    0x11a => {
        Name => 'XResolution',
        Priority => 0,  # so PRIORITY_DIR takes precedence
    },
    0x11b => {
        Name => 'YResolution',
        Priority => 0,
    },
    0x11c => {
        Name => 'PlanarConfiguration',
        PrintConv => {
            1 => 'Chunky',
            2 => 'Planar',
        },
        Priority => 0,
    },
    0x11d => 'PageName',
    0x11e => 'XPosition',
    0x11f => 'YPosition',
    0x120 => {
        Name => 'FreeOffsets',
        IsOffset => 1,
        OffsetPair => 0x121,
        ValueConv => 'length($val) > 32 ? \$val : $val',
    },
    0x121 => {
        Name => 'FreeByteCounts',
        OffsetPair => 0x120,
        ValueConv => 'length($val) > 32 ? \$val : $val',
    },
    0x122 => {
        Name => 'GrayResponseUnit',
        PrintConv => { #3
            1 => 0.1,
            2 => 0.001,
            3 => 0.0001,
            4 => 0.00001,
            5 => 0.000001,
        },
    },
    0x123 => {
        Name => 'GrayResponseCurve',
        Binary => 1,
    },
    0x124 => {
        Name => 'T4Options',
        PrintConv => { BITMASK => {
            0 => '2-Dimensional encoding',
            1 => 'Uncompressed',
            2 => 'Fill bits added',
        } }, #3
    },
    0x125 => {
        Name => 'T6Options',
        PrintConv => { BITMASK => {
            1 => 'Uncompressed',
        } }, #3
    },
    0x128 => {
        Name => 'ResolutionUnit',
        Notes => 'the value 1 is not standard EXIF',
        PrintConv => {
            1 => 'None',
            2 => 'inches',
            3 => 'cm',
        },
        Priority => 0,
    },
    0x129 => 'PageNumber',
    0x12c => 'ColorResponseUnit', #9
    0x12d => {
        Name => 'TransferFunction',
        Binary => 1,
    },
    0x131 => {
        Name => 'Software',
        RawConv => '$val =~ s/\s+$//; $val', # trim trailing blanks
    },
    0x132 => {
        Name => 'ModifyDate',
        Groups => { 2 => 'Time' },
        Notes => 'called DateTime by the EXIF spec.',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    0x13b => {
        Name => 'Artist',
        Groups => { 2 => 'Author' },
        Notes => 'becomes a list-type tag when the MWG module is loaded',
        RawConv => '$val =~ s/\s+$//; $val', # trim trailing blanks
    },
    0x13c => 'HostComputer',
    0x13d => {
        Name => 'Predictor',
        PrintConv => {
            1 => 'None',
            2 => 'Horizontal differencing',
        },
    },
    0x13e => {
        Name => 'WhitePoint',
        Groups => { 2 => 'Camera' },
    },
    0x13f => {
        Name => 'PrimaryChromaticities',
        Priority => 0,
    },
    0x140 => {
        Name => 'ColorMap',
        Format => 'binary',
        Binary => 1,
    },
    0x141 => 'HalftoneHints',
    0x142 => 'TileWidth',
    0x143 => 'TileLength',
    0x144 => {
        Name => 'TileOffsets',
        IsOffset => 1,
        OffsetPair => 0x145,
        ValueConv => 'length($val) > 32 ? \$val : $val',
    },
    0x145 => {
        Name => 'TileByteCounts',
        OffsetPair => 0x144,
        ValueConv => 'length($val) > 32 ? \$val : $val',
    },
    0x146 => 'BadFaxLines', #3
    0x147 => { #3
        Name => 'CleanFaxData',
        PrintConv => {
            0 => 'Clean',
            1 => 'Regenerated',
            2 => 'Unclean',
        },
    },
    0x148 => 'ConsecutiveBadFaxLines', #3
    0x14a => [
        {
            Name => 'SubIFD',
            # use this opportunity to identify an ARW image, and if so we
            # must decide if this is a SubIFD or the A100 raw data
            # (use SubfileType, Compression and FILE_TYPE to identify ARW/SR2,
            # then call SetARW to finish the job)
            Condition => q{
                $$self{DIR_NAME} ne 'IFD0' or $$self{FILE_TYPE} ne 'TIFF' or
                $$self{Make} !~ /^SONY/ or
                not $$self{SubfileType} or $$self{SubfileType} != 1 or
                not $$self{Compression} or $$self{Compression} != 6 or
                not require Image::ExifTool::Sony or
                Image::ExifTool::Sony::SetARW($self, $valPt)
            },
            Groups => { 1 => 'SubIFD' },
            Flags => 'SubIFD',
            SubDirectory => {
                Start => '$val',
                MaxSubdirs => 3,
            },
        },
        { #16
            Name => 'A100DataOffset',
            Notes => 'the data offset in original Sony DSLR-A100 ARW images',
            DataMember => 'A100DataOffset',
            RawConv => '$$self{A100DataOffset} = $val',
            IsOffset => 1,
            Protected => 2,
        },
    ],
    0x14c => {
        Name => 'InkSet',
        PrintConv => { #3
            1 => 'CMYK',
            2 => 'Not CMYK',
        },
    },
    0x14d => 'InkNames', #3
    0x14e => 'NumberofInks', #3
    0x150 => 'DotRange',
    0x151 => 'TargetPrinter',
    0x152 => {
        Name => 'ExtraSamples',
        PrintConv => { #20
            0 => 'Unspecified',
            1 => 'Associated Alpha',
            2 => 'Unassociated Alpha',
        },
    },
    0x153 => {
        Name => 'SampleFormat',
        Notes => 'SamplesPerPixel values',
        PrintConvColumns => 2,
        PrintConv => [ \%sampleFormat, \%sampleFormat, \%sampleFormat, \%sampleFormat ],
    },
    0x154 => 'SMinSampleValue',
    0x155 => 'SMaxSampleValue',
    0x156 => 'TransferRange',
    0x157 => 'ClipPath', #3
    0x158 => 'XClipPathUnits', #3
    0x159 => 'YClipPathUnits', #3
    0x15a => { #3
        Name => 'Indexed',
        PrintConv => { 0 => 'Not indexed', 1 => 'Indexed' },
    },
    0x15b => {
        Name => 'JPEGTables',
        Binary => 1,
    },
    0x15f => { #10
        Name => 'OPIProxy',
        PrintConv => {
            0 => 'Higher resolution image does not exist',
            1 => 'Higher resolution image exists',
        },
    },
    # 0x181 => 'Decode', #20 (typo! - should be 0x1b1, ref 21)
    # 0x182 => 'DefaultImageColor', #20 (typo! - should be 0x1b2, ref 21)
    0x190 => { #3
        Name => 'GlobalParametersIFD',
        Groups => { 1 => 'GlobParamIFD' },
        Flags => 'SubIFD',
        SubDirectory => {
            DirName => 'GlobParamIFD',
            Start => '$val',
        },
    },
    0x191 => { #3
        Name => 'ProfileType',
        PrintConv => { 0 => 'Unspecified', 1 => 'Group 3 FAX' },
    },
    0x192 => { #3
        Name => 'FaxProfile',
        PrintConv => {
            0 => 'Unknown',
            1 => 'Minimal B&W lossless, S',
            2 => 'Extended B&W lossless, F',
            3 => 'Lossless JBIG B&W, J',
            4 => 'Lossy color and grayscale, C',
            5 => 'Lossless color and grayscale, L',
            6 => 'Mixed raster content, M',
            7 => 'Profile T', #20
            255 => 'Multi Profiles', #20
        },
    },
    0x193 => { #3
        Name => 'CodingMethods',
        PrintConv => { BITMASK => {
            0 => 'Unspecified compression',
            1 => 'Modified Huffman',
            2 => 'Modified Read',
            3 => 'Modified MR',
            4 => 'JBIG',
            5 => 'Baseline JPEG',
            6 => 'JBIG color',
        } },
    },
    0x194 => 'VersionYear', #3
    0x195 => 'ModeNumber', #3
    0x1b1 => 'Decode', #3
    0x1b2 => 'DefaultImageColor', #3 (changed to ImageBaseColor, ref 21)
    0x1b3 => 'T82Options', #20
    0x1b5 => { #19
        Name => 'JPEGTables',
        Binary => 1,
    },
    0x200 => {
        Name => 'JPEGProc',
        PrintConv => {
            1 => 'Baseline',
            14 => 'Lossless',
        },
    },
    0x201 => [
        {
            Name => 'ThumbnailOffset',
            Notes => q{
                ThumbnailOffset in IFD1 of JPEG and some TIFF-based images, IFD0 of MRW
                images and AVI videos, and the SubIFD in IFD1 of SRW images;
                PreviewImageStart in MakerNotes and IFD0 of ARW and SR2 images;
                JpgFromRawStart in SubIFD of NEF images and IFD2 of PEF images; and
                OtherImageStart in everything else
            },
            # thumbnail is found in IFD1 of JPEG and TIFF images, and
            # IFD0 of EXIF information in FujiFilm AVI (RIFF) videos
            Condition => q{
                $$self{DIR_NAME} eq 'IFD1' or
                ($$self{FILE_TYPE} eq 'RIFF' and $$self{DIR_NAME} eq 'IFD0')
            },
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'IFD1',
            # according to the EXIF spec. a JPEG-compressed thumbnail image may not
            # be stored in a TIFF file, but these TIFF-based RAW image formats
            # use IFD1 for a JPEG-compressed thumbnail:  CR2, ARW, SR2 and PEF.
            # (SRF also stores a JPEG image in IFD1, but it is actually a preview
            # and we don't yet write SRF anyway) 
            WriteCondition => q{
                $$self{FILE_TYPE} ne "TIFF" or
                $$self{TIFF_TYPE} =~ /^(CR2|ARW|SR2|PEF)$/
            },
            Protected => 2,
        },
        {
            Name => 'ThumbnailOffset',
            # thumbnail in IFD0 of MRW images (Minolta A200)
            Condition => '$$self{DIR_NAME} eq "IFD0" and $$self{TIFF_TYPE} eq "MRW"',
            IsOffset => 1,
            OffsetPair => 0x202,
            # A200 uses the wrong base offset for this pointer!!
            WrongBase => '$$self{Model} =~ /^DiMAGE A200/ ? $$self{MRW_WrongBase} : undef',
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{FILE_TYPE} eq "MRW"',
            Protected => 2,
        },
        {
            Name => 'ThumbnailOffset',
            # in SubIFD of IFD1 in Samsung SRW images
            Condition => q{
                $$self{TIFF_TYPE} eq 'SRW' and $$self{DIR_NAME} eq 'SubIFD' and
                $$self{PATH}[-2] eq 'IFD1'
            },
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'SubIFD',
            WriteCondition => '$$self{TIFF_TYPE} eq "SRW"',
            Protected => 2,
        },
        {
            Name => 'PreviewImageStart',
            Condition => '$$self{DIR_NAME} eq "MakerNotes"',
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'MakerNotes',
            # (no WriteCondition necessary because MakerNotes won't be created)
            Protected => 2,
        },
        {
            Name => 'PreviewImageStart',
            # PreviewImage in IFD0 of ARW and SR2 files for all models
            Condition => '$$self{DIR_NAME} eq "IFD0" and $$self{TIFF_TYPE} =~ /^(ARW|SR2)$/',
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{TIFF_TYPE} =~ /^(ARW|SR2)$/',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawStart',
            Condition => '$$self{DIR_NAME} eq "SubIFD"',
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'SubIFD',
            # JpgFromRaw is in SubIFD of NEF, NRW and SRW files
            WriteCondition => '$$self{TIFF_TYPE} =~ /^(NEF|NRW|SRW)$/',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawStart',
            Condition => '$$self{DIR_NAME} eq "IFD2"',
            IsOffset => 1,
            OffsetPair => 0x202,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'IFD2',
            # JpgFromRaw is in IFD2 of PEF files
            WriteCondition => '$$self{TIFF_TYPE} eq "PEF"',
            Protected => 2,
        },
        {
            Name => 'OtherImageStart',
            IsOffset => 1,
            OffsetPair => 0x202,
        },
    ],
    0x202 => [
        {
            Name => 'ThumbnailLength',
            Notes => q{
                ThumbnailLength in IFD1 of JPEG and some TIFF-based images, IFD0 of MRW
                images and AVI videos, and the SubIFD in IFD1 of SRW images;
                PreviewImageLength in MakerNotes and IFD0 of ARW and SR2 images;
                JpgFromRawLength in SubIFD of NEF images, and IFD2 of PEF images; and
                OtherImageLength in everything else
            },
            Condition => q{
                $$self{DIR_NAME} eq 'IFD1' or
                ($$self{FILE_TYPE} eq 'RIFF' and $$self{DIR_NAME} eq 'IFD0')
            },
            OffsetPair => 0x201,
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'IFD1',
            WriteCondition => q{
                $$self{FILE_TYPE} ne "TIFF" or
                $$self{TIFF_TYPE} =~ /^(CR2|ARW|SR2|PEF)$/
            },
            Protected => 2,
        },
        {
            Name => 'ThumbnailLength',
            # thumbnail in IFD0 of MRW images (Minolta A200)
            Condition => '$$self{DIR_NAME} eq "IFD0" and $$self{TIFF_TYPE} eq "MRW"',
            OffsetPair => 0x201,
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{FILE_TYPE} eq "MRW"',
            Protected => 2,
        },
        {
            Name => 'ThumbnailLength',
            # in SubIFD of IFD1 in Samsung SRW images
            Condition => q{
                $$self{TIFF_TYPE} eq 'SRW' and $$self{DIR_NAME} eq 'SubIFD' and
                $$self{PATH}[-2] eq 'IFD1'
            },
            OffsetPair => 0x201,
            DataTag => 'ThumbnailImage',
            Writable => 'int32u',
            WriteGroup => 'SubIFD',
            WriteCondition => '$$self{TIFF_TYPE} eq "SRW"',
            Protected => 2,
        },
        {
            Name => 'PreviewImageLength',
            Condition => '$$self{DIR_NAME} eq "MakerNotes"',
            OffsetPair => 0x201,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'MakerNotes',
            # (no WriteCondition necessary because MakerNotes won't be created)
            Protected => 2,
        },
        {
            Name => 'PreviewImageLength',
            # PreviewImage in IFD0 of ARW and SR2 files for all models
            Condition => '$$self{DIR_NAME} eq "IFD0" and $$self{TIFF_TYPE} =~ /^(ARW|SR2)$/',
            OffsetPair => 0x201,
            DataTag => 'PreviewImage',
            Writable => 'int32u',
            WriteGroup => 'IFD0',
            WriteCondition => '$$self{TIFF_TYPE} =~ /^(ARW|SR2)$/',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawLength',
            Condition => '$$self{DIR_NAME} eq "SubIFD"',
            OffsetPair => 0x201,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'SubIFD',
            WriteCondition => '$$self{TIFF_TYPE} =~ /^(NEF|NRW|SRW)$/',
            Protected => 2,
        },
        {
            Name => 'JpgFromRawLength',
            Condition => '$$self{DIR_NAME} eq "IFD2"',
            OffsetPair => 0x201,
            DataTag => 'JpgFromRaw',
            Writable => 'int32u',
            WriteGroup => 'IFD2',
            WriteCondition => '$$self{TIFF_TYPE} eq "PEF"',
            Protected => 2,
        },
        {
            Name => 'OtherImageLength',
            OffsetPair => 0x201,
        },
    ],
    0x203 => 'JPEGRestartInterval',
    0x205 => 'JPEGLosslessPredictors',
    0x206 => 'JPEGPointTransforms',
    0x207 => {
        Name => 'JPEGQTables',
        IsOffset => 1,
        # this tag is not supported for writing, so define an
        # invalid offset pair to cause a "No size tag" error to be
        # generated if we try to write a file containing this tag
        OffsetPair => -1,
    },
    0x208 => {
        Name => 'JPEGDCTables',
        IsOffset => 1,
        OffsetPair => -1, # (see comment for JPEGQTables)
    },
    0x209 => {
        Name => 'JPEGACTables',
        IsOffset => 1,
        OffsetPair => -1, # (see comment for JPEGQTables)
    },
    0x211 => {
        Name => 'YCbCrCoefficients',
        Priority => 0,
    },
    0x212 => {
        Name => 'YCbCrSubSampling',
        PrintConvColumns => 2,
        PrintConv => \%Image::ExifTool::JPEG::yCbCrSubSampling,
        Priority => 0,
    },
    0x213 => {
        Name => 'YCbCrPositioning',
        PrintConv => {
            1 => 'Centered',
            2 => 'Co-sited',
        },
        Priority => 0,
    },
    0x214 => {
        Name => 'ReferenceBlackWhite',
        Priority => 0,
    },
    0x22f => 'StripRowCounts',
    0x2bc => {
        Name => 'ApplicationNotes', # (writable directory!)
        Writable => 'int8u',
        Format => 'undef',
        Flags => [ 'Binary', 'Protected' ],
        # this could be an XMP block
        SubDirectory => {
            DirName => 'XMP',
            TagTable => 'Image::ExifTool::XMP::Main',
        },
    },
    0x3e7 => 'USPTOMiscellaneous', #20
    0x1000 => 'RelatedImageFileFormat', #5
    0x1001 => 'RelatedImageWidth', #5
    0x1002 => { #5
        Name => 'RelatedImageHeight',
        Notes => 'called RelatedImageLength by the DCF spec.',
    },
    # (0x474x tags written by MicrosoftPhoto)
    0x4746 => 'Rating', #PH
    0x4747 => { # (written by Digital Image Pro)
        Name => 'XP_DIP_XML',
        Format => 'undef',
        # the following reference indicates this is Unicode:
        # http://social.msdn.microsoft.com/Forums/en-US/isvvba/thread/ce6edcbb-8fc2-40c6-ad98-85f5d835ddfb
        ValueConv => '$self->Decode($val,"UCS2","II")',
    }, 
    0x4748 => {
        Name => 'StitchInfo',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Microsoft::Stitch',
            ByteOrder => 'LittleEndian', #PH (NC)
        },
    },
    0x4749 => 'RatingPercent', #PH
    0x800d => 'ImageID', #10
    0x80a3 => { Name => 'WangTag1', Binary => 1 }, #20
    0x80a4 => { Name => 'WangAnnotation', Binary => 1 },
    0x80a5 => { Name => 'WangTag3', Binary => 1 }, #20
    0x80a6 => { #20
        Name => 'WangTag4',
        PrintConv => 'length($val) <= 64 ? $val : \$val',
    },
    0x80e3 => 'Matteing', #9
    0x80e4 => 'DataType', #9
    0x80e5 => 'ImageDepth', #9
    0x80e6 => 'TileDepth', #9
    0x827d => 'Model2',
    0x828d => 'CFARepeatPatternDim', #12
    0x828e => {
        Name => 'CFAPattern2', #12
        Format => 'int8u',  # (written incorrectly as 'undef' in Nikon NRW images)
    },
    0x828f => { #12
        Name => 'BatteryLevel',
        Groups => { 2 => 'Camera' },
    },
    0x8290 => {
        Name => 'KodakIFD',
        Groups => { 1 => 'KodakIFD' },
        Flags => 'SubIFD',
        Notes => 'used in various types of Kodak images',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::IFD',
            DirName => 'KodakIFD',
            Start => '$val',
        },
    },
    0x8298 => {
        Name => 'Copyright',
        Groups => { 2 => 'Author' },
        Format => 'undef',
        Notes => q{
            may contain copyright notices for photographer and editor, separated by a
            newline in ExifTool
        },
        # internally the strings are separated by a null character in this format:
        # Photographer only: photographer + NULL
        # Both:              photographer + NULL + editor + NULL
        # Editor only:       SPACE + NULL + editor + NULL
        # 1) translate first NULL to a newline, removing trailing blanks
        # 2) truncate at second NULL and remove trailing blanks
        # 3) remove trailing newline if it exists
        # (this is done as a RawConv so conditional replaces will work properly)
        RawConv => '$_=$val; s/ *\0/\n/; s/ *\0.*//s; s/\n$//; $_',
    },
    0x829a => {
        Name => 'ExposureTime',
        PrintConv => 'Image::ExifTool::Exif::PrintExposureTime($val)',
    },
    0x829d => {
        Name => 'FNumber',
        PrintConv => 'sprintf("%.1f",$val)',
    },
    0x82a5 => { #3
        Name => 'MDFileTag',
        Notes => 'tags 0x82a5-0x82ac are used in Molecular Dynamics GEL files',
    },
    0x82a6 => 'MDScalePixel', #3
    0x82a7 => 'MDColorTable', #3
    0x82a8 => 'MDLabName', #3
    0x82a9 => 'MDSampleInfo', #3
    0x82aa => 'MDPrepDate', #3
    0x82ab => 'MDPrepTime', #3
    0x82ac => 'MDFileUnits', #3
    0x830e => 'PixelScale',
    0x8335 => 'AdventScale', #20
    0x8336 => 'AdventRevision', #20
    0x835c => 'UIC1Tag', #23
    0x835d => 'UIC2Tag', #23
    0x835e => 'UIC3Tag', #23
    0x835f => 'UIC4Tag', #23
    0x83bb => { #12
        Name => 'IPTC-NAA', # (writable directory!)
        # this should actually be written as 'undef' (see
        # http://www.awaresystems.be/imaging/tiff/tifftags/iptc.html),
        # but Photoshop writes it as int32u and Nikon Capture won't read
        # anything else, so we do the same thing here...  Doh!
        Format => 'undef',      # convert binary values as undef
        Writable => 'int32u',   # but write int32u format code in IFD
        WriteGroup => 'IFD0',
        Flags => [ 'Binary', 'Protected' ],
        SubDirectory => {
            DirName => 'IPTC',
            TagTable => 'Image::ExifTool::IPTC::Main',
        },
    },
    0x847e => 'IntergraphPacketData', #3
    0x847f => 'IntergraphFlagRegisters', #3
    0x8480 => 'IntergraphMatrix',
    0x8481 => 'INGRReserved', #20
    0x8482 => {
        Name => 'ModelTiePoint',
        Groups => { 2 => 'Location' },
    },
    0x84e0 => 'Site', #9
    0x84e1 => 'ColorSequence', #9
    0x84e2 => 'IT8Header', #9
    0x84e3 => { #9
        Name => 'RasterPadding',
        PrintConv => { #20
            0 => 'Byte',
            1 => 'Word',
            2 => 'Long Word',
            9 => 'Sector',
            10 => 'Long Sector',
        },
    },
    0x84e4 => 'BitsPerRunLength', #9
    0x84e5 => 'BitsPerExtendedRunLength', #9
    0x84e6 => 'ColorTable', #9
    0x84e7 => { #9
        Name => 'ImageColorIndicator',
        PrintConv => { #20
            0 => 'Unspecified Image Color',
            1 => 'Specified Image Color',
        },
    },
    0x84e8 => { #9
        Name => 'BackgroundColorIndicator',
        PrintConv => { #20
            0 => 'Unspecified Background Color',
            1 => 'Specified Background Color',
        },
    },
    0x84e9 => 'ImageColorValue', #9
    0x84ea => 'BackgroundColorValue', #9
    0x84eb => 'PixelIntensityRange', #9
    0x84ec => 'TransparencyIndicator', #9
    0x84ed => 'ColorCharacterization', #9
    0x84ee => { #9
        Name => 'HCUsage',
        PrintConv => { #20
            0 => 'CT',
            1 => 'Line Art',
            2 => 'Trap',
        },
    },
    0x84ef => 'TrapIndicator', #17
    0x84f0 => 'CMYKEquivalent', #17
    0x8546 => { #11
        Name => 'SEMInfo',
        Notes => 'found in some scanning electron microscope images',
    },
    0x8568 => {
        Name => 'AFCP_IPTC',
        SubDirectory => {
            # must change directory name so we don't create this directory
            DirName => 'AFCP_IPTC',
            TagTable => 'Image::ExifTool::IPTC::Main',
        },
    },
    0x85b8 => 'PixelMagicJBIGOptions', #20
    0x85d8 => {
        Name => 'ModelTransform',
        Groups => { 2 => 'Location' },
    },
    0x8602 => { #16
        Name => 'WB_GRGBLevels',
        Notes => 'found in IFD0 of Leaf MOS images',
    },
    # 0x8603 - Leaf CatchLight color matrix (ref 16)
    0x8606 => {
        Name => 'LeafData',
        Format => 'undef',    # avoid converting huge block to string of int8u's!
        SubDirectory => {
            DirName => 'LeafIFD',
            TagTable => 'Image::ExifTool::Leaf::Main',
        },
    },
    0x8649 => { #19
        Name => 'PhotoshopSettings',
        Format => 'binary',
        SubDirectory => {
            DirName => 'Photoshop',
            TagTable => 'Image::ExifTool::Photoshop::Main',
        },
    },
    0x8769 => {
        Name => 'ExifOffset',
        Groups => { 1 => 'ExifIFD' },
        SubIFD => 2,
        SubDirectory => {
            DirName => 'ExifIFD',
            Start => '$val',
        },
    },
    0x8773 => {
        Name => 'ICC_Profile',
        SubDirectory => {
            TagTable => 'Image::ExifTool::ICC_Profile::Main',
        },
    },
    0x877f => { #20
        Name => 'TIFF_FXExtensions',
        PrintConv => { BITMASK => {
            0 => 'Resolution/Image Width',
            1 => 'N Layer Profile M',
            2 => 'Shared Data',
            3 => 'B&W JBIG2',
            4 => 'JBIG2 Profile M',
        }},
    },
    0x8780 => { #20
        Name => 'MultiProfiles',
        PrintConv => { BITMASK => {
            0 => 'Profile S',
            1 => 'Profile F',
            2 => 'Profile J',
            3 => 'Profile C',
            4 => 'Profile L',
            5 => 'Profile M',
            6 => 'Profile T',
            7 => 'Resolution/Image Width',
            8 => 'N Layer Profile M',
            9 => 'Shared Data',
            10 => 'JBIG2 Profile M',
        }},
    },
    0x8781 => { #22
        Name => 'SharedData',
        IsOffset => 1,
        # this tag is not supported for writing, so define an
        # invalid offset pair to cause a "No size tag" error to be
        # generated if we try to write a file containing this tag
        OffsetPair => -1,
    },
    0x8782 => 'T88Options', #20
    0x87ac => 'ImageLayer',
    0x87af => {
        Name => 'GeoTiffDirectory',
        Format => 'binary',
        Binary => 1,
    },
    0x87b0 => {
        Name => 'GeoTiffDoubleParams',
        Format => 'binary',
        Binary => 1,
    },
    0x87b1 => {
        Name => 'GeoTiffAsciiParams',
        Binary => 1,
    },
    0x8822 => {
        Name => 'ExposureProgram',
        Groups => { 2 => 'Camera' },
        Notes => 'the value of 9 is not standard EXIF, but is used by the Canon EOS 7D',
        PrintConv => {
            0 => 'Not Defined',
            1 => 'Manual',
            2 => 'Program AE',
            3 => 'Aperture-priority AE',
            4 => 'Shutter speed priority AE',
            5 => 'Creative (Slow speed)',
            6 => 'Action (High speed)',
            7 => 'Portrait',
            8 => 'Landscape',
            9 => 'Bulb', #25
        },
    },
    0x8824 => {
        Name => 'SpectralSensitivity',
        Groups => { 2 => 'Camera' },
    },
    0x8825 => {
        Name => 'GPSInfo',
        Groups => { 1 => 'GPS' },
        Flags => 'SubIFD',
        SubDirectory => {
            DirName => 'GPS',
            TagTable => 'Image::ExifTool::GPS::Main',
            Start => '$val',
        },
    },
    0x8827 => {
        Name => 'ISO',
        Notes => q{
            called ISOSpeedRatings by EXIF 2.2, then PhotographicSensitivity by the EXIF
            2.3 spec.
        },
        PrintConv => '$val=~s/\s+/, /g; $val',
    },
    0x8828 => {
        Name => 'Opto-ElectricConvFactor',
        Notes => 'called OECF by the EXIF spec.',
        Binary => 1,
    },
    0x8829 => 'Interlace', #12
    0x882a => 'TimeZoneOffset', #12
    0x882b => 'SelfTimerMode', #12
    0x8830 => { #24
        Name => 'SensitivityType',
        Notes => 'applies to EXIF:ISO tag',
        PrintConv => {
            0 => 'Unknown',
            1 => 'Standard Output Sensitivity',
            2 => 'Recommended Exposure Index',
            3 => 'ISO Speed',
            4 => 'Standard Output Sensitivity and Recommended Exposure Index',
            5 => 'Standard Output Sensitivity and ISO Speed',
            6 => 'Recommended Exposure Index and ISO Speed',
            7 => 'Standard Output Sensitivity, Recommended Exposure Index and ISO Speed',
        },
    },
    0x8831 => 'StandardOutputSensitivity', #24
    0x8832 => 'RecommendedExposureIndex', #24
    0x8833 => 'ISOSpeed', #24
    0x8834 => { #24
        Name => 'ISOSpeedLatitudeyyy',
        Description => 'ISO Speed Latitude yyy',
    },
    0x8835 => { #24
        Name => 'ISOSpeedLatitudezzz',
        Description => 'ISO Speed Latitude zzz',
    },
    0x885c => 'FaxRecvParams', #9
    0x885d => 'FaxSubAddress', #9
    0x885e => 'FaxRecvTime', #9
    0x888a => { #PH
        Name => 'LeafSubIFD',
        Format => 'int32u',     # Leaf incorrectly uses 'undef' format!
        Groups => { 1 => 'LeafSubIFD' },
        Flags => 'SubIFD',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Leaf::SubIFD',
            Start => '$val',
        },
    },
    0x9000 => {
        Name => 'ExifVersion',
        RawConv => '$val=~s/\0+$//; $val',  # (some idiots add null terminators)
    },
    0x9003 => {
        Name => 'DateTimeOriginal',
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        Notes => 'date/time when original image was taken',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    0x9004 => {
        Name => 'CreateDate',
        Groups => { 2 => 'Time' },
        Notes => 'called DateTimeDigitized by the EXIF spec.',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    0x9101 => {
        Name => 'ComponentsConfiguration',
        Format => 'int8u',
        PrintConvColumns => 2,
        PrintConv => {
            0 => '-',
            1 => 'Y',
            2 => 'Cb',
            3 => 'Cr',
            4 => 'R',
            5 => 'G',
            6 => 'B',
            OTHER => sub {
                my ($val, $inv, $conv) = @_;
                my @a = split /,?\s+/, $val;
                if ($inv) {
                    my %invConv;
                    $invConv{lc $$conv{$_}} = $_ foreach keys %$conv;
                    # strings like "YCbCr" and "RGB" still work for writing
                    @a = $a[0] =~ /(Y|Cb|Cr|R|G|B)/g if @a == 1;
                    foreach (@a) {
                        $_ = $invConv{lc $_};
                        return undef unless defined $_;
                    }
                    push @a, 0 while @a < 4;
                } else {
                    foreach (@a) {
                        $_ = $$conv{$_} || "Err ($_)";
                    }
                }
                return join ', ', @a;
            },
        },
    },
    0x9102 => 'CompressedBitsPerPixel',
    0x9201 => {
        Name => 'ShutterSpeedValue',
        Format => 'rational64s', # Leica M8 patch (incorrectly written as rational64u)
        ValueConv => 'abs($val)<100 ? 2**(-$val) : 0',
        PrintConv => 'Image::ExifTool::Exif::PrintExposureTime($val)',
    },
    0x9202 => {
        Name => 'ApertureValue',
        ValueConv => '2 ** ($val / 2)',
        PrintConv => 'sprintf("%.1f",$val)',
    },
    0x9203 => 'BrightnessValue',
    0x9204 => {
        Name => 'ExposureCompensation',
        Format => 'rational64s', # Leica M8 patch (incorrectly written as rational64u)
        Notes => 'called ExposureBiasValue by the EXIF spec.',
        PrintConv => 'Image::ExifTool::Exif::PrintFraction($val)',
    },
    0x9205 => {
        Name => 'MaxApertureValue',
        Groups => { 2 => 'Camera' },
        ValueConv => '2 ** ($val / 2)',
        PrintConv => 'sprintf("%.1f",$val)',
    },
    0x9206 => {
        Name => 'SubjectDistance',
        Groups => { 2 => 'Camera' },
        PrintConv => '$val =~ /^(inf|undef)$/ ? $val : "${val} m"',
    },
    0x9207 => {
        Name => 'MeteringMode',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Unknown',
            1 => 'Average',
            2 => 'Center-weighted average',
            3 => 'Spot',
            4 => 'Multi-spot',
            5 => 'Multi-segment',
            6 => 'Partial',
            255 => 'Other',
        },
    },
    0x9208 => {
        Name => 'LightSource',
        Groups => { 2 => 'Camera' },
        SeparateTable => 'LightSource',
        PrintConv => \%lightSource,
    },
    0x9209 => {
        Name => 'Flash',
        Groups => { 2 => 'Camera' },
        Flags => 'PrintHex',
        SeparateTable => 'Flash',
        PrintConv => \%flash,
    },
    0x920a => {
        Name => 'FocalLength',
        Groups => { 2 => 'Camera' },
        PrintConv => 'sprintf("%.1f mm",$val)',
    },
    # Note: tags 0x920b-0x9217 are duplicates of 0xa20b-0xa217
    # (The TIFF standard uses 0xa2xx, but you'll find both in images)
    0x920b => { #12
        Name => 'FlashEnergy',
        Groups => { 2 => 'Camera' },
    },
    0x920c => 'SpatialFrequencyResponse', #12 (not in Fuji images - PH)
    0x920d => 'Noise', #12
    0x920e => 'FocalPlaneXResolution', #12
    0x920f => 'FocalPlaneYResolution', #12
    0x9210 => { #12
        Name => 'FocalPlaneResolutionUnit',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            1 => 'None',
            2 => 'inches',
            3 => 'cm',
            4 => 'mm',
            5 => 'um',
        },
    },
    0x9211 => 'ImageNumber', #12
    0x9212 => { #12
        Name => 'SecurityClassification',
        PrintConv => {
            T => 'Top Secret',
            S => 'Secret',
            C => 'Confidential',
            R => 'Restricted',
            U => 'Unclassified',
        },
    },
    0x9213 => 'ImageHistory', #12
    0x9214 => {
        Name => 'SubjectArea',
        Groups => { 2 => 'Camera' },
    },
    0x9215 => 'ExposureIndex', #12
    0x9216 => 'TIFF-EPStandardID', #12
    0x9217 => { #12
        Name => 'SensingMethod',
        Groups => { 2 => 'Camera' },
        Notes => 'values 1 and 6 are not standard EXIF',
        PrintConv => {
            1 => 'Monochrome area', #12 (not standard EXIF)
            2 => 'One-chip color area',
            3 => 'Two-chip color area',
            4 => 'Three-chip color area',
            5 => 'Color sequential area',
            6 => 'Monochrome linear', #12 (not standard EXIF)
            7 => 'Trilinear',
            8 => 'Color sequential linear',
        },
    },
    0x9213 => 'ImageHistory',
    0x923a => 'CIP3DataFile', #20
    0x923b => 'CIP3Sheet', #20
    0x923c => 'CIP3Side', #20
    0x923f => 'StoNits', #9
    # handle maker notes as a conditional list
    0x927c => \@Image::ExifTool::MakerNotes::Main,
    0x9286 => {
        Name => 'UserComment',
        # may consider forcing a Format of 'undef' for this tag because I have
        # seen other applications write it incorrectly as 'string' or 'int8u'
        RawConv => 'Image::ExifTool::Exif::ConvertExifText($self,$val)',
    },
    0x9290 => {
        Name => 'SubSecTime',
        Groups => { 2 => 'Time' },
        ValueConv => '$val=~s/ +$//; $val', # trim trailing blanks
    },
    0x9291 => {
        Name => 'SubSecTimeOriginal',
        Groups => { 2 => 'Time' },
        ValueConv => '$val=~s/ +$//; $val', # trim trailing blanks
    },
    0x9292 => {
        Name => 'SubSecTimeDigitized',
        Groups => { 2 => 'Time' },
        ValueConv => '$val=~s/ +$//; $val', # trim trailing blanks
    },
    # The following 3 tags are found in MSOffice TIFF images
    # References:
    # http://social.msdn.microsoft.com/Forums/en-US/os_standocs/thread/03086d55-294a-49d5-967a-5303d34c40f8/
    # http://blogs.msdn.com/openspecification/archive/2009/12/08/details-of-three-tiff-tag-extensions-that-microsoft-office-document-imaging-modi-software-may-write-into-the-tiff-files-it-generates.aspx
    # http://www.microsoft.com/downloads/details.aspx?FamilyID=0dbc435d-3544-4f4b-9092-2f2643d64a39&displaylang=en#filelist
    0x932f => 'MSDocumentText',
    0x9330 => {
        Name => 'MSPropertySetStorage',
        Binary => 1,
    },
    0x9331 => {
        Name => 'MSDocumentTextPosition',
        Binary => 1, # (just in case -- don't know what format this is)
    },
    0x935c => { #3/19
        Name => 'ImageSourceData',
        Binary => 1,
    },
    0x9c9b => {
        Name => 'XPTitle',
        Format => 'undef',
        ValueConv => '$self->Decode($val,"UCS2","II")',
    },
    0x9c9c => {
        Name => 'XPComment',
        Format => 'undef',
        ValueConv => '$self->Decode($val,"UCS2","II")',
    },
    0x9c9d => {
        Name => 'XPAuthor',
        Groups => { 2 => 'Author' },
        Format => 'undef',
        ValueConv => '$self->Decode($val,"UCS2","II")',
    },
    0x9c9e => {
        Name => 'XPKeywords',
        Format => 'undef',
        ValueConv => '$self->Decode($val,"UCS2","II")',
    },
    0x9c9f => {
        Name => 'XPSubject',
        Format => 'undef',
        ValueConv => '$self->Decode($val,"UCS2","II")',
    },
    0xa000 => {
        Name => 'FlashpixVersion',
        RawConv => '$val=~s/\0+$//; $val',  # (some idiots add null terminators)
    },
    0xa001 => {
        Name => 'ColorSpace',
        Notes => q{
            the value of 0x2 is not standard EXIF.  Instead, an Adobe RGB image is
            indicated by "Uncalibrated" with an InteropIndex of "R03".  The values
            0xfffd and 0xfffe are also non-standard, and are used by some Sony cameras
        },
        PrintHex => 1,
        PrintConv => {
            1 => 'sRGB',
            2 => 'Adobe RGB',
            0xffff => 'Uncalibrated',
            # Sony uses these definitions: (ref JD)
            # 0xffff => 'Adobe RGB', (conflicts with Uncalibrated)
            0xfffe => 'ICC Profile',
            0xfffd => 'Wide Gamut RGB',
        },
    },
    0xa002 => {
        Name => 'ExifImageWidth',
        Notes => 'called PixelXDimension by the EXIF spec.',
    },
    0xa003 => {
        Name => 'ExifImageHeight',
        Notes => 'called PixelYDimension by the EXIF spec.',
    },
    0xa004 => 'RelatedSoundFile',
    0xa005 => {
        Name => 'InteropOffset',
        Groups => { 1 => 'InteropIFD' },
        Flags => 'SubIFD',
        Description => 'Interoperability Offset',
        SubDirectory => {
            DirName => 'InteropIFD',
            Start => '$val',
        },
    },
    0xa20b => {
        Name => 'FlashEnergy',
        Groups => { 2 => 'Camera' },
    },
    0xa20c => {
        Name => 'SpatialFrequencyResponse',
        PrintConv => 'Image::ExifTool::Exif::PrintSFR($val)',
    },
    0xa20d => 'Noise',
    0xa20e => { Name => 'FocalPlaneXResolution', Groups => { 2 => 'Camera' } },
    0xa20f => { Name => 'FocalPlaneYResolution', Groups => { 2 => 'Camera' } },
    0xa210 => {
        Name => 'FocalPlaneResolutionUnit',
        Groups => { 2 => 'Camera' },
        Notes => 'values 1, 4 and 5 are not standard EXIF',
        PrintConv => {
            1 => 'None', # (not standard EXIF)
            2 => 'inches',
            3 => 'cm',
            4 => 'mm',   # (not standard EXIF)
            5 => 'um',   # (not standard EXIF)
        },
    },
    0xa211 => 'ImageNumber',
    0xa212 => 'SecurityClassification',
    0xa213 => 'ImageHistory',
    0xa214 => {
        Name => 'SubjectLocation',
        Groups => { 2 => 'Camera' },
    },
    0xa215 => 'ExposureIndex',
    0xa216 => 'TIFF-EPStandardID',
    0xa217 => {
        Name => 'SensingMethod',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            1 => 'Not defined',
            2 => 'One-chip color area',
            3 => 'Two-chip color area',
            4 => 'Three-chip color area',
            5 => 'Color sequential area',
            7 => 'Trilinear',
            8 => 'Color sequential linear',
        },
    },
    0xa300 => {
        Name => 'FileSource',
        PrintConv => {
            1 => 'Film Scanner',
            2 => 'Reflection Print Scanner',
            3 => 'Digital Camera',
            # handle the case where Sigma incorrectly gives this tag a count of 4
            "\3\0\0\0" => 'Sigma Digital Camera',
        },
    },
    0xa301 => {
        Name => 'SceneType',
        PrintConv => {
            1 => 'Directly photographed',
        },
    },
    0xa302 => {
        Name => 'CFAPattern',
        RawConv => 'Image::ExifTool::Exif::DecodeCFAPattern($self, $val)',
        PrintConv => 'Image::ExifTool::Exif::PrintCFAPattern($val)',
    },
    0xa401 => {
        Name => 'CustomRendered',
        PrintConv => {
            0 => 'Normal',
            1 => 'Custom',
        },
    },
    0xa402 => {
        Name => 'ExposureMode',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Auto',
            1 => 'Manual',
            2 => 'Auto bracket',
            # have seen 3 for Samsung EX1 images - PH
        },
    },
    0xa403 => {
        Name => 'WhiteBalance',
        Groups => { 2 => 'Camera' },
        # set Priority to zero to keep this WhiteBalance from overriding the
        # MakerNotes WhiteBalance, since the MakerNotes WhiteBalance and is more
        # accurate and contains more information (if it exists)
        Priority => 0,
        PrintConv => {
            0 => 'Auto',
            1 => 'Manual',
        },
    },
    0xa404 => {
        Name => 'DigitalZoomRatio',
        Groups => { 2 => 'Camera' },
    },
    0xa405 => {
        Name => 'FocalLengthIn35mmFormat',
        Notes => 'called FocalLengthIn35mmFilm by the EXIF spec.',
        Groups => { 2 => 'Camera' },
        PrintConv => '"$val mm"',
    },
    0xa406 => {
        Name => 'SceneCaptureType',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Standard',
            1 => 'Landscape',
            2 => 'Portrait',
            3 => 'Night',
        },
    },
    0xa407 => {
        Name => 'GainControl',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'None',
            1 => 'Low gain up',
            2 => 'High gain up',
            3 => 'Low gain down',
            4 => 'High gain down',
        },
    },
    0xa408 => {
        Name => 'Contrast',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Normal',
            1 => 'Low',
            2 => 'High',
        },
    },
    0xa409 => {
        Name => 'Saturation',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Normal',
            1 => 'Low',
            2 => 'High',
        },
    },
    0xa40a => {
        Name => 'Sharpness',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Normal',
            1 => 'Soft',
            2 => 'Hard',
        },
    },
    0xa40b => {
        Name => 'DeviceSettingDescription',
        Groups => { 2 => 'Camera' },
        Binary => 1,
    },
    0xa40c => {
        Name => 'SubjectDistanceRange',
        Groups => { 2 => 'Camera' },
        PrintConv => {
            0 => 'Unknown',
            1 => 'Macro',
            2 => 'Close',
            3 => 'Distant',
        },
    },
    # 0xa40d - int16u: 0 (GE E1486 TW)
    # 0xa40e - int16u: 1 (GE E1486 TW)
    0xa420 => 'ImageUniqueID',
    0xa430 => { #24
        Name => 'OwnerName',
        Notes => 'called CameraOwnerName by the EXIF spec.',
    },
    0xa431 => { #24
        Name => 'SerialNumber',
        Notes => 'called BodySerialNumber by the EXIF spec.',
    },
    0xa432 => { #24
        Name => 'LensInfo',
        Notes => q{
            4 rational values giving focal and aperture ranges, called LensSpecification
            by the EXIF spec.
        },
        # convert to the form "12-20mm f/3.8-4.5" or "50mm f/1.4"
        PrintConv => \&Image::ExifTool::Exif::PrintLensInfo,
    },
    0xa433 => 'LensMake', #24
    0xa434 => 'LensModel', #24
    0xa435 => 'LensSerialNumber', #24
    0xa480 => 'GDALMetadata', #3
    0xa481 => 'GDALNoData', #3
    0xa500 => 'Gamma',
    0xafc0 => 'ExpandSoftware', #JD (Opanda)
    0xafc1 => 'ExpandLens', #JD (Opanda)
    0xafc2 => 'ExpandFilm', #JD (Opanda)
    0xafc3 => 'ExpandFilterLens', #JD (Opanda)
    0xafc4 => 'ExpandScanner', #JD (Opanda)
    0xafc5 => 'ExpandFlashLamp', #JD (Opanda)
#
# Windows Media Photo / HD Photo (WDP/HDP) tags
#
    0xbc01 => { #13
        Name => 'PixelFormat',
        PrintHex => 1,
        Format => 'undef',
        Notes => q{
            tags 0xbc** are used in Windows HD Photo (HDP and WDP) images. The actual
            PixelFormat values are 16-byte GUID's but the leading 15 bytes,
            '6fddc324-4e03-4bfe-b1853-d77768dc9', have been removed below to avoid
            unnecessary clutter
        },
        ValueConv => q{
            require Image::ExifTool::ASF;
            $val = Image::ExifTool::ASF::GetGUID($val);
            # GUID's are too long, so remove redundant information
            $val =~ s/^6fddc324-4e03-4bfe-b185-3d77768dc9//i and $val = hex($val);
            return $val;
        },
        PrintConv => {
            0x0d => '24-bit RGB',
            0x0c => '24-bit BGR',
            0x0e => '32-bit BGR',
            0x15 => '48-bit RGB',
            0x12 => '48-bit RGB Fixed Point',
            0x3b => '48-bit RGB Half',
            0x18 => '96-bit RGB Fixed Point',
            0x1b => '128-bit RGB Float',
            0x0f => '32-bit BGRA',
            0x16 => '64-bit RGBA',
            0x1d => '64-bit RGBA Fixed Point',
            0x3a => '64-bit RGBA Half',
            0x1e => '128-bit RGBA Fixed Point',
            0x19 => '128-bit RGBA Float',
            0x10 => '32-bit PBGRA',
            0x17 => '64-bit PRGBA',
            0x1a => '128-bit PRGBA Float',
            0x1c => '32-bit CMYK',
            0x2c => '40-bit CMYK Alpha',
            0x1f => '64-bit CMYK',
            0x2d => '80-bit CMYK Alpha',
            0x20 => '24-bit 3 Channels',
            0x21 => '32-bit 4 Channels',
            0x22 => '40-bit 5 Channels',
            0x23 => '48-bit 6 Channels',
            0x24 => '56-bit 7 Channels',
            0x25 => '64-bit 8 Channels',
            0x2e => '32-bit 3 Channels Alpha',
            0x2f => '40-bit 4 Channels Alpha',
            0x30 => '48-bit 5 Channels Alpha',
            0x31 => '56-bit 6 Channels Alpha',
            0x32 => '64-bit 7 Channels Alpha',
            0x33 => '72-bit 8 Channels Alpha',
            0x26 => '48-bit 3 Channels',
            0x27 => '64-bit 4 Channels',
            0x28 => '80-bit 5 Channels',
            0x29 => '96-bit 6 Channels',
            0x2a => '112-bit 7 Channels',
            0x2b => '128-bit 8 Channels',
            0x34 => '64-bit 3 Channels Alpha',
            0x35 => '80-bit 4 Channels Alpha',
            0x36 => '96-bit 5 Channels Alpha',
            0x37 => '112-bit 6 Channels Alpha',
            0x38 => '128-bit 7 Channels Alpha',
            0x39 => '144-bit 8 Channels Alpha',
            0x08 => '8-bit Gray',
            0x0b => '16-bit Gray',
            0x13 => '16-bit Gray Fixed Point',
            0x3e => '16-bit Gray Half',
            0x3f => '32-bit Gray Fixed Point',
            0x11 => '32-bit Gray Float',
            0x05 => 'Black & White',
            0x09 => '16-bit BGR555',
            0x0a => '16-bit BGR565',
            0x13 => '32-bit BGR101010',
            0x3d => '32-bit RGBE',
        },
    },
    0xbc02 => { #13
        Name => 'Transformation',
        PrintConv => {
            0 => 'Horizontal (normal)',
            1 => 'Mirror vertical',
            2 => 'Mirror horizontal',
            3 => 'Rotate 180',
            4 => 'Rotate 90 CW',
            5 => 'Mirror horizontal and rotate 90 CW',
            6 => 'Mirror horizontal and rotate 270 CW',
            7 => 'Rotate 270 CW',
        },
    },
    0xbc03 => { #13
        Name => 'Uncompressed',
        PrintConv => { 0 => 'No', 1 => 'Yes' },
    },
    0xbc04 => { #13
        Name => 'ImageType',
        PrintConv => { BITMASK => {
            0 => 'Preview',
            1 => 'Page',
        } },
    },
    0xbc80 => 'ImageWidth', #13
    0xbc81 => 'ImageHeight', #13
    0xbc82 => 'WidthResolution', #13
    0xbc83 => 'HeightResolution', #13
    0xbcc0 => { #13
        Name => 'ImageOffset',
        IsOffset => 1,
        OffsetPair => 0xbcc1,  # point to associated byte count
    },
    0xbcc1 => { #13
        Name => 'ImageByteCount',
        OffsetPair => 0xbcc0,  # point to associated offset
    },
    0xbcc2 => { #13
        Name => 'AlphaOffset',
        IsOffset => 1,
        OffsetPair => 0xbcc3,  # point to associated byte count
    },
    0xbcc3 => { #13
        Name => 'AlphaByteCount',
        OffsetPair => 0xbcc2,  # point to associated offset
    },
    0xbcc4 => { #13
        Name => 'ImageDataDiscard',
        PrintConv => {
            0 => 'Full Resolution',
            1 => 'Flexbits Discarded',
            2 => 'HighPass Frequency Data Discarded',
            3 => 'Highpass and LowPass Frequency Data Discarded',
        },
    },
    0xbcc5 => { #13
        Name => 'AlphaDataDiscard',
        PrintConv => {
            0 => 'Full Resolution',
            1 => 'Flexbits Discarded',
            2 => 'HighPass Frequency Data Discarded',
            3 => 'Highpass and LowPass Frequency Data Discarded',
        },
    },
#
    0xc427 => 'OceScanjobDesc', #3
    0xc428 => 'OceApplicationSelector', #3
    0xc429 => 'OceIDNumber', #3
    0xc42a => 'OceImageLogic', #3
    0xc44f => { Name => 'Annotations', Binary => 1 }, #7/19
    0xc4a5 => {
        Name => 'PrintIM', # (writable directory!)
        # must set Writable here so this tag will be saved with MakerNotes option
        Writable => 'undef',
        WriteGroup => 'IFD0',
        Description => 'Print Image Matching',
        SubDirectory => {
            TagTable => 'Image::ExifTool::PrintIM::Main',
        },
    },
    0xc580 => { #20
        Name => 'USPTOOriginalContentType',
        PrintConv => {
            0 => 'Text or Drawing',
            1 => 'Grayscale',
            2 => 'Color',
        },
    },
#
# DNG tags 0xc6XX and 0xc7XX (ref 2 unless otherwise stated)
#
    0xc612 => {
        Name => 'DNGVersion',
        Notes => 'tags 0xc612-0xc761 are used in DNG images unless otherwise noted',
        DataMember => 'DNGVersion',
        RawConv => '$$self{DNGVersion} = $val',
        PrintConv => '$val =~ tr/ /./; $val',
    },
    0xc613 => 'DNGBackwardVersion',
    0xc614 => 'UniqueCameraModel',
    0xc615 => {
        Name => 'LocalizedCameraModel',
        Format => 'string',
        PrintConv => '$self->Printable($val, 0)',
    },
    0xc616 => {
        Name => 'CFAPlaneColor',
        PrintConv => q{
            my @cols = qw(Red Green Blue Cyan Magenta Yellow White);
            my @vals = map { $cols[$_] || "Unknown($_)" } split(' ', $val);
            return join(',', @vals);
        },
    },
    0xc617 => {
        Name => 'CFALayout',
        PrintConv => {
            1 => 'Rectangular',
            2 => 'Even columns offset down 1/2 row',
            3 => 'Even columns offset up 1/2 row',
            4 => 'Even rows offset right 1/2 column',
            5 => 'Even rows offset left 1/2 column',
            # the following are new for DNG 1.3:
            6 => 'Even rows offset up by 1/2 row, even columns offset left by 1/2 column',
            7 => 'Even rows offset up by 1/2 row, even columns offset right by 1/2 column',
            8 => 'Even rows offset down by 1/2 row, even columns offset left by 1/2 column',
            9 => 'Even rows offset down by 1/2 row, even columns offset right by 1/2 column',
        },
    },
    0xc618 => { Name => 'LinearizationTable', Binary => 1 },
    0xc619 => 'BlackLevelRepeatDim',
    0xc61a => 'BlackLevel',
    0xc61b => { Name => 'BlackLevelDeltaH', %longBin },
    0xc61c => { Name => 'BlackLevelDeltaV', %longBin },
    0xc61d => 'WhiteLevel',
    0xc61e => 'DefaultScale',
    0xc61f => 'DefaultCropOrigin',
    0xc620 => 'DefaultCropSize',
    0xc621 => 'ColorMatrix1',
    0xc622 => 'ColorMatrix2',
    0xc623 => 'CameraCalibration1',
    0xc624 => 'CameraCalibration2',
    0xc625 => 'ReductionMatrix1',
    0xc626 => 'ReductionMatrix2',
    0xc627 => 'AnalogBalance',
    0xc628 => 'AsShotNeutral',
    0xc629 => 'AsShotWhiteXY',
    0xc62a => 'BaselineExposure',
    0xc62b => 'BaselineNoise',
    0xc62c => 'BaselineSharpness',
    0xc62d => 'BayerGreenSplit',
    0xc62e => 'LinearResponseLimit',
    0xc62f => {
        Name => 'CameraSerialNumber',
        Groups => { 2 => 'Camera' },
    },
    0xc630 => {
        Name => 'DNGLensInfo',
        Groups => { 2 => 'Camera' },
        PrintConv =>\&PrintLensInfo,
    },
    0xc631 => 'ChromaBlurRadius',
    0xc632 => 'AntiAliasStrength',
    0xc633 => 'ShadowScale',
    0xc634 => [
        {
            Condition => '$$self{TIFF_TYPE} =~ /^(ARW|SR2)$/',
            Name => 'SR2Private',
            Groups => { 1 => 'SR2' },
            Flags => 'SubIFD',
            Format => 'int32u',
            # some utilites have problems unless this is int8u format:
            # - Adobe Camera Raw 5.3 gives an error
            # - Apple Preview 10.5.8 gets the wrong white balance
            FixFormat => 'int8u', # (stupid Sony)
            SubDirectory => {
                DirName => 'SR2Private',
                TagTable => 'Image::ExifTool::Sony::SR2Private',
                Start => '$val',
            },
        },
        {
            Condition => '$$valPt =~ /^Adobe\0/',
            Name => 'DNGAdobeData',
            NestedHtmlDump => 1,
            SubDirectory => { TagTable => 'Image::ExifTool::DNG::AdobeData' },
            Format => 'undef',  # written incorrectly as int8u (change to undef for speed)
        },
        {
            Condition => '$$valPt =~ /^(PENTAX |SAMSUNG)\0/',
            Name => 'MakerNotePentax',
            MakerNotes => 1,    # (causes "MakerNotes header" to be identified in HtmlDump output)
            Binary => 1,
            # Note: Don't make this block-writable for a few reasons:
            # 1) It would be dangerous (possibly confusing Pentax software)
            # 2) It is a different format from the JPEG version of MakerNotePentax
            # 3) It is converted to JPEG format by RebuildMakerNotes() when copying
            SubDirectory => {
                TagTable => 'Image::ExifTool::Pentax::Main',
                Start => '$valuePtr + 10',
                Base => '$start - 10',
                ByteOrder => 'Unknown', # easier to do this than read byteorder word
            },
            Format => 'undef',  # written incorrectly as int8u (change to undef for speed)
        },
        {
            Name => 'DNGPrivateData',
            Binary => 1,
            Format => 'undef',
        },
    ],
    0xc635 => {
        Name => 'MakerNoteSafety',
        PrintConv => {
            0 => 'Unsafe',
            1 => 'Safe',
        },
    },
    0xc640 => { #15
        Name => 'RawImageSegmentation',
        # (int16u[3], not writable)
        Notes => q{
            used in segmented Canon CR2 images.  3 numbers: 1. Number of segments minus
            one; 2. Pixel width of segments except last; 3. Pixel width of last segment
        },
    },
    0xc65a => {
        Name => 'CalibrationIlluminant1',
        SeparateTable => 'LightSource',
        PrintConv => \%lightSource,
    },
    0xc65b => {
        Name => 'CalibrationIlluminant2',
        SeparateTable => 'LightSource',
        PrintConv => \%lightSource,
    },
    0xc65c => 'BestQualityScale',
    0xc65d => {
        Name => 'RawDataUniqueID',
        Format => 'undef',
        ValueConv => 'uc(unpack("H*",$val))',
    },
    0xc660 => { #3
        Name => 'AliasLayerMetadata',
        Notes => 'used by Alias Sketchbook Pro',
    },
    0xc68b => {
        Name => 'OriginalRawFileName',
        Format => 'string', # sometimes written as int8u
    },
    0xc68c => {
        Name => 'OriginalRawFileData', # (writable directory!)
        Writable => 'undef', # must be defined here so tag will be extracted if specified
        WriteGroup => 'IFD0',
        Flags => [ 'Binary', 'Protected' ],
        SubDirectory => {
            TagTable => 'Image::ExifTool::DNG::OriginalRaw',
        },
    },
    0xc68d => 'ActiveArea',
    0xc68e => 'MaskedAreas',
    0xc68f => {
        Name => 'AsShotICCProfile',
        Binary => 1,
        Writable => 'undef', # must be defined here so tag will be extracted if specified
        SubDirectory => {
            DirName => 'AsShotICCProfile',
            TagTable => 'Image::ExifTool::ICC_Profile::Main',
        },
    },
    0xc690 => 'AsShotPreProfileMatrix',
    0xc691 => {
        Name => 'CurrentICCProfile',
        Binary => 1,
        Writable => 'undef', # must be defined here so tag will be extracted if specified
        SubDirectory => {
            DirName => 'CurrentICCProfile',
            TagTable => 'Image::ExifTool::ICC_Profile::Main',
        },
    },
    0xc692 => 'CurrentPreProfileMatrix',
    0xc6bf => 'ColorimetricReference',
    0xc6d2 => { #JD (Panasonic DMC-TZ5)
        # this text is UTF-8 encoded (hooray!) - PH (TZ5)
        Name => 'PanasonicTitle',
        Format => 'string', # written incorrectly as 'undef'
        Notes => 'proprietary Panasonic tag used for baby/pet name, etc',
        # panasonic always records this tag (64 zero bytes),
        # so ignore it unless it contains valid information
        RawConv => 'length($val) ? $val : undef',
        ValueConv => '$self->Decode($val, "UTF8")',
    },
    0xc6d3 => { #PH (Panasonic DMC-FS7)
        Name => 'PanasonicTitle2',
        Format => 'string', # written incorrectly as 'undef'
        Notes => 'proprietary Panasonic tag used for baby/pet name with age',
        # panasonic always records this tag (128 zero bytes),
        # so ignore it unless it contains valid information
        RawConv => 'length($val) ? $val : undef',
        ValueConv => '$self->Decode($val, "UTF8")',
    },
    0xc6f3 => 'CameraCalibrationSig',
    0xc6f4 => 'ProfileCalibrationSig',
    0xc6f5 => {
        Name => 'ProfileIFD', # (ExtraCameraProfiles)
        Groups => { 1 => 'ProfileIFD' },
        Flags => 'SubIFD',
        SubDirectory => {
            ProcessProc => \&ProcessTiffIFD,
            WriteProc => \&ProcessTiffIFD,
            DirName => 'ProfileIFD',
            Start => '$val',
            Base => '$start',   # offsets relative to start of TIFF-like header
            MaxSubdirs => 10,
            Magic => 0x4352,    # magic number for TIFF-like header
        },
    },
    0xc6f6 => 'AsShotProfileName',
    0xc6f7 => 'NoiseReductionApplied',
    0xc6f8 => 'ProfileName',
    0xc6f9 => 'ProfileHueSatMapDims',
    0xc6fa => 'ProfileHueSatMapData1',
    0xc6fb => 'ProfileHueSatMapData2',
    0xc6fc => {
        Name => 'ProfileToneCurve',
        Binary => 1,
    },
    0xc6fd => {
        Name => 'ProfileEmbedPolicy',
        PrintConv => {
            0 => 'Allow Copying',
            1 => 'Embed if Used',
            2 => 'Never Embed',
            3 => 'No Restrictions',
        },
    },
    0xc6fe => 'ProfileCopyright',
    0xc714 => 'ForwardMatrix1',
    0xc715 => 'ForwardMatrix2',
    0xc716 => 'PreviewApplicationName',
    0xc717 => 'PreviewApplicationVersion',
    0xc718 => 'PreviewSettingsName',
    0xc719 => {
        Name => 'PreviewSettingsDigest',
        Format => 'undef',
        ValueConv => 'unpack("H*", $val)',
    },
    0xc71a => 'PreviewColorSpace',
    0xc71b => {
        Name => 'PreviewDateTime',
        Groups => { 2 => 'Time' },
        ValueConv => q{
            require Image::ExifTool::XMP;
            return Image::ExifTool::XMP::ConvertXMPDate($val);
        },
    },
    0xc71c => {
        Name => 'RawImageDigest',
        Format => 'undef',
        ValueConv => 'unpack("H*", $val)',
    },
    0xc71d => {
        Name => 'OriginalRawFileDigest',
        Format => 'undef',
        ValueConv => 'unpack("H*", $val)',
    },
    0xc71e => 'SubTileBlockSize',
    0xc71f => 'RowInterleaveFactor',
    0xc725 => 'ProfileLookTableDims',
    0xc726 => {
        Name => 'ProfileLookTableData',
        Binary => 1,
    },
    0xc740 => { # DNG 1.3
        Name => 'OpcodeList1',
        Binary => 1,
        # opcodes:
        # 1 => 'WarpRectilinear',
        # 2 => 'WarpFisheye',
        # 3 => 'FixVignetteRadial',
        # 4 => 'FixBadPixelsConstant',
        # 5 => 'FixBadPixelsList',
        # 6 => 'TrimBounds',
        # 7 => 'MapTable',
        # 8 => 'MapPolynomial',
        # 9 => 'GainMap',
        # 10 => 'DeltaPerRow',
        # 11 => 'DeltaPerColumn',
        # 12 => 'ScalePerRow',
        # 13 => 'ScalePerColumn',
    },
    0xc741 => { # DNG 1.3
        Name => 'OpcodeList2',
        Binary => 1,
    },
    0xc74e => { # DNG 1.3
        Name => 'OpcodeList3',
        Binary => 1,
    },
    0xc761 => 'NoiseProfile', # DNG 1.3
    0xea1c => { #13
        Name => 'Padding',
        Binary => 1,
        Writable => 'undef',
        # must start with 0x1c 0xea by the WM Photo specification
        # (not sure what should happen if padding is only 1 byte)
        # (why does MicrosoftPhoto write "1c ea 00 00 00 08"?)
        RawConvInv => '$val=~s/^../\x1c\xea/s; $val',
    },
    0xea1d => {
        Name => 'OffsetSchema',
        Notes => "Microsoft's ill-conceived maker note offset difference",
        # From the Microsoft documentation:
        #
        #     Any time the "Maker Note" is relocated by Windows, the Exif MakerNote
        #     tag (37500) is updated automatically to reference the new location. In
        #     addition, Windows records the offset (or difference) between the old and
        #     new locations in the Exif OffsetSchema tag (59933). If the "Maker Note"
        #     contains relative references, the developer can add the value in
        #     OffsetSchema to the original references to find the correct information.
        #
        # My recommendation is for other developers to ignore this tag because the
        # information it contains is unreliable. It will be wrong if the image has
        # been subsequently edited by another application that doesn't recognize the
        # new Microsoft tag.
        #
        # The new tag unfortunately only gives the difference between the new maker
        # note offset and the original offset. Instead, it should have been designed
        # to store the original offset. The new offset may change if the image is
        # edited, which will invalidate the tag as currently written. If instead the
        # original offset had been stored, the new difference could be easily
        # calculated because the new maker note offset is known.
        #
        # I exchanged emails with a Microsoft technical representative, pointing out
        # this problem shortly after they released the update (Feb 2007), but so far
        # they have taken no steps to address this.
    },

    # tags in the range 0xfde8-0xfe58 have been observed in PS7 files
    # generated from RAW images.  They are all strings with the
    # tag name at the start of the string.  To accomodate these types
    # of tags, all tags with values above 0xf000 are handled specially
    # by ProcessExif().

    0xfe00 => {
        Name => 'KDC_IFD',
        Groups => { 1 => 'KDC_IFD' },
        Flags => 'SubIFD',
        Notes => 'used in some Kodak KDC images',
        SubDirectory => {
            TagTable => 'Image::ExifTool::Kodak::KDC_IFD',
            DirName => 'KDC_IFD',
            Start => '$val',
        },
    },
);

# EXIF Composite tags (plus other more general Composite tags)
%Image::ExifTool::Exif::Composite = (
    GROUPS => { 2 => 'Image' },
    ImageSize => {
        Require => {
            0 => 'ImageWidth',
            1 => 'ImageHeight',
        },
        Desire => {
            2 => 'ExifImageWidth',
            3 => 'ExifImageHeight',
        },
        # use ExifImageWidth/Height only for Canon and Phase One TIFF-base RAW images
        ValueConv => q{
            return "$val[2]x$val[3]" if $val[2] and $val[3] and
                    $$self{TIFF_TYPE} =~ /^(CR2|Canon 1D RAW|IIQ|EIP)$/;
            return "$val[0]x$val[1]";
        },
    },
    # pick the best shutter speed value
    ShutterSpeed => {
        Desire => {
            0 => 'ExposureTime',
            1 => 'ShutterSpeedValue',
            2 => 'BulbDuration',
        },
        ValueConv => '($val[2] and $val[2]>0) ? $val[2] : (defined($val[0]) ? $val[0] : $val[1])',
        PrintConv => 'Image::ExifTool::Exif::PrintExposureTime($val)',
    },
    Aperture => {
        Desire => {
            0 => 'FNumber',
            1 => 'ApertureValue',
        },
        RawConv => '($val[0] || $val[1]) ? $val : undef',
        ValueConv => '$val[0] || $val[1]',
        PrintConv => 'sprintf("%.1f", $val)',
    },
    LightValue => {
        Notes => 'calculated LV -- similar to exposure value but includes ISO speed',
        Require => {
            0 => 'Aperture',
            1 => 'ShutterSpeed',
            2 => 'ISO',
        },
        ValueConv => 'Image::ExifTool::Exif::CalculateLV($val[0],$val[1],$prt[2])',
        PrintConv => 'sprintf("%.1f",$val)',
    },
    FocalLength35efl => { #26/PH
        Description => 'Focal Length',
        Notes => 'this value may be incorrect if the image has been resized',
        Groups => { 2 => 'Camera' },
        Require => {
            0 => 'FocalLength',
        },
        Desire => {
            1 => 'ScaleFactor35efl',
        },
        ValueConv => 'ToFloat(@val); ($val[0] || 0) * ($val[1] || 1)',
        PrintConv => '$val[1] ? sprintf("%.1f mm (35 mm equivalent: %.1f mm)", $val[0], $val) : sprintf("%.1f mm", $val)',
    },
    ScaleFactor35efl => { #26/PH
        Description => 'Scale Factor To 35 mm Equivalent',
        Notes => 'this value and any derived values may be incorrect if image has been resized',
        Groups => { 2 => 'Camera' },
        Desire => {
            0 => 'FocalLength',
            1 => 'FocalLengthIn35mmFormat',
            2 => 'Composite:DigitalZoom',
            3 => 'FocalPlaneDiagonal',
            4 => 'FocalPlaneXSize',
            5 => 'FocalPlaneYSize',
            6 => 'FocalPlaneResolutionUnit',
            7 => 'FocalPlaneXResolution',
            8 => 'FocalPlaneYResolution',
            9 => 'ExifImageWidth',
           10 => 'ExifImageHeight',
           11 => 'CanonImageWidth',
           12 => 'CanonImageHeight',
           13 => 'ImageWidth',
           14 => 'ImageHeight',
        },
        ValueConv => 'Image::ExifTool::Exif::CalcScaleFactor35efl(@val)',
        PrintConv => 'sprintf("%.1f", $val)',
    },
    CircleOfConfusion => {
        Notes => q{
            this value may be incorrect if the image has been resized.  Calculated as
            D/1440, where D is the focal plane diagonal in mm
        },
        Groups => { 2 => 'Camera' },
        Require => 'ScaleFactor35efl',
        ValueConv => 'sqrt(24*24+36*36) / ($val * 1440)',
        PrintConv => 'sprintf("%.3f mm",$val)',
    },
    HyperfocalDistance => {
        Notes => 'this value may be incorrect if the image has been resized',
        Groups => { 2 => 'Camera' },
        Require => {
            0 => 'FocalLength',
            1 => 'Aperture',
            2 => 'CircleOfConfusion',
        },
        ValueConv => q{
            ToFloat(@val);
            return 'inf' unless $val[1] and $val[2];
            return $val[0] * $val[0] / ($val[1] * $val[2] * 1000);
        },
        PrintConv => 'sprintf("%.2f m", $val)',
    },
    DOF => {
        Description => 'Depth Of Field',
        Notes => 'this value may be incorrect if the image has been resized',
        Require => {
            0 => 'FocalLength',
            1 => 'Aperture',
            2 => 'CircleOfConfusion',
        },
        Desire => {
            3 => 'FocusDistance',   # focus distance in metres (0 is infinity)
            4 => 'SubjectDistance',
            5 => 'ObjectDistance',
        },
        ValueConv => q{
            ToFloat(@val);
            my ($d, $f) = ($val[3], $val[0]);
            if (defined $d) {
                $d or $d = 1e10;    # (use large number for infinity)
            } else {
                $d = $val[4] || $val[5];
                return undef unless defined $d;
            }
            return 0 unless $f and $val[2];
            my $t = $val[1] * $val[2] * ($d * 1000 - $f) / ($f * $f);
            my @v = ($d / (1 + $t), $d / (1 - $t));
            $v[1] < 0 and $v[1] = 0; # 0 means 'inf'
            return join(' ',@v);
        },
        PrintConv => q{
            $val =~ tr/,/./;    # in case locale is whacky
            my @v = split ' ', $val;
            $v[1] or return sprintf("inf (%.2f m - inf)", $v[0]);
            return sprintf("%.2f m (%.2f - %.2f)",$v[1]-$v[0],$v[0],$v[1]);
        },
    },
    FOV => {
        Description => 'Field Of View',
        Notes => q{
            calculated for the long image dimension, this value may be incorrect for
            fisheye lenses, or if the image has been resized
        },
        Require => {
            0 => 'FocalLength',
            1 => 'ScaleFactor35efl',
        },
        Desire => {
            2 => 'FocusDistance', # (multiply by 1000 to convert to mm)
        },
        # ref http://www.bobatkins.com/photography/technical/field_of_view.html
        # (calculations below apply to rectilinear lenses only, not fisheye)
        ValueConv => q{
            ToFloat(@val);
            return undef unless $val[0] and $val[1];
            my $corr = 1;
            if ($val[2]) {
                my $d = 1000 * $val[2] - $val[0];
                $corr += $val[0]/$d if $d > 0;
            }
            my $fd2 = atan2(36, 2*$val[0]*$val[1]*$corr);
            my @fov = ( $fd2 * 360 / 3.14159 );
            if ($val[2] and $val[2] > 0 and $val[2] < 10000) {
                push @fov, 2 * $val[2] * sin($fd2) / cos($fd2);
            }
            return join(' ', @fov);
        },
        PrintConv => q{
            my @v = split(' ',$val);
            my $str = sprintf("%.1f deg", $v[0]);
            $str .= sprintf(" (%.2f m)", $v[1]) if $v[1];
            return $str;
        },
    },
    # generate DateTimeOriginal from Date and Time Created if not extracted already
    DateTimeOriginal => {
        Condition => 'not defined $$self{VALUE}{DateTimeOriginal}',
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        Desire => {
            0 => 'DateTimeCreated',
            1 => 'DateCreated',
            2 => 'TimeCreated',
        },
        ValueConv => q{
            return $val[0] if $val[0] and $val[0]=~/ /;
            return undef unless $val[1] and $val[2];
            return "$val[1] $val[2]";
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    ThumbnailImage => {
        Writable => 1,
        WriteCheck => '$self->CheckImage(\$val)',
        WriteAlso => {
            # (the 0xfeedfeed values are translated in the Exif write routine)
            ThumbnailOffset => 'defined $val ? 0xfeedfeed : undef',
            ThumbnailLength => 'defined $val ? 0xfeedfeed : undef',
        },
        Require => {
            0 => 'ThumbnailOffset',
            1 => 'ThumbnailLength',
        },
        # retrieve the thumbnail from our EXIF data
        RawConv => 'Image::ExifTool::Exif::ExtractImage($self,$val[0],$val[1],"ThumbnailImage")',
    },
    PreviewImage => {
        Writable => 1,
        WriteCheck => '$self->CheckImage(\$val)',
        DelCheck => '$val = ""; return undef', # can't delete, so set to empty string
        WriteAlso => {
            PreviewImageStart  => 'defined $val ? 0xfeedfeed : undef',
            PreviewImageLength => 'defined $val ? 0xfeedfeed : undef',
            PreviewImageValid  => 'defined $val and length $val ? 1 : 0',
        },
        Require => {
            0 => 'PreviewImageStart',
            1 => 'PreviewImageLength',
        },
        Desire => {
            2 => 'PreviewImageValid',
            # (DNG and A100 ARW may be have 2 preview images)
            3 => 'PreviewImageStart (1)',
            4 => 'PreviewImageLength (1)',
        },
        # note: extract 2nd preview, but ignore double-referenced preview
        # (in A100 ARW images, the 2nd PreviewImageLength from IFD0 may be wrong anyway)
        RawConv => q{
            if ($val[3] and $val[4] and $val[0] ne $val[3]) {
                my %val = (
                    0 => 'PreviewImageStart (1)',
                    1 => 'PreviewImageLength (1)',
                    2 => 'PreviewImageValid',
                );
                $self->FoundTag($tagInfo, \%val);
            }
            return undef if defined $val[2] and not $val[2];
            return Image::ExifTool::Exif::ExtractImage($self,$val[0],$val[1],'PreviewImage');
        },
    },
    JpgFromRaw => {
        Writable => 1,
        WriteCheck => '$self->CheckImage(\$val)',
        WriteAlso => {
            JpgFromRawStart  => 'defined $val ? 0xfeedfeed : undef',
            JpgFromRawLength => 'defined $val ? 0xfeedfeed : undef',
        },
        Require => {
            0 => 'JpgFromRawStart',
            1 => 'JpgFromRawLength',
        },
        RawConv => 'Image::ExifTool::Exif::ExtractImage($self,$val[0],$val[1],"JpgFromRaw")',
    },
    OtherImage => {
        Require => {
            0 => 'OtherImageStart',
            1 => 'OtherImageLength',
        },
        # retrieve the thumbnail from our EXIF data
        RawConv => 'Image::ExifTool::Exif::ExtractImage($self,$val[0],$val[1],"OtherImage")',
    },
    PreviewImageSize => {
        Require => {
            0 => 'PreviewImageWidth',
            1 => 'PreviewImageHeight',
        },
        ValueConv => '"$val[0]x$val[1]"',
    },
    SubSecDateTimeOriginal => {
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        Require => {
            0 => 'EXIF:DateTimeOriginal',
            1 => 'SubSecTimeOriginal',
        },
        # be careful here just in case there is a timezone following the seconds
        ValueConv => q{
            return undef if $val[1] eq '';
            $_ = $val[0]; s/( \d{2}:\d{2}:\d{2})/$1\.$val[1]/; $_;
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    SubSecCreateDate => {
        Description => 'Create Date',
        Groups => { 2 => 'Time' },
        Require => {
            0 => 'EXIF:CreateDate',
            1 => 'SubSecTimeDigitized',
        },
        ValueConv => q{
            return undef if $val[1] eq '';
            $_ = $val[0]; s/( \d{2}:\d{2}:\d{2})/$1\.$val[1]/; $_;
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    SubSecModifyDate => {
        Description => 'Modify Date',
        Groups => { 2 => 'Time' },
        Require => {
            0 => 'EXIF:ModifyDate',
            1 => 'SubSecTime',
        },
        ValueConv => q{
            return undef if $val[1] eq '';
            $_ = $val[0]; s/( \d{2}:\d{2}:\d{2})/$1\.$val[1]/; $_;
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
    CFAPattern => {
        Require => {
            0 => 'CFARepeatPatternDim',
            1 => 'CFAPattern2',
        },
        # generate CFAPattern
        ValueConv => q{
            my @a = split / /, $val[0];
            my @b = split / /, $val[1];
            return '?' unless @a==2 and @b==$a[0]*$a[1];
            return "$a[0] $a[1] @b";
        },
        PrintConv => 'Image::ExifTool::Exif::PrintCFAPattern($val)',
    },
    RedBalance => {
        Groups => { 2 => 'Camera' },
        Desire => {
            0 => 'WB_RGGBLevels',
            1 => 'WB_RGBGLevels',
            2 => 'WB_RBGGLevels',
            3 => 'WB_GRBGLevels',
            4 => 'WB_GRGBLevels',
            5 => 'WB_GBRGLevels',
            6 => 'WB_RGBLevels',
            7 => 'WB_RBLevels',
            8 => 'WBRedLevel', # red
            9 => 'WBGreenLevel',
        },
        ValueConv => 'Image::ExifTool::Exif::RedBlueBalance(0,@val)',
        PrintConv => 'int($val * 1e6 + 0.5) * 1e-6',
    },
    BlueBalance => {
        Groups => { 2 => 'Camera' },
        Desire => {
            0 => 'WB_RGGBLevels',
            1 => 'WB_RGBGLevels',
            2 => 'WB_RBGGLevels',
            3 => 'WB_GRBGLevels',
            4 => 'WB_GRGBLevels',
            5 => 'WB_GBRGLevels',
            6 => 'WB_RGBLevels',
            7 => 'WB_RBLevels',
            8 => 'WBBlueLevel', # blue
            9 => 'WBGreenLevel',
        },
        ValueConv => 'Image::ExifTool::Exif::RedBlueBalance(1,@val)',
        PrintConv => 'int($val * 1e6 + 0.5) * 1e-6',
    },
    GPSPosition => {
        Groups => { 2 => 'Location' },
        Require => {
            0 => 'GPSLatitude',
            1 => 'GPSLongitude',
        },
        ValueConv => '"$val[0] $val[1]"',
        PrintConv => '"$prt[0], $prt[1]"',
    },
    LensID => {
        Groups => { 2 => 'Camera' },
        Require => {
            0 => 'LensType',
        },
        Desire => {
            1 => 'FocalLength',
            2 => 'MaxAperture',
            3 => 'MaxApertureValue',
            4 => 'ShortFocal',
            5 => 'LongFocal',
            6 => 'LensModel',
            7 => 'LensFocalRange',
        },
        Notes => q{
            attempt to identify the actual lens from all lenses with a given LensType.
            Applies only to LensType values with a lookup table.  May be configured
            by adding user-defined lenses
        },
        # this LensID is only valid if the LensType has a PrintConv or is a model name
        ValueConv => q{
            return $val[0] if ref $$self{TAG_INFO}{LensType}{PrintConv} eq "HASH" or
                              $prt[0] =~ /(mm|\d\/F)/;
            return undef;
        },
        PrintConv => 'Image::ExifTool::Exif::PrintLensID($self, $prt[0], @val)',
    },
);

# table for unknown IFD entries
%Image::ExifTool::Exif::Unknown = (
    GROUPS => { 0 => 'EXIF', 1 => 'UnknownIFD', 2 => 'Image'},
    WRITE_PROC => \&WriteExif,
);

# add our composite tags
Image::ExifTool::AddCompositeTags('Image::ExifTool::Exif');


#------------------------------------------------------------------------------
# AutoLoad our writer routines when necessary
#
sub AUTOLOAD
{
    return Image::ExifTool::DoAutoLoad($AUTOLOAD, @_);
}

#------------------------------------------------------------------------------
# Identify RAW file type for some TIFF-based formats using Compression value
# Inputs: 0) ExifTool object reference, 1) Compression value
# - sets TIFF_TYPE and FileType if identified
sub IdentifyRawFile($$)
{
    my ($exifTool, $comp) = @_;
    if ($$exifTool{FILE_TYPE} eq 'TIFF' and not $$exifTool{IdentifiedRawFile}) {
        if ($compression{$comp} and $compression{$comp} =~ /^\w+ ([A-Z]{3}) Compressed$/) {
            $exifTool->OverrideFileType($$exifTool{TIFF_TYPE} = $1);
            $$exifTool{IdentifiedRawFile} = 1;
        }
    }
}

#------------------------------------------------------------------------------
# Calculate LV (Light Value)
# Inputs: 0) Aperture, 1) ShutterSpeed, 2) ISO
# Returns: LV value (and converts input values to floating point if necessary)
sub CalculateLV($$$)
{
    local $_;
    # do validity checks on arguments
    return undef unless @_ >= 3;
    foreach (@_) {
        return undef unless $_ and /([+-]?(?=\d|\.\d)\d*(\.\d*)?([Ee]([+-]?\d+))?)/ and $1 > 0;
        $_ = $1;    # extract float from any other garbage
    }
    # (A light value of 0 is defined as f/1.0 at 1 second with ISO 100)
    return (2*log($_[0]) - log($_[1]) - log($_[2]/100)) / log(2);
}

#------------------------------------------------------------------------------
# Calculate scale factor for 35mm effective focal length (ref 26/PH)
# Inputs: 0) Focal length
#         1) Focal length in 35mm format
#         2) Canon digital zoom factor
#         3) Focal plane diagonal size (in mm)
#         4/5) Focal plane X/Y size (in mm)
#         6) focal plane resolution units (1=None,2=inches,3=cm,4=mm,5=um)
#         7/8) Focal plane X/Y resolution
#         9/10,11/12...) Image width/height in order of precedence (first valid pair is used)
# Returns: 35mm conversion factor (or undefined if it can't be calculated)
sub CalcScaleFactor35efl
{
    my $res = $_[6];    # save resolution units (in case they have been converted to string)
    Image::ExifTool::ToFloat(@_);
    my $focal = shift;
    my $foc35 = shift;

    return $foc35 / $focal if $focal and $foc35;

    my $digz = shift || 1;
    my $diag = shift;
    unless ($diag and Image::ExifTool::IsFloat($diag)) {
        undef $diag;
        my $xsize = shift;
        my $ysize = shift;
        if ($xsize and $ysize) {
            # validate by checking aspect ratio because FocalPlaneX/YSize is not reliable
            my $a = $xsize / $ysize;
            if (abs($a-1.3333) < .1 or abs($a-1.5) < .1) {
                $diag = sqrt($xsize * $xsize + $ysize * $ysize);
            }
        }
        unless ($diag) {
            # get number of mm in units (assume inches unless otherwise specified)
            my %lkup = ( 3=>10, 4=>1, 5=>0.001 , cm=>10, mm=>1, um=>0.001 );
            my $units = $lkup{ shift() || $res || '' } || 25.4;
            my $x_res = shift || return undef;
            my $y_res = shift || return undef;
            Image::ExifTool::IsFloat($x_res) and $x_res != 0 or return undef;
            Image::ExifTool::IsFloat($y_res) and $y_res != 0 or return undef;
            my ($w, $h);
            for (;;) {
                @_ < 2 and return undef;
                $w = shift;
                $h = shift;
                next unless $w and $h;
                my $a = $w / $h;
                last if $a > 0.5 and $a < 2; # stop if we get a reasonable value
            }
            # calculate focal plane size in mm
            $w *= $units / $x_res;
            $h *= $units / $y_res;
            $diag = sqrt($w*$w+$h*$h);
            # make sure size is reasonable
            return undef unless $diag > 1 and $diag < 100;
        }
    }
    return sqrt(36*36+24*24) * $digz / $diag;
}

#------------------------------------------------------------------------------
# Print exposure compensation fraction
sub PrintFraction($)
{
    my $val = shift;
    my $str;
    if (defined $val) {
        $val *= 1.00001;    # avoid round-off errors
        if (not $val) {
            $str = '0';
        } elsif (int($val)/$val > 0.999) {
            $str = sprintf("%+d", int($val));
        } elsif ((int($val*2))/($val*2) > 0.999) {
            $str = sprintf("%+d/2", int($val * 2));
        } elsif ((int($val*3))/($val*3) > 0.999) {
            $str = sprintf("%+d/3", int($val * 3));
        } else {
            $str = sprintf("%+.3g", $val);
        }
    }
    return $str;
}

#------------------------------------------------------------------------------
# Convert fraction or number to floating point value (or 'undef' or 'inf')
sub ConvertFraction($)
{
    my $val = shift;
    if ($val =~ m{([-+]?\d+)/(\d+)}) {
        $val = $2 ? $1 / $2 : ($1 ? 'inf' : 'undef');
    }
    return $val;
}

#------------------------------------------------------------------------------
# Convert EXIF text to something readable
# Inputs: 0) ExifTool object reference, 1) EXIF text
# Returns: text encoded according to Charset option (with trailing spaces removed)
sub ConvertExifText($$)
{
    my ($exifTool, $val) = @_;
    return $val if length($val) < 8;
    my $id = substr($val, 0, 8);
    my $str = substr($val, 8);
    # Note: allow spaces instead of nulls in the ID codes because
    # it is fairly common for camera manufacturers to get this wrong
    if ($id =~ /^(ASCII)?[\0 ]+$/) {
        # truncate at null terminator (shouldn't have a null based on the
        # EXIF spec, but it seems that few people actually read the spec)
        $str =~ s/\0.*//s;
    # by the EXIF spec, the following string should be "UNICODE\0", but
    # apparently Kodak sometimes uses "Unicode\0" in the APP3 "Meta" information.
    # However, unfortunately Ricoh uses "Unicode\0" in the RR30 EXIF UserComment
    # when the text is actually ASCII, so only recognize uppercase "UNICODE\0".
    } elsif ($id =~ /^UNICODE[\0 ]$/) {
        # MicrosoftPhoto writes as little-endian even in big-endian EXIF,
        # so we must guess at the true byte ordering
        $str = $exifTool->Decode($str, 'UCS2', 'Unknown');
    } elsif ($id =~ /^JIS[\0 ]{5}$/) {
        $str = $exifTool->Decode($str, 'JIS', 'Unknown');
    } else {
        $exifTool->Warn("Invalid EXIF text encoding");
        $str = $id . $str;
    }
    $str =~ s/ +$//;    # trim trailing blanks
    return $str;
}

#------------------------------------------------------------------------------
# Print conversion for SpatialFrequencyResponse
sub PrintSFR($)
{
    my $val = shift;
    return $val unless length $val > 4;
    my ($n, $m) = (Get16u(\$val, 0), Get16u(\$val, 2));
    my @cols = split /\0/, substr($val, 4), $n+1;
    my $pos = length($val) - 8 * $n * $m;
    return $val unless @cols == $n+1 and $pos >= 4;
    pop @cols;
    my ($i, $j);
    for ($i=0; $i<$n; ++$i) {
        my @rows;
        for ($j=0; $j<$m; ++$j) {
            push @rows, Image::ExifTool::GetRational64u(\$val, $pos + 8*($i+$j*$n));
        }
        $cols[$i] .= '=' . join(',',@rows) . '';
    }
    return join '; ', @cols;
}

#------------------------------------------------------------------------------
# Print numerical parameter value (with sign, or 'Normal' for zero)
# Inputs: 0) value, 1) flag for inverse conversion, 2) conversion hash reference
sub PrintParameter($$$)
{
    my ($val, $inv, $conv) = @_;
    return $val if $inv;
    if ($val > 0) {
        if ($val > 0xfff0) {    # a negative value in disguise?
            $val = $val - 0x10000;
        } else {
            $val = "+$val";
        }
    }
    return $val;
}

#------------------------------------------------------------------------------
# Convert parameter back to standard EXIF value
#   0,0.00,etc or "Normal" => 0
#   -1,-2,etc or "Soft" or "Low" => 1
#   +1,+2,1,2,etc or "Hard" or "High" => 2
sub ConvertParameter($)
{
    my $val = shift;
    my $isFloat = Image::ExifTool::IsFloat($val);
    # normal is a value of zero
    return 0 if $val =~ /\bn/i or ($isFloat and $val == 0);
    # "soft", "low" or any negative number is a value of 1
    return 1 if $val =~ /\b(s|l)/i or ($isFloat and $val < 0);
    # "hard", "high" or any positive number is a value of 2
    return 2 if $val =~ /\bh/i or $isFloat;
    return undef;
}

#------------------------------------------------------------------------------
# Calculate Red/BlueBalance
# Inputs: 0) 0=red, 1=blue, 1-7) WB_RGGB/RGBG/RBGG/GRBG/GRGB/RGB/RBLevels,
#         8) red or blue level, 9) green level
my @rggbLookup = (
    # indices for R, G, G and B components in input value
    [ 0, 1, 2, 3 ], # 0 RGGB
    [ 0, 1, 3, 2 ], # 1 RGBG
    [ 0, 2, 3, 1 ], # 2 RBGG
    [ 1, 0, 3, 2 ], # 3 GRBG
    [ 1, 0, 2, 3 ], # 4 GRGB
    [ 2, 3, 0, 1 ], # 5 GBRG
    [ 0, 1, 1, 2 ], # 6 RGB
    [ 0, 256, 256, 1 ], # 7 RB (green level is 256)
);
sub RedBlueBalance($@)
{
    my $blue = shift;
    my ($i, $val, $levels);
    for ($i=0; $i<@rggbLookup; ++$i) {
        $levels = shift or next;
        my @levels = split ' ', $levels;
        next if @levels < 2;
        my $lookup = $rggbLookup[$i];
        my $g = $$lookup[1];    # get green level or index
        if ($g < 4) {
            next if @levels < 3;
            $g = ($levels[$g] + $levels[$$lookup[2]]) / 2 or next;
        } elsif ($levels[$$lookup[$blue * 3]] < 4) {
            $g = 1; # Some Nikon cameras use a scaling factor of 1 (E5700)
        }
        $val = $levels[$$lookup[$blue * 3]] / $g;
        last;
    }
    $val = $_[0] / $_[1] if not defined $val and ($_[0] and $_[1]);
    return $val;
}

#------------------------------------------------------------------------------
# Print exposure time as a fraction
sub PrintExposureTime($)
{
    my $secs = shift;
    if ($secs < 0.25001 and $secs > 0) {
        return sprintf("1/%d",int(0.5 + 1/$secs));
    }
    $_ = sprintf("%.1f",$secs);
    s/\.0$//;
    return $_;
}

#------------------------------------------------------------------------------
# Decode raw CFAPattern value
# Inputs: 0) ExifTool ref, 1) binary value
# Returns: string of numbers
sub DecodeCFAPattern($$)
{
    my ($self, $val) = @_;
    # some panasonic cameras (SV-AS3, SV-AS30) write this in ascii (very odd)
    if ($val =~ /^[0-6]+$/) {
        $self->Warn('Incorrectly formatted CFAPattern', 1);
        $val =~ tr/0-6/\x00-\x06/;
    }
    return $val unless length($val) >= 4;
    my @a = unpack(GetByteOrder() eq 'II' ? 'v2C*' : 'n2C*', $val);
    my $end = 2 + $a[0] * $a[1];
    if ($end > @a) {
        # try swapping byte order (I have seen this order different than in EXIF)
        my ($x, $y) = unpack('n2',pack('v2',$a[0],$a[1]));
        if (@a < 2 + $x * $y) {
            $self->Warn('Invalid CFAPattern', 1);
        } else {
            ($a[0], $a[1]) = ($x, $y);
            # (can't technically be wrong because the order isn't well defined by the EXIF spec)
            # $self->Warn('Wrong byte order for CFAPattern');
        }
    }
    return "@a";
}

#------------------------------------------------------------------------------
# Print CFA Pattern
sub PrintCFAPattern($)
{
    my $val = shift;
    my @a = split ' ', $val;
    return '<truncated data>' unless @a >= 2;
    return '<zero pattern size>' unless $a[0] and $a[1];
    my $end = 2 + $a[0] * $a[1];
    return '<invalid pattern size>' if $end > @a;
    my @cfaColor = qw(Red Green Blue Cyan Magenta Yellow White);
    my ($pos, $rtnVal) = (2, '[');
    for (;;) {
        $rtnVal .= $cfaColor[$a[$pos]] || 'Unknown';
        last if ++$pos >= $end;
        ($pos - 2) % $a[1] and $rtnVal .= ',', next;
        $rtnVal .= '][';
    }
    return $rtnVal . ']';
}

#------------------------------------------------------------------------------
# Print conversion for lens info
# Inputs: 0) string of values (min focal, max focal, min F, max F)
# Returns: string in the form "12-20mm f/3.8-4.5" or "50mm f/1.4"
sub PrintLensInfo($)
{
    my $val = shift;
    my @vals = split ' ', $val;
    return $val unless @vals == 4;
    my $c = 0;
    foreach (@vals) {
        Image::ExifTool::IsFloat($_) and ++$c, next;
        $_ eq 'inf' and $_ = '?', ++$c, next;
        $_ eq 'undef' and $_ = '?', ++$c, next;
    }
    return $val unless $c == 4;
    $val = "$vals[1]mm f/$vals[2]";
    $val = "$vals[0]-$val" if $vals[0] ne $vals[1];
    $val .= "-$vals[3]" if $vals[3] ne $vals[2];
    return $val;
}

#------------------------------------------------------------------------------
# Get lens info from lens model string
# Inputs: 0) lens string, 1) flag to allow unknown "?" values
# Returns: 0) min focal, 1) max focal, 2) min aperture, 3) max aperture
# Notes: returns empty list if lens string could not be parsed
sub GetLensInfo($;$)
{
    my ($lens, $unk) = @_;
    # extract focal length and aperture ranges for this lens
    my $pat = '\\d+(?:\\.\\d+)?';
    $pat .= '|\\?' if $unk;
    return () unless $lens =~ /($pat)(?:-($pat))?\s*mm.*?(?:[fF]\/?\s*)($pat)(?:-($pat))?/;
    # ($1=short focal, $2=long focal, $3=max aperture wide, $4=max aperture tele)
    my @a = ($1, $2, $3, $4);
    $a[1] or $a[1] = $a[0];
    $a[3] or $a[3] = $a[2];
    if ($unk) {
        local $_;
        $_ eq '?' and $_ = 'undef' foreach @a;
    }
    return @a;
}

#------------------------------------------------------------------------------
# Attempt to identify the specific lens if multiple lenses have the same LensType
# Inputs: 0) ExifTool object ref, 1) LensType string, 2) LensType value,
#         3) FocalLength, 4) MaxAperture, 5) MaxApertureValue, 6) ShortFocal,
#         7) LongFocal, 8) LensModel, 9) LensFocalRange, 10) optional PrintConv hash ref
sub PrintLensID($$@)
{
    my ($exifTool, $lensTypePrt, $lensType, $focalLength, $maxAperture,
        $maxApertureValue, $shortFocal, $longFocal, $lensModel, $lensFocalRange,
        $printConv) = @_;
    # the rest of the logic relies on the LensType lookup:
    return undef unless defined $lensType;
    # get print conversion hash if necessary
    $printConv or $printConv = $exifTool->{TAG_INFO}{LensType}{PrintConv};
    # just copy LensType PrintConv value if it was a lens name
    # (Olympus or Panasonic -- just exclude things like Nikon and Leaf LensType)
    unless (ref $printConv eq 'HASH') {
        return $lensTypePrt if $lensTypePrt =~ /mm/;
        return $lensTypePrt if $lensTypePrt =~ s/(\d)\/F/$1mm F/;
        return undef;
    }
    # use MaxApertureValue if MaxAperture is not available
    $maxAperture = $maxApertureValue unless $maxAperture;
    if ($lensFocalRange and $lensFocalRange =~ /^(\d+)(?: to (\d+))?$/) {
        ($shortFocal, $longFocal) = ($1, $2 || $1);
    }
    if ($shortFocal and $longFocal) {
        # Canon includes makernote information which allows better lens identification
        require Image::ExifTool::Canon;
        return Image::ExifTool::Canon::PrintLensID($printConv, $lensType,
                    $shortFocal, $longFocal, $maxAperture, $lensModel);
    }
    my $lens = $$printConv{$lensType};
    return ($lensModel || $lensTypePrt) unless $lens;
    return $lens unless $$printConv{"$lensType.1"};
    $lens =~ s/ or .*//s;    # remove everything after "or"
    # make list of all possible matching lenses
    my @lenses = ( $lens );
    my $i;
    for ($i=1; $$printConv{"$lensType.$i"}; ++$i) {
        push @lenses, $$printConv{"$lensType.$i"};
    }
    # attempt to determine actual lens
    my (@matches, @best, @user, $diff);
    foreach $lens (@lenses) {
        if ($Image::ExifTool::userLens{$lens}) {
            push @user, $lens;
            next;
        }
        my ($sf, $lf, $sa, $la) = GetLensInfo($lens);
        next unless $sf;
        # see if we can rule out this lens using FocalLength and MaxAperture
        if ($focalLength) {
            next if $focalLength < $sf - 0.5;
            next if $focalLength > $lf + 0.5;
        }
        if ($maxAperture) {
            # it seems that most manufacturers set MaxAperture and MaxApertureValue
            # to the maximum aperture (smallest F number) for the current focal length
            # of the lens, so assume that MaxAperture varies with focal length and find
            # the closest match (this is somewhat contrary to the EXIF specification which
            # states "The smallest F number of the lens", without mention of focal length)
            next if $maxAperture < $sa - 0.15;  # (0.15 is arbitrary)
            next if $maxAperture > $la + 0.15;
            # now determine the best match for this aperture
            my $aa; # approximate maximum aperture at this focal length
            if ($sf == $lf or $sa == $la) {
                $aa = $sa;
            } else {
                # assume a log-log variation of max aperture with focal length
                # (see http://regex.info/blog/2006-10-05/263)
                $aa = exp(log($sa) + (log($la)-log($sa)) / (log($lf)-log($sf)) *
                                     (log($focalLength)-log($sf)));
            }
            my $d = abs($maxAperture - $aa);
            if (defined $diff) {
                $d > $diff + 0.15 and next;     # (0.15 is arbitrary)
                $d < $diff - 0.15 and undef @best;
            }
            $diff = $d;
            push @best, $lens;
        }
        push @matches, $lens;
    }
    return join(' or ', @user) if @user;
    return join(' or ', @best) if @best;
    return join(' or ', @matches) if @matches;
    $lens = $$printConv{$lensType};
    return $lensModel if $lensModel and $lens =~ / or /; # (ie. Sony NEX-5N)
    return $lens;
}

#------------------------------------------------------------------------------
# translate date into standard EXIF format
# Inputs: 0) date
# Returns: date in format '2003:10:22'
# - bad formats recognized: '2003-10-22','2003/10/22','2003 10 22','20031022'
# - removes null terminator if it exists
sub ExifDate($)
{
    my $date = shift;
    $date =~ s/\0$//;       # remove any null terminator
    # separate year:month:day with colons
    # (have seen many other characters, including nulls, used erroneously)
    $date =~ s/(\d{4})[^\d]*(\d{2})[^\d]*(\d{2})$/$1:$2:$3/;
    return $date;
}

#------------------------------------------------------------------------------
# translate time into standard EXIF format
# Inputs: 0) time
# Returns: time in format '10:30:55'
# - bad formats recognized: '10 30 55', '103055', '103055+0500'
# - removes null terminator if it exists
# - leaves time zone intact if specified (ie. '10:30:55+05:00')
sub ExifTime($)
{
    my $time = shift;
    $time =~ tr/ /:/;   # use ':' (not ' ') as a separator
    $time =~ s/\0$//;   # remove any null terminator
    # add separators if they don't exist
    $time =~ s/^(\d{2})(\d{2})(\d{2})/$1:$2:$3/;
    $time =~ s/([+-]\d{2})(\d{2})\s*$/$1:$2/;   # to timezone too
    return $time;
}

#------------------------------------------------------------------------------
# extract image from file
# Inputs: 0) ExifTool object reference, 1) data offset (in file), 2) data length
#         3) [optional] tag name
# Returns: Reference to Image if specifically requested or "Binary data" message
#          Returns undef if there was an error loading the image
sub ExtractImage($$$$)
{
    my ($exifTool, $offset, $len, $tag) = @_;
    my $dataPt = \$exifTool->{EXIF_DATA};
    my $dataPos = $exifTool->{EXIF_POS};
    my $image;

    # no image if length is zero, and don't try to extract binary from XMP file
    return undef if not $len or $$exifTool{FILE_TYPE} eq 'XMP';

    # take data from EXIF block if possible
    if (defined $dataPos and $offset>=$dataPos and $offset+$len<=$dataPos+length($$dataPt)) {
        $image = substr($$dataPt, $offset-$dataPos, $len);
    } else {
        $image = $exifTool->ExtractBinary($offset, $len, $tag);
        return undef unless defined $image;
        # patch for incorrect ThumbnailOffset in some Sony DSLR-A100 ARW images
        if ($tag and $tag eq 'ThumbnailImage' and $$exifTool{TIFF_TYPE} eq 'ARW' and
            $$exifTool{Model} eq 'DSLR-A100' and $offset < 0x10000 and
            $image !~ /^(Binary data|\xff\xd8\xff)/)
        {
            my $try = $exifTool->ExtractBinary($offset + 0x10000, $len, $tag);
            if (defined $try and $try =~ /^\xff\xd8\xff/) {
                $image = $try;
                $exifTool->{VALUE}->{ThumbnailOffset} += 0x10000;
                $exifTool->Warn('Adjusted incorrect A100 ThumbnailOffset', 1);
            }
        }
    }
    return $exifTool->ValidateImage(\$image, $tag);
}

#------------------------------------------------------------------------------
# Process EXIF directory
# Inputs: 0) ExifTool object reference
#         1) Reference to directory information hash
#         2) Pointer to tag table for this directory
# Returns: 1 on success, otherwise returns 0 and sets a Warning
sub ProcessExif($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $dataPos = $$dirInfo{DataPos} || 0;
    my $dataLen = $$dirInfo{DataLen};
    my $dirStart = $$dirInfo{DirStart} || 0;
    my $dirLen = $$dirInfo{DirLen} || $dataLen - $dirStart;
    my $dirName = $$dirInfo{DirName};
    my $base = $$dirInfo{Base} || 0;
    my $firstBase = $base;
    my $raf = $$dirInfo{RAF};
    my $verbose = $exifTool->Options('Verbose');
    my $htmlDump = $exifTool->{HTML_DUMP};
    my $success = 1;
    my ($tagKey, $dirSize, $makerAddr);
    my $inMakerNotes = $tagTablePtr->{GROUPS}{0} eq 'MakerNotes';

    # ignore non-standard EXIF while in strict MWG compatibility mode
    if ($Image::ExifTool::MWG::strict and $dirName eq 'IFD0' and
        $tagTablePtr eq \%Image::ExifTool::Exif::Main and
        $$exifTool{FILE_TYPE} =~ /^(JPEG|TIFF|PSD)$/)
    {
        my $path = $exifTool->MetadataPath();
        unless ($path =~ /^(JPEG-APP1-IFD0|TIFF-IFD0|PSD-EXIFInfo-IFD0)$/) {
            $exifTool->Warn("Ignored non-standard EXIF at $path");
            return 1;
        }
    }
    $verbose = -1 if $htmlDump; # mix htmlDump into verbose so we can test for both at once
    $dirName eq 'EXIF' and $dirName = $$dirInfo{DirName} = 'IFD0';
    $$dirInfo{Multi} = 1 if $dirName =~ /^(IFD0|SubIFD)$/ and not defined $$dirInfo{Multi};
    # get a more descriptive name for MakerNote sub-directories
    my $name = $$dirInfo{Name};
    $name = $dirName unless $name and $inMakerNotes and $name !~ /^MakerNote/;

    my ($numEntries, $dirEnd);
    if ($dirStart >= 0 and $dirStart <= $dataLen-2) {
        # make sure data is large enough (patches bug in Olympus subdirectory lengths)
        $numEntries = Get16u($dataPt, $dirStart);
        $dirSize = 2 + 12 * $numEntries;
        $dirEnd = $dirStart + $dirSize;
        if ($dirSize > $dirLen) {
            if ($verbose > 0 and not $$dirInfo{SubIFD}) {
                my $short = $dirSize - $dirLen;
                $exifTool->Warn("Short directory size (missing $short bytes)");
            }
            undef $dirSize if $dirEnd > $dataLen; # read from file if necessary
        }
    }
    # read IFD from file if necessary
    unless ($dirSize) {
        $success = 0;
        if ($raf) {
            # read the count of entries in this IFD
            my $offset = $dirStart + $dataPos;
            my ($buff, $buf2);
            if ($raf->Seek($offset + $base, 0) and $raf->Read($buff,2) == 2) {
                my $len = 12 * Get16u(\$buff,0);
                # also read next IFD pointer if available
                if ($raf->Read($buf2, $len+4) >= $len) {
                    $buff .= $buf2;
                    # make copy of dirInfo since we're going to modify it
                    my %newDirInfo = %$dirInfo;
                    $dirInfo = \%newDirInfo;
                    # update directory parameters for the newly loaded IFD
                    $dataPt = $$dirInfo{DataPt} = \$buff;
                    $dataPos = $$dirInfo{DataPos} = $offset;
                    $dataLen = $$dirInfo{DataLen} = length $buff;
                    $dirStart = $$dirInfo{DirStart} = 0;
                    $dirLen = $$dirInfo{DirLen} = length $buff;
                    $success = 1;
                }
            }
        }
        unless ($success) {
            $exifTool->Warn("Bad $name directory");
            return 0;
        }
        $numEntries = Get16u($dataPt, $dirStart);
        $dirSize = 2 + 12 * $numEntries;
        $dirEnd = $dirStart + $dirSize;
    }
    $verbose > 0 and $exifTool->VerboseDir($dirName, $numEntries);
    my $bytesFromEnd = $dataLen - $dirEnd;
    if ($bytesFromEnd < 4) {
        unless ($bytesFromEnd==2 or $bytesFromEnd==0) {
            $exifTool->Warn(sprintf"Illegal $name directory size (0x%x entries)",$numEntries);
            return 0;
        }
    }
    # fix base offset for maker notes if necessary
    if (defined $$dirInfo{MakerNoteAddr}) {
        $makerAddr = $$dirInfo{MakerNoteAddr};
        delete $$dirInfo{MakerNoteAddr};
        if (Image::ExifTool::MakerNotes::FixBase($exifTool, $dirInfo)) {
            $base = $$dirInfo{Base};
            $dataPos = $$dirInfo{DataPos};
        }
    }
    if ($htmlDump) {
        my $longName = $name eq 'MakerNotes' ? ($$dirInfo{Name} || $name) : $name;
        if (defined $makerAddr) {
            my $hdrLen = $dirStart + $dataPos + $base - $makerAddr;
            $exifTool->HDump($makerAddr, $hdrLen, "MakerNotes header", $longName) if $hdrLen > 0;
        }
        $exifTool->HDump($dirStart + $dataPos + $base, 2, "$longName entries",
                 "Entry count: $numEntries");
        my $tip;
        if ($bytesFromEnd >= 4) {
            my $nxt = ($name =~ /^(.*?)(\d+)$/) ? $1 . ($2 + 1) : 'Next IFD';
            $tip = sprintf("$nxt offset: 0x%.4x", Get32u($dataPt, $dirEnd));
        }
        $exifTool->HDump($dirEnd + $dataPos + $base, 4, "Next IFD", $tip, 0);
    }

    # patch for Canon EOS 40D firmware 1.0.4 bug (incorrect directory counts)
    # (must do this before parsing directory or CameraSettings offset will be suspicious)
    if ($inMakerNotes and $$exifTool{Model} eq 'Canon EOS 40D') {
        my $entry = $dirStart + 2 + 12 * ($numEntries - 1);
        my $fmt = Get16u($dataPt, $entry + 2);
        if ($fmt < 1 or $fmt > 13) {
            $exifTool->HDump($entry+$dataPos+$base,12,"[invalid IFD entry]",
                     "Bad format type: $fmt", 1);
            # adjust the number of directory entries
            --$numEntries;
            $dirEnd -= 12;
        }
    }

    # loop through all entries in an EXIF directory (IFD)
    my ($index, $valEnd, $offList, $offHash);
    my $warnCount = 0;
    for ($index=0; $index<$numEntries; ++$index) {
        if ($warnCount > 10) {
            $exifTool->Warn("Too many warnings -- $name parsing aborted", 1) and return 0;
        }
        my $entry = $dirStart + 2 + 12 * $index;
        my $tagID = Get16u($dataPt, $entry);
        my $format = Get16u($dataPt, $entry+2);
        my $count = Get32u($dataPt, $entry+4);
        if ($format < 1 or $format > 13) {
            $exifTool->HDump($entry+$dataPos+$base,12,"[invalid IFD entry]",
                     "Bad format type: $format", 1);
            # warn unless the IFD was just padded with zeros
            if ($format) {
                $exifTool->Warn(sprintf("Unknown format ($format) for $name tag 0x%x",$tagID));
                ++$warnCount;
            }
            return 0 unless $index; # assume corrupted IFD if this is our first entry
            next;
        }
        my $formatStr = $formatName[$format];   # get name of this format
        my $valueDataPt = $dataPt;
        my $valueDataPos = $dataPos;
        my $valueDataLen = $dataLen;
        my $valuePtr = $entry + 8;      # pointer to value within $$dataPt
        my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tagID);
        my $origFormStr;
        # hack to patch incorrect count in Kodak SubIFD3 tags
        if ($count < 2 and ref $$tagTablePtr{$tagID} eq 'HASH' and $$tagTablePtr{$tagID}{FixCount}) {
            $offList or ($offList, $offHash) = GetOffList($dataPt, $dirStart, $dataPos,
                                                          $numEntries, $tagTablePtr);
            my $i = $$offHash{Get32u($dataPt, $valuePtr)};
            if (defined $i and $i < $#$offList) {
                my $oldCount = $count;
                $count = int(($$offList[$i+1] - $$offList[$i]) / $formatSize[$format]);
                $origFormStr = $formatName[$format] . '[' . $oldCount . ']' if $oldCount != $count;
            }
        }
        my $size = $count * $formatSize[$format];
        my $readSize = $size;
        if ($size > 4) {
            if ($size > 0x7fffffff) {
                $exifTool->Warn(sprintf("Invalid size ($size) for $name tag 0x%x",$tagID));
                ++$warnCount;
                next;
            }
            $valuePtr = Get32u($dataPt, $valuePtr);
            # fix valuePtr if necessary
            if ($$dirInfo{FixOffsets}) {
                my $wFlag;
                $valEnd or $valEnd = $dataPos + $dirEnd + 4;
                #### eval FixOffsets ($valuePtr, $valEnd, $size, $tagID, $wFlag)
                eval $$dirInfo{FixOffsets};
            }
            my $suspect;
            # offset shouldn't point into TIFF header
            $valuePtr < 8 and $suspect = $warnCount;
            # convert offset to pointer in $$dataPt
            if ($$dirInfo{EntryBased} or (ref $$tagTablePtr{$tagID} eq 'HASH' and
                $tagTablePtr->{$tagID}{EntryBased}))
            {
                $valuePtr += $entry;
            } else {
                $valuePtr -= $dataPos;
            }
            # value shouldn't overlap our directory
            $suspect = $warnCount if $valuePtr < $dirEnd and $valuePtr+$size > $dirStart;
            # load value from file if necessary
            if ($valuePtr < 0 or $valuePtr+$size > $dataLen) {
                # get value by seeking in file if we are allowed
                my $buff;
                if ($raf) {
                    # avoid loading large binary data unless necessary
                    # (ie. ImageSourceData -- layers in Photoshop TIFF image)
                    while ($size > BINARY_DATA_LIMIT) {
                        if ($tagInfo) {
                            # make large unknown blocks binary data
                            $$tagInfo{Binary} = 1 if $$tagInfo{Unknown};
                            last unless $$tagInfo{Binary};      # must read non-binary data
                            last if $$tagInfo{SubDirectory};    # must read SubDirectory data
                            if ($exifTool->{OPTIONS}{Binary}) {
                                # read binary data if specified unless tagsFromFile won't use it
                                last unless $$exifTool{TAGS_FROM_FILE} and $$tagInfo{Protected};
                            }
                            # must read if tag is specified by name
                            last if $exifTool->{REQ_TAG_LOOKUP}{lc($$tagInfo{Name})};
                        } else {
                            # must read value if needed for a condition
                            last if defined $tagInfo;
                        }
                        # (note: changing the value without changing $size will cause
                        # a warning in the verbose output, but we need to maintain the
                        # proper size for the htmlDump, so we can't change this)
                        $buff = "Binary data $size bytes";
                        $readSize = length $buff;
                        last;
                    }
                    # read from file if necessary
                    unless (defined $buff or
                            ($raf->Seek($base + $valuePtr + $dataPos,0) and
                             $raf->Read($buff,$size) == $size))
                    {
                        $exifTool->Warn("Error reading value for $name entry $index", $inMakerNotes);
                        return 0 unless $inMakerNotes;
                        ++$warnCount;
                        $buff = '' unless defined $buff;
                        $readSize = length $buff;
                    }
                    $valueDataPt = \$buff;
                    $valueDataPos = $valuePtr + $dataPos;
                    $valueDataLen = length $buff;
                    $valuePtr = 0;
                } else {
                    my ($tagStr, $tmpInfo);
                    if ($tagInfo) {
                        $tagStr = $$tagInfo{Name};
                    } elsif (defined $tagInfo) {
                        $tmpInfo = $exifTool->GetTagInfo($tagTablePtr, $tagID, \ '', $formatStr, $count);
                        $tagStr = $$tmpInfo{Name} if $tmpInfo;
                    }
                    if ($tagInfo and $$tagInfo{ChangeBase}) {
                        # adjust base offset for this tag only
                        #### eval ChangeBase ($dirStart,$dataPos)
                        my $newBase = eval $$tagInfo{ChangeBase};
                        $valuePtr += $newBase;
                    }
                    $tagStr or $tagStr = sprintf("tag 0x%x",$tagID);
                    # allow PreviewImage to run outside EXIF data
                    if ($tagStr eq 'PreviewImage' and $exifTool->{RAF}) {
                        my $pos = $exifTool->{RAF}->Tell();
                        $buff = $exifTool->ExtractBinary($base + $valuePtr + $dataPos, $size, 'PreviewImage');
                        $exifTool->{RAF}->Seek($pos, 0);
                        $valueDataPt = \$buff;
                        $valueDataPos = $valuePtr + $dataPos;
                        $valueDataLen = $size;
                        $valuePtr = 0;
                    } elsif ($tagStr eq 'MakerNoteLeica6' and $exifTool->{RAF}) {
                        if ($verbose > 0) {
                            $exifTool->VPrint(0, "$$exifTool{INDENT}$index) $tagStr --> (outside APP1 segment)\n");
                        }
                        if ($exifTool->Options('FastScan')) {
                            $exifTool->Warn('Ignored Leica MakerNote trailer');
                        } else {
                            $$exifTool{LeicaTrailer} = {
                                TagInfo => $tagInfo || $tmpInfo,
                                Offset  => $base + $valuePtr + $dataPos,
                                Size    => $size,
                            };
                        }
                    } else {
                        $exifTool->Warn("Bad $name offset for $tagStr");
                        ++$warnCount;
                    }
                    unless (defined $buff) {
                        next unless $htmlDump and $size < 1000000;
                        $valueDataPt = \ (' ' x $size);
                        $valueDataPos = $valuePtr + $dataPos;
                        $valueDataLen = -1; # flag the bad pointer
                        $valuePtr = 0;
                    }
                }
            }
            # warn about suspect offsets if they didn't already cause another warning
            if (defined $suspect and $suspect == $warnCount) {
                my $tagStr = $tagInfo ? $$tagInfo{Name} : sprintf('tag 0x%.4x', $tagID);
                if ($exifTool->Warn("Suspicious $name offset for $tagStr", $inMakerNotes)) {
                    ++$warnCount;
                    next unless $verbose;
                }
            }
        }
        # treat single unknown byte as int8u
        $formatStr = 'int8u' if $format == 7 and $count == 1;

        my ($val, $subdir, $wrongFormat);
        if ($tagID > 0xf000 and $tagTablePtr eq \%Image::ExifTool::Exif::Main) {
            my $oldInfo = $$tagTablePtr{$tagID};
            if (not $oldInfo or (ref $oldInfo eq 'HASH' and $$oldInfo{Condition} and
                not $$oldInfo{PSRaw}))
            {
                # handle special case of Photoshop RAW tags (0xfde8-0xfe58)
                # --> generate tags from the value if possible
                $val = ReadValue($valueDataPt,$valuePtr,$formatStr,$count,$readSize);
                if (defined $val and $val =~ /(.*): (.*)/) {
                    my $tag = $1;
                    $val = $2;
                    $tag =~ s/'s//; # remove 's (so "Owner's Name" becomes "OwnerName")
                    $tag =~ tr/a-zA-Z0-9_//cd; # remove unknown characters
                    if ($tag) {
                        $tagInfo = {
                            Name => $tag,
                            Condition => '$$self{TIFF_TYPE} ne "DCR"',
                            ValueConv => '$_=$val;s/.*: //;$_', # remove descr
                            PSRaw => 1, # (just as flag to avoid adding this again)
                        };
                        Image::ExifTool::AddTagToTable($tagTablePtr, $tagID, $tagInfo);
                        # generate conditional list if a conditional tag already existed
                        $$tagTablePtr{$tagID} = [ $oldInfo, $tagInfo ] if $oldInfo;
                    }
                }
            }
        }
        if (defined $tagInfo and not $tagInfo) {
            # GetTagInfo() required the value for a Condition
            my $tmpVal = substr($$valueDataPt, $valuePtr, $readSize < 128 ? $readSize : 128);
            # (use original format name in this call -- $formatStr may have been changed to int8u)
            $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $tagID, \$tmpVal,
                                             $formatName[$format], $count);
        }
        # make sure we are handling the 'ifd' format properly
        if (($format == 13 or $format == 18) and (not $tagInfo or not $$tagInfo{SubIFD})) {
            my $str = sprintf('%s tag 0x%.4x IFD format not handled', $dirName, $tagID);
            $exifTool->Warn($str, $inMakerNotes);
        }
        if (defined $tagInfo) {
            $subdir = $$tagInfo{SubDirectory};
            # override EXIF format if specified
            if ($$tagInfo{Format}) {
                $formatStr = $$tagInfo{Format};
                my $newNum = $formatNumber{$formatStr};
                if ($newNum and $newNum != $format) {
                    $origFormStr = $formatName[$format] . '[' . $count . ']';
                    $format = $newNum;
                    # adjust number of items for new format size
                    $count = int($size / $formatSize[$format]);
                }
            }
            # verify that offset-type values are integral
            if (($$tagInfo{IsOffset} or $$tagInfo{SubIFD}) and not $intFormat{$formatStr}) {
                $exifTool->Warn("Wrong format ($formatStr) for $name $$tagInfo{Name}");
                next unless $verbose;
                $wrongFormat = 1;
            }
        } else {
            next unless $verbose;
        }
        # convert according to specified format
        $val = ReadValue($valueDataPt,$valuePtr,$formatStr,$count,$readSize);

        if ($verbose) {
            my $tval = $val;
            if ($formatStr =~ /^rational64([su])$/ and defined $tval) {
                # show numerator/denominator separately
                my $f = ReadValue($valueDataPt,$valuePtr,"int32$1",$count*2,$readSize);
                $f =~ s/(-?\d+) (-?\d+)/$1\/$2/g;
                $tval .= " ($f)";
            }
            if ($htmlDump) {
                my ($tagName, $colName);
                if ($tagID == 0x927c and $dirName eq 'ExifIFD') {
                    $tagName = 'MakerNotes';
                } elsif ($tagInfo) {
                    $tagName = $$tagInfo{Name};
                } else {
                    $tagName = sprintf("Tag 0x%.4x",$tagID);
                }
                my $dname = sprintf("${name}-%.2d", $index);
                # build our tool tip
                $size < 0 and $size = $count * $formatSize[$format];
                my $fstr = "$formatName[$format]\[$count]";
                $fstr = "$origFormStr read as $fstr" if $origFormStr and $origFormStr ne $fstr;
                $fstr .= ' <-- WRONG' if $wrongFormat;
                my $tip = sprintf("Tag ID: 0x%.4x\n", $tagID) .
                          "Format: $fstr\nSize: $size bytes\n";
                if ($size > 4) {
                    my $offPt = Get32u($dataPt,$entry+8);
                    my $actPt = $valuePtr + $valueDataPos + $base - ($$exifTool{EXIF_POS} || 0);
                    $tip .= sprintf("Value offset: 0x%.4x\n", $offPt);
                    # highlight tag name (red for bad size)
                    my $style = ($valueDataLen < 0 or not defined $tval) ? 'V' : 'H';
                    if ($actPt != $offPt) {
                        $tip .= sprintf("Actual offset: 0x%.4x\n", $actPt);
                        my $sign = $actPt < $offPt ? '-' : '';
                        $tip .= sprintf("Offset base: ${sign}0x%.4x\n", abs($actPt - $offPt));
                        $style = 'F' if $style eq 'H';  # purple for different offsets
                    }
                    $colName = "<span class=$style>$tagName</span>";
                    $colName .= ' <span class=V>(odd)</span>' if $offPt & 0x01;
                } else {
                    $colName = $tagName;
                }
                $colName .= ' <span class=V>(err)</span>' if $wrongFormat;
                if ($valueDataLen < 0 or not defined $tval) {
                    $tval = '<bad offset>';
                } else {
                    $tval = substr($tval,0,28) . '[...]' if length($tval) > 32;
                    if ($formatStr =~ /^(string|undef|binary)/) {
                        # translate non-printable characters
                        $tval =~ tr/\x00-\x1f\x7f-\xff/./;
                    } elsif ($tagInfo and Image::ExifTool::IsInt($tval)) {
                        if ($$tagInfo{IsOffset} or $$tagInfo{SubIFD}) {
                            $tval = sprintf('0x%.4x', $tval);
                            my $actPt = $val + $base - ($$exifTool{EXIF_POS} || 0);
                            if ($actPt != $val) {
                                $tval .= sprintf("\nActual offset: 0x%.4x", $actPt);
                                my $sign = $actPt < $val ? '-' : '';
                                $tval .= sprintf("\nOffset base: ${sign}0x%.4x", abs($actPt - $val));
                            }
                        } elsif ($$tagInfo{PrintHex}) {
                            $tval = sprintf('0x%x', $tval);
                        }
                    }
                }
                $tip .= "Value: $tval";
                $exifTool->HDump($entry+$dataPos+$base, 12, "$dname $colName", $tip, 1);
                next if $valueDataLen < 0;  # don't process bad pointer entry
                if ($size > 4) {
                    my $exifDumpPos = $valuePtr + $valueDataPos + $base;
                    my $flag = 0;
                    if ($subdir) {
                        if ($$tagInfo{MakerNotes}) {
                            $flag = 0x04;
                        } elsif ($$tagInfo{NestedHtmlDump}) {
                            $flag = $$tagInfo{NestedHtmlDump} == 2 ? 0x10 : 0x04;
                        }
                    }
                    # add value data block (underlining maker notes data)
                    $exifTool->HDump($exifDumpPos,$size,"$tagName value",'SAME', $flag);
                }
            } else {
                my $fstr = $formatName[$format];
                $fstr = "$origFormStr read as $fstr" if $origFormStr;
                $exifTool->VerboseInfo($tagID, $tagInfo,
                    Table   => $tagTablePtr,
                    Index   => $index,
                    Value   => $tval,
                    DataPt  => $valueDataPt,
                    DataPos => $valueDataPos + $base,
                    Size    => $size,
                    Start   => $valuePtr,
                    Format  => $fstr,
                    Count   => $count,
                );
            }
            next if not $tagInfo or $wrongFormat;
        }
        next unless defined $val;
#..............................................................................
# Handle SubDirectory tag types
#
        if ($subdir) {
            # don't process empty subdirectories
            unless ($size) {
                unless ($$tagInfo{MakerNotes} or $inMakerNotes) {
                    $exifTool->Warn("Empty $$tagInfo{Name} data", 1);
                }
                next;
            }
            my (@values, $newTagTable, $dirNum, $newByteOrder, $invalid);
            my $tagStr = $$tagInfo{Name};
            if ($$subdir{MaxSubdirs}) {
                @values = split ' ', $val;
                # limit the number of subdirectories we parse
                my $over = @values - $$subdir{MaxSubdirs};
                if ($over > 0) {
                    $exifTool->Warn("Ignoring $over $tagStr directories");
                    pop @values while $over--;
                }
                $val = shift @values;
            }
            if ($$subdir{TagTable}) {
                $newTagTable = GetTagTable($$subdir{TagTable});
                $newTagTable or warn("Unknown tag table $$subdir{TagTable}"), next;
            } else {
                $newTagTable = $tagTablePtr;    # use existing table
            }
            # loop through all sub-directories specified by this tag
            for ($dirNum=0; ; ++$dirNum) {
                my $subdirBase = $base;
                my $subdirDataPt = $valueDataPt;
                my $subdirDataPos = $valueDataPos;
                my $subdirDataLen = $valueDataLen;
                my $subdirStart = $valuePtr;
                if (defined $$subdir{Start}) {
                    # set local $valuePtr relative to file $base for eval
                    my $valuePtr = $subdirStart + $subdirDataPos;
                    #### eval Start ($valuePtr, $val)
                    my $newStart = eval($$subdir{Start});
                    unless (Image::ExifTool::IsInt($newStart)) {
                        $exifTool->Warn("Bad value for $tagStr");
                        last;
                    }
                    # convert back to relative to $subdirDataPt
                    $newStart -= $subdirDataPos;
                    # must adjust directory size if start changed
                    $size -= $newStart - $subdirStart unless $$subdir{BadOffset};
                    $subdirStart = $newStart;
                }
                # this is a pain, but some maker notes are always a specific
                # byte order, regardless of the byte order of the file
                my $oldByteOrder = GetByteOrder();
                $newByteOrder = $$subdir{ByteOrder};
                if ($newByteOrder) {
                    if ($newByteOrder =~ /^Little/i) {
                        $newByteOrder = 'II';
                    } elsif ($newByteOrder =~ /^Big/i) {
                        $newByteOrder = 'MM';
                    } elsif ($$subdir{OffsetPt}) {
                        undef $newByteOrder;
                        warn "Can't have variable byte ordering for SubDirectories using OffsetPt";
                        last;
                    } elsif ($subdirStart + 2 <= $subdirDataLen) {
                        # attempt to determine the byte ordering by checking
                        # at the number of directory entries.  This is an int16u
                        # that should be a reasonable value.
                        my $num = Get16u($subdirDataPt, $subdirStart);
                        if ($num & 0xff00 and ($num>>8) > ($num&0xff)) {
                            # This looks wrong, we shouldn't have this many entries
                            my %otherOrder = ( II=>'MM', MM=>'II' );
                            $newByteOrder = $otherOrder{$oldByteOrder};
                        } else {
                            $newByteOrder = $oldByteOrder;
                        }
                    }
                } else {
                    $newByteOrder = $oldByteOrder;
                }
                # set base offset if necessary
                if ($$subdir{Base}) {
                    # calculate subdirectory start relative to $base for eval
                    my $start = $subdirStart + $subdirDataPos;
                    #### eval Base ($start,$base)
                    $subdirBase = eval($$subdir{Base}) + $base;
                }
                # add offset to the start of the directory if necessary
                if ($$subdir{OffsetPt}) {
                    #### eval OffsetPt ($valuePtr)
                    my $pos = eval $$subdir{OffsetPt};
                    if ($pos + 4 > $subdirDataLen) {
                        $exifTool->Warn("Bad $tagStr OffsetPt");
                        last;
                    }
                    SetByteOrder($newByteOrder);
                    $subdirStart += Get32u($subdirDataPt, $pos);
                    SetByteOrder($oldByteOrder);
                }
                if ($subdirStart < 0 or $subdirStart + 2 > $subdirDataLen) {
                    # convert $subdirStart back to a file offset
                    if ($raf) {
                        # reset SubDirectory buffer (we will load it later)
                        my $buff = '';
                        $subdirDataPt = \$buff;
                        $subdirDataLen = $size = length $buff;
                    } else {
                        my $msg = "Bad $tagStr SubDirectory start";
                        if ($verbose > 0) {
                            if ($subdirStart < 0) {
                                $msg .= " (directory start $subdirStart is before EXIF start)";
                            } else {
                                my $end = $subdirStart + $size;
                                $msg .= " (directory end is $end but EXIF size is only $subdirDataLen)";
                            }
                        }
                        $exifTool->Warn($msg);
                        last;
                    }
                }

                # must update subdirDataPos if $base changes for this subdirectory
                $subdirDataPos += $base - $subdirBase;

                # build information hash for new directory
                my %subdirInfo = (
                    Name       => $tagStr,
                    Base       => $subdirBase,
                    DataPt     => $subdirDataPt,
                    DataPos    => $subdirDataPos,
                    DataLen    => $subdirDataLen,
                    DirStart   => $subdirStart,
                    DirLen     => $size,
                    RAF        => $raf,
                    Parent     => $dirName,
                    DirName    => $$subdir{DirName},
                    FixBase    => $$subdir{FixBase},
                    FixOffsets => $$subdir{FixOffsets},
                    EntryBased => $$subdir{EntryBased},
                    TagInfo    => $tagInfo,
                    SubIFD     => $$tagInfo{SubIFD},
                    Subdir     => $subdir,
                );
                # (remember: some cameras incorrectly write maker notes in IFD0)
                if ($$tagInfo{MakerNotes}) {
                    # don't parse makernotes if FastScan > 1
                    my $fast = $exifTool->Options('FastScan');
                    last if $fast and $fast > 1;
                    $subdirInfo{MakerNoteAddr} = $valuePtr + $valueDataPos + $base;
                    $subdirInfo{NoFixBase} = 1 if defined $$subdir{Base};
                }
                # set directory IFD name from group name of family 1 if it exists,
                # unless the tag is extracted as a block in which case group 1 may
                # have been set automatically if the block was previously extracted
                if ($$tagInfo{Groups} and not $$tagInfo{BlockExtract}) {
                    $subdirInfo{DirName} = $tagInfo->{Groups}{1};
                    # number multiple subdirectories
                    $subdirInfo{DirName} =~ s/\d*$/$dirNum/ if $dirNum;
                }
                SetByteOrder($newByteOrder);    # set byte order for this subdir
                # validate the subdirectory if necessary
                my $dirData = $subdirDataPt;    # set data pointer to be used in eval
                #### eval Validate ($val, $dirData, $subdirStart, $size)
                my $ok = 0;
                if (defined $$subdir{Validate} and not eval $$subdir{Validate}) {
                    $exifTool->Warn("Invalid $tagStr data");
                    $invalid = 1;
                } else {
                    # process the subdirectory
                    $ok = $exifTool->ProcessDirectory(\%subdirInfo, $newTagTable, $$subdir{ProcessProc});
                }
                # print debugging information if there were errors
                if (not $ok and $verbose > 1 and $subdirStart != $valuePtr) {
                    my $out = $exifTool->Options('TextOut');
                    printf $out "%s    (SubDirectory start = 0x%x)\n", $exifTool->{INDENT}, $subdirStart;
                }
                SetByteOrder($oldByteOrder);    # restore original byte swapping

                @values or last;
                $val = shift @values;           # continue with next subdir
            }
            my $doMaker = $exifTool->Options('MakerNotes');
            next unless $doMaker or $exifTool->{REQ_TAG_LOOKUP}{lc($tagStr)} or
                        $$tagInfo{BlockExtract};
            # extract as a block if specified
            if ($$tagInfo{MakerNotes}) {
                # save maker note byte order (if it was significant and valid)
                if ($$subdir{ByteOrder} and not $invalid) {
                    $exifTool->{MAKER_NOTE_BYTE_ORDER} =
                        defined ($exifTool->{UnknownByteOrder}) ?
                                 $exifTool->{UnknownByteOrder} : $newByteOrder;
                }
                if ($doMaker and $doMaker eq '2') {
                    # extract maker notes without rebuilding (no fixup information)
                    delete $exifTool->{MAKER_NOTE_FIXUP};
                } elsif (not $$tagInfo{NotIFD}) {
                    # this is a pain, but we must rebuild EXIF-typemaker notes to
                    # include all the value data if data was outside the maker notes
                    my %makerDirInfo = (
                        Name       => $tagStr,
                        Base       => $base,
                        DataPt     => $valueDataPt,
                        DataPos    => $valueDataPos,
                        DataLen    => $valueDataLen,
                        DirStart   => $valuePtr,
                        DirLen     => $size,
                        RAF        => $raf,
                        Parent     => $dirName,
                        DirName    => 'MakerNotes',
                        FixOffsets => $$subdir{FixOffsets},
                        TagInfo    => $tagInfo,
                    );
                    $makerDirInfo{FixBase} = 1 if $$subdir{FixBase};
                    # rebuild maker notes (creates $exifTool->{MAKER_NOTE_FIXUP})
                    my $val2 = RebuildMakerNotes($exifTool, $newTagTable, \%makerDirInfo);
                    if (defined $val2) {
                        $val = $val2;
                    } else {
                        $exifTool->Warn('Error rebuilding maker notes (may be corrupt)');
                    }
                }
            } else {
                # extract this directory as a block if specified
                next unless $$tagInfo{Writable};
            }
        }
 #..............................................................................
        # convert to absolute offsets if this tag is an offset
        #### eval IsOffset ($val, $exifTool)
        if ($$tagInfo{IsOffset} and eval $$tagInfo{IsOffset}) {
            my $offsetBase = $$tagInfo{IsOffset} eq '2' ? $firstBase : $base;
            $offsetBase += $$exifTool{BASE};
            # handle offsets which use a wrong base (Minolta A200)
            if ($$tagInfo{WrongBase}) {
                my $self = $exifTool;
                #### eval WrongBase ($self)
                $offsetBase += eval $$tagInfo{WrongBase} || 0;
            }
            my @vals = split(' ',$val);
            foreach $val (@vals) {
                $val += $offsetBase;
            }
            $val = join(' ', @vals);
        }
        # save the value of this tag
        $tagKey = $exifTool->FoundTag($tagInfo, $val);
        # set the group 1 name for tags in specified tables
        if (defined $tagKey and $$tagTablePtr{SET_GROUP1}) {
            $exifTool->SetGroup($tagKey, $dirName);
        }
    }

    # scan for subsequent IFD's if specified
    if ($$dirInfo{Multi} and $bytesFromEnd >= 4) {
        my $offset = Get32u($dataPt, $dirEnd);
        if ($offset) {
            my $subdirStart = $offset - $dataPos;
            # use same directory information for trailing directory,
            # but change the start location (ProcessDirectory will
            # test to make sure we don't reprocess the same dir twice)
            my %newDirInfo = %$dirInfo;
            $newDirInfo{DirStart} = $subdirStart;
            # increment IFD number
            my $ifdNum = $newDirInfo{DirName} =~ s/(\d+)$// ? $1 : 0;
            $newDirInfo{DirName} .= $ifdNum + 1;
            # must validate SubIFD1 because the nextIFD pointer is invalid for some RAW formats
            if ($newDirInfo{DirName} ne 'SubIFD1' or ValidateIFD(\%newDirInfo)) {
                $exifTool->{INDENT} =~ s/..$//; # keep indent the same
                my $cur = pop @{$$exifTool{PATH}};
                $exifTool->ProcessDirectory(\%newDirInfo, $tagTablePtr) or $success = 0;
                push @{$$exifTool{PATH}}, $cur;
            } elsif ($verbose or $exifTool->{TIFF_TYPE} eq 'TIFF') {
                $exifTool->Warn('Ignored bad IFD linked from SubIFD');
            }
        }
    }
    return $success;
}

1; # end

__END__

=head1 NAME

Image::ExifTool::Exif - Read EXIF/TIFF meta information

=head1 SYNOPSIS

This module is required by Image::ExifTool.

=head1 DESCRIPTION

This module contains routines required by Image::ExifTool for processing
EXIF and TIFF meta information.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.exif.org/Exif2-2.PDF>

=item L<http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-008-2010_E.pdf>

=item L<http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf>

=item L<http://partners.adobe.com/public/developer/en/tiff/TIFFPM6.pdf>

=item L<http://www.adobe.com/products/dng/pdfs/dng_spec.pdf>

=item L<http://www.awaresystems.be/imaging/tiff/tifftags.html>

=item L<http://www.remotesensing.org/libtiff/TIFFTechNote2.html>

=item L<http://www.exif.org/dcf.PDF>

=item L<http://park2.wakwak.com/~tsuruzoh/Computer/Digicams/exif-e.html>

=item L<http://www.fine-view.com/jp/lab/doc/ps6ffspecsv2.pdf>

=item L<http://www.ozhiker.com/electronics/pjmt/jpeg_info/meta.html>

=item L<http://hul.harvard.edu/jhove/tiff-tags.html>

=item L<http://www.microsoft.com/whdc/xps/wmphoto.mspx>

=item L<http://www.asmail.be/msg0054681802.html>

=item L<http://crousseau.free.fr/imgfmt_raw.htm>

=item L<http://www.cybercom.net/~dcoffin/dcraw/>

=item L<http://www.digitalpreservation.gov/formats/content/tiff_tags.shtml>

=item L<http://community.roxen.com/developers/idocs/rfc/rfc3949.html>

=item L<http://tools.ietf.org/html/draft-ietf-fax-tiff-fx-extension1-01>

=back

=head1 ACKNOWLEDGEMENTS

Thanks to Jeremy Brown for the 35efl tags, and Matt Madrid for his help with
the XP character code conversions.

=head1 SEE ALSO

L<Image::ExifTool::TagNames/EXIF Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut
