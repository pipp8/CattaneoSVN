#------------------------------------------------------------------------------
# File:         ID3.pm
#
# Description:  Read ID3 meta information
#
# Revisions:    09/12/2005 - P. Harvey Created
#
# References:   1) http://www.id3.org/
#               2) http://www.mp3-tech.org/
#               3) http://www.fortunecity.com/underworld/sonic/3/id3tag.html
#------------------------------------------------------------------------------

package Image::ExifTool::ID3;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.29';

sub ProcessID3v2($$$);
sub ProcessPrivate($$$);
sub ConvertID3v1Text($$);

# audio formats that we process after an ID3v2 header (in order)
my @audioFormats = qw(APE MPC FLAC OGG MP3);

# audio formats where the processing proc is in a differently-named module
my %audioModule = (
    MP3 => 'ID3',
    OGG => 'Ogg',
);

# picture types for 'PIC' and 'APIC' tags
# (Note: Duplicated in ID3, ASF and FLAC modules!)
my %pictureType = (
    0 => 'Other',
    1 => '32x32 PNG Icon',
    2 => 'Other Icon',
    3 => 'Front Cover',
    4 => 'Back Cover',
    5 => 'Leaflet',
    6 => 'Media',
    7 => 'Lead Artist',
    8 => 'Artist',
    9 => 'Conductor',
    10 => 'Band',
    11 => 'Composer',
    12 => 'Lyricist',
    13 => 'Recording Studio or Location',
    14 => 'Recording Session',
    15 => 'Performance',
    16 => 'Capture from Movie or Video',
    17 => 'Bright(ly) Colored Fish',
    18 => 'Illustration',
    19 => 'Band Logo',
    20 => 'Publisher Logo',
);

my %dateTimeConv = (
    ValueConv => 'require Image::ExifTool::XMP; Image::ExifTool::XMP::ConvertXMPDate($val)',
    PrintConv => '$self->ConvertDateTime($val)',
);

# This table is just for documentation purposes
%Image::ExifTool::ID3::Main = (
    VARS => { NO_ID => 1 },
    NOTES => q{
        ExifTool extracts ID3 information from MP3, MPEG, AIFF, OGG, FLAC, APE and
        RealAudio files.  ID3v2 tags which support multiple languages (ie. Comment
        and Lyrics) are extracted by specifying the tag name, followed by a dash
        ('-'), then a 3-character
        ISO 639-2
        language code (ie. "Comment-spa"). See L<http://www.id3.org/> for the
        official ID3 specification and
        L<http://www.loc.gov/standards/iso639-2/php/code_list.php> for a list of ISO
        639-2 language codes.
    },
    ID3v1 => {
        Name => 'ID3v1',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::v1' },
    },
    ID3v1Enh => {
        Name => 'ID3v1_Enh',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::v1_Enh' },
    },
    ID3v22 => {
        Name => 'ID3v2_2',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::v2_2' },
    },
    ID3v23 => {
        Name => 'ID3v2_3',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::v2_3' },
    },
    ID3v24 => {
        Name => 'ID3v2_4',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::v2_4' },
    },
);

# Mapping for ID3v1 Genre numbers
my %genre = (
      0 => 'Blues',
      1 => 'Classic Rock',
      2 => 'Country',
      3 => 'Dance',
      4 => 'Disco',
      5 => 'Funk',
      6 => 'Grunge',
      7 => 'Hip-Hop',
      8 => 'Jazz',
      9 => 'Metal',
     10 => 'New Age',
     11 => 'Oldies',
     12 => 'Other',
     13 => 'Pop',
     14 => 'R&B',
     15 => 'Rap',
     16 => 'Reggae',
     17 => 'Rock',
     18 => 'Techno',
     19 => 'Industrial',
     20 => 'Alternative',
     21 => 'Ska',
     22 => 'Death Metal',
     23 => 'Pranks',
     24 => 'Soundtrack',
     25 => 'Euro-Techno',
     26 => 'Ambient',
     27 => 'Trip-Hop',
     28 => 'Vocal',
     29 => 'Jazz+Funk',
     30 => 'Fusion',
     31 => 'Trance',
     32 => 'Classical',
     33 => 'Instrumental',
     34 => 'Acid',
     35 => 'House',
     36 => 'Game',
     37 => 'Sound Clip',
     38 => 'Gospel',
     39 => 'Noise',
     40 => 'AlternRock',
     41 => 'Bass',
     42 => 'Soul',
     43 => 'Punk',
     44 => 'Space',
     45 => 'Meditative',
     46 => 'Instrumental Pop',
     47 => 'Instrumental Rock',
     48 => 'Ethnic',
     49 => 'Gothic',
     50 => 'Darkwave',
     51 => 'Techno-Industrial',
     52 => 'Electronic',
     53 => 'Pop-Folk',
     54 => 'Eurodance',
     55 => 'Dream',
     56 => 'Southern Rock',
     57 => 'Comedy',
     58 => 'Cult',
     59 => 'Gangsta',
     60 => 'Top 40',
     61 => 'Christian Rap',
     62 => 'Pop/Funk',
     63 => 'Jungle',
     64 => 'Native American',
     65 => 'Cabaret',
     66 => 'New Wave',
     67 => 'Psychadelic',
     68 => 'Rave',
     69 => 'Showtunes',
     70 => 'Trailer',
     71 => 'Lo-Fi',
     72 => 'Tribal',
     73 => 'Acid Punk',
     74 => 'Acid Jazz',
     75 => 'Polka',
     76 => 'Retro',
     77 => 'Musical',
     78 => 'Rock & Roll',
     79 => 'Hard Rock',
     # The following genres are Winamp extensions
     80 => 'Folk',
     81 => 'Folk-Rock',
     82 => 'National Folk',
     83 => 'Swing',
     84 => 'Fast Fusion',
     85 => 'Bebob',
     86 => 'Latin',
     87 => 'Revival',
     88 => 'Celtic',
     89 => 'Bluegrass',
     90 => 'Avantgarde',
     91 => 'Gothic Rock',
     92 => 'Progressive Rock',
     93 => 'Psychedelic Rock',
     94 => 'Symphonic Rock',
     95 => 'Slow Rock',
     96 => 'Big Band',
     97 => 'Chorus',
     98 => 'Easy Listening',
     99 => 'Acoustic',
    100 => 'Humour',
    101 => 'Speech',
    102 => 'Chanson',
    103 => 'Opera',
    104 => 'Chamber Music',
    105 => 'Sonata',
    106 => 'Symphony',
    107 => 'Booty Bass',
    108 => 'Primus',
    109 => 'Porn Groove',
    110 => 'Satire',
    111 => 'Slow Jam',
    112 => 'Club',
    113 => 'Tango',
    114 => 'Samba',
    115 => 'Folklore',
    116 => 'Ballad',
    117 => 'Power Ballad',
    118 => 'Rhythmic Soul',
    119 => 'Freestyle',
    120 => 'Duet',
    121 => 'Punk Rock',
    122 => 'Drum Solo',
    123 => 'Acapella',
    124 => 'Euro-House',
    125 => 'Dance Hall',
    # ref http://yar.hole.ru/MP3Tech/lamedoc/id3.html
    126 => 'Goa',
    127 => 'Drum & Bass',
    128 => 'Club-House',
    129 => 'Hardcore',
    130 => 'Terror',
    131 => 'Indie',
    132 => 'BritPop',
    133 => 'Negerpunk',
    134 => 'Polsk Punk',
    135 => 'Beat',
    136 => 'Christian Gangsta',
    137 => 'Heavy Metal',
    138 => 'Black Metal',
    139 => 'Crossover',
    140 => 'Contemporary C',
    141 => 'Christian Rock',
    142 => 'Merengue',
    143 => 'Salsa',
    144 => 'Thrash Metal',
    145 => 'Anime',
    146 => 'JPop',
    147 => 'SynthPop',
    255 => 'None',
    # ID3v2 adds some text short forms...
    CR  => 'Cover',
    RX  => 'Remix',
);

