#------------------------------------------------------------------------------
# File:         EXE.pm
#
# Description:  Read meta information of various executable file formats
#
# Revisions:    2008/08/28 - P. Harvey Created
#               2011/07/12 - P. Harvey Added CHM (ok, not EXE, but it fits here)
#
# References:   1) http://www.openwatcom.org/ftp/devel/docs/pecoff.pdf
#               2) http://support.microsoft.com/kb/65122
#               3) http://www.opensource.apple.com
#               4) http://www.skyfree.org/linux/references/ELF_Format.pdf
#               5) http://msdn.microsoft.com/en-us/library/ms809762.aspx
#               6) http://code.google.com/p/pefile/
#               7) http://www.codeproject.com/KB/DLL/showver.aspx
#------------------------------------------------------------------------------

package Image::ExifTool::EXE;

use strict;
use vars qw($VERSION);
use Image::ExifTool qw(:DataAccess :Utils);

$VERSION = '1.05';

sub ProcessPEResources($$);
sub ProcessPEVersion($$);

# PE file resource types (ref 6)
my %resourceType = (
    1 => 'Cursor',
    2 => 'Bitmap',
    3 => 'Icon',
    4 => 'Menu',
    5 => 'Dialog',
    6 => 'String',
    7 => 'Font Dir',
    8 => 'Font',
    9 => 'Accelerator',
    10 => 'RC Data',
    11 => 'Message Table',
    12 => 'Group Cursor',
    14 => 'Group Icon',
    16 => 'Version',
    17 => 'Dialog Include',
    19 => 'Plug-n-Play',
    20 => 'VxD',
    21 => 'Animated Cursor',
    22 => 'Animated Icon',
    23 => 'HTML',
    24 => 'Manifest',
);

my %languageCode = (
    '0000' => 'Neutral',
    '007F' => 'Invariant',
    '0400' => 'Process default',
    '0401' => 'Arabic',
    '0402' => 'Bulgarian',
    '0403' => 'Catalan',
    '0404' => 'Chinese (Traditional)',
    '0405' => 'Czech',
    '0406' => 'Danish',
    '0407' => 'German',
    '0408' => 'Greek',
    '0409' => 'English (U.S.)',
    '040A' => 'Spanish (Castilian)',
    '040B' => 'Finnish',
    '040C' => 'French',
    '040D' => 'Hebrew',
    '040E' => 'Hungarian',
    '040F' => 'Icelandic',
    '0410' => 'Italian',
    '0411' => 'Japanese',
    '0412' => 'Korean',
    '0413' => 'Dutch',
    '0414' => 'Norwegian (Bokml)',
    '0415' => 'Polish',
    '0416' => 'Portuguese (Brazilian)',
    '0417' => 'Rhaeto-Romanic',
    '0418' => 'Romanian',
    '0419' => 'Russian',
    '041A' => 'Croato-Serbian (Latin)',
    '041B' => 'Slovak',
    '041C' => 'Albanian',
    '041D' => 'Swedish',
    '041E' => 'Thai',
    '041F' => 'Turkish',
    '0420' => 'Urdu',
    # 0421-0493 ref 6
    '0421' => 'Indonesian',
    '0422' => 'Ukrainian',
    '0423' => 'Belarusian',
    '0424' => 'Slovenian',
    '0425' => 'Estonian',
    '0426' => 'Latvian',
    '0427' => 'Lithuanian',
    '0428' => 'Maori',
    '0429' => 'Farsi',
    '042a' => 'Vietnamese',
    '042b' => 'Armenian',
    '042c' => 'Azeri',
    '042d' => 'Basque',
    '042e' => 'Sorbian',
    '042f' => 'Macedonian',
    '0430' => 'Sutu',
    '0431' => 'Tsonga',
    '0432' => 'Tswana',
    '0433' => 'Venda',
    '0434' => 'Xhosa',
    '0435' => 'Zulu',
    '0436' => 'Afrikaans',
    '0437' => 'Georgian',
    '0438' => 'Faeroese',
    '0439' => 'Hindi',
    '043a' => 'Maltese',
    '043b' => 'Saami',
    '043c' => 'Gaelic',
    '043e' => 'Malay',
    '043f' => 'Kazak',
    '0440' => 'Kyrgyz',
    '0441' => 'Swahili',
    '0443' => 'Uzbek',
    '0444' => 'Tatar',
    '0445' => 'Bengali',
    '0446' => 'Punjabi',
    '0447' => 'Gujarati',
    '0448' => 'Oriya',
    '0449' => 'Tamil',
    '044a' => 'Telugu',
    '044b' => 'Kannada',
    '044c' => 'Malayalam',
    '044d' => 'Assamese',
    '044e' => 'Marathi',
    '044f' => 'Sanskrit',
    '0450' => 'Mongolian',
    '0456' => 'Galician',
    '0457' => 'Konkani',
    '0458' => 'Manipuri',
    '0459' => 'Sindhi',
    '045a' => 'Syriac',
    '0460' => 'Kashmiri',
    '0461' => 'Nepali',
    '0465' => 'Divehi',
    '047f' => 'Invariant',
    '048f' => 'Esperanto',
    '0490' => 'Walon',
    '0491' => 'Cornish',
    '0492' => 'Welsh',
    '0493' => 'Breton',
    '0800' => 'Neutral 2',
    '0804' => 'Chinese (Simplified)',
    '0807' => 'German (Swiss)',
    '0809' => 'English (British)',
    '080A' => 'Spanish (Mexican)',
    '080C' => 'French (Belgian)',
    '0810' => 'Italian (Swiss)',
    '0813' => 'Dutch (Belgian)',
    '0814' => 'Norwegian (Nynorsk)',
    '0816' => 'Portuguese',
    '081A' => 'Serbo-Croatian (Cyrillic)',
    '0C07' => 'German (Austrian)',
    '0C09' => 'English (Australian)',
    '0C0A' => 'Spanish (Modern)',
    '0C0C' => 'French (Canadian)',
    '1009' => 'English (Canadian)',
    '100C' => 'French (Swiss)',
);

