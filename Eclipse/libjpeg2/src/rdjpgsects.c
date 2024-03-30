/*
 * rdjpgsect.c
 *
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/libjpeg2/src/rdjpgsects.c,v 1.4 2010-05-13 19:53:03 cattaneo Exp $
 */

#define JPEG_CJPEG_DJPEG	/* to get the command-line config symbols */
#include "jinclude.h"		/* get auto-config symbols, <stdio.h> */
#include "jpeglib.h"
#include "jerror.h"

#include <ctype.h>		/* to declare isupper(), tolower() */
#ifdef USE_SETMODE
#include <fcntl.h>		/* to declare setmode()'s parameter macros */
/* If you have setmode() but not <io.h>, just delete this line: */
#include <io.h>			/* to declare setmode() */
#endif


#ifdef DONT_USE_B_MODE		/* define mode parameters for fopen() */
#define READ_BINARY	"r"
#else
#define READ_BINARY	"rb"
#endif

#ifndef EXIT_FAILURE		/* define exit() codes if not provided */
#define EXIT_FAILURE  1
#endif
#ifndef EXIT_SUCCESS
#define EXIT_SUCCESS  0
#endif

#define MYMOD
#include "jversion.h"

/*
 * Create the message string table.
 * We do this from the master message list in jerror.h by re-reading
 * jerror.h with a suitable definition for macro JMESSAGE.
 * The message table is made an external symbol just in case any applications
 * want to refer to it directly.
 */

#define JMESSAGE(code,string)	string ,

static const char * const jpeg_std_message_table[] = {
#include "jerror.h"
  NULL
};


/* Error exit handler */
#define ERREXIT(msg)  (fprintf(stderr, "%s\n", msg), exit(EXIT_FAILURE))
#define ERREXIT0(code)  (sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
		fprintf(stderr, fmtbuffer), exit(EXIT_FAILURE))
#define ERREXIT1(code,p1)  (sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
		fprintf(stderr, fmtbuffer, p1), exit( EXIT_FAILURE))

#define WARNMS2(code,p1,p2)  (sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
		fprintf(stderr, fmtbuffer, p1, p2))

#define TRACEMS(lvl,code) 					if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer); }
#define TRACEMS1(lvl,code,p1) 				if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1); }
#define TRACEMS2(lvl,code,p1,p2)  			if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1, p2); }
#define TRACEMS3(lvl,code,p1,p2,p3)  		if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1, p2, p3); }
#define TRACEMS4(lvl,code,p1,p2,p3,p4)  	if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1, p2, p3, p4); }
#define TRACEMS5(lvl,code,p1,p2,p3,p4,p5)  	if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1, p2, p3, p4, p5); }
#define TRACEMS8(lvl,code,p1,p2,p3,p4,p5,p6,p7,p8) 	if (verbose >= lvl) { sprintf( fmtbuffer, "%s\n", jpeg_std_message_table[code]), \
												fprintf(stderr, fmtbuffer, p1, p2, p3, p4, p5, p6, p7, p8); }

static char fmtbuffer[255];

char * getFieldName(unsigned int);

/*
 * These macros are used to read the input file.
 * To reuse this code in another application, you might need to change these.
 */

static FILE * infile;		/* input JPEG file */

static long int StartTiffHeader;

#define SHOWSOS		0x1
#define SHOWSOF		0x2
#define SHOWEXIF	0x4
#define SHOWDAC		0x8
#define SHOWDQT		0x10
#define SHOWDHT		0x20
#define	SHOWDRI		0x40
#define	SHOWAPP		0x80
#define	SHOWCOM		0x100
#define SHOWALL		0xFFFFFF

int ShowSects = 0;
int verbose = 1;

/* Return next input byte, or EOF if no more */
#define NEXTBYTE()  getc(infile)


/*
 * jpeg_natural_order[i] is the natural-order position of the i'th element
 * of zigzag order.
 *
 * When reading corrupted data, the Huffman decoders could attempt
 * to reference an entry beyond the end of this array (if the decoded
 * zero run length reaches past the end of the block).  To prevent
 * wild stores without adding an inner-loop test, we put some extra
 * "63"s after the real entries.  This will cause the extra coefficient
 * to be stored in location 63 of the block, not somewhere random.
 * The worst case would be a run-length of 15, which means we need 16
 * fake entries.
 */

const int jpeg_natural_order[DCTSIZE2+16] = {
  0,  1,  8, 16,  9,  2,  3, 10,
 17, 24, 32, 25, 18, 11,  4,  5,
 12, 19, 26, 33, 40, 48, 41, 34,
 27, 20, 13,  6,  7, 14, 21, 28,
 35, 42, 49, 56, 57, 50, 43, 36,
 29, 22, 15, 23, 30, 37, 44, 51,
 58, 59, 52, 45, 38, 31, 39, 46,
 53, 60, 61, 54, 47, 55, 62, 63,
 63, 63, 63, 63, 63, 63, 63, 63, /* extra entries for safety in decoder */
 63, 63, 63, 63, 63, 63, 63, 63
};

/*
 * JPEG markers consist of one or more 0xFF bytes, followed by a marker
 * code byte (which is not an FF).  Here are the marker codes of interest
 * in this program.  (See jdmarker.c for a more complete list.)
 */