# Tags for ID3v1
%Image::ExifTool::ID3::v1 = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 1 => 'ID3v1', 2 => 'Audio' },
    PRIORITY => 0,  # let ID3v2 tags replace these if they come later
    3 => {
        Name => 'Title',
        Format => 'string[30]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    33 => {
        Name => 'Artist',
        Groups => { 2 => 'Author' },
        Format => 'string[30]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    63 => {
        Name => 'Album',
        Format => 'string[30]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    93 => {
        Name => 'Year',
        Groups => { 2 => 'Time' },
        Format => 'string[4]',
    },
    97 => {
        Name => 'Comment',
        Format => 'string[30]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    125 => { # ID3v1.1 (ref http://en.wikipedia.org/wiki/ID3#Layout)
        Name => 'Track',
        Format => 'int8u[2]',
        Notes => 'v1.1 addition -- last 2 bytes of v1.0 Comment field',
        RawConv => '($val =~ s/^0 // and $val) ? $val : undef',
    },
    127 => {
        Name => 'Genre',
        Notes => 'CR and RX are ID3v2 only',
        Format => 'int8u',
        PrintConv => \%genre,
        PrintConvColumns => 3,
    },
);

# ID3v1 "Enhanced TAG" information (ref 3)
%Image::ExifTool::ID3::v1_Enh = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 1 => 'ID3v1_Enh', 2 => 'Audio' },
    NOTES => 'ID3 version 1 "Enhanced TAG" information (not part of the official spec).',
    PRIORITY => 0,  # let ID3v2 tags replace these if they come later
    4 => {
        Name => 'Title2',
        Format => 'string[60]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    64 => {
        Name => 'Artist2',
        Groups => { 2 => 'Author' },
        Format => 'string[60]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    124 => {
        Name => 'Album2',
        Format => 'string[60]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    184 => {
        Name => 'Speed',
        Format => 'int8u',
        PrintConv => {
            1 => 'Slow',
            2 => 'Medium',
            3 => 'Fast',
            4 => 'Hardcore',
        },
    },
    185 => {
        Name => 'Genre',
        Format => 'string[30]',
        ValueConv => 'Image::ExifTool::ID3::ConvertID3v1Text($self,$val)',
    },
    215 => {
        Name => 'StartTime',
        Format => 'string[6]',
    },
    221 => {
        Name => 'EndTime',
        Format => 'string[6]',
    },
);