# Information extracted from PE COFF (Windows EXE) file header
%Image::ExifTool::EXE::Main = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    FORMAT => 'int16u',
    NOTES => q{
        This module extracts information from various types of Windows, MacOS and
        Unix executable and library files.  The first table below lists information
        extracted from the header of Windows PE (Portable Executable) EXE files and
        DLL libraries.
    },
    0 => {
        Name => 'MachineType',
        PrintHex => 1,
        PrintConv => {
            0x014c => 'Intel 386 or later, and compatibles',
            0x014d => 'Intel i860', #5
            0x0162 => 'MIPS R3000',
            0x0166 => 'MIPS little endian (R4000)',
            0x0168 => 'MIPS R10000',
            0x0169 => 'MIPS little endian WCI v2',
            0x0183 => 'Alpha AXP (old)', #5
            0x0184 => 'Alpha AXP',
            0x01a2 => 'Hitachi SH3',
            0x01a3 => 'Hitachi SH3 DSP',
            0x01a6 => 'Hitachi SH4',
            0x01a8 => 'Hitachi SH5',
            0x01c0 => 'ARM little endian',
            0x01c2 => 'Thumb',
            0x01d3 => 'Matsushita AM33',
            0x01f0 => 'PowerPC little endian',
            0x01f1 => 'PowerPC with floating point support',
            0x0200 => 'Intel IA64',
            0x0266 => 'MIPS16',
            0x0268 => 'Motorola 68000 series',
            0x0284 => 'Alpha AXP 64-bit',
            0x0366 => 'MIPS with FPU',
            0x0466 => 'MIPS16 with FPU',
            0x0ebc => 'EFI Byte Code',
            0x8664 => 'AMD AMD64',
            0x9041 => 'Mitsubishi M32R little endian',
            0xc0ee => 'clr pure MSIL',
        },
    },
    2 => {
        Name => 'TimeStamp',
        Format => 'int32u',
        Groups => { 2 => 'Time' },
        ValueConv => 'ConvertUnixTime($val,1)',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    10 => {
        Name => 'PEType',
        PrintHex => 1,
        PrintConv => {
            0x10b => 'PE32',
            0x20b => 'PE32+',
        },
    },
    11 => {
        Name => 'LinkerVersion',
        Format => 'int8u[2]',
        ValueConv => '$val=~tr/ /./; $val',
    },
    12 => {
        Name => 'CodeSize',
        Format => 'int32u',
    },
    14 => {
        Name => 'InitializedDataSize',
        Format => 'int32u',
    },
    16 => {
        Name => 'UninitializedDataSize',
        Format => 'int32u',
    },
    18 => {
        Name => 'EntryPoint',
        Format => 'int32u',
        PrintConv => 'sprintf("0x%.4x", $val)',
    },
    30 => {
        Name => 'OSVersion',
        Format => 'int16u[2]',
        ValueConv => '$val=~tr/ /./; $val',
    },
    32 => {
        Name => 'ImageVersion',
        Format => 'int16u[2]',
        ValueConv => '$val=~tr/ /./; $val',
    },
    34 => {
        Name => 'SubsystemVersion',
        Format => 'int16u[2]',
        ValueConv => '$val=~tr/ /./; $val',
    },
    44 => {
        Name => 'Subsystem',
        PrintConv => {
            0 => 'Unknown',
            1 => 'Native',
            2 => 'Windows GUI',
            3 => 'Windows command line',
            5 => 'OS/2 Command line', #5
            7 => 'POSIX command line',
            9 => 'Windows CE GUI',
            10 => 'EFI application',
            11 => 'EFI boot service',
            12 => 'EFI runtime driver',
            13 => 'EFI ROM', #6
            14 => 'XBOX', #6
        },
    },
);

# PE file version information (ref 6)
%Image::ExifTool::EXE::PEVersion = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    FORMAT => 'int32u',
    NOTES => q{
        Information extracted from the VS_VERSION_INFO structure of Windows PE
        files.
    },
    # (boring -- always 0xfeef04bd)
    #0 => {
    #    Name => 'Signature',
    #    PrintConv => 'sprintf("0x%.4x",$val)',
    #},
    # (boring -- always 1.0)
    #1 => {
    #    Name => 'StructVersion',
    #    Format => 'int16u[2]',
    #    ValueConv => 'my @a=split(" ",$val); "$a[1].$a[0]"',
    #},
    2 => {
        Name => 'FileVersionNumber',
        Format => 'int16u[4]',
        ValueConv => 'my @a=split(" ",$val); "$a[1].$a[0].$a[3].$a[2]"',
    },
    4 => {
        Name => 'ProductVersionNumber',
        Format => 'int16u[4]',
        ValueConv => 'my @a=split(" ",$val); "$a[1].$a[0].$a[3].$a[2]"',
    },
    6 => {
        Name => 'FileFlagsMask',
        PrintConv => 'sprintf("0x%.4x",$val)',
    },
    7 => { # ref Cygwin /usr/include/w32api/winver.h
        Name => 'FileFlags',
        PrintConv => { BITMASK => {
            0 => 'Debug',
            1 => 'Pre-release',
            2 => 'Patched',
            3 => 'Private build',
            4 => 'Info inferred',
            5 => 'Special build',
        }},
    },
    8 => {
        Name => 'FileOS',
        PrintHex => 1,
        PrintConv => { # ref Cygwin /usr/include/w32api/winver.h
            0x00001 => 'Win16',
            0x00002 => 'PM-16',
            0x00003 => 'PM-32',
            0x00004 => 'Win32',
            0x10000 => 'DOS',
            0x20000 => 'OS/2 16-bit',
            0x30000 => 'OS/2 32-bit',
            0x40000 => 'Windows NT',
            0x10001 => 'Windows 16-bit',
            0x10004 => 'Windows 32-bit',
            0x20002 => 'OS/2 16-bit PM-16',
            0x30003 => 'OS/2 32-bit PM-32',
            0x40004 => 'Windows NT 32-bit',
        },
    },
    9 => { # ref Cygwin /usr/include/w32api/winver.h
        Name => 'ObjectFileType',
        PrintConv => {
            0 => 'Unknown',
            1 => 'Executable application',
            2 => 'Dynamic link library',
            3 => 'Driver',
            4 => 'Font',
            5 => 'VxD',
            7 => 'Static library',
        },
    },
    10 => 'FileSubtype',
    # (these are usually zero, so ignore them)
    # 11 => 'FileDateMS',
    # 12 => 'FileDateLS',
);