typedef enum {			/* JPEG marker codes */
  M_SOF0  = 0xc0,
  M_SOF1  = 0xc1,
  M_SOF2  = 0xc2,
  M_SOF3  = 0xc3,

  M_SOF5  = 0xc5,
  M_SOF6  = 0xc6,
  M_SOF7  = 0xc7,

  M_JPG   = 0xc8,
  M_SOF9  = 0xc9,
  M_SOF10 = 0xca,
  M_SOF11 = 0xcb,

  M_SOF13 = 0xcd,
  M_SOF14 = 0xce,
  M_SOF15 = 0xcf,

  M_DHT   = 0xc4,

  M_DAC   = 0xcc,

  M_RST0  = 0xd0,
  M_RST1  = 0xd1,
  M_RST2  = 0xd2,
  M_RST3  = 0xd3,
  M_RST4  = 0xd4,
  M_RST5  = 0xd5,
  M_RST6  = 0xd6,
  M_RST7  = 0xd7,

  M_SOI   = 0xd8,
  M_EOI   = 0xd9,
  M_SOS   = 0xda,
  M_DQT   = 0xdb,
  M_DNL   = 0xdc,
  M_DRI   = 0xdd,
  M_DHP   = 0xde,
  M_EXP   = 0xdf,

  M_APP0  = 0xe0,
  M_APP1  = 0xe1,
  M_APP2  = 0xe2,
  M_APP3  = 0xe3,
  M_APP4  = 0xe4,
  M_APP5  = 0xe5,
  M_APP6  = 0xe6,
  M_APP7  = 0xe7,
  M_APP8  = 0xe8,
  M_APP9  = 0xe9,
  M_APP10 = 0xea,
  M_APP11 = 0xeb,
  M_APP12 = 0xec,
  M_APP13 = 0xed,
  M_APP14 = 0xee,
  M_APP15 = 0xef,

  M_JPG0  = 0xf0,
  M_JPG13 = 0xfd,
  M_COM   = 0xfe,

  M_TEM   = 0x01,

  M_ERROR = 0x100
} JPEG_MARKER;



/* Read one byte, testing for EOF */
static int
read_1_byte (void)
{
  int c;

  c = NEXTBYTE();
  if (c == EOF)
    ERREXIT("Premature EOF in JPEG file");
  return c;
}

/* Read 2 bytes, convert to unsigned int */
/* All 2-byte quantities in JPEG markers are MSB first */
static unsigned int
read_2_bytes (void)
{
  int c1, c2;

  c1 = NEXTBYTE();
  if (c1 == EOF)
    ERREXIT("Premature EOF in JPEG file");
  c2 = NEXTBYTE();
  if (c2 == EOF)
    ERREXIT("Premature EOF in JPEG file");
  return (((unsigned int) c1) << 8) + ((unsigned int) c2);
}

static unsigned int
read_2_bytes_ordered (unsigned short order)
{
	int c1, c2, val;

	c1 = NEXTBYTE();
	if (c1 == EOF)
		ERREXIT("Premature EOF in JPEG file");
	c2 = NEXTBYTE();

	if (c2 == EOF)
		ERREXIT("Premature EOF in JPEG file");

	if (order == 0x4949)
		val = (((unsigned int) c2) << 8) + ((unsigned int) c1); 	// little endian
	else if (order == 0x4D4D)
		val = (((unsigned int) c1) << 8) + ((unsigned int) c2);		// big endian
	else
		ERREXIT("Unknown byte order");
	return val;
}

/* Read 4 bytes, convert to unsigned int */
static unsigned int
read_4_bytes_ordered (unsigned short order)
{
	int i, c, val = 0;

	if (order == 0x4949)
		for(i = 0; i < 4; i++) {	// little endian less significant first
			c = NEXTBYTE();
			if (c == EOF)
				ERREXIT("Premature EOF in JPEG file");
			val += c << (8*i);
		}
	else if (order == 0x4D4D)
		for(i = 3; i >= 0; i--) {	// big endian most significant first
			c = NEXTBYTE();
			if (c == EOF)
				ERREXIT("Premature EOF in JPEG file");
			val += c << (8*i);
		}
	else
		ERREXIT("Unknown byte order");

	return val;
}


void DumpValues(int off) {
	int i, v;
	fseek( infile, StartTiffHeader + off, SEEK_SET);

	printf("Current: %ld\n", ftell( infile));
	for(i = 0; i < 100; i++) {
		if (ftell( infile) % 10 == 0)
			printf("\n%06ld: ", ftell(infile));

		 v = getc(infile);
		 if (isprint( v))
			 printf(" %c  ", v);
		 else
			 printf("%03d ", v);
	}
	printf("\n");
}

static char * read_rationalstr(long value, unsigned short order)
{
	int num, den;
	static char val[64];

	long cur = ftell( infile);

	fseek( infile, value, SEEK_SET);
	den = read_4_bytes_ordered(order);
	num = read_4_bytes_ordered(order);

	fseek(infile, cur, SEEK_SET);

	sprintf( val, "%d/%d", num, den);
	return val;
}

static double read_rational(long value, unsigned short order)
{
	int num, den;

	long cur = ftell( infile);

	fseek( infile, value, SEEK_SET);
	den = read_4_bytes_ordered(order);
	num = read_4_bytes_ordered(order);

	fseek(infile, cur, SEEK_SET);

	return (double) num / (double) den;
}


static char * read_str(long off)
{
	char *c;
	long cur;
	static char val[250];

	if (off |= 0) {
		cur = ftell( infile); // save current position
		fseek( infile, off, SEEK_SET);
	}

	c = val;
	do {
		*c = getc( infile);

	} while ( *c++ > 0);

	if (off != 0) {
		fseek(infile, cur, SEEK_SET);
	}
	return val;
}