# Tags for ID2v2.2
%Image::ExifTool::ID3::v2_2 = (
    PROCESS_PROC => \&Image::ExifTool::ID3::ProcessID3v2,
    GROUPS => { 1 => 'ID3v2_2', 2 => 'Audio' },
    NOTES => q{
        ExifTool extracts mainly text-based tags from ID3v2 information.  The tags
        in the tables below are those extracted by ExifTool, and don't represent a
        complete list of available ID3v2 tags.
        
        ID3 version 2.2 tags.  (These are the tags written by iTunes 5.0.)
    },
    CNT => 'PlayCounter',
    COM => 'Comment',
    IPL => 'InvolvedPeople',
    PIC => {
        Name => 'Picture',
        Groups => { 2 => 'Image' },
        Binary => 1,
        Notes => 'the 3 tags below are also extracted from this PIC frame',
    },
    'PIC-1' => { Name => 'PictureFormat',      Groups => { 2 => 'Image' } },
    'PIC-2' => {
        Name => 'PictureType',
        Groups => { 2 => 'Image' },
        PrintConv => \%pictureType,
        SeparateTable => 1,
    },
    'PIC-3' => { Name => 'PictureDescription', Groups => { 2 => 'Image' } },
  # POP => 'Popularimeter',
    SLT => {
        Name => 'SynchronizedLyricText',
        Binary => 1,
    },
    TAL => 'Album',
    TBP => 'BeatsPerMinute',
    TCM => 'Composer',
    TCO =>{
        Name => 'Genre',
        Notes => 'uses same lookup table as ID3v1 Genre',
        PrintConv => 'Image::ExifTool::ID3::PrintGenre($val)',
    },
    TCP => 'Compilation', # not part of spec, but used by iTunes
    TCR => { Name => 'Copyright', Groups => { 2 => 'Author' } },
    TDA => { Name => 'Date', Groups => { 2 => 'Time' } },
    TDY => 'PlaylistDelay',
    TEN => 'EncodedBy',
    TFT => 'FileType',
    TIM => { Name => 'Time', Groups => { 2 => 'Time' } },
    TKE => 'InitialKey',
    TLA => 'Language',
    TLE => 'Length',
    TMT => 'Media',
    TOA => { Name => 'OriginalArtist', Groups => { 2 => 'Author' } },
    TOF => 'OriginalFilename',
    TOL => 'OriginalLyricist',
    TOR => 'OriginalReleaseYear',
    TOT => 'OriginalAlbum',
    TP1 => { Name => 'Artist', Groups => { 2 => 'Author' } },
    TP2 => 'Band',
    TP3 => 'Conductor',
    TP4 => 'InterpretedBy',
    TPA => 'PartOfSet',
    TPB => 'Publisher',
    TRC => 'ISRC', # (international standard recording code)
    TRD => 'RecordingDates',
    TRK => 'Track',
    TSI => 'Size',
    TSS => 'EncoderSettings',
    TT1 => 'Grouping',
    TT2 => 'Title',
    TT3 => 'Subtitle',
    TXT => 'Lyricist',
    TXX => 'UserDefinedText',
    TYE => { Name => 'Year', Groups => { 2 => 'Time' } },
    ULT => 'Lyrics',
    WAF => 'FileURL',
    WAR => { Name => 'ArtistURL', Groups => { 2 => 'Author' } },
    WAS => 'SourceURL',
    WCM => 'CommercialURL',
    WCP => { Name => 'CopyrightURL', Groups => { 2 => 'Author' } },
    WPB => 'PublisherURL',
    WXX => 'UserDefinedURL',
);

# tags common to ID3v2.3 and ID3v2.4
my %id3v2_common = (
  # AENC => 'AudioEncryption', # Owner, preview start, preview length, encr data
    APIC => {
        Name => 'Picture',
        Groups => { 2 => 'Image' },
        Binary => 1,
        Notes => 'the 3 tags below are also extracted from this PIC frame',
    },
    'APIC-1' => { Name => 'PictureMimeType',    Groups => { 2 => 'Image' } },
    'APIC-2' => {
        Name => 'PictureType',
        Groups => { 2 => 'Image' },
        PrintConv => \%pictureType,
        SeparateTable => 1,
    },
    'APIC-3' => { Name => 'PictureDescription', Groups => { 2 => 'Image' } },
    COMM => 'Comment',
  # COMR => 'Commercial',
  # ENCR => 'EncryptionMethod',
  # ETCO => 'EventTimingCodes',
  # GEOB => 'GeneralEncapsulatedObject',
  # GRID => 'GroupIdentification',
  # LINK => 'LinkedInformation',
    MCDI => { Name => 'MusicCDIdentifier', Binary => 1 },
  # MLLT => 'MPEGLocationLookupTable',
  # OWNE => 'Ownership', # enc(1), _price, 00, _date(8), Seller
    PCNT => 'PlayCounter',
  # POPM => 'Popularimeter', # _email, 00, rating(1), counter(4-N)
  # POSS => 'PostSynchronization',
    PRIV => {
        Name => 'Private',
        SubDirectory => { TagTable => 'Image::ExifTool::ID3::Private' },
    },
  # RBUF => 'RecommendedBufferSize',
  # RVRB => 'Reverb',
    SYLT => {
        Name => 'SynchronizedLyricText',
        Binary => 1,
    },
  # SYTC => 'SynchronizedTempoCodes',
    TALB => 'Album',
    TBPM => 'BeatsPerMinute',
    TCMP => { Name => 'Compilation', PrintConv => { 0 => 'No', 1 => 'Yes' } }, #PH (iTunes)
    TCOM => 'Composer',
    TCON =>{
        Name => 'Genre',
        Notes => 'uses same lookup table as ID3v1 Genre',
        PrintConv => 'Image::ExifTool::ID3::PrintGenre($val)',
    },
    TCOP => { Name => 'Copyright', Groups => { 2 => 'Author' } },
    TDLY => 'PlaylistDelay',
    TENC => 'EncodedBy',
    TEXT => 'Lyricist',
    TFLT => 'FileType',
    TIT1 => 'Grouping',
    TIT2 => 'Title',
    TIT3 => 'Subtitle',
    TKEY => 'InitialKey',
    TLAN => 'Language',
    TLEN => {
        Name => 'Length',
        ValueConv => '$val / 1000',
        PrintConv => '"$val s"',
    },
    TMED => 'Media',
    TOAL => 'OriginalAlbum',
    TOFN => 'OriginalFilename',
    TOLY => 'OriginalLyricist',
    TOPE => { Name => 'OriginalArtist', Groups => { 2 => 'Author' } },
    TOWN => 'FileOwner',
    TPE1 => { Name => 'Artist', Groups => { 2 => 'Author' } },
    TPE2 => 'Band',
    TPE3 => 'Conductor',
    TPE4 => 'InterpretedBy',
    TPOS => 'PartOfSet',
    TPUB => 'Publisher',
    TRCK => 'Track',
    TRSN => 'InternetRadioStationName',
    TRSO => 'InternetRadioStationOwner',
    TSRC => 'ISRC', # (international standard recording code)
    TSSE => 'EncoderSettings',
    TXXX => 'UserDefinedText',
  # UFID => 'UniqueFileID',
    USER => 'TermsOfUse',
    USLT => 'Lyrics',
    WCOM => 'CommercialURL',
    WCOP => 'CopyrightURL',
    WOAF => 'FileURL',
    WOAR => { Name => 'ArtistURL', Groups => { 2 => 'Author' } },
    WOAS => 'SourceURL',
    WORS => 'InternetRadioStationURL',
    WPAY => 'PaymentURL',
    WPUB => 'PublisherURL',
    WXXX => 'UserDefinedURL',
);