# Windows PE StringFileInfo resource strings
# (see http://msdn.microsoft.com/en-us/library/aa381049.aspx)
%Image::ExifTool::EXE::PEString = (
    GROUPS => { 2 => 'Other' },
    VARS => { NO_ID => 1 },
    NOTES => q{
        Resource strings found in Windows PE files.  The B<TagID>'s are not shown
        because they are the same as the B<Tag Name>.  ExifTool will extract any
        existing StringFileInfo tags even if not listed in this table.
    },
    LanguageCode => {
        Notes => 'extracted from the StringFileInfo value',
        # ref http://techsupt.winbatch.com/TS/T000001050F49.html
        # (also see http://support.bigfix.com/fixlet/documents/WinInspectors-2006-08-10.pdf)
        # (also see ftp://ftp.dyu.edu.tw/pub/cpatch/faq/tech/tech_nlsnt.txt)
        # (not a complete set)
        PrintString => 1,
        SeparateTable => 1,
        PrintConv => \%languageCode,
    },
    CharacterSet => {
        Notes => 'extracted from the StringFileInfo value',
        # ref http://techsupt.winbatch.com/TS/T000001050F49.html
        # (also see http://blog.chinaunix.net/u1/41189/showart_345768.html)
        PrintString => 1,
        PrintConv => {
            '0000' => 'ASCII',
            '03A4' => 'Windows, Japan (Shift - JIS X-0208)', # cp932
            '03A8' => 'Windows, Chinese (Simplified)', # cp936
            '03B5' => 'Windows, Korea (Shift - KSC 5601)', # cp949
            '03B6' => 'Windows, Taiwan (Big5)', # cp950
            '04B0' => 'Unicode', # UCS-2
            '04E2' => 'Windows, Latin2 (Eastern European)',
            '04E3' => 'Windows, Cyrillic',
            '04E4' => 'Windows, Latin1',
            '04E5' => 'Windows, Greek',
            '04E6' => 'Windows, Turkish',
            '04E7' => 'Windows, Hebrew',
            '04E8' => 'Windows, Arabic',
        },
    },
    BuildDate       => { Groups => { 2 => 'Time' } }, # (non-standard)
    BuildVersion    => { }, # (non-standard)
    Comments        => { },
    CompanyName     => { },
    Copyright       => { }, # (non-standard)
    FileDescription => { },
    FileVersion     => { },
    InternalName    => { },
    LegalCopyright  => { },
    LegalTrademarks => { },
    OriginalFilename=> { },
    PrivateBuild    => { },
    ProductName     => { },
    ProductVersion  => { },
    SpecialBuild    => { },
);