/*
 * Find the next JPEG marker and return its marker code.
 * We expect at least one FF byte, possibly more if the compressor used FFs
 * to pad the file.
 * There could also be non-FF garbage between markers.  The treatment of such
 * garbage is unspecified; we choose to skip over it but emit a warning msg.
 * NB: this routine must not be used after seeing SOS marker, since it will
 * not deal correctly with FF/00 sequences in the compressed image data...
 */

static int
next_marker (void)
{
  int c;
  int discarded_bytes = 0;

  /* Find 0xFF byte; count and skip any non-FFs. */
  c = read_1_byte();
  while (c != 0xFF) {
    discarded_bytes++;
    c = read_1_byte();
  }
  /* Get marker code byte, swallowing any duplicate FF bytes.  Extra FFs
   * are legal as pad bytes, so don't count them in discarded_bytes.
   */
  do {
    c = read_1_byte();
  } while (c == 0xFF);


  if (discarded_bytes != 0) {
    fprintf(stderr, "Warning: garbage data found in JPEG file\n");
  }

  return c;
}


/*
 * Read the initial marker, which should be SOI.
 * For a JFIF file, the first two bytes of the file should be literally
 * 0xFF M_SOI.  To be more general, we could use next_marker, but if the
 * input file weren't actually JPEG at all, next_marker might read the whole
 * file and then return a misleading error message...
 */

static int
first_marker (void)
{
  int c1, c2;

  c1 = NEXTBYTE();
  c2 = NEXTBYTE();
  if (c1 != 0xFF || c2 != M_SOI)
    ERREXIT("Not a JPEG file");

  TRACEMS(3, JTRC_SOI);
  return c2;
}


/*
 * Most types of marker are followed by a variable-length parameter segment.
 * This routine skips over the parameters for any marker we don't otherwise
 * want to process.
 * Note that we MUST skip the parameter segment explicitly in order not to
 * be fooled by 0xFF bytes that might appear within the parameter segment;
 * such bytes do NOT introduce new markers.
 */

static boolean
skip_variable (int marker)
/* Skip over an unknown or uninteresting variable-length marker */
{
  unsigned int length;

  /* Get the marker parameter length count */
  length = read_2_bytes();
  /* Length includes itself, so must be at least 2 */
  if (length < 2)
    ERREXIT("Erroneous JPEG marker length");
  length -= 2;

  TRACEMS2(3, JTRC_SKIPPED_MARKER, marker, (int) length);

  /* Skip over the remaining bytes */
  while (length > 0) {
    (void) read_1_byte();
    length--;
  }
  return TRUE;
}


/*
 * Process a COM marker.
 * We want to print out the marker contents as legible text;
 * we must guard against non-text junk and varying newline representations.
 */

static void
process_COM (void)
{
  unsigned int length;
  int ch;
  int lastch = 0;

  /* Get the marker parameter length count */
  length = read_2_bytes();
  /* Length includes itself, so must be at least 2 */
  if (length < 2)
    ERREXIT("Erroneous JPEG marker length");
  length -= 2;

  while (length > 0) {
    ch = read_1_byte();
    /* Emit the character in a readable form.
     * Nonprintables are converted to \nnn form,
     * while \ is converted to \\.
     * Newlines in CR, CR/LF, or LF form will be printed as one newline.
     */
    if (ch == '\r') {
      printf("\n");
    } else if (ch == '\n') {
      if (lastch != '\r')
	printf("\n");
    } else if (ch == '\\') {
      printf("\\\\");
    } else if (isprint(ch)) {
      putc(ch, stdout);
    } else {
      printf("\\%03o", ch);
    }
    lastch = ch;
    length--;
  }
  printf("\n");
}


/*
 * Process a SOFn marker.
 * This code is only needed if you want to know the image dimensions...
 */

static boolean get_sof (int marker)
{
  unsigned int length;
  unsigned int image_height, image_width;
  int component_id, h_samp_factor, v_samp_factor, quant_tbl_no;
  int data_precision, num_components;
  const char * process;
  int c, ci;

  length = read_2_bytes();	/* usual parameter length count */

  data_precision = read_1_byte();
  image_height = read_2_bytes();
  image_width = read_2_bytes();
  num_components = read_1_byte();

  switch (marker) {
	  case M_SOF0:	process = "Baseline";  break;
	  case M_SOF1:	process = "Extended sequential";  break;
	  case M_SOF2:	process = "Progressive";  break;
	  case M_SOF3:	process = "Lossless";  break;
	  case M_SOF5:	process = "Differential sequential";  break;
	  case M_SOF6:	process = "Differential progressive";  break;
	  case M_SOF7:	process = "Differential lossless";  break;
	  case M_SOF9:	process = "Extended sequential, arithmetic coding";  break;
	  case M_SOF10:	process = "Progressive, arithmetic coding";  break;
	  case M_SOF11:	process = "Lossless, arithmetic coding";  break;
	  case M_SOF13:	process = "Differential sequential, arithmetic coding";  break;
	  case M_SOF14:	process = "Differential progressive, arithmetic coding"; break;
	  case M_SOF15:	process = "Differential lossless, arithmetic coding";  break;
	  default:	process = "Unknown";  break;
  }

  printf("JPEG image is %uw * %uh, %d color components, %d bits per sample\n",
	 image_width, image_height, num_components, data_precision);
  printf("JPEG process: %s\n", process);
  TRACEMS4( 1, JTRC_SOF, marker, image_width, image_height, num_components);
  if (length != (unsigned int) (8 + num_components * 3))
    ERREXIT("Bogus SOF marker length");

  /* We don't support files in which the image height is initially specified */
  /* as 0 and is later redefined by DNL.  As long as we have to check that,  */
  /* might as well have a general sanity check. */
  if (image_height <= 0 || image_width <= 0 || num_components <= 0)
     ERREXIT0(JERR_EMPTY_IMAGE);

  for (ci = 0; ci < num_components; ci++) {
	component_id = read_1_byte();	/* Component ID code */
    c = read_1_byte();				/* H, V sampling factors */
    h_samp_factor = (c >> 4) & 15;
    v_samp_factor = (c     ) & 15;
    quant_tbl_no = read_1_byte();	/* Quantization table number */

    TRACEMS4( 1, JTRC_SOF_COMPONENT, component_id, h_samp_factor, v_samp_factor, quant_tbl_no);
  }
  return TRUE;
}