# Tags for ID3v2.3 (http://www.id3.org/id3v2.3.0)
%Image::ExifTool::ID3::v2_3 = (
    PROCESS_PROC => \&Image::ExifTool::ID3::ProcessID3v2,
    GROUPS => { 1 => 'ID3v2_3', 2 => 'Audio' },
    NOTES => 'ID3 version 2.3 tags',
    %id3v2_common,  # include common tags
  # EQUA => 'Equalization',
    IPLS => 'InvolvedPeople',
  # RVAD => 'RelativeVolumeAdjustment',
    TDAT => { Name => 'Date', Groups => { 2 => 'Time' } },
    TIME => { Name => 'Time', Groups => { 2 => 'Time' } },
    TORY => 'OriginalReleaseYear',
    TRDA => 'RecordingDates',
    TSIZ => 'Size',
    TYER => { Name => 'Year', Groups => { 2 => 'Time' } },
);

# Tags for ID3v2.4 (http://www.id3.org/id3v2.4.0-frames)
%Image::ExifTool::ID3::v2_4 = (
    PROCESS_PROC => \&Image::ExifTool::ID3::ProcessID3v2,
    GROUPS => { 1 => 'ID3v2_4', 2 => 'Audio' },
    NOTES => 'ID3 version 2.4 tags',
    %id3v2_common,  # include common tags
  # EQU2 => 'Equalization',
  # RVA2 => 'RelativeVolumeAdjustment',
  # SEEK => 'Seek',
  # SIGN => 'Signature',
    TDEN => { Name => 'EncodingTime',       Groups => { 2 => 'Time' }, %dateTimeConv },
    TDOR => { Name => 'OriginalReleaseTime',Groups => { 2 => 'Time' }, %dateTimeConv },
    TDRC => { Name => 'RecordingTime',      Groups => { 2 => 'Time' }, %dateTimeConv },
    TDRL => { Name => 'ReleaseTime',        Groups => { 2 => 'Time' }, %dateTimeConv },
    TDTG => { Name => 'TaggingTime',        Groups => { 2 => 'Time' }, %dateTimeConv },
    TIPL => 'InvolvedPeople',
    TMCL => 'MusicianCredits',
    TMOO => 'Mood',
    TPRO => 'ProducedNotice',
    TSOA => 'AlbumSortOrder',
    TSOP => 'PerformerSortOrder',
    TSOT => 'TitleSortOrder',
    TSST => 'SetSubtitle',
);

# ID3 PRIV tags (ref PH)
%Image::ExifTool::ID3::Private = (
    PROCESS_PROC => \&Image::ExifTool::ID3::ProcessPrivate,
    GROUPS => { 1 => 'ID3', 2 => 'Audio' },
    NOTES => 'ID3 private (PRIV) tags.',
    XMP => {
        SubDirectory => {
            DirName => 'XMP',
            TagTable => 'Image::ExifTool::XMP::Main',
        },
    },
    PeakValue => {
        ValueConv => 'length($val)==4 ? unpack("V",$val) : \$val',
    },
    AverageLevel => {
        ValueConv => 'length($val)==4 ? unpack("V",$val) : \$val',
    },
);

# ID3 Composite tags
%Image::ExifTool::ID3::Composite = (
    GROUPS => { 2 => 'Image' },
    DateTimeOriginal => {
        Description => 'Date/Time Original',
        Groups => { 2 => 'Time' },
        Priority => 0,
        Desire => {
            0 => 'ID3:RecordingTime',
            1 => 'ID3:Year',
            2 => 'ID3:Date',
            3 => 'ID3:Time',
        },
        ValueConv => q{
            return $val[0] if $val[0];
            return undef unless $val[1];
            return $val[1] unless $val[2] and $val[2] =~ /^(\d{2})(\d{2})$/;
            $val[1] .= ":$1:$2";
            return $val[1] unless $val[3] and $val[3] =~ /^(\d{2})(\d{2})$/;
            return "$val[1] $1:$2";
        },
        PrintConv => '$self->ConvertDateTime($val)',
    },
);

# add our composite tags
Image::ExifTool::AddCompositeTags('Image::ExifTool::ID3');

# can't share tagInfo hashes between two tables, so we must make
# copies of the necessary hashes
{
    my $tag;
    foreach $tag (keys %id3v2_common) {
        next unless ref $id3v2_common{$tag} eq 'HASH';
        my %tagInfo = %{$id3v2_common{$tag}};
        # must also copy Groups hash if it exists
        my $groups = $tagInfo{Groups};
        $tagInfo{Groups} = { %$groups } if $groups;
        $Image::ExifTool::ID3::v2_4{$tag} = \%tagInfo;
    }
}