# Information extracted from Mach-O (Mac OS X) file header
%Image::ExifTool::EXE::MachO = (
    GROUPS => { 2 => 'Other' },
    VARS => { ID_LABEL => 'Index' },
    NOTES => q{
        Information extracted from Mach-O (Mac OS X) executable files and DYLIB
        libraries.
    },
    # ref http://www.opensource.apple.com/darwinsource/DevToolsOct2007/cctools-622.9/include/mach/machine.h
    0 => 'CPUArchitecture',
    1 => 'CPUByteOrder',
    2 => 'CPUCount',
    # ref /System/Library/Frameworks/Kernel.framework/Versions/A/Headers/mach/machine.h
    3 => {
        Name => 'CPUType',
        List => 1,
        PrintConv => {
            # handle 64-bit flag (0x1000000)
            OTHER => sub {
                my ($val, $inv, $conv) = @_;
                my $v = $val & 0xfeffffff;
                return $$conv{$v} ? "$$conv{$v} 64-bit" : "Unknown ($val)";
            },
            -1 => 'Any',
            1 => 'VAX',
            2 => 'ROMP',
            4 => 'NS32032',
            5 => 'NS32332',
            6 => 'MC680x0',
            7 => 'x86',
            8 => 'MIPS',
            9 => 'NS32532',
            10 => 'MC98000',
            11 => 'HPPA',
            12 => 'ARM',
            13 => 'MC88000',
            14 => 'SPARC',
            15 => 'i860 big endian',
            16 => 'i860 little endian',
            17 => 'RS6000',
            18 => 'PowerPC',
            255 => 'VEO',
        },
    },
    # ref /System/Library/Frameworks/Kernel.framework/Versions/A/Headers/mach/machine.h
    4 => {
        Name => 'CPUSubtype',
        List => 1,
        PrintConv => {
            # handle 64-bit flags on CPUType (0x1000000) and CPUSubtype (0x80000000)
            OTHER => sub {
                my ($val, $inv, $conv) = @_;
                my @v = split ' ', $val;
                my $v = ($v[0] & 0xfeffffff) . ' ' . ($v[1] & 0x7fffffff);
                return $$conv{$v} ? "$$conv{$v} 64-bit" : "Unknown ($val)";
            },
            # in theory, subtype can be -1 for multiple CPU types,
            # but in practice I'm not sure anyone uses this - PH
            '1 0' => 'VAX (all)',
            '1 1' => 'VAX780',
            '1 2' => 'VAX785',
            '1 3' => 'VAX750',
            '1 4' => 'VAX730',
            '1 5' => 'UVAXI',
            '1 6' => 'UVAXII',
            '1 7' => 'VAX8200',
            '1 8' => 'VAX8500',
            '1 9' => 'VAX8600',
            '1 10' => 'VAX8650',
            '1 11' => 'VAX8800',
            '1 12' => 'UVAXIII',
            '2 0' => 'RT (all)',
            '2 1' => 'RT PC',
            '2 2' => 'RT APC',
            '2 3' => 'RT 135',
            # 32032/32332/32532 subtypes.
            '4 0' => 'NS32032 (all)',
            '4 1' => 'NS32032 DPC (032 CPU)',
            '4 2' => 'NS32032 SQT',
            '4 3' => 'NS32032 APC FPU (32081)',
            '4 4' => 'NS32032 APC FPA (Weitek)',
            '4 5' => 'NS32032 XPC (532)',
            '5 0' => 'NS32332 (all)',
            '5 1' => 'NS32332 DPC (032 CPU)',
            '5 2' => 'NS32332 SQT',
            '5 3' => 'NS32332 APC FPU (32081)',
            '5 4' => 'NS32332 APC FPA (Weitek)',
            '5 5' => 'NS32332 XPC (532)',
            '6 1' => 'MC680x0 (all)',
            '6 2' => 'MC68040',
            '6 3' => 'MC68030',
            '7 3' => 'i386 (all)',
            '7 4' => 'i486',
            '7 132' => 'i486SX',
            '7 5' => 'i586',
            '7 22' => 'Pentium Pro',
            '7 54' => 'Pentium II M3',
            '7 86' => 'Pentium II M5',
            '7 103' => 'Celeron',
            '7 119' => 'Celeron Mobile',
            '7 8' => 'Pentium III',
            '7 24' => 'Pentium III M',
            '7 40' => 'Pentium III Xeon',
            '7 9' => 'Pentium M',
            '7 10' => 'Pentium 4',
            '7 26' => 'Pentium 4 M',
            '7 11' => 'Itanium',
            '7 27' => 'Itanium 2',
            '7 12' => 'Xeon',
            '7 28' => 'Xeon MP',
            '8 0' => 'MIPS (all)',
            '8 1' => 'MIPS R2300',
            '8 2' => 'MIPS R2600',
            '8 3' => 'MIPS R2800',
            '8 4' => 'MIPS R2000a',
            '8 5' => 'MIPS R2000',
            '8 6' => 'MIPS R3000a',
            '8 7' => 'MIPS R3000',
            '10 0' => 'MC98000 (all)',
            '10 1' => 'MC98601',
            '11 0' => 'HPPA (all)',
            '11 1' => 'HPPA 7100LC',
            '12 0' => 'ARM (all)',
            '12 1' => 'ARM A500 ARCH',
            '12 2' => 'ARM A500',
            '12 3' => 'ARM A440',
            '12 4' => 'ARM M4',
            '12 5' => 'ARM A680/V4T',
            '12 6' => 'ARM V6',
            '12 7' => 'ARM V5TEJ',
            '12 8' => 'ARM XSCALE',
            '12 9' => 'ARM V7',
            '13 0' => 'MC88000 (all)',
            '13 1' => 'MC88100',
            '13 2' => 'MC88110',
            '14 0' => 'SPARC (all)',
            '14 1' => 'SUN 4/260',
            '14 2' => 'SUN 4/110',
            '15 0' => 'i860 (all)',
            '15 1' => 'i860 860',
            '16 0' => 'i860 little (all)',
            '16 1' => 'i860 little',
            '17 0' => 'RS6000 (all)',
            '17 1' => 'RS6000',
            '18 0' => 'PowerPC (all)',
            '18 1' => 'PowerPC 601',
            '18 2' => 'PowerPC 602',
            '18 3' => 'PowerPC 603',
            '18 4' => 'PowerPC 603e',
            '18 5' => 'PowerPC 603ev',
            '18 6' => 'PowerPC 604',
            '18 7' => 'PowerPC 604e',
            '18 8' => 'PowerPC 620',
            '18 9' => 'PowerPC 750',
            '18 10' => 'PowerPC 7400',
            '18 11' => 'PowerPC 7450',
            '18 100' => 'PowerPC 970',
            '255 1' => 'VEO 1',
            '255 2' => 'VEO 2',
        },
    },
    5 => {
        Name => 'ObjectFileType',
        PrintHex => 1,
        # ref https://svn.red-bean.com/pyobjc/branches/pyobjc-20x-branch/macholib/macholib/mach_o.py
        PrintConv => {
           -1 => 'Static library', #PH (internal use only)
            1 => 'Relocatable object',
            2 => 'Demand paged executable',
            3 => 'Fixed VM shared library',
            4 => 'Core',
            5 => 'Preloaded executable',
            6 => 'Dynamically bound shared library',
            7 => 'Dynamic link editor',
            8 => 'Dynamically bound bundle',
            9 => 'Shared library stub for static linking',
        },
    },
);

# Information extracted from PEF (Classic MacOS executable) file header
%Image::ExifTool::EXE::PEF = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    NOTES => q{
        Information extracted from PEF (Classic MacOS) executable files and
        libraries.
    },
    FORMAT => 'int32u',
    2 => {
        Name => 'CPUArchitecture',
        Format => 'undef[4]',
        PrintConv => {
            pwpc => 'PowerPC',
            m68k => '68000',
        },
    },
    3 => 'PEFVersion',
    4 => {
        Name => 'TimeStamp',
        Groups => { 2 => 'Time' },
        # timestamp is relative to Jan 1, 1904
        ValueConv => 'ConvertUnixTime($val - ((66 * 365 + 17) * 24 * 3600))',
        PrintConv => '$self->ConvertDateTime($val)',
    },
    #5 => 'OldDefVersion',
    #6 => 'OldImpVersion',
    #7 => 'CurrentVersion',
);