/* Process a SOS marker */
boolean get_sos ()
{
  INT32 length;
  int i, ci, n, c, cc, Ss, Se, Ah, Al;
  int dc_tbl_no;
  int ac_tbl_no;

  length = read_2_bytes();

  n = read_1_byte(); 		/* Number of components */

  TRACEMS1( 3, JTRC_SOS, n);

  if (length != (n * 2 + 6) || n < 1 || n > MAX_COMPS_IN_SCAN)
    ERREXIT0(JERR_BAD_LENGTH);


  /* Collect the component-spec parameters */
  for (i = 0; i < n; i++) {
    cc = NEXTBYTE(); // component ID
    c  = NEXTBYTE();

//    cinfo->cur_comp_info[i] = compptr;
    dc_tbl_no = (c >> 4) & 15;
    ac_tbl_no = (c     ) & 15;

    TRACEMS3( 1, JTRC_SOS_COMPONENT, cc, dc_tbl_no, ac_tbl_no);
  }

  /* Collect the additional scan parameters Ss, Se, Ah/Al. */
  Ss = NEXTBYTE(); // Ss
  Se = NEXTBYTE(); // Se
  c = NEXTBYTE(); // Ah/Al
  Ah = (c >> 4) & 15;
  Al = (c     ) & 15;

  TRACEMS4( 1, JTRC_SOS_PARAMS, Ss, Se, Ah, Al);
  return TRUE;
}


#ifdef D_ARITH_CODING_SUPPORTED

LOCAL(boolean)
get_dac (j_decompress_ptr cinfo)
/* Process a DAC marker */
{
  INT32 length;
  int index, val;
  INPUT_VARS(cinfo);

  INPUT_2BYTES(cinfo, length, return FALSE);
  length -= 2;

  while (length > 0) {
    INPUT_BYTE(cinfo, index, return FALSE);
    INPUT_BYTE(cinfo, val, return FALSE);

    length -= 2;

    TRACEMS2(cinfo, 1, JTRC_DAC, index, val);

    if (index < 0 || index >= (2*NUM_ARITH_TBLS))
      ERREXIT1(cinfo, JERR_DAC_INDEX, index);

    if (index >= NUM_ARITH_TBLS) { /* define AC table */
      cinfo->arith_ac_K[index-NUM_ARITH_TBLS] = (UINT8) val;
    } else {			/* define DC table */
      cinfo->arith_dc_L[index] = (UINT8) (val & 0x0F);
      cinfo->arith_dc_U[index] = (UINT8) (val >> 4);
      if (cinfo->arith_dc_L[index] > cinfo->arith_dc_U[index])
	ERREXIT1(cinfo, JERR_DAC_VALUE, val);
    }
  }

  if (length != 0)
    ERREXIT(cinfo, JERR_BAD_LENGTH);

  INPUT_SYNC(cinfo);
  return TRUE;
}

#else /* ! D_ARITH_CODING_SUPPORTED */

#define get_dac(marker)  skip_variable(marker)

#endif /* D_ARITH_CODING_SUPPORTED */


/* Process a DHT marker */
static boolean get_dht ()
{
  INT32 length;
  UINT8 bits[17];
  UINT8 huffval[256];
  int i, index, count;

  length = read_2_bytes();
  length -= 2;

  while (length > 16) {
    index = NEXTBYTE();

    TRACEMS1( 3, JTRC_DHT, index);

    bits[0] = 0;
    count = 0;
    for (i = 1; i <= 16; i++) {
      bits[i] = NEXTBYTE();
      count += bits[i];
    }

    length -= 1 + 16;

    TRACEMS8(1, JTRC_HUFFBITS,
	     bits[1], bits[2], bits[3], bits[4],
	     bits[5], bits[6], bits[7], bits[8]);
    TRACEMS8( 1, JTRC_HUFFBITS,
	     bits[9], bits[10], bits[11], bits[12],
	     bits[13], bits[14], bits[15], bits[16]);
    printf("\n");

    /* Here we just do minimal validation of the counts to avoid walking
     * off the end of our table space.  jdhuff.c will check more carefully.
     */
    if (count > 256 || ((INT32) count) > length)
      ERREXIT0(JERR_BAD_HUFF_TABLE);

    for (i = 0; i < count; i++)
      huffval[i] = NEXTBYTE();

    length -= count;

    if (index & 0x10) {		/* AC table definition */
      index -= 0x10;
//      htblptr = &cinfo->ac_huff_tbl_ptrs[index];
    } else {			/* DC table definition */
//      htblptr = &cinfo->dc_huff_tbl_ptrs[index];
    }

    if (index < 0 || index >= NUM_HUFF_TBLS)
      ERREXIT1(JERR_DHT_INDEX, index);
  }

  if (length != 0)
    ERREXIT0(JERR_BAD_LENGTH);
  return TRUE;
}