#------------------------------------------------------------------------------
# Convert ID3v1 text to exiftool character set
# Inputs: 0) ExifTool object ref, 1) text string
# Returns: converted text
sub ConvertID3v1Text($$)
{
    my ($exifTool, $val) = @_;
    return $exifTool->Decode($val, $exifTool->Options('CharsetID3'));
}

#------------------------------------------------------------------------------
# Process ID3 PRIV data
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
sub ProcessPrivate($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my ($tag, $start);
    if ($$dataPt =~ /^(.*?)\0/) {
        $tag = $1;
        $start = length($tag) + 1;
    } else {
        $tag = '';
        $start = 0;
    }
    unless ($$tagTablePtr{$tag}) {
        $tag =~ tr{/ }{_}d; # translate '/' to '_' and remove spaces
        $tag = 'private' unless $tag =~ /^[-\w]{1,24}$/;
        unless ($$tagTablePtr{$tag}) {
            Image::ExifTool::AddTagToTable($tagTablePtr, $tag,
                { Name => ucfirst($tag), Binary => 1 });
        }
    }
    my $key = $exifTool->HandleTag($tagTablePtr, $tag, undef,
        Size  => length($$dataPt) - $start,
        Start => $start,
        DataPt => $dataPt,
    );
    # set group1 name
    $exifTool->SetGroup($key, $$exifTool{ID3_Ver}) if $key;
}

#------------------------------------------------------------------------------
# Print ID3v2 Genre
# Inputs: TCON or TCO frame data
# Returns: Content type with decoded genre numbers
sub PrintGenre($)
{
    my $val = shift;
    # make sure that %genre has an entry for all numbers we are interested in
    # (genre numbers are in brackets for ID3v2.2 and v2.3)
    while ($val =~ /\((\d+)\)/g) {
        $genre{$1} or $genre{$1} = "Unknown ($1)";
    }
    # (genre numbers are separated by nulls in ID3v2.4,
    #  but nulls are converted to '/' by DecodeString())
    while ($val =~ /(?:^|\/)(\d+)/g) {
        $genre{$1} or $genre{$1} = "Unknown ($1)";
    }
    $val =~ s/\((\d+)\)/\($genre{$1}\)/g;
    $val =~ s/(^|\/)(\d+)/$1$genre{$2}/g;
    $val =~ s/^\(([^)]+)\)\1?$/$1/; # clean up by removing brackets and duplicates
    return $val;
}

#------------------------------------------------------------------------------
# Decode ID3 string
# Inputs: 0) ExifTool object reference
#         1) string beginning with encoding byte unless specified as argument
#         2) optional encoding (0=ISO-8859-1, 1=UTF-16 BOM, 2=UTF-16BE, 3=UTF-8)
# Returns: Decoded string in scalar context, or list of strings in list context
sub DecodeString($$;$)
{
    my ($exifTool, $val, $enc) = @_;
    return '' unless length $val;
    unless (defined $enc) {
        $enc = unpack('C', $val);
        $val = substr($val, 1); # remove encoding byte
    }
    my @vals;
    if ($enc == 0 or $enc == 3) { # ISO 8859-1 or UTF-8
        $val =~ s/\0+$//;   # remove any null padding
        # (must split before converting because conversion routines truncate at null)
        @vals = split "\0", $val;
        foreach $val (@vals) {
            $val = $exifTool->Decode($val, $enc ? 'UTF8' : 'Latin');
        }
    } elsif ($enc == 1 or $enc == 2) {  # UTF-16 with BOM, or UTF-16BE
        my $bom = "\xfe\xff";
        my %order = ( "\xfe\xff" => 'MM', "\xff\xfe", => 'II' );
        for (;;) {
            my $v;
            # split string at null terminators on word boundaries
            if ($val =~ s/((..)*?)\0\0//) {
                $v = $1;
            } else {
                last unless length $val > 1;
                $v = $val;
                $val = '';
            }
            $bom = $1 if $v =~ s/^(\xfe\xff|\xff\xfe)//;
            push @vals, $exifTool->Decode($v, 'UCS2', $order{$bom});
        }
    } else {
        $val =~ s/\0+$//;
        return "<Unknown encoding $enc> $val";
    }
    return @vals if wantarray;
    return join('/',@vals);
}

#------------------------------------------------------------------------------
# Convert sync-safe integer to a number we can use
# Inputs: 0) int32u sync-safe value
# Returns: actual number or undef on invalid value
sub UnSyncSafe($)
{
    my $val = shift;
    return undef if $val & 0x80808080;
    return ($val & 0x0000007f) |
          (($val & 0x00007f00) >> 1) |
          (($val & 0x007f0000) >> 2) |
          (($val & 0x7f000000) >> 3);
}