# Information extracted from ELF (Unix executable) file header
%Image::ExifTool::EXE::ELF = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    NOTES => q{
        Information extracted from ELF (Unix) executable files and SO libraries.
    },
    4 => {
        Name => 'CPUArchitecture',
        PrintConv => {
            1 => '32 bit',
            2 => '64 bit',
        },
    },
    5 => {
        Name => 'CPUByteOrder',
        PrintConv => {
            1 => 'Little endian',
            2 => 'Big endian',
        },
    },
    16 => {
        Name => 'ObjectFileType',
        Format => 'int16u',
        PrintConv => {
            0 => 'None',
            1 => 'Relocatable file',
            2 => 'Executable file',
            3 => 'Shared object file',
            4 => 'Core file',
        },
    },
    18 => {
        Name => 'CPUType',
        Format => 'int16u',
        # ref /usr/include/linux/elf-em.h
        PrintConv => {
            0 => 'None',
            1 => 'AT&T WE 32100',
            2 => 'SPARC',
            3 => 'i386',
            4 => 'Motorola 68000',
            5 => 'Motorola 88000',
            6 => 'i486',
            7 => 'i860',
            8 => 'MIPS R3000',
            10 => 'MIPS R4000',
            15 => 'HPPA',
            18 => 'Sun v8plus',
            20 => 'PowerPC',
            21 => 'PowerPC 64-bit',
            22 => 'IBM S/390',
            23 => 'Cell BE SPU',
            42 => 'SuperH',
            43 => 'SPARC v9 64-bit',
            46 => 'Renesas H8/300,300H,H8S',
            50 => 'HP/Intel IA-64',
            62 => 'AMD x86-64',
            76 => 'Axis Communications 32-bit embedded processor',
            87 => 'NEC v850',
            88 => 'Renesas M32R',
            0x5441 => 'Fujitsu FR-V',
            0x9026 => 'Alpha', # (interim value)
            0x9041 => 'm32r (old)',
            0x9080 => 'v850 (old)',
            0xa390 => 'S/390 (old)',
        },
    },
);

# Microsoft compiled help format (ref http://www.russotto.net/chm/chmformat.html)
%Image::ExifTool::EXE::CHM = (
    PROCESS_PROC => \&Image::ExifTool::ProcessBinaryData,
    GROUPS => { 2 => 'Other' },
    NOTES => 'Tags extracted from Microsoft Compiled HTML files.',
    FORMAT => 'int32u',
    1 => { Name => 'CHMVersion' },
    # 2 - total header length
    # 3 - 1
    # 4 - low bits of date/time value plus 42 (ref http://www.nongnu.org/chmspec/latest/ITSF.html)
    5 => {
        Name => 'LanguageCode',
        SeparateTable => 1,
        ValueConv => 'sprintf("%.4X", $val)',
        PrintConv => \%languageCode,
    },
);

#------------------------------------------------------------------------------
# Extract information from a CHM file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid CHM file
sub ProcessCHM($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my $buff;

    return 0 unless $raf->Read($buff, 56) == 56 and
        $buff =~ /^ITSF.{20}\x10\xfd\x01\x7c\xaa\x7b\xd0\x11\x9e\x0c\0\xa0\xc9\x22\xe6\xec/;
    my $tagTablePtr = GetTagTable('Image::ExifTool::EXE::CHM');
    $exifTool->SetFileType();
    SetByteOrder('II');
    $exifTool->ProcessDirectory({ DataPt => \$buff }, $tagTablePtr);
    return 1;
}

#------------------------------------------------------------------------------
# Read Unicode string (null terminated) from resource
# Inputs: 0) data ref, 1) start offset, 2) optional ExifTool object ref
# Returns: 0) Unicode string translated to UTF8, or current CharSet with ExifTool ref
#          1) end pos (rounded up to nearest 4 bytes)
sub ReadUnicodeStr($$;$)
{
    my ($dataPt, $pos, $exifTool) = @_;
    my $len = length $$dataPt;
    my $str = '';
    while ($pos + 2 <= $len) {
        my $ch = substr($$dataPt, $pos, 2);
        $pos += 2;
        last if $ch eq "\0\0";
        $str .= $ch;
    }
    $pos += 2 if $pos & 0x03;
    my $to = $exifTool ? $exifTool->Options('Charset') : 'UTF8';
    return (Image::ExifTool::Decode(undef,$str,'UCS2','II',$to), $pos);
}

#------------------------------------------------------------------------------
# Process Windows PE Version Resource
# Inputs: 0) ExifTool object ref, 1) dirInfo ref
# Returns: true on success
sub ProcessPEVersion($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $dataPt = $$dirInfo{DataPt};
    my $pos = $$dirInfo{DirStart};
    my $end = $pos + $$dirInfo{DirLen};
    my ($index, $len, $valLen, $type, $string, $strEnd);

    # get VS_VERSION_INFO
    for ($index = 0; ; ++$index) {
        $pos = ($pos + 3) & 0xfffffffc;  # align on a 4-byte boundary
        last if $pos + 6 > $end;
        $len = Get16u($dataPt, $pos);
        $valLen = Get16u($dataPt, $pos + 2);
        $type = Get16u($dataPt, $pos + 4);
        return 0 unless $len or $valLen;  # prevent possible infinite loop
        ($string, $strEnd) = ReadUnicodeStr($dataPt, $pos + 6);
        return 0 if $strEnd + $valLen > $end;
        unless ($index or $string eq 'VS_VERSION_INFO') {
            $exifTool->Warn('Invalid Version Info block');
            return 0;
        }
        if ($string eq 'VS_VERSION_INFO') {
            # parse the fixed version info
            $$dirInfo{DirStart} = $strEnd;
            $$dirInfo{DirLen} = $valLen;
            my $subTablePtr = GetTagTable('Image::ExifTool::EXE::PEVersion');
            $exifTool->ProcessDirectory($dirInfo, $subTablePtr);
            $pos = $strEnd + $valLen;
        } elsif ($string eq 'StringFileInfo' and $valLen == 0) {
            $pos += $len;
            my $pt = $strEnd;
            # parse string table
            my $tagTablePtr = GetTagTable('Image::ExifTool::EXE::PEString');
            for ($index = 0; $pt + 6 < $pos; ++$index) {
                $len = Get16u($dataPt, $pt);
                $valLen = Get16u($dataPt, $pt + 2);
                # $type = Get16u($dataPt, $pt + 4);
                # get tag ID (converted to UTF8)
                ($string, $pt) = ReadUnicodeStr($dataPt, $pt + 6);
                unless ($index) {
                    # separate the language code and character set
                    # (not sure what the CharacterSet tag is for, but the string
                    # values stored here are UCS-2 in all my files even if the
                    # CharacterSet is otherwise)
                    my $char;
                    if (length($string) > 4) {
                        $char = substr($string, 4);
                        $string = substr($string, 0, 4);
                    }
                    $exifTool->HandleTag($tagTablePtr, 'LanguageCode', uc $string);
                    $exifTool->HandleTag($tagTablePtr, 'CharacterSet', uc $char) if $char;
                    next;
                }
                my $tag = $string;
                # create entry in tag table if it doesn't already exist
                unless ($$tagTablePtr{$tag}) {
                    my $name = $tag;
                    $name =~ tr/-_a-zA-Z0-9//dc; # remove illegal characters
                    next unless length $name;
                    Image::ExifTool::AddTagToTable($tagTablePtr, $tag, { Name => $name });
                }
                # get tag value (converted to current Charset)
                if ($valLen) {
                    ($string, $pt) = ReadUnicodeStr($dataPt, $pt, $exifTool);
                } else {
                    $string = '';
                }
                $exifTool->HandleTag($tagTablePtr, $tag, $string);
            }
        } else {
            $pos += $len + $valLen;
            # ignore other information (for now)
        }
    }
    return 1;
}