/* Process a DQT marker */
static boolean get_dqt ()
{
  INT32 length;
  int n, i, prec;
  unsigned int tmp;
  unsigned int table[DCTSIZE2];

  length = read_2_bytes();
  length -= 2;

  while (length > 0) {
    n = NEXTBYTE();
    prec = n >> 4;
    n &= 0x0F;

    TRACEMS2(3, JTRC_DQT, n, prec);

    if (n >= NUM_QUANT_TBLS)
      ERREXIT1(JERR_DQT_INDEX, n);

    for (i = 0; i < DCTSIZE2; i++) {
      if (prec)
    	  tmp = read_2_bytes();
      else
    	  tmp = NEXTBYTE();
      /* We convert the zigzag-order table to natural array order. */
      table[jpeg_natural_order[i]] = (UINT16) tmp;
    }

	for (i = 0; i < DCTSIZE2; i += 8) {
	TRACEMS8(1, JTRC_QUANTVALS,
		 table[i],   table[i+1],	 table[i+2], table[i+3],
		 table[i+4], table[i+5],	 table[i+6], table[i+7]);
    }
	printf("\n");
    length -= DCTSIZE2+1;
    if (prec) length -= DCTSIZE2;
  }

  if (length != 0)
    ERREXIT0(JERR_BAD_LENGTH);

  return TRUE;
}


/* Process a DRI marker */
static boolean get_dri ()
{
  INT32 length;
  unsigned int tmp;

  length = NEXTBYTE();

  if (length != 4)
    ERREXIT0(JERR_BAD_LENGTH);

  tmp = read_2_bytes();

  TRACEMS1(3, JTRC_DRI, tmp);
  return TRUE;
}


/*
 * Routines for processing APPn and COM markers.
 * These are either saved in memory or discarded, per application request.
 * APP0 and APP14 are specially checked to see if they are
 * JFIF and Adobe markers, respectively.
 */

#define APP0_DATA_LEN	14	/* Length of interesting data in APP0 */
#define APP14_DATA_LEN	12	/* Length of interesting data in APP14 */
#define APPN_DATA_LEN	14	/* Must be the largest of the above!! */


/* Examine first few bytes from an APP0.
 * Take appropriate action if it is a JFIF marker.
 * datalen is # of bytes at data[], remaining is length of rest of marker data.
 */
static void examine_app0 (JOCTET FAR * data,
	      unsigned int datalen, INT32 remaining)
{
  INT32 totallen = (INT32) datalen + remaining;
  int JFIF_major_version, JFIF_minor_version;
  int density_unit, X_density, Y_density;

  if (datalen >= APP0_DATA_LEN &&
      GETJOCTET(data[0]) == 0x4A &&
      GETJOCTET(data[1]) == 0x46 &&
      GETJOCTET(data[2]) == 0x49 &&
      GETJOCTET(data[3]) == 0x46 &&
      GETJOCTET(data[4]) == 0) {
    /* Found JFIF APP0 marker: save info */

    JFIF_major_version = GETJOCTET(data[5]);
    JFIF_minor_version = GETJOCTET(data[6]);
    density_unit = GETJOCTET(data[7]);
    X_density = (GETJOCTET(data[8]) << 8) + GETJOCTET(data[9]);
    Y_density = (GETJOCTET(data[10]) << 8) + GETJOCTET(data[11]);
    /* Check version.
     * Major version must be 1, anything else signals an incompatible change.
     * (We used to treat this as an error, but now it's a nonfatal warning,
     * because some bozo at Hijaak couldn't read the spec.)
     * Minor version should be 0..2, but process anyway if newer.
     */
    if (JFIF_major_version != 1)
      WARNMS2(JWRN_JFIF_MAJOR, JFIF_major_version,JFIF_minor_version);
    /* Generate trace messages */
    TRACEMS5( 1, JTRC_JFIF, JFIF_major_version, JFIF_minor_version,
	     X_density, Y_density, density_unit);
    /* Validate thumbnail dimensions and issue appropriate messages */
    if (GETJOCTET(data[12]) | GETJOCTET(data[13]))
      TRACEMS2( 1, JTRC_JFIF_THUMBNAIL,
	       GETJOCTET(data[12]), GETJOCTET(data[13]));
    totallen -= APP0_DATA_LEN;
    if (totallen !=	((INT32)GETJOCTET(data[12]) * (INT32)GETJOCTET(data[13]) * (INT32) 3))
      TRACEMS1(1, JTRC_JFIF_BADTHUMBNAILSIZE, (int) totallen);
	 } else if (datalen >= 6 &&
      GETJOCTET(data[0]) == 0x4A &&
      GETJOCTET(data[1]) == 0x46 &&
      GETJOCTET(data[2]) == 0x58 &&
      GETJOCTET(data[3]) == 0x58 &&
      GETJOCTET(data[4]) == 0) {
    /* Found JFIF "JFXX" extension APP0 marker */
    /* The library doesn't actually do anything with these,
     * but we try to produce a helpful trace message.
     */
    switch (GETJOCTET(data[5])) {
    case 0x10:
      TRACEMS1( 1, JTRC_THUMB_JPEG, (int) totallen);
      break;
    case 0x11:
      TRACEMS1( 1, JTRC_THUMB_PALETTE, (int) totallen);
      break;
    case 0x13:
      TRACEMS1( 1, JTRC_THUMB_RGB, (int) totallen);
      break;
    default:
      TRACEMS2( 1, JTRC_JFIF_EXTENSION,
	       GETJOCTET(data[5]), (int) totallen);
      break;
    }
  } else {
    /* Start of APP0 does not match "JFIF" or "JFXX", or too short */
    TRACEMS1( 1, JTRC_APP0, (int) totallen);
  }
}