#------------------------------------------------------------------------------
# Process ID3v2 information
# Inputs: 0) ExifTool object ref, 1) dirInfo ref, 2) tag table ref
sub ProcessID3v2($$$)
{
    my ($exifTool, $dirInfo, $tagTablePtr) = @_;
    my $dataPt  = $$dirInfo{DataPt};
    my $offset  = $$dirInfo{DirStart};
    my $size    = $$dirInfo{DirLen};
    my $vers    = $$dirInfo{Version};
    my $verbose = $exifTool->Options('Verbose');
    my $len;    # frame data length

    $verbose and $exifTool->VerboseDir($tagTablePtr->{GROUPS}->{1}, 0, $size);

    for (;;$offset+=$len) {
        my ($id, $flags, $hi);
        if ($vers < 0x0300) {
            # version 2.2 frame header is 6 bytes
            last if $offset + 6 > $size;
            ($id, $hi, $len) = unpack("x${offset}a3Cn",$$dataPt);
            last if $id eq "\0\0\0";
            $len += $hi << 16;
            $offset += 6;
        } else {
            # version 2.3/2.4 frame header is 10 bytes
            last if $offset + 10 > $size;
            ($id, $len, $flags) = unpack("x${offset}a4Nn",$$dataPt);
            last if $id eq "\0\0\0\0";
            $offset += 10;
            # length is a "sync-safe" integer by the ID3v2.4 specification, but
            # reportedly some versions of iTunes write this as a normal integer
            # (ref http://www.id3.org/iTunes)
            while ($vers >= 0x0400 and $len > 0x7f and not $len & 0x80808080) {
                my $oldLen = $len;
                $len =  UnSyncSafe($len);
                if (not defined $len or $offset + $len + 10 > $size) {
                    $exifTool->Warn('Invalid ID3 frame size');
                    last;
                }
                # check next ID to see if it makes sense
                my $nextID = substr($$dataPt, $offset + $len, 4);
                last if $$tagTablePtr{$nextID};
                # try again with the incorrect length word (patch for iTunes bug)
                last if $offset + $oldLen + 10 > $size;
                $nextID = substr($$dataPt, $offset + $len, 4);
                $len = $oldLen if $$tagTablePtr{$nextID};
                last; # yes, "while" was really a "goto" in disguise
            }
        }
        last if $offset + $len > $size;
        my $tagInfo = $exifTool->GetTagInfo($tagTablePtr, $id);
        unless ($tagInfo) {
            next unless $verbose or $exifTool->Options('Unknown');
            $id =~ tr/-A-Za-z0-9_//dc;
            $id = 'unknown' unless length $id;
            unless ($$tagTablePtr{$id}) {
                $tagInfo = { Name => "ID3_$id", Binary => 1 };
                Image::ExifTool::AddTagToTable($tagTablePtr, $id, $tagInfo);
            }
        }
        # decode v2.3 and v2.4 flags
        my %flags;
        if ($flags) {
            if ($vers < 0x0400) {
                # version 2.3 flags
                $flags & 0x80 and $flags{Compress} = 1;
                $flags & 0x40 and $flags{Encrypt}  = 1;
                $flags & 0x20 and $flags{GroupID}  = 1;
            } else {
                # version 2.4 flags
                $flags & 0x40 and $flags{GroupID}  = 1;
                $flags & 0x08 and $flags{Compress} = 1;
                $flags & 0x04 and $flags{Encrypt}  = 1;
                $flags & 0x02 and $flags{Unsync}   = 1;
                $flags & 0x01 and $flags{DataLen}  = 1;
            }
        }
        if ($flags{Encrypt}) {
            $exifTool->WarnOnce('Encrypted frames currently not supported');
            next;
        }
        # extract the value
        my $val = substr($$dataPt, $offset, $len);

        # reverse the unsynchronization
        $val =~ s/\xff\x00/\xff/g if $flags{Unsync};

        # read grouping identity
        if ($flags{GroupID}) {
            length($val) >= 1 or $exifTool->Warn("Short $id frame"), next;
            $val = substr($val, 1); # (ignore it)
        }
        # read data length
        my $dataLen;
        if ($flags{DataLen} or $flags{Compress}) {
            length($val) >= 4 or $exifTool->Warn("Short $id frame"), next;
            $dataLen = unpack('N', $val);   # save the data length word
            $val = substr($val, 4);
        }
        # uncompress data
        if ($flags{Compress}) {
            if (eval 'require Compress::Zlib') {
                my $inflate = Compress::Zlib::inflateInit();
                my ($buff, $stat);
                $inflate and ($buff, $stat) = $inflate->inflate($val);
                if ($inflate and $stat == Compress::Zlib::Z_STREAM_END()) {
                    $val = $buff;
                } else {
                    $exifTool->Warn("Error inflating $id frame");
                    next;
                }
            } else {
                $exifTool->WarnOnce('Install Compress::Zlib to decode compressed frames');
                next;
            }
        }
        # validate data length
        if (defined $dataLen) {
            $dataLen = UnSyncSafe($dataLen);
            defined $dataLen or $exifTool->Warn("Invalid length for $id frame"), next;
            $dataLen == length($val) or $exifTool->Warn("Wrong length for $id frame"), next;
        }
        $verbose and $exifTool->VerboseInfo($id, $tagInfo,
            Table   => $tagTablePtr,
            Value   => $val,
            DataPt  => $dataPt,
            DataPos => $$dirInfo{DataPos},
            Size    => $len,
            Start   => $offset,
        );
        next unless $tagInfo;
#
# decode data in this frame
#
        my $lang;
        my $valLen = length($val);  # actual value length (after decompression, etc)
        if ($id =~ /^(TXX|TXXX)$/) {
            # two encoded strings separated by a null
            my @vals = DecodeString($exifTool, $val);
            foreach (0..1) { $vals[$_] = '' unless defined $vals[$_]; }
            ($val = "($vals[0]) $vals[1]") =~ s/^\(\) //;
        } elsif ($id =~ /^T/ or $id =~ /^(IPL|IPLS)$/) {
            $val = DecodeString($exifTool, $val);
        } elsif ($id =~ /^(WXX|WXXX)$/) {
            # one encoded string and one Latin string separated by a null
            my $enc = unpack('C', $val);
            my $url;
            if ($enc == 1 or $enc == 2) {
                ($val, $url) = ($val =~ /^(.(?:..)*?)\0\0(.*)/);
            } else {
                ($val, $url) = ($val =~ /^(..*?)\0(.*)/);
            }
            unless (defined $val and defined $url) {
                $exifTool->Warn("Invalid $id frame value");
                next;
            }
            $val = DecodeString($exifTool, $val);
            $url =~ s/\0.*//;
            $val = length($val) ? "($val) $url" : $url;
        } elsif ($id =~ /^W/) {
            $val =~ s/\0.*//;   # truncate at null
        } elsif ($id =~ /^(COM|COMM|ULT|USLT)$/) {
            $valLen > 4 or $exifTool->Warn("Short $id frame"), next;
            $lang = substr($val,1,3);
            my @vals = DecodeString($exifTool, substr($val,4), Get8u(\$val,0));
            foreach (0..1) { $vals[$_] = '' unless defined $vals[$_]; }
            $val = length($vals[0]) ? "($vals[0]) $vals[1]" : $vals[1];
        } elsif ($id eq 'USER') {
            $valLen > 4 or $exifTool->Warn('Short USER frame'), next;
            $lang = substr($val,1,3);
            $val = DecodeString($exifTool, substr($val,4), Get8u(\$val,0));
        } elsif ($id =~ /^(CNT|PCNT)$/) {
            $valLen >= 4 or $exifTool->Warn("Short $id frame"), next;
            my $cnt = unpack('N', $val);
            my $i;
            for ($i=4; $i<$valLen; ++$i) {
                $cnt = $cnt * 256 + unpack("x${i}C", $val);
            }
            $val = $cnt;
        } elsif ($id =~ /^(PIC|APIC)$/) {
            $valLen >= 4 or $exifTool->Warn("Short $id frame"), next;
            my ($hdr, $attr);
            my $enc = unpack('C', $val);
            if ($enc == 1 or $enc == 2) {
                $hdr = ($id eq 'PIC') ? ".(...)(.)((?:..)*?)\0\0" : ".(.*?)\0(.)((?:..)*?)\0\0";
            } else {
                $hdr = ($id eq 'PIC') ? ".(...)(.)(.*?)\0"        : ".(.*?)\0(.)(.*?)\0";
            }
            # remove header (encoding, image format or MIME type, picture type, description)
            $val =~ s/^$hdr//s or $exifTool->Warn("Invalid $id frame"), next;
            my @attrs = ($1, ord($2), DecodeString($exifTool, $3, $enc));
            my $i = 1;
            foreach $attr (@attrs) {
                # must store descriptions even if they are empty to maintain
                # sync between copy numbers when multiple images
                $exifTool->HandleTag($tagTablePtr, "$id-$i", $attr);
                ++$i;
            }
        } elsif ($id eq 'PRIV') {
            # save version number to set group1 name for tag later
            $exifTool->{ID3_Ver} = $tagTablePtr->{GROUPS}->{1};
            $exifTool->HandleTag($tagTablePtr, $id, $val);
            next;
        } elsif (not $$tagInfo{Binary}) {
            $exifTool->Warn("Don't know how to handle $id frame");
            next;
        }
        if ($lang and $lang =~ /^[a-z]{3}$/i and $lang ne 'eng') {
            $tagInfo = Image::ExifTool::GetLangInfo($tagInfo, lc $lang);
        }
        $exifTool->FoundTag($tagInfo, $val);
    }
}