#------------------------------------------------------------------------------
# Process Windows PE Resources
# Inputs: 0) ExifTool object ref, 1) dirInfo ref
# Returns: true on success
sub ProcessPEResources($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my $base = $$dirInfo{Base};
    my $dirStart = $$dirInfo{DirStart} + $base;
    my $level = $$dirInfo{Level} || 0;
    my $verbose = $exifTool->Options('Verbose');
    my ($buff, $buf2, $item);

    return 0 if $level > 10;    # protect against deep recursion
    # read the resource header
    $raf->Seek($dirStart, 0) and $raf->Read($buff, 16) == 16 or return 0;
    my $nameEntries = Get16u(\$buff, 12);
    my $idEntries = Get16u(\$buff, 14);
    my $count = $nameEntries + $idEntries;
    $raf->Read($buff, $count * 8) == $count * 8 or return 0;
    # loop through all resource entries
    for ($item=0; $item<$count; ++$item) {
        my $pos = $item * 8;
        my $name = Get32u(\$buff, $pos);
        my $entryPos = Get32u(\$buff, $pos + 4);
        unless ($level) {
            # set resource type if this is the 0th level directory
            my $resType = $resourceType{$name} || sprintf('Unknown (0x%x)', $name);
            # ignore everything but the Version resource unless verbose
            if ($verbose) {
                $exifTool->VPrint(0, "$resType resource:\n");
            } else {
                next unless $resType eq 'Version';
            }
            $$dirInfo{ResType} = $resType;
        }
        if ($entryPos & 0x80000000) { # is this a directory?
            # descend into next directory level
            $$dirInfo{DirStart} = $entryPos & 0x7fffffff;
            $$dirInfo{Level} = $level + 1;
            ProcessPEResources($exifTool, $dirInfo) or return 0;
            --$$dirInfo{Level};
        } elsif ($$dirInfo{ResType} eq 'Version' and $level == 2 and
            not $$dirInfo{GotVersion}) # (only process first Version resource)
        {
            # get position of this resource in the file
            my $buf2;
            $raf->Seek($entryPos + $base, 0) and $raf->Read($buf2, 16) == 16 or return 0;
            my $off = Get32u(\$buf2, 0);
            my $len = Get32u(\$buf2, 4);
            # determine which section this is in so we can convert the virtual address
            my ($section, $filePos);
            foreach $section (@{$$dirInfo{Sections}}) {
                next unless $off >= $$section{VirtualAddress} and
                            $off <  $$section{VirtualAddress} + $$section{Size};
                $filePos = $off + $$section{Base} - $$section{VirtualAddress};
                last;
            }
            return 0 unless $filePos;
            $raf->Seek($filePos, 0) and $raf->Read($buf2, $len) == $len or return 0;
            ProcessPEVersion($exifTool, {
                DataPt   => \$buf2,
                DataLen  => $len,
                DirStart => 0,
                DirLen   => $len,
            }) or $exifTool->Warn('Possibly corrupt Version resource');
            $$dirInfo{GotVersion} = 1;  # set flag so we don't do this again
        }
    }
    return 1;
}

#------------------------------------------------------------------------------
# Process Windows PE file data dictionary
# Inputs: 0) ExifTool object ref, 1) dirInfo ref
# Returns: true on success
sub ProcessPEDict($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my $dataPt = $$dirInfo{DataPt};
    my $dirLen = length($$dataPt);
    my ($pos, @sections, %dirInfo);

    # loop through all sections
    for ($pos=0; $pos+40<=$dirLen; $pos+=40) {
        my $name = substr($$dataPt, $pos, 8);
        my $va = Get32u($dataPt, $pos + 12);
        my $size = Get32u($dataPt, $pos + 16);
        my $offset = Get32u($dataPt, $pos + 20);
        # remember the section offsets for the VirtualAddress lookup later
        push @sections, { Base => $offset, Size => $size, VirtualAddress => $va };
        # save details of the first resource section
        %dirInfo = (
            RAF      => $raf,
            Base     => $offset,
            DirStart => 0,   # (relative to Base)
            DirLen   => $size,
            Sections => \@sections,
        ) if $name eq ".rsrc\0\0\0" and not %dirInfo;
    }
    # process the first resource section
    ProcessPEResources($exifTool, \%dirInfo) or return 0 if %dirInfo;
    return 1;
}

