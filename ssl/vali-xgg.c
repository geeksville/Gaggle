/*
 * Mainly based on code from Tom Payne licenced under GPL3
 * 
 * Modified by Marc Poulhiès (GPLv3)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#include <openssl/sha.h>
#include <openssl/bio.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/err.h>

#include <string.h>
#include <openssl/hmac.h>
#include <openssl/evp.h>
#include <openssl/buffer.h>

#define G_RECORD_LEN 1024

#ifdef DEBUG
#define DEBUG_PRINTF(args...) printf(args)
#else
#define DEBUG_PRINTF(args...) 
#endif

#define SUCCESS() printf("PASSED\n")
#define FAILURE() printf("FAILED\n")

/*
 * The **PUBLIC** key in PEM format
 */

unsigned char pkey_b64[] = "-----BEGIN PUBLIC KEY-----\n"
  "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdklbAnU6nves/VuGJCJqodAen\n"
  "w18Pp3Ol2kQ1Japy6YyA7NgSDwCi7v/EFko+eL7dEUQGMb46+If4G/4ugy1HHiwg\n"
  "h2FdxhiTwESrVBPpFSh8qJRKLA+6Mp6gJs0sZS0Fb4r0mlfT7hPeu1MzKfUdIFcv\n"
  "O4PCtlMBnf+cxpKmkwIDAQAB\n"
  "-----END PUBLIC KEY-----\n";

int parse_line(const char *s)
{
	while (*s && *s != '\n')
		++s;
	if (*s != '\n')
		return -1;
	++s;
	if (*s)
		return -1;
	return 0;
}

int unbase64(unsigned char *input, int input_len, unsigned char*output, int output_len) {
  BIO *b64, *bmem;
  int size;

  /* char *buffer = malloc(length); */
  memset(output, 0, output_len);

  b64 = BIO_new(BIO_f_base64());
  BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
  bmem = BIO_new_mem_buf(input, input_len);
  BIO_push(b64, bmem);

  size = BIO_read(b64, output, output_len);
  BIO_free_all(b64);
  return size;
}

int parse_g_record(const char *s, unsigned char *g_record, int *g_record_size)
{
  DEBUG_PRINTF("s: [%s]\n", s);
  DEBUG_PRINTF("parse_g_record: [%s]\n", g_record);
	if (*s++ != 'G')
		return -1;
        DEBUG_PRINTF("copying (%zu)... ", strlen(s));
        int l = strlen(s);
	for (int i = 0; i < l; i++){
          DEBUG_PRINTF("[%c", *s);
	  if (*s != '\r' && *s != '\n' && *s){
	    g_record[*g_record_size] = *s;
	    ++(*g_record_size);
            DEBUG_PRINTF(":1]\n");
	  } else {
            DEBUG_PRINTF(":0]\n");
          }
	  ++s;
	}
        DEBUG_PRINTF("\n");

/* 	for (int i = 0; i < G_RECORD_LEN; ++i) { */
/* 		if ('0' <= *s && *s <= '9') */
/* 			*g_record = (*s - '0') << 4; */
/* 		else if ('A' <= *s && *s <= 'F') */
/* 			*g_record = (*s - 'A' + 0xA) << 4; */
/* 		else */
/* 			return -1; */
/* 		++s; */
/* 		if ('0' <= *s && *s <= '9') */
/* 			*g_record++ |= *s - '0'; */
/* 		else if ('A' <= *s && *s <= 'F') */
/* 			*g_record++ |= *s - 'A' + 0xA; */
/* 		else */
/* 			return -1; */
/* 		++s; */
/* 	} */
/* #if 0 */
/* 	if (*s++ != '\r') */
/* 		return -1; */
/* #endif */
/* 	if (*s++ != '\n') */
/* 		return -1; */
/* 	if (*s) */
/* 		return -1; */
	return 0;
}

int main(int argc, char *argv[])
{

	if (argc != 2) {
		fprintf(stderr, "Usage: %s filename\n", argv[0]);
		return EXIT_FAILURE;
	}

	FILE *file = 0;
	if (strcmp(argv[1], "-")) {
		file = fopen(argv[1], "r");
		if (!file)
			goto error;
	} else {
		file = stdin;
	}

	SHA_CTX ctx = {0};
	SHA1_Init(&ctx);

	/* HMAC_Init_ex(&ctx, key, key_len, EVP_sha256(), 0); */

	/* int g_records = 0; */
	unsigned char g_record[G_RECORD_LEN];
	int g_record_size = 0;
        g_record[0] = 0;

	char line[1024];
	while (fgets(line, sizeof line, file)) {
		if (parse_line(line))
			goto error;
		/* if (g_records) */
		/* 	goto error; */
		if (line[0] == 'H' && (line[1] == 'O' || line[1] == 'P')) {
                  DEBUG_PRINTF("skipping %s\n", line);
		} else if (line[0] == 'L' && strncmp(line, "LXGG", 4)) {
                  DEBUG_PRINTF("skipping %s\n", line);
		} else if (line[0] == 'G') {
		  if (parse_g_record(line, g_record, &g_record_size))
				goto error;
			/* ++g_records; */
		} else {
                  DEBUG_PRINTF ("using: %s\n", line);
                  unsigned long end_idx = (line[strlen(line)-2] == '\r') ? strlen(line)-2 : strlen(line)-1;

			SHA1_Update(&ctx, (unsigned char *) line, end_idx);
		}
	}
	if (!feof(file))
		goto error;

	g_record[g_record_size] = 0;

	// decode b64 signature
	unsigned char sig[G_RECORD_LEN];
	int sig_len;

        DEBUG_PRINTF("Sig 64: %s\n", g_record);
	sig_len = unbase64(g_record, g_record_size, sig, sizeof(sig));

	unsigned char digest[SHA_DIGEST_LENGTH];
	int rc = SHA1_Final(digest, &ctx);
	if (1 != rc) {  goto error; }

	BIO *b = NULL;
	RSA *r = NULL;
	b = BIO_new_mem_buf(pkey_b64, sizeof(pkey_b64));
	PEM_read_bio_RSA_PUBKEY(b, &r, NULL, NULL);

	rc = RSA_verify(NID_sha1, digest, sizeof (digest), sig, sig_len, r);
	// verification went well.
	if (NULL != r) RSA_free(r);
	if (NULL != b) BIO_free(b);

	if (rc != 1)
		goto error;

	DEBUG_PRINTF("Validation check passed, data indicated as correct\n");

        SUCCESS();

	return EXIT_SUCCESS;

error:
        FAILURE();

	DEBUG_PRINTF("Validation check failed\n");
	return EXIT_FAILURE;

}