#------------------------------------------------------------------------------
# Extract ID3 information from an audio file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this file didn't contain ID3 information
# - also processes audio data if any ID3 information was found
# - sets ExifTool DoneID3 to 1 when called, or to 2 if an ID3v1 trailer exists
sub ProcessID3($$)
{
    my ($exifTool, $dirInfo) = @_;

    return 0 if $exifTool->{DoneID3};  # avoid infinite recursion
    $exifTool->{DoneID3} = 1;

    # allow this to be called with either RAF or DataPt
    my $raf = $$dirInfo{RAF} || new File::RandomAccess($$dirInfo{DataPt});
    my ($buff, %id3Header, %id3Trailer, $hBuff, $tBuff, $eBuff, $tagTablePtr);
    my $rtnVal = 0;
    my $hdrEnd = 0;
    my $id3Len = 0;

    # read first 3 bytes of file
    $raf->Seek(0, 0);
    return 0 unless $raf->Read($buff, 3) == 3;
#
# identify ID3v2 header
#
    while ($buff =~ /^ID3/) {
        $rtnVal = 1;
        $raf->Read($hBuff, 7) == 7 or $exifTool->Warn('Short ID3 header'), last;
        my ($vers, $flags, $size) = unpack('nCN', $hBuff);
        $size = UnSyncSafe($size);
        defined $size or $exifTool->Warn('Invalid ID3 header'), last;
        my $verStr = sprintf("2.%d.%d", $vers >> 8, $vers & 0xff);
        if ($vers >= 0x0500) {
            $exifTool->Warn("Unsupported ID3 version: $verStr");
            last;
        }
        unless ($raf->Read($hBuff, $size) == $size) {
            $exifTool->Warn('Truncated ID3 data');
            last;
        }
        # this flag only indicates use of unsynchronized frames in ID3v2.4
        if ($flags & 0x80 and $vers < 0x0400) {
            # reverse the unsynchronization
            $hBuff =~ s/\xff\x00/\xff/g;
        }
        my $pos = 10;
        if ($flags & 0x40) {
            # skip the extended header
            $size >= 4 or $exifTool->Warn('Bad ID3 extended header'), last;
            my $len = unpack('N', $hBuff);
            if ($len > length($hBuff) - 4) {
                $exifTool->Warn('Truncated ID3 extended header');
                last;
            }
            $hBuff = substr($hBuff, $len + 4);
            $pos += $len + 4;
        }
        if ($flags & 0x10) {
            # ignore v2.4 footer (10 bytes long)
            $raf->Seek(10, 1);
        }
        %id3Header = (
            DataPt   => \$hBuff,
            DataPos  => $pos,
            DirStart => 0,
            DirLen   => length($hBuff),
            Version  => $vers,
            DirName  => "ID3v$verStr",
        );
        $id3Len += length($hBuff) + 10;
        if ($vers >= 0x0400) {
            $tagTablePtr = GetTagTable('Image::ExifTool::ID3::v2_4');
        } elsif ($vers >= 0x0300) {
            $tagTablePtr = GetTagTable('Image::ExifTool::ID3::v2_3');
        } else {
            $tagTablePtr = GetTagTable('Image::ExifTool::ID3::v2_2');
        }
        $hdrEnd = $raf->Tell();
        last;
    }
#
# read ID3v1 trailer if it exists
#
    if ($raf->Seek(-128, 2) and $raf->Read($tBuff, 128) == 128 and $tBuff =~ /^TAG/) {
        $exifTool->{DoneID3} = 2;  # set to 2 as flag that trailer exists
        %id3Trailer = (
            DataPt   => \$tBuff,
            DataPos  => $raf->Tell() - 128,
            DirStart => 0,
            DirLen   => length($tBuff),
        );
        $id3Len += length($tBuff);
        $rtnVal = 1;
        # load 'Enhanced TAG' information if available
        if ($raf->Seek(-355, 2) and $raf->Read($eBuff, 227) == 227 and $eBuff =~ /^TAG+/) {
            $id3Trailer{EnhancedTAG} = \$eBuff;
        }
    }
#
# process the the information
#
    if ($rtnVal) {
        # first process audio data if it exists
        if ($$dirInfo{RAF}) {
            my $oldType = $exifTool->{FILE_TYPE};   # save file type
            # check current file type first
            my @types = grep /^$oldType$/, @audioFormats;
            push @types, grep(!/^$oldType$/, @audioFormats);
            my $type;
            foreach $type (@types) {
                # seek to end of ID3 header
                $raf->Seek($hdrEnd, 0);
                # set type for this file if we are successful
                $exifTool->{FILE_TYPE} = $type;
                my $module = $audioModule{$type} || $type;
                require "Image/ExifTool/$module.pm" or next;
                my $func = "Image::ExifTool::${module}::Process$type";
                # process the file
                no strict 'refs';
                &$func($exifTool, $dirInfo) and last;
                use strict 'refs';
            }
            $exifTool->{FILE_TYPE} = $oldType;      # restore original file type
        }
        # set file type to MP3 if we didn't find audio data
        $exifTool->SetFileType('MP3');
        # record the size if the ID3 metadata
        $exifTool->FoundTag('ID3Size', $id3Len);
        # process ID3v2 header if it exists
        if (%id3Header) {
            $exifTool->VPrint(0, "$id3Header{DirName}:\n");
            $exifTool->ProcessDirectory(\%id3Header, $tagTablePtr);
        }
        # process ID3v1 trailer if it exists
        if (%id3Trailer) {
            $exifTool->VPrint(0, "ID3v1:\n");
            SetByteOrder('MM');
            $tagTablePtr = GetTagTable('Image::ExifTool::ID3::v1');
            $exifTool->ProcessDirectory(\%id3Trailer, $tagTablePtr);
            # process "Enhanced TAG" information if available
            if ($id3Trailer{EnhancedTAG}) {
                $exifTool->VPrint(0, "ID3v1 Enhanced TAG:\n");
                $tagTablePtr = GetTagTable('Image::ExifTool::ID3::v1_Enh');
                $id3Trailer{DataPt} = $id3Trailer{EnhancedTAG};
                $id3Trailer{DataPos} -= 227; # (227 = length of Enhanced TAG block)
                $id3Trailer{DirLen} = 227;
                $exifTool->ProcessDirectory(\%id3Trailer, $tagTablePtr);
            }
        }
    }
    # return file pointer to start of file to read audio data if necessary
    $raf->Seek(0, 0);
    return $rtnVal;
}