// stampa tutti i tag di una setione del segmento APP1 (Exif)
static int printExifTags(int nfields, unsigned short order) {
	int i, k, pos, ExifPtr;
	int tag, type, count, value;
	char *s;

	for(i = 0; i < nfields; i++) {
		pos = ftell(infile) - StartTiffHeader;
		tag = read_2_bytes_ordered(order);
		type = read_2_bytes_ordered(order);
		count = read_4_bytes_ordered(order);
		value = read_4_bytes_ordered(order);

		if (verbose > 1)
		  printf("[%2d]: Offset=0x%04x - tag=0x%04x - type=%2d - count=%3d -> ", i, pos, tag, type, count);
		printf("%25s: ", getFieldName( tag));

		switch (type) {
		  case 1:	case 2:
			  s = read_str(StartTiffHeader + value);
			  printf("%s\n", s);
			  break;

		  case 3:	case 4:	case 9:
			  printf("%d\n", value);
			  break;

		  case 5:  case 10:
			  if (count == 1) {
				  s = read_rationalstr(StartTiffHeader + value, order);
				  printf("%s\n", s);
			  }
			  else {
				  for(k = 0; k < count; k++) {
					  printf("%.3f ", read_rational(StartTiffHeader + value + 8 * k, order));
				  }
				  printf("\n");
			  }
			  break;

		  default:
			  printf("0x%x (unknown type)\n", value);
			  break;
		}
		if (tag == 0x8769)
		  ExifPtr = value;
	}
	printf("\n");
	return ExifPtr; // returns last value
}


// process APP1 section
// cerca i dati EXIF
static boolean get_exiff_info (int marker)
{
	  INT32 length;
	  unsigned short order, id;
	  unsigned int Ptr0IFD, Ptr1IFD, ExifPtr, nextSegment;
	  int nfields, len;
	  char *s;

	  length = read_2_bytes();
	  length -= 2;

	  nextSegment = ftell(infile) + length;

	  if (length < 8)
	    ERREXIT("Bogus APP1 marker length");

	  s = read_str(0);
	  if (strcmp(s, "Exif") == 0)
		  read_1_byte();	// read one pad char

	  StartTiffHeader = ftell( infile);

	  order = read_2_bytes();
	  id = read_2_bytes_ordered(order);
	  if (id != 0x2A)
		    ERREXIT("Wrong TIFF Header");

	  Ptr0IFD = read_4_bytes_ordered(order);
	  nfields = read_2_bytes_ordered(order);
	  if (verbose > 1) {
		  printf("APP1 frame - Len:%d, Exif ID Code: %s\n", length, s);
		  printf("TIFF Header: Order: 0x%x, id:0x%x, IFD Offset: 0x%08x (%d)\n", order, id, Ptr0IFD, Ptr0IFD);
		  printf("Number of fields: %d\n", nfields);
	  }

	  ExifPtr = printExifTags( nfields, order);

	  Ptr1IFD = read_2_bytes_ordered(order);
	  fseek(infile, StartTiffHeader+ExifPtr, SEEK_SET);
	  nfields = read_2_bytes_ordered(order);

	  if (verbose > 1)
		  printf("Number of fields (Exif IFD): %d\n", nfields);

	  printExifTags( nfields, order);

	  len = read_4_bytes_ordered(order); // should be zero
	  if (len != 0)
		  ERREXIT("Too many IFDs");

	  if (Ptr1IFD > 0) {
		  fseek(infile, StartTiffHeader+Ptr1IFD, SEEK_SET);
		  nfields = read_2_bytes_ordered(order);
		  if (verbose > 1)
			  printf("Number of fields (1st IFD): %d\n", nfields);
		  printExifTags( nfields, order);

		  len = read_4_bytes_ordered(order); // should be zero
		  if (len != 0)
			  ERREXIT("Too many IFDs");

			  // thumbnail Image data
	  }
	  fseek( infile, nextSegment, SEEK_SET);
	  return TRUE;
}

/* Examine first few bytes from an APP14.
 * Take appropriate action if it is an Adobe marker.
 * datalen is # of bytes at data[], remaining is length of rest of marker data.
 */
static void examine_app14 (JOCTET FAR * data,
	       unsigned int datalen, INT32 remaining)
{
  unsigned int version, flags0, flags1, transform;

  if (datalen >= APP14_DATA_LEN &&
      GETJOCTET(data[0]) == 0x41 &&
      GETJOCTET(data[1]) == 0x64 &&
      GETJOCTET(data[2]) == 0x6F &&
      GETJOCTET(data[3]) == 0x62 &&
      GETJOCTET(data[4]) == 0x65) {
    /* Found Adobe APP14 marker */
    version = (GETJOCTET(data[5]) << 8) + GETJOCTET(data[6]);
    flags0 = (GETJOCTET(data[7]) << 8) + GETJOCTET(data[8]);
    flags1 = (GETJOCTET(data[9]) << 8) + GETJOCTET(data[10]);
    transform = GETJOCTET(data[11]);
    TRACEMS4( 1, JTRC_ADOBE, version, flags0, flags1, transform);
  } else {
    /* Start of APP14 does not match "Adobe", or too short */
    TRACEMS1(1, JTRC_APP14, (int) (datalen + remaining));
  }
}