#------------------------------------------------------------------------------
# Extract information from an EXE file
# Inputs: 0) ExifTool object reference, 1) dirInfo reference
# Returns: 1 on success, 0 if this wasn't a valid EXE file
sub ProcessEXE($$)
{
    my ($exifTool, $dirInfo) = @_;
    my $raf = $$dirInfo{RAF};
    my ($buff, $buf2, $type, $tagTablePtr, %dirInfo);

    my $size = $raf->Read($buff, 0x40) or return 0;
#
# DOS and Windows EXE
#
    if ($buff =~ /^MZ/ and $size == 0x40) {
        # DOS/Windows executable
        # validate DOS header
        # (ref http://www.delphidabbler.com/articles?article=8&part=2)
        #   0  magic   : int16u    # Magic number ("MZ")
        #   2  cblp    : int16u    # Bytes on last page of file
        #   4  cp      : int16u    # Pages in file
        #   6  crlc    : int16u    # Relocations
        #   8  cparhdr : int16u    # Size of header in paragraphs
        #  10  minalloc: int16u    # Minimum extra paragraphs needed
        #  12  maxalloc: int16u    # Maximum extra paragraphs needed
        #  14  ss      : int16u    # Initial (relative) SS value
        #  16  sp      : int16u    # Initial SP value
        #  18  csum    : int16u    # Checksum
        #  20  ip      : int16u    # Initial IP value
        #  22  cs      : int16u    # Initial (relative) CS value
        #  24  lfarlc  : int16u    # Address of relocation table
        #  26  ovno    : int16u    # Overlay number
        #  28  res     : int16u[4] # Reserved words
        #  36  oemid   : int16u    # OEM identifier (for oeminfo)
        #  38  oeminfo : int16u    # OEM info; oemid specific
        #  40  res2    : int16u[10]# Reserved words
        #  60  lfanew  : int32u;   # File address of new exe header
        SetByteOrder('II');
        my ($cblp, $cp, $lfarlc, $lfanew) = unpack('x2v2x18vx34V', $buff);
        my $fileSize = ($cp - ($cblp ? 1 : 0)) * 512 + $cblp;
        #(patch to accommodate observed 64-bit files)
        #return 0 if $fileSize < 0x40 or $fileSize < $lfarlc;
        return 0 if $fileSize < 0x40;
        # read the Windows NE, PE or LE (virtual device driver) header
        #if ($lfarlc == 0x40 and $fileSize > $lfanew + 2 and ...
        if ($raf->Seek($lfanew, 0) and $raf->Read($buff, 0x40) and $buff =~ /^(NE|PE|LE)/) {
            if ($1 eq 'NE') {
                if ($size >= 0x40) { # NE header is 64 bytes (ref 2)
                    # check for DLL
                    my $appFlags = Get16u(\$buff, 0x0c);
                    $type = 'Win16 ' . ($appFlags & 0x80 ? 'DLL' : 'EXE');
                    # offset 0x02 is 2 bytes with linker version and revision numbers
                    # offset 0x36 is executable type (2 = Windows)
                }
            } elsif ($1 eq 'PE') {
                # PE header comes at byte 4 in buff:
                #   4 int16u Machine
                #   6 int16u NumberOfSections
                #   8 int32u TimeDateStamp
                #  12 int32u PointerToSymbolTable
                #  16 int32u NumberOfSymbols
                #  20 int16u SizeOfOptionalHeader
                #  22 int16u Characteristics
                if ($size >= 24) {  # PE header is 24 bytes (plus optional header)
                    my $machine = $Image::ExifTool::EXE::Main{0}{PrintConv}{Get16u(\$buff, 4)} || '';
                    my $winType = $machine =~ /64/ ? 'Win64' : 'Win32';
                    my $flags = Get16u(\$buff, 22);
                    $exifTool->SetFileType($winType . ' ' . ($flags & 0x2000 ? 'DLL' : 'EXE'));
                    # read the rest of the optional header if necessary
                    my $optSize = Get16u(\$buff, 20);
                    my $more = $optSize + 24 - $size;
                    if ($more > 0) {
                        if ($raf->Read($buf2, $more) == $more) {
                            $buff .= $buf2;
                            $size += $more;
                            my $magic = Get16u(\$buff, 24);
                            # verify PE32/PE32+ magic number
                            unless ($magic == 0x10b or $magic == 0x20b) {
                                $exifTool->Warn('Unknown PE magic number');
                                return 1;
                            }
                        } else {
                            $exifTool->Warn('Error reading optional header');
                        }
                    }
                    # process PE COFF file header
                    $tagTablePtr = GetTagTable('Image::ExifTool::EXE::Main');
                    %dirInfo = (
                        DataPt => \$buff,
                        DataPos => $raf->Tell() - $size,
                        DataLen => $size,
                        DirStart => 4,
                        DirLen => $size,
                    );
                    $exifTool->ProcessDirectory(\%dirInfo, $tagTablePtr);
                    # process data dictionary
                    my $num = Get16u(\$buff, 6);    # NumberOfSections
                    if ($raf->Read($buff, 40 * $num) == 40 * $num) {
                        %dirInfo = (
                            RAF => $raf,
                            DataPt => \$buff,
                        );
                        ProcessPEDict($exifTool, \%dirInfo) or $exifTool->Warn('Error processing PE data dictionary');
                    }
                    return 1;
                }
            } else {
                $type = 'Virtual Device Driver';
            }
        } else {
            $type = 'DOS EXE';
        }
#
# Mach-O (Mac OS X)
#
    } elsif ($buff =~ /^(\xca\xfe\xba\xbe|\xfe\xed\xfa(\xce|\xcf)|(\xce|\xcf)\xfa\xed\xfe)/ and $size > 12) {
        # Mach-O executable
        # (ref http://developer.apple.com/documentation/DeveloperTools/Conceptual/MachORuntime/Reference/reference.html)
        $tagTablePtr = GetTagTable('Image::ExifTool::EXE::MachO');
        if ($1 eq "\xca\xfe\xba\xbe") {
            SetByteOrder('MM');
            $exifTool->SetFileType('Mach-O fat binary executable');
            my $count = Get32u(\$buff, 4);  # get architecture count
            my $more = $count * 20 - ($size - 8);
            if ($more > 0) {
                unless ($raf->Read($buf2, $more) == $more) {
                    $exifTool->Warn('Error reading fat-arch headers');
                    return 1;
                }
                $buff .= $buf2;
                $size += $more;
            }
            $exifTool->HandleTag($tagTablePtr, 2, $count);
            my $i;
            for ($i=0; $i<$count; ++$i) {
                my $cpuType = Get32s(\$buff, 8 + $i * 20);
                my $cpuSubtype = Get32u(\$buff, 12 + $i * 20);
                $exifTool->HandleTag($tagTablePtr, 3, $cpuType);
                $exifTool->HandleTag($tagTablePtr, 4, "$cpuType $cpuSubtype");
            }
            # load first Mach-O header to get the object file type
            my $offset = Get32u(\$buff, 16);
            if ($raf->Seek($offset, 0) and $raf->Read($buf2, 16) == 16) {
                if ($buf2 =~ /^(\xfe\xed\xfa(\xce|\xcf)|(\xce|\xcf)\xfa\xed\xfe)/) {
                    SetByteOrder($buf2 =~ /^\xfe\xed/ ? 'MM' : 'II');
                    my $objType = Get32s(\$buf2, 12);
                    $exifTool->HandleTag($tagTablePtr, 5, $objType);
                } elsif ($buf2 =~ /^!<arch>\x0a/) {
                    # .a libraries use this magic number
                    $exifTool->HandleTag($tagTablePtr, 5, -1);
                } else {
                    $exifTool->Warn('Unrecognized object file type');
                }
            } else {
                $exifTool->Warn('Error reading file');
            }
       } elsif ($size >= 16) {
            $exifTool->SetFileType('Mach-O executable');
            my $info = {
                "\xfe\xed\xfa\xce" => ['32 bit', 'Big endian'],
                "\xce\xfa\xed\xfe" => ['32 bit', 'Little endian'],
                "\xfe\xed\xfa\xcf" => ['64 bit', 'Big endian'],
                "\xcf\xfa\xed\xfe" => ['64 bit', 'Little endian'],
            }->{substr($buff, 0, 4)};
            my $byteOrder = ($buff =~ /^\xfe/) ? 'MM' : 'II';
            SetByteOrder($byteOrder);
            my $cpuType = Get32s(\$buff, 4);
            my $cpuSubtype = Get32s(\$buff, 8);
            my $objType = Get32s(\$buff, 12);
            $exifTool->HandleTag($tagTablePtr, 0, $$info[0]);
            $exifTool->HandleTag($tagTablePtr, 1, $$info[1]);
            $exifTool->HandleTag($tagTablePtr, 3, $cpuType);
            $exifTool->HandleTag($tagTablePtr, 4, "$cpuType $cpuSubtype");
            $exifTool->HandleTag($tagTablePtr, 5, $objType);
        }
        return 1;
#
# PEF (classic MacOS)
#
    } elsif ($buff =~ /^Joy!peff/ and $size > 12) {
        # ref http://developer.apple.com/documentation/mac/pdf/MacOS_RT_Architectures.pdf
        $exifTool->SetFileType('Classic MacOS executable');
        SetByteOrder('MM');
        $tagTablePtr = GetTagTable('Image::ExifTool::EXE::PEF');
        %dirInfo = (
            DataPt => \$buff,
            DataPos => 0,
            DataLen => $size,
            DirStart => 0,
            DirLen => $size,
        );
        $exifTool->ProcessDirectory(\%dirInfo, $tagTablePtr);
        return 1;
#
# ELF (Unix)
#
    } elsif ($buff =~ /^\x7fELF/ and $size >= 16) {
        $exifTool->SetFileType("ELF executable");
        SetByteOrder(Get8u(\$buff,5) == 1 ? 'II' : 'MM');
        $tagTablePtr = GetTagTable('Image::ExifTool::EXE::ELF');
        %dirInfo = (
            DataPt => \$buff,
            DataPos => 0,
            DataLen => $size,
            DirStart => 0,
            DirLen => $size,
        );
        $exifTool->ProcessDirectory(\%dirInfo, $tagTablePtr);
        return 1;
#
# various scripts (perl, sh, etc...)
#
    } elsif ($buff =~ m{^#!\s*/\S*bin/(\w+)}) {
        $type = "$1 script";
#
# .a libraries
#
    } elsif ($buff =~ /^!<arch>\x0a/) {
        $type = 'Static library',
    }
    return 0 unless $type;
    $exifTool->SetFileType($type);
    return 1;
}

1;  # end

__END__

=head1 NAME

Image::ExifTool::EXE - Read executable file meta information

=head1 SYNOPSIS

This module is used by Image::ExifTool

=head1 DESCRIPTION

This module contains definitions required by Image::ExifTool to extract meta
information from various types of Windows, MacOS and Unix executable and
library files.

=head1 AUTHOR

Copyright 2003-2011, Phil Harvey (phil at owl.phy.queensu.ca)

This library is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 REFERENCES

=over 4

=item L<http://www.openwatcom.org/ftp/devel/docs/pecoff.pdf>

=item L<http://support.microsoft.com/kb/65122>

=item L<http://www.opensource.apple.com>

=item L<http://www.skyfree.org/linux/references/ELF_Format.pdf>

=item L<http://msdn.microsoft.com/en-us/library/ms809762.aspx>

=item L<http://code.google.com/p/pefile/>

=item L<http://www.codeproject.com/KB/DLL/showver.aspx>

=back

=head1 SEE ALSO

L<Image::ExifTool::TagNames/EXE Tags>,
L<Image::ExifTool(3pm)|Image::ExifTool>

=cut