#------------------------------------------------------------------------------
# Extract ID3 information from an MP3 audio file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid MP3 file
sub ProcessMP3($$)
{
    my ($exifTool, $dirInfo) = @_;

    # must first check for leading/trailing ID3 information
    unless ($exifTool->{DoneID3}) {
        ProcessID3($exifTool, $dirInfo) and return 1;
    }
    my $raf = $$dirInfo{RAF};
    my $rtnVal = 0;
    my $buff;
#
# extract information from first audio/video frame headers
# (if found in the first $scanLen bytes)
#
    # scan further into a file that should be an MP3
    my $scanLen = ($$exifTool{FILE_EXT} and $$exifTool{FILE_EXT} eq 'MP3') ? 8192 : 256;
    if ($raf->Read($buff, $scanLen)) {
        require Image::ExifTool::MPEG;
        if ($buff =~ /\0\0\x01(\xb3|\xc0)/) {
            # look for A/V headers in first 64kB
            my $buf2;
            $raf->Read($buf2, 0x10000 - $scanLen) and $buff .= $buf2;
            $rtnVal = 1 if Image::ExifTool::MPEG::ParseMPEGAudioVideo($exifTool, \$buff);
        } else {
            # look for audio frame sync in first $scanLen bytes
            $rtnVal = 1 if Image::ExifTool::MPEG::ParseMPEGAudio($exifTool, \$buff);
        }
    }
    return $rtnVal;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::ID3 - Read ID3 meta information

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to extract ID3
information from audio files.  ID3 information is found in MP3 and various
other types of audio files.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.id3.org/>

=item L<http://www.mp3-tech.org/>

=item L<http://www.fortunecity.com/underworld/sonic/3/id3tag.html>

=back

=head1 SEE ALSO

L<Image::ExifTool::TagNames/ID3 Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