/* Process an APP0 or APP14 marker without saving it */
static boolean get_interesting_appn (int marker)
{
  INT32 length;
  JOCTET b[APPN_DATA_LEN];
  unsigned int i, numtoread;

  length = read_2_bytes();;
  length -= 2;

  /* get the interesting part of the marker data */
  if (length >= APPN_DATA_LEN)
    numtoread = APPN_DATA_LEN;
  else if (length > 0)
    numtoread = (unsigned int) length;
  else
    numtoread = 0;
  for (i = 0; i < numtoread; i++)
    b[i] = NEXTBYTE();
  length -= numtoread;

  /* process it */
  switch (marker) {
  case M_APP0:
    examine_app0((JOCTET FAR *) b, numtoread, length);
    break;
  case M_APP14:
    examine_app14((JOCTET FAR *) b, numtoread, length);
    break;
  default:
    /* can't get here unless jpeg_save_markers chooses wrong processor */
    ERREXIT1(JERR_UNKNOWN_MARKER, marker);
    break;
  }

  return TRUE;
}


/*
 * Read markers until SOS or EOI.
 *
 * Returns same codes as are defined for jpeg_consume_input:
 * JPEG_SUSPENDED, JPEG_REACHED_SOS, or JPEG_REACHED_EOI.
 */
int read_markers ()
{
	int marker;

	/* Expect SOI at start of file */
	if (first_marker() != M_SOI)
		ERREXIT("Expected SOI marker first");

	/* Outer loop repeats once for each marker. */
	/* Scan miscellaneous markers until we reach SOS. */
	for (;;) {
		/* Collect the marker proper, unless we already did. */
		/* NB: first_marker() enforces the requirement that SOI appear first. */
		marker = next_marker();

		/* At this point marker contains the marker code and the
		 * input point is just past the marker proper, but before any parameters.
		 * A suspension will cause us to return with this state still true.
		 */

		switch (marker) {
		case M_SOI:			/* Start Of Image (beginning of datastream) */
			ERREXIT("Duplicated SOI marker");
			break;


		case M_SOF0:		/* Baseline  Start Of Frame N */
							/* N indicates which compression process */
							/* Only SOF0-SOF2 are now in common use */
							/* NB: codes C4 and CC are NOT SOF markers */
		case M_SOF1:		/* Extended sequential, Huffman */
		case M_SOF2:		/* Progressive, Huffman */
		case M_SOF3:		/* Lossless, Huffman */
		case M_SOF9:		/* Extended sequential, arithmetic */
		case M_SOF10:		/* Progressive, arithmetic */
		case M_SOF11:		/* Lossless, arithmetic */
			if (ShowSects & SHOWSOF) {
				if (! get_sof( marker))
					ERREXIT("Fail reading SOF marker");
			}
			else
				skip_variable( marker);
			break;

		/* Currently unsupported SOFn types */
		case M_SOF5:		/* Differential sequential, Huffman */
		case M_SOF6:		/* Differential progressive, Huffman */
		case M_SOF7:		/* Differential lossless, Huffman */
		case M_JPG:			/* Reserved for JPEG extensions */
		case M_SOF13:		/* Differential sequential, arithmetic */
		case M_SOF14:		/* Differential progressive, arithmetic */
		case M_SOF15:		/* Differential lossless, arithmetic */
		  ERREXIT1(JERR_SOF_UNSUPPORTED, marker);
		  break;

		case M_SOS:			/* Start Of Scan (begins compressed data) */
			if (ShowSects & SHOWSOS) {
				if (! get_sos())
					ERREXIT("Fail reading SOS marker");
			}
			if (verbose)
				ERREXIT("SOS encountered. Exiting");
			else
				return;
			break;

		case M_EOI:			/* End Of Image (end of datastream) */
		  if (verbose) {
			  TRACEMS( 3, JTRC_EOI);
			  ERREXIT("EOI encountered. Exiting");
		  }
		  else
			  return;

		case M_DAC:
			if (ShowSects & SHOWDAC) {
				if (! get_dac( marker))
					ERREXIT("Fail reading DAC marker");
			}
			else
				skip_variable( marker);
			break;

		case M_DHT:
			if (ShowSects & SHOWDHT) {
				if (! get_dht())
					ERREXIT("Fail reading DHT marker");
			}
			else
				skip_variable( marker);
			break;

		case M_DQT:
			if (ShowSects & SHOWDQT) {
				if (! get_dqt())
					ERREXIT("Fail reading DQT marker");
			}
			else
				skip_variable( marker);
			break;

		case M_DRI:
			if (ShowSects & SHOWDRI) {
				if (! get_dri())
					ERREXIT("Fail reading DRI marker");
			}
			else
				skip_variable( marker);
			break;

		case M_APP0:		/* Application-specific marker, type N */
		case M_APP14:
			if (ShowSects & SHOWAPP) {
				if (! get_interesting_appn( marker))
					ERREXIT("Fail reading APP0-APP14 marker");
			}
			else
				skip_variable( marker);
			break;

		case M_APP1:
			if (ShowSects & SHOWEXIF) {
				if (! get_exiff_info(marker))
					ERREXIT("Fail reading APP1 (EXIFF) marker");
			}
			else
				skip_variable( marker);
			break;

		case M_APP2:
		case M_APP3:
		case M_APP4:
		case M_APP5:
		case M_APP6:
		case M_APP7:
		case M_APP8:
		case M_APP9:
		case M_APP10:
		case M_APP11:
		case M_APP13:
		case M_APP15:
			if (verbose) {
				TRACEMS1(3, JERR_UNKNOWN_MARKER, marker);
			}
			if (! skip_variable(marker))
					ERREXIT("Fail skipping APPn marker");
			break;

		  /* Some digital camera makers put useful textual information into
		   * APP12 markers, so we print those out too when in -verbose mode.
		   */
		case M_APP12:
			if (ShowSects & SHOWCOM) {
				printf("Processing Marker Application 12 (0x%x) marker (only some cameras put informations in this section)\n", marker);
				process_COM();
			}
			else
				skip_variable( marker);
			break;

		case M_COM:			/* COMment */
			if (ShowSects & SHOWCOM) {
		  	  printf("Processing Comment (0x%x) marker\n", marker);
		      process_COM();
			}
			else
				skip_variable( marker);
		    break;

		case M_RST0:		/* these are all parameterless */
		case M_RST1:
		case M_RST2:
		case M_RST3:
		case M_RST4:
		case M_RST5:
		case M_RST6:
		case M_RST7:
		case M_TEM:
			if (verbose)
				TRACEMS1(3, JTRC_PARMLESS_MARKER, marker);
			break;

		case M_DNL:			/* Ignore DNL ... perhaps the wrong thing */
				if (! skip_variable(marker))
					ERREXIT("Fail skipping M_DNL marker");
				break;

		default:			/* must be DHP, EXP, JPGn, or RESn */
		  /* For now, we treat the reserved markers as fatal errors since they are
		   * likely to be used to signal incompatible JPEG Part 3 extensions.
		   * Once the JPEG 3 version-number marker is well defined, this code
		   * ought to change!
		   */
			if (verbose)
				TRACEMS1(3, JERR_UNKNOWN_MARKER, marker);
			break;
		}
		/* Successfully processed marker, so reset state variable */
	} /* end loop */
}


/* Command line parsing code */

static const char * progname;	/* program name for error messages */


static void
usage (void)
/* complain about bad command line */
{
  fprintf(stderr, "%s displays any textual comments in a JPEG file.\n", progname);

  fprintf(stderr, "Usage: %s [switches] [inputfile]\n", progname);

  fprintf(stderr, "Switches (names may be abbreviated):\n");
  fprintf(stderr, "  -verbose    Also display dimensions of JPEG image\n");

  exit(EXIT_FAILURE);
}


static int
keymatch (char * arg, const char * keyword, int minchars)
/* Case-insensitive matching of (possibly abbreviated) keyword switches. */
/* keyword is the constant keyword (must be lower case already), */
/* minchars is length of minimum legal abbreviation. */
{
  register int ca, ck;
  register int nmatched = 0;

  while ((ca = *arg++) != '\0') {
    if ((ck = *keyword++) == '\0')
      return 0;			/* arg longer than keyword, no good */
    if (isupper(ca))		/* force arg to lcase (assume ck is already) */
      ca = tolower(ca);
    if (ca != ck)
      return 0;			/* no good */
    nmatched++;			/* count matched characters */
  }
  /* reached end of argument; fail if it's too short for unique abbrev */
  if (nmatched < minchars)
    return 0;
  return 1;			/* A-OK */
}


/*
 * The main program.
 */

int
main (int argc, char **argv)
{
  int argn;
  char * arg;

  progname = argv[0];
  if (progname == NULL || progname[0] == 0)
    progname = "rdjpgsects";	/* in case C library doesn't provide it */

  /* Parse switches, if any */
  for (argn = 1; argn < argc; argn++) {
    arg = argv[argn];
    if (arg[0] != '-')
      break;			/* not switch, must be file name */
    arg++;			/* advance over '-' */
    if (keymatch(arg, "all", 1)) {
     	ShowSects = SHOWALL;
    }
    else if (keymatch(arg, "verbose", 1)) {
     	verbose = 9;
    }
    else if (keymatch(arg, "sof", 1)) {
     	ShowSects |= SHOWSOF;
    }
    else if (keymatch(arg, "dac", 1)) {
     	ShowSects |= SHOWDAC;
    }
    else if (keymatch(arg, "exif", 1)) {
     	ShowSects |= SHOWEXIF;
    }
    else if (keymatch(arg, "dqt", 1)) {
     	ShowSects |= SHOWDQT;
    }
    else if (keymatch(arg, "dht", 1)) {
     	ShowSects |= SHOWDHT;
    }
    else
      usage();
  }

  /* Open the input file. */
  /* Unix style: expect zero or one file name */
  if (argn < argc-1) {
    fprintf(stderr, "%s: only one input file\n", progname);
    usage();
  }
  if (argn < argc) {
    if ((infile = fopen(argv[argn], READ_BINARY)) == NULL) {
      fprintf(stderr, "%s: can't open %s\n", progname, argv[argn]);
      exit(EXIT_FAILURE);
    }
  } else {
    /* default input file is stdin */
#ifdef USE_SETMODE		/* need to hack file mode? */
    setmode(fileno(stdin), O_BINARY);
#endif
#ifdef USE_FDOPEN		/* need to re-open in binary mode? */
    if ((infile = fdopen(fileno(stdin), READ_BINARY)) == NULL) {
      fprintf(stderr, "%s: can't open stdin\n", progname);
      exit(EXIT_FAILURE);
    }
#else
    infile = stdin;
#endif
  }

  /* Scan the JPEG headers. */
  (void) read_markers();

  /* All done. */
  exit(EXIT_SUCCESS);
  return 0;			/* suppress no-return-value warnings */
}
